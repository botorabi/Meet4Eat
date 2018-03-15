/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.user.business;

import net.m4e.app.auth.RoleEntity;
import net.m4e.app.resources.DocumentPool;
import net.m4e.common.*;
import net.m4e.system.core.*;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.*;
import org.mockito.stubbing.Answer;

import java.util.*;

import static org.mockito.Matchers.*;

/**
 * Base test class for Users
 *
 * @author boto
 * Date of creation February 6, 2018
 */
class UsersTestBase extends UserEntityCreator {

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

        Mockito.when(entities.findByField(eq(RoleEntity.class), eq("name"), anyString()))
                .thenAnswer((Answer<List<RoleEntity>>) invocationOnMock -> {

                    String roleName = invocationOnMock.getArgumentAt(2, String.class);

                    if (Users.getAvailableUserRoles().contains(roleName) ) {
                        RoleEntity roleEntity = new RoleEntity();
                        roleEntity.setId(200L);
                        roleEntity.setName(roleName);
                        return Arrays.asList(roleEntity);
                    }

                    return Collections.emptyList();
                });
    }
}
