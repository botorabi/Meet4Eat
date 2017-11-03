/*
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

package net.m4e.app.mailbox;

import net.m4e.app.user.*;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import net.m4e.common.Entities;
import net.m4e.system.core.Log;

/**
 * A collection of mailbox related utilities
 * 
 * @author boto
 * Date of creation Oct 31, 2017
 */
public class Mails {

    /**
     * Used for logging
     */
    private final static String TAG = "Mails";

    private final EntityManager entityManager;

    /**
     * Create an instance of mailbox utilities.
     * 
     * @param entityManager    Entity manager
     */
    public Mails(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /**
     * Given a mail entity filled with all its fields, create it in database.
     * 
     * @param mail          Mail entity
     * @throws Exception    Throws exception if any problem occurred.
     */
    public void createMail(MailEntity mail) throws Exception {
        Entities entities = new Entities(entityManager);
        entities.createEntity(mail);
        createMailUser(entities, mail.getId(), mail.getSenderId());
        // sometimes ppl send mails to themselves, catch that
        if (!Objects.equals(mail.getSenderId(), mail.getReceiverId())) {
            createMailUser(entities, mail.getId(), mail.getReceiverId());
        }
    }

    /**
     * Create the join table MailUserEntity for given user and mail IDs.
     * 
     * @param entities  Entities utils
     * @param mailId    Mail ID
     * @param userId    User ID
     */
    private void createMailUser(Entities entities, Long mailId, Long userId) throws Exception {
        MailUserEntity mailuser = new MailUserEntity();
        mailuser.setMailId(mailId);
        mailuser.setUserId(userId);
        mailuser.setTrashDate(0L);
        mailuser.setUnread(true);
        entities.createEntity(mailuser);        
    }

    /**
     * Get the count of user's unread mails.
     * 
     * @param user  The user
     * @return      The count of unread mails
     */
    public long getCountUnreadMails(UserEntity user) {
        Query query = entityManager.createNamedQuery("MailUserEntity.countUnreadMails");
        query.setParameter("userId", user.getId());
        long count = (long)query.getSingleResult();
        return count;
    }

    /**
     * Get user's mails in given range. Pass 0/0 as range in order to get all user mails (limited to 100 mails).
     * 
     * @param user  User entity
     * @param from  Range begin
     * @param to    Range end
     * @return      User mails in given range
     */
    public List<Mail> getMails(UserEntity user, int from, int to) {
        int MAX_RANGE = 100;
        TypedQuery<Object[]> query = entityManager.createNamedQuery("MailUserEntity.findMails", Object[].class);
        query.setParameter("userId", user.getId());
        List<Object[]> results;
        // limit the max range
        if (to > 0 && from > 0) {
            if (to - from > MAX_RANGE) {
                to = from + MAX_RANGE;
            }
            results = query.setMaxResults(to - from + 1).setFirstResult(from).getResultList();
        }
        else {
            results = query.setMaxResults(MAX_RANGE).getResultList();
        }
        //! NOTE unfortunately (at least with JPA 2.0) it is not possible to easily store the results using a proper class type,
        //        so we have to do it manually here
        List<Mail> mails = new ArrayList();
        for (int i = 0; i < results.size(); i++) {
            Object[] res = results.get(i);
            mails.add(new Mail((MailEntity)res[0], (boolean)res[1], (Long)res[2]));
        }
        return mails;
    }

    /**
     * Find a MailUser entity. Return null if not found.
     * 
     * @param userId        ID of user referring to the mail
     * @param mailId        The actual Mail ID
     * @return              MailUser if found or null if no entry for given user and mail ID exists.
     */
    public MailUserEntity findMailUser(Long userId, Long mailId) {
        TypedQuery<MailUserEntity> query = entityManager.createNamedQuery("MailUserEntity.findMailUser", MailUserEntity.class);
        query.setParameter("userId", userId);
        query.setParameter("mailId", mailId);
        MailUserEntity mailuser = query.getSingleResult();
        return mailuser;
    }

    /**
     * Trash or untrash a user mail. Trashed mails get purged after some period of time. Notice that
     * only the user specific reference to an actual mail is trashed, not the mail itself.
     * 
     * @param userId        ID of user referring to the mail
     * @param mailId        The actual Mail ID
     * @param trash         Pass true to trash, false to untrash the mail
     * @throws Exception    Throws an exception if something goes wrong.
     */
    public void trashUserMail(Long userId, Long mailId, boolean trash) throws Exception {
        MailUserEntity mailuser = findMailUser(userId, mailId);
        if (mailuser == null) {
            throw new Exception("Mail does not exist.");
        }
        if (trash && mailuser.isTrashed()) {
            throw new Exception("Mail is already trashed.");
        }
        if (!trash && !mailuser.isTrashed()) {
            throw new Exception("Mail was not trashed.");
        }
        mailuser.setTrashDate(trash ? (new Date()).getTime() : 0L);
        Entities entities = new Entities(entityManager);
        entities.updateEntity(mailuser);
    }

    /**
     * Mark a user mail as 'read' or 'unread'.
     * 
     * @param userId        ID of user referring to the mail
     * @param mailId        The actual Mail ID
     * @param unread        Pass true for marking the mail as 'unread'
     * @throws Exception    Throws an exception if something goes wrong.
     */
    public void markAsUnreadUserMail(Long userId, Long mailId, boolean unread) throws Exception {
        MailUserEntity mailuser = findMailUser(userId, mailId);
        if (mailuser == null) {
            throw new Exception("Mail does not exist.");
        }
        mailuser.setUnread(unread);
        Entities entities = new Entities(entityManager);
        entities.updateEntity(mailuser);
    }

    /**
     * Perform a mail operation.
     * 
     * @param userId        ID of user referring to the mail
     * @param mailId        The actual Mail ID
     * @param operation     Mail operation: trash, untrash, read, unread
     * @throws Exception    Throws an exception if something goes wrong.
     */
    public void performMailOperation(Long userId, Long mailId, String operation) throws Exception {
        if (operation == null) {
            throw new Exception("Unsupported operation");
        }
        else switch (operation) {
            case "trash":
                trashUserMail(userId, mailId, true);
                break;
            case "untrash":
                trashUserMail(userId, mailId, false);
                break;
            case "read":
                markAsUnreadUserMail(userId, mailId, false);
                break;
            case "unread":
                markAsUnreadUserMail(userId, mailId, true);
                break;
            default:
                throw new Exception("Unsupported operation");
        }
    }

    /**
     * Give a mail entity export the necessary fields into a JSON object.
     * 
     * @param mail    The mail to export
     * @return        A JSON object containing builder the proper entity fields
     */
    public JsonObjectBuilder exportMailJSON(Mail mail) {
        MailEntity mailentity = mail.getMailEntity();
        JsonObjectBuilder json = Json.createObjectBuilder();
        json.add("id", (mailentity.getId() != null) ? mailentity.getId().toString() : "")
            .add("subject", mailentity.getSubject())
            .add("content", mailentity.getContent())
            .add("senderId", (mailentity.getSenderId() != null) ? "" + mailentity.getSenderId() : "<System>")
            .add("receiverId", (mailentity.getReceiverId() != null) ? "" + mailentity.getReceiverId() : "<System>")
            .add("sendDate", (mailentity.getSendDate()!= null) ? mailentity.getSendDate() : 0)
            .add("unread", mail.isUnread())
            .add("trashDate", (mail.getTrashDate() != null) ? mail.getTrashDate() : 0);

        //! TODO put the attachments into jason document

        return json;
    }

    /**
     * Export mails of given user. Use from/to for pagination.
     * 
     * @param user  User entity
     * @param from  Range begin
     * @param to    Range end
     * @return      User mails in a JSON array
     */
    public JsonArrayBuilder exportUserMails(UserEntity user, int from, int to) {
        JsonArrayBuilder mails = Json.createArrayBuilder();
        getMails(user, from, to).forEach((mail) -> {
            mails.add(exportMailJSON(mail));
        });
        return mails;
    }

    /**
     * Give a JSON string import the necessary fields and create a mail entity.
     * 
     * @param jsonString JSON string representing a mail entity
     * @return           Mail entity or null if the JSON string was not appropriate
     * @throws Exception Throws exception if the input was not valid
     */
    public MailEntity importMailJSON(String jsonString) throws Exception {
        if (jsonString == null) {
            return null;
        }

        String subject, content, receiverid;
        try {
            JsonReader jreader = Json.createReader(new StringReader(jsonString));
            JsonObject jobject = jreader.readObject();
            subject    = jobject.getString("subject", null);
            content    = jobject.getString("content", null);
            receiverid = jobject.getString("receiverId", null);

            //! TODO get the attachments
        }
        catch(Exception ex) {
            Log.warning(TAG, "Could not setup a mail entity out of given JSON string, reason: " + ex.getLocalizedMessage());
            throw new Exception("Invalid input");
        }

        if (receiverid ==  null) {
            Log.warning(TAG, "Could not setup a mail entity out of given JSON string, missing recipient");
            throw new Exception("Missing mail recipient");
        }
        long recvid = 0;
        try {
            recvid = Long.parseLong(receiverid);
        }
        catch(NumberFormatException ex) {}
        if (recvid == 0L) {
            Log.warning(TAG, "Could not setup a mail entity out of given JSON string, invalid recipient");
            throw new Exception("Invalid mail recipient");            
        }

        MailEntity entity = new MailEntity();
        entity.setSubject(subject);
        entity.setContent(content);
        entity.setSendDate((new Date()).getTime());
        entity.setReceiverId(recvid);

        return entity;
    }
}
