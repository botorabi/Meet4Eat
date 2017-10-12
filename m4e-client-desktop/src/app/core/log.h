/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

/*
  General purpose log system
  This code is basing on the open-source project yag2002.sf.net
*/

#ifndef _LOG_H_
#define _LOG_H_

#include <iostream>
#include <ostream>
#include <vector>
#include <QString>

// convenient macros for logging
#define log_out         m4e::core::defaultlog
#define log_verbose     m4e::core::defaultlog << m4e::core::Log::LogLevel( m4e::core::Log::L_VERBOSE )
#define log_debug       m4e::core::defaultlog << m4e::core::Log::LogLevel( m4e::core::Log::L_DEBUG   )
#define log_error       m4e::core::defaultlog << m4e::core::Log::LogLevel( m4e::core::Log::L_ERROR   )
#define log_warning     m4e::core::defaultlog << m4e::core::Log::LogLevel( m4e::core::Log::L_WARNING )
#define log_info        m4e::core::defaultlog << m4e::core::Log::LogLevel( m4e::core::Log::L_INFO    )


namespace m4e
{
namespace core
{

class Log;

//! This is the default system log instance
/** Usage:
*            log_out << Log::LogLevel( Log::L_INFO ) << " this is an info log" << endl;
*            log_out << " number: " << mynum << " string: " << mystring << endl;
*/
extern Log defaultlog;

//! Class for logging messages to defined sinks
class Log : public std::basic_ostream< char >
{
    public:

        //! Logging thresholds
        enum Level 
        {
            L_VERBOSE = 0x1,
            L_DEBUG   = 0x2,
            L_ERROR   = 0x3,
            L_WARNING = 0x4,
            L_INFO    = 0x5
        };

                                                    Log();

        virtual                                     ~Log();

        //! Add a file sink
        bool                                        addSink( const std::string& sinkname, const std::string& filename, unsigned int loglevel = Log::L_DEBUG );

        //! Add standard sink such as cout
        bool                                        addSink( const std::string& sinkname, std::ostream& sink = std::cout, unsigned int loglevel = Log::L_DEBUG );

        //! Remove a sink given its name
        void                                        removeSink( const std::string& sinkname );

        //! Output a message on sinks with given severity
        void                                        out( const std::string& msg );

        //! Set current message severity
        void                                        setSeverity( unsigned int severity );

        //! Enables/disables severity level printing in output, the default is 'enabled'.
        void                                        enableSeverityLevelPrinting( bool en );

        //! Enable/disable timestamps, the default is 'enabled'.
        void                                        enableTimeStamp( bool en );

        //! Reset the log system, all registered sinks are removed.
        void                                        reset();

        //! Struct for setting new loglevel via stream operator <<
        struct LogLevel
        {
                explicit LogLevel( Level threshold ) : _level( threshold ) {}

                Level     _level;
        };

        //! Stream operator for setting current logging severity
        std::ostream&                               operator << ( const Log::LogLevel& ll );

    protected:

        //! Avoid assignment operator
        Log&                                        operator = ( const Log& l );

        //! Avoid copy constructor
                                                    Log( const Log& );

        //! Currently set severity
        unsigned int                                _severity;

        //! Severity level printing 
        bool                                        _printSeverityLevel;

        //! Time stamp enable/disable flag
        bool                                        _enableTimeStamp;

        //! Log sink 
        class Sink
        {
            public:

                                                        Sink( const std::string& name, std::ostream* p_stream, unsigned int loglevel, bool stdstream = false ) :
                                                            _name( name ),
                                                            _p_stream( p_stream ), 
                                                            _logLevel( loglevel ),
                                                            _stdstream( stdstream )
                                                        {}
                                                        
            virtual                                     ~Sink() { if ( _p_stream && !_stdstream ) delete _p_stream; }

            std::string                                 _name;

            std::ostream*                               _p_stream;

            unsigned int                                _logLevel;

            bool                                        _stdstream;
        };

        std::vector< Sink* >                        _sinks;

        //! Stream buffer class
        class LogStreamBuf : public std::basic_streambuf< char >
        {
            public:

                                                        LogStreamBuf();

                virtual                                 ~LogStreamBuf();

                void                                    setLog( Log *p_log );

            protected:

                virtual int_type                        overflow( int_type c );

                Log*                                    _p_log;

                std::string                             _msg;


        }                                               _stream;
};

} // namespace core
} // namespace m4e

//! Accept QString for log stream
std::ostream& operator << ( std::ostream& stream, const QString& msg );

#endif //_LOG_H_

