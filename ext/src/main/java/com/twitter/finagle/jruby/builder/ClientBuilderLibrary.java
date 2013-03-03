// package com.twitter.finagle.http;

// import com.twitter.finagle.Service;
// import com.twitter.finagle.http.Http;
// import java.io.IOException;
// import java.lang.StringBuffer;
// import org.jboss.netty.buffer.ChannelBuffer;
// import org.jboss.netty.handler.codec.http.*;
// import org.jruby.*;
// import org.jruby.anno.JRubyClass;
// import org.jruby.anno.JRubyMethod;
// import org.jruby.runtime.Block;
// import org.jruby.runtime.builtin.IRubyObject;
// import org.jruby.runtime.load.Library;
// import org.jruby.runtime.ObjectAllocator;
// import org.jruby.runtime.ThreadContext;
// import com.twitter.finagle.builder.ClientBuilder;
// import com.twitter.finagle.FinagleCodec;

// public class ClientBuilderLibrary implements Library {
//   public static RubyClass clientBuilderKlass;

//    public void load(Ruby runtime, boolean wrap) throws IOException {
//     RubyModule finagle = runtime.getOrCreateModule("Finagle");
//     load(runtime, finagle, wrap);
//   }

//   public void load(Ruby runtime, RubyModule module, boolean wrap) {
//     clientBuilderKlass = module.defineClassUnder(
//       "FinagleClientBuilder",
//       runtime.getObject(),
//       ALLOCATOR
//     );
//     clientBuilderKlass.defineAnnotatedMethods(FinagleClientBuilder.class);
//   }

//   private final static ObjectAllocator ALLOCATOR = new ObjectAllocator() {
//     public IRubyObject allocate(Ruby runtime, RubyClass klass) {
//       return new FinagleClientBuilder(runtime, klass);
//     }
//   };

//   public static class FinagleClientBuilder extends RubyObject {
//     private static ClientBuilder underlying;

//     public FinagleClientBuilder(Ruby runtime, RubyClass metaClass, ClientBuilder underlying) {
//       super(runtime, metaClass)
//       this.underlying = underlying
//     }

//     public FinagleClientBuilder(Ruby runtime, RubyClass metaClass) {
//       super(runtime, metaClass)
//       this.underlying = ClientBuilder.get()
//     }

//     public FinagleCodec getCodec() {
//       return FinagleCodec();
//     }

//     @JRubyMethod(name = "hosts", required = 1)
//     public IRubyObject hosts(final ThreadContext context, IRubyObject hosts) {
//       this.underlying = underlying.hosts(hosts.toString());
//     }

//     @JRubyMethod(name = "host_connection_limit", required = 1)
//     public IRubyObject hostConnectionLimit(final ThreadContext context, IRubyObject hostConnectionLimit) {
//       this.underlying = underlying.hostConnectionLimit(RubyNumeric.num2int(hostConnectionLimit));
//     }

//     @JRubyMethod(name = "build")
//     public IRubyObject build(final ThreadContext context, IRubyObject) {

//     }
//     // //codec is overloaded here since this is the ruby codec
//     // @JRubyMethod(name = "codec", required = 1)
//     // public IRubyObject codec(final ThreadContext context, IRubyObject codec) {
//     //   this.underlying = underlying.codec(Codec));
//     // }
//   }
// }