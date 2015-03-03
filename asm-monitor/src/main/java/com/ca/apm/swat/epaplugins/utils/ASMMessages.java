package com.ca.apm.swat.epaplugins.utils;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

public class ASMMessages {

	public static final String initializationError	 			= "initializationError";
	public static final String runError	 						= "runError";
	public static final String readingProperties 				= "readingProperties";
	public static final String folderThreadTimeout 				= "folderThreadTimeout";
	public static final String folderThreadError 				= "folderThreadError";
	public static final String connectionError 					= "connectionError";
	public static final String connectionRetry					= "connectionRetry";
	public static final String connectionRetryError				= "connectionRetryError";
	public static final String loginError						= "loginError";
	public static final String loginInfo						= "loginInfo";
	public static final String noError							= "noError";
	public static final String noInfo							= "noInfo";
	public static final String connectionRetry5					= "connectionRetry";
	public static final String connectionRetry6					= "connectionRetry";
	public static final String connectionRetry7					= "connectionRetry";
	public static final String connectionRetry8					= "connectionRetry";
	public static final String parentThread						= "parentThread";
	public static final String agentInitialization 				= "agentInitialization";

	
	private static ResourceBundle messages = null;

	private static Locale locale = null;

	public static void setLocale(Locale loc) {
		locale = loc;
	}

	public static ResourceBundle getMessages() {
		if (null == messages) {
			if (null == locale) {
				messages = ResourceBundle.getBundle("messages");
			} else {
				messages = ResourceBundle.getBundle("messages", locale);
			}
		}

		return messages;
	}

	public static String getMessage(String key) {
		return getMessages().getString(key);
	}

	public static String getMessage(String key, Object[] params) {
		MessageFormat formatter = new MessageFormat(getMessages().getString(key), locale);
		return formatter.format(params);
	}

	public static String getMessage(String key, String param) {
		return getMessage(key, new Object[]{param});
	}
}
