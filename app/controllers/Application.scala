package controllers

import play.api.mvc.{Action, Controller}
import play.api.Routes
import models._
import play.api.libs.json.Json
import play.api.libs.json._
import play.api.libs.functional.syntax._
import java.sql.Timestamp
import java.util.Date
import play.api.mvc.Results

object Application extends Controller with Secured {
  
  def index = IsAuthenticated { email => implicit request =>
    Ok(views.html.index("Welcome to Democracy"))
  }
  
  /**
   * class for post data
   */
  case class PostData(topicId: Long, content: String)
  
  def post() = withUserWithBodyParser(parse.json) { user => implicit request =>
    /**
     * Reads[PostData] helps to convert Json data to PostData object which will be further converted to POST object and persisted in Database
     */
    implicit val postDataReads: Reads[PostData] = (
        (JsPath \ "topicId").read[Long] and
        (JsPath \ "content").read[String]
        )(PostData.apply _)
    request.body.validate[PostData].fold(
        valid = { postData =>
          val post = Post(user.id.get, postData.topicId, new Timestamp(new Date().getTime()), postData.content, None)
          UserDAO.savePost(post)
          Ok("saved to Database")
        },
        invalid = { errors =>
          BadRequest(JsError.toFlatJson(errors))
        }
    )
  }
 
 
  
  def javascriptRoutes() = Action { implicit request =>
  	import routes.javascript._
  	Ok(Routes.javascriptRouter("jsRoutes")(
  			controllers.routes.javascript.Application.post,
  	   		controllers.routes.javascript.Application.message
  	        )
  	    ).as(JAVASCRIPT)
  	}

  def message = Action {
    Ok(Json.toJson("hello world"))
  }
}