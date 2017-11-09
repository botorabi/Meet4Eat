/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */


#ifndef CORE_H
#define CORE_H

#include <QObject>


class QApplication;

namespace m4e
{

namespace gui {
  class MainWindow;
}

namespace core
{

class SingleProc;

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

        bool                        checkOrSetupSingleProc( QObject* p_notifyObject );

        QApplication*               _p_app          = nullptr;

        m4e::gui::MainWindow*       _p_mainWindow   = nullptr;

        SingleProc*                 _p_singleProc   = nullptr;
};

} // namespace core
} // namespace m4e

#endif // CORE_H
