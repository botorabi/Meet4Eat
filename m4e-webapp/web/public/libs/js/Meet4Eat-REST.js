/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
"use strict";

function Meet4EatREST() {
	/* self ref */
	var self = this;

	/* API version */
	this._version = "0.4.0";

	/* Root path of web service */
	this._webRoot = "/m4e-webapp";
	
	/* URL for accessing app information */
	this._urlAppInfo  = this._webRoot + '/webresources/rest/appinfo';

	/* URL for accessing maintenance information */
	this._urlMaintenance  = this._webRoot + '/webresources/rest/maintenance';

	/* URL for accessing user authentication */
	this._urlUserAuth  = this._webRoot + '/webresources/rest/authentication';

	/* URL for accessing users */
	this._urlUsers  = this._webRoot + '/webresources/rest/users';

	/* URL for accessing events */
	this._urlEvents = this._webRoot + '/webresources/rest/events';

	/* Last time the server was accessed */
	this._lastAccessTime = 0;

	/**
	 * Get the web interface version.
	 * @returns {string} Version of this web interface.
	 */
	this.getVersion = function() {
		return this._version;
	};

	/**
	 * Get server information
	 * 
	 * @param {function} resultsCallback  Callback which is used when the results arrive.
	 */
	this.getServerInfo = function(resultsCallback) {
		this._requestJSON(this._urlAppInfo, null, 'GET', resultsCallback);
	};

	/**
	 * Get the time of last server access.
	 * 
	 * @returns {integer}	Last access time in seconds
	 */
	this.getLastAccessTime = function() {
		return Math.floor(this._lastAccessTime / 1000);
	};

	/**
	 * Get past time since last server access.
	 * 
	 * @returns {integer}	Past time since last server access in seconds
	 */
	this.getPastAccessTime = function() {
		if (this._lastAccessTime === 0) {
			return 0;
		}
		return Math.floor(((new Date()).getTime() - this._lastAccessTime) / 1000);
	};

	/**
	 * Build a REST api for user authentication.
	 * 
	 * @returns {Meet4EatBaseREST}			REST API for user authentication
	 */
	this.buildUserAuthREST = function() {
		var inst = new Meet4EatAuth();
		inst.initialize(this._urlUserAuth, this._requestJSON);
		return inst;
	};

	/**
	 * Build a REST api for users operations.
	 * 
	 * @returns {Meet4EatBaseREST}			REST API for user operations
	 */
	this.buildUserREST = function() {
		var inst = new Meet4EatBaseREST();
		inst.initialize(this._urlUsers, this._requestJSON);
		return inst;
	};

	/**
	 * Build a REST api for event operations.
	 * 
	 * @returns {Meet4EatBaseREST}			REST API for event operations
	 */
	this.buildEventREST = function() {
		var base = new Meet4EatBaseREST();
		base.initialize(this._urlEvents, this._requestJSON);
		// extend the base REST module by Meet4EatEventREST
		var events = new Meet4EatEventREST(base);
		events.initialize();
		return base;
	};

	/**
	 * Build a REST api for maintenance operations.
	 * 
	 * @returns {Meet4EatMaintenanceREST}    REST API for maintenance operations
	 */
	this.buildMaintenanceREST = function() {
		var maintenance = new Meet4EatMaintenanceREST();
		maintenance.initialize(this._urlMaintenance, this._requestJSON);
		return maintenance;
	};

	/**
     * Asynchronous request which expects JSON as response.
     * 
     * @param requestUrl        Request URL
     * @param requestData       Request data in javascript object format
     * @param method            Request method
     * @param responseCallback  Callback for reponse notification.
     *                           The response is parsed as JSON and a corresponding
     *                           javascript object is used for the callback.
	 *                          The callback object is expected to be of following structure:
	 *                          
	 *                           callback {
	 *                             
	 *                             // object: parsed json response
	 *                             // response: original response from server
	 *                             success: function(object, response) {}
	 *                             
	 *                             // text: error text
	 *                             // response: original response from server
	 *                             error: function(text, response) {}
	 *                           }
     */
    this._requestJSON = function(requestUrl, requestData, method, responseCallback) {
		var json = requestData ? JSON.stringify(requestData) : null;
        $.ajax({
            type: method,
            url: requestUrl,
            data: json,
			contentType: "application/json; charset=UTF-8",
			dataType: "text",
			cache: false,
            success: function(response) {
				self._lastAccessTime = (new Date()).getTime();
                if (responseCallback !== null) {
                    var results = null;
                    try {
                        results = $.parseJSON(response);
						// the data is also expected to be in JSON format
						if (results.data) {
							results.data = $.parseJSON(results.data);
						}
                    }
                    catch(e) {
						if (responseCallback.error) {
							responseCallback.error("Exception occurred while parsing JSON response, reason: " + e, response);
						}
                    }
					if (responseCallback.success) {
						responseCallback.success(results, response);
					}
                }
            },
			error: function(jqXHR, errorString, errorThrown ) {
                if (responseCallback && responseCallback.error) {
					responseCallback.error("Problem while contacting '" + requestUrl + "' | Error string: " + errorString + " | Error thrown: " + errorThrown, jqXHR);
				}
			}
        });
    };
}

