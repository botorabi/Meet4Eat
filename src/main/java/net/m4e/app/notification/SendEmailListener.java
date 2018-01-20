/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.notification;

import net.m4e.system.core.AppConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Stateless;
import javax.enterprise.event.ObservesAsync;
import javax.inject.Inject;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.ServletContext;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.invoke.MethodHandles;
import java.util.Properties;


/**
 * Event listener for sending e-mails.
 * 
 * @author boto
 * Date of creation Oct 2, 2017
 */
@Stateless
public class SendEmailListener {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

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
        LOGGER.debug("Sending out an email");
        assembleMail(event);
    }

    /**
     * Assemble and send out the mail
     * 
     * @param event Event contains the mail content
     */
    private void assembleMail(SendEmailEvent event) {
        Properties cfg = getMailerConfig();
        if (cfg == null) {
            LOGGER.warn("Cannot send e-mail, invalid configuration");
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
            if (event.getRecipientsCC() != null) {
                for (String rec: event.getRecipientsCC()) {
                    message.addRecipient(Message.RecipientType.CC, new InternetAddress(rec));
                }                
            }
            if (event.getRecipientsBCC() != null) {
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
            LOGGER.warn("*** could not send out e-mail, reason: " + ex.getLocalizedMessage());
        }
    }

    /**
     * Get the mailer configuration. It is stored in a file specified in application configuration.
     * 
     * @return Mailer configuration
     */
    private Properties getMailerConfig() {
        if (mailServerConfig != null) {
            return mailServerConfig;
        }

        try {
            String cfgfile = AppConfiguration.getInstance().getConfigValue(AppConfiguration.TOKEN_MAILER_CONFIG_FILE);
            if (cfgfile == null) {
                LOGGER.error("*** Missing mailer configuration file entry in application configuration!");
                return null;
            }
            InputStream configcontent = context.getResourceAsStream("/WEB-INF/" + cfgfile);
            if (configcontent == null) {
                LOGGER.error("*** Missing mail config file in application!");
                return null;
            }
            Properties cfg = new Properties();
            cfg.load(configcontent);
            mailServerConfig = cfg;
        }
        catch (IOException ex) {
            LOGGER.warn("*** Could not read e-mail sender configuration, reason: " + ex.getLocalizedMessage());
        }

        return mailServerConfig;
    }
}
