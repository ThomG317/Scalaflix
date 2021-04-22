import model.{Actor, Movie, MovieDirector}
import org.json4s.{DefaultFormats, JValue}
import org.json4s.native.JsonMethods.parse

import scala.io.{BufferedSource, Source}

object Scalaflix {
  // json4s var
 private[this] implicit val formats: DefaultFormats.type = org.json4s.DefaultFormats

  // API var
  private[this] val baseUrl = "https://api.themoviedb.org/3"
  private[this] val api_key = "344e85c6d6fcb941f8c0f5da6200c97a"

  // APi methods
  private[this] def sourceFromURL(url: String): BufferedSource = Source.fromURL(url)
  private[this] def sourceFromRequest(path: String, request: String): BufferedSource = sourceFromURL(s"$baseUrl$path?api_key=$api_key&$request")
  private[this] def sourceFromRequestParams(path: String, requestParams: List[String] = Nil): BufferedSource = sourceFromRequest(path, requestParams.mkString("&"))
  private[this] def jsonFromRequest(path: String, request: String = ""): JValue = parse(sourceFromRequest(path, request).mkString)

  private[this] var cacheFindActorId: Map[(String, String), Int] = Map()
  def findActorId(name: String, surname: String): Option[Int] = {
    // check if exist in cache
    val cache = cacheFindActorId.get((name, surname))
    if (cache.nonEmpty) return cache

    val jval = jsonFromRequest("/search/person", s"query=$name%20$surname")
    val res = (jval \ "results").extract[List[Any]] match {
      case Nil => None
      case _ :: _ => ((jval \ "results")(0) \ "id").extract[Option[Int]]
    }
    // store in cache
    if (res.nonEmpty) cacheFindActorId += ((name, surname) -> res.get)
    return res;
  }

  private[this] var cacheFindActorMovie: Map[Int, Set[Movie]] = Map()
  private[this] case class ActorCast(id: Int, original_title: String)
  def findActorMovies(id: Int): Set[Movie] = {
    // check if exist in cache
    val cache = cacheFindActorMovie.get(id)
    if (cache.nonEmpty) return cache.get

    val movieList = (jsonFromRequest(s"/person/$id/movie_credits") \ "cast").extract[Set[ActorCast]]
    val res = movieList.map(x => new Movie(x.id, x.original_title))

    // store in cache
    cacheFindActorMovie += (id -> res)
    return  res
  }

  private[this] var cacheCreditWorker: Map[Int, MovieDirector] = Map()
  private[this] case class CreditWorker(id: Int, known_for_department: String, name: String)
  def findMovieDirector(id: Int): Option[MovieDirector] = {
    // check if exist in cache
    val cache = cacheCreditWorker.get(id)
    if (cache.nonEmpty) return cache

    val movieList = (jsonFromRequest(s"/movie/$id/credits") \ "cast").extract[Set[CreditWorker]]
    val res = movieList.filter(x => x.known_for_department == "Directing").map(x => new MovieDirector(x.id, x.name)).headOption

    // store in cache
    if (res.nonEmpty) {
      cacheCreditWorker += (id -> res.get)
    }
    return res
  }

  private[this] var cacheRequest: Map[(Actor, Actor), Set[(String, String)]] = Map()
  def request(actor1: Actor, actor2: Actor): Set[(String, String)] = {
    // check if exist in cache
    val cache = cacheRequest.get((actor1, actor2))
    if (cache.nonEmpty) return cache.get


    def moviesFromFullName(fn: Actor): Set[Movie] = findActorId(actor1.name, actor1.surname) match {
      case Some(id1) => findActorMovies(id1)
      case None => Set.empty
    }

    val movies1: Set[Movie] = moviesFromFullName(actor1)
    val movies2: Set[Movie] = moviesFromFullName(actor2)

    val commonMovies = movies1.intersect(movies2)

    val res = commonMovies.map(movie => findMovieDirector(movie.id) match {
      case Some(director) => (movie.title, director.name)
      case None => (movie.title, "")
    })

    // store in cache
    if (res.nonEmpty) cacheRequest += ((actor1, actor2) -> res)
    return  res
  }
}
