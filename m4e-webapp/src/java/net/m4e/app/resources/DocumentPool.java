/*
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

package net.m4e.app.resources;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import javax.persistence.EntityManager;
import net.m4e.common.EntityUtils;
import net.m4e.system.core.Log;

/**
 * This class manages a pool of documents. Documents are handled as sharable entities which
 * can be referenced by other entities. Documents entity's etag is used to detect documents
 * with same content.
 * 
 * NOTE: The Status field ReferenceCount of a document can be used for purging purpose.
 * 
 * @author boto
 * Date of creation Sep 15, 2017
 */
public class DocumentPool {

    /**
     * Used for logging
     */
    private final static String TAG = "DocumentPool";

    private final EntityManager entityManager;

    /**
     * Create an instance of document pool.
     * 
     * @param entityManager    Entity manager
     */
    public DocumentPool(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /**
     * Try to find a document in pool with the same content etag and return it and increase 
     * its reference count, if found. If no such document exists then a new pool document is
     * created and returned.
     * 
     * @param etag        Document etag to find
     * @return            A document entity with given content etag
     * @throws Exception  Throws an exception if something goes wrong.
     */
    public DocumentEntity getOrCreatePoolDocument(String etag) throws Exception {
        if (Objects.isNull(etag)) {
            throw new Exception("Invalid document etag");
        }
        DocumentEntity doc = findPoolDocument(etag);
        if (Objects.isNull(doc)) {
            return createDocument();
        }
        return doc;
    }

    /**
     * Release the document from pool. This call decreases the document reference count.
     * 
     * @param document   The document to release
     * @return           Return true if the document was successfully released from pool. Return false
     *                    if the document was invalid or it was not found in pool.
     */
    public boolean releasePoolDocument(DocumentEntity document) {
        if (Objects.isNull(document) || !document.getStatus().getIsActive()) {
            return false;
        }
        if (document.getStatus().getReferenceCount() < 1L) {
            Log.warning(TAG, "No reference to given document exists.");
            return false;
        }
        document.getStatus().decreaseRefCount();
        EntityUtils eutils = new EntityUtils(entityManager);
        eutils.updateEntity(document);
        return true;
    }

    /**
     * Compare two given documents by their content etags.
     * 
     * @param document1  Document to compare
     * @param document2  Document to compare
     * @return           Return true if both are valid (i.e. active) and have the same content etag, otherwise return false.
     */
    public boolean equals(DocumentEntity document1, DocumentEntity document2) {
        if (Objects.isNull(document1) || Objects.isNull(document2)) {
            return false;
        }
        if (!document1.getStatus().getIsActive() || !document2.getStatus().getIsActive()) {
            return false;
        }
        return Objects.equals(document1.getETag(), document2.getETag());
    }

    /**
     * Compare a document content with given document etag.
     * 
     * @param document  Document to compare
     * @param etag      Document etag to compare
     * @return          Return true if document is valid (i.e. active) and has the given content etag, otherwise return false.
     */
    public boolean compareETag(DocumentEntity document, String etag) {
        if (Objects.isNull(document) || !document.getStatus().getIsActive() || Objects.isNull(etag)) {
            return false;
        }
        return Objects.equals(document.getETag(), etag);
    }

    /**
     * Create a document entity in database. Its reference count will be set to 1.
     * 
     * @param document  The document which should be created
     * @return          The document instance, or null if it could not be created in database.
     */
    private DocumentEntity createDocument() {
        EntityUtils eutils = new EntityUtils(entityManager);
        DocumentEntity document = new DocumentEntity();
        document.updateETag();
        StatusEntity status = new StatusEntity();
        status.setDateCreation((new Date()).getTime());
        status.setDateLastUpdate((new Date()).getTime());
        status.setReferenceCount(1L);
        document.setStatus(status);
        eutils.createEntity(document);
        return document;
    }

    /**
     * Go through all document entities and try to find a document with given etag. If a document
     * was found then increase its reference count, update its 'last update date' and return it.
     * 
     * @param etag          Document etag to find
     * @return              A document entity or null if no document with given etag was found.
     */
    private DocumentEntity findPoolDocument(String etag) {
        EntityUtils eutils = new EntityUtils(entityManager);
        List<DocumentEntity> documents = eutils.findEntityByField(DocumentEntity.class, "eTag", etag);
        for (DocumentEntity doc: documents) {
            if (doc.getStatus().getIsActive() && Objects.equals(doc.getETag(), etag)) {
                // update the document reference count
                doc.getStatus().increaseRefCount();
                doc.getStatus().setDateLastUpdate((new Date()).getTime());
                eutils.updateEntity(doc);
                return doc;
            }
        }
        return null;
    }
}
