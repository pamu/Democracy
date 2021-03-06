package models

import scala.slick.driver.MySQLDriver.simple._
import scala.slick.lifted.TableQuery

object DAO {
	val users = TableQuery[Users]
	val topics = TableQuery[Topics]
	val posts = TableQuery[Posts]
	val comments = TableQuery[Comments]
	val endorsePosts = TableQuery[EndorsePosts]
	val endorseComments = TableQuery[EndorseComments]
	val subscriptions = TableQuery[Subscriptions]
	val topicSubscriptions = TableQuery[TopicSubscriptions]
	val userSubscriptions = TableQuery[UserSubscriptions]
	val postSubscriptions = TableQuery[PostSubscriptions]
	
	def db = Database.forURL(url = "jdbc:mysql://localhost/democracy_db", driver = "com.mysql.jdbc.Driver", user="root", password="root")
	
	def init = db.withSession(implicit session => {
	  (users.ddl ++ topics.ddl ++ posts.ddl ++ comments.ddl ++ endorsePosts.ddl ++ endorseComments.ddl ++ subscriptions.ddl 
	      ++ topicSubscriptions.ddl ++ userSubscriptions.ddl ++ postSubscriptions.ddl).create
	})
	
	def clean = db.withSession(implicit session => {
	  import scala.slick.jdbc.StaticQuery.interpolation
	  
	  val query = sql"drop database if exists democracy_db".as[String]
	  query.execute
	  val create = sql"create database democracy_db".as[String]
	  create.execute
	})
	
	def initTables = db.withSession(implicit session => {
	 import AutoInc._
	  val id = userAutoId.insert(User("nagarjuna.pamu@gmail.com","pamu_1234","mandi","inquisitive learner"))
	  println(s"id is $id")
	  val iid = userAutoId.insert(User("admin@gmail.com","admin_1234","hyd","inquisitive learner"))
	  println(s"id is $iid")
	  
	})
	
	/**
	 * get auto increment values id values during insertion of the data
	 */
	object AutoInc {
	  
	  def userAutoId = users returning users.map(_.id) into {
	  	case (_, id) => id 
	  }
	  
	  def postAutoId = posts returning posts.map(_.id) into {
	    case (_, id) => id
	  }
	  
	  def topicAutoId = topics returning topics.map(_.id) into {
	    case (_, id) => id
	  }
	  
	  def subscriptionId = subscriptions returning subscriptions.map(_.id) into {
	    case (_, id) => id
	  }
	}
	
	/**
	 * topics this project targets 
	 */
	def fillTopics = db.withTransaction(implicit tx => {
	  topics += Topic("Technology","technical issues")
	  topics += Topic("Social", "social issues")
	  topics += Topic("Culture", "cultural issues")
	  topics += Topic("Economic", "economic issues")
	  topics += Topic("Crime", "crime issues")
	  topics += Topic("Politics", "political issues")
	  topics += Topic("Cooruption", "cooruption related issues")
	  topics += Topic("Government", "issues related to poor working of government departments and government servents")
	  topics += Topic("Other", "other issues")
	})

}