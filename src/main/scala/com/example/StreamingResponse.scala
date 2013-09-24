package com.example

import akka.actor.{ActorRefFactory, ActorLogging, Props, Actor}
import spray.http._
import scala.concurrent.Future
import spray.can.Http
import spray.http.HttpResponse
import scala.util.Success
import spray.routing.RequestContext
import scala.util.Failure
import spray.http.ChunkedResponseStart
import scala.concurrent.duration._

/**
 * Utility object to handle streamed Http response.
 */
object StreamingResponse {
  private val DEFAULT_ACK_TIMEOUT = 500.millis
  val CHUNK = "\0"

  // simple case class whose instances we use as send confirmation message for streaming chunks
  case object Ack

  /**
   * Send a streamed Http response to a client.
   * The client will receive the intermediate `CHUNK` blocks until the final response is available.
   * This allows the server to detect `ConnectionClosed` errors when trying to send the intermediate `CHUNK` and allow the caller
   * to be notified when this happen.
   *
   * @param contentType the content type of the response.
   * @param future the future used to compute the final response to be sent to the client.
   * @param onConnectionClosed optional callback to be called when a `ConnectionClosed` is detected.
   * @param ackTimeout the time between each ack being sent to the client.
   * @param ctx the request context provided by spray.
   * @param refFactory an implicit `ActorRefFactory` used to start the `StreamingResponseActor`
   * @return the handler function to be used by spray.
   */
  def sendStreamingResponse(contentType: ContentType, future: Future[String], onConnectionClosed: => Unit = (), ackTimeout: FiniteDuration = DEFAULT_ACK_TIMEOUT)(ctx: RequestContext)(implicit refFactory: ActorRefFactory): Unit = {
    refFactory.actorOf(Props(new StreamingResponseActor(contentType, future, onConnectionClosed, ackTimeout, ctx)))
  }

  /**
   * Internal actor used to manage interaction between the client and the server.
   * @param contentType the content type of the response.
   * @param future the future used to compute the final response to be sent to the client.
   * @param onConnectionClosed optional callback to be called when a `ConnectionClosed` is detected.
   * @param ackTimeout the time between each ack being sent to the client.
   * @param ctx the request context provided by spray.
   */
  private class StreamingResponseActor(contentType: ContentType, future: Future[String], onConnectionClosed: => Unit, ackTimeout: FiniteDuration, ctx: RequestContext) extends Actor with ActorLogging {

    import context.dispatcher

    private var isRunning = true

    // we use the successful sending of a chunk as trigger for scheduling the next chunk
    ctx.responder ! ChunkedResponseStart(HttpResponse(entity = HttpEntity(contentType, ""))).withAck(Ack)
    future.onComplete {
      case r =>
        log.debug(s"Future completed with r=$r")
        val nextChunk = MessageChunk(CHUNK)
        ctx.responder ! nextChunk.withAck(r)
    }

    def receive = {
      case Success(r: String) =>
        log.debug(s"Received success $r")
        isRunning = false
        ctx.responder ! MessageChunk(r)
        ctx.responder ! ChunkedMessageEnd
        context.stop(self)

      case Failure(e) =>
        log.debug(s"Received failure $e")
        isRunning = false
        ctx.responder ! ChunkedMessageEnd
        context.stop(self)

      case `Ack` =>
        log.debug(s"Ack received")
        in(ackTimeout) {
          if (isRunning) {
            val nextChunk = MessageChunk(CHUNK)
            ctx.responder ! nextChunk.withAck(Ack)
          }
        }

      case ev: Http.ConnectionClosed =>
        log.warning("Stopping response streaming due to {}", ev)
        onConnectionClosed
        context.stop(self)
    }

    private def in[U](duration: FiniteDuration)(body: => U): Unit =
      context.system.scheduler.scheduleOnce(duration)(body)
  }

}
