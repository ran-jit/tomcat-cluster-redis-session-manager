# Tomcat Clustering Redis Session Manager

The Redis session manager is pluggable one. It stores session into Redis for easy distribution of HTTP Requests across a cluster of Tomcat servers.

Here the Sessions are implemented as non-sticky (means, each request can able to go to any server in the cluster, unlike the Apache provided Tomcat clustering setup.)

Request Sessions will be stored into Redis immediately (Session attributes must be Serializable), for the use of other servers. When tomcat receives a request from the client, Sessions are loaded directly from Redis.

Supports Redis default, sentinel and cluster mode, based on the configuration.

Going forward, we no need to enable sticky session (JSESSIONID) in Load Balancer.

## Supports:
   - Apache Tomcat 7
   - Apache Tomcat 8
   - Apache Tomcat 9
   - Apache Tomcat 10

## Downloads: [![Total Downloads](https://img.shields.io/github/downloads/ran-jit/tomcat-cluster-redis-session-manager/total.svg)](https://github.com/ran-jit/tomcat-cluster-redis-session-manager/wiki)
   - [latest version (4.0)](https://github.com/ran-jit/tomcat-cluster-redis-session-manager/releases/tag/4.0)
   - [older versions](https://github.com/ran-jit/tomcat-cluster-redis-session-manager/wiki)

<p align="center">
  <a href="https://www.buymeacoffee.com/ranmanic" target="_blank"><img src="https://cdn.buymeacoffee.com/buttons/v2/default-red.png" alt="Buy Me A Coffee" style="height: 60px !important;width: 217px !important;" ></a>
</p>

## Maven configuration
```
<repository>
  <id>repsy</id>
  <name>tomcat-cluster-redis-session-manager-repo</name>
  <url>https://repo.repsy.io/mvn/ranmanic/tomcat-session-manager</url>
</repository>

<dependency>
  <groupId>tomcat-session-manager</groupId>
  <artifactId>redis</artifactId>
  <version>4.0</version>
</dependency>
```

#### Pre-requisite:
1. jedis.jar
2. commons-pool2.jar
3. commons-logging.jar

more details.. https://github.com/ran-jit/tomcat-cluster-redis-session-manager/wiki
    

#### Steps to be done,
1. Copy the downloaded jars to your tomcat/lib directory.
```
tomcat/lib/
```
	
2. Add tomcat system property "catalina.base".
```
catalina.base="TOMCAT_LOCATION"
example: env "catalina.base=/opt/tomcat" bash
```

3. Copy the redis-data-cache.properties file to your tomcat/conf directory and update your Redis server details.
```
tomcat/conf/redis-data-cache.properties
```

4. Add the below two lines in your tomcat/conf/context.xml file.
```
<Valve className="tomcat.request.session.redis.SessionHandlerValve" />
<Manager className="tomcat.request.session.redis.SessionManager" />
```

5. Verify the session expiration time in tomcat/conf/web.xml file.
```
<session-config>
  <session-timeout>60</session-timeout>
</session-config>
```

### Note:
  - **All your session attribute values must implement java.io.Serializable.**

### Configuration Properties:
<html>
<body>
    <table border="1px" style="width: 80%;margin-left: 10%;margin-right: 10%;line-height: 1.5;">
        <tr><th style="width: 30%;">Property</th><th style="width: 50%;">Description</th></tr>
        <tr><td>redis.hosts</td><td>Redis server running instance IP address and port number<br/>- ex: 127.0.0.1:6379, 127.0.0.2:6379, 127.0.0.2:6380, ..<br/>- default: 127.0.0.1:6379</td></tr>
        <tr><td>redis.password</td><td>Redis protected password</td></tr>
        <tr><td>redis.database</td><td>Redis database selection. (Numeric value)<br/>- default: 0</td></tr>
        <tr><td>redis.timeout</td><td>Redis connection timeout<br/>- default: 2000 ms</td></tr>
        <tr><td>redis.cluster.enabled</td><td>To enable redis cluster mode<br/>- default: false<br>- supported values: true/false</td></tr>
        <tr><td>redis.sentinel.enabled</td><td>To enable redis sentinel mode<br/>- default: false<br>- supported values: true/false</td></tr>
        <tr><td>redis.sentinel.master</td><td>Redis sentinel master name<br/>- default: mymaster</td></tr>
        <tr><td>lb.sticky-session.enabled</td><td>To enable redis and standard session mode<br><br>If enabled,<ol><li>Must be enabled sticky session in your load balancer configuration. Else this manager may not return the updated session values</li><li>Session values are stored in local jvm and redis</li><li>If redis is down/not responding, requests uses jvm stored session values to process user requests. Redis comes back the values will be synced</li></ol>- default: false</td></tr>
	    <tr><td>session.persistent.policies</td><td>session persistent policies.<br/><br/>- policies - DEFAULT, SAVE_ON_CHANGE, ALWAYS_SAVE_AFTER_REQUEST <br/><ol><li>SAVE_ON_CHANGE: every time session.setAttribute() or session.removeAttribute() is called the session will be saved.</li><li>ALWAYS_SAVE_AFTER_REQUEST: force saving after every request, regardless of whether or not the manager has detected changes to the session.</li></ol>- default: DEFAULT</td></tr>
	    <tr><td>redis.sso.timeout</td><td>single-sign-on session timeout.<br/>- default: 0 ms (-no expiry)</td></tr>
    </table>
</body>
</html>
