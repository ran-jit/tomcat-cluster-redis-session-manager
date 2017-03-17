# Tomcat Clustering Redis Session Manager

Redis session manager is pluggable one. It uses to store sessions into Redis for easy distribution of HTTP Requests across a cluster of Tomcat servers. Sessions are implemented as as non-sticky i.e, each request is forwarded to any server in round-robin manner.

The HTTP Requests session setAttribute(name, value) method stores the session into Redis (must be Serializable) immediately and the session getAttribute(name) method request directly from Redis. Also, the inactive sessions has been removed based on the session time-out configuration.

It supports, both single redis master and redis cluster based on the RedisDataCache.properties configuration.

Going forward, we no need to enable sticky session (JSESSIONID) in Load balancer.

## Supports:
   * [Apache Tomcat 7](https://github.com/ran-jit/TomcatClusterRedisSessionManager/releases/tag/1.0)
   * Apache Tomcat 8

## Downloads:

##### Pre-requisite:
1. jedis.jar
2. commons-pool2.jar
3. commons-logging.jar

**Tomcat Redis Cluster Enabled Session Manager is available in below location**
  
    https://github.com/ran-jit/TomcatClusterRedisSessionManager/wiki
    


#### Steps to be done,
1. Move the downloaded jars to tomcat/lib directory
	* **$catalina.home/lib/**
	
2. Add tomcat system property "catalina.base"
	* **catalina.base="TOMCAT_LOCATION"**

3. Configure downloaded Redis credentials in RedisDataCache.properties file and move the file to tomcat/conf directory
	* **tomcat/conf/RedisDataCache.properties**

4. Add the below two lines in tomcat/conf/context.xml
	* **&#60;Valve className="com.r.tomcat.session.management.RequestSessionHandlerValve" &#47;&#62;**
	* **&#60;Manager className="com.r.tomcat.session.management.RequestSessionManager" &#47;&#62;**

5. Verify the session expiration time in tomcat/conf/web.xml
	* **&#60;session-config&#62;**
	* 	**&#60;session-timeout&#62;60&#60;&#47;session-timeout&#62;**
	* **&#60;&#47;session-config&#62;**

### Note:
  * The Redis session manager supports, both single redis master and redis cluster based on the redis.properties configuration.
