package com.asu.log.models

import javax.inject.Singleton

import com.asu.log.modules.QuillContext
import com.google.inject.Inject
import com.twitter.util.Future

/**
  * Created by hjun  chenhj on 2017/10/26.
  */
@Singleton
class UserDao @Inject()(ctx:QuillContext) {

  import ctx._

  def find(userId: String): Future[User] = {
    run(quote(query[User]).filter(_.id == lift(userId))).map(_.head)
  }

  def insert(user: User) = {
  //run(quote(query[User]).insert(_.id->lift(user.id),_.token->lift(user.token)).returning(_.id))
    run(quote(liftQuery(List(user))).foreach(e => query[User].insert(e)))
    user
}

  def deleteAll:Future[Unit]=
    run(quote(query[User].delete)).unit

  def getUserByToken(token:String):Future[User]=
    run(quote(query[User].filter(_.token==lift(token)))).map(_.head)


}
