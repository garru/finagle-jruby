package com.twitter.finagle.jruby.http;

import com.twitter.finagle.builder.ClientBuilder;
import com.twitter.finagle.http.Http;
import com.twitter.finagle.Service;
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


public class ClientLibrary implements Library {
  public static RubyClass httpClient;
  public static RubyClass httpRequest;

  public void load(Ruby runtime, boolean wrap) throws IOException {
    RubyModule finagle = runtime.getOrCreateModule("Finagle");
    RubyModule rack = finagle.defineOrGetModuleUnder("Http");
    load(runtime, rack, wrap);
  }

  public void load(Ruby runtime, RubyModule module, boolean wrap) {
    httpClient = module.defineClassUnder(
      "Client",
      runtime.getObject(),
      CLIENT_ALLOCATOR
    );
    httpClient.defineAnnotatedMethods(HttpClient.class);
    httpRequest = module.defineClassUnder(
      "Request",
      runtime.getObject(),
      REQUEST_ALLOCATOR
    );
    httpRequest.defineAnnotatedMethods(RubyRequest.class);
  }

  private final static ObjectAllocator CLIENT_ALLOCATOR = new ObjectAllocator() {
    public IRubyObject allocate(Ruby runtime, RubyClass klass) {
      return new HttpClient(runtime, klass);
    }
  };

  private final static ObjectAllocator REQUEST_ALLOCATOR = new ObjectAllocator() {
    public IRubyObject allocate(Ruby runtime, RubyClass klass) {
      return new RubyRequest(runtime, klass);
    }
  };

  @JRubyClass(name = "Finagle::Http:Request")
  public static class RubyRequest extends RubyObject {
    private IRubyObject headers;
    private IRubyObject uri;
    private IRubyObject content;
    private IRubyObject httpMethod;

    public RubyRequest(Ruby runtime, RubyClass metaClass) {
      super(runtime, metaClass);
    }

    @JRubyMethod(name = "initialize", required = 4)
    public IRubyObject initialize(final ThreadContext context, IRubyObject[] args) {
      this.uri = args[0];
      this.headers = args[1];
      this.content = args[2];
      this.httpMethod = args[3];
      return getRuntime().getNil();
    }

    public IRubyObject getUri() {
      return this.uri;
    }

    public IRubyObject getContent() {
      return this.content;
    }

    public IRubyObject getHeaders() {
      return this.headers;
    }

    public IRubyObject getHttpMethod() {
      return this.httpMethod;
    }
  }

  @JRubyClass(name = "Finagle::Http::Client")
  public static class HttpClient extends RubyObject {
    private Service<HttpRequest, HttpResponse> underlying;

    public HttpClient(Ruby runtime, RubyClass metaClass, Service<HttpRequest, HttpResponse> underlying) {
      super(runtime, metaClass);
      this.underlying = underlying;
    }

    public HttpClient(Ruby runtime, RubyClass metaClass) {
      super(runtime, metaClass);
      this.underlying = null;
    }

    public HttpClient(Ruby runtime, Service<HttpRequest, HttpResponse> underlying) {
      super(runtime, ClientLibrary.httpClient);
      this.underlying = underlying;
    }

    @JRubyMethod(name = "initialize", required = 1)
    public IRubyObject initialize(final ThreadContext context, IRubyObject options) {
      RubyHash optionsHash = (RubyHash) options;
      String host = (String) optionsHash.get(RubySymbol.newSymbol(context.getRuntime(), "host"));
      if (host == null)
        throw getRuntime().newArgumentError("Finagle::Http::Client.new(options) must contain host key");

      Long connectionLimit = (Long) optionsHash.get(RubySymbol.newSymbol(context.getRuntime(), "connection_limit"));
      if (connectionLimit == null) {
        connectionLimit = new Long(1);
      }

      this.underlying = ClientBuilder.safeBuild(
        ClientBuilder.get()
                     .codec(Http.get())
                     .hosts(host)
                     .hostConnectionLimit(connectionLimit.intValue())
                     .tcpConnectTimeout(Duration.apply(1, TimeUnit.SECONDS)));
      return getRuntime().getNil();
    }

    @JRubyMethod(name = "apply", required = 1)
    public IRubyObject apply(final ThreadContext context, IRubyObject request) {
      RubyRequest rubyRequest = (RubyRequest) request;
      return Util.httpResponseToRubyHttpResponse(getRuntime(),
        underlying.apply(Util.rubyHttpRequestToNettyHttpRequest(rubyRequest))
      );
    }
  }
}