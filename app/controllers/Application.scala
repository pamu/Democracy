package controllers

import play.api.mvc.{Action, Controller}
import play.api.Routes
import models._
import play.api.libs.json.Json
import play.api.libs.json._
import play.api.libs.functional.syntax._
import java.sql.Timestamp
import java.util.Date

object Application extends Controller with Secured {
  
  def index = IsAuthenticated { email => implicit request =>
    Ok(views.html.index("Welcome to Democracy"))
  }
  
  case class PostData(topicId: Long, content: String)
  def post() = Action(parse.json) { implicit request =>
    val session = request.session
    val email = session.get("email").get
    val user: User = UserDAO.findOneByEmail(email) match {
      case Some(user) => user
      case None => User("","","","",Some(1))
    }
    println("post route used")
    implicit val postDataReads: Reads[PostData] = (
        (JsPath \ "topicId").read[Long] and
        (JsPath \ "content").read[String]
        )(PostData.apply _)
        
    val jsonData = request.body
    jsonData.validate[PostData].fold(
        valid = { postData =>
          val post = Post(user.id.get, postData.topicId,new Timestamp(new Date().getTime()), postData.content, None)
          UserDAO.savePost(post)
          Ok("saved")
        },
        invalid = {
          errors => BadRequest(JsError.toFlatJson(errors))
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