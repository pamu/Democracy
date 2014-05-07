package controllers

import play.api.mvc.{Action}
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
   * 
   */
  def IsAuthenticated(f: => String => Request[AnyContent] => Result) = Security.Authenticated(email, onUnauthorized) { email =>
    Action(request => f(email)(request))
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
  
  /**
   * now this method works with any body parser
   */
  def IsAuthenticatedWithBosyParser[A](p: BodyParser[A])(f: => String => Request[A] => Result) = Security.Authenticated(email, onUnauthorized) { user =>
    Action(p)(request => f(user)(request))
  }
  
  /**
   * now this method works with any body parser
   */
  def withUserWithBodyParser[A](p: BodyParser[A])(f: User => Request[A] => Result) = withAuth(p) { email => implicit request => 
    UserDAO.findOneByEmail(email) match {
      case Some(user) => f(user)(request)
      case None => Results.Forbidden
    }
  }
}