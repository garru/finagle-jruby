package com.twitter.finagle.jruby.http

import com.twitter.finagle.RubyFutureLibrary.RubyFuture
import com.twitter.util.FuturePool
import org.jboss.netty.handler.codec.http._
import org.jruby.RubyArray
import org.jruby.runtime.builtin.IRubyObject


class RackProcessor(rackApp: IRubyObject) extends Processor {
  // val futurePool = FuturePool.defaultPool
  def apply(request: HttpRequest) = {
    val blocking = rackApp.callMethod(
        rackApp.getRuntime().getCurrentContext(),
        "call",
        RackAdapter.httpRequestToRackRequest(rackApp.getRuntime(), request)
      ).asInstanceOf[RubyFuture]

    blocking.getUnderlying() map { response =>
      RackAdapter.rackResponseToHttpResponse(response.asInstanceOf[RubyArray])
    }
  }
}