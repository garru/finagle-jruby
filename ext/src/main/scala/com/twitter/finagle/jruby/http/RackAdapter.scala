package com.twitter.finagle.jruby.http

import com.twitter.finagle.jruby.rack.IOLibrary._
import java.nio.charset.Charset
import org.jboss.netty.handler.codec.http._
import org.jboss.netty.buffer.{ChannelBuffer, ChannelBuffers}
import org.jruby.{Ruby, RubyArray, RubyEnumerable, RubyHash, RubyIO, RubyNumeric,RubyString}
import org.jruby.runtime._
import org.jruby.runtime.builtin.IRubyObject
import org.jruby.util.io.STDIO;
import scala.collection.JavaConversions._

object RackAdapter {
  def httpRequestToRackRequest(runtime: Ruby, request: HttpRequest): IRubyObject = {
    val headerNames = request.getHeaderNames()
    val env = RubyHash.newHash(runtime);
    val headers = headerNames.toSeq foreach { name =>
      env.put(name, request.getHeader(name).toString())
    }
    val finagle = runtime.getModule("Finagle");
    val rack = finagle.defineOrGetModuleUnder("Rack")
    val rackIO = rack.getClass("IO")
    val uri = request.getUri().split('?')
    val query = if (uri.size > 1) uri(1) else ""
    val stderr = new RubyIO(runtime, STDIO.ERR)

    env.put("rack.input", new RackIO(runtime, rackIO, request.getContent()))
    env.put("rack.error", stderr)
    env.put("REQUEST_METHOD", request.getMethod().toString())
    env.put("PATH_INFO", uri(0))
    env.put("QUERY_STRING", query)
    return env
  }

  def rackResponseToHttpResponse(response: RubyArray): HttpResponse = {
    val httpCode = response.entry(0).asInstanceOf[RubyNumeric]
    val headers = response.entry(1).asInstanceOf[RubyHash]
    val body = response.entry(2)
    val httpResponse = new DefaultHttpResponse(HttpVersion.HTTP_1_1, new HttpResponseStatus(RubyNumeric.num2int(httpCode), "FinagleRulz"))
    httpResponse.setContent(Util.bodyToBuffer(body))

    val iterator = headers.iterator
    while(iterator.hasNext()) {
      val (key, value) = iterator.next()
      value.toString.split("\n") foreach { value =>
        httpResponse.addHeader(key.toString(), value)
      }
    }

    httpResponse
  }
}
