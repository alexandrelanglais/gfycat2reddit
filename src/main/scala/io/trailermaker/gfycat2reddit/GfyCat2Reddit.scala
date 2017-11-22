package io.trailermaker.gfycat2reddit

import akka.actor.ActorSystem
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.stream.ActorMaterializer
import io.trailermaker.gfycat2reddit.common.GfyCat
import io.trailermaker.gfycat2reddit.common.GfyCats
import io.trailermaker.gfycat2reddit.common.RedditSubmit
import io.trailermaker.gfycat2reddit.gfycat.GfyCatLib
import io.trailermaker.gfycat2reddit.mongo.MongoImpl
import io.trailermaker.gfycat2reddit.reddit.RedditLib
import spray.json.DefaultJsonProtocol

import scala.concurrent.Future

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val gcf  = jsonFormat18(GfyCat)
  implicit val gcf2 = jsonFormat2(GfyCats)
  implicit val rsf  = jsonFormat18(RedditSubmit)
}

object Gfycat2Reddit extends JsonSupport {
  implicit val system       = ActorSystem()
  implicit val executor     = system.dispatcher
  implicit val materializer = ActorMaterializer()

  def loadDatabase(gfycatUser: String): Unit = {
    val result = GfyCatLib.retrieveAllCatsFromUser(gfycatUser, 200)

    result.map(_.map(_.map(cat => {
      println(s"${cat.gfycats.size} images")
      cat.gfycats.map(x => {
        println(s"${x.gfyId} => ${x.gifUrl}")
        MongoImpl.createGfyCatIfNotExists(x.copy(sentToReddit = Some(false)))
      })
    })))
  }

  def main(args: Array[String]): Unit = {
    val username    = args(0)
    val passwd      = args(1)
    val appClientId = args(2)
    val appSecret   = args(3)
    val subreddit   = args(4)
    val gfycatUser  = args(5)
    val reddit      = RedditLib.initOAuth(username, passwd, appClientId, appSecret)

    MongoImpl
      .pickGfyCatNotSent()
      .map(optcat =>
        optcat.map(cat => {
          RedditLib.submitLink(reddit, subreddit, cat.gifUrl.getOrElse(""), cat.title)
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
