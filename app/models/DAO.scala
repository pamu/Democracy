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
	
	def db = Database.forURL(url = "jdbc:mysql://localhost/play", driver = "com.mysql.jdbc.Driver", user="root", password="Android@4.4.2")
	
	def init = db.withSession(implicit session => {
	  (users.ddl ++ topics.ddl ++ posts.ddl ++ comments.ddl ++ endorsePosts.ddl ++ endorseComments.ddl ++ subscriptions.ddl 
	      ++ topicSubscriptions.ddl ++ userSubscriptions.ddl ++ postSubscriptions.ddl).create
	})
	
	def clean = db.withSession(implicit session => {
	  import scala.slick.jdbc.StaticQuery.interpolation
	  
	  val query = sql"drop database if exists play".as[String]
	  query.execute
	  val create = sql"create database play".as[String]
	  create.execute
	})
	
	def initTables = db.withSession(implicit session => {
	  //Insert users
	  users += User(11111, "admin", "1234", "inquisitive")
	  //Insert Topics
	  topics += Topic("politics", "your region politics ideas and problems")
	 
	  
	})
}