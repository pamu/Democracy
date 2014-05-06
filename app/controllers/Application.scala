package controllers

import play.api.mvc.{Action, Controller}

object Application extends Controller with Secured {
  
  def index = IsAuthenticated{ email => implicit request =>
    Ok(views.html.index("Welcome to Democracy"))
  }
  
}