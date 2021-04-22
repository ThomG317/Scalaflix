import model.Actor

import scala.io.{BufferedSource, Source}
import org.json4s._
import org.json4s.native.JsonMethods._

object main extends App {

  // println(findActorId("jennifer", ""))
  Scalaflix.findActorId("jennifer", "")

  Scalaflix.findActorMovies(4491)

  Scalaflix.findMovieDirector(744539)

  // println(request(new FullName("john", "legend"), new FullName("shia", "labeouf")))
  Scalaflix.request(new Actor("john", "legend"), new Actor("shia", "labeouf"))


  println(Scalaflix.cacheFindActorId)
  println(Scalaflix.cacheFindActorMovie)
  println(Scalaflix.cacheCreditWorker)
  println(Scalaflix.cacheRequest)
}
