/**
 * Tomcat clustering implementation
 *
 * Redis session manager is the pluggable one. It uses to store session objects from Tomcat catalina to Redis data cache.
 *
 * @author Ranjith Manickam
 * @since 1.0
 */

Pre-requisite:
--------------
1. jedis-2.8.0.jar (Available in - http://central.maven.org/maven2/redis/clients/jedis/2.8.0/jedis-2.8.0.jar)
2. commons-pool2-2.2.jar (Available in - http://central.maven.org/maven2/org/apache/commons/commons-pool2/2.2/commons-pool2-2.2.jar)
3. commons-logging-1.1.jar (Available in - http://central.maven.org/maven2/commons-logging/commons-logging/1.1.1/commons-logging-1.1.1.jar)
Note: Download all the above three jars and move it into tomcat/lib directory


Steps to be done,
-----------------
1. Add Tomcat system property "catalina.home"
	catalina.home="TOMCAT_ROOT_LOCATION"
				example: c:/ranjith/apache-tomcat-8.0.32

2. Download the TomcatRedisSessionManager-1.1.jar
	Available in - 

3. Move the downloaded jar (TomcatRedisSessionManager-1.1.jar) to tomcat/lib directory
	$catalina.home/lib/TomcatRedisSessionManager-1.1.jar
				example: c:/ranjith/apache-tomcat-8.0.32/lib/TomcatRedisSessionManager-1.1.jar

4. Extract downloaded jar (TomcatRedisSessionManager-1.1.jar) to configure redis data cache credentials in "RedisDataCache.properties" file and move the file to $catalina.home/conf directory
	$catalina.home/conf/RedisDataCache.properties
				example: c:/ranjith/apache-tomcat-8.0.32/conf/RedisDataCache.properties

5. Add the below two lines in $catalina.home/conf/context.xml
    <Valve className="com.r.tomcat.session.management.RequestSessionHandlerValve" />
    <Manager className="com.r.tomcat.session.management.RequestSessionManager" />

6. Verify the session expiration time in $catalina.home/conf/web.xml
	<session-config>
        <session-timeout>EXPIRATION TIME IN MINUTES</session-timeout>
    </session-config>

Note:
-----
  * This clustering implementation supports, both redis single node and cluster environment based on the RedisDataCache.properties configuration.