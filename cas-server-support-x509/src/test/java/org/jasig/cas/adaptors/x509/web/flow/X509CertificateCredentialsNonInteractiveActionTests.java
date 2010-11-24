/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.cas.adaptors.x509.web.flow;

import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.jasig.cas.adaptors.x509.authentication.handler.support.X509CredentialsAuthenticationHandler;
import org.jasig.cas.adaptors.x509.authentication.principal.AbstractX509CertificateTests;
import org.jasig.cas.authentication.principal.SimpleWebApplicationServiceImpl;
import org.jasig.cas.server.util.UniqueTicketIdGenerator;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.test.MockRequestContext;


public class X509CertificateCredentialsNonInteractiveActionTests extends
    AbstractX509CertificateTests {
    
    private X509CertificateCredentialsNonInteractiveAction action;
    
    protected void setUp() throws Exception {
        this.action = new X509CertificateCredentialsNonInteractiveAction();
//        final DefaultCentralAuthenticationServiceImpl centralAuthenticationService = new DefaultCentralAuthenticationServiceImpl();
        final Map<String, UniqueTicketIdGenerator> idGenerators = new HashMap<String, UniqueTicketIdGenerator>();
        idGenerators.put(SimpleWebApplicationServiceImpl.class.getName(), new UniqueTicketIdGenerator() {
            public String getNewTicketId(String prefix) {
                return UUID.randomUUID().toString();
            }
        });

        final X509CredentialsAuthenticationHandler a = new X509CredentialsAuthenticationHandler();
        a.setTrustedIssuerDnPattern("JA-SIG");

//        final DefaultAuthenticationManagerImpl authenticationManager = new DefaultAuthenticationManagerImpl(Arrays.asList(a), Arrays.asList(new CredentialToPrincipalResolver[] {new X509CertificateCredentialsToSerialNumberPrincipalResolver()}));

/*
        centralAuthenticationService.setTicketGrantingTicketUniqueTicketIdGenerator(new DefaultUniqueTicketIdGenerator());
        centralAuthenticationService.setUniqueTicketIdGeneratorsForService(idGenerators);
        centralAuthenticationService.setServiceTicketExpirationPolicy(new NeverExpiresExpirationPolicy());
        centralAuthenticationService.setTicketGrantingTicketExpirationPolicy(new NeverExpiresExpirationPolicy());
*/
//        centralAuthenticationService.setAuthenticationManager(authenticationManager);
        
//        this.action.setCentralAuthenticationService(centralAuthenticationService);
        this.action.afterPropertiesSet();
    }
    
    public void testNoCredentialsResultsInError() throws Exception {
        final MockRequestContext context = new MockRequestContext();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), new MockHttpServletRequest(), new MockHttpServletResponse()));
        assertEquals("error", this.action.execute(context).getId());
    }

    // TODO DISABLED FOR NOW
    /**
    public void testCredentialsResultsInSuccess() throws Exception {
        final MockRequestContext context = new MockRequestContext();
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute("javax.servlet.request.X509Certificate", new X509Certificate[] {VALID_CERTIFICATE});
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
        assertEquals("success", this.action.execute(context).getId());
    }  */
}