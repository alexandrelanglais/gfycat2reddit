package io.trailermaker.gfycat2reddit.mongo

import io.trailermaker.gfycat2reddit.common.GfyCat
import io.trailermaker.gfycat2reddit.mongo.MongoImpl.findGfyCatById
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.api.commands.WriteResult

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import reactivemongo.api.DefaultDB
import reactivemongo.api.MongoConnection
import reactivemongo.api.MongoDriver
import reactivemongo.bson.BSONDocumentReader
import reactivemongo.bson.BSONDocumentWriter
import reactivemongo.bson.Macros
import reactivemongo.bson.document

import scala.util.Failure
import scala.util.Success

@SuppressWarnings(Array("org.wartremover.warts.OptionPartial", "org.wartremover.warts.TryPartial"))
object MongoImpl {
  // My settings (see available connection options)
  val mongoUri = "mongodb://localhost:27917/gfycat2reddit?authMode=scram-sha1"

  import ExecutionContext.Implicits.global // use any appropriate context

  // Connect to the database: Must be done only once per application
  val driver     = MongoDriver()
  val parsedUri  = MongoConnection.parseURI(mongoUri)
  val connection = parsedUri.map(driver.connection(_))

  // Database and collections: Get references
  val futureConnection = Future.fromTry(connection)
  def db:         Future[DefaultDB]      = futureConnection.flatMap(_.database("g2r"))
  def collection: Future[BSONCollection] = db.map(_.collection("gifs"))

  // Write Documents: insert or update

  implicit def gfycatWriter: BSONDocumentWriter[GfyCat] = Macros.writer[GfyCat]
  // or provide a custom one

  def createGfyCat(gfycat: GfyCat): Future[Unit] = {
    val writeRes: Future[WriteResult] = collection.flatMap(_.insert(gfycat))

    writeRes.onComplete { // Dummy callbacks
      case Failure(e) => e.printStackTrace()
      case Success(writeResult) =>
        println(s"successfully inserted document with result: $writeResult")
    }

    writeRes.map(_ => {})
  }

  def createGfyCatIfNotExists(gfycat: GfyCat): Future[Unit] =
    findGfyCatById(gfycat.gfyId).flatMap(
      _.fold(createGfyCat(gfycat)) { _ =>
        Future(())
      }
    )

  def listGfyCats(): Future[List[GfyCat]] =
    for {
      coll <- collection
      gfys <- coll.find(document()).cursor[GfyCat].collect[List]()
    } yield gfys

  def updateGfyCat(gfycat: GfyCat): Future[Int] = {
    val selector = document(
      "gfyId" -> gfycat.gfyId
    )

    // Update the matching person
    collection.flatMap(_.update(selector, gfycat).map(_.n))
  }

  implicit def gfycatReader: BSONDocumentReader[GfyCat] = Macros.reader[GfyCat]
  // or provide a custom one

  def findGfyCatByDuration(duration: Int): Future[List[GfyCat]] =
    collection.flatMap(
      _.find(document("duration" -> duration)). // query builder
      cursor[GfyCat]().collect[List]()) // collect using the result cursor

  def findGfyCatById(id: String): Future[Option[GfyCat]] =
    for {
      coll <- collection
      cat <- coll.find(document("gfyId" -> id)).one
    } yield cat

  def pickGfyCatNotSent(): Future[Option[GfyCat]] =
    for {
      coll <- collection
      cat <- coll.find(document("sentToReddit" -> false)).one
    } yield cat

}
