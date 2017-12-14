/*
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

package net.m4e.app.event;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.enterprise.event.Event;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import net.m4e.app.notification.NotifyUserRelativesEvent;
import net.m4e.app.notification.NotifyUsersEvent;
import net.m4e.app.user.UserEntity;


/**
 * Event related notifications are are implemented in this class.
 * Use it to send notifications to related users if any event changes happened.
 *
 * @author boto
 * Date of creation Oct 17, 2017
 */
public class EventNotifications {

    /**
     * Event for sending user notification
     */
    Event<NotifyUsersEvent> notifyUsersEvent;

    /**
     * Event used for sending notification to user's relatives
     */
    Event<NotifyUserRelativesEvent> notifyUserRelativesEvent;

    /**
     * Used to characterize the change type in a notification
     */
    public enum ChangeType {

        Add("add"),

        Remove("remove"),

        Modify("modify");

        private final String value;
        private ChangeType(String value) {
            this.value = value;
        }

        /**
         * Get the string value of enum.
         * 
         * @return String value
         */
        public String value() {
            return value;
        }

        /**
         * Get the past form of the verb such as 'added' or 'modified'.
         * 
         * @return The past form of the verb
         */
        public String pastForm() {
            String text;
            switch(value) {
                case "add":
                    text = "added";
                    break;
                case "remove":
                    text = "removed";
                    break;
                case "modify":
                    text = "modified";
                    break;
                default:
                    text = "?";
            }
            return text;
        }
    }

    /**
     * Create an instance with given javax enterprise events which are used for sending
     * notifications. The notifications are sent using firing asynchronous javax events.
     * 
     * @param notifyUsersEvent          Event used for user related notifications
     * @param notifyUserRelativesEvent  Event used for user relatives notifications
     */
    public EventNotifications(Event<NotifyUsersEvent> notifyUsersEvent, Event<NotifyUserRelativesEvent> notifyUserRelativesEvent) {
        this.notifyUsersEvent = notifyUsersEvent;
        this.notifyUserRelativesEvent = notifyUserRelativesEvent;
    }

    /**
     * Notify about adding/removing an event.
     * 
     * @param changeType    Change type
     * @param event         The event which was added or removed
     * @param user          User sending the notification
     */
    public void sendNotifyEventChanged(ChangeType changeType, UserEntity user, EventEntity event) {
        JsonObjectBuilder json = Json.createObjectBuilder();
        json.add("subject", "Event")
            .add("type", changeType.value() + "event")
            .add("text", "Event was " + changeType.pastForm() + ".");
        JsonObjectBuilder data = Json.createObjectBuilder();
        data.add("eventId", event.getId().toString());
        json.add("data", data);

        notifyEventMembers(user, event, json.build());
    }

    /**
     * Notify event members about adding/removing a location.
     * 
     * @param changeType    Change type
     * @param user          User sending the notification
     * @param event         Members of this event are notified.
     * @param locationId    ID of location which was added/removed
     */
    public void sendNotifyLocationChanged(ChangeType changeType, UserEntity user, EventEntity event, Long locationId) {
        JsonObjectBuilder json = Json.createObjectBuilder();
        json.add("subject", "Event Location")
            .add("type", changeType.value() + "location")
            .add("text", "Location was " + changeType.pastForm() + ".");
        JsonObjectBuilder data = Json.createObjectBuilder();
        data.add("eventId", event.getId().toString())
            .add("locationId", locationId.toString());
        json.add("data", data);

        notifyEventMembers(user, event, json.build());
    }

    /**
     * Notify event members about a user voting/unvoting a location.
     * 
     * @param changeType    Change type
     * @param user          User sending the notification
     * @param event         Members of this event are notified.
     * @param locationId    ID of location which was voted/unvoted
     * @param vote          Pass true for vote, false for unvote
     */
    public void sendNotifyLocationVote(ChangeType changeType, UserEntity user, EventEntity event, Long locationId, boolean vote) {
        JsonObjectBuilder json = Json.createObjectBuilder();
        json.add("subject", "Location Vote")
            .add("type", changeType.value() + "vote")
            .add("text", "Location vote was " + changeType.pastForm() + ".");
        JsonObjectBuilder data = Json.createObjectBuilder();
        data.add("eventId", event.getId().toString())
            .add("locationId", locationId.toString())
            .add("vote", vote);
        json.add("data", data);

        notifyEventMembers(user, event, json.build());
    }

