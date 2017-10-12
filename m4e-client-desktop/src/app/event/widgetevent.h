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
#include <QLabel>
#include <QMap>


namespace Ui
{
    class WidgetEvent;
}


namespace m4e
{
namespace event
{

/**
 * @brief Class for decorating the "Event" widget.
 *
 * @author boto
 * @date Aug 2, 2017
 */
class WidgetEvent : public QWidget
{
    /**
     * @brief TAG Used for logging
     */
    const std::string TAG = "(WidgetEvent) ";

    Q_OBJECT

    public:

        /**
         * @brief Create a new "Event" widget
         *
         * @param p_webApp          Web application interface
         * @param p_parent          Parent widget
         */
                                    WidgetEvent( webapp::WebApp* p_webApp, QWidget* p_parent = nullptr );

        /**
         * @brief Destroy WidgetEvent
         */
        virtual                     ~WidgetEvent();

        /**
         * @brief Set the event which is represented by this widget.
         *
         * @param id    Event ID
         */
        void                        setEvent( const QString& id );

        /**
         * @brief Set the ChatSystem which allows the chat messanging.
         *
         * @param p_chatSystem  The chat system
         */
        void                        setChatSystem( m4e::chat::ChatSystem* p_chatSystem );

    protected slots:

        void                        onBtnBuzzClicked();

        void                        onBtnRemoveVotesClicked();

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
         * @brief Results of remove event location request.
         *
         * @param success    true if user data could successfully be retrieved, otherwise false
         * @param eventId    ID of event
         * @param locationId ID of location to remove
         */
        void                        onResponseRemoveLocation( bool success, QString eventId, QString locationId );

    protected:

        /**
         * @brief Setup the widget.
         */
        void                        setupUI();

        /**
         * @brief Setup the head elements in event widget (info fields, etc.)
         */
        void                        setupWidgetHead();

        /**
         * @brief Setup all location widgets.
         */
        void                        setupLocations();

        /**
         * @brief Setup a widget informing that the event has no location.
         */
        void                        setupNoLocationWidget();

        /**
         * @brief Add a new location for an event
         *
         * @param location      New location to add
         * @param userIsOwner   Is the user also the owner of the event?
         */
        void                        addLocation( event::ModelLocationPtr location, bool userIsOwner );

        /**
         * @brief Setup the event members.
         */
        void                        setEventMembers();

        Ui::WidgetEvent*            _p_ui           = nullptr;

        QListWidget*                _p_clientArea   = nullptr;

        webapp::WebApp*             _p_webApp       = nullptr;

        m4e::chat::ChatSystem*      _p_chatSystem   = nullptr;

        typedef QMap< QString /*id*/, QString /*name*/>  Locations;

        m4e::event::ModelEventPtr   _event;
};

} // namespace event
} // namespace m4e

#endif // WIDGETEVENT_H
