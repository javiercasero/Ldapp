package org.tfgdomain.ldapp;

import org.junit.Before;
import org.junit.Test;

import static android.support.test.InstrumentationRegistry.getContext;
import static org.junit.Assert.*;

public class MyLdapTest {
    private MyLdap myLdap;
    private String domain;
    @Before
    public void setUp() throws Exception {
        myLdap = new MyLdap(getContext(), 0);
        domain = "tfgdomain.org";
    }
    @Test
    public void testGetDN() throws Exception {
        String result = myLdap.getDN(domain);
        assertEquals("DC=tfgdomain,DC=org", result);
    }
    @Test void testCheckIfAdmin() throws Exception {

    }
}