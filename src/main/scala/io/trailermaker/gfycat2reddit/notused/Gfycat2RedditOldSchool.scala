package io.trailermaker.gfycat2reddit.notused

import java.io.IOException

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import spray.json.DefaultJsonProtocol

import scala.concurrent.Future
import scala.util.Failure
import scala.util.Success

final case class GfyCat(
    gfyId:       String,
    gfyNumber:   String,
    webmUrl:     String,
    gifUrl:      String,
    posterUrl:   String,
    mjpgUrl:     String,
    frameRate:   Int,
    numFrames:   Int,
    mp4Size:     Int,
    webmSize:    Int,
    gifSize:     Int,
    nsfw:        String,
    mp4Url:      String,
    tags:        List[String],
    gfyName:     String,
    title:       String,
    description: String
)

final case class GfyCats(gfycats: List[GfyCat], cursor: String)

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val gcf  = jsonFormat17(GfyCat)
  implicit val gcf2 = jsonFormat2(GfyCats)
}

final case class Gfycat2Reddit() extends JsonSupport {

  implicit val system       = ActorSystem()
  implicit val executor     = system.dispatcher
  implicit val materializer = ActorMaterializer()

  println("senfing resuest")

  val responseFuture: Future[HttpResponse] =
    Http().singleRequest(HttpRequest(uri = "https://api.gfycat.com/v1/users/sannahparker/gfycats"))

  println("request sent")

  val res: Unit = responseFuture.onComplete(x =>
    x match {
      case Success(response) => {
        println(s"success! : ${response.status} ${response.entity.contentType}")
        response.status match {
          case OK if (response.entity.contentType == ContentTypes.`application/json`) => {
            println(s"got here!")
            Unmarshal(response.entity)
              .to[GfyCats]
              .map { cat =>
                println(cat.cursor)
              }
              .recoverWith {
                case x =>
                  println(s"failed: ${x.getMessage}")
                  Future.failed(new IOException("failed"))
              }
          }
          case BadRequest => Future.successful(Left(s"bad request"))
          case _ =>
            Unmarshal(response.entity).to[String].flatMap { entity =>
              val error = s"Google GeoCoding request failed with status code ${response.status} and entity $entity"
              Future.failed(new IOException(error))
            }
        }
      }
      case Failure(e) => println(e.toString)
  }) match { case _ => println("finished") }
}

object Gfycat2Reddit {

  def main(args: Array[String]): Unit =
    Gfycat2Reddit()
}
