package com.twitter.finagle.jruby.client;

import com.twitter.finagle.Service;
import org.jruby.*;
import org.jruby.anno.JRubyClass;
import org.jruby.anno.JRubyMethod;
import org.jruby.runtime.Block;
import org.jruby.runtime.builtin.IRubyObject;
import org.jruby.runtime.load.Library;
import org.jruby.runtime.ObjectAllocator;
import org.jruby.runtime.ThreadContext;


public class ServiceLibrary implements Library {
  public static RubyClass service;

  public void load(Ruby runtime, boolean wrap) throws IOException {
    RubyModule finagle = runtime.getOrCreateModule("Finagle");
    load(runtime, finagle, wrap);
  }

  public void load(Ruby runtime, RubyModule module, boolean wrap) {
    service = module.defineClassUnder(
      "Service",
      runtime.getObject(),
      SERVICE_ALLOCATOR
    );
    service.defineAnnotatedMethods(RubyService.class);
  }

  private final static ObjectAllocator SERVICE_ALLOCATOR = new ObjectAllocator() {
    public IRubyObject allocate(Ruby runtime, RubyClass klass) {
      return new RubyService(runtime, klass);
    }
  };

  @JRubyClass(name = "Finagle::Service")
  public static class RubyService extends RubyObject {
    private Service<IRubyObject, IRubyObject> underlying;

    public RubyService(Ruby runtime, RubyClass metaClass, Service<IRubyObject, IRubyObject> underlying) {
      super(runtime, metaClass);
      this.underlying = underlying;
    }

    public RubyService(Ruby runtime, RubyClass metaClass) {
      super(runtime, metaClass);
      this.underlying = null;
    }

    public RubyService(Ruby runtime, Service<IRubyObject, IRubyObject> underlying) {
      super(runtime, ServiceLibrary.service);
      this.underlying = underlying;
    }

    @JRubyMethod(name = "apply", required = 1)
    public IRubyObject apply(final ThreadContext context, IRubyObject request) {
      underlying.apply(request)
    }
  }
}