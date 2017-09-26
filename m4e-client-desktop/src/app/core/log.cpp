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

#include "log.h"
#include "utils.h"

#include <cassert>
#include <fstream>

namespace m4e
{
namespace core
{

//! This is the default system log instance
Log defaultlog;


//! Implementation of logging system
Log::Log() :
 std::basic_ostream< char >( &_stream ),
 _severity( L_DEBUG ),
 _printSeverityLevel( true ),
 _enableTimeStamp( true )
{
    _stream.setLog( this );
}

Log::~Log()
{
    reset();
}

bool Log::addSink( const std::string& sinkname, const std::string& filename, unsigned int loglevel )
{
    if( loglevel > L_INFO || loglevel < L_VERBOSE )
    {
        log_error << "invalid log level for given sink '"  << sinkname <<  "'" << std::endl;
        return false;
    }

    // check if there is already a sink with requested sink name
    std::vector< Sink* >::iterator p_sink = _sinks.begin(), p_sinkEnd = _sinks.end();
    for ( ; p_sink != p_sinkEnd; p_sink++ )
    {
        if ( ( *p_sink )->_name == sinkname )
        {
            log_warning << "sink name '" << sinkname << "' already exists!" << std::endl;
            return false;
        }
    }

    std::fstream* p_stream = new std::fstream;
    p_stream->open( filename.c_str(), std::ios_base::binary | std::ios_base::out );
    if ( !*p_stream )
    {   
        delete p_stream;
        log_error << "cannot open log file '" << filename << "' for sink '" << sinkname << "'" << std::endl;
        return false;

    }
    else
    {
        Sink* p_s = new Sink( sinkname, p_stream, loglevel );
        _sinks.push_back( p_s );
    }

    return true;
}

bool Log::addSink( const std::string& sinkname, std::ostream& sink, unsigned int loglevel )
{
    if( loglevel > L_INFO || loglevel < L_VERBOSE )
    {
        log_error << "invalid log level for given sink '" << sinkname <<  "'" << std::endl;
        return false;
    }
    
    // check if there is already a sink with requested sink name
    std::vector< Sink* >::iterator p_beg = _sinks.begin(), p_end = _sinks.end();
    for ( ; p_beg != p_end; ++p_beg )
    {
        if ( ( *p_beg )->_name == sinkname )
        {
            log_warning << "sink name '" << sinkname << "' already exists!"  << std::endl;
            return false;
        }
    }

    Sink* p_sink = new Sink( sinkname, &sink, loglevel, true );
    _sinks.push_back( p_sink );

    return true;
}

void Log::removeSink( const std::string& sinkname )
{
    std::vector< Sink* >::iterator p_sink = _sinks.begin(), p_sinkEnd = _sinks.end();
    for ( ; p_sink != p_sinkEnd; ++p_sink )
    {
        if ( ( *p_sink )->_name == sinkname )
        {
            delete *p_sink;
            _sinks.erase( p_sink );
            return;
        }
    }

    assert( false && "sink name does not exist!" );
}

void Log::setSeverity( unsigned int severity )
{
    _severity = severity;
}

void Log::enableSeverityLevelPrinting( bool en )
{
    _printSeverityLevel = en;
}

void Log::enableTimeStamp( bool en )
{
    _enableTimeStamp = en;
}

void Log::reset()
{
    // delete all allocated streams, except the std streams
    std::vector< Sink* >::iterator p_sink = _sinks.begin(), p_sinkEnd = _sinks.end();
    for ( ; p_sink != p_sinkEnd; p_sink++ )
        delete *p_sink;

    _sinks.clear();
}

void Log::out( const std::string& msg )
{
    std::vector< Sink* >::iterator p_sink = _sinks.begin(), p_sinkEnd = _sinks.end();
    for ( ; p_sink != p_sinkEnd; p_sink++ )
    {
        if ( ( *p_sink )->_logLevel <= _severity )
        {
            *( ( *p_sink )->_p_stream ) << msg;
            ( ( *p_sink )->_p_stream )->flush();
        }
    }
}

//---------------------------
Log::LogStreamBuf::LogStreamBuf() :
std::basic_streambuf< char >(),
_p_log ( NULL )
{
}

Log::LogStreamBuf::~LogStreamBuf()
{
}

void Log::LogStreamBuf::setLog( Log *p_log )
{
    _p_log = p_log;
}

std::basic_ios< char >::int_type Log::LogStreamBuf::overflow( int_type c )
{
    if( !std::char_traits< char >::eq_int_type( c, std::char_traits< char >::eof() ) )
    {
        _msg += static_cast< char >( c );
        if( c == '\n' )
        {
            std::string severity;
            if ( _p_log->_printSeverityLevel )
            {
                switch ( _p_log->_severity )
                {
                case L_INFO:
                    severity = "[info]    ";
                    break;
                case L_DEBUG:
                    severity = "[debug]   ";
                    break;
                case L_WARNING:
                    severity = "[warning] ";
                    break;
                case L_ERROR:
                    severity = "[error]   ";
                    break;
                case L_VERBOSE:
                    severity = "[verbose] ";
                    break;
                default:
                    assert( false && "unknown log severity" );
                }
            }

            // add a carriage return to end of line
            _msg[ _msg.length() - 1 ] = '\r';
            _msg += "\n";

            if ( _p_log->_enableTimeStamp )
                _p_log->out( "[" + getFormatedTime() + "] " + severity + _msg );
            else
                _p_log->out( severity + _msg );

            _msg = "";
        }
    }
    return std::char_traits< char >::not_eof( c );
}

std::ostream& Log::operator << ( const Log::LogLevel& ll )
{
    setSeverity( ll._level );
    return *this;
}

} // namespace core
} // namespace m4e


