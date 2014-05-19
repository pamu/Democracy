package controllers

import play.api.mvc.{Controller, Action}
import play.api.data.Form
import play.api.data.Forms._
import models._

object Auth extends Controller {
  
  /**
   * Login Form definition, constraints and errors (errors definitions are in conf/messages)
   */
  val loginForm = Form(
      
      tuple(
          
          "email" -> email,
          
          "password" -> nonEmptyText(minLength = 6)
          
          ).verifying(
              "error.loginfailed",
              tuple => check(tuple._1, tuple._2)
              )
             )
  
  /**
   * check email and password , triggers a database access
   */
  def check(email: String, password: String): Boolean = {
    UserDAO.authenticate(email, password)
  }
  
  /**
   * fetches a login page
   */
  def loginPage() = Action {implicit request =>
    Ok(views.html.loginPage(loginForm))
  }
  
  /**
   * action to which login form will be submitted
   */
  def loginAuth() = Action { implicit request =>
    loginForm.bindFromRequest().fold(
      formWithErrors => BadRequest(views.html.loginPage(formWithErrors)),
      user => Redirect(routes.Application.index()).withSession("email" -> user._1) 
    )
  }
  
  /**
   * Signup form definition
   */
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
		      
		      //binding information
		      {
			  	(email, passwords, description, location) => User(email, passwords._1, location, description, None)
		      }
		  	  //unbinding information
		  	  {
		  		user => Some(user.email, (user.password, ""), user.location, user.desc)
		      }.verifying("error.email_exists", user => {
		        UserDAO.findOneByEmail(user.email) match {
		          case Some(user) => false
		          case None => true
		        }
		      })
		  	  
		)//form
		      	
		  	
  /**
   * check whether user with given email exists
   */
  def exists(email: String): Boolean = {
    /**
     * Check is email exists in the database
     */
    UserDAO.findOneByEmail(email) match {
      case Some(user) => true
      case None => false
    }
    
  }
  
  /**
   * fetch signupPage
   */
  def signupPage() = Action {implicit request =>
    Ok(views.html.signupPage(signupForm))
  }
  
  /**
   * target action for signup form
   */
  def signupAuth() = Action { implicit request =>
    signupForm.bindFromRequest().fold(
        formWithErrors => BadRequest(views.html.signupPage(formWithErrors)),
        /**
         * One user signs up redirect to login page
         */
        user => {
          UserDAO.saveUser(user)
          Redirect(routes.Auth.loginPage())
        }
     )
  }
  
  /**
   * logout action discards the cookies
   */
  def logout() = Action {
    Redirect(routes.Auth.loginPage()).withNewSession
  }
  
}