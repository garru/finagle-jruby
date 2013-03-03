package com.twitter.finagle.jruby.http

import com.twitter.conversions.time._
import com.twitter.finagle.builder.{Server, ServerBuilder}
import com.twitter.finagle.http.Http
import com.twitter.finagle.Service
import java.net.InetSocketAddress
import com.twitter.finagle.stats.StatsReceiver
import org.jboss.netty.handler.codec.http._


class HttpServerBuilder(host: String, port: Int, statsReceiver: StatsReceiver, processor: Processor) {
	private[this] var server: Option[Server] = None

	def start() {
		synchronized {
			if (server.isEmpty) {
				val service = new Service[HttpRequest, HttpResponse]() {
					def apply(request: HttpRequest) = processor.apply(request)
				}

				server = Some(
					ServerBuilder()
					.name("http server")
					.codec(Http())
					.keepAlive(true)
					.maxConcurrentRequests(10000)
					.reportTo(statsReceiver)
					.bindTo(new InetSocketAddress(host, port))
					.build(service)
				)
			}
		}
	}

	def shutdown() {
		synchronized {
			server foreach { aserver =>
				aserver.close(1.seconds)
				server = None
			}
		}
	}
}
