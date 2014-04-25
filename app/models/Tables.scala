package models

import scala.slick.driver.MySQLDriver.simple._
import scala.slick.lifted.{TableQuery, Tag}
import scala.slick.model.ForeignKeyAction
import java.sql.Timestamp

case class User(anum: Long, name: String, password: String, desc: String, id: Option[Long] = None)

class Users(tag: Tag) extends Table[User](tag, "USERS") {
  def anum = column[Long]("ANUM", O.NotNull)
  def name = column[String]("NAME", O.NotNull)
  def password = column[String]("PASSWORD", O.NotNull)
  def desc = column[String]("DESC", O.NotNull)
  def id = column[Long]("ID", O.NotNull, O.PrimaryKey, O.AutoInc)
  def * = (anum, name, password, desc, id.?) <> (User.tupled, User.unapply)
}


case class Topic(name: String, desc: String, id: Option[Long] = None)

class Topics(tag: Tag) extends Table[Topic](tag, "TOPICS") {
  def name = column[String]("NAME")
  def desc = column[String]("DESC")
  def id = column[Long]("ID", O.NotNull, O.PrimaryKey, O.AutoInc)
  def * = (name, desc, id.?) <> (Topic.tupled, Topic.unapply)
}


case class Post(userId: Long, topicId: Long, timestamp: Timestamp, content: String, id: Option[Long] = None)

class Posts(tag: Tag) extends Table[Post](tag, "POSTS") {
  def userId = column[Long]("USER_ID", O.NotNull)
  def topicId = column[Long]("TOPIC_ID", O.NotNull)
  def timestamp = column[Timestamp]("TIMESTAMP", O.NotNull)
  def content = column[String]("CONTENT", O.NotNull)
  def id = column[Long]("ID", O.NotNull, O.PrimaryKey, O.AutoInc)
  def * = (userId, topicId, timestamp, content, id.?) <> (Post.tupled, Post.unapply)
  def userIdFk = foreignKey("POSTS_USER_ID_FK", userId, TableQuery[Users])(_.id, ForeignKeyAction.Cascade, ForeignKeyAction.Cascade)
  def topicIdFk = foreignKey("POSTS_TOPIC_ID_FK", topicId, TableQuery[Topics])(_.id, ForeignKeyAction.Cascade, ForeignKeyAction.Cascade)
}

case class Comment(commenterId: Long, postId: Long, content: String, id: Option[Long] = None)

class Comments(tag: Tag) extends Table[Comment](tag, "COMMENTS") {
  def commenterId = column[Long]("COMMENTER_ID", O.NotNull)
  def postId = column[Long]("POST_ID", O.NotNull)
  def content = column[String]("CONTENT", O.NotNull)
  def id = column[Long]("ID", O.NotNull, O.PrimaryKey, O.AutoInc)
  def * = (commenterId, postId, content, id.?) <> (Comment.tupled, Comment.unapply)
  def commenterIdFk = foreignKey("COMMENTS_COMMENTER_ID_FK", commenterId, TableQuery[Users])(_.id, ForeignKeyAction.Cascade, ForeignKeyAction.Cascade)
  def postIdFk = foreignKey("COMMENTS_POST_ID_FK", postId, TableQuery[Posts])(_.id, ForeignKeyAction.Cascade, ForeignKeyAction.Cascade)
}


case class EndorsePost(postId: Long, endorserId: Long, id: Option[Long] = None)

class EndorsePosts(tag: Tag) extends Table[EndorsePost](tag, "ENDORSE_POSTS") {
  def postId = column[Long]("POST_ID", O.NotNull)
  def endorserId = column[Long]("ENDORSE_ID", O.NotNull)
  def id = column[Long]("ID", O.PrimaryKey, O.NotNull, O.AutoInc)
  def * = (postId, endorserId, id.?) <> (EndorsePost.tupled, EndorsePost.unapply)
  def postIdFk = foreignKey("ENDORSE_POST_POST_ID_FK", postId, TableQuery[Posts])(_.id, ForeignKeyAction.Cascade, ForeignKeyAction.Cascade)
  def endorserIdFk = foreignKey("ENDORSE_POST_ENDORSER_ID", endorserId, TableQuery[Users])(_.id, ForeignKeyAction.Cascade, ForeignKeyAction.Cascade)
}


case class EndorseComment(commentId: Long, endorserId: Long, id: Option[Long] = None)

