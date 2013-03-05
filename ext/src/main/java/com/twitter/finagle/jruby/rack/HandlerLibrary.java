package com.twitter.finagle.jruby.rack;

import com.twitter.finagle.jruby.http.HttpServerBuilder;
import com.twitter.finagle.jruby.http.RackProcessor;
import com.twitter.finagle.Service;
import com.twitter.finagle.stats.OstrichStatsReceiver;
import com.twitter.util.Future;
import com.twitter.ostrich.admin.RuntimeEnvironment;
import java.io.IOException;
import org.jruby.*;
import org.jruby.anno.JRubyClass;
import org.jruby.anno.JRubyMethod;
import org.jruby.runtime.builtin.IRubyObject;
import org.jruby.runtime.load.Library;
import org.jruby.runtime.ObjectAllocator;
import org.jruby.runtime.ThreadContext;
import com.twitter.finagle.jruby.StatsReceiverLibrary.*;
import com.twitter.finagle.jruby.AdminServiceFactory;
import com.twitter.finagle.jruby.WrappedOstrichStatsReceiver;

public class HandlerLibrary implements Library {
  public static RubyClass rackHandler;

  public void load(Ruby runtime, boolean wrap) throws IOException {
    RubyModule finagle = runtime.getOrCreateModule("Finagle");
    RubyModule rack = finagle.defineOrGetModuleUnder("Rack");
    load(runtime, rack, wrap);
  }

  public void load(Ruby runtime, RubyModule module, boolean wrap) throws IOException {
    rackHandler = module.defineClassUnder(
      "Handler",
      runtime.getObject(),
      HANDLER_ALLOCATOR
    );
    rackHandler.defineAnnotatedMethods(RackHandler.class);
  }

  private final static ObjectAllocator HANDLER_ALLOCATOR = new ObjectAllocator() {
    public IRubyObject allocate(Ruby runtime, RubyClass klass) {
      return runtime.getNil();
    }
  };

  @JRubyClass(name = "Finagle::Rack::Handler")
  public static class RackHandler extends RubyObject {
    private static HttpServerBuilder builder;

    public RackHandler(Ruby runtime, RubyClass metaClass) {
      super(runtime, metaClass);
    }

    @JRubyMethod(name = "run", meta = true, required = 1, optional = 1)
    public static IRubyObject createServer(final ThreadContext context, IRubyObject recv, final IRubyObject[] args) {
      RubyHash serverOptions = (RubyHash) args[1];
      String host = (String) serverOptions.get(RubySymbol.newSymbol(context.getRuntime(), "Host"));
      if (host == null) {
        host = "0.0.0.0";
      }

      Long port = (Long) serverOptions.get(RubySymbol.newSymbol(context.getRuntime(), "Port"));
      if (port == null) {
        port = new Long(8080);
      }

      Long adminPort = (Long) serverOptions.get(RubySymbol.newSymbol(context.getRuntime(), "AdminPort"));
      if (adminPort == null) {
        adminPort = new Long(9000);
      }

      RubyStatsReceiver stats = (RubyStatsReceiver) serverOptions.get(RubySymbol.newSymbol(context.getRuntime(), "stats"));

      if (stats == null) {
        stats = new RubyStatsReceiver(context.getRuntime());
      }

      AdminServiceFactory adminFactory = new AdminServiceFactory(adminPort.intValue());
      adminFactory.apply(new RuntimeEnvironment(null));

      builder = new HttpServerBuilder(host, port.intValue(), stats.getUnderlying().getUnderlying(), new RackProcessor(args[0]));
      builder.start();

      return context.getRuntime().getNil();
    }

    @JRubyMethod(name = "stop", meta = true)
    public static IRubyObject stopServer(final ThreadContext context, IRubyObject recv) {
      builder.shutdown();
      return context.getRuntime().getNil();
    }
  }
}