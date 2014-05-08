package controllers

import play.api.mvc.{Action, Controller}
import play.api.Routes
import models._
import play.api.libs.json.Json
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import java.sql.Timestamp
import java.util.Date
import play.api.mvc.Results

object Application extends Controller with Secured {
  
  /**
   * Index action ... for index page for news feed page
   */
  def index = IsAuthenticated { email => implicit request =>
    Ok(views.html.index("Welcome to Democracy"))
  }
  
  /**
   * class for post data
   */
  case class PostData(topicId: Long, content: String)
  
  /**
   * post action which consumes post data in the form of JSON
   */
  def post() = withUserWithBodyParser(parse.json) { user => implicit request =>
    /**
     * Reads[PostData] helps to convert Json data to PostData object which will be further converted to POST object and persisted in Database
     */
    implicit val postDataReads: Reads[PostData] = (
        (JsPath \ "topicId").read[Long] and
        (JsPath \ "content").read[String](minLength[String](10))
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
  
  /**
   * Javascript Routes action
   */
  def javascriptRoutes() = Action { implicit request =>
  	import routes.javascript._
  	Ok(Routes.javascriptRouter("jsRoutes")(
  			controllers.routes.javascript.Application.post,
  	   		controllers.routes.javascript.Application.message,
  	   		controllers.routes.javascript.Application.endorseOrDismissPost,
  	   		controllers.routes.javascript.Application.endorseOrDismissComment,
  	   		controllers.routes.javascript.Application.comment
  	        )
  	    ).as(JAVASCRIPT)
  	}

  /**
   * example of exposing action as javascript routes 
   */
  def message = Action {
    Ok(Json.toJson("hello world"))
  }
  
  /**
   * news feed
   */
  
 /**
  *  like or unlike post and send the current state
  */
  def endorseOrDismissPost(postId: Long) = withUser { user => implicit request =>
    val endorsed = UserDAO.isEndorsedPost(user.id.get, postId)
    if(endorsed){
      UserDAO.endorseOrDismissPost(user.id.get, postId)
      Ok(Json.toJson("endorse"))
    }else {
      UserDAO.endorseOrDismissPost(user.id.get, postId)
      Ok(Json.toJson("dismiss"))
    }
    
  }
  
  /**
   * like or unlike comment
   */
  def endorseOrDismissComment(commentId: Long) = withUser { user => implicit request => 
    val endorsed = UserDAO.isEndorsedComment(user.id.get, commentId)
    if(endorsed) {
      UserDAO.endorseOrDismissComment(user.id.get, commentId)
      Ok(Json.toJson("endorse"))
    }else {
     UserDAO.endorseOrDismissComment(user.id.get, commentId)
     Ok(Json.toJson("dismiss"))
    }
  }
  
  /**
   * action for adding comments to a post
   */
  def comment() = withUserWithBodyParser(parse.json) { user => implicit request =>
    Ok("")
  }
  
}
