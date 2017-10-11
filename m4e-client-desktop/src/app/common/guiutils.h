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
#include <document/modeldocument.h>
#include <webapp/webapp.h>
#include <QPixmap>
#include <QWidget>


namespace m4e
{
namespace common
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
        static QPixmap      createRoundIcon( doc::ModelDocumentPtr input );

        /**
         * @brief Create and assign a ShadowEffect to given widget.
         *
         * @param p_widget Widget getting the effect
         * @param color    Shadow color
         * @param offset   Offset
         * @param blurr    Blurr radius
         */
        static void         createShadowEffect( QWidget* p_widget,
                                                const QColor& color = QColor( 100, 100, 100, 180 ),
                                                const QPoint& offset = QPoint( -2, 2 ),
                                                int blurr = 6 );

        /**
         * @brief Check if the given owner ID matches to currently authorized user's ID.
         *
         * @param ownerId   Owner ID to check
         * @param p_webApp  Web app interface containing information about the authorized user
         * @return          Return true if user is the owner, otherwise false.
         */
        static bool         userIsOwner( const QString& ownerId, webapp::WebApp* p_webApp );
};

} // namespace common
} // namespace m4e

#endif // GUIUTILS_H
