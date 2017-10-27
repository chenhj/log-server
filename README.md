#log-server 日志服务器
   
##打包插件[sbt-native-packager](https://github.com/sbt/sbt-native-packager)   
##[finagle-redis](https://github.com/twitter/finagle/tree/develop/finagle-redis)
##[finatra](https://twitter.github.io/finatra/)
##数据库 [quill](http://getquill.io/)

   
###sbt-native-packager
Add the following to your project/plugins.sbt file:

    // for autoplugins
    addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.3.1")
    
In your build.sbt enable the plugin you want. For example the JavaAppPackaging.
    
    enablePlugins(JavaAppPackaging)

Or if you need a server with autostart support

    enablePlugins(JavaServerAppPackaging)

if gen conf need copy resource file


    mappings in Universal += {
    // we are using the reference.conf as default application.conf
    // the user can override settings here
      val conf = (resourceDirectory in Compile).value / "reference.conf"
      conf -> "conf/application.conf"
    }
    
    这个只是处理一个文件,如果需要把src目录中的所有文件都拷贝过
    修改为:
    mappings in Universal <++= sourceDirectory map (src => contentOf(src / "main" / "resources").map(m=>(m._1,"conf/"+m._2) ))

    启动时要服务读取conf目前中配置文件 ,在build.sbt中
    bashScriptExtraDefines += """addJava "-Dconfig.file=${app_home}/../conf/application.conf""""

        

Build

    sbt <config-scope>:packageBin

Examples

    # universal zip
    sbt universal:packageBin
    
    # debian package
    sbt debian:packageBin
    
    # rpm package
    sbt rpm:packageBin
    
    # docker image
    sbt docker:publishLocal
    
#### 生成zip包
     
   在项目根目录下
     
     sbt universal:packageBin
   
     zip包已经打好,存target目录中,解压后可以直接运行

    
### Version
    可以通过resuorce/appliction.conf文件设置版本
    
    默认的版本为1
  
### Public API

####[curl](https://curl.haxx.se/docs/httpscripting.html)调用测试
    

#### /logserver/api/v3/user/authentication
    
     获取授权的token 的url
     授权请求说明:需要权限的接口,在路径中加/auth/
     
```
   $ curl -i  -X POST http://localhost:8008/logserver/api/v1/user/authentication
   
   {"flag":"1","message":"ok","code":"","data":{"token":"43f853a5fe428a6018a1a855f2690d6ac2b407e48d5e5d2ec57fb7f71c422db3"}}localhost:log-server hjun$ 

```

#### /logserver/api/v3/cast/auth/upload

上传日志文件 
   参数:
        busyType:业务类型,1:腕投系统日志,2:手机app日志,3:腕投app日志
        logfile:文件流对象
```
 
     curl  -F logfile=@/Users/hjun/file.txt -i -H token:9fd49b272b4f322e74354896723253e7bd47f17faf7e7d15e7d8159df599a036 -X POST http://localhost:8008/logserver/api/v1/cast/auth/upload?busyType=1
```
    
###Redis 

在application.conf中配置redis访问地址和密码

使用方式 
``` 
    //存入缓存
    val key="user:123456"
    val user=Map("id"->"123456","name"->"zhangshang","ip"->"192.168.0.1")   
    RedisClientUtil.write(key,user,1.day.inSeconds)
    
    //从缓存中读取一个Map结果
    val user=RedisClientUtil.read[Map[String,String]](key)
    
```
    
###Quill

数据库操作插件

通过TwitterModule来注册Quill组件
``` 
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

```

在APP中注册模块App.scala
``` 
     override protected def modules: Seq[Module] = Seq(QuillContextModule)
```

创建一个case 对象
``` 
 import java.util.Date
import com.asu.log.helpers.ID

/**
  * Created by hjun  chenhj on 2017/10/26.
  */
case class User(id:String,token:String,ip:String,createDate:Date)
object User{
  def apply(token: String,ip:String): User = new User(ID.gen, token,ip,new Date())
}
```

通过dao可以来操作User表

``` 
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
   
```

****注意:
  插入数据,如果使用自增
  ```$xslt
    run(quote(query[User]).insert(_.id->lift(user.id),_.token->lift(user.token)).returning(_.id))
    run(quote(query[User]).insert(user).returning(_.id))
    
    上面两种方式,生成的sql 
    INSERT INTO user (token,ip,create_date) VALUES (?, ?, ?)
    不会加入id主键在sql语句中,在保存时也不会带上主键id
    
    如果在插入时需要使用自己的主键
    run(quote(liftQuery(List(user))).foreach(e => query[User].insert(e)))
    生成的语句才会生成带id的主键
    INSERT INTO user (id,token,ip,create_date) VALUES (?, ?, ?, ?)

        
```
  