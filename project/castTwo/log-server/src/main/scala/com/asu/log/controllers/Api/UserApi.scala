package com.asu.log.controllers.Api

import com.asu.log.API
import com.asu.log.controllers.ResponseController
import com.asu.log.helpers.{BearerTokenGeneratorHelp, RedisClientUtil}
import com.asu.log.models.User
import com.asu.log.service.UserService
import com.google.inject.Inject
import com.twitter.finagle.http.Request
import com.twitter.util.Future
import com.twitter.conversions.time._
import scala.util.{Failure, Success, Try}


/**
  * Created by hjun  chenhj on 2017/10/26.
  */
class UserApi @Inject()(userService: UserService) extends ResponseController{

  post(API.getBaseUrl ++"/user/authentication"){request:Request=>
    (for{
      token<-Try(new BearerTokenGeneratorHelp().generateSHAToken("CASTLOG"))
      addedRow <-Try(userService.insert(User(token,request.remoteHost)))
      redisToken <-Try(RedisClientUtil.write(API.getUserTokenKey(token),addedRow,1.day.inSeconds))
    }yield Map("token"->token)) match {
      case Failure(error) => fail("error","",Future.value(None))
      case Success(users)=> success("ok","",Future.value(Some(users)))
    }
  }

}
