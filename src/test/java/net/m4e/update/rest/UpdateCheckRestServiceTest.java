/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.update.rest;

import net.m4e.common.*;
import net.m4e.tests.ResponseAssertions;
import net.m4e.update.business.*;
import net.m4e.update.rest.comm.*;
import org.junit.jupiter.api.*;
import org.mockito.*;

import java.util.List;

import static org.mockito.Matchers.*;

/**
 * @author boto
 * Date of creation March 12, 2018
 */
public class UpdateCheckRestServiceTest {

    @Mock
    Entities entities;
    @Mock
    UpdateChecks updateChecks;
    @Mock
    UpdateCheckValidator validator;

    UpdateCheckRestService restService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);

        restService = new UpdateCheckRestService(entities, updateChecks, validator);
    }

    @Test
    void defaultConstructor() {
        new UpdateCheckRestService();
    }

    @Nested
    class CreateDelete {
        @Test
        void createEntryInvalidInput() throws Exception {
            Mockito.doThrow(new Exception("")).when(validator).validateNewEntityInput(anyObject());

            GenericResponseResult<UpdateCheckId> result = restService.createUpdate(new UpdateCheckEntity());
            ResponseAssertions.assertThat(result)
                    .hasStatusNotOk()
                    .codeIsBadRequest();
        }

        @Test
        void createEntrySuccess() {
            Mockito.doAnswer(invocationOnMock -> {
                UpdateCheckEntity entity = invocationOnMock.getArgumentAt(0, UpdateCheckEntity.class);
                entity.setId(1000L);
                return null;
            }).when(entities).create(anyObject());

            GenericResponseResult<UpdateCheckId> result = restService.createUpdate(new UpdateCheckEntity());

            ResponseAssertions.assertThat(result)
                    .hasStatusOk()
                    .hasData();

            ResponseAssertions.assertThat(result.getData().getId()).isEqualTo("" + 1000L);
        }

        @Test
        void deleteNoExisting() {
            Mockito.doReturn(null).when(entities).find(eq(UpdateCheckEntity.class), anyLong());

            GenericResponseResult<UpdateCheckId> result = restService.remove(0L);

            ResponseAssertions.assertThat(result)
                    .hasStatusNotOk()
                    .codeIsNotFound();
        }

        @Test
        void deleteSuccess() {
            Mockito.doReturn(new UpdateCheckEntity()).when(entities).find(eq(UpdateCheckEntity.class), anyLong());

            GenericResponseResult<UpdateCheckId> result = restService.remove(0L);

            ResponseAssertions.assertThat(result)
                    .hasStatusOk();
        }
    }

    @Nested
    class Modify {

        @Test
        void modifyNoExisting() {
            Mockito.doReturn(null).when(entities).find(eq(UpdateCheckEntity.class), anyLong());

            GenericResponseResult<UpdateCheckId> result = restService.editUpdate(0L, null);

            ResponseAssertions.assertThat(result)
                    .hasStatusNotOk()
                    .codeIsNotFound();
        }

        @Test
        void modifyInvalidInput() throws Exception {
            Mockito.doThrow(new Exception("")).when(validator).validateUpdateEntityInput(anyObject());

            GenericResponseResult<UpdateCheckId> result = restService.editUpdate(0L, null);

            ResponseAssertions.assertThat(result)
                    .hasStatusNotOk()
                    .codeIsBadRequest();
        }

        @Test
        void modifyPartialFields() {
            UpdateCheckEntity entity = new UpdateCheckEntity();
            entity.setId(1000L);

            UpdateCheckEntity input = new UpdateCheckEntity();
            input.setOs(null);
            input.setFlavor(null);
            input.setReleaseDate(0L);
            input.setName(null);
            input.setActive(true);
            input.setUrl(null);
            input.setVersion(null);

            Mockito.doReturn(entity).when(entities).find(eq(UpdateCheckEntity.class), anyLong());

            GenericResponseResult<UpdateCheckId> result = restService.editUpdate(0L, input);

            ResponseAssertions.assertThat(result)
                    .hasStatusOk()
                    .hasData();

            ResponseAssertions.assertThat(result.getData().getId()).isEqualTo("" + 1000L);
        }

        @Test
        void modifySuccess() {
            UpdateCheckEntity entity = new UpdateCheckEntity();
            entity.setId(1000L);

            UpdateCheckEntity input = new UpdateCheckEntity();

            Mockito.doReturn(entity).when(entities).find(eq(UpdateCheckEntity.class), anyLong());

            GenericResponseResult<UpdateCheckId> result = restService.editUpdate(0L, input);

            ResponseAssertions.assertThat(result)
                    .hasStatusOk()
                    .hasData();

            ResponseAssertions.assertThat(result.getData().getId()).isEqualTo("" + 1000L);
        }
    }

    @Nested
    class Find {

        @Test
        void findNotFound() {
            GenericResponseResult<UpdateCheckEntity> result = restService.find(0L);

            ResponseAssertions.assertThat(result)
                    .hasStatusNotOk()
                    .codeIsNotFound();
        }

        @Test
        void findSuccess() {
            Mockito.doReturn(new UpdateCheckEntity()).when(entities).find(eq(UpdateCheckEntity.class), anyLong());

            GenericResponseResult<UpdateCheckEntity> result = restService.find(0L);

            ResponseAssertions.assertThat(result)
                    .hasStatusOk();
        }

        @Test
        void findAll() {
            GenericResponseResult<List<UpdateCheckEntity>> result = restService.findAllUpdates();

            ResponseAssertions.assertThat(result)
                    .hasStatusOk()
                    .hasData();
        }

        @Test
        void findRange() {
            GenericResponseResult<List<UpdateCheckEntity>> result = restService.findRange(0, 10);

            ResponseAssertions.assertThat(result)
                    .hasStatusOk()
                    .hasData();
        }
    }

    @Nested
    class UpdateCheck {

        @Test
        void checkFail() throws Exception {
            Mockito.doThrow(new Exception("Update check failed!")).when(updateChecks).checkForUpdate(anyObject());

            GenericResponseResult<UpdateCheckResult> result = restService.checkForUpdate(new UpdateCheckCmd());

            ResponseAssertions.assertThat(result)
                    .hasStatusNotOk()
                    .codeIsBadRequest();
        }

        @Test
        void checkSuccess() throws Exception {
            Mockito.doReturn(new UpdateCheckResult()).when(updateChecks).checkForUpdate(anyObject());

            GenericResponseResult<UpdateCheckResult> result = restService.checkForUpdate(new UpdateCheckCmd());

            ResponseAssertions.assertThat(result)
                    .hasStatusOk()
                    .hasData();
        }
    }

    @Test
    void countUpdateEntries() {
        GenericResponseResult<UpdateCheckCount> result = restService.count();

        ResponseAssertions.assertThat(result)
                .hasStatusOk()
                .hasData();
    }
}
