package com.example

import com.typesafe.config.ConfigFactory
import akka.actor.{Props, ActorSystem}
import akka.io.IO
import spray.can.Http

/**
 *
 */
object Main extends App {
  private lazy val config = ConfigFactory.load()
  private lazy val serverAddress = config.getString("com.example.address")
  private lazy val serverPort = config.getInt("com.example.port")

  // we need an ActorSystem to host our application in
  implicit val system = ActorSystem("SprayTimeoutHerokuExample")

  // the handler actor replies to incoming HttpRequests
  val handler = system.actorOf(Props[TestActor], "Main")

  // create a new HttpServer using our handler and tell it where to bind to
  IO(Http) ! Http.Bind(handler, interface = serverAddress, port = serverPort)
}
