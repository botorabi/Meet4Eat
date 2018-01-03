/**
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
"use strict";

/**
 * Meet4Eat Admin Panel UI Control - User Module
 * 
 * Dependencies:
 *   - jQuery
 *   - Meet4Eat-REST.js
 *   - Meet4Eat-UI.js
 *   - This modules assumes existing UI elements with proper IDs in HTML.
 * 
 * Usage:
 *   This module is used by Meet4Eat-UI
 * 
 * @param baseModule The base module Meet4EatUI which is extended by this module
 * 
 * @author boto
 * Date of creation Sep 15, 2017
 */
function Meet4EatUI_User(baseModule) {

	/* self points to base module */
	var base = baseModule;

	/* self pointer */
	var self = this;

	/* UI version */
	self._version = "1.1.0";

	/* User photo data */
	self._userPhotoData = null;

	/* Default user photo used when the user has no photo */
	self._userPhotoDefault = "public/images/user-default.png";

	/**
	 * Initialize the Meet4Eat UI User module.
	 */
	self.initialize = function() {
		// this initialized the user table
		self._getUserTable();
	};

	/**
	 * Setup the user module.
	 */
	base.setupUiModuleUser = function() {
		self._getUserTable().clear();
		self._userPhotoData = null;
		base._m4eRESTUsers.getAll({
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
				base.showModalBox(err, "Cannot retrieve users", "Dismiss");
			}
		});
	};

	/**
	 * Delete user with given ID.
	 * 
	 * @param {integer} id	User ID
	 */
	base.onBtnUserDelete = function(id) {
		base.showModalBox("Are you really sure you want to delete the user?", "Delete User", "No", "Yes", {
			onClickBtn1: function() {
				// nothing to do
			},
			onClickBtn2: function() {
				// sure to delete
				// NOTE as deleteUser opens also a modal messagebox we have to defer the call!
				base._deferExecution(function() {
					self._deleteUser(id);
				}, 500);
			}
		});
	};

	/**
	 * Create a new user.
	 */
	base.onBtnUserNew = function() {
		base._showElement('menu_users', false);
		base._showElement('page_users_edit', true);
		self._setupUiUserNew();
	};

	/**
	 * Edit user with given ID.
	 * 
	 * @param {integer} id User ID
	 */
	base.onBtnUserEdit = function(id) {
		base._showElement('menu_users', false);
		base._showElement('page_users_edit', true);
		self._setupUiUserEdit(id);
	};

	/**
	 * Apply user settings changes. This is used on "edit user" and "new user".
	 */
	base.onBtnUserEditApply = function() {
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
				base.showModalBox("Password fields do not match!", "Invalid Input", "Dismiss");
				return;
			}
			fields['password'] = base._m4eRESTAuth.createHash(passwd);
		}
		var roles = [];
		var selroles = $('#page_users_edit_roles_sel option:selected');
		selroles.each(function(index, item){
			var r = $(item);
			roles.push(r.text());
		});
		fields['roles'] = roles;
		if (self._userPhotoData) {
			fields['photo'] = self._userPhotoData;
		}
		fields['status'] = "";
		self._createOrUpdateUser(fields, function(results) {
			if (results.status === "ok") {
				base.showModalBox("Changes were successfully applied to user.", "User Update", "Dismiss");
				if (newuser) {
					// on successful creation the new user id is in results.data
					fields.id = results.data.id;
					self._updateUiUserTableAdd(fields);
					self._getUserTable().draw("full-hold");
				}
				else {
					self._updateUiUserTableUpdate(fields);
				}
				base._showElement('menu_users', true);
				base._showElement('page_users_edit', false);
				// are we editting our own settings?
				if ((""+base._getAuthUser().id) === (""+fields.id)) {
					base._getAuthUser().name = inputfields.name;
					$('#user_name').text(inputfields.name);
				}
			}
			else {
				base.showModalBox(results.description, "Failed to Update User", "Dismiss");
			}
		});
	};

	/**
	 * Cancel user edit ui.
	 */
	base.onBtnUserEditCancel = function() {
		base.onBtnUserClearForm();
		base._showElement('page_users_edit', false);
		base._showElement('menu_users', true);
	};

	/**
	 * Clear the user edit form.
	 */
	base.onBtnUserClearForm = function() {
		$("#page_users_edit_form :input").val("");
	};

	/**
	 * Load and create a new image for a user.
	 * 
	 * @param event The event which was created on an file input button.
	 */
	base.onBtnUserPhoto = function(event) {
		base.getImageModule().loadImageFile(event, {
			success: function(image) {
				$('#page_users_edit_photo_img').prop('src', image.src);
				// update _userPhotoDate so on next apply the new icon data will be transmitted, too
				self._userPhotoData = image.src;
			},
			error: function(errorString) {
				base.showModalBox(errorString, "Problem Loading Image File", "Dismiss");
			}	
		});
	};

	/**********************************************************************/
	/*                        Private functions                           */
	/**********************************************************************/

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

	self._setupUiUserNew = function() {
		$('#page_users_edit_title').text("Create a new user");
		$("#page_users_edit_form :input").val("");
		$('#page_users_edit_form_login').prop("disabled", false);
		$('#page_users_edit_form_email').prop("disabled", false);
	};

	self._updateUiUserTableAdd = function(userFields) {
		if (userFields.id === "") {
			base.showModalBox("updateUiUserTableAdd: Cannot add user, invalid user id!", "Internal Error", "Dismiss");
			return;
		}
		var lastlogin = "-";
		var datecreation = "-";
		if (userFields.dateLastLogin && userFields.dateLastLogin > 0) {
			var timestamp = new Date(parseInt(userFields.dateLastLogin));
			lastlogin = base._formatTime(timestamp);
		}
		if (userFields.dateCreation && userFields.dateCreation > 0) {
			var timestamp = new Date(parseInt(userFields.dateCreation));
			datecreation = base._formatTime(timestamp);
		}
		var roles = userFields.roles ? userFields.roles.join("<br>") : "";
		var me = (""+userFields.id === ""+base._getAuthUser().id);
		var candelete = base._userIsAdmin() && !me;
		var canedit = base._userIsAdmin() || me;
		self._getUserTable().row.add({
				"DT_RowId" : userFields.id,
				// make 'me' bold
				"name" : (me ? "<strong>" : "") + userFields.name + (me ? "</strong>" : "") +
						 ((userFields.status === "online") ? " <a role='button disable'>[online]</a>" : ""),
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
			base.showModalBox("updateUiUserTableUpdate Cannot update user, invalid user id!", "Internal Error", "Dismiss");
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
		$('#page_users_edit_form_email').prop("disabled", true);
		$("#page_users_edit_form input[name='password']").val("");
		$("#page_users_edit_form input[name='password-repeat']").val("");

		var showpasswdinput = (base._userIsAdmin() || ((""+base._getAuthUser().id) === (""+userId)));
		base._showElement('page_users_edit_form_grp_passwd', showpasswdinput);
		base._showElement('page_users_edit_form_grp_passwd_repeat', showpasswdinput);

		$('#page_users_edit_roles_sel').empty();

		// fetch user details from server
		base._m4eRESTUsers.find({
			success: function(results, response) {
				if (results.status !== "ok") {
					base.showModalBox("User was not found for edit!", "Error", "Dismiss");
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
				elemrolesel.prop("disabled", !base._userIsAdmin());
				// select user roles in list
				elemrolesel.val(user.roles);
				// setup the photo
				if (user.photoId) {
					self._loadUserPhoto(user.photoId);
				}
				else {
					$("#page_users_edit_photo_img").prop('src', self._userPhotoDefault);
				}
			},
			error: function(err) {
				base.showModalBox(err, "Connection Error", "Dismiss");
			}
		}, userId);
	};

	self._createOrUpdateUser = function(fields, resultsCallback) {
		base._m4eRESTUsers.createOrUpdate({
			success: function(results) {
				if (resultsCallback) {
					resultsCallback(results);
				}
			},
			error: function(text, response) {
				base.displayErrorHTML(response);
				base.showModalBox("Could not create or update user! Reason: " + text, "Connection Error", "Dismiss");
			}					
		}, fields.id, fields);
	};

	self._deleteUser = function(id) {
		base._m4eRESTUsers.delete({
			success: function(results) {
				if (results.status !== "ok") {
					base.showModalBox("Could not delete user! Reason: " + results.description, "Problem Deleting User", "Dismiss");
					return;
				}
				base.showModalBox("User was successfully removed", "Delete User", "Dismiss");
				self._updateUiUserTableRemove(id);
				base._showElement('page_users_edit', false);
				base._showElement('menu_users', true);
			},
			error: function(text, response) {
				base.showModalBox(text, "Problem Deleting User", "Dismiss");
				base.displayErrorHTML(response);
			}
		}, id);
	};

	self._loadUserPhoto = function(photoId) {
		base.getImageModule().loadImageFromServer('page_users_edit_photo_img', photoId);
	};
}