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
  def index = withUser { user => implicit request =>
     val feed = UserDAO.getFeed(user.id.get)
    Ok(views.html.index(feed))
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
  	   		controllers.routes.javascript.Application.comment,
  	   		controllers.routes.javascript.Application.subscribePost,
  	   		controllers.routes.javascript.Application.subscribeUser,
  	   		controllers.routes.javascript.Application.subscribeTopic,
  	   		controllers.routes.javascript.Application.posts,
  	   		controllers.routes.javascript.Application.isTopicSubscribed,
  	   		controllers.routes.javascript.Application.isEndorsedPost
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
   * queries database get the feeds data and incrementally sends it to user
   */
  /*
  def feed() = withUser { user => implicit request =>
    val feed = UserDAO.getFeed(user.id.get)
    
    implicit val feedWrites: Writes[(User, Post)] = new Writes[(User, Post)] {
      def writes(feeds: ((User, Post)) ): JsValue = Json.obj(
        "uid" -> Json.toJson(feeds._1.id.get),
        "email" -> Json.toJson(feeds._1.email),
        "description" -> Json.toJson(feeds._1.desc),
        "location" -> Json.toJson(feeds._1.location),
        "postid" -> Json.toJson(feeds._2.id),
        "content" -> Json.toJson(feeds._2.content),
        "timestamp" -> Json.toJson(feeds._2.timestamp),
        "topicId" -> Json.toJson(feeds._2.topicId)
        )
    }
    
    Ok(Json.toJson(feed)).as(JAVASCRIPT)
  }*/
  
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
   * Helper class for taking comment data from json
   */
  case class CommentData(postId: Long, content: String)
  /**
   * action for adding comments to a post
   */
  def comment() = withUserWithBodyParser(parse.json) { user => implicit request =>
    /**
     * Reads ... helps in reading the JSON content and converts into CommentData object
     */
    implicit val commentDataReads: Reads[CommentData] = (
        (JsPath \ "postId").read[Long] and
        (JsPath \ "content").read[String](minLength[String](1))
        )(CommentData.apply _)
        
    request.body.validate[CommentData].fold(
        valid = {commentData =>
          val comment = Comment(user.id.get, commentData.postId, commentData.content)
          UserDAO.saveComment(comment)
          Ok("saved")
        },
        invalid = {errors =>
          Results.BadRequest(JsError.toFlatJson(errors))
        }
        )
  }
  
  def profile(email: String) = withUser { user => implicit request =>
    UserDAO.findOneByEmail(email) match {
      case Some(p) => Ok(views.html.user(p))
      case None => Results.Forbidden
    }
    
  }
  
  def subscribePost(id: Long) = withUser { user => implicit request =>
    val subscribed = UserDAO.isPostSubscribed(user.id.get, id)
    if(subscribed) {
      UserDAO.subscribeOrUnsubscribePost(user.id.get, id)
      Ok("unsubscribed").as(JAVASCRIPT)
    }else {
      UserDAO.subscribeOrUnsubscribePost(user.id.get, id)
      Ok("subscribed").as(JAVASCRIPT)
    }
  }
  
  def subscribeUser(id: Long) = withUser { user => implicit request =>
    val subscribed = UserDAO.isUserSubscribed(user.id.get, id)
    if(subscribed) {
      UserDAO.subscribeOrUnsubscribeUser(user.id.get, id)
      Ok("unsubscribed").as(JAVASCRIPT)
    }else {
      UserDAO.subscribeOrUnsubscribeUser(user.id.get, id)
      Ok("subscribed").as(JAVASCRIPT)
    }
  }
  
  def subscribeTopic(id: Long) = withUser { user => implicit request =>
     val subscribed = UserDAO.isTopicSubscribed(user.id.get, id)
    if(subscribed) {
      UserDAO.subscribeOrUnsubscribeTopic(user.id.get, id)
     Ok(Json.toJson("follow"))
    }else {
      UserDAO.subscribeOrUnsubscribeTopic(user.id.get, id)
      Ok(Json.toJson("unfollow"))
    }
  }
 
  /*
  def posts() = withUser { user => implicit request => 
    val posts = UserDAO.getPosts(user.id.get)
    implicit val postWrites: Writes[Post] = new Writes[Post] {
      def writes(p: Post): JsValue = Json.obj(
          "postId" -> p.id,
          "content" -> p.content,
          "userId" -> p.userId,
          "topicId" -> p.topicId,
          "timestamp" -> p.timestamp
      )
    }
    Ok(Json.toJson(posts))
  }
  */
  
  def posts() = withUser { user => implicit request =>
    val posts = UserDAO.getPosts(user.id.get)
  	Ok(views.html.posts(posts))
  }
  
  def topics = withUser { user => implicit request =>
		  Ok(views.html.topics(user))
  }
  
  def isEndorsedPost(id: Long) = withUser { user => implicit request =>
    val endorsed = UserDAO.isEndorsedPost(user.id.get, id)
    Ok(Json.toJson(endorsed)).as(JAVASCRIPT)
  }
  
  def isEndorsedComment(id: Long) = withUser { user => implicit request =>
    val endorsed = UserDAO.isEndorsedComment(user.id.get, id)
    Ok(Json.toJson(endorsed)).as(JAVASCRIPT)
  }
  
  def isUserSubscribed(id: Long) = withUser { user => implicit request =>
    val subscribed = UserDAO.isUserSubscribed(user.id.get, id)
    Ok(Json.toJson(subscribed)).as(JAVASCRIPT)
  }
  
   def isPostSubscribed(id: Long) = withUser { user => implicit request =>
    val subscribed = UserDAO.isPostSubscribed(user.id.get, id)
    Ok(Json.toJson(subscribed)).as(JAVASCRIPT)
  }
   
    def isTopicSubscribed(id: Long) = withUser { user => implicit request =>
    val subscribed = UserDAO.isTopicSubscribed(user.id.get, id)
    Ok(Json.toJson(subscribed)).as(JAVASCRIPT)
  }
    
  def showPost(postId: Long) = withUser { user => implicit request =>
    Ok("")
  }
  
}
