/*
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.chat;

import net.m4e.app.communication.ChannelChatEvent;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.ObservesAsync;
import net.m4e.system.core.Log;


/**
 * Central chat functionality providing realtime messanging.
 * 
 * @author boto
 * Date of creation Oct 07, 2017
 */
@ApplicationScoped
public class ChatSystem {

    /**
     * Used for logging
     */
    private final static String TAG = "ChatSystem";

    public void dispatchMessge(@ObservesAsync ChannelChatEvent event) {
        //! TODO
        Log.verbose(TAG, "dispatching chat message...");
    }
}
