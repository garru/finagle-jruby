package com.twitter.finagle.jruby.test;

import com.twitter.finagle.jruby.RubyFutureLibrary;
import java.lang.Exception;
import org.jruby.*;
import org.junit.*;
import static com.twitter.finagle.jruby.RubyFutureLibrary.*;
import static junit.framework.Assert.*;
import org.jruby.exceptions.RaiseException;


public class TestRubyFutureLibrary extends TestRubyBase {
  private RubyNil rubyNil;
  private RubyFutureLibrary library;
  private RubyClass futureKlass;

  @Before
  public void setUp() throws Exception {
    if (runtime == null) {
      runtime = Ruby.newInstance();
    }
    rubyNil = (RubyNil) runtime.getNil();
    library = new RubyFutureLibrary();
    library.load(runtime, false);
    eval("Future.value(1)");
  }

  @Test
  public void testMap() throws Exception {
    String result = eval("f1 = Future.value(1); f2 = f1.map{|x| x + 1}; p f2.apply()");
    assertEquals("2", result);
  }

  @Test
  public void testFlatMap() throws Exception {
    String result = eval("f1 = Future.value(1); f2 = f1.flat_map{|x| Future.value(x + 1)}; p f2.apply()");
    assertEquals("2", result);
  }

  @Test(expected = RaiseException.class)
  public void testUnhandledExceptionMap() throws Exception {
    eval("f1 = Future.value(1); f2 = f1.map{|x| raise ArgumentError}; p f2.apply()");
  }

  @Test
  public void testHandle() throws Exception {
    String handle = eval("f1 = Future.value(1); f2 = f1.map{|x| raise ArgumentError}; p f2.handling(ArgumentError){|x| 2}.apply()");
    assertEquals("2", handle);
  }

  @Test
  public void testHandleSupportsObjectHierarchy() throws Exception {
    String handle = eval("f1 = Future.value(1); f2 = f1.map{|x| raise ArgumentError}; p f2.handling(Exception){|x| 2}.apply()");
    assertEquals("2", handle);
  }

  @Test
  public void testFlatHandle() throws Exception {
    String handle = eval("f1 = Future.value(1); f2 = f1.map{|x| raise ArgumentError}; p f2.flat_handling(ArgumentError){|x| Future.value(2)}.apply()");
    assertEquals("2", handle);
  }
}
