package com.ca.apm.swat.epaplugins.utils;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

class ProxyAuthenticator extends Authenticator
{
  private String user;
  private String password;

  public ProxyAuthenticator(String user, String password)
  {
    this.user = user;
    this.password = password;
  }

  protected PasswordAuthentication getPasswordAuthentication() {
    return new PasswordAuthentication(this.user, this.password.toCharArray());
  }
}

/* Location:           \\vmware-host\Shared Folders\Documents\1APM-Wily\EPA\CloudMonitor\APMCMAgent\lib\APMCMPlugin.jar
 * Qualified Name:     com.wily.fieldext.epaplugins.utils.ProxyAuthenticator
 * JD-Core Version:    0.6.0
 */