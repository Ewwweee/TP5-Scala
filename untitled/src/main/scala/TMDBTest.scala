import Request._

object TMDBTest extends App {

  // Test 1: Find Actor ID
  val actorId: Option[Int] = findActorId("Leonardo", "DiCaprio")
  println(s"Actor ID for Leonardo DiCaprio: $actorId")

  // Test 2: Find Movies for an Actor
  actorId.foreach { id =>
    val movies = findActorMovies(id)
    println(s"Movies for Leonardo DiCaprio: $movies")
  }

  // Test 3: Find Collaboration between Two Actors
  private val collaborationMovies = collaboration(("Leonardo", "DiCaprio"), ("Kate", "Winslet"))
  println(s"Movies with Leonardo DiCaprio and Kate Winslet: $collaborationMovies")
  
  println("________________________________________________")

  import TMDBCache._

  // Example: Find director for a movie
  val movieId = 272 // Example movie ID
  val director: Any = findMovieDirector(movieId)
  println(s"Director of movie $movieId: $director")

  // Example: Call again to demonstrate caching
  val cachedDirector: Any = findMovieDirector(movieId)
  println(s"Cached Director of movie $movieId: $cachedDirector")
}