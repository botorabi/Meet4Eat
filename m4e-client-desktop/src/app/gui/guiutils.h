/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

#ifndef GUIUTILS_H
#define GUIUTILS_H

#include <configuration.h>
#include <data/modeldocument.h>
#include <data/webapp.h>
#include <QPixmap>


namespace m4e
{
namespace ui
{

/**
 * @brief A collection of UI utilities
 *
 * @author boto
 * @date Sep 19, 2017
 */
class GuiUtils
{
    public:

        /**
         * @brief Given an input image, create a round icon out of it.
         *
         * @param input Input image
         * @return      Round icon
         */
        static QPixmap      createRoundIcon( const QPixmap& input );

        /**
         * @brief createRoundIcon
         * @brief Given an image document, create a round icon out of it.
         *
         * @param input Input image document
         * @return      Round icon
         */
        static QPixmap      createRoundIcon( data::ModelDocumentPtr input );

        /**
         * @brief Check if the given owner ID matches to currently authorized user's ID.
         *
         * @param ownerId   Owner ID to check
         * @param p_webApp  Web app interface containing information about the authorized user
         * @return          Return true if user is the owner, otherwise false.
         */
        static bool         userIsOwner( const QString& ownerId, data::WebApp* p_webApp );
};

} // namespace ui
} // namespace m4e

#endif // GUIUTILS_H
