package models

import scala.slick.driver.MySQLDriver.simple._
import DAO._

object UserDAO {
  
  /**
   * check if any user exists with given email
   */
	def findOneByEmail(email: String) = db.withTransaction(implicit tx => {
	 val query = for(user <- users.filter(_.email === email)) yield user
	 query.firstOption
	})
	
	/**
	 * check is password and email entered are correct
	 */
	def authenticate(email: String, password: String) = db.withTransaction(implicit tx => {
	  val query = for(user <- users.filter(_.email === email).filter(_.password === password)) yield user
	  query.exists.run
	})
	
	/**
	 * persist user object into database
	 */
	def saveUser(user: User) = db.withTransaction(implicit tx => {
	  users += user
	})
	
	/**
	 * persist post object into database
	 */
	def savePost(post: Post) = db.withTransaction(implicit tx => {
	  posts += post
	})
	
	/**
	 * check if post is endorsed by logged in user
	 */
	def isEndorsedPost(userId: Long, postId: Long) = db.withTransaction(implicit tx => {
	  val query = for(endorsePost <- endorsePosts.filter(_.endorserId === userId).filter(_.postId === postId)) yield endorsePost
	  query.exists.run
	})
	
	/**
	 * 
	 */
	def endorseOrDismissPost(userId: Long, postId: Long) = db.withTransaction(implicit tx => {
	  val query = for(endorsePost <- endorsePosts.filter(_.endorserId === userId).filter(_.postId === postId)) yield endorsePost
	  if(query.exists.run) {
	    query.delete
	  }else{
	    endorsePosts += EndorsePost(postId, userId, None)
	  }
	})
	
	/**
	 * 
	 */
	def isEndorsedComment(userId: Long, commentId: Long) = db.withTransaction(implicit tx => {
	  val query = for(endorseComment <- endorseComments.filter(_.endorserId === userId).filter(_.commentId === commentId)) yield endorseComment
	  query.exists.run
	})
	
	/**
	 * 
	 */
	def endorseOrDismissComment(userId: Long, commentId: Long) = db.withTransaction(implicit tx => {
	  val query = for(endorseComment <- endorseComments.filter(_.endorserId === userId).filter(_.commentId === commentId)) yield endorseComment
	  if(query.exists.run) {
	    query.delete
	  }else {
	    endorseComments += EndorseComment(commentId, userId, None)
	  }
	})
	
	/**
	 * # of endorses on post
	 */
	def noOfEndorsesOnPost(postId: Long) = db.withTransaction(implicit tx => {
	  val query = for(endorsePost <- endorsePosts.filter(_.postId === postId)) yield endorsePost
	  query.countDistinct
	})
	
	/**
	 * # of comments on the post
	 */
	def noOfCommentsOnPost(postId: Long) = db.withTransaction(implicit tx => {
	  val query = for(comment <- comments.filter(_.postId === postId)) yield comment
	  query.countDistinct
	})
	
	/**
	 * # of endorses comments 
	 */
	def noOfEndorsesOnComments(commentId: Long) = db.withTransaction(implicit tx => {
	  def query = for(endorseComment <- endorseComments.filter(_.commentId === commentId )) yield endorseComment
	  query.countDistinct
	})
}