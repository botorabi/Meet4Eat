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
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
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
    public void createMailEntity(MailEntity mail) throws Exception {
        Entities eutils = new Entities(entityManager);
        eutils.createEntity(mail);
    }

    /**
     * Get user's mails in given range. Pass 0/0 as range in order to get all user mails.
     * 
     * @param user  User entity
     * @param from  Range begin
     * @param to    Range end
     * @return      User mails in given range
     */
    public List<MailEntity> getUserMails(UserEntity user, int from, int to) {

        //***************************
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        javax.persistence.criteria.CriteriaQuery cq = cb.createQuery();
        javax.persistence.criteria.Root<MailEntity> rt = cq.from(MailEntity.class);

        Predicate pred = cb.equal(rt.get("senderId"), user.getId());

        cq.select(rt).where(pred);
        javax.persistence.Query q = entityManager.createQuery(cq);
        List<MailEntity> mails;
        if (to > 0 && from > 0) {
            mails = q.setMaxResults(to - from + 1).setFirstResult(from).getResultList();
        }
        else {
            mails = q.setMaxResults(100).getResultList();
        }
        //***************************

        return mails;
    }

    /**
     * Give a mail entity export the necessary fields into a JSON object.
     * 
     * @param entity        Mail entity to export
     * @return              A JSON object containing builder the proper entity fields
     */
    public JsonObjectBuilder exportMailJSON(MailEntity entity) {
        JsonObjectBuilder json = Json.createObjectBuilder();
        json.add("id", (entity.getId() != null) ? entity.getId().toString() : "")
            .add("subject", entity.getSubject())
            .add("content", entity.getContent())
            .add("senderId", (entity.getSenderId() != null) ? "" + entity.getSenderId() : "<System>")
            .add("sendDate", "" +((entity.getSendDate()!= null) ? entity.getSendDate() : 0));

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
        getUserMails(user, from, to).forEach((mail) -> {
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
