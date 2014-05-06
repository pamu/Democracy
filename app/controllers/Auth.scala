package controllers

import play.api.mvc.{Controller, Action}
import play.api.data.Form
import play.api.data.Forms._
import models._

object Auth extends Controller {
  
  val loginForm = Form(
      
      tuple(
          
          "email" -> email,
          
          "password" -> nonEmptyText(minLength = 6)
          
          ).verifying(
              "error.loginfailed",
              tuple => check(tuple._1, tuple._2)
              )
             )
  
  def check(email: String, password: String): Boolean = {
    UserDAO.authenticate(email, password)
  }
  
  def loginPage() = Action {
    Ok(views.html.loginPage(loginForm))
  }
  
  def loginAuth() = Action { implicit request =>
    
    loginForm.bindFromRequest().fold(
        
      formWithErrors => BadRequest(views.html.loginPage(formWithErrors)),
      
      user => Redirect(routes.Application.index()).withSession("email" -> user._1)
      
    )
    
  }
  
  val signupForm = Form(
      
		  mapping(
		      "email" -> email,
		      
		      // Create a tuple mapping for the password/confirm
		      "password" -> tuple(
		        "main" -> nonEmptyText(minLength = 6),
		        "confirm" -> nonEmptyText
		       ).verifying(
		        // Add an additional constraint: both passwords must match
		    	"passwords do not match", data => data._1 == data._2
		    	),
		    	
		      "location" -> nonEmptyText,
		      
		      "description" -> nonEmptyText
		       
		      )//mapping
		      {
			  	(email, passwords, location, description) => User(email, passwords._1, location, description, None)
		      }
		  	  {
		  		user => Some(user.email, (user.password, ""), user.location, user.desc)
		      }
		      )//form
		      	
		  	
  
  def exists(email: String): Boolean = {
    UserDAO.findOneByEmail(email) match {
      case Some(user) => true
      case None => false
    }
  }
  
  def signupPage() = Action {
    Ok(views.html.signupPage(signupForm))
  }
  
  def signupAuth() = Action { implicit request =>
    signupForm.bindFromRequest().fold(
        formWithErrors => BadRequest(views.html.signupPage(formWithErrors)),
        user => Ok(s"${user.email} is @ ${user.location}")
        )
  }
  
}