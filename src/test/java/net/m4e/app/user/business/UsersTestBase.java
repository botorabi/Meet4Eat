/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.user.business;

import net.m4e.app.resources.DocumentPool;
import net.m4e.common.Entities;
import net.m4e.system.core.*;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.*;

/**
 * Base test class for Users
 *
 * @author boto
 * Date of creation February 6, 2018
 */
class UsersTestBase implements DefaultUserData {

    @Mock
    Entities entities;

    @Mock
    AppInfos appInfos;

    @Mock
    AppInfoEntity appInfo;

    @Mock
    DocumentPool docPool;

    Users users;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        users = new Users(entities, appInfos, docPool);

        Mockito.when(appInfos.getAppInfoEntity()).thenReturn(appInfo);
    }
}
