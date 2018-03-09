/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.event.business;

import net.m4e.app.communication.ConnectedClients;
import net.m4e.app.mailbox.business.Mails;
import net.m4e.app.resources.DocumentPool;
import net.m4e.app.user.business.Users;
import net.m4e.common.Entities;
import net.m4e.system.core.AppInfos;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.*;


/**
 * Base class for Events related tests
 *
 * @author boto
 * Date of creation March 9, 2018
 */
public class EventsTestBase {

    @Mock
    Users users;
    @Mock
    Entities entities;
    @Mock
    AppInfos appInfos;
    @Mock
    Mails mails;
    @Mock
    DocumentPool docPool;
    @Mock
    ConnectedClients connectedClients;

    Events events;

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);

        events = new Events(entities, users, appInfos, mails, docPool, connectedClients);
    }
}
