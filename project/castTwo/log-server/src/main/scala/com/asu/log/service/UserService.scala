package com.asu.log.service

import com.asu.log.models.{User, UserDao}
import com.google.inject.Inject

/**
  * Created by hjun  chenhj on 2017/10/26.
  */
class UserService @Inject()(userDao: UserDao) {
    def getUserByToken(token:String)=userDao.getUserByToken(token)
    def insert(user:User)=userDao.insert(user)
    def find(userId:String)=userDao.find(userId)
}
