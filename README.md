    Copyright (c) 2017-2018 by Botorabi. All rights reserved.
    https://github.com/botorabi/Meet4Eat

    License: MIT License (MIT)
    Read the LICENSE text in main directory for more details.

    Current Version:   0.9.2
    First Created:     August 2017
    Author:            Botorabi (botorabi AT gmx DOT net)

[![Maintainability](https://api.codeclimate.com/v1/badges/ec89d87586154de2cbe9/maintainability)](https://codeclimate.com/github/botorabi/Meet4Eat/maintainability)
[![Test Coverage](https://api.codeclimate.com/v1/badges/ec89d87586154de2cbe9/test_coverage)](https://codeclimate.com/github/botorabi/Meet4Eat/test_coverage)
[![Build Status](https://travis-ci.org/botorabi/Meet4Eat.svg?branch=feature%2Fcdi)](https://travis-ci.org/botorabi/Meet4Eat)

# Meet4Eat

**Meet4Eat** is a social collaboration system providing an easy way to communicate with your colleagues and friends to make an appointment for a social event such as meeting for daily lunch.

For more details visit the [Meet4Eat website https://m4e.org](https://m4e.org) 

# Meet4Eat - A Cloud Application

The system consists of two major parts: a server providing proper web services and a client for user interaction.


# Web Services? Tel me more!

The web services are implemented by using bare-bone Java EE providing RESTful services. They provide necessary functionality such as

 - User authentication
 
 - Resource access control considering various user authorization roles
 
 - Application business logic
 
 - Support for automated migration tasks during an application update

 
An administration panel provides a convenient user interface for system monitoring and trouble shooting.


# What about the User Interaction

The user interacts with the system by the means of an application client. For desktops a Qt based application is unter development. Later, native apps for smartphones and a browser based solution may follow.
All clients have the same REST interface for interacting with the system, so it is simple to develop third party applications for **Meet4Eat**.
