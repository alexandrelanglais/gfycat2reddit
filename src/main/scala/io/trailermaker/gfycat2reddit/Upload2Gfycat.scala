package io.trailermaker.gfycat2reddit

import java.io.File
import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.stream.ActorMaterializer
import io.trailermaker.gfycat2reddit.common._
import io.trailermaker.gfycat2reddit.gfycat.GfyCatLib
import io.trailermaker.gfycat2reddit.mongo.MongoImpl
import io.trailermaker.gfycat2reddit.reddit.RedditLib
import spray.json.DefaultJsonProtocol

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

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
      upReq <- GfyCatLib.requestUpload(token.access_token)
      res <- GfyCatLib.uploadFile(token.access_token, new File("OfficialMedicalIcterinewarbler"), upReq.gfyname, upReq.uploadType)
    } yield res

    fut.recoverWith {
      case e => {
        println(e.getMessage)
        Future.failed(e)
      }
    }

  }

}
