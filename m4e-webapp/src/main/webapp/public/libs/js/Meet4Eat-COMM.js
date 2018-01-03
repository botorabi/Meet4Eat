/**
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
"use strict";

/**
 * Communication channel to Meet4Eat app server basing on a
 * WebSocket connection
 * 
 * @author boto
 * Date of creation Oct 6, 2017
 */
function Meet4EatCOMM() {

	/* self ref */
	var self = this;

	/* API version */
	self._version = "1.0.0";

	/* Server's base URL */
	self._baseUrl = "/m4e-webapp";

	/* WebSocket URL */
	self._wsUrl = "/ws";

	/* WebSocket connection */
	self._connection = null;

	/**
	 * Get the WebSocket interface version.
	 * 
	 * @returns {string} Version of WebSocket interface.
	 */
	self.getVersion = function() {
		return self._version;
	};

	/**
	 * Try to connect the app server. The passed callback object has the following optional fields:
	 * 
	 *   {
	 *		function onOpen()
	 *		function onError(error)
	 *		function onMessage(message object)
	 *		function onClose(response)
	 *	 }
	 * 
	 * @param {function} resultsCallback  Callback which is used when the results arrive.
	 */
	self.connect = function(resultsCallback) {
		// first close any open connection
		self.close();
		var connection = new WebSocket(self._getWsURL());
		self._connection = connection;

		if (resultsCallback && resultsCallback.onOpen) {
			connection.onopen = resultsCallback.onOpen;
		}
		if (resultsCallback && resultsCallback.onError) {
			connection.onerror = resultsCallback.onError;
		}
		if (resultsCallback && resultsCallback.onMessage) {
			connection.onmessage = function(msg) {
				resultsCallback.onMessage(self._processMessage(msg.data));
			};
		}
		if (resultsCallback && resultsCallback.onClose) {
			connection.onclose = resultsCallback.onClose;
		}
	};

	/**
	 * Close the connection.
	 */
	self.close = function() {
		if (self._connection) {
			self._connection.close();
		}
		self._connection = null;
	};

	/**
	 * Send a message to server. Create a connection before.
	 * 
	 * @param {number} source  User ID of this message sender
	 * @param {string} channel Communication channel such as 'notify', 'chat', etc.
	 * @param {object} msg	   Message fields to send
	 */
	self.send = function(source, channel, msg) {
		if (!self._connection) {
			return;
		}
		var fields = {
			'source' : source,
			'channel' : channel,
			'time' : (new Date()).getTime(),
			'data' : msg
		};
		self._connection.send(JSON.stringify(fields));
	};

	//##########################################################################
	//#                         Private functions                              #
	//##########################################################################

	/**
	 * Parse and create an object representing the received message in JSON format.
	 * 
	 * @param {string} msg	Message string received from server, JSON format
	 * @returns {object}	Fields parsed from received JSON string
	 */
	self._processMessage = function(msg) {
		var fields = JSON.parse(msg);
		if (fields.data) {
			fields.data = JSON.parse(fields.data);
		}
		return fields;
	};

	/**
	 * Create the WebSocket URL basing on current http(s) URL.
	 * 
	 * @returns WebSocket URL
	 */
	self._getWsURL = function() {
		var ref = window.location.href;
		var index = ref.indexOf(self._baseUrl);
		if (index < 0) {
			return "";
		}
		var url = ref.substr(0, index + self._baseUrl.length);
		url = url.replace("http", "ws");
		url += self._wsUrl;
		return url;
	};
}
