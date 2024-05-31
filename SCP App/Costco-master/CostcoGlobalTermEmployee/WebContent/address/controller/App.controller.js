/**
 * @module Display Address Application
 */
sap.ui.define([
		"com/costco/address/controller/BaseController",
		"sap/ui/model/json/JSONModel"
	], function (BaseController, JSONModel) {
		"use strict";

		return BaseController.extend("com.costco.address.controller.App", {

			/**
			 * Controller's initialization
			 * @memberof module:Display Address Application
			 */
			onInit : function () {
				var oViewModel,
					fnSetAppNotBusy,
					iOriginalBusyDelay = this.getView().getBusyIndicatorDelay();

				oViewModel = new JSONModel({
					busy : true,
					delay : 0
				});
				this.setModel(oViewModel, "appView");

				this._loadPicklist("/a/picklist");
				
				fnSetAppNotBusy = function() {
					oViewModel.setProperty("/busy", false);
					oViewModel.setProperty("/delay", iOriginalBusyDelay);
				};

				fnSetAppNotBusy();
				
				// apply content density mode to root view
				this.getView().addStyleClass(this.getOwnerComponent().getContentDensityClass());
				
				$("#splash-screen").remove();

			},
			
			/**
			 * Retrieve picklists
			 * 
			 * @memberof module:Display Address Application
			 * @private
			 * @param {string}
			 *                url for picklist api
			 */
			_loadPicklist : function(url) {
			   // var def = new jQuery.Deferred();
			    var oPicklistModel = new sap.ui.model.json.JSONModel();
			    sap.ui.getCore().setModel(oPicklistModel, "picklist");

			    jQuery.ajax({
				type : "GET",
				url : this.getUri(url),
				dataType : 'json',
				async : true,
				success : function(data) {
				    oPicklistModel.setData(data);
				    //oPicklistModel.setProperty("/picklist", data);
				    sap.ui.getCore().setModel(oPicklistModel, "picklist");
				//    def.resolve("success");
				}
			    });

			  //  return def.promise();
			}
		});

	}
);
