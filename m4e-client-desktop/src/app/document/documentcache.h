/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#ifndef DOCUMENTCACHE_H
#define DOCUMENTCACHE_H

#include <configuration.h>
#include <document/modeldocument.h>
#include <webapp/request/rest-document.h>
#include <QObject>


namespace m4e
{
namespace doc
{

/**
 * @brief The DocumentCache cares about caching resources retrieved from server.
 *        The cache directory is in host user's data directory.
 *
 * @author boto
 * @date Sep 20, 2017
 */
class DocumentCache : public QObject
{
    /**
     * @brief TAG Used for logging
     */
    const std::string TAG = "(DocumentCache) ";

    Q_OBJECT

    public:

        /**
         * @brief Construct an instance.
         *
         * @param p_parent Parent instance
         */
        explicit                        DocumentCache( QObject* p_parent );

        /**
         * @brief Destruct the instance.
         */
        virtual                         ~DocumentCache();

        /**
         * @brief Request a document from server. If it is available in local cache, then no server request will be performed.
         *        The document is delivered by signal 'onDocumentReady'.
         *
         * @param id    Document ID
         * @param eTag  Document ETag, this is used for caching documents locally
         */
        void                            requestDocument( const QString& id, const QString& eTag );

        /**
         * @brief Clear all locally cached documents.
         */
        void                            clearCache();

        /**
         * @brief Purge the local document cache by deleting documents which were not used for a long period of time
         *         given by 'expirationDays'.
         *
         * @param expirationDays Documents which were not fetched longer than given days will be deleted.
         */
        void                            purgeCache( int expirationDays );

    signals:

        /**
         * @brief This signal is emitted when a requested document was arrived. The document will be empty if retrieving it was not successful.
         *
         * @param document   Document
         */
        void                            onDocumentReady( m4e::doc::ModelDocumentPtr document );

    protected slots:

        /**
         * @brief Results of document request.
         *
         * @param document Document
         */
        void                            onRESTDocumentGet( m4e::doc::ModelDocumentPtr document );

        /**
         * @brief Signal is emitted when there were a problem communicating to server or the results status were not ok.
         *
         * @param errorCode Error code if any exits
         * @param reason    Error string
         */
        void                            onRESTDocumentErrorGet( QString errorCode, QString reason );

    protected:

        webapp::RESTDocument*           getOrCreateRESTDocument();

        /**
         * @brief Try to find a document in local cache.
         *
         * @param id        Document ID
         * @param eTag      Document ETag
         * @return          A valid document if found, otherwise an invalid document is returned.
         */
        ModelDocumentPtr                findDocument( const QString& id, const QString& eTag );

        /**
         * @brief Locally cache the document.
         *
         * @param document Document to cache
         * @return         Return true if caching was successful.
         */
        bool                            cacheDocument( ModelDocumentPtr document );

        /**
         * @brief Get the cache directory path. If it does not exist then create it.
         *
         * @return Cache file directory path.
         */
        const QString&                  getOrCreateCacheDirectory();

        /**
         * @brief Store a document to given cache file.
         *
         * @param file     File for storing the document into
         * @param document Document to store
         */
        void                            storeDocument( QFile& file, ModelDocumentPtr document );

        /**
         * @brief Load a document from given cache file.
         *
         * @param file     File containing a stored document
         * @param document Document to load the cache into
         * @return         Return false if the document was not in expected format.
         */
        bool                            loadDocument( QFile& file, ModelDocumentPtr document );

        /**
         * @brief Check the cache file header and update the header if the check was successful.
         *        The "last fetch" timestamp is updated.
         *
         * @param file              Cache file (needs read/write permissions)
         * @param data              Byte array containing the document.
         * @param offset            Offset in 'data' which points to begin of document content
         * @param age               Cache age since last fetch in milliseconds, this can be used for expiration handling
         * @param updateFetchTime   Pass true in order to update the fetch time in cache file.
         * @return                  Return true if the check was successful.
         */
        bool                            checkAndUpdateCacheFileHeader( QFile& file, QByteArray& data, int& offset, ulong& age, bool updateFetchTime );

        /**
         * @brief Read a single field from cache file.
         *
         * @param data
         * @param offset
         * @param fieldName
         * @param fieldValue
         * @return True if successful, otherwise false
         */
        bool                            readNextField( QByteArray& data, int& offset, QString& fieldName, QByteArray& fieldValue );

        webapp::RESTDocument*           _p_restDocument = nullptr;

        QString                         _cacheDir;
};

} // namespace doc
} // namespace m4e

#endif // DOCUMENTCACHE_H
