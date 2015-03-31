package com.ca.apm.swat.epaplugins.utils;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

class ProxyAuthenticator extends Authenticator {
    private String user;
    private String password;

    public ProxyAuthenticator(String user, String password) {
        this.user = user;
        this.password = password;
    }

    protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(this.user, this.password.toCharArray());
    }
}
