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
	self._version = "0.3.0";
	
	self.m4e = null;
	self.m4eAuth = null;
	self.m4eAuthUser = {'auth': 'no', 'id' : 0, 'login': '', 'name' : '', 'roles' : [] };
	self.m4eUsers = null;
	self.m4eGroups = null;
	self.m4eAppInfo = {'clientVersion' : '0.0.0', 'serverVersion' : '0.0.0', 'viewVersion' : '0'};
	self.m4eUserTable = null;

	/**
	 * Initialize the Meet4Eat UI.
	 */
	self.inititialize = function() {
		self.m4e = new Meet4EatREST();
		self.m4eAppInfo.clientVersion = self.m4e.getVersion();
		self.m4eAppInfo.viewVersion = self._version;
		self.m4eAuth = self.m4e.buildUserAuthREST();
		self.m4eUsers = self.m4e.buildUserREST();
		self.m4eGroups = self.m4e.buildGroupREST();
		self.setupUi();
	};

	/**
	 * Get the web interface version.
	 * @returns {string} Version of this web interface.
	 */
	self.getVersion = function() {
		return self._version;
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
	 * @param {type} text		    Messagebox text
	 * @param {type} title		    Title
	 * @param {type} textBtn1	    Text of button 1, pass null to hide button 1
	 * @param {type} textBtn2       Text of button 2, pass null to hide button 2
	 * @param {type} btnCallbacks	Optional callback object used on button clicks.
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
	 * Show/hide an element given its ID.
	 * 
	 * @param {string} elemId   Element ID
	 * @param {bool} show  Pass true for showing and false for hiding element
	 */
	self.showElement = function(elemId, show) {
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
	self.deferExecution = function(fcn, delay) {
		setTimeout(fcn, delay);
	};

	/**
	 * Given a time stamp format it to a string.
	 * 
	 * @param {Date} timeStamp Time stamp to be formatted
	 * @returns {string} Formatted time string
	 */
	self.formatTime = function(timeStamp) {
		var minutes = "" + timeStamp.getMinutes();
		minutes = minutes < 10 ? ("0" + minutes) : minutes;
		var text = timeStamp.getFullYear() + "-" + (timeStamp.getMonth()+1) + "-" + timeStamp.getDate()  
					 + " " + timeStamp.getHours() + ":" + minutes;
		return text;
	};

	/*++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/
	/*                ui init related stuff                   */
	/*++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/

	self.setupAboutPage = function() {
		$('#info_clientversion').text(self.m4eAppInfo.clientVersion);
		$('#info_viewversion').text(self.m4eAppInfo.viewVersion);
		self.m4e.getServerInfo({
			success: function(results, response) {
				if (results.status === "ok") {
					self.m4eAppInfo.serverVersion = results.data.version;
					$('#info_serverversion').text(self.m4eAppInfo.serverVersion);
				}
			}
		});
	};

	self.setupDashboardPage = function() {
		self.m4eGroups.getCount({
			success: function(results, response) {
				$('#count_groups').text(results.data.count);
			}
		});
		self.m4eUsers.getCount({
			success: function(results, response) {
				$('#count_users').text(results.data.count);
			}
		});
		self.m4eUserTable = $('#table_users').DataTable({
			responsive: true,
			"columns": [
				{ "data": "name" },
				{ "data": "login" },
				{ "data": "creation" },
				{ "data": "lastlogin" },
				{ "data": "roles" },
				{ "data": "ops" }
			]});

		$('#table_groups').DataTable({
			responsive: true
		});
	};

	self.getUserTable = function() {
		return self.m4eUserTable;
	};

	/*++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/
	/*                   auth related stuff                   */
	/*++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/

	self.setupUi = function() {
		self.m4eAuth.getAuthState({
			success: function(results, response) {
				if (results.status === "ok") {
					if (results.data.auth === "yes") {
						self.showElement('main_content', true);
						self.showElement('main_login', false);
						// get some info on logged-in user
						self.deferExecution(function () {
							self.m4eUsers.find({
								success: function(res, resp) {
									if (res.status === "ok") {
										self.m4eAuthUser.auth = 'yes';
										self.m4eAuthUser.login = res.data.login;
										self.m4eAuthUser.name = res.data.name;
										self.m4eAuthUser.id = res.data.id;
										self.m4eAuthUser.roles = res.data.roles;
										self.onAuthenticated();
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
						self.showElement('main_login', true);
						self.showElement('main_content', false);
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

	self.login = function(loginName, pasword) {
		$("#form_login").removeClass("has-error");
		self.m4eAuth.login({
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

	self.logout = function() {
		self.m4eAuth.logout({
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
	self.getAuthUser = function() {
		return self.m4eAuthUser;
	};

	/**
	 * Check if at least one of the given roles is contained in roles of
	 * authenticated user.
	 * 
	 * @param  roles    Roles to check, a string array
	 * @returns {bool}  Return true if at least one matching role was found, otherwise false.
	 */
	self.authUserRolesContain = function(roles) {
		for (var i= 0; i < roles.length; i++) {
			if ($.inArray(roles[i], self.getAuthUser().roles) > -1) {
				return true;
			}
		}
		return false;
	};

	//--------------------------------------------
	self.onAuthenticated = function() {
		if (self.authUserRolesContain(["ADMIN"])) {
			self.setupUiAdmin();
		}
		$('#user_name').text(self.getAuthUser().name);
		self.setupDashboardPage();
		self.setupAboutPage();
		self.setupUiTableUser();
	};

	self.onBtnLoginClicked = function() {
		var fields = $('#form_login').serializeArray().reduce(function(obj, item) {
			obj[item.name] = item.value;
			return obj;
		}, {});
		self.login(fields.login, fields.password);
		$("#form_login :input").val("");
	};

	/*++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/
	/*                   admin related stuff                  */
	/*++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/

	self.setupUiAdmin = function() {
		self.m4e.getServerStats({
			success: function(results, reponse) {
				if (results.status !== "ok") {
					self.showModalBox("Cannot retrieve server stats!", "Communication Problem", "Dismiss");
					return;
				}
				var stats = results.data;
				var maintenance = "-";
				if (stats.dateLastMaintenance) {
					maintenance = self.formatTime(new Date(parseInt(stats.dateLastMaintenance)));
				}
				var update = "-";
				if (stats.dateLastUpdate) {
					update = self.formatTime(new Date(parseInt(stats.dateLastUpdate)));
				}
				$('#sys_serverversion').text(stats.version);
				$('#sys_lastmaintenance').text(maintenance);
				$('#sys_lastupdate').text(update);
				$('#sys_countuserspurge').text(stats.userCountPurge);
				$('#sys_countgroupspurge').text(stats.groupCountPurge);
			},
			error: function(err) {
				self.showModalBox("Cannot retrieve server stats! Reason: " + err, "Connection Problem", "Dismiss");
			}
		});
	};

	/*++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/
	/*                   user related stuff                   */
	/*++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/
	self.setupUiTableUser = function() {
		self.getUserTable().clear();
		self.m4eUsers.getAll({
			success: function(results, response) {
				if (results.status !== "ok") {
					return;
				}
				for (var i = 0; i < results.data.length; i++) {
					self.updateUiUserTableAdd(results.data[i]);
				}
				self.getUserTable().draw("full-reset");
			},
			error: function(err) {
				self.showModalBox(err, "Cannot retrieve users", "Dismiss");
			}
		});
	};

	self.setupUiUserNew = function() {
		$('#page_users_edit_title').text("Create a new user");
		$("#page_users_edit_form :input").val("");
		$('#page_users_edit_form_login').prop("disabled", false);
	};

	self.updateUiUserTableAdd = function(userFields) {
		if (userFields.id === "") {
			self.showModalBox("updateUiUserTableNew: Cannot add user, invalid user id!", "Internal Error", "Dismiss");
			return;
		}
		var lastlogin = "-";
		var datecreation = "-";
		if (userFields.dateLastLogin && userFields.dateLastLogin > 0) {
			var timestamp = new Date(parseInt(userFields.dateLastLogin));
			lastlogin = self.formatTime(timestamp);
		}
		if (userFields.dateCreation && userFields.dateCreation > 0) {
			var timestamp = new Date(parseInt(userFields.dateCreation));
			datecreation = self.formatTime(timestamp);
		}
		var roles = userFields.roles ? userFields.roles.join("<br>") : "";
		var me = (""+userFields.id === ""+self.getAuthUser().id);
		var candelete = self.authUserRolesContain(["ADMIN", "MODERATOR"]) && !me;
		var canedit = self.authUserRolesContain(["ADMIN", "MODERATOR"]) || me;
		self.getUserTable().row.add({
				"DT_RowId" : userFields.id,
				// make 'me' bold
				"name" : (me ? "<strong>" : "") + userFields.name + (me ? "</strong>" : ""),
				"login" : userFields.login,
				"creation" : datecreation,
				"lastlogin" : lastlogin,
				"roles": roles,
				"ops" :	(candelete ? "<a role='button' onclick='getMeet4EatUI().onBtnDeleteUser(\"" + userFields.id + "\")'>DELETE</a> | " : "") +
						(canedit ? "<a role='button' onclick='getMeet4EatUI().onBtnEditUser(\"" + userFields.id + "\")'>EDIT</a>" : "")
			});
	};

	self.updateUiUserTableUpdate = function(userFields) {
		if (userFields.id === "") {
			self.showModalBox("updateUiUserTableUpdate Cannot update user, invalid user id!", "Internal Error", "Dismiss");
			return;
		}
		var user = self.getUserTable().row('#' + userFields.id);
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

	self.updateUiUserTableRemove = function(id) {
		var user = self.getUserTable().row('#' + id);
		if (user) {
			user.remove().draw(true);
		}
	};

	self.setupUiUserEdit = function(userId) {
		$('#page_users_edit_title').text("Edit existing user");
		$('#page_users_edit_form_login').prop("disabled", true);
		$("#page_users_edit_form input[name='password']").val("");
		$("#page_users_edit_form input[name='password-repeat']").val("");

		var showpasswdinput = (self.authUserRolesContain(["ADMIN"]) || ((""+self.getAuthUser().id) === (""+userId)));
		self.showElement('page_users_edit_form_grp_passwd', showpasswdinput);
		self.showElement('page_users_edit_form_grp_passwd_repeat', showpasswdinput);

		$('#page_users_edit_roles_sel').empty();

		// fetch user details from server
		self.m4eUsers.find({
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
				elemrolesel.prop("disabled", !self.authUserRolesContain(["ADMIN", "MODERATOR"]));
				// select user roles in list
				elemrolesel.val(user.roles);
			},
			error: function(err) {
				self.showModalBox(err, "Connection Error", "Dismiss");
			}
		}, userId);
	};

	self.createOrUpdateUser = function(fields, resultsCallback) {
		self.m4eUsers.createOrUpdate({
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

	self.deleteUser = function(id) {
		self.m4eUsers.delete({
			success: function(results) {
				if (results.status !== "ok") {
					self.showModalBox("Could not delete user! Reason: " + results.description, "Problem Deleting User", "Dismiss");
					return;
				}
				self.showModalBox("User was successfully removed", "Delete User", "Dismiss");
				self.updateUiUserTableRemove(id);
				self.showElement('page_users_edit', false);
				self.showElement('menu_users', true);
			},
			error: function(text, response) {
				self.showModalBox(text, "Problem Deleting User", "Dismiss");
				$('#display_error').html(response);
			}
		}, id);
	};

	self.onBtnDeleteUser = function(id) {
		self.showModalBox("Are you really sure, delete the user?", "Delete User", "No", "Yes", {
			onClickBtn1: function() {
				// nothing to do
			},
			onClickBtn2: function() {
				// sure to delete
				// NOTE as deleteUser opens also a modal messagebox we have to defer the call!
				self.deferExecution(function() {
					self.deleteUser(id);
				}, 500);
			}
		});
	};

	self.onBtnUserNew = function() {
		self.showElement('menu_users', false);
		self.showElement('page_users_edit', true);
		self.setupUiUserNew();
	};

	self.onBtnEditUser = function(id) {
		self.showElement('menu_users', false);
		self.showElement('page_users_edit', true);
		self.setupUiUserEdit(id);
	};

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
			fields['password'] = self.m4eAuth.createHash(passwd);
		}
		var roles = [];
		var selroles = $('#page_users_edit_roles_sel option:selected');
		selroles.each(function(index, item){
			var r = $(item);
			roles.push(r.text());
		});
		fields['roles'] = roles;

		self.createOrUpdateUser(fields, function(results) {
			if (results.status === "ok") {
				self.showModalBox("Changes were successfully applied to user.", "User Update", "Dismiss");
				if (newuser) {
					// on successful creation the new user id is in results.data
					fields.id = results.data.id;
					self.updateUiUserTableAdd(fields);
					self.getUserTable().draw("full-hold");
				}
				else {
					self.updateUiUserTableUpdate(fields);
				}
				self.showElement('menu_users', true);
				self.showElement('page_users_edit', false);
				// are we editting our own settings?
				if ((""+self.getAuthUser().id) === (""+fields.id)) {
					self.getAuthUser().name = inputfields.name;
					$('#user_name').text(inputfields.name);
				}
			}
			else {
				self.showModalBox("Could not apply changes to user! Reason: " + results.description, "User Update", "Dismiss");
			}
		});
	};

	self.onBtnUserEditCancel = function() {
		self.onBtnClearFormUser();
		self.showElement('page_users_edit', false);
		self.showElement('menu_users', true);
	};

	self.onBtnClearFormUser = function() {
		$("#page_users_edit_form :input").val("");
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
		switch(item) {
			case "dashboard":
				self.onInitPageDashboard();
				break;
			case "users":
				self.onInitPageUsers();
				break;
			case "groups":
				self.onInitPageGroups();
				break;
			case "system":
				self.onInitPageSystem();
				break;
			case "about":
				self.onInitPageAbout();
				break;
		}
	};

	self.onInitPageDashboard = function() {			
	};

	self.onInitPageUsers = function() {
		self.showElement("page_users_new", self.authUserRolesContain(["ADMIN"]));
	};

	self.onInitPageGroups = function() {
	};

	self.onInitPageSystem = function() {
	};

	self.onInitPageAbout = function() {	
	};
}