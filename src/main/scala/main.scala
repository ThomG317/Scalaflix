import Scalaflix.FullName
import model.Actor

import scala.io.{BufferedSource, Source}
import org.json4s._
import org.json4s.native.JsonMethods._

object main extends App {

  // println(findActorId("jennifer", ""))
  Scalaflix.findActorId("jennifer", "aniston")

  Scalaflix.findActorMovies(4491)

  Scalaflix.findMovieDirector(744539)

  // println(request(new FullName("john", "legend"), new FullName("shia", "labeouf")))
  Scalaflix.request(new FullName("john", "legend"), new FullName("shia", "labeouf"))
  Scalaflix.request(new FullName("john", "legend"), new FullName("jennifer", "aniston"))
  Scalaflix.request(new FullName("shia", "labeouf"), new FullName("jennifer", "aniston"))


  println(Scalaflix.cacheFindActorId)
  println(Scalaflix.cacheFindActorMovie)
  println(Scalaflix.cacheCreditWorker)
  println(Scalaflix.cacheRequest)
  println(Scalaflix.findActorsOftenPaired())
}
