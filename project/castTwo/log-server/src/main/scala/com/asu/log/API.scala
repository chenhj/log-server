package com.asu.log

import com.typesafe.config.ConfigFactory


/**
  * Created by hjun  chenhj on 2017/10/26.
  */
object API {
    private[this] val config=ConfigFactory.load()
    private[this] val version=config.getString("api.version")
    private[this] val auth=config.getBoolean("api.auth")
    private[this] val basePath=config.getString("api.log.filepath")
    private[this] val redisHosts=config.getString("redis.hosts")
    private[this] val redisPassword=config.getString("redis.password")
    private[this] val baseUrl="/logserver/api/v" ++ version

   def getBaseUrl=baseUrl
   def getBasePath=basePath
   def getRedisHost=redisHosts
   def getRedisPassword=redisPassword

   def getUserTokenKey(s:String)="TOKEN:"+s
   def getAuth=auth


}
