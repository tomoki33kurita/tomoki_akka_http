package docs.http.scaladsl

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.stream.scaladsl._
import akka.util.ByteString
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ HttpEntity, ContentTypes }
import akka.http.scaladsl.server.Directives._
import scala.util.Random
import scala.io.StdIn

object HttpServerStreamingRandomNumbers {

  def main(args: Array[String]): Unit = {

    implicit val system = ActorSystem(Behaviors.empty, "RandomNumbers")
    implicit val executionContext = system.executionContext

    val numbers = Source.fromIterator(() =>
      Iterator.continually(Random.nextInt())
    )

    val route =
      path("random") {
        get {
          complete(
            HttpEntity(
              ContentTypes.`text/html(UTF-8)`,
              numbers.map(n => ByteString(s"$n\n"))
            )
          )
        }
      }

    val bindingFuture = Http().newServerAt("localhost", 9999).bind(route)
    println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
    StdIn.readLine()
    bindingFuture
      .flatMap(_.unbind())
      .onComplete(_ => system.terminate())
  }
}