package request
import data.FullName

class Request(override val apiKey:String) extends API_json4(apiKey) {

  override def findActorId(firstName: String, lastName: String): Option[Int] = {
    super.findActorId(firstName, lastName)
  }

  override def findActorMovies(actorId: Int): Set[(Int, String)] = {
    super.findActorMovies(actorId)
  }

  override def findMovieDirector(movieId: Int): Option[(Int, String)] = {
    super.findMovieDirector(movieId)
  }

  override  def collaboration(actor1: FullName, actor2: FullName): Set[(String, String)] = {
    super.collaboration(actor1,actor2)
  }
}
