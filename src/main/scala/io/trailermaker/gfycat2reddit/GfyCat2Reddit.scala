package io.trailermaker.gfycat2reddit

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

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val gcf  = jsonFormat18(GfyCat)
  implicit val gcf2 = jsonFormat2(GfyCats)
  implicit val rsf  = jsonFormat18(RedditSubmit)

  implicit val gforeq = jsonFormat5(GfyOAuthRequest)
  implicit val gfores = jsonFormat6(GfyOAuthResponse)
}

object Gfycat2Reddit extends JsonSupport {
  implicit val system       = ActorSystem()
  implicit val executor     = system.dispatcher
  implicit val materializer = ActorMaterializer()
  val scheduler             = system.scheduler

  def loadDatabase(token: String, markSent: Option[Boolean] = None): Unit = {
    val result = GfyCatLib.retrieveAllCatsFromUser(token, 200)

    result.map(_.map(_.map(cat => {
      println(s"${cat.gfycats.size} images")
      cat.gfycats.map(x => {
        println(s"${x.gfyId} => ${x.gifUrl}")
        MongoImpl.createGfyCatIfNotExists(x.copy(sentToReddit = markSent.orElse(Some(false))))
      })
    })))
  }

  def main(args: Array[String]): Unit = {
    val redditUsername    = args(0)
    val redditPassword    = args(1)
    val redditAppClientId = args(2)
    val redditAppSecret   = args(3)
    val subreddit         = args(4)
    val gfyAppId          = args(5)
    val gfyAppSecret      = args(6)
    val gfyUsername       = args(7)
    val gfyPassword       = args(8)

    val taskLoadDb = new Runnable {
      def run(): Unit = GfyCatLib.retrieveToken(gfyAppId, gfyAppSecret, gfyUsername, gfyPassword).map(res => loadDatabase(res.access_token))
    }

    val taskSendToReddit = new Runnable {
      def run(): Unit = {
        val reddit = RedditLib.initOAuth(redditUsername, redditPassword, redditAppClientId, redditAppSecret)

        MongoImpl
          .pickGfyCatNotSent()
          .map(optcat =>
            optcat.map(cat => {
              RedditLib.submitLink(reddit, subreddit, cat.webmUrl, cat.title)
              cat
            }))
          .flatMap(
            opt =>
              opt
                .fold(Future.failed[Int](new Exception("No more gfycat unposted"))) { cat =>
                  MongoImpl.updateGfyCat(cat.copy(sentToReddit = Some(true)))
              }
          )
      }
    }

    scheduler.schedule(initialDelay = 5.seconds, interval = 1.hour, runnable = taskLoadDb)

    scheduler.schedule(initialDelay = 15.seconds, interval = 15.minutes, runnable = taskSendToReddit)
  }

}
