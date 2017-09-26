/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */


/*
 Utility functions
*/

#ifdef _WIN32
 #define  _CRT_SECURE_NO_WARNINGS
#endif

#include "utils.h"

#include <algorithm>
#include <chrono>
#include <ctime>
#include <iomanip>
#include <sstream>
#include <string>
#include <thread>


namespace m4e
{
namespace core
{

//! Sleep for given time (milliseconds)
void milliSleep( unsigned int t )
{
    std::this_thread::sleep_for( std::chrono::milliseconds( t ) );
}

std::string getFormatedDate()
{
    char p_buf[ 128 ];
    time_t t = time( nullptr );
    std::strftime( p_buf, sizeof( p_buf ), "%Y-%m-%d", localtime( &t ) );
    return std::string( p_buf );
}

std::string getFormatedTime()
{
    char p_buf[ 128 ];
    time_t t = time( nullptr );
    std::strftime( p_buf, sizeof( p_buf ), "%H:%M:%S", localtime( &t ) );
    return std::string( p_buf );
}

std::string getFormatedDateAndTime()
{
    char p_buf[ 128 ];
    time_t t = time( nullptr );
    std::strftime( p_buf, sizeof( p_buf ), "%Y-%m-%d %H:%M:%S", localtime( &t ) );
    return std::string( p_buf );
}

std::string::size_type explode( const std::string& str, const std::string& separators, std::vector< std::string >* p_result )
{
    std::string::size_type len = str.length();
    if( !len )
        return 0;

    if( !separators.length() )
    {
        p_result->push_back( str );
        return 1;
    }

    std::string::size_type token = 0;
    std::string::size_type end   = 0;
    unsigned int      org   = static_cast< unsigned int >( p_result->size() );

    while( end < len )
    {
        token = str.find_first_not_of( separators, end );

        // handle the special case that there is nothing between separators, put an empty string in this case
        if ( ( token - end ) == 2 )
        {
            p_result->push_back( "" );
            end = str.find_first_of( separators, token );
            p_result->push_back( str.substr( token, ( end != std::string::npos ) ? ( end - token ) : std::string::npos ) );
            continue;
        }

        end = str.find_first_of( separators, token );
        if( token != std::string::npos )
            p_result->push_back( str.substr( token, ( end != std::string::npos ) ? ( end - token ) : std::string::npos ) );
    }

    return( p_result->size() - org );
}

std::string extractPath( const std::string& fullpath )
{
    std::string res = fullpath;
    // first clean the path
    for ( std::string::iterator i = res.begin(), e = res.end(); i != e; ++i ) if ( *i == '\\') *i = '/';
    res = res.substr( 0, res.rfind( "/" ) );
    if ( !res.empty() )
        return res;

    return "";
}

std::string extractFileName( const std::string& fullpath )
{
    std::string res = fullpath;
    // first clean the path
    for ( std::string::iterator i = res.begin(), e = res.end(); i != e; ++i ) if ( *i == '\\') *i = '/';
    res = res.substr( res.rfind( "/" ) );
    if ( res[ 0 ] == '/' ) res.erase( 0, 1 ); // cut leading slash
    if ( !res.empty() )
        return res;

    return "";
}

std::string cleanPath( const std::string& path )
{
    // substitude backslashes by forwardslaches
    std::string cleanpath = path;
    for ( std::string::iterator i = cleanpath.begin(), e = cleanpath.end(); i != e; ++i )
        if ( *i == '\\') *i = '/';

    // resolve directory changes
    std::string::size_type pos = 0, posback = 0;
    do
    {
        std::string tmp( cleanpath );
        pos = tmp.find( "/../" );
        if ( ( pos != std::string::npos ) && ( pos > 1 ) )
        {
            posback = tmp.rfind( "/", pos - 1 );
            if ( posback != std::string::npos )
            {
                cleanpath = tmp.substr( 0, posback );
                cleanpath += tmp.substr( pos + 3 );
            }
            else
            {
                cleanpath = cleanpath.substr( pos + 4 );
                pos = std::string::npos;
            }
        }
    }
    while ( pos != std::string::npos );

    return cleanpath;
}

std::string tolower( const std::string& str )
{
    if ( !str.length() )
        return std::string( "" );

    std::string copy = str;
    std::transform( copy.begin(), copy.end(), copy.begin(), ::tolower );
    return copy;
}

std::string toupper( const std::string& str )
{
    if ( !str.length() )
        return std::string( "" );

    std::string copy = str;
    std::transform( copy.begin(), copy.end(), copy.begin(), ::toupper );
    return copy;
}

} // namespace core
} // namespace m4e
