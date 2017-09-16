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
import javax.transaction.UserTransaction;
import net.m4e.common.EntityUtils;
import net.m4e.system.core.Log;

/**
 * This class manages a pool of images. Images are handled as sharable entities which
 * can be referenced by other entities. Image entity's imageHash is used to detect images
 * with same content.
 * 
 * NOTE: The Status field ReferenceCount of an image can be used for purging purpose.
 * 
 * @author boto
 * Date of creation Sep 15, 2017
 */
public class ImagePool {

    /**
     * Used for logging
     */
    private final static String TAG = "ImagePool";

    private final EntityManager entityManager;

    private final UserTransaction userTransaction;

    /**
     * Create an instance of image pool.
     * 
     * @param entityManager    Entity manager
     * @param userTransaction  User transaction
     */
    public ImagePool(EntityManager entityManager, UserTransaction userTransaction) {
        this.entityManager = entityManager;
        this.userTransaction = userTransaction;
    }

    /**
     * Try to find an image in pool with the same content hash and return it and increase 
     * its reference count, if found. If no such image exists then a new pool image is created and returned.
     * 
     * @param contentHash         Image content hash to find
     * @return                    An image entity with given content hash
     * @throws Exception          Throws an exception if something goes wrong.
     */
    public ImageEntity getOrCreatePoolImage(String contentHash) throws Exception {
        if (Objects.isNull(contentHash)) {
            throw new Exception("Invalid image hash");
        }
        ImageEntity img = findPoolImage(contentHash);
        if (Objects.isNull(img)) {
            return createImage();
        }
        return img;
    }

    /**
     * Release the image from pool. This call decreases the image reference count.
     * 
     * @param image      The imge to release
     * @return           Return true if the image was successfully released from pool. Return false
     *                    if the image was invalid or it was not found in pool.
     * @throws Exception Throws an exception if the image is not valid.
     */
    public boolean releasePoolImage(ImageEntity image) throws Exception {
        if (Objects.isNull(image) || image.getStatus().getIsDeleted()) {
            return false;
        }
        if (image.getStatus().getReferenceCount() < 1L) {
            Log.warning(TAG, "No reference to given image exists.");
            return false;
        }
        image.getStatus().decreaseRefCount();
        EntityUtils eutils = new EntityUtils(entityManager, userTransaction);
        eutils.updateEntity(image);
        return true;
    }

    /**
     * Compare two given images by their content hashs.
     * 
     * @param image1    Image to compare
     * @param image2    Image to compare
     * @return          Return true if both are valid (not marked as deleted) and have the same content hash, otherwise return false.
     */
    public boolean equals(ImageEntity image1, ImageEntity image2) {
        if (Objects.isNull(image1) || Objects.isNull(image2)) {
            return false;
        }
        if (image1.getStatus().getIsDeleted() || image2.getStatus().getIsDeleted()) {
            return false;
        }
        return Objects.equals(image1.getImageHash(), image2.getImageHash());
    }

    /**
     * Compare an image content and given image hash.
     * 
     * @param image      Image to compare
     * @param imageHash  Image hash to compare
     * @return           Return true if image is valid (not marked as deleted) and has the given content hash, otherwise return false.
     */
    public boolean compareImageHash(ImageEntity image, String imageHash) {
        if (Objects.isNull(image) || image.getStatus().getIsDeleted() || Objects.isNull(imageHash)) {
            return false;
        }
        return Objects.equals(image.getImageHash(), imageHash);
    }

    /**
     * Create an image entity in database. Its reference count will be set to 1.
     * 
     * @param image  The image which should be created
     * @return       The image instance, or null if it could not be created in database.
     */
    private ImageEntity createImage() {
        EntityUtils eutils = new EntityUtils(entityManager, userTransaction);
        ImageEntity image = new ImageEntity();
        image.updateImageHash();
        StatusEntity status = new StatusEntity();
        status.setDateCreation((new Date()).getTime());
        status.setDateLastUpdate((new Date()).getTime());
        status.setReferenceCount(1L);
        image.setStatus(status);
        try {
            eutils.createEntity(image);
        }
        catch(Exception ex) {
            Log.warning(TAG, "Could not create image, reason: " + ex.getLocalizedMessage());
            return null;
        }
        return image;
    }

    /**
     * Go through all image entities and try to find an image with given content hash. If an image
     * was found then increase its reference count and return it.
     * 
     * @param contentHash   Image hash to find
     * @return              An image entity or null if no image with given content hash was found.
     * @throws Exception    Throws an exception if the image is not valid.
     */
    private ImageEntity findPoolImage(String contentHash) throws Exception {
        EntityUtils eutils = new EntityUtils(entityManager, userTransaction);
        List<ImageEntity> images = eutils.findEntityByField(ImageEntity.class, "imageHash", contentHash);
        for (ImageEntity img: images) {
            if (!img.getStatus().getIsDeleted() && Objects.equals(img.getImageHash(), contentHash)) {
                // update the image reference count
                img.getStatus().increaseRefCount();
                img.getStatus().setDateLastUpdate((new Date()).getTime());
                eutils.updateEntity(img);
                return img;
            }
        }
        return null;
    }
}
