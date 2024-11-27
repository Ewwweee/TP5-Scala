import org.json4s._; import org.json4s.native.JsonMethods._
import scala.io.Source

object Request {
  
  private def do_request(url:String) : org.json4s.JValue = {
    val source = Source.fromURL(url)
    val contents = source.mkString

    parse(contents)
  }
}