class EndorseComments(tag: Tag) extends Table[EndorseComment](tag, "ENDORSE_COMMENTS") {
  def commentId = column[Long]("COMMENT_ID", O.NotNull)
  def endorserId = column[Long]("ENDORSER_ID", O.NotNull)
  def id = column[Long]("ID", O.NotNull, O.PrimaryKey, O.AutoInc)
  def * = (commentId, endorserId, id.?) <> (EndorseComment.tupled, EndorseComment.unapply)
  def commentIdFk = foreignKey("ENDORSE_COMMENT_COMMENT_ID_FK", commentId, TableQuery[Comments])(_.id, ForeignKeyAction.Cascade, ForeignKeyAction.Cascade)
  def endorserIdFk = foreignKey("ENDORSE_COMMENT_ENDORSER_ID_FK",  endorserId, TableQuery[Users])(_.id, ForeignKeyAction.Cascade, ForeignKeyAction.Cascade)
}


case class Subscription(subscriberId: Long, id: Option[Long] = None)

class Subscriptions(tag: Tag) extends Table[Subscription](tag, "SUBSCRIPTIONS") {
  def subscriberId = column[Long]("SUBSCRIBER_ID", O.NotNull)
  def id = column[Long]("ID", O.NotNull, O.PrimaryKey, O.AutoInc)
  def * = (subscriberId, id.?) <> (Subscription.tupled, Subscription.unapply)
  def subscriptionIdFk = foreignKey("SUBSCRIPTIONS_SUBSCRIBER_ID_FK", subscriberId, TableQuery[Users])(_.id, ForeignKeyAction.Cascade, ForeignKeyAction.Cascade)
}


case class TopicSubscription(subscriptionId: Long, topicId: Long, id: Option[Long] = None)

class TopicSubscriptions(tag: Tag) extends Table[TopicSubscription](tag, "TOPIC_SUBSCRIPTIONS") {
  def subscriptionId = column[Long]("SUBSCRIPTION_ID", O.NotNull)
  def topicId = column[Long]("TOPIC_ID", O.NotNull)
  def id = column[Long]("ID", O.NotNull, O.PrimaryKey, O.AutoInc)
  def * = (subscriptionId, topicId, id.?) <> (TopicSubscription.tupled, TopicSubscription.unapply)
  def subscriptionIdFk = foreignKey("TOPIC_SUBSCRIPTIONS_SUBSCRIPTION_ID_FK", subscriptionId, TableQuery[Subscriptions])(_.id, ForeignKeyAction.Cascade, ForeignKeyAction.Cascade)
  def topicIdFk = foreignKey("TOPIC_SUBSCIPTIONS_TOPIC_ID_FK", topicId, TableQuery[Topics])(_.id, ForeignKeyAction.Cascade, ForeignKeyAction.Cascade)
}


case class PostSubscription(subscriptionId: Long, postId: Long, id: Option[Long] = None)

class PostSubscriptions(tag: Tag) extends Table[PostSubscription](tag, "POST_SUBSCRIPTIONS") {
 def subscriptionId = column[Long]("SUBSCRIPTION_ID", O.NotNull)
 def postId = column[Long]("POST_ID", O.NotNull)
 def id = column[Long]("ID", O.PrimaryKey, O.NotNull, O.AutoInc)
 def * = (subscriptionId, postId, id.?) <> (PostSubscription.tupled, PostSubscription.unapply)
 def subscriptionIdFk = foreignKey("POST_SUBSCRIPTIONS_SUBSCRIPTION_ID_FK", subscriptionId, TableQuery[Subscriptions])(_.id, ForeignKeyAction.Cascade, ForeignKeyAction.Cascade)
 def postIdFk = foreignKey("POST_SUBSCRIPTIONS_POST_ID_Fk", postId, TableQuery[Posts])(_.id, ForeignKeyAction.Cascade, ForeignKeyAction.Cascade)
}


case class UserSubscription(subscriptionId: Long, userId: Long, id: Option[Long] = None)

class UserSubscriptions(tag: Tag) extends Table[UserSubscription](tag, "USER_SUBSCRIPTIONS") {
 def subscriptionId = column[Long]("SUBSCRIPTION_ID", O.NotNull)
 def userId = column[Long]("USER_ID", O.NotNull)
 def id = column[Long]("ID", O.PrimaryKey, O.NotNull, O.AutoInc)
 def * = (subscriptionId, userId, id.?) <> (UserSubscription.tupled, UserSubscription.unapply)
 def subscriptionIdFk = foreignKey("USER_SUBSCRIPTIONS_SUBSCRIPTION_ID_FK", subscriptionId, TableQuery[Subscriptions])(_.id, ForeignKeyAction.Cascade, ForeignKeyAction.Cascade)
 def userIdFk = foreignKey("USER_SUBSCRIPTIONS_POST_ID_Fk", userId, TableQuery[Users])(_.id, ForeignKeyAction.Cascade, ForeignKeyAction.Cascade)
}