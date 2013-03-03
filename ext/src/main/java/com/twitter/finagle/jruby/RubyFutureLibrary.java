package com.twitter.finagle.jruby;

import com.twitter.util.Function;
import com.twitter.util.ExceptionalFunction;
import com.twitter.util.Future;
import java.io.IOException;
import org.jruby.*;
import org.jruby.exceptions.RaiseException;
import org.jruby.anno.JRubyClass;
import org.jruby.anno.JRubyMethod;
import org.jruby.runtime.Block;
import org.jruby.runtime.builtin.IRubyObject;
import org.jruby.runtime.load.Library;
import org.jruby.runtime.ObjectAllocator;
import org.jruby.runtime.ThreadContext;
import java.lang.Throwable;
import java.lang.Boolean;
import scala.PartialFunction;

public class RubyFutureLibrary implements Library {
  public static RubyClass futureKlass;

  public void load(Ruby runtime, boolean wrap) throws IOException {
    futureKlass = runtime.defineClass(
      "Future",
      runtime.getObject(),
      FUTURE_ALLOCATOR
    );
    futureKlass.defineAnnotatedMethods(RubyFuture.class);
  }

  private final static ObjectAllocator FUTURE_ALLOCATOR = new ObjectAllocator() {
    public IRubyObject allocate(Ruby runtime, RubyClass klass) {
      return new RubyFuture(runtime, klass);
    }
  };

  public static class HandledExceptionFunction extends ExceptionalFunction<Throwable, IRubyObject> {
    private RubyClass exceptionClass;
    private Block block;

    public HandledExceptionFunction(RubyClass exceptionClass, final Block block) {
      this.exceptionClass = exceptionClass;
      this.block = block;
    }

    public IRubyObject applyE(Throwable x) {
      IRubyObject wrappedException = ((RaiseException) x).getException();
      return this.block.yield(wrappedException.getMetaClass().getRuntime().getCurrentContext(), wrappedException);
    }

    public boolean isDefinedAt(Throwable x) {
      if (x instanceof RaiseException) {
        IRubyObject exception = ((RaiseException) x).getException();
        RubyBoolean result = (RubyBoolean) exception.callMethod(
          exception.getRuntime().getCurrentContext(),
          "is_a?",
          exceptionClass
        );

        Boolean a = (Boolean) result.toJava(Boolean.class);
        return a.booleanValue();
      } else {
        return false;
      }
    }
  }

  public static class FutureHandledExceptionFunction extends ExceptionalFunction<Throwable, Future<IRubyObject>> {
    private HandledExceptionFunction underlying;

    public FutureHandledExceptionFunction(RubyClass exceptionClass, final Block block) {
      this.underlying = new HandledExceptionFunction(exceptionClass, block);
    }

    public Future<IRubyObject> applyE(Throwable x) {
      RubyFuture result = (RubyFuture) this.underlying.applyE(x);
      return result.getUnderlying();
    }

    public boolean isDefinedAt(Throwable x) {
      return underlying.isDefinedAt(x);
    }
  }

  public static class WrappedRubyFunction extends Function<IRubyObject, IRubyObject> {
    private Block block;

    public WrappedRubyFunction(final Block block) {
      this.block = block;
    }


    public IRubyObject apply(IRubyObject value) {
      return block.yield(value.getRuntime().getCurrentContext(), value);
    }
  }

  public static class WrappedFutureRubyFunction extends Function<IRubyObject, Future<IRubyObject>> {
    private WrappedRubyFunction underlying;

    public WrappedFutureRubyFunction(final Block block) {
      this.underlying = new WrappedRubyFunction(block);
    }

    public Future<IRubyObject> apply(IRubyObject value) {
      RubyFuture result = (RubyFuture) underlying.apply(value);
      return result.getUnderlying();
    }
  }

  @JRubyClass(name = "Future")
  public static class RubyFuture extends RubyObject {
    private Future<IRubyObject> underlying;

    public RubyFuture(Ruby runtime, RubyClass metaClass, Future<IRubyObject> underlying) {
      super(runtime, metaClass);
      this.underlying = underlying;
    }

    public RubyFuture(Ruby runtime, RubyClass metaClass) {
      super(runtime, metaClass);
      this.underlying = null;
    }

    public RubyFuture(Ruby runtime, Future<IRubyObject> underlying) {
      super(runtime, RubyFutureLibrary.futureKlass);
      this.underlying = underlying;
    }

    public Future<IRubyObject> getUnderlying() {
      return underlying;
    }

    @JRubyMethod
    public IRubyObject initialize(ThreadContext context) {
      underlying = null;
      return getRuntime().getNil();
    }

    @JRubyMethod(name = "apply")
    public IRubyObject apply(final ThreadContext context) {
      return underlying.apply();
    }

    @JRubyMethod(name = "map")
    public RubyFuture map(final ThreadContext context, final Block block) {
      final Ruby runtime = getRuntime();
      if (!block.isGiven()) {
        throw runtime.newLocalJumpErrorNoBlock();
      }


      Future<IRubyObject> result = underlying.map(new WrappedRubyFunction(block));
      return new RubyFuture(runtime, runtime.getClass("Future"), result);
    }

    @JRubyMethod(name = "flat_map")
    public RubyFuture flatMap(final ThreadContext context, final Block block) {
      final Ruby runtime = getRuntime();
      if (!block.isGiven()) {
        throw runtime.newLocalJumpErrorNoBlock();
      }

      Future<IRubyObject> result = underlying.flatMap(new WrappedFutureRubyFunction(block));
      return new RubyFuture(runtime, runtime.getClass("Future"), result);
    }

    @JRubyMethod(name = "handling")
    public RubyFuture handling(final ThreadContext context, IRubyObject value, final Block block) {
      final Ruby runtime = getRuntime();
      if (!block.isGiven()) {
        throw runtime.newLocalJumpErrorNoBlock();
      }

      HandledExceptionFunction handler = new HandledExceptionFunction((RubyClass) value, block);
      Future<IRubyObject> newUnderlying = underlying.handle(handler);

      return new RubyFuture(runtime, runtime.getClass("Future"), newUnderlying);
    }

    @JRubyMethod(name = "flat_handling")
    public RubyFuture flatHandling(final ThreadContext context, IRubyObject value, final Block block) {
      final Ruby runtime = getRuntime();
      if (!block.isGiven()) {
        throw runtime.newLocalJumpErrorNoBlock();
      }

      FutureHandledExceptionFunction handler = new FutureHandledExceptionFunction((RubyClass) value, block);
      Future<IRubyObject> newUnderlying = underlying.rescue(handler);

      return new RubyFuture(runtime, runtime.getClass("Future"), newUnderlying);
    }

    @JRubyMethod(name = "value", meta = true)
    public static IRubyObject createValue(final ThreadContext context, IRubyObject recv, IRubyObject value) {
      RubyClass klass = (RubyClass) recv;
      return new RubyFuture(klass.getRuntime(), klass, Future.value(value));
    }
  }
}
