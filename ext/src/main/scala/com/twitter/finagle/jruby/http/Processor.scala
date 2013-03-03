package com.twitter.finagle.jruby.http

import com.twitter.util.Future
import org.jboss.netty.handler.codec.http.{HttpRequest, HttpResponse}

trait Processor {
  def apply(request: HttpRequest): Future[HttpResponse]
}