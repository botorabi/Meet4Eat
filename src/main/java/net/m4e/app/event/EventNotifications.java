/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

package net.m4e.app.event;

import java.util.*;

import javax.enterprise.event.Event;

import net.m4e.app.notification.*;
import net.m4e.app.user.business.UserEntity;


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
    private final Event<NotifyUsersEvent> notifyUsersEvent;

    /**
     * Event used for sending notification to user's relatives
     */
    private final Event<NotifyUserRelativesEvent> notifyUserRelativesEvent;

    /**
     * Used to characterize the change type in a notification
     */
    public enum ChangeType {

        Add("add"),

        Remove("remove"),

        Modify("modify");

        private final String value;
        ChangeType(String value) {
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
        Map<String, Object> data = new HashMap<>();
        data.put("eventId", event.getId().toString());

        Notification notification = new Notification("Event",
                "Event was " + changeType.pastForm() + ".",
                changeType.value() + "event",
                data);

        notifyEventMembers(user, event, notification);
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
        Map<String, Object> data = new HashMap<>();
        data.put("eventId", event.getId().toString());
        data.put("locationId", locationId.toString());

        Notification notification = new Notification("Event Location",
                "Location was " + changeType.pastForm() + ".",
                changeType.value() + "location",
                data);

        notifyEventMembers(user, event, notification);
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

        Map<String, Object> data = new HashMap<>();
        data.put("eventId", event.getId().toString());
        data.put("locationId", locationId.toString());
        data.put("vote", vote);

        Notification notification = new Notification("Location Vote",
                "Location vote was " + changeType.pastForm() + ".",
                changeType.value() + "vote",
                data);

        notifyEventMembers(user, event, notification);
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
        Map<String, Object> data = new HashMap<>();
        data.put("eventId", event.getId().toString());
        data.put("memberId", memberId.toString());

        Notification notification = new Notification("Event Member",
                "Member was " + changeType.pastForm() + ".", changeType.value() + "member", data);

        notifyEventMembers(user, event, notification);
    }


    /**
     * Notify user relatives about going on/off, i.e. the online status.
     * 
     * @param user      The user sending its status
     * @param online    Pass true for notifying about going online, otherwise offline
     */
    public void sendNotifyOnlineStatusChanged(UserEntity user, boolean online) {
        Map<String, Object> data = Collections.singletonMap("onlineStatus", (online ? "online" : "offline"));
        Notification notification = new Notification("Event Member",
                "User went " + (online ? "online" : "offline") + ".", "onlinestatus", data);

        notifyUserRelatives(user, notification);
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
     * @param notification        the  notification
     */
    public void notifyEventMembers(UserEntity sender, EventEntity event, Notification notification) {
        String subject = notification.getSubject();
        String text = notification.getText();
        String type = notification.getType();
        if (subject.isEmpty() && text.isEmpty()) {
            return;
        }

        // the owner and all event members get the notification
        List<Long> userids = new ArrayList<>();
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
        if (notification.getData() != null) {
            notify.setData(notification.getData());
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
     * @param notification               the notification
     */
    public void notifyUserRelatives(UserEntity user, Notification notification) {
        String subject = notification.getSubject();
        String text = notification.getText();
        String type = notification.getType();
        if (subject.isEmpty() && text.isEmpty()) {
            return;
        }

        NotifyUserRelativesEvent notify = new NotifyUserRelativesEvent();
        notify.setSenderId(user.getId());
        notify.setSubject(subject);
        notify.setType(type);
        notify.setText(text);
        if (notification.getData() != null) {
            notify.setData(notification.getData());
        }
        notifyUserRelativesEvent.fireAsync(notify);
    }
}
