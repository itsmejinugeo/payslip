/**
 * @module Main Component
 * 
 */
sap.ui.define([
	"sap/ui/core/UIComponent",
	"sap/ui/Device",
	"com/costco/admin/model/models",
	"sap/ui/model/json/JSONModel"
], function(UIComponent, Device, models, JSONModel) {
	"use strict";

	return UIComponent.extend("com.costco.admin.Component", {

		metadata: {
			manifest: "json"
		},

		/**
		 * The component is initialized by UI5 automatically during the startup of the app and calls the init method once.
		 * In this function, the FLP and device models are set and the router is initialized.
		 * @memberof module:Main Component
		 * @override
		 */
		init: function() {
			// call the base component's init function
			UIComponent.prototype.init.apply(this, arguments);

			// set component model
			var componentModel = new JSONModel({
				proxyUserId: undefined
			});
			this.setModel(componentModel);
			
			// set the device model
			this.setModel(models.createDeviceModel(), "device");
			
			this.getRouter().initialize(); 
			
			this.getRouter().attachBeforeRouteMatched(() => {
				sap.ui.core.BusyIndicator.show(0);
			});
			this.getRouter().attachRouteMatched(() => {
				sap.ui.core.BusyIndicator.hide();
			});
			this.getRouter().attachBypassed(() => {
				sap.ui.core.BusyIndicator.hide();
			});
		},
		
		/**
		 * The component is destroyed by UI5 automatically.
		 * In this method, the ErrorHandler is destroyed.
		 * @memberof module:Main Component
		 * @override
		 */
		destroy : function () {
			// call the base component's destroy function
			UIComponent.prototype.destroy.apply(this, arguments);
		},
		
		/**
		 * This method can be called to determine whether the sapUiSizeCompact or sapUiSizeCozy
		 * design mode class should be set, which influences the size appearance of some controls.
		 *
		 * @return {string} css class, either 'sapUiSizeCompact' or 'sapUiSizeCozy' - or an empty string if no css class should be set
		 */
		getContentDensityClass : function() {
			if (this._sContentDensityClass === undefined) {
				// check whether FLP has already set the content density class; do nothing in this case
				if (jQuery(document.body).hasClass("sapUiSizeCozy") || jQuery(document.body).hasClass("sapUiSizeCompact")) {
					this._sContentDensityClass = "";
				} else if (!Device.support.touch) { // apply "compact" mode if touch is not supported
					this._sContentDensityClass = "sapUiSizeCompact";
				} else {
					// "cozy" in case of touch support; default for most sap.m controls, but needed for desktop-first controls like sap.ui.table.Table
					this._sContentDensityClass = "sapUiSizeCozy";
				}
			}
			return this._sContentDensityClass;
		}
	});
});