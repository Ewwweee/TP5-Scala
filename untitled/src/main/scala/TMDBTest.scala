import Request._

object TMDBTest extends App {

  // Test 1: Find Actor ID
  val actorId = findActorId("Leonardo", "DiCaprio")
  println(s"Actor ID for Leonardo DiCaprio: $actorId")

  // Test 2: Find Movies for an Actor
  actorId.foreach { id =>
    val movies = findActorMovies(id)
    println(s"Movies for Leonardo DiCaprio: $movies")
  }

  // Test 3: Find Collaboration between Two Actors
  val collaborationMovies = collaboration(("Leonardo", "DiCaprio"), ("Kate", "Winslet"))
  println(s"Movies with Leonardo DiCaprio and Kate Winslet: $collaborationMovies")
}