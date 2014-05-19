package models

import scala.slick.driver.MySQLDriver.simple._
import DAO._
import scala.slick.ast.SortBy

case class Page[A](items: Seq[A], page: Int, offset: Long, total: Long) {
  lazy val prev = Option(page - 1).filter(_ >= 0)
  lazy val next = Option(page + 1).filter(_ => (offset + items.size) < total)
}	

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
	 * endorse or dismiss the post
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
	 * check endorse state of comment 
	 */
	def isEndorsedComment(userId: Long, commentId: Long) = db.withTransaction(implicit tx => {
	  val query = for(endorseComment <- endorseComments.filter(_.endorserId === userId).filter(_.commentId === commentId)) yield endorseComment
	  query.exists.run
	})
	
	/**
	 * endorse or dismiss the comment
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
	  query.length.run
	})
	
	/**
	 * # of comments on the post
	 */
	def noOfCommentsOnPost(postId: Long) = db.withTransaction(implicit tx => {
	  val query = for(comment <- comments.filter(_.postId === postId)) yield comment
	  query.length.run
	})
	
	/**
	 * # of endorses comments 
	 */
	def noOfEndorsesOnComments(commentId: Long) = db.withTransaction(implicit tx => {
	  def query = for(endorseComment <- endorseComments.filter(_.commentId === commentId )) yield endorseComment
	  query.length.run
	})
	
	/**
	 * add comment to a post
	 */
	def saveComment(comment: Comment) = db.withTransaction(implicit tx => {
	  comments += comment
	})
	
	/**
	 * extract feed
	 */
	def getFeed(userId: Long) = db.withTransaction(implicit tx => {
	 val pfeed = for( subscription <- subscriptions.filter(_.subscriberId === userId);
	                 postSubscription <- postSubscriptions.filter(_.subscriptionId === subscription.id);
	                 post <- posts.filter(_.id === postSubscription.postId);
	                 user <- users.filter(_.id === post.userId)
			 	) yield (user, post)
			 	
	 val tfeed = for( subscription <- subscriptions.filter(_.subscriberId === userId);
	                 topicSubscription <- topicSubscriptions.filter(_.subscriptionId === subscription.id);
	                 post <- posts.filter(_.topicId === topicSubscription.topicId);
	                 user <- users.filter(_.id === post.userId)
			 	) yield (user, post)
	
	val ufeed = for( subscription <- subscriptions.filter(_.subscriberId === userId);
	                 userSubscription <- userSubscriptions.filter(_.subscriptionId === subscription.id);
	                 post <- posts.filter(_.userId === userSubscription.userId);
	                 user <- users.filter(_.id === post.userId)
			 	) yield (user, post)
			 	
			 	(pfeed ++ tfeed ++ ufeed).sortBy(_._2.id.desc.nullsLast)list
	})
	
	/**
	 * extract comments 
	 */
	
	def getIteratorOfCommentInfoWithPostId(postId: Long) = db.withTransaction(implicit tx => {
	  val query = for{(comment, user) <- comments.filter(_.postId === postId) innerJoin users on(_.commenterId === _.id) 
	  } yield (comment, user)
	  query.sorted(_._1.id.desc.nullsLast).list
	})
	
	/**
	 * 
	 */
	def subscribeOrUnsubscribeUser(subscriberId: Long, userId: Long) = db.withTransaction(implicit tx => {
	  val subscribed = for((subscriber, userSubscriber) <- subscriptions.filter(_.subscriberId === subscriberId) innerJoin userSubscriptions.filter(_.userId === userId) on(_.id === _.subscriptionId))
	    yield subscriber
	  if(subscribed.exists.run) {
	    val query = for(subscription <- subscriptions.filter(_.id === subscribed.first.id)) yield subscription
	    query.delete	
	  }else {
	    val id = AutoInc.subscriptionId.insert( Subscription(subscriberId))
	    userSubscriptions += UserSubscription(id, userId)
	  }
	  
	})
	/**
	 * 
	 */
	def isUserSubscribed(subscriberId: Long, userId: Long) = db.withTransaction(implicit tx => {
	  val subscribed = for((subscriber, userSubscriber) <- subscriptions.filter(_.subscriberId === subscriberId) innerJoin userSubscriptions.filter(_.userId === userId) on(_.id === _.subscriptionId))
	    yield subscriber
	  subscribed.exists.run
	})
	/**
	 * 
	 */
	def subscribeOrUnsubscribePost(subscriberId: Long, postId: Long) = db.withTransaction(implicit tx => {
	  val subscribed = for((subscriber, postSubscriber) <- subscriptions.filter(_.subscriberId === subscriberId) innerJoin postSubscriptions.filter(_.postId === postId) on(_.id === _.subscriptionId))
	    yield subscriber
	  if(subscribed.exists.run){
	    val query = for(subscription <- subscriptions.filter(_.id === subscribed.first.id)) yield subscription
	    query.delete
	  }else {
	    val id = AutoInc.subscriptionId.insert( Subscription(subscriberId))
	    postSubscriptions += PostSubscription(id, postId)
	  }
	})
	/**
	 * 
	 */
	def isPostSubscribed(subscriberId: Long, postId: Long) = db.withTransaction(implicit tx => {
	  val subscribed = for((subscriber, postSubscriber) <- subscriptions.filter(_.subscriberId === subscriberId) innerJoin postSubscriptions.filter(_.postId === postId) on(_.id === _.subscriptionId))
	    yield subscriber
	  subscribed.exists.run
	})
	/**
	 * 
	 */
	def subscribeOrUnsubscribeTopic(subscriberId: Long, topicId: Long) = db.withTransaction(implicit tx => {
	  val subscribed = for((subscriber, topicSubscriber) <- subscriptions.filter(_.subscriberId === subscriberId) innerJoin topicSubscriptions.filter(_.topicId === topicId) on(_.id === _.subscriptionId))
	    yield subscriber
	  if(subscribed.exists.run) {
	   val query = for(subscription <- subscriptions.filter(_.id === subscribed.first.id)) yield subscription
	   query.delete
	  }else {
	    val id = AutoInc.subscriptionId.insert(Subscription(subscriberId))
	    topicSubscriptions += TopicSubscription(id, topicId)
	  }
	  
	})
	/**
	 * 
	 */
	def isTopicSubscribed(subscriberId: Long, topicId: Long) = db.withTransaction(implicit tx => {
	   val subscribed = for((subscriber, topicSubscriber) <- subscriptions.filter(_.subscriberId === subscriberId) innerJoin topicSubscriptions.filter(_.topicId === topicId) on(_.id === _.subscriptionId))
	    yield subscriber
	   subscribed.exists.run
	})
	/**
	 * 
	 */
	def getPosts(userId: Long) = db.withTransaction(implicit tx => {
	  val query = for(post <- posts.filter(_.userId === userId)) yield post
	  query.sortBy(_.id.desc.nullsLast).list
	})
}