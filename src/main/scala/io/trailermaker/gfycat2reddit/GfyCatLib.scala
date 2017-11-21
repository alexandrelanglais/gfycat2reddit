package io.trailermaker.gfycat2reddit

import java.io.IOException

import io.trailermaker.gfycat2reddit.common.GfyCat
import io.trailermaker.gfycat2reddit.common.GfyCats
import net.softler.client.ClientRequest

import scala.annotation.tailrec
import scala.concurrent.Future

object GfyCatLib {
  import Gfycat2Reddit._

  def retrieveCatsFromUser(user: String, limit: Int) = {
    ClientRequest(s"https://api.gfycat.com/v1/users/${user}/gfycats?count=$limit").withJson.get[GfyCats]
  }

  @SuppressWarnings(Array("org.wartremover.warts.Recursion"))
  def retrieveAllCatsFromUser(user: String, step: Int): Future[List[Future[GfyCats]]] = {
    def go(cursor: Option[String], lastRes: Future[GfyCats], curList: List[Future[GfyCats]]): Future[List[Future[GfyCats]]] = {
      println(s"Recurse call with cursor ${cursor.getOrElse("")}")
      lastRes.flatMap { cats => cats.cursor match {
        case "" => {
          val xx: List[Future[GfyCats]] = lastRes :: curList
          Future(xx)
        }
        case _ => go(Some(cats.cursor),
          ClientRequest(s"https://api.gfycat.com/v1/users/${user}/gfycats?count=$step&cursor=${cats.cursor}").withJson.get[GfyCats],
          lastRes :: curList
        )
      }
      }.recoverWith {
        case x => println(x)
          Future.failed(new IOException(x.toString))
      }


    }
    go(None, ClientRequest(s"https://api.gfycat.com/v1/users/${user}/gfycats?count=$step").withJson.get[GfyCats], Nil)
  }

}
