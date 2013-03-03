// package com.twitter.finagle.jruby

// import org.jboss.netty.channel.{SimpleChannelHandler, MessageEvent, ChannelHandlerContext}
// import org.jboss.netty.buffer.ChannelBuffer

// class FinagleChannelHandler extends SimpleChannelHandler {
//   override def messageReceived(ctx: ChannelHandlerContext, e: MessageEvent) = {
//     ctx.sendUpstream(new IO(e.getMessage().asInstanceOf[ChannelBuffer]))
//   }

//   override def writeRequested(ctx: ChannelHandlerContext, e: MessageEvent) = {
//     val buffer = e.getMessage().asInstanceOf[IO]
//     ctx.sendDownstream(buffer.underlying)
//   }
// }