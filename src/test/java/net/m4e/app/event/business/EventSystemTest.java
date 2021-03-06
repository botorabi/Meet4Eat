/*
 * Copyright (c) 2017-2019 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.event.business;

import net.m4e.app.communication.*;
import net.m4e.app.user.business.UserEntity;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.*;

import static java.util.Collections.singletonList;
import static org.mockito.Matchers.*;

/**
 * @author ybroeker
 */
class EventSystemTest {

    @Test
    void dispatchMessage() {
        Events events = Mockito.mock(Events.class);
        Mockito.when(events.getMembers(Mockito.anyLong())).thenReturn(new HashSet<>());
        UserEntity entity = new UserEntity();
        entity.setId(42L);
        ConnectedClients connections = Mockito.mock(ConnectedClients.class);
        Mockito.when(connections.getConnectedUser(any())).thenReturn(entity);
        EventSystem eventSystem = new EventSystem(events, connections);

        ChannelEventEvent event = new ChannelEventEvent();
        event.setSenderId(1L);
        event.setPacket(buildPacket());
        eventSystem.dispatchMessage(event);

        Mockito.verify(connections).sendPacket(any(), eq(singletonList(42L)));
    }

    Packet<Map<String, Object>> buildPacket() {
        Map<String, Object> innerData = new HashMap<>();
        innerData.put("eventId", "15");

        Map<String, Object> data = new HashMap<>();
        data.put("data", innerData);

        Packet<Map<String, Object>> packet = new Packet<>();

        packet.setData(data);
        return packet;
    }
}
