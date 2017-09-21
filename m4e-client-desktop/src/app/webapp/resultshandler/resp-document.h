/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#ifndef RESP_DOCUMENT_H
#define RESP_DOCUMENT_H

#include <configuration.h>
#include <webapp/m4e-api/m4e-response.h>
#include <QJsonDocument>


namespace m4e
{
namespace webapp
{

class RESTDocument;

/**
 * @brief Response handler for GetDocument
 *
 * @author boto
 * @date Sep 19, 2017
 */
class ResponseGetDocument: public Meet4EatRESTResponse
{
    public:

        explicit        ResponseGetDocument( RESTDocument* p_requester );

        void            onRESTResponseSuccess( const QJsonDocument& results );

        void            onRESTResponseError( const QString& reason );

    protected:

        RESTDocument*   _p_requester;
};

} // namespace webapp
} // namespace m4e

#endif // RESP_DOCUMENT_H
