/**
 * @module Display Address
 */
sap.ui.define([ 
	"com/costco/admin/controller/BaseController", 
	"jquery.sap.global",
	"sap/ui/model/json/JSONModel", 
	"com/costco/admin/model/formatter", 
	"sap/m/Dialog",	
	"sap/m/Button",
	"sap/m/Text", 
	"sap/m/MessageBox"
], function(BaseController, jQuery, JSONModel, formatter, Dialog, Button, Text, MessageBox) {
	"use strict";
	
	var that;
	
    return BaseController.extend("com.costco.admin.controller.AdminAddress", {

		formatter : formatter,

		onInit : function() {
			that = this;
			this.getRouter().getRoute('AdminAddress').attachMatched(this._onRouteMatched, this);

			var displayModel = new JSONModel({
				busy: true
			});
			this.getView().setModel(displayModel, 'display');
		},

		_onRouteMatched : function() {
			var proxyUserId = this.getProxyUserId();
			if (!proxyUserId) {
				this.navToHomepage();
			} else {
				this._updateEmployeeData(proxyUserId);
			}
		},

		_updateEmployeeData: function(employeeId) {
			this._setBusy(true);
			this._loadEmployeeData(employeeId)
			.then((employeeData) => {
				this.getModel().setProperty('/employee', employeeData);
			})
			.catch((err) => {
				MessageBox.error(err.message, {
					onClose: () => {
						that.navToHomepage();
					}
				});
			})
			.then(() => {
				this._setBusy(false);
			});
		},

		_loadEmployeeData: function(employeeId) {
			var url = `/a/admin/employeeData/${employeeId}`;
			return this.httpGet(this.getUri(url));
		},

		_setBusy: function(busy) {
			this.getView().getModel('display').setProperty('/busy', busy);
		}

    });
});
