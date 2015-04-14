---
layout: default
title: CAS - Delegate authentication
---

#Overview
The CAS server implements the CAS protocol on server side and may even behave like an OAuth provider, an OpenID provider or a SAML IdP. Whatever the protocol, the CAS server is first of all a server.

But the CAS server can also act as a client using the [pac4j library](https://github.com/leleuj/pac4j) and delegate the authentication to:

* another CAS server
* an OAuth provider: Facebook, Twitter, Google, LinkedIn, Yahoo and several other providers
* an OpenID provider: myopenid.com
* a SAML identity provider
* an OpenID Connect identity provider.

Support is enabled by including the following dependency in the Maven WAR overlay:

{% highlight xml %}
<dependency>
	<groupId>org.jasig.cas</groupId>
	<artifactId>cas-server-support-pac4j</artifactId>
	<version>${cas.version}</version>
</dependency>
{% endhighlight %}

##How to use CAS/OAuth/OpenID/SAML client support in CAS applications?


###Information returned by a delegated authentication

Once you have configured (see information below) your CAS server to act as an OAuth, CAS, OpenID (Connect) or SAML client, users will be able to authenticate at a OAuth/CAS/OpenID/SAML provider (like Facebook) instead of authenticating directly inside the CAS server.

In the CAS server, after this kind of delegated authentication, users have specific authentication data.

The `Authentication` object has:

* the attribute `AuthenticationManager.AUTHENTICATION_METHOD_ATTRIBUTE` (authenticationMethod) set to *`org.jasig.cas.support.pac4j.authentication.handler.support.ClientAuthenticationHandler`*
* the attribute *clientName* set to the type of the provider used during authentication process.

The `Principal` object of the `Authentication` object has:

* an identifier which is the profile type + # + the identifier of the user for this provider (example : FacebookProfile#0000000001)
* attributes populated by the data retrieved from the provider (first name, last name, birthdate...)

###How to send profile attributes to CAS client applications?

In CAS applications, through service ticket validation, user information are pushed to the CAS client and therefore to the application itself.

The identifier of the user is always pushed to the CAS client. For user attributes, it involves both the configuration at the server and the way of validating service tickets.

On CAS server side, to push attributes to the CAS client, it should be configured in the `deployerConfigContext.xml` file for the expected service:

{% highlight xml %}
<bean id="serviceRegistryDao" class="org.jasig.cas.services.InMemoryServiceRegistryDaoImpl">
 <property name="registeredServices">
   <list>
     <bean class="org.jasig.cas.services.RegisteredServiceImpl">
       <property name="id" value="0" />
       <property name="name" value="HTTP" />
       <property name="description" value="Only Allows HTTP Urls" />
       <property name="serviceId" value="http://**" />
       <property name="evaluationOrder" value="10000001" />
       <property name="allowedAttributes">
        <list>
          <!-- facebook -->
          <value>name</value>
          <value>first_name</value>
          <value>middle_name</value>
...
{% endhighlight %}

On CAS client side, to receive attributes, you need to use the SAML validation or the CAS 3.0 validation, that is `/p3/serviceValidate` url.

###How to recreate user profiles in CAS applications?

In the CAS server, the complete user profile is known but when attributes are sent back to the CAS client applications, there is some kind of "CAS serialization" which makes data uneasy to be restored at their original state.

Though, you can now completely rebuild the original user profile from data returned in the CAS `Assertion`.

After validating the service ticket, an `Assertion` is available in the CAS client from which you can get the identifier and the attributes of the authenticated user using the pac4j library:

{% highlight java %}
final AttributePrincipal principal = assertion.getPrincipal();
final String id = principal.getName();
final Map<String, Object> attributes = principal.getAttributes();
{% endhighlight %}

As the identifier stores the kind of profile in its own definition (`*clientName#idAtProvider*`), you can use the `org.pac4j.core.profile.ProfileHelper.buildProfile(id, attributes)` method to recreate the original profile:

{% highlight java %}
final FacebookProfile rebuiltProfileOnCasClientSide = (FacebookProfile) ProfileHelper.buildProfile(id, attributes);
{% endhighlight %}

and then use it in your application!

##Configuration

###Add the required pac4j-* libraries

To add CAS client support, add the following dependency:

    <dependency>
      <groupId>org.pac4j</groupId>
      <artifactId>pac4j-cas</artifactId>
      <version>${pac4j.version}</version>
    </dependency>

To add OAuth client support, add the following dependency:

    <dependency>
      <groupId>org.pac4j</groupId>
      <artifactId>pac4j-oauth</artifactId>
      <version>${pac4j.version}</version>
    </dependency>

To add OpenID client support, add the following dependency:

    <dependency>
      <groupId>org.pac4j</groupId>
      <artifactId>pac4j-openid</artifactId>
      <version>${pac4j.version}</version>
    </dependency>

To add OpenID Connect client support, add the following dependency:

    <dependency>
      <groupId>org.pac4j</groupId>
      <artifactId>pac4j-oidc</artifactId>
      <version>${pac4j.version}</version>
    </dependency>

To add SAML support, add the following dependency:

    <dependency>
      <groupId>org.pac4j</groupId>
      <artifactId>pac4j-saml</artifactId>
      <version>${pac4j.version}</version>
    </dependency>

###Add the needed clients

A provider is a server which can authenticate user (like Google, Yahoo...) instead of a CAS server. If you want to delegate the CAS authentication to Twitter for example, you have to add an OAuth client for the provider: Twitter. Clients classes are defined in the pac4j library.

All the needed clients to authenticate against providers must be declared in the `applicationContext.xml` file:

{% highlight xml %}
<bean id="facebook1" class="org.pac4j.oauth.client.FacebookClient">
  <property name="key" value="fbkey" />
  <property name="secret" value="fbsecret" />
  <property name="scope" value="email,user_likes,user_about_me,user_birthday,user_education_history,user_hometown" />
  <property name="fields" value="id,name,first_name,middle_name,last_name,gender,locale,languages,link,username,third_party_id,timezone,updated_time" />
</bean>
 
<bean id="twitter1" class="org.pac4j.oauth.client.TwitterClient">
  <property name="key" value="twkey" />
  <property name="secret" value="twsecret" />
</bean>
 
<bean id="caswrapper1" class="org.pac4j.oauth.client.CasOAuthWrapperClient">
  <property name="key" value="this_is_the_key" />
  <property name="secret" value="this_is_the_secret" />
  <property name="casOAuthUrl" value="http://mycasserver2/oauth2.0" />
</bean>
  
<bean id="cas1" class="org.pac4j.cas.client.CasClient">
  <property name="casLoginUrl" value="http://mycasserver2/login" />
</bean>
 
<bean id="myopenid1" class="org.pac4j.openid.client.MyOpenIdClient" />
{% endhighlight %}

For each OAuth provider, the CAS server is considered as an OAuth client and therefore should be declared also at the OAuth provider. After declaration, a key and a secret is given by the OAuth provider which has to be defined in the beans (*the_key_for_xxx* and *the_secret_for_xxx* values for the *key* and *secret* properties).

For the CAS OAuth wrapping, the *casOAuthUrl* property must be set to the OAuth wrapping url of the other CAS server which is using OAuth wrapping (something like *http://mycasserver2/oauth2.0*).

To simplify configuration, all clients and the CAS server login url are gathered in the same `Clients` configuration bean (in the `applicationContext.xml` file):

{% highlight xml %}
<bean id="clients" class="org.pac4j.core.client.Clients">
  <property name="callbackUrl" value="http://localhost:8080/cas/login" />
  <property name="clients">
    <list>
      <ref bean="facebook1" />
      <ref bean="twitter1" />
      <ref bean="caswrapper1" />
      <ref bean="cas1" />
      <ref bean="myopenid1" />
    </list>
  </property>
</bean>
{% endhighlight %}


###Add the client action in webflow

In the `login-webflow.xml` file, the `ClientAction` must be added at the beginning of the webflow. Its role is to intercept callback calls from providers (like Facebook, Twitter...) after a delegated authentication:

{% highlight xml %}
<action-state id="clientAction">
  <evaluate expression="clientAction" />
  <transition on="success" to="sendTicketGrantingTicket" />
  <transition on="error" to="ticketGrantingTicketCheck" />
  <transition on="stop" to="stopWebflow" />
</action-state>
<view-state id="stopWebflow" />
{% endhighlight %}

This `ClientAction` has to be defined in the `cas-servlet.xml` file with all the needed clients:

{% highlight xml %}
<bean id="clientAction" class="org.jasig.cas.support.pac4j.web.flow.ClientAction">
  <constructor-arg index="0" ref="centralAuthenticationService"/>
  <constructor-arg index="1" ref="clients"/>
</bean>
{% endhighlight %}

This `ClientAction` uses the *centralAuthenticationService* bean to finish the CAS authentication and references all the clients.

###Add the handler and the metadata populator (optional) for authentication

To be able to finish authenticating users in the CAS server after a remote authentication by an external provider, you have to add the `ClientAuthenticationHandler` class and might add the `ClientAuthenticationMetaDataPopulator` class (to track the provider) in the `deployerConfigContext.xml` file:

{% highlight xml %}
<bean id="authenticationManager" class="org.jasig.cas.authentication.PolicyBasedAuthenticationManager">
    <constructor-arg>
        <map>
        </map>
    </constructor-arg>
    <property name="authenticationMetaDataPopulators">
        <util:list>
           <bean class="org.jasig.cas.support.pac4j.authentication.ClientAuthenticationMetaDataPopulator" />
        </util:list>
    </property>
    <property name="authenticationPolicy">
        <bean class="org.jasig.cas.authentication.AnyAuthenticationPolicy" />
    </property>
</bean>
<bean id="primaryAuthenticationHandler" class="org.jasig.cas.support.pac4j.authentication.handler.support.ClientAuthenticationHandler">
    <constructor-arg index="0" ref="clients"/>
</bean>
{% endhighlight %}

By default, the identifier returned by a delegated authentication is composed of the profile name and the technical identifier of the provider, like `FacebookProfile#1234`, to ensure the identifier uniqueness. Though, you can remove this behaviour and only return the technical identifier by using:

{% highlight xml %}
<bean id="primaryAuthenticationHandler" class="org.jasig.cas.support.pac4j.authentication.handler.support.ClientAuthenticationHandler">
    <constructor-arg index="0" ref="clients"/>
    <property name="typedIdUsed" value="false" />
</bean>
{% endhighlight %}


###Add links on the login page to authenticate on remote providers

To start authentication on a remote provider, these links must be added on the login page `casLoginView.jsp` (*ClientNameUrl* attributes are automatically created by the `ClientAction`):

{% highlight xml %}
<a href="${FacebookClientUrl}">Authenticate with Facebook</a> <br />
<br />
<a href="${TwitterClientUrl}">Authenticate with Twitter</a><br />
<br />
<a href="${CasOAuthWrapperClientUrl}">Authenticate with another CAS server using OAuth v2.0 protocol</a><br />
<br />
<a href="${CasClientUrl}">Authenticate with another CAS server using CAS protocol</a><br />
<br />
<form action="${MyOpenIdClientUrl}" method="POST">
  <input type="text" name="openIdUser" value="http://xxx.myopenid.com/" />
  <input type="submit" value="Authenticate with myopenid.com" />
</form>
{% endhighlight %}


##Demo

Take a look at this demo: [cas-pac4j-oauth-demo](https://github.com/leleuj/cas-pac4j-oauth-demo) to see this authentication delegation mechanism in action.
