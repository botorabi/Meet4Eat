/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

package net.m4e.app.communication;


import java.util.Map;

/**
 * Event used for handling an event channel event
 * 
 * NOTE: The name ChannelEventEvent contains 'Event' twice. The former addresses
 * the meeting event (a term used in Meet4Eat), the latter stands for javax.enterprise.event.Event.
 * 
 * @author boto
 * Date of creation Oct 29, 2017
 */
//TODO: 'Event'-Type instead of Map
public class ChannelEventEvent extends ChannelEvent<Map<String,Object>> {

    public ChannelEventEvent() {}
}