/**
 * Base REST services
 */
function Meet4EatBaseREST() {
	/* API version */
	this._version = "1.1.0";

	/* Root URL for REST requests */
	this._rootPath = "";

	/* Function for contacting the server via JSON */
	this._fcnRequestJson = null;

	/**
	 * Initialize the instance.
	 * 
	 * @param {string} rootPath			Root URL
	 * @param {string} fcnRequestJson	Function for contacting the server via JSON
	 */
	this.initialize = function (rootPath, fcnRequestJson) {
		this._rootPath = rootPath;
		this._fcnRequestJson = fcnRequestJson;
	};

	/**
	 * Request for getting the total entity count.
	 * 
	 * @param {function} resultsCallback  Callback which is used when the results arrive.
	 */
	this.getCount = function(resultsCallback) {
		this._fcnRequestJson(this._rootPath + '/count', null, 'GET', resultsCallback);
	};

	/**
	 * Request for getting all entities.
	 * 
	 * @param {function} resultsCallback  Callback which is used when the results arrive.
	 */
	this.getAll = function(resultsCallback) {
		this._fcnRequestJson(this._rootPath, null, 'GET', resultsCallback);
	};

	/**
	 * Request for getting an entity with given ID.
	 * 
	 * @param {function} resultsCallback  Callback which is used when the results arrive.
	 * @param {integer}  id               Entity ID
	 */
	this.find = function(resultsCallback, id) {
		this._fcnRequestJson(this._rootPath + "/" + id, null, 'GET', resultsCallback);
	};

	/**
	 * Search for a keyword in all entities and return a limited count of hits.
	 * 
	 * NOTE: Some entity types may not support the search service interface!
	 * 
	 * @param {function} resultsCallback  Callback which is used when the results arrive.
	 * @param {string} keyword	The keyword to search for
	 */
	this.search = function(resultsCallback, keyword) {
		this._fcnRequestJson(this._rootPath + "/search/" + keyword, null, 'GET', resultsCallback);		
	};

	/**
	 * Create a new entity or update an existing entity.
	 * 
	 * @param {function} resultsCallback   Callback which is used when the results arrive.
	 * @param {string}   id                Pass an empty string or null to create a new entity.
	 * @param {array}    fields            Entity fields
	 */
	this.createOrUpdate = function(resultsCallback, id, fields) {
		if (!id || id === "") {
			this._fcnRequestJson(this._rootPath + '/create', fields, 'POST', resultsCallback);
		}
		else {
			this._fcnRequestJson(this._rootPath + "/" + id, fields, 'PUT', resultsCallback);
		}
	};

	/**
	 * Delete the entity with given ID.
	 * 
	 * @param {function} resultsCallback   Callback which is used when the results arrive.
	 * @param {integer}  id                Entity ID
	 */
	this.delete = function(resultsCallback, id) {
		this._fcnRequestJson(this._rootPath + "/" + id, null, 'DELETE', resultsCallback);
	};
}

