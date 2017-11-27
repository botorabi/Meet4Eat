/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#ifndef WIDGETEVENT_H
#define WIDGETEVENT_H

#include <webapp/webapp.h>
#include <event/modelevent.h>
#include <event/modellocation.h>
#include <chat/chatsystem.h>
#include <QListWidget>
#include <QWidget>
#include <QTimer>
#include <QLabel>
#include <QMap>


namespace Ui
{
    class WidgetEventPanel;
}


namespace m4e
{
namespace event
{

class WidgetLocation;

/**
 * @brief Class for decorating the "Event" widget.
 *
 * @author boto
 * @date Aug 2, 2017
 */
class WidgetEventPanel : public QWidget
{
    /**
     * @brief TAG Used for logging
     */
    const std::string TAG = "(WidgetEventPanel) ";

    Q_OBJECT

    public:

        /**
         * @brief Create a new event panel widget
         *
         * @param p_webApp          Web application interface
         * @param p_parent          Parent widget
         */
                                    WidgetEventPanel( webapp::WebApp* p_webApp, QWidget* p_parent = nullptr );

        /**
         * @brief Destroy the panel.
         */
        virtual                     ~WidgetEventPanel();

        /**
         * @brief Setup the widget for an event with given ID.
         *
         * @param id    Event ID
         */
        void                        setupEvent( const QString& id );

        /**
         * @brief Get the event ID.
         *
         * @return The event ID, or empty string if no event was setup before.
         */
        QString                     getEventId() const;

    signals:

        /**
         * @brief This signal is emitted when the user requests for creating a new location.
         *
         * @param eventId  The ID of event which should get a new location
         */
        void                        onCreateNewLocation( QString eventId );

    protected slots:

        /**
         * @brief This signal is received when an event voting time was reached.
         *
         * @param event The event
         */
        void                        onLocationVotingStart(  m4e::event::ModelEventPtr event );

        /**
         * @brief This signal is received when an event voting time has ended.
         *
         * @param event The event
         */
        void                        onLocationVotingEnd(  m4e::event::ModelEventPtr event );

        /**
         * @brief Called when a link in any QLabel was clicked.
         *
         * @param link Activated link
         */
        void                        onLinkActivated( QString link );

        /**
         * @brief The buzz button was clicked.
         */
        void                        onBtnBuzzClicked();

        /**
         * @brief Emitted when the user clicks "location delete" button in location widget.
         *
         * @param id   The location ID
         */
        void                        onDeleteLocation( QString id );

        /**
         * @brief The chat widget has a new message to send.
         *
         * @param msg Chat message
         */
        void                        onSendMessage( m4e::chat::ChatMessagePtr msg );

        /**
         * @brief This signal is received from chat system whenever a new event chat message arrived.
         *
         * @param msg Chat message
         */
        void                        onReceivedChatMessageEvent( m4e::chat::ChatMessagePtr msg );

        /**
         * @brief This signal is received  when an event message was arrived. An event message can be used to buzz all event members.
         *
         * @param senderId      Message sender Id (usually an user ID)
         * @param senderName    Message sender's name
         * @param eventId       ID of receiving event
         * @param notify        Notification object containing the message content
         */
        void                        onEventMessage( QString senderId, QString senderName, QString eventId, m4e::notify::NotifyEventPtr notify );

        /**
         * @brief Notify about a user's online status.
         *
         * @param senderId      User ID
         * @param senderName    User Name
         * @param online        true if the user went online, otherwise false for user going offline
         */
        void                        onUserOnlineStatusChanged( QString senderId, QString senderName, bool online );

        /**
         * @brief Results of remove event location request.
         *
         * @param success    true if user data could successfully be retrieved, otherwise false
         * @param eventId    ID of event
         * @param locationId ID of location to remove
         */
        void                        onResponseRemoveLocation( bool success, QString eventId, QString locationId );

        /**
         * @brief Results of location votes request by time range.
         *
         * @param success   true if user votes could successfully be retrieved, otherwise false
         * @param votes     The event location votes list
         */
        void                        onResponseGetLocationVotesByTime( bool success, QList< m4e::event::ModelLocationVotesPtr > votes );

        /**
         * @brief This signal is emitted when an event location vote arrives.
         *
         * @param senderId   User ID of the voter
         * @param senderName User name of the voter
         * @param eventId    Event ID
         * @param loactionId Event location ID
         * @param vote       true for vote and false for unvote the given location
         */
        void                        onEventLocationVote( QString senderId, QString senderName, QString eventId, QString locationId, bool vote );

        /**
         * @brief Timeout callback used for buzz button activation
         */
        void                        onBuzzActivationTimer();

    protected:

        /**
         * @brief Setup the widget.
         */
        void                        setupUI();

        /**
         * @brief Schedule a time in milliseconds for activating the buzz button. This
         * is used for spam protection.
         *
         * @param msec    Time to activate the button in milliseconds
         */
        void                        scheduleEnableBuzz( int msec );

        /**
         * @brief Setup the head elements in event widget (info fields, etc.)
         */
        void                        setupWidgetHead();

        /**
         * @brief Get all chat messages of current event and restore the chat window.
         */
        void                        restoreChatMessages();

        /**
         * @brief Setup all location widgets.
         */
        void                        setupLocations();

        /**
         * @brief Add a new location for an event
         *
         * @param event         The event
         * @param location      New location to add
         * @param userIsOwner   Is the user also the owner of the event?
         */
        void                        addLocation( event::ModelEventPtr event, event::ModelLocationPtr location, bool userIsOwner );

        /**
         * @brief Try to find the widget item of an event location given its ID.
         *
         * @param locationId  Location ID
         * @return Widget item, or nullptr if the ID was not found
         */
        QListWidgetItem*            findLocationItem( const QString& locationId );

        /**
         * @brief Setup the event members.
         */
        void                        setEventMembers();

        /**
         * @brief Request for currently running event location votes.
         *
         * @return Return false if it's not time to vote.
         */
        bool                        requestCurrentLoctionVotes();

        /**
         * @brief Try to find a WidgetLocation given its ID.
         *
         * @param locationId    The location ID
         * @return              The widget if the ID was found, otherwise nullptr
         */
        WidgetLocation*             findWidgetLocation( const QString& locationId );

        Ui::WidgetEventPanel*       _p_ui                   = nullptr;

        QListWidget*                _p_clientArea           = nullptr;

        QTimer*                     _p_buzzActivationTimer  = nullptr;

        webapp::WebApp*             _p_webApp               = nullptr;

        typedef QMap< QString /*id*/, QString /*name*/>  Locations;

        m4e::event::ModelEventPtr   _event;

        QList< WidgetLocation* >    _widgets;
};

} // namespace event
} // namespace m4e

#endif // WIDGETEVENT_H
