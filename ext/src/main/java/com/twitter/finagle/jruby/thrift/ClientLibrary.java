package com.twitter.finagle.jruby.thrift;

import com.twitter.finagle.builder.ClientBuilder;
import com.twitter.finagle.jruby.http.Util;
import com.twitter.finagle.jruby.RubyFutureLibrary.*;
import com.twitter.finagle.jruby.StatsReceiverLibrary.*;
import com.twitter.finagle.Service;
import com.twitter.finagle.thrift.ClientId;
import com.twitter.finagle.thrift.ThriftClientFramedCodec;
import com.twitter.finagle.thrift.ThriftClientRequest;
import com.twitter.util.Future;
import java.io.IOException;
import java.lang.StringBuffer;
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
import scala.Some;

public class ClientLibrary implements Library {
  public static RubyClass thriftClient;
  public static RubyClass thriftTransport;

  public void load(Ruby runtime, boolean wrap) throws IOException {
    RubyModule finagle = runtime.getOrCreateModule("Finagle");
    RubyModule rack = finagle.defineOrGetModuleUnder("Thrift");
    load(runtime, rack, wrap);
  }

  public void load(Ruby runtime, RubyModule module, boolean wrap) {
    thriftClient = module.defineClassUnder(
      "Client",
      runtime.getObject(),
      CLIENT_ALLOCATOR
    );
    thriftClient.defineAnnotatedMethods(ThriftClient.class);
    // httpRequest = module.defineClassUnder(
    //   "Transport",
    //   runtime.getObject(),
    //   REQUEST_ALLOCATOR
    // );
    // httpRequest.defineAnnotatedMethods(RubyRequest.class);
  }

  private final static ObjectAllocator CLIENT_ALLOCATOR = new ObjectAllocator() {
    public IRubyObject allocate(Ruby runtime, RubyClass klass) {
      return new ThriftClient(runtime, klass);
    }
  };

  // private final static ObjectAllocator REQUEST_ALLOCATOR = new ObjectAllocator() {
  //   public IRubyObject allocate(Ruby runtime, RubyClass klass) {
  //     return new RubyRequest(runtime, klass);
  //   }
  // };

  // @JRubyClass(name = "Finagle::Http:Request")
  // public static class RubyRequest extends RubyObject {
  //   private IRubyObject headers;
  //   private IRubyObject uri;
  //   private IRubyObject content;
  //   private IRubyObject httpMethod;

  //   public RubyRequest(Ruby runtime, RubyClass metaClass) {
  //     super(runtime, metaClass);
  //   }

  //   @JRubyMethod(name = "initialize", required = 4)
  //   public IRubyObject initialize(final ThreadContext context, IRubyObject[] args) {
  //     this.uri = args[0];
  //     this.headers = args[1];
  //     this.content = args[2];
  //     this.httpMethod = args[3];
  //     return getRuntime().getNil();
  //   }

  //   public IRubyObject getUri() {
  //     return this.uri;
  //   }

  //   public IRubyObject getContent() {
  //     return this.content;
  //   }

  //   public IRubyObject getHeaders() {
  //     return this.headers;
  //   }

  //   public IRubyObject getHttpMethod() {
  //     return this.httpMethod;
  //   }
  // }

  @JRubyClass(name = "Finagle::Thrift::Client")
  public static class ThriftClient extends RubyObject {
    private Service<ThriftClientRequest, byte[]> underlying;

    public ThriftClient(Ruby runtime, RubyClass metaClass, Service<ThriftClientRequest, byte[]> underlying) {
      super(runtime, metaClass);
      this.underlying = underlying;
    }

    public ThriftClient(Ruby runtime, RubyClass metaClass) {
      super(runtime, metaClass);
      this.underlying = null;
    }

    public ThriftClient(Ruby runtime, Service<ThriftClientRequest, byte[]>  underlying) {
      super(runtime, ClientLibrary.thriftClient);
      this.underlying = underlying;
    }

    @JRubyMethod(name = "initialize", required = 1)
    public IRubyObject initialize(final ThreadContext context, IRubyObject options) {
      RubyHash optionsHash = (RubyHash) options;
      String host = (String) optionsHash.get(RubySymbol.newSymbol(context.getRuntime(), "host"));
      String clientId = (String) optionsHash.get(RubySymbol.newSymbol(context.getRuntime(), "client_id"));

      if (host == null)
        throw getRuntime().newArgumentError("Finagle::Thrift::Client.new(options) must contain host key");

      Long connectionLimit = (Long) optionsHash.get(RubySymbol.newSymbol(context.getRuntime(), "connection_limit"));
      if (connectionLimit == null) {
        connectionLimit = new Long(1);
      }

      IRubyObject statsReceiver = (IRubyObject) optionsHash.get(RubySymbol.newSymbol(context.getRuntime(), "stats_receiver"));
      if (statsReceiver == null) {
        System.out.println("sup");
      }

      this.underlying = ClientBuilder.safeBuild(ClientBuilder.get()
        .codec(ThriftClientFramedCodec.apply(new Some<ClientId>(new ClientId(clientId))))
        .hosts(host)
        .reportTo(((RubyStatsReceiver) statsReceiver).toJava())
        .hostConnectionLimit(connectionLimit.intValue()));
      return getRuntime().getNil();
    }

    @JRubyMethod(name = "apply", required = 1)
    public IRubyObject apply(final ThreadContext context, IRubyObject request) {
      ThriftClientRequest thriftRequest = new ThriftClientRequest(Util.bodyToBuffer(request).array(), false);
      Future<byte[]> fu = underlying.apply(thriftRequest);
      return Util.futureByteArrayToRubyFutureString(getRuntime(), fu);
    }
  }
}