/**
 * REST services for events
 * 
 * @param {Meet4EatBaseREST}  base Base REST module
 */
function Meet4EatEventREST(base) {
	/* API version */
	this._version = "0.1.0";

	/* Base module */
	var _base = base;

	/**
	 * Initialize the instance.
	 */
	this.initialize = function () {};

	/**
	 * Request for adding a member to an event.
	 * 
	 * @param {function} resultsCallback   Callback which is used when the results arrive.
	 * @param {integer}  eventId           Event ID
	 * @param {integer}  memberId          Member ID
	 */
	_base.addEventMember = function(resultsCallback, eventId, memberId) {
		_base._fcnRequestJson(_base._rootPath + '/addmember/' + eventId + "/" + memberId, null, 'GET', resultsCallback);
	};

	/**
	 * Request for removing a member from an event.
	 * 
	 * @param {function} resultsCallback   Callback which is used when the results arrive.
	 * @param {integer}  eventId           Event ID
	 * @param {integer}  memberId          Member ID
	 */
	_base.removeEventMember = function(resultsCallback, eventId, memberId) {
		_base._fcnRequestJson(_base._rootPath + '/removemember/' + eventId + "/" + memberId, null, 'GET', resultsCallback);
	};
}

/**
 * Maintenance REST services
 */
function Meet4EatMaintenanceREST() {
	/* API version */
	this._version = "1.0.0";

	/* Root URL for REST requests */
	this._rootPath = "";

	/* Function for contacting the server via JSON */
	this._fcnRequestJson = null;

	/**
	 * Initialize the instance.
	 * 
	 * @param {string} rootPath			Root URL
	 * @param {string} fcnRequestJson	Function for contacting the server via JSON
	 */
	this.initialize = function (rootPath, fcnRequestJson) {
		this._rootPath = rootPath;
		this._fcnRequestJson = fcnRequestJson;
	};

	/**
	 * Get maintenance stats
	 * 
	 * @param {function} resultsCallback  Callback which is used when the results arrive.
	 */
	this.getMaintenanceStats = function(resultsCallback) {
		this._fcnRequestJson(this._rootPath + "/stats", null, 'GET', resultsCallback);
	};

	/**
	 * Purge all resources which are no longer needed.
	 * 
	 * @param {function} resultsCallback  Callback which is used when the results arrive.
	 */
	this.maintenancePurge = function(resultsCallback) {
		this._fcnRequestJson(this._rootPath + "/purge", null, 'GET', resultsCallback);
	};
}

/**
 * Authentication functions
 */
