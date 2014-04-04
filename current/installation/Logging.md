---
layout: default
title: CAS - Logging Configuration
---

<a name="Logging">  </a>
#Logging 
CAS provides a logging facility that logs important informational events like authentication success and failure; it can be customized to produce additional information for troubleshooting. CAS uses the Slf4J Logging framework as a facade for the [Log4J engine](logging.apache.org/log4j/‎) by default. 

The log4j configuration file is located in `cas-server-webapp/src/main/webapp/WEB-INF/classes/log4j.xml`. By default logging is set to `INFO` for all functionality related to `org.jasig.cas` code and `WARN` for messages related to Spring framework, etc. For debugging and diagnostic purposes you may want to set these levels to  `DEBUG`. 

<div class="alert alert-warning"><strong>Usage Warning!</strong><p>When in production though, you probably want to run them both as `WARN`.</p></div>

<a name="Components">  </a>
##Components
The log4j configuration is by default loaded using the following components at `cas-server-webapp/src/main/webapp/WEB-INF/spring-configuration/log4jConfiguration.xml`:

{% highlight xml %}
<bean id="log4jInitialization" class="org.springframework.beans.factory.config.MethodInvokingFactoryBean"
    p:targetClass="org.springframework.util.Log4jConfigurer" p:targetMethod="initLogging" p:arguments-ref="arguments"/>

<util:list id="arguments">
   <value>${log4j.config.location:classpath:log4j.xml}</value>
   <value>${log4j.refresh.interval:60000}</value>
</util:list>
{% endhighlight %}

It is often time helpful to externalize `log4j.xml` to a system path to preserve settings between upgrades. The location of `log4j.xml` file as well as its refresh interval by default is on the runtime classpath and at minute intervals respective. These may be overriden by the `cas.properties` file
{% highlight bash %}
# log4j.config.location=classpath:log4j.xml
#
# log4j refresh interval in millis
# log4j.refresh.interval=60000
{% endhighlight %}

<a name="Configuration">  </a>
##Configuration
The `log4j.xml` file by default at `WEB-INF/classes` provides the following `appender` elements that decide where and how messages from components should be displayed. Two are provided by default that output messages to the system console and a `cas.log` file:

<a name="Appenders">  </a>
###Appenders
{% highlight xml %}
<appender name="console" class="org.apache.log4j.ConsoleAppender">
    <layout class="org.apache.log4j.PatternLayout">
        <param name="ConversionPattern" value="%d %p [%c] - &lt;%m&gt;%n"/>
    </layout>
</appender>

<appender name="cas" class="org.apache.log4j.RollingFileAppender">
    <param name="File" value="cas.log" />
    <param name="MaxFileSize" value="512KB" />
    <param name="MaxBackupIndex" value="3" />
    <layout class="org.apache.log4j.PatternLayout">
        <param name="ConversionPattern" value="%d %p [%c] - %m%n"/>
    </layout>
</appender>
{% endhighlight %}

<a name="Loggers">  </a>
###Loggers
Additional loggers are available to specify the logging level for component categories.

{% highlight xml %}
<logger name="org.springframework">
    <level value="WARN" />
</logger>

<logger name="org.springframework.webflow">
    <level value="WARN" />
</logger>

<logger name="org.jasig" additivity="true">
    <level value="INFO" />
    <appender-ref ref="cas" />
</logger>

<logger name="com.github.inspektr.audit.support.Slf4jLoggingAuditTrailManager">
    <level value="INFO" />
    <appender-ref ref="cas" />
</logger>

<logger name="org.jasig.cas.web.flow" additivity="true">
    <level value="INFO" />
    <appender-ref ref="cas" />
</logger>
{% endhighlight %}

