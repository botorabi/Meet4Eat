/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#include <configuration.h>
#include "modelrequpdateinfo.h"
#include <QJsonDocument>
#include <QJsonObject>


namespace m4e
{
namespace update
{

QJsonDocument ModelRequestUpdateInfo::toJSON()
{
    QJsonObject obj;
    obj.insert( "name", getName() );
    obj.insert( "os", getOS() );
    obj.insert( "flavor", getFlavor() );
    obj.insert( "version", getVersion() );

    QJsonDocument doc( obj );
    return doc;
}

} // namespace update
} // namespace m4e
