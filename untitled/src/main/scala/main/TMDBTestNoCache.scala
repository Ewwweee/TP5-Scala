package main
import request.Request
import data.FullName

object TMDBTestNoCache extends App{
  val apikey:String = "87551e2a0dd79b7a73f084bb30486087"
  val request = Request(apikey)

  // Test 1: Find Actor ID
  val actorId: Option[Int] = request.findActorId("Leonardo", "DiCaprio")
  println(s"Actor ID for Leonardo DiCaprio: $actorId")

  // Test 2: Find Movies for an Actor
  actorId.foreach { id =>
    val movies = request.findActorMovies(id)
    println(s"Movies for Leonardo DiCaprio: $movies")
  }

  val actor1 = new FullName("Leonardo", "DiCaprio")
  val actor2 = new FullName("Kate", "Winslet")

  // Test 3: Find Collaboration between Two Actors
  private val collaborationMovies = request.collaboration(actor1, actor2)
  println(s"Movies with Leonardo DiCaprio and Kate Winslet: $collaborationMovies")

  println("________________________________________________")
}
