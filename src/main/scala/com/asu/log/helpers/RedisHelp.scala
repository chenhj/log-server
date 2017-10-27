package com.asu.log.helpers

import com.asu.log.API
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper}
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import com.twitter.conversions.time._
import com.twitter.finagle.Redis
import com.twitter.finagle.redis.Client
import com.twitter.finagle.redis.util.{BufToString, StringToBuf}
import com.twitter.util._
import com.typesafe.scalalogging.StrictLogging



object RedisClientUtil extends SampleCommands

/**
 * finatra-two-app com.asu.cast.core.redis
 * Created by chenhj on 2017/7/12 0012.
 *
 */
trait SampleCommands {

  //lazy private val redisHosts =List("127.0.0.1:6379") //ApplicationUtil.redisHost
  //lazy private val password=""

  lazy private val redisHosts =List(API.getRedisHost) //ApplicationUtil.redisHost
  lazy private val password=API.getRedisPassword


  //val client = Client(RedisCluster.hostAddresses())

  private val redisClients: List[Client] = redisHosts  map {
        Redis.client.withRequestTimeout(6000.second).newRichClient(_) match {
          case client: Client => {
            client.auth(StringToBuf(password))
            client
          }
        }
   }


  def read[T: Manifest](key: String): Future[Option[T]] = {
    val k = StringToBuf(key)
    val fannedOutGetFutures = redisClients map (_.get(k))
    firstSuccessOf(fannedOutGetFutures) map {
      case Some(v) => {
        val result = new RedisMarshaller[T].str2Obj(BufToString(v))
        result.toOption
      }
      case _ => None
    }
  }

  def exists(key: String): Future[Option[Boolean]] = {
    val k = StringToBuf(key)
    val fannedOutGetFutures = redisClients map (_.exists(k))
    firstSuccessOf(fannedOutGetFutures) map { Some(_) }
  }

  def exist(key: String): Boolean = {
    println("exist===============start ...")
    val rest = exists(key).toJavaFuture.get().get
    println("exist===============end ...")
    rest
  }

  def write[T: Manifest](key: String, t: T) = {
    val value = new RedisMarshaller[T].obj2Str(t)
    val k = StringToBuf(key)
    val v = StringToBuf(value)
    val fannedOutSetFutures = redisClients map (_.set(k, v))
    firstSuccessOf(fannedOutSetFutures)

  }

  /**
    *
    * @param key
    * @param t
    * @param seconds  缓存时间 秒 4.hour.inSeconds  引入时间包   import com.twitter.conversions.time._
    * @tparam T
    * @return
    *         例如:
    *         import com.twitter.conversions.time._
    *         write("k1","username",4.hour.inSeconds)
    */
  def write[T: Manifest](key: String, t: T, seconds: Long) = {
    val value = new RedisMarshaller[T].obj2Str(t)
    val k = StringToBuf(key)
    val v = StringToBuf(value)
    val fannedOutSetFutures = redisClients map (_.setEx(k, seconds, v))
    firstSuccessOf(fannedOutSetFutures)
  }

  def bAdd(key: String, field: String, value: String) = {
    val k =   StringToBuf(key)
    val f = StringToBuf(field)
    val v = StringToBuf(value)
    val result = redisClients map (_.bAdd(k, f, v))
    firstSuccessOf(result)
  }

  def bGet(key: String, field: String) = {
    val k = StringToBuf(key)
    val f = StringToBuf(field)
    val result = redisClients map (_.bGet(k, f))
    firstSuccessOf(result)
  }

  def lRange(key: String, start: Long, end: Long) = {
    val k = StringToBuf(key)
    val result = redisClients map (_.lRange(k, start, end))
    firstSuccessOf(result)
  }

  def firstSuccessOf[A](fs: List[Future[A]]): Future[A] = {
    def loop(selection: Future[(Try[A], Seq[Future[A]])]): Future[A] = selection flatMap {
      case (Return(result), _) => Future.value(result)
      case (Throw(error), Nil) => Future.exception(error)
      case (_, fs)             => loop(Future.select(fs))
    }
    loop(Future.select(fs))
  }

  def pushDataToQueue(queueKey: String, value: String) = {
    val k = StringToBuf(queueKey)
    //val v = StringToChannelBuffer((math.random * 10).toInt.toString)]
    val v = StringToBuf(value)
    // val k=StringToBuf(queueKey)
    //val v=StringToBuf(value)
    val fannedOutGetFutures = redisClients map (_.rPush(k, List(v)))
    firstSuccessOf(fannedOutGetFutures) map {
      //  case Some(v) => Some(CBToString(v))
      case v1: java.lang.Long => Some(v1)
      case _                  => None
    }
  }

  def queueListener(queueKey: String) = {
    println("Queue '" + queueKey + "' opened (Dot signals queue polling, the numbers are data received at the queue)")
    val k = StringToBuf(queueKey)
    val fannedOutGetFutures = redisClients map (_.lPop(k))
    firstSuccessOf(fannedOutGetFutures) map {
    //  case Some(v) => Some(CBToString(v))
      case Some(v) => Some(BufToString(v))
      case _       => None
    }
  }
}

class RedisMarshaller[T: Manifest] extends StrictLogging {

  import scalaz.Scalaz._
  import scalaz.ValidationNel


  protected val mapper = new ObjectMapper with ScalaObjectMapper
  mapper.registerModule(DefaultScalaModule)
  mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)

  mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
  mapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false)
  mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
  mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)
  mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true)

  def obj2Str(t: T): String = {
    mapper.writeValueAsString(t)
  }

  def str2Obj(json: String): ValidationNel[String, T] = {
    mapper.readValue(json, manifest.runtimeClass.asInstanceOf[Class[T]]).successNel[String]
  }

}

