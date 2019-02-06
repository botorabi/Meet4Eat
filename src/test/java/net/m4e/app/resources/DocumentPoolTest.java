/*
 * Copyright (c) 2017-2019 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.resources;

import net.m4e.app.user.business.UserEntity;
import net.m4e.common.*;
import org.junit.jupiter.api.*;
import org.mockito.*;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Matchers.eq;

/**
 * @author boto
 * Date of creation March 12, 2018
 */
public class DocumentPoolTest {

    private final static String VALID_ETAG = "ValidETAG";
    private final static String INVALID_ETAG = "InvalidETAG";
    private final static String INACTIVE_ETAG = "InactiveETAG";

    @Mock
    Entities entities;

    DocumentPool documentPool;

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);

        documentPool = new DocumentPool(entities);

        DocumentEntity validDocument = DocumentEntityCreator.create();
        validDocument.setETag(VALID_ETAG);

        DocumentEntity inactiveDocument = DocumentEntityCreator.create();
        inactiveDocument.setETag(INACTIVE_ETAG);

        Mockito.doReturn(Arrays.asList(validDocument)).when(entities).findByField(eq(DocumentEntity.class), eq("eTag"), eq(VALID_ETAG));
        Mockito.doReturn(Arrays.asList(inactiveDocument)).when(entities).findByField(eq(DocumentEntity.class), eq("eTag"), eq(INACTIVE_ETAG));
        Mockito.doReturn(Collections.emptyList()).when(entities).findByField(eq(DocumentEntity.class), eq("eTag"), eq(INVALID_ETAG));
    }

    @Test
    void defaultConstructor() {
        new DocumentPool();
    }

    @Test
    void getDocumentInvalidInput() {
        assertThatThrownBy(() -> documentPool.getOrCreatePoolDocument(null))
                .isInstanceOf(IllegalArgumentException.class);

    }

    @Test
    void getDocumentSkipInvalids() {
        DocumentEntity validDocument = DocumentEntityCreator.create();
        validDocument.setETag(VALID_ETAG);

        DocumentEntity inactiveDocument = DocumentEntityCreator.create();
        inactiveDocument.setETag(VALID_ETAG);
        inactiveDocument.getStatus().setEnabled(false);

        DocumentEntity invalidDocument = DocumentEntityCreator.create();
        invalidDocument.setETag(VALID_ETAG);
        invalidDocument.setStatus(null);

        Mockito.doReturn(Arrays.asList(inactiveDocument, invalidDocument, validDocument)).when(entities).findByField(eq(DocumentEntity.class), eq("eTag"), eq(VALID_ETAG));

        assertThat(documentPool.getOrCreatePoolDocument(VALID_ETAG)).isEqualTo(validDocument);
    }

    @Test
    void getDocument() {
        DocumentEntity documentEntity = documentPool.getOrCreatePoolDocument(VALID_ETAG);

        assertThat(documentEntity).isNotNull();
        assertThat(documentEntity.getETag()).isEqualTo(VALID_ETAG);
    }

    @Test
    void createNewDocument() {
        DocumentEntity documentEntity = documentPool.getOrCreatePoolDocument(INVALID_ETAG);

        assertThat(documentEntity).isNotNull();
        assertThat(documentEntity.getStatus().getReferenceCount()).isEqualTo(1L);
    }

    @Nested
    class Releasing {
        @Test
        void releaseDocumentInactive() {
            DocumentEntity inactiveDocument = documentPool.getOrCreatePoolDocument(VALID_ETAG);
            inactiveDocument.getStatus().setEnabled(false);

            assertThat(documentPool.releasePoolDocument(inactiveDocument)).isFalse();
        }

        @Test
        void releaseDocumentNoReference() {
            DocumentEntity noRefDocument = documentPool.getOrCreatePoolDocument(VALID_ETAG);
            noRefDocument.getStatus().setReferenceCount(0L);

            assertThat(documentPool.releasePoolDocument(noRefDocument)).isFalse();
        }

        @Test
        void releaseDocumentSuccess() {
            DocumentEntity document = documentPool.getOrCreatePoolDocument(VALID_ETAG);
            document.getStatus().setReferenceCount(1L);

            assertThat(documentPool.releasePoolDocument(document)).isTrue();
            assertThat(document.getStatus().getReferenceCount()).isEqualTo(0L);
        }
    }

    @Nested
    class Compare {

        private final static String ETAG1 = "ETAG1";
        private final static String ETAG2 = "ETAG2";

        DocumentEntity documentA1;
        DocumentEntity documentA2;
        DocumentEntity documentB;

        @BeforeEach
        void setup() {
            documentA1 = DocumentEntityCreator.create();
            documentA1.setETag(ETAG1);

            documentA2 = DocumentEntityCreator.create();
            documentA2.setETag(ETAG1);

            documentB = DocumentEntityCreator.create();
            documentB.setETag(ETAG2);
        }

        @Test
        void equalDocumentsInvalidInput() {
            assertThat(documentPool.equals(documentA1, null)).isFalse();
            assertThat(documentPool.equals(null, documentA2)).isFalse();
            assertThat(documentPool.equals(null, null)).isFalse();
        }

        @Test
        void equalDocumentsInactive() {
            documentA1.getStatus().setEnabled(false);

            assertThat(documentPool.equals(documentA1, documentA2)).isFalse();

            documentA1.getStatus().setEnabled(true);
            documentA2.getStatus().setEnabled(false);

            assertThat(documentPool.equals(documentA1, documentA2)).isFalse();
        }

        @Test
        void notEqualDocuments() {
            assertThat(documentPool.equals(documentA1, documentB)).isFalse();
        }

        @Test
        void equalDocuments() {
            assertThat(documentPool.equals(documentA1, documentA2)).isTrue();
        }

        @Test
        void notEqualDocumentByETag() {
            assertThat(documentPool.compareETag(null, ETAG1)).isFalse();
            assertThat(documentPool.compareETag(documentA1, null)).isFalse();
            assertThat(documentPool.compareETag(null, null)).isFalse();

            documentA1.getStatus().setEnabled(false);
            assertThat(documentPool.compareETag(documentA1, ETAG1)).isFalse();
        }

        @Test
        void equalDocumentByETag() {
            assertThat(documentPool.compareETag(documentA1, ETAG1)).isTrue();
        }
    }

    @Test
    void updatePhotoNoUpdate() {
        DocumentEntity document = documentPool.getOrCreatePoolDocument(VALID_ETAG);

        UserEntity userEntity = UserEntityCreator.create();
        userEntity.setPhoto(document);

        documentPool.updatePhoto(userEntity, document);

        assertThat(documentPool.equals(userEntity.getPhoto(), document)).isTrue();
    }

    @Test
    void updatePhotoWithExistingDocument() {
        DocumentEntity newPhoto = documentPool.getOrCreatePoolDocument(VALID_ETAG);

        UserEntity userEntity = UserEntityCreator.create();

        documentPool.updatePhoto(userEntity, newPhoto);

        assertThat(documentPool.equals(userEntity.getPhoto(), newPhoto)).isTrue();
        assertThat(newPhoto.getStatus().getReferenceCount()).isEqualTo(2L);
    }

    @Test
    void updatePhotoWithNewDocument() {
        DocumentEntity newPhoto = documentPool.getOrCreatePoolDocument(INVALID_ETAG);

        UserEntity userEntity = UserEntityCreator.create();

        assertThat(documentPool.equals(userEntity.getPhoto(), newPhoto)).isFalse();

        documentPool.updatePhoto(userEntity, newPhoto);

        assertThat(newPhoto.getStatus().getReferenceCount()).isEqualTo(1L);
    }
}