# Tomcat Clustering Redis Session Manager

Redis session manager is pluggable one. It uses to store sessions into Redis for easy distribution of HTTP Requests across a cluster of Tomcat servers. Sessions are implemented as as non-sticky i.e, each request is forwarded to any server in round-robin manner.

The HTTP Requests session setAttribute(name, value) method stores the session into Redis (session attribute values must be Serializable) immediately and the session getAttribute(name) method request directly from Redis. Also, the inactive sessions has been removed based on the session time-out configuration.

Supports redis default, sentinel and cluster mode, based on the configuration.

Going forward, we no need to enable sticky session (JSESSIONID) in Load balancer.

## Supports:
   - Apache Tomcat 7
   - Apache Tomcat 8
   - Apache Tomcat 9

## Downloads:
   - [latest version (3.0)](https://github.com/ran-jit/tomcat-cluster-redis-session-manager/releases/tag/3.0)
   - [older versions](https://github.com/ran-jit/tomcat-cluster-redis-session-manager/wiki)

#### Pre-requisite:
1. jedis.jar
2. commons-pool2.jar
3. commons-logging.jar

more details.. https://github.com/ran-jit/tomcat-cluster-redis-session-manager/wiki
    

#### Steps to be done,
1. Move the downloaded jars to tomcat/lib directory
	- **tomcat/lib/**
	
2. Add tomcat system property "catalina.base"
	- **catalina.base="TOMCAT_LOCATION"**
	     * example: export catalina.base=/opt/tomcat

3. Extract downloaded package (tomcat-cluster-redis-session-manager.zip) to configure Redis credentials in redis-data-cache.properties file and move the file to tomcat/conf directory
	- **tomcat/conf/redis-data-cache.properties**

4. Add the below two lines in tomcat/conf/context.xml
	- **&#60;Valve className="tomcat.request.session.redis.SessionHandlerValve" &#47;&#62;**
	- **&#60;Manager className="tomcat.request.session.redis.SessionManager" &#47;&#62;**

5. Verify the session expiration time in tomcat/conf/web.xml
	- **&#60;session-config&#62;**
	- 	&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; **&#60;session-timeout&#62;60&#60;&#47;session-timeout&#62;**
	- **&#60;&#47;session-config&#62;**

### Note:
  - **All your session attribute values must implement java.io.Serializable.**
  - Supports redis default, sentinel and cluster mode, based on the redis-data-cache.properties configuration.

### Configuration Properties:
<html>
<body>
    <table border="1px" style="width: 80%;margin-left: 10%;margin-right: 10%;line-height: 1.5;">
        <tr><th style="width: 20%;">Property</th><th style="width: 40%;">Description</th><th style="width: 30%;">Value</th></tr>
        <tr><td>redis.hosts</td><td>Redis server running instance IP address and port number<br>ex: 127.0.0.1:6379, 127.0.0.2:6379, 127.0.0.2:6380, ..</td><td>default: 127.0.0.1:6379</td></tr>
        <tr><td>redis.password</td><td>Redis protected password</td><td></td></tr>
        <tr><td>redis.database</td><td>Redis database selection. (Numeric value)</td><td>default: 0</td></tr>
        <tr><td>redis.timeout</td><td>Redis connection timeout</td><td>default: 2000</td></tr>
        <tr><td>redis.cluster.enabled</td><td>To enable redis cluster mode</td><td>default: false<br>supported values: true/false</td></tr>
        <tr><td>redis.sentinel.enabled</td><td>To enable redis sentinel mode</td><td>default: false<br>supported values: true/false</td></tr>
        <tr><td>redis.sentinel.master</td><td>Redis sentinel master name</td><td>default: mymaster</td></tr>
        <tr><td>lb.sticky-session.enabled</td><td>To enable redis and standard session mode<br><br>If enabled,<ol><li>Must be enabled sticky session in your load balancer configuration. Else this manager may not return the updated session values</li><li>Session values are stored in local jvm and redis</li><li>If redis is down/not responding, requests uses jvm stored session values to process user requests. Redis comes back the values will be synced</li></ol></td><td>default: false</td></tr>
    </table>
</body>
</html>
