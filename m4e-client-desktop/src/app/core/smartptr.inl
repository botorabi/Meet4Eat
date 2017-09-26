/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

/*
  Simple non-threadsafe, ref-counted smart pointer.
  This code is basing on the open-source project yag2002.sf.net
*/

#ifndef _SMARTPTR_H_
#error "don't include this file directly, include smartptr.h instead!"
#endif

template< class Type >
SmartPtr< Type >::SmartPtr() :
 _p_ref( nullptr )
{
}

template< class Type >
SmartPtr< Type >::SmartPtr( const SmartPtr& copy ) :
 _p_ref( copy.getRef() )
{
    if ( _p_ref )
        _p_ref->increaseRefCnt();
}

template< class Type >
SmartPtr< Type >::SmartPtr( Type* p_ref ) :
 _p_ref( p_ref )
{
    if ( _p_ref )
        _p_ref->increaseRefCnt();
}

template< class Type >
SmartPtr< Type >::~SmartPtr()
{
    if ( _p_ref )
    {
        _p_ref->decreaseRefCnt();
        if ( !_p_ref->getCount() )
        {
            delete _p_ref;
            _p_ref = nullptr;
        }
    }
}

template< class Type >
Type* SmartPtr< Type >::getRef() const
{
    return _p_ref;
}

template< class Type >
bool SmartPtr< Type >::valid() const
{
    return ( _p_ref != nullptr );
}

template< class Type >
Type& SmartPtr< Type >::operator * ()
{
    return *_p_ref;
}

template< class Type >
Type& SmartPtr< Type >::operator * () const
{
    return *_p_ref;
}

template< class Type >
Type* SmartPtr< Type >::operator -> ()
{
    return _p_ref;
}

template< class Type >
Type* SmartPtr< Type >::operator -> () const
{
    return _p_ref;
}

template< class Type >
SmartPtr< Type >& SmartPtr< Type >::operator = ( Type* p_right )
{
    if ( _p_ref == p_right )
        return *this;

    // check for nullptr assignment
    if ( !p_right )
    {
        if ( _p_ref )
        {
            _p_ref->decreaseRefCnt();
            if ( !_p_ref->getCount() )
            {
                delete _p_ref;
                _p_ref = nullptr;
            }
            _p_ref = p_right;
        }
    }
    else
    {
        if ( _p_ref )
        {
            _p_ref->decreaseRefCnt();
            if ( !_p_ref->getCount() )
            {
                delete _p_ref;
                _p_ref = nullptr;
            }
        }

        _p_ref = p_right;
        _p_ref->increaseRefCnt();
    }

    return *this;
}

template< class Type >
SmartPtr< Type >& SmartPtr< Type >::operator = ( const SmartPtr< Type >& right )
{
    // check self assignment
    if ( _p_ref == right.getRef() )
        return *this;

    if ( _p_ref )
    {
        _p_ref->decreaseRefCnt();
        if ( !_p_ref->getCount() )
        {
            delete _p_ref;
            _p_ref = nullptr;
        }
    }

    if ( right.getRef() )
    {
        right->increaseRefCnt();
        _p_ref = right.getRef();
    }

    return *this;
}

template< class Type >
bool SmartPtr< Type >::operator == ( const SmartPtr& right )
{
    return *_p_ref == *( right._p_ref );
}

template< class Type >
bool SmartPtr< Type >::operator != ( const SmartPtr& right )
{
    return *_p_ref != *( right._p_ref );
}
