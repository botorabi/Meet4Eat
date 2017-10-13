/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#ifndef MODELDOCUMENT_H
#define MODELDOCUMENT_H

#include <configuration.h>
#include <core/smartptr.h>
#include <event/modellocation.h>
#include <QJsonDocument>
#include <QString>


namespace m4e
{
namespace doc
{

/**
 * @brief A class for defining a document
 *
 * @author boto
 * @date Sep 19, 2017
 */
class ModelDocument : public m4e::core::RefCount< ModelDocument >
{
    SMARTPTR_DEFAULTS( ModelDocument )

    public:

        /**
         * @brief Construct an instance.
         */
                                            ModelDocument() {}

        /**
         * @brief Get the unique document ID.
         *
         * @return Document ID
         */
        const QString&                      getId() const { return _id; }

        /**
         * @brief Set the unique document ID.
         *ETag
         * @param id Document ID
         */
        void                                setId( const QString &id ) { _id = id; }

        /**
         * @brief Get the document name.
         *
         * @return Document name
         */
        const QString&                      getName() const { return _name; }

        /**
         * @brief Set the document name.
         *
         * @param name  Document name
         */
        void                                setName( const QString& name ) { _name = name; }

        /**
         * @brief Get the document type.
         *
         * @return Document type
         */
        const QString&                      getType() const { return _type; }

        /**
         * @brief Set the document type.
         *
         * @param name  Document type
         */
        void                                setType( const QString& type ) { _type = type; }

        /**
         * @brief Get encoding.
         *
         * @return Document encoding
         */
        const QString&                      getEncoding() const { return _encoding; }

        /**
         * @brief Set document encoding.
         *
         * @param name  Document encoding
         */
        void                                setEncoding( const QString& encoding ) { _encoding = encoding; }

        /**
         * @brief Get the document content.
         * @return Document content
         */
        const QByteArray&                   getContent() const { return _content; }

        /**
         * @brief Set the document content.
         *
         * @param name  Document content
         */
        void                                setContent( const QByteArray& content ) { _content = content; }

        /**
         * @brief Get the document ETag.
         * @return Document ETag
         */
        const QString&                      getETag() const { return _eTag; }

        /**
         * @brief Set the document ETag.
         *
         * @param name  Document ETag
         */
        void                                setETag( const QString& etag ) { _eTag = etag; }

        /**
         * @brief Create a JSON string out of the document.
         *
         * @return JSON formatted string representing the document
         */
        QString                             toJSON();

        /**
         * @brief Setup the document given a JSON formatted string.
         *
         * @param input Input string in JSON format
         * @return Return false if the input was not in proper format.
         */
        bool                                fromJSON( const QString& input );

        /**
         * @brief Setup the document given a JSON document.
         *
         * @param input Input in JSON document
         * @return Return false if the input was not in proper format.
         */
        bool                                fromJSON( const QJsonDocument& input );

        /**
         * @brief Comparison operator which considers the document ID.
         *
         * @param right     Right hand of operation.
         * @return Return true if both documents have the same ID, otherwise false.
         */
        bool                                operator == ( const ModelDocument& right ) { return _id == right.getId(); }

        /**
         * @brief Unequal operator which considers the document ID.
         *
         * @param right     Right hand of operation.
         * @return true if both documents have the same ID, otherwise false.
         */
        bool                                operator != ( const ModelDocument& right ) { return _id != right.getId(); }

        /**
         * @brief Try to extract image data out of the document content. The image is expected to have a Data-URL header such as:
         *         "data:image/png;base64".
         *
         * @param data      Binary image data if extraction was successful
         * @param format    Image format such as 'png' or 'jpg'
         * @return          Return true if the content data was an image, otherwise false.
         */
        bool                                extractImageData( QByteArray& data, QString& format );

    protected:

        /**
         * @brief Try to extract the header info from given content. The content is expected to have
         *         a leading "Data URL".
         *
         * @param content        The content to check
         * @param mimeType       MIME type, e.g. image
         * @param format         Data format, e.g. png in the case of image
         * @param encoding       Data encoding, e.g. base64
         * @param headerLength   Length of header
         * @return               Return true if the given content is encoded with a leading Data URL
         */
        bool                                getHeaderInfo( QByteArray& content, QString& mimeType, QString& format, QString& encoding, int& headerLength );

        QString                             _id;
        QString                             _name;
        QString                             _type;
        QString                             _encoding;
        QByteArray                          _content;
        QString                             _eTag;
};

typedef m4e::core::SmartPtr< ModelDocument > ModelDocumentPtr;

} // namespace doc
} // namespace m4e

Q_DECLARE_METATYPE( m4e::doc::ModelDocumentPtr )

#endif // MODELDOCUMENT_H
