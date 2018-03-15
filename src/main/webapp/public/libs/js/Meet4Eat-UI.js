/**
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
"use strict";

/**
 * Main Meet4Eat Admin Panel UI Control
 * 
 * Dependencies:
 *   - Meet4Eat-REST.js
 *   - This modules assumes existing UI elements with proper IDs in HTML.
 * 
 * Usage:
 *   Once the page content is loaded, create an instance of Meet4Eat and call initialize on it.
 *   
 * @author boto
 * Date of creation Sep 1, 2017
 */
function Meet4EatUI() {

	var self = this;

	/* UI version */
	self._version              = "0.9.0";

	self._m4eAppInfo           = {'clientVersion' : '0.0.0', 'serverVersion' : '0.0.0', 'viewVersion' : '0'};
	self._m4eAuthUser          = {'auth': false, 'id' : 0, 'login': '', 'name' : '', 'roles' : [] };

	self._m4eRESTAuth          = null;
	self._m4eREST              = null;
	self._m4eRESTUsers         = null;
	self._m4eRESTUserReg       = null;
	self._m4eRESTEvents        = null;
	self._m4eRESTMaintenance   = null;
	self._m4eRESTUpdateCheck   = null;	
	self._eventEventDatePicker = null;
	self._lastMenuItem         = null;

	self._uiModuleUser         = null;
	self._uiModuleEvent        = null;
	self._uiModuleImage        = null;
	self._uiModuleUpdateCheck  = null;

	/**
	 * Initialize the Meet4Eat UI.
	 */
	self.initialize = function() {
		// setup the rest modules
		self._m4eREST            = new Meet4EatREST();
		self._m4eRESTAuth        = self._m4eREST.buildUserAuthREST();
		self._m4eRESTUsers       = self._m4eREST.buildUserREST();
		self._m4eRESTUserReg     = self._m4eREST.buildUserRegistrationREST();
		self._m4eRESTEvents      = self._m4eREST.buildEventREST();
		self._m4eRESTMaintenance = self._m4eREST.buildMaintenanceREST();
		self._m4eRESTUpdateCheck = self._m4eREST.buildUpdateCheckREST();

		self._m4eAppInfo.clientVersion = self._m4eREST.getVersion();
		self._m4eAppInfo.viewVersion   = self._version;

		// setup the ui modules which are used, i.e. referenced in  html code
		if (typeof(Meet4EatUI_User) === typeof(Function)) {
			self._uiModuleUser = new Meet4EatUI_User(self);
			self._uiModuleUser.initialize();
		}
		if (typeof(Meet4EatUI_Event) === typeof(Function)) {
			self._uiModuleEvent = new Meet4EatUI_Event(self);
			self._uiModuleEvent.initialize();
		}
		if (typeof(Meet4EatUI_Image) === typeof(Function)) {
			self._uiModuleImage = new Meet4EatUI_Image(self);
			self._uiModuleImage.initialize();
		}
		if (typeof(Meet4EatUI_UpdateCheck) === typeof(Function)) {
			self._uiModuleUpdateCheck = new Meet4EatUI_UpdateCheck(self);
			self._uiModuleUpdateCheck.initialize();
		}

		self._setupUi();
	};

	/**
	 * Get the module version.
	 * 
	 * @returns {string} Version of this module.
	 */
	self.getVersion = function() {
		return self._version;
	};

	/**
	 * Get past time since last server access.
	 * 
	 * @returns {integer}	Past time since last server access in seconds
	 */
	self.getPastAccessTime = function() {
		return self._m4eREST.getPastAccessTime();
	};

	/**
	 * Show a modal dialog with given content text, title and button texts.
	 * If a callback object is given then it is used for propagating the 
	 * button click event.
	 * 
	 * The callback object has the following form:
	 * 
	 *   {
	 *		onClickBtn1: function(btnText) {
	 *		    // handle button click 1
	 *		},
	 *		onClickBtn2: function(btnText) {
	 *		    // handle button click 2
	 *		}
	 *   }
	 *   
	 * @param {string}   text		    Messagebox text
	 * @param {string}   title		    Title
	 * @param {string}   textBtn1	    Text of button 1, pass null to hide button 1
	 * @param {string}   textBtn2       Text of button 2, pass null to hide button 2
	 * @param {function} btnCallbacks	Optional callback object used on button clicks.
	 */
	self.showModalBox = function(text, title, textBtn1, textBtn2, btnCallbacks) {
		$('#msg_box_text').text(text);
		$('#msg_box_title').text(title);
		if (textBtn1) {
			$('#msg_box_btn1').text(textBtn1);
			$('#msg_box_btn1').removeClass("hide");
			$("#msg_box_btn1").click(function() {
				if (btnCallbacks && btnCallbacks.onClickBtn1) {
					btnCallbacks.onClickBtn1(textBtn1);
				}
			});
		}
		else {
			$('#msg_box_btn1').addClass("hide");
		}
		if (textBtn2) {
			$('#msg_box_btn2').text(textBtn2);
			$('#msg_box_btn2').removeClass("hide");
			$("#msg_box_btn2").click(function() {
				if (btnCallbacks && btnCallbacks.onClickBtn2) {
					btnCallbacks.onClickBtn2(textBtn2);
				}
			});
		}
		else {
			$('#msg_box_btn2').addClass("hide");
		}
		$('#msg_box').modal("show");
	};

	/**
	 * Show a modal dialog with given html code in body.
	 * 
	 * @param {string}   html        HTML code to display
	 * @param {string}   title       Title
	 * @param {string}   textBtn     Button text
	 * @param {function} btnCallback Optional callback object used on button click.
	 */
	self.showModalInfoBox = function(html, title, textBtn, btnCallback) {
		$('#msg_box_text').html(html);
		$('#msg_box_title').text(title);
		$('#msg_box_btn2').addClass("hide");
		if (textBtn) {
			$('#msg_box_btn1').removeClass("hide");
			$('#msg_box_btn1').text(textBtn);
			$("#msg_box_btn1").click(function() {
				if (btnCallback) {
					btnCallback();
				}
			});
		}
		$('#msg_box').modal("show");
	};

	/**
	 * Display an error message in HTML format.
	 * 
	 * @param {type} html
	 */
	self.displayErrorHTML = function(html) {
		$('#display_error').html(html);
	};

	/**
	 * Get the image module. This modules handles image/icon related operations.
	 * 
	 * @returns {Meet4EatUI_Image} Image module
	 */
	self.getImageModule = function() {
		return self._uiModuleImage;
	};

	/**
	 * Handle login button click.
	 */
	self.onBtnLogin = function() {
		var fields = $('#form_login').serializeArray().reduce(function(obj, item) {
			obj[item.name] = item.value;
			return obj;
		}, {});
		self._login(fields.login, fields.password);
		$("#form_login :input").val("");
	};

	/**
	 * Handle logout button click.
	 */
	self.onBtnLogout = function() {
		self._logout();
	};

	/**
	 * Handle user registration button click.
	 */
	self.onBtnRegisterUser = function() {
		var inputfields = $('#user_registration_form').serializeArray().reduce(function(obj, item) {
			obj[item.name] = item.value;
			return obj;
		}, {});
		var passwd = inputfields['password'];
		var passwdrepeat = inputfields['password-repeat'];
		if (passwd !== passwdrepeat) {
			self.showModalBox("The password and its repetition do not match!", "Invalid Input", "Dismiss");
			return;
		}
		if (passwd.length < 8) {
			self.showModalBox("The password must have at least 8 characters!", "Invalid Input", "Dismiss");
			return;
		}
		inputfields['password'] = self._m4eRESTAuth.createHash(passwd);
		self._m4eRESTUserReg.accountRegister({
			success: function(res, resp) {
				if (res.status === "ok") {
					self.showModalBox("You were successfully registered. An activation link has been sent to the email address you supplied, along with instructions for activating your account.", "Registration", "Dismiss", null, {
						 onClickBtn1: function() {
							 window.location.href = "index.html";
						 }
					});
				}
				else {
					self.showModalBox("Registration failed. Reason: " + res.description, "Registration Problem", "Dismiss");
				}
			},
			error: function(err) {
				self.showModalBox(err, "Connection Problem", "Dismiss");
			}
		}, inputfields);		
	};

	/**
	 * Request the server for purging dead resources.
	 * This can be used only by an authorized admin.
	 */
	self.onBtnMaintenancePurge = function() {
		if (!self._userIsAdmin()) {
			return;
		}
		self._m4eRESTMaintenance.maintenancePurge({
			success: function(res, resp) {
				if (res.status === "ok") {
					self.showModalBox("Purging was successful. Results: " + res.description, "Maintenance: Purge Results", "Dismiss");
					// update the maintenance stats
					self._deferExecution(function() {
						self._setupUiAdmin();
					}, 1000);
				}
				else {
					self.showModalBox("A problem occurred during pruging. Results: " + res.description, "Maintenance: Purge Results", "Dismiss");
				}
			},
			error: function(err) {
				self.showModalBox(err, "Connection Problem", "Dismiss");
			}
		});
	};

	/**********************************************************************/
	/*                        Private functions                           */
	/**********************************************************************/

	/**
	 * Show/hide an element given its ID.
	 * 
	 * @param {string} elemId   Element ID
	 * @param {bool} show  Pass true for showing and false for hiding element
	 */
	self._showElement = function(elemId, show) {
		var elem = $('#' + elemId);
		if (show) {
			elem.removeClass("hide");
		}
		else {
			elem.addClass("hide");					
		}
	};

	/**
	 * Scroll to given element.
	 * 
	 * @param {string} elementId ID of element to scroll to
	 * @param {string} speed     Optional scrolling speed, e.g. 'slow', 'fast'. Default is 'slow'.
	 */
	self._scrollToElement = function(elementId, speed) {
		if (!speed) {
			speed = 'slow';
		}
		var scrollto = $('#' + elementId).offset();
		$('html, body').animate({ scrollTop: (scrollto.top)}, speed);
	};

	/**
	 * Deferr the execution of given function by 'delay' milliseconds.
	 * 
	 * @param {function} fcn   Function to execute
	 * @param {int} delay      Execution delay in millisecond
	 */
	self._deferExecution = function(fcn, delay) {
		setTimeout(fcn, delay);
	};

	/**
	 * Given a time stamp format it to a string.
	 * 
	 * @param {Date} timeStamp Time stamp to be formatted
	 * @returns {string}       Formatted time string
	 */
	self._formatTime = function(timeStamp) {
		var minutes = "" + timeStamp.getMinutes();
		minutes = minutes < 10 ? ("0" + minutes) : minutes;
		var text = timeStamp.getFullYear() + "-" + (timeStamp.getMonth()+1) + "-" + timeStamp.getDate()  
					 + " " + timeStamp.getHours() + ":" + minutes;
		return text;
	};

	/**
	 * Convert the given text to a HTML safe string.
	 * 
	 * @param {string} text         Input text
	 * @returns {string}            HTML safe string
	 */
	self._encodeString = function(text) {
		return $('<div>').text(text).html();
	};

	/*++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/
	/*                ui init related stuff                   */
	/*++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/

	self._setupAboutPage = function() {
		$('#info_clientversion').text(self._m4eAppInfo.clientVersion);
		$('#info_viewversion').text(self._m4eAppInfo.viewVersion);
		self._m4eREST.getServerInfo({
			success: function(results, response) {
				if (results.status === "ok") {
					self._m4eAppInfo.serverVersion = results.data.version;
					$('#info_serverversion').text(self._m4eAppInfo.serverVersion);
				}
			}
		});
	};

	self._setupDashboardPage = function() {
		self._m4eRESTEvents.getCount({
			success: function(results, response) {
				$('#count_events').text(results.data.count);
			}
		});
		self._m4eRESTUsers.getCount({
			success: function(results, response) {
				$('#count_users').text(results.data.count);
			}
		});
	};

	/*++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/
	/*                   auth related stuff                   */
	/*++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/

	self._setupUi = function() {
		self._m4eRESTAuth.getAuthState({
			success: function(results, response) {
				if (results.status === "ok") {
					if (results.data.auth) {
						self._showElement('main_content', true);
						self._showElement('main_login', false);
						// get some info on logged-in user
						self._deferExecution(function () {
							self._m4eRESTUsers.find({
								success: function(res, resp) {
									if (res.status === "ok") {
										self._m4eAuthUser.auth = true;
										self._m4eAuthUser.login = res.data.login;
										self._m4eAuthUser.name = res.data.name;
										self._m4eAuthUser.id = res.data.id;
										self._m4eAuthUser.roles = res.data.roles;
										self._onAuthenticated();
									}
									else {
										self.showModalBox(res.description, "Connection Problem", "Dismiss");
									}
								},
								error: function(err) {
									self.showModalBox(err, "Connection Problem", "Dismiss");
								}
							}, results.data.id);
						}, 0);
					}
					else {
						self._showElement('main_login', true);
						self._showElement('main_content', false);
						$("#form_login input[name='login']").focus();
					}
				}
				else {
					self.showModalBox(response, "Connection Problem", "Dismiss");
				}
			},
			error: function(text, response) {
				$('#display_error').html(response);
				self.showModalBox(text, "Connection Problem", "Dismiss");
			}
		});
	};

	self._login = function(loginName, pasword) {
		$("#form_login").removeClass("has-error");
		self._m4eRESTAuth.login({
			success: function(results, response) {
				if (results.status === "ok") {
					window.location.href = "index.html";
				}
				else {
					$("#form_login input[name='login']").focus();
					$("#form_login").addClass("has-error");
				}
			},
			error: function(text, response) {
				$('#display_error').html(response);
				self.showModalBox(text, "Login Problem", "Dismiss");
			}
		}, loginName, pasword);
	};

	self._logout = function() {
		self._m4eRESTAuth.logout({
			success: function(results, response) {
				window.location.href = "index.html";
			},
			error: function(text, response) {
				$('#display_error').html(response);
				self.showModalBox(text, "Logout Problem", "Dismiss");
			}
		});
	};

	/**
	 * Get currently authorized (logged in) user.
	 * 
	 * @returns The current logged in user
	 */
	self._getAuthUser = function() {
		return self._m4eAuthUser;
	};

	/**
	 * Check if at least one of the given roles is contained in roles of
	 * authenticated user.
	 * 
	 * @param  roles    Roles to check, a string array
	 * @returns {bool}  Return true if at least one matching role was found, otherwise false.
	 */
	self._authUserRolesContain = function(roles) {
		for (var i= 0; i < roles.length; i++) {
			if ($.inArray(roles[i], self._getAuthUser().roles) > -1) {
				return true;
			}
		}
		return false;
	};

	/**
	 * Check if the authorized user is an admin.
	 * 
	 * @returns {bool} Return true if the user is admin.
	 */
	self._userIsAdmin = function() {
		return self._authUserRolesContain(["ADMIN"]);
	};

	//--------------------------------------------
	self._onAuthenticated = function() {
		$('#user_name').text(self._getAuthUser().name);
		self._setupDashboardPage();
		self._uiModuleUser.initialize();
		self._uiModuleEvent.initialize();
		// setup the admin menu if the authenticated user is an admin
		self._showElement('admin_nav_system', self._userIsAdmin());
	};

	/*++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/
	/*                   admin related stuff                  */
	/*++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/

	self._setupUiAdmin = function() {
		self._m4eRESTMaintenance.getMaintenanceStats({
			success: function(results, reponse) {
				if (results.status !== "ok") {
					self.showModalBox("Cannot retrieve system stats! Reason: " + results.description, "Problem Getting System Stats.", "Dismiss");
					return;
				}
				var stats = results.data;
				var maintenance = "-";
				if (stats.dateLastMaintenance) {
					maintenance = self._formatTime(new Date(parseInt(stats.dateLastMaintenance)));
				}
				var update = "-";
				if (stats.dateLastUpdate) {
					update = self._formatTime(new Date(parseInt(stats.dateLastUpdate)));
				}
				$('#sys_serverversion').text(stats.version);
				$('#sys_lastmaintenance').text(maintenance);
				$('#sys_lastupdate').text(update);
				$('#sys_countuserspurge').text(stats.userCountPurge);
				$('#sys_counteventspurge').text(stats.eventCountPurge);
				$('#sys_counteventlocationspurge').text(stats.eventLocationCountPurge);
				$('#sys_countpendingaccounts').text(stats.pendingAccountRegistration);
				$('#sys_countpendingpasswords').text(stats.pendingPasswordResets);
			},
			error: function(err) {
				self.showModalBox("Cannot retrieve server stats! Reason: " + err, "Connection Problem", "Dismiss");
			}
		});

		self.setupUiModuleUpdateCheck();
	};

	/*++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/
	/*                   menu related stuff                   */
	/*++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/

	self.onMenu = function(item) {
		// main menu element IDs are expected to begin with menu_
		var allitems = $("[id*='menu_'");
		if (allitems) {
			allitems.addClass("hide");
		}
		var wrapper = $('#menu_' + item);
		if (wrapper) {
			wrapper.removeClass("hide");
		}

		switch(self._lastMenuItem) {
			case "dashboard":
				self.onLeavePageDashboard();
				break;
			case "users":
				self.onLeavePageUsers();
				break;
			case "events":
				self.onLeavePageEvents();
				break;
			case "system":
				self.onLeavePageSystem();
				break;
			case "about":
				self.onLeavePageAbout();
				break;
		}

		switch(item) {
			case "dashboard":
				self.onEnterPageDashboard();
				break;
			case "users":
				self.onEnterPageUsers();
				break;
			case "events":
				self.onEnterPageEvents();
				break;
			case "system":
				self.onEnterPageSystem();
				break;
			case "about":
				self.onEnterPageAbout();
				break;
		}
		self._lastMenuItem = item;
	};

	/****************  menu change callbacks ****************/

	self.onEnterPageDashboard = function() {			
		self._setupDashboardPage();
	};

	self.onLeavePageDashboard = function() {			
	};

	self.onEnterPageUsers = function() {
		self._showElement("page_users_new", self._userIsAdmin());
		self.setupUiModuleUser();
	};

	self.onLeavePageUsers = function() {
		self._showElement("page_users_edit", false);
	};

	self.onEnterPageEvents = function() {
		self.setupUiModuleEvent();
	};

	self.onLeavePageEvents = function() {
		self._showElement("page_events_edit", false);
	};

	self.onEnterPageSystem = function() {
		self._setupUiAdmin();
	};

	self.onLeavePageSystem = function() {
	};

	self.onEnterPageAbout = function() {
		self._setupAboutPage();
	};

	self.onLeavePageAbout = function() {	
	};
}