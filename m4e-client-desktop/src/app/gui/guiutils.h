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
         * @param input Input image
         * @return      Round icon
         */
        static QPixmap      createRoundIcon( const QPixmap& input );
};

} // namespace ui
} // namespace m4e

#endif // GUIUTILS_H
