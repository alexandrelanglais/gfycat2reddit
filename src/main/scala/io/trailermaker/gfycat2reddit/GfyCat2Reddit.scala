package io.trailermaker.gfycat2reddit

import akka.actor.ActorSystem
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.stream.ActorMaterializer
import io.trailermaker.gfycat2reddit.common.GfyCat
import io.trailermaker.gfycat2reddit.common.GfyCats
import net.softler.client.ClientRequest
import spray.json.DefaultJsonProtocol

import scala.concurrent.Future

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val gcf  = jsonFormat17(GfyCat)
  implicit val gcf2 = jsonFormat2(GfyCats)
}

object Gfycat2Reddit extends App with JsonSupport {
  implicit val system       = ActorSystem()
  implicit val executor     = system.dispatcher
  implicit val materializer = ActorMaterializer()

//  val result: Future[GfyCats] = GfyCatLib.retrieveCatsFromUser("sannahparker", 200)
//
//  result.map { x =>
//    println(s"${x.gfycats.size} images")
//    x.gfycats.map(cat => println(s"${cat.gfyId} => ${cat.gifUrl}"))
//  }

  val allImages = GfyCatLib.retrieveAllCatsFromUser("sannahparker", 200)

  allImages.map(_.map(_.map(cat => {
    println(s"${cat.gfycats.size} images")
    cat.gfycats.map(x => println(s"${x.gfyId} => ${x.gifUrl}"))
  })))

}
