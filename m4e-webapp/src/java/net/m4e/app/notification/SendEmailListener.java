/*
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.notification;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Properties;
import javax.ejb.Stateless;
import javax.enterprise.event.ObservesAsync;
import javax.inject.Inject;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.ServletContext;
import net.m4e.system.core.AppConfiguration;
import net.m4e.system.core.Log;


/**
 * Event listener for sending e-mails.
 * 
 * @author boto
 * Date of creation Oct 2, 2017
 */
@Stateless
public class SendEmailListener {

    /**
     * Used for logging
     */
    private final static String TAG = "SendEmailListener";

    @Inject
    ServletContext context;

    /**
     * JavaMail configuration which is read from a config file
     */
    private static Properties mailServerConfig;
  

    public SendEmailListener() {}
    
    /**
     * Event observer
     * 
     * @param event Mail event
     */
    public void sendEmail(@ObservesAsync SendEmailEvent event) {
        Log.debug(TAG, "Sending out an email");
        assembleMail(event);
    }

    /**
     * Assemble and send out the mail
     * 
     * @param event Event contains the mail content
     */
    private void assembleMail(SendEmailEvent event) {
        Properties cfg = getMailerConfig();
        if (null == cfg) {
            Log.warning(TAG, "Cannot send e-mail, invalid configuration");
            return;
        }

        Authenticator auth = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                String user = cfg.getProperty("mail.smtp.user", "");
                String pw = cfg.getProperty("mail.smtp.password", "");
                return new PasswordAuthentication(user, pw);
            }
        };

        Session session = Session.getInstance(cfg, auth);
        MimeMessage message = new MimeMessage(session);
        try {
            message.setFrom(new InternetAddress(cfg.getProperty("mail.from", ""), cfg.getProperty("mail.from.name", "")));
            for (String rec: event.getRecipients()) {
                message.addRecipient(Message.RecipientType.TO, new InternetAddress(rec));
            }
            if (null != event.getRecipientsCC()) {
                for (String rec: event.getRecipientsCC()) {
                    message.addRecipient(Message.RecipientType.CC, new InternetAddress(rec));
                }                
            }
            if (null != event.getRecipientsBCC()) {
                for (String rec: event.getRecipientsBCC()) {
                    message.addRecipient(Message.RecipientType.BCC, new InternetAddress(rec));
                }                
            }
            message.setSubject(event.getSubject());
            if (event.getHtmlBody()) {
                message.setContent(event.getBody(), "text/html; charset=utf-8");
            }
            else {
              message.setText(event.getBody());
            }
            Transport.send(message);
        }
        catch (MessagingException | UnsupportedEncodingException ex){
            Log.warning(TAG, "*** could not send out e-mail, reason: " + ex.getLocalizedMessage());
        }
    }

    /**
     * Get the mailer configuration. It is stored in a file specified in application configuration.
     * 
     * @return Mailer configuration
     */
    private Properties getMailerConfig() {
        if (null != mailServerConfig) {
            return mailServerConfig;
        }

        try {
            String cfgfile = AppConfiguration.getInstance().getConfigValue(AppConfiguration.TOKEN_MAILER_CONFIG_FILE);
            if (null == cfgfile) {
                Log.error(TAG, "*** Missing mailer configuration file entry in application configuration!");
                return null;
            }
            InputStream resourceContent = context.getResourceAsStream("/WEB-INF/" + cfgfile);
            if (null == cfgfile) {
                Log.error(TAG, "*** Missing mailer configuration file: " + "/WEB-INF/" + cfgfile);
                return null;
            }
            Properties cfg = new Properties();
            cfg.load(resourceContent);
            mailServerConfig = cfg;
        }
        catch (IOException ex) {
            Log.warning(TAG, "*** Could not read e-mail sender configuration, reason: " + ex.getLocalizedMessage());
        }

        return mailServerConfig;
    }
}