function Meet4EatAuth() {

	this._version = "1.0.0";

	/* Root URL for REST requests */
	this._rootPath = "";

	/* Function for contacting the server via JSON */
	this._fcnRequestJson = null;

	/* Number of iterarions for creating a hash. */
	this._numHashIteration = 10;

	/* self ref */
	var self = this;

	/**
	 * Initialize the instance.
	 * 
	 * @param {string} rootPath			Root URL
	 * @param {string} fcnRequestJson	Function for contacting the server via JSON
	 */
	this.initialize = function (rootPath, fcnRequestJson) {
		this._rootPath = rootPath;
		this._fcnRequestJson = fcnRequestJson;
	};

	/**
	 * Get authentication state.
	 * 
	 * Format: { auth: yes/no, sid : session id }
	 * 
	 * @param {function} resultsCallback  Callback which is used when the results arrive.
	 */
	this.getAuthState = function(resultsCallback) {
		this._fcnRequestJson(this._rootPath + '/state', null, 'GET', resultsCallback);
	};

	/**
	 * Try to login the user.
	 * 
	 * @param {string} userName           User name
	 * @param {string} userPassword       Plain user password, it will be hashed before transmission.
	 * @param {function} resultsCallback  Callback which is used when the results arrive.
	 */
	this.login = function(resultsCallback, userName, userPassword) {
		this._fcnRequestJson(this._rootPath + '/state', null, 'GET', {
			success: function(results, response) {
				var sid = "";
				// already authenticated?
				try {
					if (results.data.auth === "yes") {
						if (resultsCallback){
							resultsCallback.success(results, response);
							return;
						}
					}
					sid = results.data.sid;
				}
				catch(e) {
					if (resultsCallback.error) {
						resultsCallback.error("Exception occurred while parsing JSON response of auth state, reason: " + e, data);
					}
				}
				var pw = self.createHash(userPassword);
				pw = self.createHash(pw + sid);
				var data = {'login' : userName, 'password' : pw};
				self._fcnRequestJson(self._rootPath + '/login', data, 'POST', resultsCallback);
			},
			error: function(err) {
				if (resultsCallback){
					resultsCallback.error(err);
				}
			}
		});
	};

	/**
	 * Logout the user.
	 * 
	 * @param {function} resultsCallback  Callback which is used when the results arrive.
	 */
	this.logout = function(resultsCallback) {
		this._fcnRequestJson(this._rootPath + '/logout', null, 'POST', resultsCallback);
	};

	/**
	 * Create a hash representing a given string. Use this function for hashing
	 * a user password.
	 * 
	 *  @param {string} str   String which will be used for creating hash
	 *  @return {string}      SHA512 hash of given string
	 */
	this.createHash = function(str) {
		var hash = "" + str;
		for (var i = 0; i < this._numHashIteration; i++) {
			hash = this._SHA512(hash);
		}
		return hash;
	};

	//##########################################################################
	//#                         Private functions                              #
	//##########################################################################

	/*
	*  Secure Hash Algorithm (SHA512)
	*  http://www.happycode.info/
	*  
	*  NOTE (boto): Big thanks go to http://coursesweb.net/javascript/sha512-encrypt-hash_cs
	*               No Copyright notice was found.
	*  
	*  @param {string} str   String which will be used for creating a SHA512 hash
	*  @return {string}      SHA512 hash of given string
	*/
	this._SHA512 = function(str) {
	  function int64(msint_32, lsint_32) {
		this.highOrder = msint_32;
		this.lowOrder = lsint_32;
	  }

	  var H = [new int64(0x6a09e667, 0xf3bcc908), new int64(0xbb67ae85, 0x84caa73b),
		  new int64(0x3c6ef372, 0xfe94f82b), new int64(0xa54ff53a, 0x5f1d36f1),
		  new int64(0x510e527f, 0xade682d1), new int64(0x9b05688c, 0x2b3e6c1f),
		  new int64(0x1f83d9ab, 0xfb41bd6b), new int64(0x5be0cd19, 0x137e2179)];

	  var K = [new int64(0x428a2f98, 0xd728ae22), new int64(0x71374491, 0x23ef65cd),
		  new int64(0xb5c0fbcf, 0xec4d3b2f), new int64(0xe9b5dba5, 0x8189dbbc),
		  new int64(0x3956c25b, 0xf348b538), new int64(0x59f111f1, 0xb605d019),
		  new int64(0x923f82a4, 0xaf194f9b), new int64(0xab1c5ed5, 0xda6d8118),
		  new int64(0xd807aa98, 0xa3030242), new int64(0x12835b01, 0x45706fbe),
		  new int64(0x243185be, 0x4ee4b28c), new int64(0x550c7dc3, 0xd5ffb4e2),
		  new int64(0x72be5d74, 0xf27b896f), new int64(0x80deb1fe, 0x3b1696b1),
		  new int64(0x9bdc06a7, 0x25c71235), new int64(0xc19bf174, 0xcf692694),
		  new int64(0xe49b69c1, 0x9ef14ad2), new int64(0xefbe4786, 0x384f25e3),
		  new int64(0x0fc19dc6, 0x8b8cd5b5), new int64(0x240ca1cc, 0x77ac9c65),
		  new int64(0x2de92c6f, 0x592b0275), new int64(0x4a7484aa, 0x6ea6e483),
		  new int64(0x5cb0a9dc, 0xbd41fbd4), new int64(0x76f988da, 0x831153b5),
		  new int64(0x983e5152, 0xee66dfab), new int64(0xa831c66d, 0x2db43210),
		  new int64(0xb00327c8, 0x98fb213f), new int64(0xbf597fc7, 0xbeef0ee4),
		  new int64(0xc6e00bf3, 0x3da88fc2), new int64(0xd5a79147, 0x930aa725),
		  new int64(0x06ca6351, 0xe003826f), new int64(0x14292967, 0x0a0e6e70),
		  new int64(0x27b70a85, 0x46d22ffc), new int64(0x2e1b2138, 0x5c26c926),
		  new int64(0x4d2c6dfc, 0x5ac42aed), new int64(0x53380d13, 0x9d95b3df),
		  new int64(0x650a7354, 0x8baf63de), new int64(0x766a0abb, 0x3c77b2a8),
		  new int64(0x81c2c92e, 0x47edaee6), new int64(0x92722c85, 0x1482353b),
		  new int64(0xa2bfe8a1, 0x4cf10364), new int64(0xa81a664b, 0xbc423001),
		  new int64(0xc24b8b70, 0xd0f89791), new int64(0xc76c51a3, 0x0654be30),
		  new int64(0xd192e819, 0xd6ef5218), new int64(0xd6990624, 0x5565a910),
		  new int64(0xf40e3585, 0x5771202a), new int64(0x106aa070, 0x32bbd1b8),
		  new int64(0x19a4c116, 0xb8d2d0c8), new int64(0x1e376c08, 0x5141ab53),
		  new int64(0x2748774c, 0xdf8eeb99), new int64(0x34b0bcb5, 0xe19b48a8),
		  new int64(0x391c0cb3, 0xc5c95a63), new int64(0x4ed8aa4a, 0xe3418acb),
		  new int64(0x5b9cca4f, 0x7763e373), new int64(0x682e6ff3, 0xd6b2b8a3),
		  new int64(0x748f82ee, 0x5defb2fc), new int64(0x78a5636f, 0x43172f60),
		  new int64(0x84c87814, 0xa1f0ab72), new int64(0x8cc70208, 0x1a6439ec),
		  new int64(0x90befffa, 0x23631e28), new int64(0xa4506ceb, 0xde82bde9),
		  new int64(0xbef9a3f7, 0xb2c67915), new int64(0xc67178f2, 0xe372532b),
		  new int64(0xca273ece, 0xea26619c), new int64(0xd186b8c7, 0x21c0c207),
		  new int64(0xeada7dd6, 0xcde0eb1e), new int64(0xf57d4f7f, 0xee6ed178),
		  new int64(0x06f067aa, 0x72176fba), new int64(0x0a637dc5, 0xa2c898a6),
		  new int64(0x113f9804, 0xbef90dae), new int64(0x1b710b35, 0x131c471b),
		  new int64(0x28db77f5, 0x23047d84), new int64(0x32caab7b, 0x40c72493),
		  new int64(0x3c9ebe0a, 0x15c9bebc), new int64(0x431d67c4, 0x9c100d4c),
		  new int64(0x4cc5d4be, 0xcb3e42b6), new int64(0x597f299c, 0xfc657e2a),
		  new int64(0x5fcb6fab, 0x3ad6faec), new int64(0x6c44198c, 0x4a475817)];

	  var W = new Array(64);
	  var a, b, c, d, e, f, g, h, i, j;
	  var T1, T2;
	  var charsize = 8;

	  function utf8_encode(str) {
		return unescape(encodeURIComponent(str));
	  }

	  function str2binb(str) {
		var bin = [];
		var mask = (1 << charsize) - 1;
		var len = str.length * charsize;

		for (var i = 0; i < len; i += charsize) {
		  bin[i >> 5] |= (str.charCodeAt(i / charsize) & mask) << (32 - charsize - (i % 32));
		}

		return bin;
	  }

	  function binb2hex(binarray) {
		var hex_tab = "0123456789abcdef";
		var str = "";
		var length = binarray.length * 4;
		var srcByte;

		for (var i = 0; i < length; i += 1) {
		  srcByte = binarray[i >> 2] >> ((3 - (i % 4)) * 8);
		  str += hex_tab.charAt((srcByte >> 4) & 0xF) + hex_tab.charAt(srcByte & 0xF);
		}

		return str;
	  }

	  function safe_add_2(x, y) {
		var lsw, msw, lowOrder, highOrder;

		lsw = (x.lowOrder & 0xFFFF) + (y.lowOrder & 0xFFFF);
		msw = (x.lowOrder >>> 16) + (y.lowOrder >>> 16) + (lsw >>> 16);
		lowOrder = ((msw & 0xFFFF) << 16) | (lsw & 0xFFFF);

		lsw = (x.highOrder & 0xFFFF) + (y.highOrder & 0xFFFF) + (msw >>> 16);
		msw = (x.highOrder >>> 16) + (y.highOrder >>> 16) + (lsw >>> 16);
		highOrder = ((msw & 0xFFFF) << 16) | (lsw & 0xFFFF);

		return new int64(highOrder, lowOrder);
	  }

	  function safe_add_4(a, b, c, d) {
		var lsw, msw, lowOrder, highOrder;

		lsw = (a.lowOrder & 0xFFFF) + (b.lowOrder & 0xFFFF) + (c.lowOrder & 0xFFFF) + (d.lowOrder & 0xFFFF);
		msw = (a.lowOrder >>> 16) + (b.lowOrder >>> 16) + (c.lowOrder >>> 16) + (d.lowOrder >>> 16) + (lsw >>> 16);
		lowOrder = ((msw & 0xFFFF) << 16) | (lsw & 0xFFFF);

		lsw = (a.highOrder & 0xFFFF) + (b.highOrder & 0xFFFF) + (c.highOrder & 0xFFFF) + (d.highOrder & 0xFFFF) + (msw >>> 16);
		msw = (a.highOrder >>> 16) + (b.highOrder >>> 16) + (c.highOrder >>> 16) + (d.highOrder >>> 16) + (lsw >>> 16);
		highOrder = ((msw & 0xFFFF) << 16) | (lsw & 0xFFFF);

		return new int64(highOrder, lowOrder);
	  }

	  function safe_add_5(a, b, c, d, e) {
		var lsw, msw, lowOrder, highOrder;

		lsw = (a.lowOrder & 0xFFFF) + (b.lowOrder & 0xFFFF) + (c.lowOrder & 0xFFFF) + (d.lowOrder & 0xFFFF) + (e.lowOrder & 0xFFFF);
		msw = (a.lowOrder >>> 16) + (b.lowOrder >>> 16) + (c.lowOrder >>> 16) + (d.lowOrder >>> 16) + (e.lowOrder >>> 16) + (lsw >>> 16);
		lowOrder = ((msw & 0xFFFF) << 16) | (lsw & 0xFFFF);

		lsw = (a.highOrder & 0xFFFF) + (b.highOrder & 0xFFFF) + (c.highOrder & 0xFFFF) + (d.highOrder & 0xFFFF) + (e.highOrder & 0xFFFF) + (msw >>> 16);
		msw = (a.highOrder >>> 16) + (b.highOrder >>> 16) + (c.highOrder >>> 16) + (d.highOrder >>> 16) + (e.highOrder >>> 16) + (lsw >>> 16);
		highOrder = ((msw & 0xFFFF) << 16) | (lsw & 0xFFFF);

		return new int64(highOrder, lowOrder);
	  }

	  function maj(x, y, z) {
		return new int64(
		  (x.highOrder & y.highOrder) ^ (x.highOrder & z.highOrder) ^ (y.highOrder & z.highOrder),
		  (x.lowOrder & y.lowOrder) ^ (x.lowOrder & z.lowOrder) ^ (y.lowOrder & z.lowOrder)
		);
	  }

	  function ch(x, y, z) {
		return new int64(
		  (x.highOrder & y.highOrder) ^ (~x.highOrder & z.highOrder),
		  (x.lowOrder & y.lowOrder) ^ (~x.lowOrder & z.lowOrder)
		);
	  }

	  function rotr(x, n) {
		if (n <= 32) {
		  return new int64(
		   (x.highOrder >>> n) | (x.lowOrder << (32 - n)),
		   (x.lowOrder >>> n) | (x.highOrder << (32 - n))
		  );
		} else {
		  return new int64(
		   (x.lowOrder >>> n) | (x.highOrder << (32 - n)),
		   (x.highOrder >>> n) | (x.lowOrder << (32 - n))
		  );
		}
	  }

	  function sigma0(x) {
		var rotr28 = rotr(x, 28);
		var rotr34 = rotr(x, 34);
		var rotr39 = rotr(x, 39);

		return new int64(
		  rotr28.highOrder ^ rotr34.highOrder ^ rotr39.highOrder,
		  rotr28.lowOrder ^ rotr34.lowOrder ^ rotr39.lowOrder
		);
	  }

	  function sigma1(x) {
		var rotr14 = rotr(x, 14);
		var rotr18 = rotr(x, 18);
		var rotr41 = rotr(x, 41);

		return new int64(
		  rotr14.highOrder ^ rotr18.highOrder ^ rotr41.highOrder,
		  rotr14.lowOrder ^ rotr18.lowOrder ^ rotr41.lowOrder
		);
	  }

	  function gamma0(x) {
		var rotr1 = rotr(x, 1), rotr8 = rotr(x, 8), shr7 = shr(x, 7);

		return new int64(
		  rotr1.highOrder ^ rotr8.highOrder ^ shr7.highOrder,
		  rotr1.lowOrder ^ rotr8.lowOrder ^ shr7.lowOrder
		);
	  }

	  function gamma1(x) {
		var rotr19 = rotr(x, 19);
		var rotr61 = rotr(x, 61);
		var shr6 = shr(x, 6);

		return new int64(
		  rotr19.highOrder ^ rotr61.highOrder ^ shr6.highOrder,
		  rotr19.lowOrder ^ rotr61.lowOrder ^ shr6.lowOrder
		);
	  }

	  function shr(x, n) {
		if (n <= 32) {
		  return new int64(
		   x.highOrder >>> n,
		   x.lowOrder >>> n | (x.highOrder << (32 - n))
		  );
		} else {
		  return new int64(
		   0,
		   x.highOrder << (32 - n)
		  );
		}
	  }

	  str = utf8_encode(str);
	  var strlen = str.length*charsize;
	  str = str2binb(str);

	  str[strlen >> 5] |= 0x80 << (24 - strlen % 32);
	  str[(((strlen + 128) >> 10) << 5) + 31] = strlen;

	  for (var i = 0; i < str.length; i += 32) {
		a = H[0];
		b = H[1];
		c = H[2];
		d = H[3];
		e = H[4];
		f = H[5];
		g = H[6];
		h = H[7];

		for (var j = 0; j < 80; j++) {
		  if (j < 16) {
		   W[j] = new int64(str[j*2 + i], str[j*2 + i + 1]);
		  } else {
		   W[j] = safe_add_4(gamma1(W[j - 2]), W[j - 7], gamma0(W[j - 15]), W[j - 16]);
		  }

		  T1 = safe_add_5(h, sigma1(e), ch(e, f, g), K[j], W[j]);
		  T2 = safe_add_2(sigma0(a), maj(a, b, c));
		  h = g;
		  g = f;
		  f = e;
		  e = safe_add_2(d, T1);
		  d = c;
		  c = b;
		  b = a;
		  a = safe_add_2(T1, T2);
		}

		H[0] = safe_add_2(a, H[0]);
		H[1] = safe_add_2(b, H[1]);
		H[2] = safe_add_2(c, H[2]);
		H[3] = safe_add_2(d, H[3]);
		H[4] = safe_add_2(e, H[4]);
		H[5] = safe_add_2(f, H[5]);
		H[6] = safe_add_2(g, H[6]);
		H[7] = safe_add_2(h, H[7]);
	  }

	  var binarray = [];
	  for (var i = 0; i < H.length; i++) {
		binarray.push(H[i].highOrder);
		binarray.push(H[i].lowOrder);
	  }
	  return binb2hex(binarray);
	};
}