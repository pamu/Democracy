package models

import scala.slick.driver.MySQLDriver.simple._
import DAO._

object UserDAO {
  
	def findIfOneExistsWith(username: String) = db.withSession(implicit session => {
	 val list = (for(user <- users.filter(_.authId === username)) yield user).list
	 list.headOption match {
	   case None => false
	   case Some(user) => true
	 }
	})
	
}