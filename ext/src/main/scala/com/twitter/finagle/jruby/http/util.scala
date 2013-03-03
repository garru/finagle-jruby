package com.twitter.finagle.jruby.http

import com.twitter.finagle.rack.IOLibrary._
import com.twitter.finagle.http.ClientLibrary._
import com.twitter.finagle.RubyFutureLibrary._
import com.twitter.util.Future
import org.jboss.netty.handler.codec.http._
import org.jboss.netty.buffer.{ChannelBuffer, ChannelBuffers}
import org.jruby.{Ruby, RubyArray, RubyEnumerable, RubyHash, RubyNumeric,RubyString}
import org.jruby.runtime._
import org.jruby.runtime.builtin.IRubyObject
import scala.collection.JavaConversions._
import org.jboss.netty.util.CharsetUtil

object Util {
  def rubyHttpRequestToNettyHttpRequest(request: RubyRequest): HttpRequest = {
    val httpMethod = request.getHttpMethod().toString() match {
      case "connect"  => HttpMethod.CONNECT
      case "delete"   => HttpMethod.DELETE
      case "get"      => HttpMethod.GET
      case "head"     => HttpMethod.HEAD
      case "options"  => HttpMethod.OPTIONS
      case "post"     => HttpMethod.POST
      case "put"      => HttpMethod.PUT
      case "trace"    => HttpMethod.TRACE
      case _          => HttpMethod.GET
    }

    val nettyRequest = new DefaultHttpRequest(HttpVersion.HTTP_1_1, httpMethod, request.getUri().toString())

    val headers = request.getHeaders().asInstanceOf[RubyHash]

    val iterator = headers.iterator
    while(iterator.hasNext()) {
      val (key, value) = iterator.next()
      nettyRequest.addHeader(key.toString(), value.toString())
    }

    nettyRequest.setContent(bodyToBuffer(request.getContent()))
    nettyRequest
  }

  def bodyToBuffer(body: IRubyObject): ChannelBuffer = {
    val outBuffer = ChannelBuffers.dynamicBuffer()
    var callback = new BlockCallback(){
      def call(context: ThreadContext, args: Array[IRubyObject], block: Block): IRubyObject = {
        val line = args(0).asInstanceOf[RubyString]
        outBuffer.writeBytes(line.getBytes())
        body.getRuntime().getNil()
      }
    }
    RubyEnumerable.callEach(body.getRuntime(), body.getRuntime().getCurrentContext(), body, callback)
    outBuffer
  }

  def httpResponseToRubyHttpResponse(runtime: Ruby, response: Future[HttpResponse]): RubyFuture = {
    val fu = response map { response =>
      val status = response.getStatus()
      val headerNames = response.getHeaderNames()
      val env = RubyHash.newHash(runtime);
      val headers = headerNames.toSeq foreach { name =>
        env.put(name, response.getHeader(name).toString())
      }

      val elements: java.util.Collection[IRubyObject] = Array(RubyNumeric.dbl2num(runtime, status.getCode()), env, new RackIO(runtime, response.getContent())).toSeq
      RubyArray.newArray(runtime, elements)
    }
    new RubyFuture(runtime, fu)
  }

  def futureByteArrayToRubyFutureString(runtime: Ruby, response: Future[Array[Byte]]) = {
    val fu = response map { response =>
      RubyString.newStringNoCopy(runtime, response)
    }
    new RubyFuture(runtime, fu)
  }
}