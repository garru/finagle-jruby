package com.twitter.finagle.jruby.builder;

import com.twitter.finagle.builder.ClientBuilder;
import com.twitter.finagle.jruby.StatsReceiverLibrary.*;

import com.twitter.util.Duration;
import java.io.IOException;
import java.lang.StringBuffer;
import java.util.concurrent.TimeUnit;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.handler.codec.http.*;
import org.jruby.*;
import org.jruby.anno.JRubyClass;
import org.jruby.anno.JRubyMethod;
import org.jruby.runtime.Block;
import org.jruby.runtime.builtin.IRubyObject;
import org.jruby.runtime.load.Library;
import org.jruby.runtime.ObjectAllocator;
import org.jruby.runtime.ThreadContext;

public class ClientBuilderLibrary implements Library {
  public static RubyClass clientBuilderKlass;

   public void load(Ruby runtime, boolean wrap) throws IOException {
    RubyModule finagle = runtime.getOrCreateModule("Finagle");
    load(runtime, finagle, wrap);
  }

  public void load(Ruby runtime, RubyModule module, boolean wrap) {
    clientBuilderKlass = module.defineClassUnder(
      "ClientBuilder",
      runtime.getObject(),
      ALLOCATOR
    );
    clientBuilderKlass.defineAnnotatedMethods(RubyClientBuilder.class);
  }

  private final static ObjectAllocator ALLOCATOR = new ObjectAllocator() {
    public IRubyObject allocate(Ruby runtime, RubyClass klass) {
      return new RubyClientBuilder(runtime, klass);
    }
  };

  public static class RubyClientBuilder extends RubyObject {
    private static ClientBuilder underlying;

    public RubyClientBuilder(Ruby runtime, RubyClass metaClass, ClientBuilder underlying) {
      super(runtime, metaClass);
      this.underlying = underlying;
    }

    public RubyClientBuilder(Ruby runtime, RubyClass metaClass) {
      super(runtime, metaClass);
      this.underlying = ClientBuilder.get();
    }

    // @JRubyMethod(name = "cluster", required = 1)
    // public IRubyObject cluster(final ThreadContext context, IRubyObject cluster) {
    //   RubyCluster rubyCluster = (RubyCluster) cluster;
    //   this.underlying.cluster(rubyCluster.toJava())
    // }

    // @JRubyMethod(name = "codec_factory", required = 1)
    // public IRubyObject codecFactory(final ThreadContext context, IRubyObject request) {
    //   RubyRequest rubyRequest = (RubyRequest) request;
    // }

    //timeout in milliseconds
    @JRubyMethod(name = "tcp_connect_timeout", required = 1)
    public IRubyObject tcpConnectTimeout(final ThreadContext context, IRubyObject timeout) {
      Long tcpConnectTimeout = (Long) timeout.toJava(Long.class);
      return copy(this.underlying.tcpConnectTimeout(Duration.apply(tcpConnectTimeout, TimeUnit.MILLISECONDS)));
    }

    //timeout in milliseconds
    @JRubyMethod(name = "request_timeout", required = 1)
    public IRubyObject requestTimeout(final ThreadContext context, IRubyObject timeout) {
      Long requestTimeout = (Long) timeout.toJava(Long.class);
      return copy(this.underlying.requestTimeout(Duration.apply(requestTimeout, TimeUnit.MILLISECONDS)));
    }

    //timeout in milliseconds
    @JRubyMethod(name = "connect_timeout", required = 1)
    public IRubyObject connectTimeout(final ThreadContext context, IRubyObject timeout) {
      Long connectTimeout = (Long) timeout.toJava(Long.class);
      return copy(this.underlying.connectTimeout(Duration.apply(connectTimeout, TimeUnit.MILLISECONDS)));
    }

    //timeout in milliseconds
    @JRubyMethod(name = "timeout", required = 1)
    public IRubyObject timeout(final ThreadContext context, IRubyObject aTimeout) {
      Long timeout = (Long) aTimeout.toJava(Long.class);
      return copy(this.underlying.timeout(Duration.apply(timeout, TimeUnit.MILLISECONDS)));
    }

    @JRubyMethod(name = "keep_alive", required = 1)
    public IRubyObject keepAlive(final ThreadContext context, IRubyObject akeepAlive) {
      Boolean keepAlive = (Boolean) akeepAlive.toJava(Boolean.class);
      return copy(this.underlying.keepAlive(keepAlive));
    }

    @JRubyMethod(name = "reader_idle_timeout", required = 1)
    public IRubyObject readerIdleTimeout(final ThreadContext context, IRubyObject timeout) {
      Long readerIdleTimeout = (Long) timeout.toJava(Long.class);
      return copy(this.underlying.readerIdleTimeout(Duration.apply(readerIdleTimeout, TimeUnit.MILLISECONDS)));
    }

    @JRubyMethod(name = "writer_idle_timeout", required = 1)
    public IRubyObject writerIdleTimeout(final ThreadContext context, IRubyObject timeout) {
      Long writerIdleTimeout = (Long) timeout.toJava(Long.class);
      return copy(this.underlying.writerIdleTimeout(Duration.apply(writerIdleTimeout, TimeUnit.MILLISECONDS)));
    }

    @JRubyMethod(name = "report_to", required = 1)
    public IRubyObject reportTo(final ThreadContext context, IRubyObject statsReceiver) {
      RubyStatsReceiver stats = (RubyStatsReceiver) statsReceiver;
      return copy(this.underlying.reportTo(stats.toJava()));
    }

    // returns client
    @JRubyMethod(name = "build")
    public IRubyObject build(final ThreadContext context) {
      return getRuntime().getNil();
    }

    public IRubyObject copy(ClientBuilder newUnderlying) {
      final Ruby runtime = getRuntime();
      return new RubyClientBuilder(runtime, runtime.getClass("Finagle::ClientBuilder"), newUnderlying);
    }
  }

}