/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */


#ifndef CORE_H
#define CORE_H


class QApplication;

namespace m4e
{

namespace ui {
  class MainWindow;
}

namespace core
{

/**
 * @brief The application core functionality is implemented in this class.
 *
 * @author boto
 * @date Aug 2, 2017
 */
class Core
{
    public:

                                    Core();

        virtual                     ~Core();

        void                        initialize( int &argc, char* argv[] );

        void                        start();

        void                        shutdown();

    protected:

        QApplication*               _p_app          = nullptr;

        m4e::ui::MainWindow*        _p_mainWindow   = nullptr;
};

} // namespace core
} // namespace m4e

#endif // CORE_H
