package com.asu.log.models

 import java.util.Date

import com.asu.log.helpers.ID

/**
  * Created by hjun  chenhj on 2017/10/26.
  */
case class User(id:String,token:String,ip:String,createDate:Date)
object User{
  def apply(token: String,ip:String): User = new User(ID.gen, token,ip,new Date())
}