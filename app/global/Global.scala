package global

import play.api._
import models._

object Global extends GlobalSettings {

  override def onStart(app: Application) {
    Logger.info("Application has started")
    //DAO.clean
    //DAO.init
  }  
  
  override def onStop(app: Application) {
    Logger.info("Application shutdown...")
  }  
    
}