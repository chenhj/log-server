#log-server 日志服务器
   
##打包插件[sbt-native-packager](https://github.com/sbt/sbt-native-packager)   
##finagle-redis 
##finatra
##数据库orm [quill](http://getquill.io/)

   
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
    
    
    