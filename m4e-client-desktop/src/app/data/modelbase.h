/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#ifndef MODELBASE_H
#define MODELBASE_H

#include <configuration.h>
#include <QString>


namespace m4e
{
namespace data
{

/**
 * @brief A base class for  some model types. It contains common fields such ID, name, description, etc.
 *
 * @author boto
 * @date Sep 19, 2017
 */
class ModelBase
{
    public:

        /**
         * @brief Construct an instance.
         */
                                            ModelBase() {}

        /**
         * @brief Get the unique ID.
         *
         * @return The ID
         */
        const QString&                      getId() const { return _id; }

        /**
         * @brief Set the unique ID.
         *
         * @param id    The ID
         */
        void                                setId( const QString &id ) { _id = id; }

        /**
         * @brief Get the name.
         * @return The name
         */
        const QString&                      getName() const { return _name; }

        /**
         * @brief Set the name.
         *
         * @param name  The name
         */
        void                                setName( const QString& name ) { _name = name; }

        /**
         * @brief Get the description.
         *
         * @return The description
         */
        const QString&                      getDescription() const { return _description; }

        /**
         * @brief Set the description.
         *
         * @param name  The description
         */
        void                                setDescription( const QString& description ) { _description = description; }

        /**
         * @brief Get the photo ID.
         * @return The photo ID
         */
        const QString&                      getPhotoId() const { return _photoId; }

        /**
         * @brief Set the photo ID.
         *
         * @param name  The photo ID
         */
        void                                setPhotoId( const QString& id ) { _photoId = id; }

        /**
         * @brief Get the photo ETag.
         * @return The photo ETag
         */
        const QString&                      getPhotoETag() const { return _photoETag; }

        /**
         * @brief Set the photo ETag.
         *
         * @param name  The photo ETag
         */
        void                                setPhotoETag( const QString& etag ) { _photoETag = etag; }

    protected:

        virtual                             ~ModelBase() {}

        //! Omit copy construction!
                                            ModelBase( const ModelBase& );

        QString                             _id;
        QString                             _name;
        QString                             _description;
        QString                             _photoId;
        QString                             _photoETag;
};

} // namespace data
} // namespace m4e

#endif // MODELBASE_H
