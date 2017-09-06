/**
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
"use strict";

/**
 * Meet4Eat Admin Panel UI Control
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
	self._version              = "0.5.0";

	self._m4e                  = null;
	self._m4eAuth              = null;
	self._m4eAuthUser          = {'auth': 'no', 'id' : 0, 'login': '', 'name' : '', 'roles' : [] };
	self._m4eUsers             = null;
	self._m4eEvents            = null;
	self._m4eAppInfo           = {'clientVersion' : '0.0.0', 'serverVersion' : '0.0.0', 'viewVersion' : '0'};
	self._m4eUserTable         = null;
	self._eventEventDatePicker = null;
	self._lastMenuItem         = null;

	/**
	 * Initialize the Meet4Eat UI.
	 */
	self.inititialize = function() {
		self._m4e = new Meet4EatREST();
		self._m4eAppInfo.clientVersion = self._m4e.getVersion();
		self._m4eAppInfo.viewVersion = self._version;
		self._m4eAuth = self._m4e.buildUserAuthREST();
		self._m4eUsers = self._m4e.buildUserREST();
		self._m4eEvents = self._m4e.buildEventREST();
		self._setupUi();
	};

	/**
	 * Get the web interface version.
	 * @returns {string} Version of this web interface.
	 */
	self.getVersion = function() {
		return self._version;
	};

	/**
	 * Get past time since last server access.
	 * 
	 * @returns {integer}	Past time since last server access in seconds
	 */
	this.getPastAccessTime = function() {
		return self._m4e.getPastAccessTime();
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
	 * Delete user with given ID.
	 * 
	 * @param {integer} id	User ID
	 */
	self.onBtnUserDelete = function(id) {
		self.showModalBox("Are you really sure you want to delete the user?", "Delete User", "No", "Yes", {
			onClickBtn1: function() {
				// nothing to do
			},
			onClickBtn2: function() {
				// sure to delete
				// NOTE as deleteUser opens also a modal messagebox we have to defer the call!
				self._deferExecution(function() {
					self._deleteUser(id);
				}, 500);
			}
		});
	};

	/**
	 * Create a new user.
	 */
	self.onBtnUserNew = function() {
		self._showElement('menu_users', false);
		self._showElement('page_users_edit', true);
		self._setupUiUserNew();
	};

	/**
	 * Edit user with given ID.
	 * 
	 * @param {integer} id User ID
	 */
	self.onBtnUserEdit = function(id) {
		self._showElement('menu_users', false);
		self._showElement('page_users_edit', true);
		self._setupUiUserEdit(id);
	};

	/**
	 * Apply user settings changes. This is used on "edit user" and "new user".
	 */
	self.onBtnUserEditApply = function() {
		var inputfields = $('#page_users_edit_form').serializeArray().reduce(function(obj, item) {
			obj[item.name] = item.value;
			return obj;
		}, {});
		var fields = {};
		var newuser = (inputfields.id === null) || (inputfields.id === "");
		fields['id'] = inputfields.id;
		fields['name'] = inputfields.name;
		fields['login'] = inputfields.login;
		fields['email'] = inputfields.email;
		var passwd = inputfields['password'];
		var passwdr = inputfields['password-repeat'];
		if (passwd !== "" || passwdr !== "") {
			if (passwd !== passwdr) {
				self.showModalBox("Password fields do not match!", "Invalid Input", "Dismiss");
				return;
			}
			fields['password'] = self._m4eAuth.createHash(passwd);
		}
		var roles = [];
		var selroles = $('#page_users_edit_roles_sel option:selected');
		selroles.each(function(index, item){
			var r = $(item);
			roles.push(r.text());
		});
		fields['roles'] = roles;

		self._createOrUpdateUser(fields, function(results) {
			if (results.status === "ok") {
				self.showModalBox("Changes were successfully applied to user.", "User Update", "Dismiss");
				if (newuser) {
					// on successful creation the new user id is in results.data
					fields.id = results.data.id;
					self._updateUiUserTableAdd(fields);
					self._getUserTable().draw("full-hold");
				}
				else {
					self._updateUiUserTableUpdate(fields);
				}
				self._showElement('menu_users', true);
				self._showElement('page_users_edit', false);
				// are we editting our own settings?
				if ((""+self._getAuthUser().id) === (""+fields.id)) {
					self._getAuthUser().name = inputfields.name;
					$('#user_name').text(inputfields.name);
				}
			}
			else {
				self.showModalBox("Could not apply changes to user! Reason: " + results.description, "User Update", "Dismiss");
			}
		});
	};

	/**
	 * Cancel user edit ui.
	 */
	self.onBtnUserEditCancel = function() {
		self.onBtnUserClearForm();
		self._showElement('page_users_edit', false);
		self._showElement('menu_users', true);
	};

	/**
	 * Clear the user edit form.
	 */
	self.onBtnUserClearForm = function() {
		$("#page_users_edit_form :input").val("");
	};

	/**
	 * Delete the event with given ID.
	 * 
	 * @param {interger} id Event ID
	 */
	self.onBtnEventDelete = function(id) {
		self.showModalBox("Are you really sure you want to delete the event?", "Delete Event", "No", "Yes", {
			onClickBtn1: function() {
				// nothing to do
			},
			onClickBtn2: function() {
				// sure to delete
				// NOTE as deleteUser opens also a modal messagebox we have to defer the call!
				self._deferExecution(function() {
					self._deleteEvent(id);
				}, 500);
			}
		});
	};

	/**
	 * Create a new event.
	 */
	self.onBtnEventNew = function() {
		self._showElement('menu_events', false);
		self._showElement('page_events_edit', true);
		self._setupUiEventNew();
	};

	/**
	 * Edit the event with given ID.
	 * 
	 * @param {interger} id Event ID
	 */
	self.onBtnEventEdit = function(id) {
		self._showElement('menu_events', false);
		self._showElement('page_events_edit', true);
		self._setupUiEventEdit(id);
	};

	/**
	 * Apply event settings changes. This is used on "edit event" and "new event".
	 */
	self.onBtnEventEditApply = function() {
		var inputfields = $('#page_events_edit_form').serializeArray().reduce(function(obj, item) {
			obj[item.name] = item.value;
			return obj;
		}, {});
		var fields = {};
		var newevent = (inputfields.id === null) || (inputfields.id === "");
		fields['id'] = inputfields.id;
		fields['name'] = inputfields.name;
		fields['description'] = inputfields.description;
		if (self._eventEventDatePicker.date()) {
			var msec = self._eventEventDatePicker.date().toDate().getTime();
			fields['eventStart'] = msec / 1000;
		}
		if (self._eventEventDayTimePicker.date()) {
			var msec = self._eventEventDayTimePicker.date().toDate().getTime();
			fields['repeatDayTime'] = msec / 1000;
		}
		fields['repeatWeekDays'] = self._getEventWeekDays();

		self._createOrUpdateEvent(fields, function(results) {
			if (results.status === "ok") {
				self.showModalBox("Changes were successfully applied to event.", "Event Update", "Dismiss");
				if (newevent) {
					// on successful creation the new user id is in results.data
					fields.id = results.data.id;
					self._updateUiEventTableAdd(fields);
					self._getEventTable().draw("full-hold");
				}
				else {
					self._updateUiEventTableUpdate(fields);
				}
				self._showElement('menu_events', true);
				self._showElement('page_events_edit', false);
			}
			else {
				self.showModalBox("Could not apply changes to event! Reason: " + results.description, "Event Update", "Dismiss");
			}
		});
	};

	/**
	 * Cancel event edit ui.
	 */
	self.onBtnEventEditCancel = function() {
		self.onBtnEventClearForm();
		self._showElement('page_events_edit', false);
		self._showElement('menu_events', true);
	};

	/**
	 * Clear the event edit form.
	 */
	self.onBtnEventClearForm = function() {
		$("#page_events_edit_form :input").val("");
	};

	/**
	 * Search for a member. The keyword is expected to be in an input
	 * element with ID 'page_events_edit_form_mem_search'.
	 */
	self.onBtnSearchMember = function() {
		var keyword = $('#page_events_edit_form_mem_search').val();
		var sel = $('#page_events_edit_form_mem_search_hits');
		sel.empty();
		if (!keyword) {
			return;
		}
		self._m4eUsers.search({
			success: function(res, resp) {
				if (res.status === "ok") {
					var hits = res.data;
					for (var i = 0; i < hits.length; i++) {
						sel.append(new Option(hits[i].name, hits[i].id));
					}
				}
				else {
					self.showModalBox(res.description, "Connection Problem", "Dismiss");
				}
			},
			error: function(err) {
				self.showModalBox(err, "Connection Problem", "Dismiss");
			}
		}, keyword);
	};

	/**
	 * Show information of the user with given ID in a modal dialog.
	 * 
	 * @param {integer} memberId  User ID
	 */
	self.onBtnEventMemberInfo = function(memberId) {
		self._m4eUsers.find({
			success: function(res, resp) {
				if (res.status === "ok") {
					var user = res.data;
					var lastlogin = "-";
					if (user.dateLastLogin && user.dateLastLogin > 0) {
						var timestamp = new Date(parseInt(user.dateLastLogin));
						lastlogin = self._formatTime(timestamp);
					}
					var html = "<table class='table table-striped table-bordered'>";
					html += "<tr><td>User Name</td><td>" + user.name + "</td></tr>";
					html += "<tr><td>E-Mail</td><td>" + user.email + "</td></tr>";
					html += "<tr><td>Last Online</td><td>" + lastlogin + "</td></tr>";
					html += "</table>";
					self.showModalInfoBox(html, "User Info", "Dismiss");
				}
				else {
					self.showModalBox(res.description, "Problem Getting User Info", "Dismiss");
				}
			},
			error: function(err) {
				self.showModalBox(err, "Connection Problem", "Dismiss");
			}
		}, memberId);
	};

	/**
	 * Remove a member from an event.
	 * 
	 * @param {type} eventId  Event ID
	 * @param {type} memberId ID of the user to be removed
	 */
	self.onBtnEventMemberRemove = function(eventId, memberId) {
		self.showModalBox("Are you really sure you want to remove the member?", "Remove member", "No", "Yes", {
			onClickBtn1: function() {
				// nothing to do
			},
			onClickBtn2: function() {
				// sure to delete
				// NOTE as deleteUser opens also a modal messagebox we have to defer the call!
				self._deferExecution(function() {
					self._m4eEvents.removeEventMember({
						success: function(res, resp) {
							if (res.status === "ok") {
								self.showModalBox("Member was successfully removed from Event.", "Remove Event Member", "Dismiss");
								self._updateUiEventMemberRemove(eventId, memberId);
							}
							else {
								self.showModalBox(res.description, "Problem Removing Member", "Dismiss");
							}
						},
						error: function(err) {
							self.showModalBox(err, "Connection Problem", "Dismiss");
						}
					}, eventId, memberId);
				}, 500);
			}
		});
	};

	/**
	 * Make a member new owner of an event.
	 * 
	 * @param {type} eventId  Event ID
	 * @param {type} memberId ID of the user to get new owner
	 */
	self.onBtnEventMemberMakeOwner = function(eventId, memberId) {
		alert("TODO: onBtnEventMemberMakeOwner");
	};

	/**
	 * Add the member which is selected in selection element with
	 * ID 'page_events_edit_form_mem_search_hits'.
	 */
	self.onBtnEventMemberAdd = function() {
		var sel = $('#page_events_edit_form_mem_search_hits option:selected');
		var userid = sel.val();
		var eventid = $("#page_events_edit_form input[name='id']").val();
		if (!eventid || !userid) {
			self.showModalBox("onBtnEventMemberAdd: Cannot add user to event, invalid user or event id!", "Internal Error", "Dismiss");
		}

		self._m4eEvents.addEventMember({
			success: function(res, resp) {
				if (res.status === "ok") {
					self.showModalBox("Member was successfully added to Event.", "Add Event Member", "Dismiss");
					self._updateUiEventMemberAdd(true, eventid, res.data.memberId, res.data.memberName);
				}
				else {
					self.showModalBox(res.description, "Problem Adding Member", "Dismiss");
				}
			},
			error: function(err) {
				self.showModalBox(err, "Connection Problem", "Dismiss");
			}
		}, eventid, userid);
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
	 * @returns {string} Formatted time string
	 */
	self._formatTime = function(timeStamp) {
		var minutes = "" + timeStamp.getMinutes();
		minutes = minutes < 10 ? ("0" + minutes) : minutes;
		var text = timeStamp.getFullYear() + "-" + (timeStamp.getMonth()+1) + "-" + timeStamp.getDate()  
					 + " " + timeStamp.getHours() + ":" + minutes;
		return text;
	};

	/*++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/
	/*                ui init related stuff                   */
	/*++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/

	self._setupAboutPage = function() {
		$('#info_clientversion').text(self._m4eAppInfo.clientVersion);
		$('#info_viewversion').text(self._m4eAppInfo.viewVersion);
		self._m4e.getServerInfo({
			success: function(results, response) {
				if (results.status === "ok") {
					self._m4eAppInfo.serverVersion = results.data.version;
					$('#info_serverversion').text(self._m4eAppInfo.serverVersion);
				}
			}
		});
	};

	self._setupDashboardPage = function() {
		self._m4eEvents.getCount({
			success: function(results, response) {
				$('#count_events').text(results.data.count);
			}
		});
		self._m4eUsers.getCount({
			success: function(results, response) {
				$('#count_users').text(results.data.count);
			}
		});
	};

	self._getUserTable = function() {
		if (!self._m4eUserTable) {
			self._m4eUserTable = $('#table_users').DataTable({
				responsive: true,
				"columns": [
					{ "data": "name" },
					{ "data": "login" },
					{ "data": "creation" },
					{ "data": "lastlogin" },
					{ "data": "roles" },
					{ "data": "ops" }
				]});
		}
		return self._m4eUserTable;
	};

	self._getEventTable = function() {
		if (!self.m4eEventTable) {
			self.m4eEventTable = $('#table_events').DataTable({
				responsive: true,
				"columns": [
					{ "data": "name" },
					{ "data": "description" },
					{ "data": "eventStart" },
					{ "data": "eventRepeat" },
					{ "data": "ops" }
				]});
		}
		return self.m4eEventTable;
	};

	/*++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/
	/*                   auth related stuff                   */
	/*++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/

	self._setupUi = function() {
		self._m4eAuth.getAuthState({
			success: function(results, response) {
				if (results.status === "ok") {
					if (results.data.auth === "yes") {
						self._showElement('main_content', true);
						self._showElement('main_login', false);
						// get some info on logged-in user
						self._deferExecution(function () {
							self._m4eUsers.find({
								success: function(res, resp) {
									if (res.status === "ok") {
										self._m4eAuthUser.auth = 'yes';
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
		self._m4eAuth.login({
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
		self._m4eAuth.logout({
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

	//--------------------------------------------
	self._onAuthenticated = function() {
		$('#user_name').text(self._getAuthUser().name);
		self._setupDashboardPage();
		// this initialized the tables
		self._getUserTable();
		self._getEventTable();
	};

	/*++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/
	/*                   admin related stuff                  */
	/*++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/

	self._setupUiAdmin = function() {
		self._m4e.getServerStats({
			success: function(results, reponse) {
				if (results.status !== "ok") {
					self.showModalBox("Cannot retrieve server stats!", "Communication Problem", "Dismiss");
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
			},
			error: function(err) {
				self.showModalBox("Cannot retrieve server stats! Reason: " + err, "Connection Problem", "Dismiss");
			}
		});
	};

	/*++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/
	/*                   user related stuff                   */
	/*++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/

	self._setupUiTableUser = function() {
		self._getUserTable().clear();
		self._m4eUsers.getAll({
			success: function(results, response) {
				if (results.status !== "ok") {
					return;
				}
				for (var i = 0; i < results.data.length; i++) {
					self._updateUiUserTableAdd(results.data[i]);
				}
				self._getUserTable().draw("full-reset");
			},
			error: function(err) {
				self.showModalBox(err, "Cannot retrieve users", "Dismiss");
			}
		});
	};

	self._setupUiUserNew = function() {
		$('#page_users_edit_title').text("Create a new user");
		$("#page_users_edit_form :input").val("");
		$('#page_users_edit_form_login').prop("disabled", false);
	};

	self._updateUiUserTableAdd = function(userFields) {
		if (userFields.id === "") {
			self.showModalBox("updateUiUserTableNew: Cannot add user, invalid user id!", "Internal Error", "Dismiss");
			return;
		}
		var lastlogin = "-";
		var datecreation = "-";
		if (userFields.dateLastLogin && userFields.dateLastLogin > 0) {
			var timestamp = new Date(parseInt(userFields.dateLastLogin));
			lastlogin = self._formatTime(timestamp);
		}
		if (userFields.dateCreation && userFields.dateCreation > 0) {
			var timestamp = new Date(parseInt(userFields.dateCreation));
			datecreation = self._formatTime(timestamp);
		}
		var roles = userFields.roles ? userFields.roles.join("<br>") : "";
		var me = (""+userFields.id === ""+self._getAuthUser().id);
		var candelete = self._authUserRolesContain(["ADMIN"]) && !me;
		var canedit = self._authUserRolesContain(["ADMIN"]) || me;
		self._getUserTable().row.add({
				"DT_RowId" : userFields.id,
				// make 'me' bold
				"name" : (me ? "<strong>" : "") + userFields.name + (me ? "</strong>" : ""),
				"login" : userFields.login,
				"creation" : datecreation,
				"lastlogin" : lastlogin,
				"roles": roles,
				"ops" :	(candelete ? "<a role='button' onclick='getMeet4EatUI().onBtnUserDelete(\"" + userFields.id + "\")'>DELETE</a> | " : "") +
						(canedit ? "<a role='button' onclick='getMeet4EatUI().onBtnUserEdit(\"" + userFields.id + "\")'>EDIT</a>" : "")
			});
	};

	self._updateUiUserTableUpdate = function(userFields) {
		if (userFields.id === "") {
			self.showModalBox("updateUiUserTableUpdate Cannot update user, invalid user id!", "Internal Error", "Dismiss");
			return;
		}
		var user = self._getUserTable().row('#' + userFields.id);
		if (user) {
			var cols = user.data();
			if (userFields.name) {
				cols.name = userFields.name;
			}
			if (userFields.login) {
				cols.login = userFields.login;
			}
			if (userFields.email) {
				cols.email = userFields.email;
			}
			if (userFields.roles) {
				cols.roles = userFields.roles.join("<br>");
			}
			user.data(cols);
			user.draw("page");
		}
	};

	self._updateUiUserTableRemove = function(id) {
		var user = self._getUserTable().row('#' + id);
		if (user) {
			user.remove().draw(true);
		}
	};

	self._setupUiUserEdit = function(userId) {
		$('#page_users_edit_title').text("Edit existing user");
		$('#page_users_edit_form_login').prop("disabled", true);
		$("#page_users_edit_form input[name='password']").val("");
		$("#page_users_edit_form input[name='password-repeat']").val("");

		var showpasswdinput = (self._authUserRolesContain(["ADMIN"]) || ((""+self._getAuthUser().id) === (""+userId)));
		self._showElement('page_users_edit_form_grp_passwd', showpasswdinput);
		self._showElement('page_users_edit_form_grp_passwd_repeat', showpasswdinput);

		$('#page_users_edit_roles_sel').empty();

		// fetch user details from server
		self._m4eUsers.find({
			success: function(results, response) {
				if (results.status !== "ok") {
					self.showModalBox("User was not found for edit!", "Error", "Dismiss");
					return;
				}
				var user = results.data;
				$("#page_users_edit_form input[name='id']").val(user.id);
				$("#page_users_edit_form input[name='login']").val(user.login);
				$("#page_users_edit_form input[name='name']").val(user.name);
				$("#page_users_edit_form input[name='email']").val(user.email);

				// setup the available roles
				var elemrolesel = $('#page_users_edit_roles_sel');
				elemrolesel.append(new Option("ADMIN", "ADMIN"));
				elemrolesel.append(new Option("MODERATOR", "MODERATOR"));
				elemrolesel.append(new Option("", ""));
				elemrolesel.prop("disabled", !self._authUserRolesContain(["ADMIN"]));
				// select user roles in list
				elemrolesel.val(user.roles);
			},
			error: function(err) {
				self.showModalBox(err, "Connection Error", "Dismiss");
			}
		}, userId);
	};

	self._createOrUpdateUser = function(fields, resultsCallback) {
		self._m4eUsers.createOrUpdate({
			success: function(results) {
				if (resultsCallback) {
					resultsCallback(results);
				}
			},
			error: function(text, response) {
				$('#display_error').html(response);
				self.showModalBox("Could not create or update user! Reason: " + text, "Connection Error", "Dismiss");
			}					
		}, fields.id, fields);
	};

	self._deleteUser = function(id) {
		self._m4eUsers.delete({
			success: function(results) {
				if (results.status !== "ok") {
					self.showModalBox("Could not delete user! Reason: " + results.description, "Problem Deleting User", "Dismiss");
					return;
				}
				self.showModalBox("User was successfully removed", "Delete User", "Dismiss");
				self._updateUiUserTableRemove(id);
				self._showElement('page_users_edit', false);
				self._showElement('menu_users', true);
			},
			error: function(text, response) {
				self.showModalBox(text, "Problem Deleting User", "Dismiss");
				$('#display_error').html(response);
			}
		}, id);
	};

	/*++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/
	/*                   event related stuff                  */
	/*++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/

	self._setupUiTableEvent = function() {
		self._getEventTable().clear();
		self._m4eEvents.getAll({
			success: function(results, response) {
				if (results.status !== "ok") {
					return;
				}
				for (var i = 0; i < results.data.length; i++) {
					self._updateUiEventTableAdd(results.data[i]);
				}
				self._getEventTable().draw("full-reset");
			},
			error: function(err) {
				self.showModalBox(err, "Cannot retrieve events", "Dismiss");
			}
		});
	};

	self._setupEventTimeInputs = function() {
		if (self._eventEventDatePicker) {
			self._eventEventDatePicker.destroy();
		}
		self._eventEventDatePicker = $('#page_events_edit_form_eventstart').datetimepicker({
			format : 'YYYY-MM-DD HH:mm',
			stepping: 1,
			defaultDate: 'moment',
			showTodayButton: true
		}).data("DateTimePicker");

		if (self._eventEventDayTimePicker) {
			self._eventEventDayTimePicker.destroy();
		}
		self._eventEventDayTimePicker = $('#page_events_edit_form_daytime').datetimepicker({
			format : 'HH:mm',
			stepping: 1,
			enabledDates: false,
			defaultDate: false
		}).data("DateTimePicker");		
	};

	self._getEventWeekDays = function() {
		var days = 0;
		for (var i = 0; i < 7; i++) {
			var checked = $('#page_events_edit_form_weekday' + i).prop('checked');
			if (checked) {
				days |= (1<<i);
			}
		}
		return days;
	};

	self._setEventWeekDays = function(days) {
		for (var i = 0; i < 7; i++) {
			var state = (days & (1<<i)) > 0;
			$('#page_events_edit_form_weekday' + i).prop('checked', state);
		}
	};

	self._updateUiEventTableAdd = function(eventFields) {
		if (eventFields.id === "") {
			self.showModalBox("updateUiEventTableAdd: Cannot add event, invalid user id!", "Internal Error", "Dismiss");
			return;
		}
		var eventstart = "-";
		var eventrepeat = "No";
		if (eventFields.eventStart && parseInt(eventFields.eventStart) > 0) {
			var timestamp = new Date(parseInt(eventFields.eventStart * 1000));
			eventstart = self._formatTime(timestamp);
		}
		if (eventFields.repeatWeekDays && eventFields.repeatWeekDays > 0) {
			eventrepeat = "Yes";
		}
		var desc = eventFields.description;
		if (desc.length > 32) {
			desc = desc.substring(0, 32);
			desc += "...";
		}
		var me = (""+eventFields.ownerId === ""+self._getAuthUser().id);
		var candelete = self._authUserRolesContain(["ADMIN"]) || me;
		var canedit = self._authUserRolesContain(["ADMIN"]) || me;
		self._getEventTable().row.add({
				"DT_RowId" : eventFields.id,
				// make 'me' bold
				"name" : (me ? "<strong>" : "") + eventFields.name + (me ? "</strong>" : ""),
				"description" : desc,
				"eventStart" : eventstart,
				"eventRepeat" : eventrepeat,
				"ops" :	(candelete ? "<a role='button' onclick='getMeet4EatUI().onBtnEventDelete(\"" + eventFields.id + "\")'>DELETE</a> | " : "") +
						(canedit ? "<a role='button' onclick='getMeet4EatUI().onBtnEventEdit(\"" + eventFields.id + "\")'>EDIT</a>" : "")
			});
	};

	self._updateUiEventMembersClear = function() {
		$('#page_events_edit_members').empty();
		$('#page_events_edit_form_mem_search').val('');
		$('#page_events_edit_form_mem_search_hits').empty();
	};

	self._updateUiEventMemberAdd = function(allowModification, eventId, memberId, memberName) {
		var members = $('#page_events_edit_members');
		var liid = "page_events_edit_members-" + eventId + "-" + memberId;
		var argsinfo = "(" + memberId + ")";
		var argsremove = "(" + eventId + "," + memberId + ")";
		var argsmakeowner = argsremove;
		var ops = "";
		if (allowModification) {
			ops = "<a onclick='getMeet4EatUI().onBtnEventMemberRemove" + argsremove + ";'>" +
				"<span class='btn fa fa-remove fa-2x text-danger pull-right' title='Remove member'> </span></a>"+
				"<a onclick='getMeet4EatUI().onBtnEventMemberMakeOwner" + argsmakeowner + ";'>" +
				"<span class='btn fa fa-graduation-cap fa-2x text-primary pull-right' title='Make event owner'> </span></a>";
		}
		var li = "<li id='"+ liid + "' class='list-group-item'>" +
				 "<a class='btn' onclick='getMeet4EatUI().onBtnEventMemberInfo" + argsinfo + ";'>" + memberName + "</a>" + ops + "</li>";
		members.append(li);
	};

	self._updateUiEventMemberRemove = function(eventId, memberId) {
		var liid = "page_events_edit_members-" + eventId + "-" + memberId;
		$('#' + liid).remove();
	};

	self._setupUiEventNew = function() {
		$('#page_events_edit_title').text("Create a new event");
		$("#page_events_edit_form :input").val("");
		self._showElement('page_nav_events_edit', false);
		self._setupEventTimeInputs();
	};

	self._updateUiEventTableUpdate = function(eventFields) {
		if (eventFields.id === "") {
			self.showModalBox("updateUiEventTableUpdate Cannot update event, invalid event id!", "Internal Error", "Dismiss");
			return;
		}
		var event = self._getEventTable().row('#' + eventFields.id);
		if (event) {
			var cols = event.data();
			if (eventFields.name) {
				cols.name = eventFields.name;
			}
			if (eventFields.description) {
				var desc = eventFields.description;
				if (desc.length > 32) {
					desc = desc.substring(0, 32);
					desc += "...";
				}
				cols.description = desc;
			}
			if (eventFields.eventStart) {
				var timestamp = new Date(parseInt(eventFields.eventStart * 1000));
				cols.eventStart = self._formatTime(timestamp);
			}
			cols.eventRepeat = (eventFields.repeatWeekDays > 0) ? "Yes" : "No";

			event.data(cols);
			event.draw("page");
		}
	};

	self._updateUiEventTableRemove = function(id) {
		var event = self._getEventTable().row('#' + id);
		if (event) {
			event.remove().draw(true);
		}
	};

	self._setupUiEventEdit = function(id) {
		$('#page_events_edit_title').text("Edit existing event");
		self._showElement('page_nav_events_edit', true);
		self._setupEventTimeInputs();
		self._updateUiEventMembersClear();
		// fetch event details from server
		self._m4eEvents.find({
			success: function(results, response) {
				if (results.status !== "ok") {
					self.showModalBox("Event was not found for edit!", "Error", "Dismiss");
					return;
				}
				var ev = results.data;
				$("#page_events_edit_form input[name='id']").val(ev.id);
				$("#page_events_edit_form input[name='name']").val(ev.name);
				$("#page_events_edit_form textarea[name='description']").val(ev.description);
				//! NOTE eventStart is in seconds!
				if (ev.eventStart && ev.eventStart > 0) {
					var date = new Date(ev.eventStart * 1000);
					date = moment(date);
					self._eventEventDatePicker.date(date);
				}
				//! NOTE repeatDayTime is in seconds!
				if (ev.repeatDayTime && ev.repeatDayTime > 0) {
					var date = new Date(ev.repeatDayTime * 1000);
					date = moment(date);
					self._eventEventDayTimePicker.date(date);
				}
				self._setEventWeekDays(ev.repeatWeekDays);
				var modperms = (""+ev.ownerId === ""+self._getAuthUser().id) ||
						        self._authUserRolesContain(["ADMIN"]);
				for (var i = 0; i < ev.members.length; i++) {
					self._updateUiEventMemberAdd(modperms, ev.id, ev.members[i].id, ev.members[i].name);
				}
				self._setupUiEventOwner(ev.ownerId, ev.ownerName);
			},
			error: function(err) {
				self.showModalBox(err, "Connection Error", "Dismiss");
			}
		}, id);
	};

	self._setupUiEventOwner = function(ownerId, ownerName) {
		var argsinfo = "(" + ownerId + ")";
		var html = "<a class='btn' onclick='getMeet4EatUI().onBtnEventMemberInfo" + argsinfo + ";'>" + ownerName + "</a>";
		$('#page_events_edit_owner').html(html);
	};

	self._createOrUpdateEvent = function(fields, resultsCallback) {
		self._m4eEvents.createOrUpdate({
			success: function(results) {
				if (resultsCallback) {
					resultsCallback(results);
				}
			},
			error: function(text, response) {
				$('#display_error').html(response);
				self.showModalBox("Could not create or update event! Reason: " + text, "Connection Error", "Dismiss");
			}					
		}, fields.id, fields);
	};

	self._deleteEvent = function(id) {
		self._m4eEvents.delete({
			success: function(results) {
				if (results.status !== "ok") {
					self.showModalBox("Could not delete event! Reason: " + results.description, "Problem Deleting Event", "Dismiss");
					return;
				}
				self.showModalBox("Event was successfully removed", "Delete Event", "Dismiss");
				self._updateUiEventTableRemove(id);
				self._showElement('page_events_edit', false);
				self._showElement('menu_events', true);
			},
			error: function(text, response) {
				self.showModalBox(text, "Problem Deleting Event", "Dismiss");
				$('#display_error').html(response);
			}
		}, id);
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
		self._showElement("page_users_new", self._authUserRolesContain(["ADMIN"]));
		self._setupUiTableUser();

	};

	self.onLeavePageUsers = function() {
		self._showElement("page_users_edit", false);
	};

	self.onEnterPageEvents = function() {
		self._setupUiTableEvent();
	};

	self.onLeavePageEvents = function() {
		self._showElement("page_events_edit", false);
	};

	self.onEnterPageSystem = function() {
		if (self._authUserRolesContain(["ADMIN"])) {
			self._setupUiAdmin();
		}
	};

	self.onLeavePageSystem = function() {
	};

	self.onEnterPageAbout = function() {
		self._setupAboutPage();
	};

	self.onLeavePageAbout = function() {	
	};
}