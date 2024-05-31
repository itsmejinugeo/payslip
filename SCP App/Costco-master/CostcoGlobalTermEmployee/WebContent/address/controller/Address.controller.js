/**
 * @module Display Address
 */
sap.ui.define([ "com/costco/address/controller/BaseController", "jquery.sap.global", "sap/ui/core/mvc/Controller", "sap/ui/model/json/JSONModel", "com/costco/address/model/formatter", "sap/m/Dialog",	"sap/m/Button",
	"sap/m/Text", ], function(
	BaseController, jQuery, Controller, JSONModel, formatter, Dialog, Button, Text) {
    "use strict";
    var that;
    return BaseController.extend("com.costco.address.controller.Address", {

	formatter : formatter,

	/**
	 * Controller's initialization
	 * 
	 * @memberof module:Display Address
	 */

	onInit : function(oEvent) {

	    that = this;

	    var displayModel = new sap.ui.model.json.JSONModel({
		"showApp" : false,
		"state" : false,
		"editButtonVisible" : true,
		"saveButtonVisible" : false
	    });
	    this.getView().setModel(displayModel, "display");

	    // var oRouter = this.getRouter();

	    // oRouter.attachRouteMatched(function(oEvent) {
	    // var sRouteName;
	    // sRouteName = oEvent.getParameter("name");
	    // if (sRouteName === "address") {
	    this._onRouteMatched("");
	    // var test ="tes";
	    // }
	    // }, this);

	},

	/**
	 * Route match
	 * 
	 * @memberof module:Display Pay Advice List
	 * @private
	 * @param {sap.ui.base.Event}
	 *                event
	 */
	_onRouteMatched : function(event) {
	    this._loadPicklist();
	    var promise = this._loadAddress("/a/employeeData");
	    promise.done(function(data) {
		that._cloneData();
		that._linkDropdownList(data.address);
	    });
	},

	/**
	 * Initialize dropdown lists
	 * 
	 * @memberof module:Display Address
	 * @private
	 */
	_linkDropdownList : function(addressData) {
	    var oTemplate = new sap.ui.core.ListItem({
		key : "{picklist>externalCode}",
		text : "{picklist>label}"
	    });

	    var oView = this.getView();
	    var oState = oView.byId("stateList");

	    var countryPath = this._initCountryPath(addressData.country);
	    if (typeof countryPath != 'undefined') {

		oState.bindAggregation("items", "picklist>" + countryPath + "/stateList", oTemplate);
		var statePath = this._initStatePath(addressData.state, this._initGetItemNumberFromPath(countryPath));
		if (typeof statePath != 'undefined') {
		    var stateId = this.getView().getModel("picklist").getObject(countryPath).stateList[this._initGetItemNumberFromPath(statePath)].pickListId;
		    console.debug(stateId);
		}
	    }

	},

	/**
	 * Initialize Country object path
	 * 
	 * @memberof module:Display Address
	 * @private
	 * @param {string}
	 *                country
	 * @return {string} path
	 */
	_initCountryPath : function(country) {
	    var oCountryData = this.getView().getModel("picklist").oData.addressPicklist;
	    var path;
	    for (var i = 0; i < oCountryData.length; i++) {
		if (oCountryData[i].code === country) {
		    path = /addressPicklist/ + i;
		    break;
		}
	    }
	    return path;
	},

	/**
	 * Initialize State object path
	 * 
	 * @memberof module:Display Address
	 * @private
	 * @param {string}
	 *                location
	 * @param {string}
	 *                companyPathNumber
	 * @return {string} path
	 */
	_initStatePath : function(state, countryPathNumber) {
	    var oStateData = this.getView().getModel("picklist").oData.addressPicklist[countryPathNumber].stateList;
	    var path;
	    for (var i = 0; i < oStateData.length; i++) {
		if (oStateData[i].externalCode === state) {
		    path = /stateList/ + i;
		    break;
		}
	    }
	    return path;
	},

	/**
	 * Get index from object path
	 * 
	 * @memberof module:Display Address
	 * @private
	 * @param {string}
	 *                path
	 * @return {string} index of object path
	 */
	_initGetItemNumberFromPath : function(path) {
	    if (path !== undefined) {
		return path.split('/')[2];
	    }
	},

	/**
	 * Clone Address data
	 * 
	 * @memberof module:Display Address
	 * @private
	 */
	_cloneData : function() {
	    var oView = this.getView();
	    var oAddressModel = sap.ui.getCore().getModel("addressData");

	    var oCloneAddressModel = new sap.ui.model.json.JSONModel();
	    // oCloneAddressModel = this._clone(oAddressModel);
	    jQuery.extend(true, oCloneAddressModel, oAddressModel);
	    oView.setModel(oCloneAddressModel, "overview")
	},

	/**
	 * Load picklists
	 * 
	 * @memberof module:Display Address
	 * @private
	 */
	_loadPicklist : function() {
	    var oView = this.getView();
	    var oPicklistModel = new sap.ui.model.json.JSONModel();
	    oPicklistModel = sap.ui.getCore().getModel("picklist");
	    oView.setModel(oPicklistModel, "picklist");
	},

	/**
	 * Retrieve employee data and data binding
	 * 
	 * @memberof module:Display Address
	 * @private
	 * @param {string}
	 *                url for employee data api
	 */
	_loadAddress : function(url) {
	    var def = new jQuery.Deferred();
	    var oView = this.getView();
	    var oAddressModel = new sap.ui.model.json.JSONModel();
	    sap.ui.getCore().setModel(oAddressModel, "addressData");
	    var oDisplayModel = this.getView().getModel("display");
	    oDisplayModel.setData({
		"showApp" : false,
		"state" : false,
		"editButtonVisible" : true,
		"saveButtonVisible" : false
	    });
	    var splashScreen = document.getElementsByClassName("splash-screen-div");
	    if (splashScreen[0] !== undefined) {
		splashScreen[0].style.display = "inline";
	    }

	    jQuery.ajax({
		type : "GET",
		url : this.getUri(url),
		dataType : 'json',
		async : true,
		success : function(data) {
		    // oAddressModel.setProperty("/overview", data);
		    oAddressModel.setData(data);
		    sap.ui.getCore().setModel(oAddressModel, "addressData");
		    oDisplayModel.setData({
			"showApp" : true,
			"state" : false,
			"editButtonVisible" : true,
			"saveButtonVisible" : false
		    });
		    oView.setModel(oDisplayModel, "display");
		    var splashScreen = document.getElementsByClassName("splash-screen-div");
		    if (splashScreen[0] !== undefined) {
			splashScreen[0].style.display = "none";
		    }

		    def.resolve(data);
		}
	    });
	    this.getView().setModel(oDisplayModel, "display");
	    return def.promise();
	},

	/**
	 * Edit Button Pressed
	 * 
	 * @memberof module:Display Address
	 */

	handleEditPress : function() {

	    var displayModel = this.getView().getModel("display");
	    displayModel.setData({
		"showApp" : true,
		"state" : true,
		"editButtonVisible" : false,
		"saveButtonVisible" : true
	    });

	},

	/**
	 * Validate Address
	 * 
	 * @memberof module:Display Address
	 */

	_validateAddress : function() {
	    var noChange = true;
	    var ori = sap.ui.getCore().getModel("addressData").getData().address;
	    var data = this.getView().getModel("overview").getData().address;
	    noChange = JSON.stringify(ori) == JSON.stringify(data);
	    return noChange;
	},
	
	/**
	 * Save Button Pressed
	 * 
	 * @memberof module:Display Address
	 */

	handleSavePress : function() {
	    if (this._validateAddress()) {
			var noChangeMsg = that.getView().getModel("i18n").getResourceBundle()
				.getText("msgAddressNoChange");
			sap.m.MessageToast.show(noChangeMsg);
			return;
	    }
	    
	    var data = this.getView().getModel("overview").getData().address;

	    var busyDialog;
	    if (this._dialog === undefined) {
			busyDialog = new sap.ui.xmlfragment("com.costco.address.view.fragments.BusyDialogSave", this);
			this._dialog = busyDialog;
			this.getView().addDependent(this._dialog);
	    } else {
			busyDialog = this._dialog;
	    }

	    // open dialog
	    jQuery.sap.syncStyleClass("sapUiSizeCompact", this.getView(), this._dialog);
	    this._dialog.open();

	    jQuery.ajax({
			type : "POST",
			contentType : "application/json;charset=utf-8",
			url : this.getUri("/a/updateAddress"),
			dataType : "json",
			data : JSON.stringify(data),
			async : true,
			success : function(data, textStatus, jqXHR) {
				busyDialog.close();
				setTimeout(function() {
					var updateSuccessMsg = that.getView().getModel("i18n").getResourceBundle()
						.getText("msgAddressUpdateSuccess");
					sap.m.MessageToast.show(updateSuccessMsg);
				}, 1000);
				that._addressUpdated();
			},
			error : function(jqXHR, textStatus, errorThrown) {
				busyDialog.close();
				switch (jqXHR.status) {
					case 404:
						sap.m.MessageToast.show(textStatus);
						break;

					default:
						var responseText = jqXHR.responseText;
						// var msg = responseText.match("<b>Message</b>(.*)</p><p><b>Description</b>");
						var resBody = JSON.parse(responseText);
						var msg = resBody.errorMessage;
						var i18nBundle = that.getView().getModel("i18n").getResourceBundle();
						var dialogTitle = i18nBundle.getText('dialogTitleError');
						var btnText = i18nBundle.getText('btnOk');
						var dialog = new Dialog({
							title : dialogTitle,
							type : 'Message',
							state : 'Error',
							content : new Text({
								// text : msg[1]
								text: msg
							}),
							beginButton : new Button({
								text : btnText,
								press : function() {
									dialog.close();
								}
							}),
							afterClose : function() {
								dialog.destroy();
							}
						});

						dialog.open();
						break;
				}

			}
	    });
	},

	/**
	 * Cancel Button Pressed
	 * 
	 * @memberof module:Display Address
	 */

	handleCancelPress : function() {

	    this._cloneData();
	    this._resetUI();
	    var oAddressModel = this.getView().getModel("overview");
	    that._linkDropdownList(oAddressModel.getData().address);
	},

	/**
	 * Event handler for {@link sap.m.Select} Country dropdown list
	 * 
	 * @memberof module:Display Address
	 * 
	 * @param {sap.ui.base.Event}
	 *                oEvent
	 */
	handleCountrySelect : function(oEvent) {
		var oAddressModel = this.getView().getModel("overview");
		// reset postcode
		oAddressModel.setProperty('/address/zipcode', '');
	    // reset state
	    oAddressModel.setProperty("/address/stateId", "");
	    oAddressModel.setProperty("/address/state", "");
	    this._linkDropdownList(oAddressModel.getData().address);
	},

	/**
	 * Event handler for {@link sap.m.Select} State dropdown list
	 * 
	 * @memberof module:Display Address
	 * 
	 * @param {sap.ui.base.Event}
	 *                oEvent
	 */
	handleStateSelect : function(oEvent) {
	    var oAddressModel = this.getView().getModel("overview");
	    var selectedStatePath = oEvent.getParameter("selectedItem").getBindingContext("picklist").getPath();
	    var selectedStateId = this.getView().getModel("picklist").getObject(selectedStatePath).pickListId;
	    oAddressModel.setProperty("/address/stateId", selectedStateId);
	},

	/**
	 * Address updated
	 * 
	 * @memberof module:Display Address
	 * @private
	 */
	_addressUpdated : function() {
		var oAddressModel = this.getView().getModel("overview");
		var copiedData = {};
		jQuery.extend(true, copiedData, oAddressModel.getData());
	    sap.ui.getCore().setModel(new JSONModel(copiedData), "addressData");
	    this._resetUI();
	},

	/**
	 * Reset UI State
	 * 
	 * @memberof module:Display Address
	 * @private
	 */
	_resetUI : function() {

	    var displayModel = this.getView().getModel("display");
	    displayModel.setData({
		"showApp" : true,
		"state" : false,
		"editButtonVisible" : true,
		"saveButtonVisible" : false
	    });

	},

	/**
	 * Clone
	 * 
	 * @memberof module:Display Address
	 * @private
	 */
	_clone : function clone(item) {
	    if (!item) {
		return item;
	    } // null, undefined values check

	    var types = [ Number, String, Boolean ], result;

	    // normalizing primitives if someone did new String('aaa'), or new
	    // Number('444');
	    types.forEach(function(type) {
		if (item instanceof type) {
		    result = type(item);
		}
	    });

	    if (typeof result == "undefined") {
		if (Object.prototype.toString.call(item) === "[object Array]") {
		    result = [];
		    item.forEach(function(child, index, array) {
			result[index] = clone(child);
		    });
		} else if (typeof item == "object") {
		    // testing that this is DOM
		    if (item.nodeType && typeof item.cloneNode == "function") {
			result = item.cloneNode(true);
		    } else if (!item.prototype) { // check that this is a
						    // literal
			if (item instanceof Date) {
			    result = new Date(item);
			} else {
			    // it is an object literal
			    result = {};
			    for ( var i in item) {
				result[i] = clone(item[i]);
			    }
			}
		    } else {
			// depending what you would like here,
			// just keep the reference, or create new object
			if (false && item.constructor) {
			    // would not advice to do that, reason? Read below
			    result = new item.constructor();
			} else {
			    result = item;
			}
		    }
		} else {
		    result = item;
		}
	    }

	    return result;
	}

    });

});