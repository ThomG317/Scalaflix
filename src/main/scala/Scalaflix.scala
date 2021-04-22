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

  /**
   * Execute request
   * @param url Full url of the request
   * @return Response of the request
   */
  private[this] def sourceFromURL(url: String): BufferedSource = Source.fromURL(url)

  /**
   * Execute a request using themoviedb API and authentication key
   * @param path Path of the request
   * @param request Parameters of the request
   * @return Response of the request
   */
  private[this] def sourceFromRequest(path: String, request: String): BufferedSource = sourceFromURL(s"$baseUrl$path?api_key=$api_key&$request")

  /**
   * Execute a request using themoviedb API and and return a json response
   * @param path Path of the request
   * @param request Query params of the request
   * @return JSON response of the request
   */
  private[this] def jsonFromRequest(path: String, request: String = ""): JValue = parse(sourceFromRequest(path, request).mkString)

  var cacheFindActorId: Map[(String, String), Int] = Map()

  /**
   * Find an actor's Id
   * @param name Name of the actor
   * @param surname Surname of the actor
   * @return Id of the first actor matching the request if existing
   */
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

 var cacheFindActorMovie: Map[Int, Set[Movie]] = Map()
  private[this] case class ActorCast(id: Int, original_title: String)

  /**
   * Find the movies an actor played in
   * @param id ID of the actor
   * @return list of movies (first page of results only)
   */
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

  var cacheCreditWorker: Map[Int, MovieDirector] = Map()
  private[this] case class CreditWorker(id: Int, known_for_department: String, name: String)

  /**
   * Find a movie director
   * @param movieId ID of the movie
   * @return the first director found for this movie if existing
   */
  def findMovieDirector(movieId: Int): Option[MovieDirector] = {
    // check if exist in cache
    val cache = cacheCreditWorker.get(movieId)
    if (cache.nonEmpty) return cache

    val movieList = (jsonFromRequest(s"/movie/$movieId/credits") \ "cast").extract[Set[CreditWorker]]
    val res = movieList.filter(x => x.known_for_department == "Directing").map(x => new MovieDirector(x.id, x.name)).headOption

    // store in cache
    if (res.nonEmpty) {
      cacheCreditWorker += (movieId -> res.get)
    }
    return res
  }

  var cacheRequest: Map[(Actor, Actor), Set[(String, String)]] = Map()

  /**
   * Find common movie between two actors
   * @param actor1 first actor of the pair
   * @param actor2 second actor of the pair
   * @return a list of the common movies (first page of results only)
   */
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

  /**
   * Find the pair of actors that played together the most
   * @return a list of the pair of actors
   */
  def findActorsOftenPaired(): Set[(Actor, Actor)] = {
    cacheRequest.toSeq
                .sortBy(_._2.size)
                .reverse
                .slice(0, cacheRequest.size / 2)
                .toSet
                .map(x => x._1)
  }
}
