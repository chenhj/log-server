package com.asu.log.modules

import com.google.inject.{Provides, Singleton}
import com.twitter.inject.TwitterModule
import io.getquill.{FinagleMysqlContext, SnakeCase}

class QuillContext extends FinagleMysqlContext(SnakeCase,"ctx")

/**
  * Created by hjun  chenhj on 2017/10/26.
  */
object QuillContextModule extends TwitterModule{

   @Singleton
   @Provides
   def providesQuillDbContext:QuillContext=new QuillContext
}
