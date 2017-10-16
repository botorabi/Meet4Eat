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
#define _SMARTPTR_H_


/**
 * Convenient macro for setting up the smart pointer access, place it into derived class.
 * Consider also to use the macro SMARTPTR_DEFAULTS below.
 */
#define DECLARE_SMARTPTR_ACCESS( classname )    friend class m4e::core::SmartPtr< classname >; \
  protected: \
    /* Omit copy construction*/ \
    classname( const classname& );


/**
 * Declare the common things on a smart pointer class. Take care that this macro also
 * defines the destructor with an emptry body.
 */
#define SMARTPTR_DEFAULTS( classname )  DECLARE_SMARTPTR_ACCESS( classname ) \
 protected: \
    virtual ~classname() {} \
    /* Omit assignment operator */ \
    classname& operator = ( const classname& ref );


namespace m4e
{
namespace core
{

//! Smart pointer base class, derive from this class for implementing concrete smart pointers.
template< class Type >
class SmartPtr
{
    public:

                                             SmartPtr();

                                             SmartPtr( const SmartPtr& copy );

                                             SmartPtr( Type* p_ref );

        virtual                              ~SmartPtr();

        //! Return the reference pointer with ref count modifications
        Type*                                getRef() const;

        //! Return true if the smart pointer contains an object.
        bool                                 valid() const;

        //! Pointer access operator
        Type*                                operator -> ();

        //! Pointer access operator
        Type*                                operator -> () const;

        //! Dereference operator
        Type&                                operator * ();

        //! Dereference operator
        Type&                                operator * () const;

        //! Assigment operator for ref type pointer to smart pointer
        SmartPtr&                           operator = ( Type* p_right );

        //! Assigment operator for smart pointer to smart pointer
        SmartPtr&                           operator = ( const SmartPtr& right );

        //! Comparison operator "equal" which delegates the actual comparison to the hosted references.
        bool                                operator == ( const SmartPtr& right );

        //! Comparison operator "unequal" which delegates the actual comparison to the hosted references.
        bool                                operator != ( const SmartPtr& right );

    protected:

        Type*                               _p_ref;
};

//! Ref counter class used by classes as base which derive from SmartPtr
template< class Type >
class RefCount
{
    public:

                                            RefCount() :
                                             _refCount( 0 )
                                            {
                                            }

    protected:

        virtual                             ~RefCount() {}

        unsigned int                        getCount() const { return _refCount; }

        void                                increaseRefCnt() { ++_refCount; }

        void                                decreaseRefCnt() { --_refCount; }

        unsigned int                        _refCount;

    friend class SmartPtr< Type >;
};

#include "smartptr.inl"

} // namespace core
} // namespace m4e

#endif // _SMARTPTR_H_
