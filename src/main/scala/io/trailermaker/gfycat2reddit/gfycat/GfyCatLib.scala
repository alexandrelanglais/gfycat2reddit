package io.trailermaker.gfycat2reddit.gfycat

import java.io.IOException

import akka.http.javadsl.model.headers.ContentType
import akka.http.scaladsl.model.ContentType
import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.model.HttpHeader
import akka.http.scaladsl.model.MediaTypes
import akka.http.scaladsl.model.headers.RawHeader
import io.trailermaker.gfycat2reddit.Gfycat2Reddit
import io.trailermaker.gfycat2reddit.common.GfyCatUploadRequest
import io.trailermaker.gfycat2reddit.common.GfyCats
import io.trailermaker.gfycat2reddit.common.GfyOAuthRequest
import io.trailermaker.gfycat2reddit.common.GfyOAuthResponse
import net.softler.client.ClientRequest

import scala.concurrent.Future
import spray.json._

object GfyCatLib {
  import Gfycat2Reddit._

  def retrieveCatsFromUser(user: String, limit: Int) =
    ClientRequest(s"https://api.gfycat.com/v1/users/$user/gfycats?count=$limit").withJson.get[GfyCats].recoverWith {
      case e => {
        println(e.getMessage)
        Future.failed(e)
      }
    }

  def retrieveToken(appId: String, appSecret: String, username: String, passwd: String): Future[GfyOAuthResponse] = {
    val req = GfyOAuthRequest(appId, appSecret, "password", username, passwd)

    ClientRequest(s"https://api.gfycat.com/v1/oauth/token").entity(req.toJson.toString).post[GfyOAuthResponse].recoverWith {
      case e => {
        println(e.getMessage)
        Future.failed(e)
      }
    }
  }

  @SuppressWarnings(Array("org.wartremover.warts.Recursion"))
  def retrieveAllCatsFromUser(token: String, step: Int): Future[List[Future[GfyCats]]] = {
    def go(cursor: Option[String], lastRes: Future[GfyCats], curList: List[Future[GfyCats]]): Future[List[Future[GfyCats]]] = {
      println(s"Recurse call with cursor ${cursor.getOrElse("")}")
      lastRes
        .flatMap { cats =>
          cats.cursor match {
            case "" => {
              val xx: List[Future[GfyCats]] = lastRes :: curList
              Future(xx)
            }
            case _ =>
              go(
                Some(cats.cursor),
                ClientRequest(s"https://api.gfycat.com/v1/me/gfycats?count=$step&cursor=${cats.cursor}")
                  .headers(List(RawHeader("Authorization", s"Bearer $token")))
                  .withJson
                  .get[GfyCats],
                lastRes :: curList
              )
          }
        }
        .recoverWith {
          case x =>
            println(x)
            Future.failed(new IOException(x.toString))
        }

    }
    go(
      None,
      ClientRequest(s"https://api.gfycat.com/v1/me/gfycats?count=$step").headers(List(RawHeader("Authorization", s"Bearer $token"))).withJson.get[GfyCats],
      Nil
    )
  }

  def requestUpload(): Future[GfyCatUploadRequest] =
    ClientRequest(s"https://api.gfycat.com/v1/gfycats").entity("").asJson.post[GfyCatUploadRequest].recoverWith {
      case e => {
        println(e.getMessage)
        Future.failed(e)
      }
    }

}
