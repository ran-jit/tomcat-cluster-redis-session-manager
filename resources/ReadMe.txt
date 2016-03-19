/**
 * Tomcat clustering implementation
 *
 * Redis session manager is the pluggable one. It uses to store session objects from Tomcat catalina to Redis data cache.
 *
 * @author Ranjith Manickam
 * @since 1.0
 */

Supports:
   * Apache Tomcat 8

Downloads:
	Pre-requisite:
	1. jedis-2.8.0.jar
	2. commons-pool2-2.4.2.jar
	3. commons-logging-1.2.jar
	
Tomcat Redis Cluster Enabled Session Manager jar is available in below location:
https://github.com/ran-jit/TomcatClusterRedisSessionManager/wiki

Steps to be done,
1. Move the downloaded jars to tomcat/lib directory
	* $catalina.home/lib/
	
2. Add tomcat system property "catalina.base"
	* catalina.base="TOMCAT_LOCATION"

3. Extract downloaded jar (TomcatClusterEnabledRedisSessionManager-1.0.jar) to configure Redis credentials in RedisDataCache.properties file and move the file to tomcat/conf directory
	* tomcat/conf/RedisDataCache.properties

4. Add the below two lines in tomcat/conf/context.xml
	* <Valve className="com.r.tomcat.session.management.RequestSessionHandlerValve">
	* <Manager className="com.r.tomcat.session.management.RequestSessionManager">

5. Verify the session expiration time in tomcat/conf/web.xml
	* <session-config>
	* 	<session-timeout>60<session-timeout>
	* <session-config>

Note:
  * The Redis session manager supports, both single redis master and redis cluster based on the redis.properties configuration.
