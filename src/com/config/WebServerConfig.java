package com.config;

import java.util.Properties;

public class WebServerConfig {
	private static WebServerConfig instance;
	private Properties properties = new Properties();
	
	private WebServerConfig() {
		try {
			properties.load(getClass().getResourceAsStream(
					"/setting.properties"));
		} catch (Exception e) {
			System.err.println("加载服务器配置信息失败");
			e.printStackTrace();
		}
	}

	public static WebServerConfig getInstance() {
		if(instance == null){
			instance = new WebServerConfig();
		}
		return instance;
	}

	public Properties getProperties() {
		return properties;
	}
	
	public String getProperty(String key){
		return properties.getProperty(key);
	}
}
