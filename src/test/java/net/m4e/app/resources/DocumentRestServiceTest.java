/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.resources;

import net.m4e.common.*;
import net.m4e.tests.ResponseAssertions;
import org.junit.jupiter.api.*;
import org.mockito.*;

import javax.servlet.http.HttpServletRequest;

import static org.mockito.Matchers.eq;

/**
 * @author boto
 * Date of creation March 12, 2018
 */
class DocumentRestServiceTest {

    private final Long VALID_DOC_ID = 1000L;
    private final Long INVALID_DOC_ID = 1100L;
    private final Long INACTIVE_DOC_ID = 1200L;

    @Mock
    Entities entities;

    @Mock
    HttpServletRequest request;

    DocumentRestService restService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);

        restService = new DocumentRestService(entities);

        DocumentEntity validDocument = DocumentEntityCreator.create();
        validDocument.setId(VALID_DOC_ID);

        DocumentEntity inactiveDocument = DocumentEntityCreator.create();
        inactiveDocument.getStatus().setEnabled(false);
        inactiveDocument.setId(INACTIVE_DOC_ID);

        Mockito.doReturn(validDocument).when(entities).find(eq(DocumentEntity.class), eq(VALID_DOC_ID));
        Mockito.doReturn(inactiveDocument).when(entities).find(eq(DocumentEntity.class), eq(INACTIVE_DOC_ID));
        Mockito.doReturn(null).when(entities).find(eq(DocumentEntity.class), eq(INVALID_DOC_ID));
    }

    @Test
    void defaultConstructor() {
        new DocumentRestService();
    }

    @Test
    void findDocumentNotExisting() {
        GenericResponseResult<DocumentInfo> result = restService.find(INVALID_DOC_ID, request);

        ResponseAssertions.assertThat(result)
                .hasStatusNotOk()
                .codeIsNotFound();
    }

    @Test
    void findDocumentInactive() {
        GenericResponseResult<DocumentInfo> result = restService.find(INACTIVE_DOC_ID, request);

        ResponseAssertions.assertThat(result)
                .hasStatusNotOk()
                .codeIsNotFound();
    }

    @Test
    void findDocumentSuccess() {
        GenericResponseResult<DocumentInfo> result = restService.find(VALID_DOC_ID, request);

        ResponseAssertions.assertThat(result).hasStatusOk();

        DocumentInfo docInfo = result.getData();
        ResponseAssertions.assertThat(docInfo.getId()).isEqualTo("" + DocumentEntityCreator.DOC_ID);
        ResponseAssertions.assertThat(docInfo.getName()).isEqualTo(DocumentEntityCreator.DOC_NAME);
        ResponseAssertions.assertThat(docInfo.getEncoding()).isEqualTo(DocumentEntityCreator.DOC_ENCODING);
        ResponseAssertions.assertThat(docInfo.getETag()).isEqualTo(DocumentEntityCreator.DOC_ETAG);
        ResponseAssertions.assertThat(docInfo.getType()).isEqualTo(DocumentEntityCreator.DOC_TYPE);
        ResponseAssertions.assertThat(docInfo.getResourceURL()).isEqualTo(DocumentEntityCreator.DOC_RES_URL);
        ResponseAssertions.assertThat(docInfo.getContent().getBytes()).isEqualTo(DocumentEntityCreator.DOC_CONTENT);
    }
}
