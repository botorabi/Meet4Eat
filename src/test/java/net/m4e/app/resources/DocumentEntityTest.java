/*
 * Copyright (c) 2017-2019 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.resources;

import net.m4e.tests.EntityAssertions;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * @author boto
 * Date of creation March 12, 2018
 */
public class DocumentEntityTest {

    @Test
    void commonEntityTests() {
        EntityAssertions.assertThat(DocumentEntity.class)
                .isSerializable()
                .hasSerialVersionUID()
                .hasEntityAnnotation()
                .hasIdAnnotation()
                .conformsToEqualsContract()
                .hasHashCode()
                .hasProperToString();
    }

    @Test
    void setterGetter() {
        DocumentEntity entity = DocumentEntityCreator.create();

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(entity.getId()).isEqualTo(DocumentEntityCreator.DOC_ID);
        softly.assertThat(entity.getStatus()).isNotNull();
        softly.assertThat(entity.getName()).isEqualTo(DocumentEntityCreator.DOC_NAME);
        softly.assertThat(entity.getEncoding()).isEqualTo(DocumentEntityCreator.DOC_ENCODING);
        softly.assertThat(entity.getResourceURL()).isEqualTo(DocumentEntityCreator.DOC_RES_URL);
        softly.assertThat(entity.getContent()).isEqualTo(DocumentEntityCreator.DOC_CONTENT);
        softly.assertThat(entity.getETag()).isEqualTo(DocumentEntityCreator.DOC_ETAG);
        softly.assertThat(entity.getIsEmpty()).isFalse();

        softly.assertAll();
    }

    @Test
    void updateContent() {
        DocumentEntity entity = DocumentEntityCreator.create();
        entity.setETag(null);

        entity.updateContent(DocumentEntityCreator.DOC_CONTENT);

        assertThat(entity.getETag()).isNotEmpty();
    }

    @Test
    void checkEmptyDocument() {
        DocumentEntity entity = DocumentEntityCreator.create();
        entity.updateContent(null);

        assertThat(entity.getIsEmpty()).isTrue();
        assertThat(entity.getETag()).isEmpty();
    }
}
