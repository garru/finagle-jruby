package com.twitter.finagle.jruby

import com.twitter.ostrich.admin.{AdminServiceFactory => OstrichAdminFactory}
import com.twitter.ostrich.admin.RuntimeEnvironment;
import com.twitter.finagle.stats.OstrichStatsReceiver;

class AdminServiceFactory(int: Int) {
  val underlying = OstrichAdminFactory(9000)

  def apply(runtime: RuntimeEnvironment) = {
    OstrichAdminFactory(httpPort = 9000).apply(runtime)
  }
}

class WrappedOstrichStatsReceiver {
  val underlying = new OstrichStatsReceiver

  def getUnderlying = underlying


  def counter(name: String, delta: Int) = {
    underlying.counter(name).incr(delta)
  }

  def stat(name: String, add: Int) = {
    underlying.stat(name).add(add)
  }
}