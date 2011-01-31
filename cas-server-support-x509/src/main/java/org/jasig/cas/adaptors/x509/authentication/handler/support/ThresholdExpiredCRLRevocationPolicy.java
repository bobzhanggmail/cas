/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.adaptors.x509.authentication.handler.support;

import java.security.GeneralSecurityException;
import java.security.cert.X509CRL;
import java.util.Calendar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.cas.adaptors.x509.util.CertUtils;


/**
 * Implements a policy to handle expired CRL data whereby expired data is permitted
 * up to a threshold period of time but not afterward.
 *
 * @author Marvin S. Addison
 * @version $Revision$
 * @since 3.4.7
 *
 */
public class ThresholdExpiredCRLRevocationPolicy implements RevocationPolicy<X509CRL> {
    /** Logger instance */
    private final Log logger = LogFactory.getLog(getClass());

    /** Default threshold is 48 hours. */
    private static final int DEFAULT_THRESHOLD = 172800;

    /** Expired threshold period in seconds. */
    private int threshold = DEFAULT_THRESHOLD;


    /**
     * The CRL next update time is compared against the current time with the threshold
     * applied and rejected if and only if the next update time is in the past.
     *
     * @param crl CRL instance to evaluate.
     *
     * @throws ExpiredCRLException On expired CRL data.
     *
     * @see org.jasig.cas.adaptors.x509.authentication.handler.support.RevocationPolicy#apply(java.lang.Object)
     */
    public void apply(final X509CRL crl) throws GeneralSecurityException {
        final Calendar cutoff = Calendar.getInstance();
        if (CertUtils.isExpired(crl, cutoff.getTime())) {
            cutoff.add(Calendar.SECOND, -this.threshold);
            if (CertUtils.isExpired(crl, cutoff.getTime())) {
                throw new ExpiredCRLException(crl.toString(), cutoff.getTime(), this.threshold);
            }
            this.logger.info(
                String.format("CRL expired on %s but is within threshold period, %s seconds.",
                    crl.getNextUpdate(), this.threshold));
        }
    }
    
    /**
     * Sets the threshold period of time after which expired CRL data is rejected.
     * 
     * @param threshold Number of seconds; MUST be non-negative integer.
     */
    public void setThreshold(final int threshold) {
        if (threshold < 0) {
            throw new IllegalArgumentException("Threshold must be non-negative.");
        }
        this.threshold = threshold;
    }

    
}