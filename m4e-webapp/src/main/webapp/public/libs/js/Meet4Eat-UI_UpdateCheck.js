/**
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
"use strict";

/**
 * Meet4Eat Admin Panel UI Control - Update Check Module
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
function Meet4EatUI_UpdateCheck(baseModule) {

	/* self points to base module */
	var base = baseModule;

	/* self pointer */
	var self = this;

	/* UI version */
	self._version = "1.0.0";

	/**
	 * Initialize the Meet4Eat UI module for update check.
	 */
	self.initialize = function() {
		// this initialized the udpate rule table
		self._getCheckEntryTable();
	};

	/**
	 * Setup the update check module.
	 */
	base.setupUiModuleUpdateCheck = function() {
		self._getCheckEntryTable().clear();
		base._m4eRESTUpdateCheck.getAll({
			success: function(results, response) {
				if (results.status !== "ok") {
					base.showModalBox("Cannot retrieve update entries. Reason: " + results.description, "Problem Getting Entries", "Dismiss");
					return;
				}
				for (var i = 0; i < results.data.length; i++) {
					self._updateUiUpdateCheckEntryTableAdd(results.data[i]);
				}
				self._getCheckEntryTable().draw("full-reset");
			},
			error: function(err) {
				base.showModalBox(err, "Cannot retrieve update entries", "Dismiss");
			}
		});
	};

	/**
	 * Delete update check entry with given ID.
	 * 
	 * @param {integer} id	Entry ID
	 */
	base.onBtnUpdateEntryDelete = function(id) {
		base.showModalBox("Are you really sure you want to delete the entry?", "Delete Update Entry", "No", "Yes", {
			onClickBtn1: function() {
				// nothing to do
			},
			onClickBtn2: function() {
				base._deferExecution(function() {
					self._deleteUpdateCheckEntry(id);
				}, 500);
			}
		});
	};

	/**
	 * Create a new update check entry.
	 */
	base.onBtnUpdateEntryNew = function() {
		self._setupUiCheckEntryNew();
		$('#dialog_updates_edit').modal("show");
	};

	/**
	 * Edit update check entry with given ID.
	 * 
	 * @param {integer} id Entry ID
	 */
	base.onBtnUpdateEntryEdit = function(id) {
		self._setupUiUpdateCheckEntryEdit(id);
		$('#dialog_updates_edit').modal("show");
	};

	/**
	 * Apply update check entry settings changes. This is used on "edit" and "new".
	 */
	base.onBtnUpdateEntryEditApply = function() {
		var inputfields = $('#page_update_edit_form').serializeArray().reduce(function(obj, item) {
			obj[item.name] = item.value;
			return obj;
		}, {});
		inputfields['active'] = $("#page_update_edit_form input[name='active']").prop('checked');

		var fields = {};
		var newentry = (inputfields.id === null) || (inputfields.id === "");
		fields['id'] = inputfields.id;
		fields['name'] = inputfields.name;
		fields['os'] = inputfields.os;
		fields['flavor'] = inputfields.flavor;
		fields['version'] = inputfields.version;
		fields['url'] = inputfields.url;
		fields['active'] = inputfields.active;
		self._createOrUpdateUpdateCheckEntry(fields, function(results) {
			if (results.status === "ok") {
				if (newentry) {
					fields.id = results.data.id;
					self._updateUiUpdateCheckEntryTableAdd(fields);
					self._getCheckEntryTable().draw("full-hold");
				}
				else {
					self._updateUiUpdateCheckEntryTableUpdate(fields);
				}
			}
			else {
				base.showModalBox(results.description, "Failed to Update Entry", "Dismiss");
			}
		});
	};

	/**
	 * Cancel update check entry edit ui.
	 */
	base.onBtnUpdateEntryEditCancel = function() {
		base.onBtnUpdateEntryClearForm();
	};

	/**
	 * Clear the update check entry edit form.
	 */
	base.onBtnUpdateEntryClearForm = function() {
		$("#page_update_edit_form :input").val("");
	};

	/**
	 * Perform an update check.
	 */
	base.onBtnUpdateEntryCheck = function() {
		var inputfields = $('#page_update_check_form').serializeArray().reduce(function(obj, item) {
			obj[item.name] = item.value;
			return obj;
		}, {});
		$('#update_check_results_version').text("");
		$('#update_check_results_url').text("");
		$('#update_check_results_releasedate').text("");
		base._m4eRESTUpdateCheck.performCheck({
			success: function(results, response) {
				if (results.status === "ok") {
					if (results.data.updateVersion === "") {
						base.showModalBox("There is no update for given client specs!", "Update Check Results", "Dismiss");
					}
					else {
						var entryFields = results.data;
						$('#update_check_results_version').text(entryFields.updateVersion);
						$('#update_check_results_url').text(entryFields.url);
						var releasedate = "-";
						if (entryFields.releaseDate && entryFields.releaseDate > 0) {
							var timestamp = new Date(parseInt(entryFields.releaseDate));
							releasedate = base._formatTime(timestamp);
						}
						$('#update_check_results_releasedate').text(releasedate);
					}
				}
				else {
					base.showModalBox(results.description, "Failed to Check for Update", "Dismiss");
				}
			},
			error: function(err) {
				base.showModalBox(err, "Cannot retrieve update check results", "Dismiss");
			}
		}, inputfields);
	};

	/**********************************************************************/
	/*                        Private functions                           */
	/**********************************************************************/

	self._getCheckEntryTable = function() {
		if (!self._m4eCheckEntryTable) {
			self._m4eCheckEntryTable = $('#table_updates').DataTable({
				responsive: true,
				"columns": [
					{ "data": "name" },
					{ "data": "os" },
					{ "data": "flavor" },
					{ "data": "version" },
					{ "data": "releasedate" },
					{ "data": "url" },
					{ "data": "active" },
					{ "data": "ops" }
				]});
		}
		return self._m4eCheckEntryTable;
	};

	self._setupUiCheckEntryNew = function() {
		base.onBtnUpdateEntryClearForm();
	};

	self._updateUiUpdateCheckEntryTableAdd = function(entryFields) {
		if (entryFields.id === "") {
			base.showModalBox("updateUiCheckEntryTableAdd: Cannot add update entry, invalid id!", "Internal Error", "Dismiss");
			return;
		}
		var releasedate = "-";
		if (entryFields.releaseDate && entryFields.releaseDate > 0) {
			var timestamp = new Date(parseInt(entryFields.releaseDate));
			releasedate = base._formatTime(timestamp);
		}
		var url = entryFields.url ? ("<a href='" + entryFields.url + "'>" + entryFields.url + "</a>") : "";
		self._getCheckEntryTable().row.add({
				"DT_RowId" : entryFields.id,
				"name" : entryFields.name,
				"os" : entryFields.os,
				"flavor" : (entryFields.flavor ? entryFields.flavor : ""),
				"version" : entryFields.version,
				"releasedate" : releasedate,
				"url" : url,
				"active" : (entryFields.active ? "Yes" : "No"),
				"ops" :	"<a role='button' onclick='getMeet4EatUI().onBtnUpdateEntryDelete(\"" + entryFields.id + "\")'>DELETE</a> | " +
						"<a role='button' onclick='getMeet4EatUI().onBtnUpdateEntryEdit(\"" + entryFields.id + "\")'>EDIT</a>"
			});
	};

	self._updateUiUpdateCheckEntryTableUpdate = function(entryFields) {
		if (entryFields.id === "") {
			base.showModalBox("updateUiUpdateCheckEntryTableUpdate Cannot update the entry, invalid id!", "Internal Error", "Dismiss");
			return;
		}		
		var entry = self._getCheckEntryTable().row('#' + entryFields.id);
		if (entry) {
			var cols = entry.data();
			if (entryFields.name) {
				cols.name = entryFields.name;
			}
			if (entryFields.os) {
				cols.os = entryFields.os;
			}
			if (entryFields.flavor) {
				cols.flavor = entryFields.flavor;
			}
			if (entryFields.version) {
				cols.version = entryFields.version;
			}
			if (entryFields.url) {
				var url = entryFields.url ? ("<a href='" + entryFields.url + "'>" + entryFields.url + "</a>") : "";
				cols.url = url;
			}
			cols.active = (entryFields.active ? "Yes" : "No");

			entry.data(cols);
			entry.draw("page");
		}
	};

	self._updateUiUpdateCheckEntryTableRemove = function(id) {
		var entry = self._getCheckEntryTable().row('#' + id);
		if (entry) {
			entry.remove().draw(true);
		}
	};

	self._setupUiUpdateCheckEntryEdit = function(entryId) {
		base.onBtnUpdateEntryClearForm();
		base._m4eRESTUpdateCheck.find({
			success: function(results, response) {
				if (results.status !== "ok") {
					base.showModalBox("Entry was not found for edit!", "Error", "Dismiss");
					return;
				}
				var entry = results.data;
				$("#page_update_edit_form input[name='id']").val(entry.id);
				$("#page_update_edit_form input[name='name']").val(entry.name);
				$("#page_update_edit_form input[name='os']").val(entry.os);
				$("#page_update_edit_form input[name='flavor']").val(entry.flavor);
				$("#page_update_edit_form input[name='version']").val(entry.version);
				$("#page_update_edit_form input[name='url']").val(entry.url);
				$("#page_update_edit_form input[name='active']").prop('checked', entry.active);
				$('#dialog_updates_edit').modal("show");
			},
			error: function(err) {
				base.showModalBox(err, "Connection Error", "Dismiss");
			}
		}, entryId);
	};

	self._createOrUpdateUpdateCheckEntry = function(fields, resultsCallback) {
		base._m4eRESTUpdateCheck.createOrUpdate({
			success: function(results) {
				if (resultsCallback) {
					resultsCallback(results);
				}
			},
			error: function(text, response) {
				base.displayErrorHTML(response);
				base.showModalBox("Could not create or update the client update entry! Reason: " + text, "Connection Error", "Dismiss");
			}					
		}, fields.id, fields);
	};

	self._deleteUpdateCheckEntry = function(id) {
		base._m4eRESTUpdateCheck.delete({
			success: function(results) {
				if (results.status !== "ok") {
					base.showModalBox("Could not delete client update entry! Reason: " + results.description, "Problem Deleting Update Entry", "Dismiss");
					return;
				}
				base.showModalBox("Update entry was successfully removed", "Delete Update Entry", "Dismiss");
				self._updateUiUpdateCheckEntryTableRemove(id);
			},
			error: function(text, response) {
				base.showModalBox(text, "Problem Deleting Update Entry", "Dismiss");
				base.displayErrorHTML(response);
			}
		}, id);
	};
}