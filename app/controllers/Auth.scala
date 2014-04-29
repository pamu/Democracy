package controllers

import play.api.mvc.{Controller, Action}
import play.api.data.Form
import play.api.data.Forms._
import models._

object Auth extends Controller {
  
  val loginForm = Form(tuple("username" -> nonEmptyText , "password" -> nonEmptyText) verifying("error.login", tuple => check(tuple._1, tuple._2)))
  
  def check(username: String, password: String): Boolean = {
    true
  }
  
  def loginPage() = Action {
    Ok(views.html.loginPage(loginForm))
  }
  
  def loginAuth() = Action { implicit request =>
    loginForm.bindFromRequest().fold(
      formWithErrors => BadRequest(views.html.loginPage(formWithErrors)),
      data => Redirect(routes.Application.index)
    )
  }
  
  val signupForm = Form(tuple("username" -> nonEmptyText , "password" -> nonEmptyText) verifying("error.exists", tuple => exists(tuple._1)))
  
  def exists(username: String): Boolean = {
    ! UserDAO.findIfOneExistsWith(username)
  }
  
  def signupPage() = Action {
    Ok(views.html.signupPage(signupForm))
  }
  
  def signupAuth() = Action { implicit request =>
    signupForm.bindFromRequest().fold(
        formWithErrors => BadRequest(views.html.signupPage(formWithErrors)),
        data => Ok("signup done :)")
        )
  }
  
}