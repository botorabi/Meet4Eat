/*
 * Copyright (c) 2017-2019 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.event.rest;

import net.m4e.app.event.rest.comm.EventCount;
import net.m4e.common.GenericResponseResult;
import net.m4e.system.core.AppInfoEntity;
import net.m4e.tests.ResponseAssertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * @author boto
 * Date of creation February 20, 2018
 */
class EventRestServiceMiscTest extends EventRestServiceTestBase {

    @Test
    void defaultConstructor() {
        new EventRestService();
    }

    @Test
    void countEvents() {
        Mockito.when(appInfos.getAppInfoEntity()).thenReturn(new AppInfoEntity());

        GenericResponseResult<EventCount> response = restService.count();

        ResponseAssertions.assertThat(response)
                .hasStatusOk()
                .hasData();
    }
}
