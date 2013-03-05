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
import com.twitter.finagle.stats.OstrichStatsReceiver;
import com.twitter.finagle.stats.Counter;
import com.twitter.finagle.stats.Stat;

public class StatsReceiverLibrary implements Library {
  public static RubyClass statsReceiverKlass;


  public void load(Ruby runtime, boolean wrap) throws IOException {
    RubyModule finagle = runtime.getOrCreateModule("Finagle");
    load(runtime, finagle, wrap);
  }

  public void load(Ruby runtime, RubyModule module, boolean wrap) throws IOException {
    statsReceiverKlass = module.defineClassUnder(
      "StatsReceiver",
      runtime.getObject(),
      STATS_RECEIVER_ALLOCATOR
    );
    statsReceiverKlass.defineAnnotatedMethods(RubyStatsReceiver.class);
  }

  private final static ObjectAllocator STATS_RECEIVER_ALLOCATOR = new ObjectAllocator() {
    public IRubyObject allocate(Ruby runtime, RubyClass klass) {
      return new RubyStatsReceiver(runtime, klass);
    }
  };

  @JRubyClass(name = "StatsReceiver")
  public static class RubyStatsReceiver extends RubyObject {
    private WrappedOstrichStatsReceiver underlying;

    public RubyStatsReceiver(Ruby runtime, RubyClass metaClass, WrappedOstrichStatsReceiver underlying) {
      super(runtime, metaClass);
      this.underlying = underlying;
    }

    public RubyStatsReceiver(Ruby runtime, RubyClass metaClass) {
      super(runtime, metaClass);
      this.underlying = null;
    }

    public RubyStatsReceiver(Ruby runtime, WrappedOstrichStatsReceiver underlying) {
      super(runtime, StatsReceiverLibrary.statsReceiverKlass);
      this.underlying = underlying;
    }

    public RubyStatsReceiver(Ruby runtime) {
      super(runtime, StatsReceiverLibrary.statsReceiverKlass);
      this.underlying = new WrappedOstrichStatsReceiver();
    }

    public WrappedOstrichStatsReceiver getUnderlying() {
      return underlying;
    }

    public OstrichStatsReceiver toJava() {
      return this.underlying.getUnderlying();
    }

    @JRubyMethod
    public IRubyObject initialize(ThreadContext context) {
      underlying = new WrappedOstrichStatsReceiver();
      return getRuntime().getNil();
    }

    @JRubyMethod(name = "incr", required = 1, optional = 1)
    public IRubyObject incr(final ThreadContext context, IRubyObject[] args) {
      final Ruby runtime = getRuntime();
      String name = ((RubyString) args[0]).toString();
      Long delta;
      if(args[1].isNil()) {
        delta = new Long(1);
      } else {
        delta = (Long) ((RubyNumeric) args[1]).toJava(Long.class);
      }

      underlying.counter(name, delta.intValue());
      return getRuntime().getNil();
    }


    @JRubyMethod(name = "metric", required = 2)
    public IRubyObject metric(final ThreadContext context, IRubyObject arg1, IRubyObject arg2) {
      final Ruby runtime = getRuntime();

      String name = ((RubyString) arg1).toString();
      Long delta = (Long) ((RubyNumeric) arg2).toJava(Long.class);

      underlying.stat(name, delta.intValue());
      return getRuntime().getNil();
    }
  }
}
