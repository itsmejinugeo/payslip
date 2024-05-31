/**
 * @module Display Employee Pay Advice Application
 */
sap.ui.define([
		"com/costco/au/admin/controller/BaseController",
		"sap/ui/model/json/JSONModel"
	], function (BaseController, JSONModel) {
		"use strict";

		return BaseController.extend("com.costco.au.admin.controller.App", {

			/**
			 * Controller's initialization
			 * @memberof module:Display Employee Pay Advice Application
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

				fnSetAppNotBusy = function() {
					oViewModel.setProperty("/busy", false);
					oViewModel.setProperty("/delay", iOriginalBusyDelay);
				};

				fnSetAppNotBusy();
				
				// apply content density mode to root view
				this.getView().addStyleClass(this.getOwnerComponent().getContentDensityClass());
				
				$("#splash-screen").remove();

			}
		});

	}
);
