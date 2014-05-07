package models

import scala.slick.driver.MySQLDriver.simple._
import DAO._

object UserDAO {
  
	def findOneByEmail(email: String) = db.withSession(implicit session => {
	 val query = for(user <- users.filter(_.email === email)) yield user
	 query.firstOption
	})
	
	def authenticate(email: String, password: String) = db.withSession(implicit session => {
	  val query = for(user <- users.filter(_.email === email).filter(_.password === password)) yield user
	  query.firstOption match {
	    case Some(user) => true
	    case None => false
	  }
	})
	
	def saveUser(user: User) = db.withTransaction(implicit tx => {
	  users += user
	})
	
	def savePost(post: Post) = db.withTransaction(implicit tx => {
	  posts += post
	})
}