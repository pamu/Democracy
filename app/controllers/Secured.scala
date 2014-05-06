package controllers

import play.api.mvc._
import models.User
import models.UserDAO

trait Secured {
  
   /**
   * Retrieve the connected user email.
   */
  private def email(request: RequestHeader) = request.session.get("email")
  
  /**
   * Redirect to login if the user in not authorized.
   */
  private def onUnauthorized(request: RequestHeader) = Results.Redirect(routes.Auth.loginPage())
  
   /** 
   * Action for authenticated users.
   */
  def IsAuthenticated(f: => String => Request[AnyContent] => Result) = Security.Authenticated(email, onUnauthorized) { user =>
    Action(request => f(user)(request))
  }
  
  /**
   * This method shows how you could wrap the isAuthenticated method to also fetch your user
   * You will need to implement UserDAO.findOneByEmail
   */
  def withUser(f: User => Request[AnyContent] => Result) = IsAuthenticated{ email => implicit request =>
    UserDAO.findOneByEmail(email) match {
      case Some(user) => f(user)(request)
      case None => Results.Forbidden//onUnauthorized(request)
    }
  }
  
}