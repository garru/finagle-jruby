package com.twitter.finagle.jruby.io;

import java.io.IOException;
import java.lang.StringBuffer;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jruby.*;
import org.jruby.anno.JRubyClass;
import org.jruby.anno.JRubyMethod;
import org.jruby.runtime.Block;
import org.jruby.runtime.builtin.IRubyObject;
import org.jruby.runtime.load.Library;
import org.jruby.runtime.ObjectAllocator;
import org.jruby.runtime.ThreadContext;

public class IOLibrary implements Library {
  public static RubyClass rackIO;

  public void load(Ruby runtime, boolean wrap) throws IOException {
    RubyModule finagle = runtime.getOrCreateModule("Finagle");
    load(runtime, finagle, wrap);
  }

  public void load(Ruby runtime, RubyModule module, boolean wrap) {
    rackIO = module.defineClassUnder(
      "IO",
      runtime.getObject(),
      FUTURE_ALLOCATOR
    );
    rackIO.defineAnnotatedMethods(FinagleRubyIO.class);
  }

  private final static ObjectAllocator FUTURE_ALLOCATOR = new ObjectAllocator() {
    public IRubyObject allocate(Ruby runtime, RubyClass klass) {
      return new FinagleRubyIO(runtime, klass);
    }
  };

  @JRubyClass(name = "Finagle::IO")
  public static class FinagleRubyIO extends RubyObject {
    private static char lineBreak = '\n';

    private ChannelBuffer underlying;

    public FinagleRubyIO(Ruby runtime, RubyClass metaClass, ChannelBuffer underlying) {
      super(runtime, metaClass);
      this.underlying = underlying;
    }

    public FinagleRubyIO(Ruby runtime, RubyClass metaClass) {
      super(runtime, metaClass);
      this.underlying = null;
    }

    public FinagleRubyIO(Ruby runtime, ChannelBuffer underlying) {
      super(runtime, IOLibrary.rackIO);
      this.underlying = underlying;
    }

    public ChannelBuffer getUnderlying() {
      return underlying;
    }

    // gets must be called without arguments and return a string, or nil on EOF.
    // read behaves like IO#read. Its signature is read([length, [buffer]]). If given, length must be an non-negative Integer (>= 0) or nil, and buffer must be a String and may not be nil. If length is given and not nil, then this method reads at most length bytes from the input stream. If length is not given or nil, then this method reads all data until EOF. When EOF is reached, this method returns nil if length is given and not nil, or "" if length is not given or is nil. If buffer is given, then the read data will be placed into buffer instead of a newly created String object.
    // each must be called without arguments and only yield Strings.
    // rewind must be called without arguments. It rewinds the input stream back to the beginning. It must not raise Errno::ESPIPE: that is, it may not be a pipe or a socket. Therefore, handler developers must buffer the input data into some rewindable object if the underlying input stream is not rewindable.
    // close must never be called on the input stream.

    // probably not strictly necessary but going to make gets act like IO.gets($/)
    @JRubyMethod(name = "gets")
    public IRubyObject gets(final ThreadContext context) {
      if (underlying.readable()) {
        StringBuffer sbs = new StringBuffer();

        char lastChar = 'a';
        while (underlying.readable() && lastChar != RackIO.lineBreak) {
          lastChar = (char) underlying.readByte();
          sbs.append(lastChar);
        }
        return RubyString.newString(getRuntime(), sbs.toString());
      } else {
        return getRuntime().getNil();
      }
    }

    @JRubyMethod(name = "read", optional = 2)
    public IRubyObject read(final ThreadContext context, IRubyObject[] args) {
      switch (args.length) {
        case 0:
          return read(context, underlying.readableBytes());
        case 1:
          if(args[0].isNil()){
            return read(context, underlying.readableBytes());
          } else {
            return read(context, RubyInteger.num2int(args[0]));
          }
        case 2:
          return readIntoBuffer(context, args);
        default:
          throw getRuntime().newArgumentError("FinagleRackIO#read called with too many arguments");
      }
    }

    private IRubyObject readIntoBuffer(final ThreadContext context, IRubyObject[] args) {
      IRubyObject[] argsCopy = { args[0] };
      IRubyObject buffer = read(context, argsCopy);
      if (!buffer.isNil()) {
        RubyString stringBuffer = (RubyString) args[1];
        stringBuffer.setValue(buffer.toString());
        stringBuffer.setTaint(true);
      }
      return buffer;
    }

    private IRubyObject read(final ThreadContext context, int length) {
      if (length == 0) {
        return RubyString.newString(getRuntime(), "");
      } else if (underlying.readable()) {
        StringBuffer buffer = new StringBuffer();
        int readableBytes = underlying.readableBytes();
        int numReadBytes = length;

        if (readableBytes < numReadBytes) {
          numReadBytes = readableBytes;
        }

        byte[] dest = new byte[numReadBytes];
        underlying.readBytes(dest);
        return RubyString.newString(getRuntime(), dest);
      } else {
        return getRuntime().getNil();
      }
    }

    @JRubyMethod(name = "each")
    public IRubyObject each(final ThreadContext context, final Block block) {
      if (!block.isGiven()) {
        throw getRuntime().newLocalJumpErrorNoBlock();
      }

      IRubyObject line = gets(context);
      while (!line.isNil()) {
        block.yield(context, line);
        line = gets(context);
      }

      return this;
    }

    @JRubyMethod(name = "rewind")
    public IRubyObject rewind(final ThreadContext context) {
      underlying.resetReaderIndex();
      return getRuntime().getNil();
    }
  }
}