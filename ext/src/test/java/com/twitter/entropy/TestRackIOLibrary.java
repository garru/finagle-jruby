package com.twitter.finagle.test;

import com.twitter.finagle.rack.IOLibrary;
import com.twitter.finagle.RubyFutureLibrary;
import java.lang.Exception;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.util.CharsetUtil;
import org.jruby.*;
import org.jruby.runtime.builtin.IRubyObject;
import org.junit.*;
import static com.twitter.finagle.rack.IOLibrary.*;
import static junit.framework.Assert.*;

public class TestRackIOLibrary {
  private Ruby runtime;
  private RubyNil rubyNil;
  private IOLibrary library;
  private RubyClass rackKlass;

  @Before
  public void setUp() throws Exception {
    runtime = Ruby.newInstance();
    rubyNil = (RubyNil) runtime.getNil();
    library = new IOLibrary();
    library.load(runtime, false);
    rackKlass = runtime.getModule("Finagle").defineOrGetModuleUnder("Rack").getClass("IO");
  }

  @Test
  public void testGets() throws Exception {
    ChannelBuffer input = ChannelBuffers.copiedBuffer("HELLO", CharsetUtil.US_ASCII);
    RackIO rackIO = new RackIO(runtime, rackKlass, input);
    RubyString result = (RubyString) rackIO.gets(runtime.getCurrentContext());
    assertEquals("HELLO", result.toString());
  }

  @Test
  public void testGetsWithMultipleLines() throws Exception {
    ChannelBuffer input = ChannelBuffers.copiedBuffer("HELLO\nBYE", CharsetUtil.US_ASCII);
    RackIO rackIO = new RackIO(runtime, rackKlass, input);
    RubyString result = (RubyString) rackIO.gets(runtime.getCurrentContext());
    assertEquals("HELLO\n", result.toString());
    result = (RubyString) rackIO.gets(runtime.getCurrentContext());
    assertEquals("BYE", result.toString());
  }

  @Test
  public void testGetsWithEmptyBuffer() throws Exception {
    ChannelBuffer input = ChannelBuffers.copiedBuffer("", CharsetUtil.US_ASCII);
    RackIO rackIO = new RackIO(runtime, rackKlass, input);
    IRubyObject result = rackIO.gets(runtime.getCurrentContext());
    assertTrue(result.isNil());
  }

  @Test
  public void testGetsReturnsNilWhenAtEOF() throws Exception {
    ChannelBuffer input = ChannelBuffers.copiedBuffer("HELLO", CharsetUtil.US_ASCII);
    RackIO rackIO = new RackIO(runtime, rackKlass, input);
    RubyString result = (RubyString) rackIO.gets(runtime.getCurrentContext());
    assertEquals("HELLO", result.toString());
    IRubyObject result2 = rackIO.gets(runtime.getCurrentContext());
    assertTrue(result2.isNil());
  }

  @Test
  public void testReadOnEOFReturnsEmptyString() throws Exception {
    ChannelBuffer input = ChannelBuffers.copiedBuffer("", CharsetUtil.US_ASCII);
    RackIO rackIO = new RackIO(runtime, rackKlass, input);
    IRubyObject [] args = new IRubyObject [] {};
    RubyString result = (RubyString) rackIO.read(runtime.getCurrentContext(), args);
    assertEquals("", result.toString());
  }

  @Test
  public void testReadOnEOFWithPositiveLengthReturnsNil() throws Exception {
    ChannelBuffer input = ChannelBuffers.copiedBuffer("", CharsetUtil.US_ASCII);
    RackIO rackIO = new RackIO(runtime, rackKlass, input);
    IRubyObject [] args = new IRubyObject [] {RubyNumeric.int2fix(runtime, 1)};
    IRubyObject result = rackIO.read(runtime.getCurrentContext(), args);
    assertTrue("", result.isNil());
  }

  @Test
  public void testReadWithoutLengthReturnsWholeString() throws Exception {
    String buffer = "just setting up my twttr\nand setting up more twitters";
    ChannelBuffer input = ChannelBuffers.copiedBuffer(buffer, CharsetUtil.US_ASCII);
    RackIO rackIO = new RackIO(runtime, rackKlass, input);
    IRubyObject [] args = new IRubyObject [] {};
    IRubyObject result = rackIO.read(runtime.getCurrentContext(), args);
    assertEquals(buffer, result.toString());
  }

  @Test
  public void testReadWithLengthReturnsString() throws Exception {
    String buffer = "just setting up my twttr\nand setting up more twitters";
    ChannelBuffer input = ChannelBuffers.copiedBuffer(buffer, CharsetUtil.US_ASCII);
    RackIO rackIO = new RackIO(runtime, rackKlass, input);
    IRubyObject [] args = new IRubyObject [] {RubyNumeric.int2fix(runtime, 1)};
    IRubyObject result = rackIO.read(runtime.getCurrentContext(), args);
    assertEquals("j", result.toString());
    IRubyObject result2 = rackIO.read(runtime.getCurrentContext(), args);
    assertEquals("u", result2.toString());
  }

  @Test
  public void testReadWithBufferOnEOFReturnsEmptyString() throws Exception {
    ChannelBuffer input = ChannelBuffers.copiedBuffer("", CharsetUtil.US_ASCII);
    RackIO rackIO = new RackIO(runtime, rackKlass, input);
    RubyString buffer = RubyString.newEmptyString(runtime);
    IRubyObject [] args = new IRubyObject [] { rubyNil, buffer};
    RubyString result = (RubyString) rackIO.read(runtime.getCurrentContext(), args);
    assertEquals("", result.toString());
    // for some reason IO.read appends null bytes to the buffer at EOF
    assertEquals("", buffer.toString());
  }
}