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

#ifndef _UTILS_H_
#define _UTILS_H_

#include <string>
#include <vector>

namespace m4e
{
namespace core
{

//! Sleep for given time (milliseconds)
void milliSleep( unsigned int t );

//! Returns a string with current date
std::string getFormatedDate();

//! Returns a string with current time
std::string getFormatedTime();

//! Returns a string with current date and time
std::string getFormatedDateAndTime();

//! Emplode a given std string into vector elements, borrowed from evoluioN engine
std::string::size_type explode( const std::string& str, const std::string& separators, std::vector< std::string >* p_result );

//! Given a full path this function extracts the path cutting away the file name
std::string extractPath( const std::string& fullpath );

//! Given a full path this function extracts the file name
std::string extractFileName( const std::string& fullpath );

//! Given a path this functions replaces the backslashes by slashes
std::string cleanPath( const std::string& path );

//! Convert the string to lower-case.
std::string tolower( const std::string& str );

//! Convert the string to upper-case.
std::string toupper( const std::string& str );

} // namespace core
} // namespace m4e

#endif /* _UTILS_H_ */

