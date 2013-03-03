// package com.twitter.finagle.jruby

// import com.twitter.finagle.{Filter, Service}
// import org.jruby.runtime.builtin.IRubyObject
// import com.twitter.finagle.RubyFutureLibrary._
// import com.twitter.finagle.http.Util

// class FinagleService(filter: IRubyObject)
//   extends Service[IO, ChannelBuffer]
// {
//   def apply(request: IRubyObject) = {
//     val t = filter.callMethod(
//       request.getRuntime().getCurrentContext(),
//       "call",
//       request
//     ).asInstanceOf[RubyFuture]

//     t.getUnderlying() map { rubyObj =>
//       new IO(Util.bodyToBuffer(rubyObj))
//     }
//   }
// }
