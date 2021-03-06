package io.trailermaker.gfycat2reddit.gfycat

import java.io.File
import java.io.IOException
import java.nio.charset.Charset

import akka.event.Logging
import akka.http.javadsl.model.headers.ContentType
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.headers.`Content-Length`
import akka.http.scaladsl.server.directives.DebuggingDirectives
import akka.stream.scaladsl.FileIO
import akka.stream.scaladsl.Sink
import akka.stream.scaladsl.Source
import io.trailermaker.gfycat2reddit.Gfycat2Reddit
import io.trailermaker.gfycat2reddit.common.GfyCatUpload
import io.trailermaker.gfycat2reddit.common.GfyCatUploadRequest
import io.trailermaker.gfycat2reddit.common.GfyCats
import io.trailermaker.gfycat2reddit.common.GfyOAuthRequest
import io.trailermaker.gfycat2reddit.common.GfyOAuthResponse
import net.softler.client.ClientRequest
import net.softler.client.ClientRequest
import shaded.google.common.net.HttpHeaders

import scala.concurrent.Future
import scala.concurrent.duration._
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

  def requestUpload(token: String, file: File): Future[GfyCatUploadRequest] = {
    val fileName    = better.files.File(file.getAbsolutePath).nameWithoutExtension(false)
    val title       = fileName.replaceAll("_", " ")

    val gfyCatUpload = GfyCatUpload(title, 1)

    ClientRequest(s"https://api.gfycat.com/v1/gfycats")
      .headers(List(RawHeader("Authorization", s"Bearer $token")))
      .entity(gfyCatUpload.toJson.toString)
      .asJson
      .post[GfyCatUploadRequest]
      .recoverWith {
        case e => {
          println(e.getMessage)
          Future.failed(e)
        }
      }
  }

  private def createEntity(file: File): Future[RequestEntity] = {
    require(file.exists())
    val formData =
      Multipart.FormData(
        Multipart.FormData.BodyPart.Strict(
          "key",
          file.getName
        ),
        Multipart.FormData.BodyPart.fromPath(
          "file",
          ContentTypes.`application/octet-stream`,
          file.toPath,
          chunkSize = 8192
        )
      ).toStrict(15.seconds)
    Marshal(formData).to[RequestEntity]
  }

  def uploadFile(token: String, file: File, gfyName: String, uploadType: String) = {
    val renamedFile = new File(s"/tmp/$gfyName") // ./AWhaleInABoat
    val fetchUrl    = s"$uploadType/$gfyName"

    better.files.File(file.getAbsolutePath).copyTo(better.files.File(renamedFile.getCanonicalPath), true) // cp /tmp/path/file.webm -> ./backup
//    file.renameTo(renamedFile) // mv /tmp/path/file.webm -> ./AWhaleInABoat
//    better.files.File("backup.webm").moveTo(better.files.File(file.getAbsolutePath), true) // mv ./backup -> /tmp/path/file.webm

    println(renamedFile.length())
    for {
      ent <- createEntity(renamedFile)
      _ = println("Created entity")

      req <- Http().singleRequest(HttpRequest(uri = s"https://$uploadType", method = HttpMethods.POST, entity = ent))

      _ <- req.entity.dataBytes
            .runWith(Sink.head)
            .map(z => println(s"********** ${z.decodeString(Charset.defaultCharset())}"))

      _ = println(s"Sent binary file with req  ${req.toString}")
      _ = better.files.File(renamedFile.getCanonicalPath).delete()
    } yield req

  }
}
