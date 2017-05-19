package com.util;

import java.io.File;
import java.util.List;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import com.config.WebServerConfig;

public class MailUtil {
	private static boolean openDebug;
	private static MailUtil instance;
	
	private MailUtil(boolean debug) {
		openDebug = debug;
	}
	
	public static MailUtil getInstance(boolean debug){
		if(instance == null){
			instance = new MailUtil(debug);
		}
		return instance;
	}
	
	public void sendMessageMail(String content, String subject, List<String> receiveAccounts) throws AddressException, MessagingException {
		sendAttachmentMail(content, subject, receiveAccounts, null);
	}
	
	public void sendAttachmentMail(String content, String subject, List<String> receiveAccounts, List<String> attachementsPath) throws AddressException, MessagingException{
		if(!checkMail(content, subject, receiveAccounts)){
			return;
		}
		Session session = Session.getInstance(initMailProperties());
		session.setDebug(openDebug);
		Message message = new MimeMessage(session);
		message.setFrom(new InternetAddress(WebServerConfig.getInstance().getProperty("sendMailAccount")));
		message.setSubject(subject);//设置主题
		
		MimeBodyPart contentPart = createMail(content);//邮件内容
		MimeMultipart part = new MimeMultipart("mixed");
		part.addBodyPart(contentPart);//组装文本信息
		if(attachementsPath != null && attachementsPath.size() > 0){//组装附件信息
			for(String path : attachementsPath){
				File file = new File(path);
				if(file.exists() && file.isFile()){
					MimeBodyPart attachmentPart = createAttachment(path);//附件
					part.addBodyPart(attachmentPart);
				} else {
					System.out.println(path + "文件不存在或者为目录");
				}
			}
		}
		message.setContent(part);
		message.saveChanges();
		
		Transport transport = session.getTransport();
		transport.connect(WebServerConfig.getInstance().getProperty("sendMailHost"), 
				WebServerConfig.getInstance().getProperty("sendMailAccount"),
				WebServerConfig.getInstance().getProperty("sendMailAccountPassword"));
		if(receiveAccounts != null && receiveAccounts.size() > 0){
			InternetAddress[] receives = new InternetAddress[receiveAccounts.size()];
			for(int i = 0; i < receiveAccounts.size(); ++ i){
				receives[i] = new InternetAddress(receiveAccounts.get(i));
			}
			transport.sendMessage(message, receives);
		}
		transport.close();
		System.out.println("邮件发送成功！");
	}
	
	private Properties initMailProperties(){
		Properties properties = new Properties();
		properties.setProperty("mail.transport.protocol", "smtp");
		properties.setProperty("mail.smtp.auth", "true");//设置验证机制
		return properties;
	}
	
	private boolean checkMail(String content, String subject, List<String> receiveAccounts){
		if(content == null || "".equals(content)){
			System.out.println("邮件内容不能为空");
			return false;
		}
		if(subject == null || "".equals(subject)){
			System.out.println("主题不能为空");
			return false;
		}
		if(receiveAccounts != null && receiveAccounts.size() > 0){
			for(String receive : receiveAccounts){
				if(receive == null || "".equals(receive)){
					System.out.println("收件人不能为空, receivePaths info : " + receiveAccounts);
					return false;
				}
			}
		}
		return true;
	}
	/**
	 * 创建邮件文本信息模块
	 * */
	private MimeBodyPart createMail(String content) throws MessagingException{
		MimeBodyPart result = new MimeBodyPart();
		result.setContent(content, "text/html;charset=utf-8");
		return result;
	}
	/**
	 * 创建邮件附件模块
	 * */
	private MimeBodyPart createAttachment(String attachmentPath) throws MessagingException {
		File file = new File(attachmentPath);
		MimeBodyPart result = new MimeBodyPart();
		FileDataSource source = new FileDataSource(file);
		result.setDataHandler(new DataHandler(source));
		result.setFileName(file.getName());
		return result;
	}
}
