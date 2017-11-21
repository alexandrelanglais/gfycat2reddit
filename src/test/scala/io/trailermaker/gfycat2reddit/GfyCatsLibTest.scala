package io.trailermaker.gfycat2reddit

import akka.actor.ActorSystem
import akka.testkit.ImplicitSender
import akka.testkit.TestKit
import better.files._
import org.scalatest._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@SuppressWarnings(Array("org.wartremover.warts.OptionPartial"))
class GfyCatLibTest extends TestKit(ActorSystem("GfyCatLibSpec")) with ImplicitSender with WordSpecLike with Matchers with BeforeAndAfterAll {
  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  "GfyCatLib" must {
    "be able to retrieve gfycats from a given user" ignore {
//      implicit val system: ActorSystem = ActorSystem("testsystem")
//
//      val user = "sannahparker"
//
//      for {
//        cats <- GfyCatLib.retrieveCatsFromUser(user, 100)
//        _ = assert(cats.gfycats.nonEmpty)
//      } yield Succeeded
    }
  }

}
