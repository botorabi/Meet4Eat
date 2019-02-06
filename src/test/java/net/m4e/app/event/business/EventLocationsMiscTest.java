/*
 * Copyright (c) 2017-2019 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.event.business;

import net.m4e.app.resources.DocumentEntity;
import org.junit.jupiter.api.Test;


/**
 * Base class for event related tests
 *
 * @author boto
 * Date of creation February 14, 2018
 */
public class EventLocationsMiscTest extends EventLocationsTestBase {

    @Test
    void defaultConstructor() {
        new EventLocations();
    }

    @Test
    void getVotes() {
        eventLocations.getVotes(new EventEntity(), 0L, 0L);
    }

    @Test
    void findLocation() {
        eventLocations.findLocation(0L);
    }

    @Test
    void updateLocationImage() {
        eventLocations.updateEventLocationImage(new EventLocationEntity(), new DocumentEntity());
    }
}
