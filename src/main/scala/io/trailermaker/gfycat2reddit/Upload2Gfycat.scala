package io.trailermaker.gfycat2reddit

import java.io.File
import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import io.trailermaker.gfycat2reddit.common._
import io.trailermaker.gfycat2reddit.gfycat.GfyCatLib
import io.trailermaker.gfycat2reddit.mongo.MongoImpl
import io.trailermaker.gfycat2reddit.reddit.RedditLib
import spray.json.DefaultJsonProtocol

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.RawHeader

object Upload2Gfycat extends JsonSupport {

  implicit val system       = ActorSystem()
  implicit val executor     = system.dispatcher
  implicit val materializer = ActorMaterializer()
  val scheduler             = system.scheduler

  def main(args: Array[String]): Unit = {
    import Gfycat2Reddit._
    val filePath     = args(0)
    val gfyAppId     = args(1)
    val gfyAppSecret = args(2)
    val gfyUsername  = args(3)
    val gfyPassword  = args(4)

    val fut = for {
      token <- GfyCatLib.retrieveToken(gfyAppId, gfyAppSecret, gfyUsername, gfyPassword)
      _ = println(s"Got token ${token}")
      upReq <- GfyCatLib.requestUpload(token.access_token, new File(filePath))
      _ = println(s"Got uppload name ${upReq.gfyname}")
      res <- GfyCatLib.uploadFile(token.access_token, new File(filePath), upReq.gfyname, upReq.uploadType)
    } yield res

    fut.recoverWith {
      case e => {
        println(e.getMessage)
        e.printStackTrace()
        Future.failed(e)
      }
    }

  }

}
