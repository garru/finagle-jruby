// package com.twitter.finagle.jruby

// import com.twitter.finagle._

// class FinagleCodec extends CodecFactory[IO, ChannelBuffer] {
//   def client = {
//     new Codec[IO, ChannelBuffer] {
//      def pipelineFactory = new ChannelPipelineFactory {
//         def getPipeline() = {
//           val pipeline = Channels.pipeline()
//           pipeline.addLast("channelToRubyIO", new FinagleChannelHandler())
//           pipeline
//         }
//       }
//     }
//   }

//   def server = {
//     new Codec[IO, ChannelBuffer] {
//       def pipelineFactory = new ChannelPipelineFactory {
//         def getPipeline() = {
//           val pipeline = Channels.pipeline()
//           pipeline.addLast("channelToRubyIO", new FinagleChannelHandler())
//           pipeline
//         }
//       }
//     }
//   }
// }