package com.example

import spray.routing.HttpServiceActor
import spray.http.ContentType._
import spray.http.MediaTypes._
import scala.concurrent.Future
import akka.actor.ActorLogging

/**
 *
 */
class TestActor extends HttpServiceActor with ActorLogging {
  import context.dispatcher

  def receive = runRoute(testRoute)

  def testRoute = {
    path("test" / IntNumber) {
      timeout =>
        StreamingResponse.sendStreamingResponse(
          contentType = `application/json`,
          onConnectionClosed = {
            log.info("Connection closed")
          },
          future = Future {
            log.info("Start computation")
            // Just simulate some workload
            Thread.sleep(timeout * 1000)
            log.info("Computation completed")
            "Success"
          }
        )
    }
  }
}
