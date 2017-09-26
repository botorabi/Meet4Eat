/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#ifndef REST_DOCUMENT_H
#define REST_DOCUMENT_H

#include <configuration.h>
#include <webapp/m4e-api/m4e-rest.h>
#include <data/modeldocument.h>


namespace m4e
{
namespace webapp
{

/**
 * @brief Class handling the document related web app interaction
 *
 * @author boto
 * @date Sep 19, 2017
 */
class RESTDocument : public Meet4EatREST
{
    /**
     * @brief TAG Used for logging
     */
    const std::string TAG = "(RESTDocument) ";

    Q_OBJECT

    public:

        /**
         * @brief Construct an instance.
         *
         * @param p_parent Parent instance
         */
        explicit                RESTDocument( QObject* p_parent );

        /**
         * @brief Destroy instance.
         */
        virtual                 ~RESTDocument();

        /**
         * @brief Get a document with given ID. The results are emitted by signal 'onRESTDocumentGet'.
         *
         * @param documentId  Document ID
         */
        void                    getDocument( const QString& documentId );

    signals:

        /**
         * @brief Emit the results of getDocument request.
         *
         * @param document  Document
         */
        void                    onRESTDocumentGet( m4e::data::ModelDocumentPtr document );

        /**
         * @brief Signal is emitted when there were a problem communicating to server or the results status were not ok.
         *
         * @param errorCode Error code if any exits
         * @param reason    Error string
         */
        void                    onRESTDocumentErrorGet( QString errorCode, QString reason );
};

} // namespace webapp
} // namespace m4e

#endif // REST_DOCUMENT_H