<a name="PerformanceStatistics">  </a>
##Performance Statistics
CAS also uses the [Perf4J framework](http://perf4j.codehaus.org/), that provides set of utilities for calculating and displaying performance statistics. Similar to above, there are specific appenders and loggers available for logging performance data.

<a name="PerfAppenders">  </a>
###Appenders
{% highlight xml %}
<appender name="CoalescingStatistics" class="org.perf4j.log4j.AsyncCoalescingStatisticsAppender">
    <param name="TimeSlice" value="60000"/>
    <appender-ref ref="fileAppender"/>
    <appender-ref ref="graphExecutionTimes"/>
    <appender-ref ref="graphExecutionTPS"/>
</appender>

<!-- This file appender is used to output aggregated performance statistics -->
<appender name="fileAppender" class="org.apache.log4j.FileAppender">
    <param name="File" value="perfStats.log"/>
    <layout class="org.apache.log4j.PatternLayout">
        <param name="ConversionPattern" value="%m%n"/>
    </layout>
</appender>

<appender name="graphExecutionTimes" class="org.perf4j.log4j.GraphingStatisticsAppender">
    <!-- Possible GraphTypes are Mean, Min, Max, StdDev, Count and TPS -->
    <param name="GraphType" value="Mean"/>
    <!-- The tags of the timed execution blocks to graph are specified here -->
    <param name="TagNamesToGraph" value="DESTROY_TICKET_GRANTING_TICKET,GRANT_SERVICE_TICKET,GRANT_PROXY_GRANTING_TICKET,VALIDATE_SERVICE_TICKET,CREATE_TICKET_GRANTING_TICKET,AUTHENTICATE" />
</appender>

<appender name="graphExecutionTPS" class="org.perf4j.log4j.GraphingStatisticsAppender">
    <param name="GraphType" value="TPS" />
    <param name="TagNamesToGraph" value="DESTROY_TICKET_GRANTING_TICKET,GRANT_SERVICE_TICKET,GRANT_PROXY_GRANTING_TICKET,VALIDATE_SERVICE_TICKET,CREATE_TICKET_GRANTING_TICKET,AUTHENTICATE" />
</appender>
{% endhighlight %}

<a name="PerfLoggers">  </a>
###Loggers
{% highlight xml %}
<logger name="org.perf4j.TimingLogger" additivity="false">
    <level value="INFO" />
    <appender-ref ref="CoalescingStatistics" />
</logger>
{% endhighlight %}


<a name="SampleOutput">  </a>
###Sample Output
{% highlight bash %}
Performance Statistics   2013-12-15 00:19:00 - 2013-12-15 00:20:00
Tag                                                  Avg(ms)         Min         Max     Std Dev       Count

Performance Statistics   2013-12-15 00:24:00 - 2013-12-15 00:25:00
Tag                                                  Avg(ms)         Min         Max     Std Dev       Count
CREATE_TICKET_GRANTING_TICKET                        42215.0       42215       42215         0.0           1
GRANT_SERVICE_TICKET                                 21023.0       21023       21023         0.0           1
{% endhighlight %}


<a name="Audits">  </a>
#Audits
CAS uses the [Inspektr framework](https://github.com/dima767/inspektr) for auditing purposes and statistics. The Inspektr project allows for non-intrusive auditing and logging of the coarse-grained execution paths e.g. Spring-managed beans method executions by using annotations and Spring-managed `@Aspect`-style aspects.

##Components
<a name="AuditTrailManagementAspect">  </a>
###`AuditTrailManagementAspect`
Aspect modularizing management of an audit trail data concern.

<a name="Slf4jLoggingAuditTrailManager">  </a>
###`Slf4jLoggingAuditTrailManager`
`AuditTrailManager` that dumps auditable information to a configured logger based on SLF4J, at the `INFO` level.

<a name="JdbcAuditTrailManager">  </a>
###`JdbcAuditTrailManager`
`AuditTrailManager` to persist the audit trail to the `AUDIT_TRAIL` table in a rational database.

<a name="TicketAsFirstParameterResourceResolver">  </a>
###`TicketAsFirstParameterResourceResolver`
`ResourceResolver` that can determine the ticket id from the first parameter of the method call.

<a name="TicketOrCredentialPrincipalResolver">  </a>
###`TicketOrCredentialPrincipalResolver`
`PrincipalResolver` that can retrieve the username from either the Ticket or from the `Credential`.

##Configuration
Audit functionality is specifically controlled by the `WEB-INF/spring-configuration/auditTrailContext.xml`. Configuration of the audit trail manager is defined inside `deployerConfigContext.xml`.

<a name="DatabaseAudits">  </a>
###Database Audits
By default, audit messages appear in log files via the `Slf4jLoggingAuditTrailManager`. If you intend to use a database for auditing functionality, adjust the audit manager to match the sample configuration below:
{% highlight xml %}
<bean id="auditManager" class="com.github.inspektr.audit.support.JdbcAuditTrailManager">
  <constructor-arg index="0" ref="inspektrTransactionTemplate" />
  <property name="dataSource" ref="dataSource" />
  <property name="cleanupCriteria" ref="auditCleanupCriteria" />
</bean>
<bean id="auditCleanupCriteria"
  class="com.github.inspektr.audit.support.MaxAgeWhereClauseMatchCriteria">
  <constructor-arg index="0" value="180" />
</bean>
{% endhighlight %}

Refer to [Inspektr documentation](https://github.com/dima767/inspektr/wiki/Inspektr-Auditing) on how to create the database schema.

<a name="SampleLogOutput">  </a>
##Sample Log Output
{% highlight bash %}
WHO: org.jasig.cas.support.oauth.authentication.principal.OAuthCredentials@6cd7c975
WHAT: supplied credentials: org.jasig.cas.support.oauth.authentication.principal.OAuthCredentials@6cd7c975
ACTION: AUTHENTICATION_SUCCESS
APPLICATION: CAS
WHEN: Mon Aug 26 12:35:59 IST 2013
CLIENT IP ADDRESS: 172.16.5.181
SERVER IP ADDRESS: 192.168.200.22

WHO: org.jasig.cas.support.oauth.authentication.principal.OAuthCredentials@6cd7c975
WHAT: TGT-9-qj2jZKQUmu1gQvXNf7tXQOJPOtROvOuvYAxybhZiVrdZ6pCUwW-cas01.example.org
ACTION: TICKET_GRANTING_TICKET_CREATED
APPLICATION: CAS
WHEN: Mon Aug 26 12:35:59 IST 2013
CLIENT IP ADDRESS: 172.16.5.181
SERVER IP ADDRESS: 192.168.200.22
{% endhighlight %}