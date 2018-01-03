/**
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
"use strict";

/**
 * Meet4Eat Admin Panel UI Control - Event Module
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
function Meet4EatUI_Event(baseModule) {

	/* self points to base module */
	var base = baseModule;

	/* self pointer */
	var self = this;

	/* Event photo data */
	self._eventPhotoData = null;

	/* Default event photo used when the event has no photo */
	self._eventPhotoDefault = "public/images/event-default.png";

	/* Event location photo data */
	self._eventLocationPhotoData = null;

	/* Default event location photo used when the event has no photo */
	self._eventLocationPhotoDefault = "public/images/eventlocation-default.png";

	/**
	 * Initialize the Meet4Eat UI Event module.
	 */
	self.initialize = function() {
		// this initialized the event table
		self._getEventTable();
	};

	/**
	 * Setup the event module.
	 */
	base.setupUiModuleEvent = function() {
		self._getEventTable().clear();
		self._eventPhotoData = null;
		self._eventLocationPhotoData = null;
		base._m4eRESTEvents.getAll({
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
				base.showModalBox(err, "Cannot retrieve events", "Dismiss");
			}
		});
	};

	/**
	 * Delete the event with given ID.
	 * 
	 * @param {interger} id Event ID
	 */
	base.onBtnEventDelete = function(id) {
		base.showModalBox("Are you really sure you want to delete the event?", "Delete Event", "No", "Yes", {
			onClickBtn1: function() {
				// nothing to do
			},
			onClickBtn2: function() {
				// sure to delete
				// NOTE as deleteUser opens also a modal messagebox we have to defer the call!
				base._deferExecution(function() {
					self._deleteEvent(id);
				}, 500);
			}
		});
	};

	/**
	 * Create a new event.
	 */
	base.onBtnEventNew = function() {
		base._showElement('menu_events', false);
		base._showElement('page_events_edit', true);
		self._setupUiEventNew();
	};

	/**
	 * Edit the event with given ID.
	 * 
	 * @param {integer} id Event ID
	 */
	base.onBtnEventEdit = function(id) {
		base._showElement('menu_events', false);
		base._showElement('page_events_edit', true);
		self._setupUiEventEdit(id);
	};

	/**
	 * Apply event settings changes. This is used on "edit event" and "new event".
	 */
	base.onBtnEventEditApply = function() {
		var inputfields = $('#page_events_edit_form').serializeArray().reduce(function(obj, item) {
			obj[item.name] = item.value;
			return obj;
		}, {});
		inputfields['public'] = $("#page_events_edit_form input[name='public']").prop('checked');

		var fields = {};
		var newevent = (inputfields.id === null) || (inputfields.id === "");
		fields['id'] = inputfields.id;
		fields['name'] = inputfields.name;
		fields['description'] = inputfields.description;
		fields['public'] = inputfields.public;
		if (self._eventEventDatePicker.date()) {
			var msec = self._eventEventDatePicker.date().toDate().getTime();
			fields['eventStart'] = msec / 1000;
		}
		if (self._eventEventDayTimePicker.date()) {
			var mom = self._eventEventDayTimePicker.date();
			var sec = mom.hours() * 60 + mom.minutes();
			fields['repeatDayTime'] = sec;
		}
		fields['repeatWeekDays'] = self._getEventWeekDays();

		if (self._eventPhotoData) {
			fields['photo'] = self._eventPhotoData;
		}

		self._createOrUpdateEvent(fields, function(results) {
			if (results.status === "ok") {
				base.showModalBox("Changes were successfully applied to event.", "Event Update", "Dismiss");
				fields.ownerId = base._getAuthUser().id;
				if (newevent) {
					// on successful creation the new user id is in results.data
					fields.id = results.data.id;
					self._updateUiEventTableAdd(fields);
					self._getEventTable().draw("full-hold");
				}
				else {
					self._updateUiEventTableUpdate(fields);
					self._eventPhotoData = null;
				}
				base._showElement('menu_events', true);
				base._showElement('page_events_edit', false);
			}
			else {
				base.showModalBox("Could not apply changes to event! Reason: " + results.description, "Event Update", "Dismiss");
			}
		});
	};

	/**
	 * Cancel event edit ui.
	 */
	base.onBtnEventEditCancel = function() {
		base.onBtnEventClearForm();
		base._showElement('page_events_edit', false);
		base._showElement('menu_events', true);
	};

	/**
	 * Clear the event edit form.
	 */
	base.onBtnEventClearForm = function() {
		$("#page_events_edit_form :input").val("");
	};

	/**
	 * Search for a member. The keyword is expected to be in an input
	 * element with ID 'page_events_edit_form_mem_search'.
	 */
	base.onBtnSearchMember = function() {
		var keyword = $('#page_events_edit_form_mem_search').val();
		var sel = $('#page_events_edit_form_mem_search_hits');
		sel.empty();
		if (!keyword) {
			return;
		}
		base._m4eRESTUsers.search({
			success: function(res, resp) {
				if (res.status === "ok") {
					var hits = res.data;
					for (var i = 0; i < hits.length; i++) {
						sel.append(new Option(hits[i].name, hits[i].id));
					}
				}
				else {
					base.showModalBox(res.description, "Connection Problem", "Dismiss");
				}
			},
			error: function(err) {
				base.showModalBox(err, "Connection Problem", "Dismiss");
			}
		}, keyword);
	};

	/**
	 * Show information of the user with given ID in a modal dialog.
	 * 
	 * @param {integer} memberId  User ID
	 */
	base.onBtnEventMemberInfo = function(memberId) {
		base._m4eRESTUsers.find({
			success: function(res, resp) {
				if (res.status === "ok") {
					var user = res.data;
					var lastlogin = "-";
					if (user.dateLastLogin && user.dateLastLogin > 0) {
						var timestamp = new Date(parseInt(user.dateLastLogin));
						lastlogin = base._formatTime(timestamp);
					}
					var html = "<table class='table table-striped table-bordered'>";
					html += "<tr><td>User Name</td><td>" + user.name + "</td></tr>";
					html += "<tr><td>E-Mail</td><td>" + user.email + "</td></tr>";
					html += "<tr><td>Last Online</td><td>" + lastlogin + "</td></tr>";
					html += "</table>";
					base.showModalInfoBox(html, "User Info", "Dismiss");
				}
				else {
					base.showModalBox(res.description, "Problem Getting User Info", "Dismiss");
				}
			},
			error: function(err) {
				base.showModalBox(err, "Connection Problem", "Dismiss");
			}
		}, memberId);
	};

	/**
	 * Remove a member from an event.
	 * 
	 * @param {type} eventId  Event ID
	 * @param {type} memberId ID of the user to be removed
	 */
	base.onBtnEventMemberRemove = function(eventId, memberId) {
		base.showModalBox("Are you really sure you want to remove the member?", "Remove member", "No", "Yes", {
			onClickBtn1: function() {
				// nothing to do
			},
			onClickBtn2: function() {
				// sure to delete
				// NOTE as deleteUser opens also a modal messagebox we have to defer the call!
				base._deferExecution(function() {
					base._m4eRESTEvents.removeEventMember({
						success: function(res, resp) {
							if (res.status === "ok") {
								base.showModalBox("Member was successfully removed from Event.", "Remove Event Member", "Dismiss");
								self._updateUiEventMemberRemove(eventId, memberId);
							}
							else {
								base.showModalBox(res.description, "Problem Removing Member", "Dismiss");
							}
						},
						error: function(err) {
							base.showModalBox(err, "Connection Problem", "Dismiss");
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
	base.onBtnEventMemberMakeOwner = function(eventId, memberId) {
		alert("TODO: onBtnEventMemberMakeOwner");
	};

	/**
	 * Add the member which is selected in selection element with
	 * ID 'page_events_edit_form_mem_search_hits'.
	 */
	base.onBtnEventMemberAdd = function() {
		var sel = $('#page_events_edit_form_mem_search_hits option:selected');
		var userid = sel.val();
		var eventid = $("#page_events_edit_form input[name='id']").val();
		if (!eventid || !userid) {
			base.showModalBox("onBtnEventMemberAdd: Cannot add user to event, invalid user or event id!", "Internal Error", "Dismiss");
		}

		base._m4eRESTEvents.addEventMember({
			success: function(res, resp) {
				if (res.status === "ok") {
					base.showModalBox("Member was successfully added to Event.", "Add Event Member", "Dismiss");
					self._updateUiEventMemberAdd(true, eventid, res.data.memberId, res.data.memberName);
				}
				else {
					base.showModalBox(res.description, "Problem Adding Member", "Dismiss");
				}
			},
			error: function(err) {
				base.showModalBox(err, "Connection Problem", "Dismiss");
			}
		}, eventid, userid);
	};

	/**
	 * Apply event location changes.
	 */
	base.onBtnEventLocationEditApply = function() {
		var inputfields = $('#page_events_edit_location_form').serializeArray().reduce(function(obj, item) {
			obj[item.name] = item.value;
			return obj;
		}, {});
		var eventid = $("#page_events_edit_form input[name='id']").val();

		if (self._eventLocationPhotoData) {
			inputfields['photo'] = self._eventLocationPhotoData;
		}

		base._m4eRESTEvents.addOrUpdateLocation({
			success: function(res, resp) {
				if (res.status === "ok") {
					if (inputfields.id) {
						base.showModalBox("Location was successfully updated.", "Update Event Location", "Dismiss");						
					}
					else {
						base.showModalBox("Location was successfully added to Event.", "Add Event Location", "Dismiss");
					}
					var elemid = self._updateUiEventLocationAddOrUpdate(true, res.data.eventId, res.data.locationId, inputfields.name, inputfields.description);
					self._eventLocationPhotoData = null;
					self._updateUiEventLocationFormClear();
					base._scrollToElement(elemid, 'slow');
				}
				else {
					base.showModalBox(res.description, "Problem Adding Location", "Dismiss");
				}
			},
			error: function(err) {
				base.showModalBox(err, "Connection Problem", "Dismiss");
			}
		}, eventid, inputfields);
	};

	/**
	 * Remove an event location.
	 * 
	 * @param eventId     Event ID
	 * @param locationId  Location ID
	 */
	base.onBtnEventLocationRemove = function(eventId, locationId) {
		base.showModalBox("Are you really sure you want to remove the location?", "Remove location", "No", "Yes", {
			onClickBtn1: function() {
				// nothing to do
			},
			onClickBtn2: function() {
				// sure to delete
				// NOTE as deleteUser opens also a modal messagebox we have to defer the call!
				base._deferExecution(function() {
					base._m4eRESTEvents.removeEventLocation({
						success: function(res, resp) {
							if (res.status === "ok") {
								base.showModalBox("Location was successfully removed from Event.", "Remove Event Location", "Dismiss");
								self._updateUiEventLocationRemove(eventId, locationId);
							}
							else {
								base.showModalBox(res.description, "Problem Removing Location", "Dismiss");
							}
						},
						error: function(err) {
							base.showModalBox(err, "Connection Problem", "Dismiss");
						}
					}, eventId, locationId);
				}, 500);
			}
		});
	};

	/**
	 * Edit an event location.
	 * 
	 * @param eventId     Event ID
	 * @param locationId  Location ID
	 */
	base.onBtnEventLocationEdit = function(eventId, locationId) {
		self._eventLocationPhotoData = null;
		base._m4eRESTEvents.getEventLocation({
			success: function(res, resp) {
				if (res.status === "ok") {
					var location = res.data;
					$("#page_events_edit_location_form :input").val("");
					$("#page_events_edit_location_form input[name='id']").val(location.id);
					$("#page_events_edit_location_form input[name='name']").val(location.name);
					$("#page_events_edit_location_form textarea[name='description']").val(location.description);
					if (location.photoId) {
						self._loadPhoto('page_events_edit_location_icon_img', location.photoId);
					}
					else {
						$('#page_events_edit_location_icon_img').prop('src', self._eventLocationPhotoDefault);
					}
					base._scrollToElement('page_events_edit_location_form', 'slow');
				}
				else {
					base.showModalBox(res.description, "Problem Getting Event Location", "Dismiss");
				}
			},
			error: function(err) {
				base.showModalBox(err, "Connection Problem", "Dismiss");
			}
		}, eventId, locationId);
	};

	/**
	 * Show event location information.
	 * 
	 * @param eventId         Event ID
	 * @param locationId      Location ID
	 * @param locationPhotoId Location photo ID
	 */
	base.onBtnEventLocationInfo = function(eventId, locationId, locationPhotoId) {
		base._m4eRESTEvents.getEventLocation({
			success: function(res, resp) {
				if (res.status === "ok") {
					var location = res.data;
					var html = "<div class='text-center'>" + "<img id='event_location_info_img' src='" + self._eventLocationPhotoDefault + "'></div>";
					html += "<table style='margin-top: 20px;' class='table table-striped table-bordered'>";
					html += "<tr><td>Name</td><td>" + location.name + "</td></tr>";
					html += "<tr><td>Description</td><td>" + location.description + "</td></tr>";
					html += "</table>";
					if (locationPhotoId) {
						self._loadPhoto('event_location_info_img', locationPhotoId);
					}
					base.showModalInfoBox(html, "Event Location Info", "Dismiss");
				}
				else {
					base.showModalBox(res.description, "Problem Getting Event Location Info", "Dismiss");
				}
			},
			error: function(err) {
				base.showModalBox(err, "Connection Problem", "Dismiss");
			}
		}, eventId, locationId);
	};

	/**
	 * Clear the location form.
	 */
	base.onBtnEventLocationClearForm = function() {
		self._updateUiEventLocationFormClear();
	};

	/**
	 * Load and create a new image for an event.
	 * 
	 * @param event The event which was created on an file input button.
	 */
	base.onBtnEventIcon = function(event) {
		base.getImageModule().loadImageFile(event, {
			success: function(image) {
				$('#page_events_edit_pane_icon_img').prop('src', image.src);
				// update _eventPhotoData so on next apply the new icon data will be transmitted, too
				self._eventPhotoData = image.src;
			},
			error: function(errorString) {
				base.showModalBox(errorString, "Problem Loading Image File", "Dismiss");
			}	
		});
	};

	/**
	 * Load and create a new image for an event location.
	 * 
	 * @param event The event which was created on an file input button.
	 */
	base.onBtnEventLocationIcon = function(event) {
		base.getImageModule().loadImageFile(event, {
			success: function(image) {
				$('#page_events_edit_location_icon_img').prop('src', image.src);
				// update _eventLocationPhotoData so on next apply the new icon data will be transmitted, too
				self._eventLocationPhotoData = image.src;
			},
			error: function(errorString) {
				base.showModalBox(errorString, "Problem Loading Image File", "Dismiss");
			}	
		});
	};

	/**********************************************************************/
	/*                        Private functions                           */
	/**********************************************************************/

	self._getEventTable = function() {
		if (!self.m4eEventTable) {
			self.m4eEventTable = $('#table_events').DataTable({
				responsive: true,
				"columns": [
					{ "data": "name" },
					{ "data": "description" },
					{ "data": "eventStart" },
					{ "data": "eventRepeat" },
					{ "data": "public" },
					{ "data": "ops" }
				]});
		}
		return self.m4eEventTable;
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
			base.showModalBox("updateUiEventTableAdd: Cannot add event, invalid user id!", "Internal Error", "Dismiss");
			return;
		}
		var eventstart = "-";
		var eventrepeat = "No";
		var ispublic = eventFields.public ? "Yes" : "No";
		if (eventFields.eventStart && parseInt(eventFields.eventStart) > 0) {
			var timestamp = new Date(parseInt(eventFields.eventStart * 1000));
			eventstart = base._formatTime(timestamp);
		}
		if (eventFields.repeatWeekDays && eventFields.repeatWeekDays > 0) {
			eventrepeat = "Yes";
		}
		var desc = eventFields.description;
		if (desc.length > 32) {
			desc = desc.substring(0, 32);
			desc += "...";
		}
		var me = (""+eventFields.ownerId === ""+base._getAuthUser().id);
		var candelete = base._userIsAdmin() || me;
		var canedit = base._userIsAdmin() || me;
		self._getEventTable().row.add({
				"DT_RowId" : eventFields.id,
				// make 'me' bold
				"name" : (me ? "<strong>" : "") + eventFields.name + (me ? "</strong>" : ""),
				"description" : desc,
				"eventStart" : eventstart,
				"eventRepeat" : eventrepeat,
				"public" : ispublic,
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
				 "<a class='btn' onclick='getMeet4EatUI().onBtnEventMemberInfo" + argsinfo + ";'>" + base._encodeString(memberName) + "</a>" + ops + "</li>";
		members.append(li);
	};

	self._updateUiEventMemberRemove = function(eventId, memberId) {
		var liid = "page_events_edit_members-" + eventId + "-" + memberId;
		$('#' + liid).remove();
	};

	self._updateUiEventLocationFormClear = function() {
		$('#page_events_edit_location_form :input').val('');
		$('#page_events_edit_location_icon_img').prop('src', self._eventLocationPhotoDefault);
	};

	self._updateUiEventLocationsClear = function() {
		$('#page_events_edit_locations').empty();
		self._updateUiEventLocationFormClear();
	};

	/**
	 * Add or update a location entry in list and return its element ID.
	 */
	self._updateUiEventLocationAddOrUpdate = function(allowModification, eventId, locationId, locationName, locationDescription, locationPhotoId) {
		var locations = $('#page_events_edit_locations');
		var liid = "page_events_edit_locations-" + eventId + "-" + locationId;
		var argsinfo = "(" + eventId + "," + locationId + "," + locationPhotoId + ")";
		var argsremove = "(" + eventId + "," + locationId + ")";
		var ops = "";
		if (allowModification) {
			ops = "<a onclick='getMeet4EatUI().onBtnEventLocationRemove" + argsremove + ";'>" +
				"<span class='btn fa fa-remove fa-2x text-danger pull-right' title='Remove location'> </span></a>" +
				"<a onclick='getMeet4EatUI().onBtnEventLocationEdit" + argsinfo + ";'>" +
				"<span class='btn fa fa-edit fa-2x pull-right' title='Edit location'> </span></a>";
		}
		var desc = (locationDescription ? locationDescription : "");
		if (desc.length > 32) {
			desc = desc.substr(0, 32) + "...";
		}
		var litext = base._encodeString(locationName) + (desc ? ", <small>" + base._encodeString(desc) + "</small>" : "");
		var li = "<li id='"+ liid + "' class='list-group-item'>" +
				 "<a class='btn' onclick='getMeet4EatUI().onBtnEventLocationInfo" + argsinfo + ";'>" + litext + "</a>" + ops + "</li>";

		/* if the element exists then update it, otherwise append a new element to list */
		if ($('#' + liid).length === 0) {
			locations.append(li);
		}
		else {
			$('#' + liid).replaceWith(li);
		}
		return liid;
	};

	self._updateUiEventLocationRemove = function(eventId, locationId) {
		var liid = "page_events_edit_locations-" + eventId + "-" + locationId;
		$('#' + liid).remove();
	};

	self._setupUiEventNew = function() {
		$('#page_events_edit_title').text("Create a new event");
		$("#page_events_edit_form :input").val("");
		$("#page_events_edit_form input[name='public']").prop('checked', false);
		base._showElement('page_nav_events_edit', false);
		self._setupEventTimeInputs();
		self._setEventWeekDays(0);
	};

	self._updateUiEventTableUpdate = function(eventFields) {
		if (eventFields.id === "") {
			base.showModalBox("updateUiEventTableUpdate Cannot update event, invalid event id!", "Internal Error", "Dismiss");
			return;
		}
		var me = (""+eventFields.ownerId === ""+base._getAuthUser().id);
		var event = self._getEventTable().row('#' + eventFields.id);
		if (event) {
			var cols = event.data();
			if (eventFields.name) {
				cols.name = (me ? "<strong>" : "") + eventFields.name + (me ? "</strong>" : "");
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
				cols.eventStart = base._formatTime(timestamp);
			}
			cols.eventRepeat = (eventFields.repeatWeekDays > 0) ? "Yes" : "No";
			cols.public = (eventFields.public) ? "Yes" : "No";
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
		base._showElement('page_nav_events_edit', true);
		self._setupEventTimeInputs();
		self._updateUiEventMembersClear();
		self._updateUiEventLocationsClear();
		
		// fetch event details from server
		base._m4eRESTEvents.find({
			success: function(results, response) {
				if (results.status !== "ok") {
					base.showModalBox("Event was not found for edit!", "Error", "Dismiss");
					return;
				}
				var ev = results.data;
				$("#page_events_edit_form input[name='id']").val(ev.id);
				$("#page_events_edit_form input[name='name']").val(ev.name);
				$("#page_events_edit_form input[name='public']").prop('checked', ev.public);
				$("#page_events_edit_form textarea[name='description']").val(ev.description);
				//! NOTE eventStart is in seconds!
				if (ev.eventStart && ev.eventStart > 0) {
					var date = new Date(ev.eventStart * 1000);
					date = moment(date);
					self._eventEventDatePicker.date(date);
				}
				//! NOTE repeatDayTime is in seconds!
				if (ev.repeatDayTime && ev.repeatDayTime > 0) {
					date = moment();
					var hour = Math.floor(ev.repeatDayTime / 60);
					var min = ev.repeatDayTime - hour * 60;
					date.hours(hour);
					date.minutes(min);
					self._eventEventDayTimePicker.date(date);
				}
				self._setEventWeekDays(ev.repeatWeekDays);
				var modperms = (""+ev.ownerId === ""+base._getAuthUser().id) || base._userIsAdmin();
				for (var i = 0; i < ev.members.length; i++) {
					self._updateUiEventMemberAdd(modperms, ev.id, ev.members[i].id, ev.members[i].name);
				}
				self._setupUiEventOwner(ev.ownerId, ev.ownerName);
				
				for (var i = 0; i < ev.locations.length; i++) {
					self._updateUiEventLocationAddOrUpdate(modperms, ev.id, ev.locations[i].id, ev.locations[i].name, ev.locations[i].description, ev.locations[i].photoId);
				}
				if (ev.photoId) {
					self._loadPhoto('page_events_edit_pane_icon_img', ev.photoId);
				}
				else {
					$("#page_events_edit_pane_icon_img").prop('src', self._eventPhotoDefault);
				}
			},
			error: function(err) {
				base.showModalBox(err, "Connection Error", "Dismiss");
			}
		}, id);
	};

	self._setupUiEventOwner = function(ownerId, ownerName) {
		var argsinfo = "(" + ownerId + ")";
		var html = "<a class='btn' onclick='getMeet4EatUI().onBtnEventMemberInfo" + argsinfo + ";'>" + ownerName + "</a>";
		$('#page_events_edit_owner').html(html);
	};

	self._createOrUpdateEvent = function(fields, resultsCallback) {
		base._m4eRESTEvents.createOrUpdate({
			success: function(results) {
				if (resultsCallback) {
					resultsCallback(results);
				}
			},
			error: function(text, response) {
				base.displayErrorHTML(response);
				base.showModalBox("Could not create or update event! Reason: " + text, "Connection Error", "Dismiss");
			}					
		}, fields.id, fields);
	};

	self._deleteEvent = function(id) {
		base._m4eRESTEvents.delete({
			success: function(results) {
				if (results.status !== "ok") {
					base.showModalBox("Could not delete event! Reason: " + results.description, "Problem Deleting Event", "Dismiss");
					return;
				}
				base.showModalBox("Event was successfully removed", "Delete Event", "Dismiss");
				self._updateUiEventTableRemove(id);
				base._showElement('page_events_edit', false);
				base._showElement('menu_events', true);
			},
			error: function(text, response) {
				base.showModalBox(text, "Problem Deleting Event", "Dismiss");
				base.displayErrorHTML(response);
			}
		}, id);
	};

	self._loadPhoto = function(elemId, photoId) {
		base.getImageModule().loadImageFromServer(elemId, photoId);
	};
}