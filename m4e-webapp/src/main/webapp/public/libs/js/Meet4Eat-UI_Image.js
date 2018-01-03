/**
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
"use strict";

/**
 * Meet4Eat Admin Panel UI Control - Image Module
 * 
 * Dependencies:
 *   - jQuery
 *   - EXIF
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
function Meet4EatUI_Image(baseModule) {

	/* self points to base module */
	var base = baseModule;

	/* self pointer */
	var self = this;

	/* UI version */
	self._version = "1.0.0";

	/* Max image size width and height */
    self.IMG_MAX_DIM = 256;

	/* Image REST Api */
    self._m4eRESTImages = null;

	/**
	 * Initialize the Meet4Eat UI User module.
	 */
	self.initialize = function() {
		var  restbuilder = new Meet4EatREST();
		self._m4eRESTImages = restbuilder.buildImageREST();
	};

	/**
	 * Setup the user module.
	 * 
	 * @param event The event which was created on an file input button.
	 * @param callbackImage Callback used when an image was selected. The callback has following function:
	 * 
	 *                      {
	 *                         success: function(image),
	 *                         error: function(errorString)
	 *                      }
	 */
	self.loadImageFile = function(event, callbackImage) {
		var reader = new FileReader();
		reader.onload = function (e) {
			var img = new Image();
			img.onload = function () {
				var maxside = Math.max(img.width, img.height);
				var newwidth = img.width;
				var newheight = img.height;

				// rescale image if necessary
				if (maxside > self.IMG_MAX_DIM) {
					if (img.width === maxside) {
						newwidth = self.IMG_MAX_DIM;
						newheight = Math.floor(img.height * (self.IMG_MAX_DIM / img.width));
					}
					else {
						newwidth = Math.floor(img.width * (self.IMG_MAX_DIM / img.height));
						newheight = self.IMG_MAX_DIM;
					}
					var tmpcanvas = $('<canvas/>').prop({ width: newwidth, height: newheight })[0];
					tmpcanvas.getContext('2d').drawImage(img, 0, 0, img.width, img.height, 0, 0, newwidth, newheight);
					img.src = tmpcanvas.toDataURL('image/png');
					img.width = newwidth;
					img.height = newheight;
					tmpcanvas = null;
				}
				if (callbackImage) {
					callbackImage.success(img);
				}
			};
			img.src = e.target.result;
		};

		reader.onerror = function () {
			if (callbackImage) {
				callbackImage.error("An error occurred while loading image.");
			}
		};

		// check for file type
		var file = event.target.files[0];
		// cancelled?
		if (!file) {
			return false;
		}
		if ((file.type.indexOf("image/") !== 0) || (file.size < 1)) {
			if (callbackImage) {
				callbackImage.error("File is not an image.");
			}
			return false;
		}

		reader.readAsDataURL(file);
		return true;
	};

	/**
	 * Try to load the image from server and assign it to given HTML img element, if successful.
	 * 
	 * @param {string} imageElementId  IMG element ID
	 * @param {integer} imageId        Image ID
	 */
	self.loadImageFromServer = function(imageElementId, imageId) {
		self._m4eRESTImages.getImage({
			success: function(results) {
				if (results.status !== "ok") {
					base.showModalBox("Could not get image! Reason: " + results.description, "Problem Getting Image", "Dismiss");
					return;
				}
				$('#' + imageElementId).prop('src', results.data.content);
			},
			error: function(text, response) {
				base.showModalBox(text, "Problem Getting Image", "Dismiss");
				base.displayErrorHTML(response);
			}
		}, imageId);
	};
}
