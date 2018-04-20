# Tomcat Clustering Redis Session Manager

Redis session manager is pluggable one. It uses to store sessions into Redis for easy distribution of HTTP Requests across a cluster of Tomcat servers. Sessions are implemented as as non-sticky i.e, each request is forwarded to any server in round-robin manner.

The HTTP Requests session setAttribute(name, value) method stores the session into Redis (must be Serializable) immediately and the session getAttribute(name) method request directly from Redis. Also, the inactive sessions has been removed based on the session time-out configuration.

It supports, both single redis master and redis cluster based on the RedisDataCache.properties configuration.

Going forward, we no need to enable sticky session (JSESSIONID) in Load balancer.

## Supports:
   * Apache Tomcat 7
   * Apache Tomcat 8
   * Apache Tomcat 9

## Downloads:
   * [latest version (2.0.3)](https://github.com/ran-jit/tomcat-cluster-redis-session-manager/releases/tag/2.0.3)
   * [older versions](https://github.com/ran-jit/tomcat-cluster-redis-session-manager/wiki)

#### Pre-requisite:
1. jedis.jar
2. commons-pool2.jar
3. commons-logging.jar

more details.. https://github.com/ran-jit/tomcat-cluster-redis-session-manager/wiki
    

#### Steps to be done,
1. Move the downloaded jars to tomcat/lib directory
	* **tomcat/lib/**
	
2. Add tomcat system property "catalina.base"
	* **catalina.base="TOMCAT_LOCATION"**

3. Extract downloaded package (tomcat-cluster-redis-session-manager.zip) to configure Redis credentials in redis-data-cache.properties file and move the file to tomcat/conf directory
	* **tomcat/conf/redis-data-cache.properties**

4. Add the below two lines in tomcat/conf/context.xml
	* **&#60;Valve className="tomcat.request.session.redis.SessionHandlerValve" &#47;&#62;**
	* **&#60;Manager className="tomcat.request.session.redis.SessionManager" &#47;&#62;**

5. Verify the session expiration time in tomcat/conf/web.xml
	* **&#60;session-config&#62;**
	* 	**&#60;session-timeout&#62;60&#60;&#47;session-timeout&#62;**
	* **&#60;&#47;session-config&#62;**

### Note:
  * This supports, both redis stand-alone and multiple node cluster based on the redis-data-cache.properties configuration.
