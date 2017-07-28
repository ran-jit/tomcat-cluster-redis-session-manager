/**
 * Tomcat clustering with Redis data-cache implementation.
 *
 * Tomcat clustering with Redis is the plugable one. It uses to store session objects to Redis data cache.
 *
 * @author Ranjith Manickam
 * @since 2.0
 */

Supports:
   * Apache Tomcat 7
   * Apache Tomcat 8

Pre-requisite:
	1. jedis.jar
	2. commons-pool2.jar
	3. commons-logging.jar

more details.. https://github.com/ran-jit/TomcatClusterRedisSessionManager/wiki

Steps to be done,
	1. Move the downloaded jars to tomcat/lib directory
		* $catalina.home/lib/

	2. Add tomcat system property "catalina.base"
		* catalina.base="TOMCAT_LOCATION"

	3. Extract downloaded package (tomcat-cluster-redis-session-manager.zip) to configure Redis credentials in redis-data-cache.properties file and move the file to tomcat/conf directory
		* tomcat/conf/redis-data-cache.properties

	4. Add the below two lines in tomcat/conf/context.xml
		<Valve className="tomcat.request.session.redis.SessionHandlerValve" />
		<Manager className="tomcat.request.session.redis.SessionManager" />

	5. Verify the session expiration time (minutes) in tomcat/conf/web.xml
		<session-config>
			<session-timeout>60<session-timeout>
		<session-config>

Note:
  * This supports, both redis stand-alone and multiple node cluster based on the redis-data-cache.properties configuration.
