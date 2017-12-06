/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#ifndef UPDATECHECK_H
#define UPDATECHECK_H

#include <configuration.h>
#include <webapp/request/rest-updatecheck.h>
#include <update/modelupdateinfo.h>
#include <QObject>


namespace m4e
{
namespace update
{

/**
 * @brief This class provides client update check.
 *
 * @author boto
 * @date Dec 6, 2017
 */
class UpdateCheck : public QObject
{
    /**
     * @brief TAG Used for logging
     */
    const std::string TAG = "(UpdateCheck) ";

    Q_OBJECT

    public:

        /**
         * @brief Construct an update check instance.
         *
         * @param p_parent Parent object
         */
        explicit                            UpdateCheck( QObject* p_parent );

        /**
         * @brief Destruct the instance
         */
        virtual                             ~UpdateCheck();

        /**
         * @brief Set webapp server's URL including port number. Set this URL before using any services below.
         *
         * @param url Server's URL
         */
        void                                setServerURL( const QString& url );

        /**
         * @brief Get webapp's server URL.
         *
         * @return Server URL
         */
        const QString&                      getServerURL() const;

        /**
         * @brief Get the error which occurred while the last request. Use this if a response delivers a 'success' set to false.
         *
         * @return Last error
         */
        const QString&                      getLastError() const { return _lastError; }

        /**
         * @brief Get the error code set by REST response, see getLastError above.
         *
         * @return Last error code
         */
        const QString&                      getLastErrorCode() const { return _lastErrorCode; }

        /**
         * @brief Get the last update information. Consider to request it before via 'requestGetUpdateInfo'.
         *
         * @return User events
         */
        update::ModelUpdateInfoPtr          getUpdateInfo();

        /**
         * @brief Request for getting client update information 'onResponseGetUpdateInfo'.
         */
        void                                requestGetUpdateInfo();

    signals:

        /**
         * @brief Get the results of update check iformation.
         *
         * @param success       true if the update information could successfully be retrieved, otherwise false
         * @param updateInfo    The update check information
         */
        void                                onResponseGetUpdateInfo( bool success, m4e::update::ModelUpdateInfoPtr updateInfo );

    protected slots:

        /**
         * @brief Signal is received when the results of getEvents arrive.
         *
         * @param events    User events
         */
        void                                onRESTUpdatetGetInfo( m4e::update::ModelUpdateInfoPtr updateInfo );

        /**
         * @brief Signal is received when there were a problem communicating to server or the results status were not ok.
         *
         * @param errorCode Error code if any exits
         * @param reason    Error string
         */
        void                                onRESTUpdateErrorGetInfo( QString errorCode, QString reason );

    protected:

        void                                setLastError( const QString& error ="", const QString& errorCode ="" );

        webapp::RESTUpdateCheck*            _p_restUpdateCheck = nullptr;

        m4e::update::ModelUpdateInfoPtr     _updateInfo;

        QString                             _lastError;

        QString                             _lastErrorCode;
};

} // namespace update
} // namespace m4e

#endif // UPDATECHECK_H