    /**
     * Notify event members about adding/removing a member.
     * 
     * @param changeType    Change type
     * @param user          User sending the notification
     * @param event         Members of this event are notified.
     * @param memberId      ID of member which was added/removed
     */
    public void sendNotifyMemberChanged(ChangeType changeType, UserEntity user, EventEntity event, Long memberId) {
        JsonObjectBuilder json = Json.createObjectBuilder();
        json.add("subject", "Event Member")
            .add("type", changeType.value() + "member")
            .add("text", "Member was " + changeType.pastForm() + ".");
        JsonObjectBuilder data = Json.createObjectBuilder();
        data.add("eventId", event.getId().toString())
            .add("memberId", memberId.toString());
        json.add("data", data);

        notifyEventMembers(user, event, json.build());
    }


    /**
     * Notify user relatives about going on/off, i.e. the online status.
     * 
     * @param user      The user sending its status
     * @param online    Pass true for notifying about going online, otherwise offline
     */
    public void sendNotifyOnlineStatusChanged(UserEntity user, boolean online) {
        JsonObjectBuilder json = Json.createObjectBuilder();
        json.add("subject", "Event Member")
            .add("type", "onlinestatus")
            .add("text", "User went " + (online ? "online" : "offline") + ".");
        JsonObjectBuilder data = Json.createObjectBuilder();
        data.add("onlineStatus", (online ? "online" : "offline"));
        json.add("data", data);

        notifyUserRelatives(user, json.build());
    }

    /**
     * Send a notification to all event members. The notification data is extracted from given notificationJson string, which is
     * expected to have the following fields:
     * 
     *   type (string)
     *   subject (string)
     *   text (string)
     * 
     * @param sender            Sender of this notification, if null is passed then a 0 is used as sender ID.
     * @param event             Members of this meeting event are notified
     * @param jsonObject        JSON object containing the necessary notification fields (subject and text)
     */
    public void notifyEventMembers(UserEntity sender, EventEntity event, JsonObject jsonObject) {
        String subject = jsonObject.getString("subject", "");
        String text = jsonObject.getString("text", "");
        String type = jsonObject.getString("type", "");
        if (subject.isEmpty() && text.isEmpty()) {
            return;
        }

        // the owner and all event members get the notification
        List<Long> userids = new ArrayList();
        userids.add(event.getStatus().getIdOwner());
        Collection<UserEntity> members = event.getMembers();
        if (members != null) {
            members.stream()
                .filter((user) -> (user.getStatus().getIsActive()))
                .forEach(user -> {
                    userids.add(user.getId());
            });
        }

        NotifyUsersEvent notify = new NotifyUsersEvent();
        notify.setRecipientIds(userids);
        notify.setSenderId((sender == null) ? 0L : sender.getId());
        notify.setSubject(subject);
        notify.setType(type);
        notify.setText(text);
        JsonObject data = jsonObject.getJsonObject("data");
        if (data != null) {
            notify.setData(data);
        }
        notifyUsersEvent.fireAsync(notify);
    }

    /**
     * Send a notification to all user relatives.
     * 
     *   subject (string)
     *   text (string)
     * 
     * @param user                      The user
     * @param jsonObject                JSON object containing the necessary notification fields (subject and text)
     */
    public void notifyUserRelatives(UserEntity user, JsonObject jsonObject) {
        String subject = jsonObject.getString("subject", "");
        String text = jsonObject.getString("text", "");
        String type = jsonObject.getString("type", "");
        if (subject.isEmpty() && text.isEmpty()) {
            return;
        }

        NotifyUserRelativesEvent notify = new NotifyUserRelativesEvent();
        notify.setSenderId(user.getId());
        notify.setSubject(subject);
        notify.setType(type);
        notify.setText(text);
        JsonObject data = jsonObject.getJsonObject("data");
        if (data != null) {
            notify.setData(data);
        }
        notifyUserRelativesEvent.fireAsync(notify);
    }
}
