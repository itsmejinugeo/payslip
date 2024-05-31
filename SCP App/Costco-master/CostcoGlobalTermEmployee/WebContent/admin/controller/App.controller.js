/**
 * @module Display Address Application
 */
sap.ui.define([
		"com/costco/admin/controller/BaseController"
	], function (BaseController) {
		"use strict";

		return BaseController.extend("com.costco.admin.controller.App", {

			/**
			 * Controller's initialization
			 * @memberof module:Display Address Application
			 */
			onInit : function () {
				// apply content density mode to root view
				this.getView().addStyleClass(this.getOwnerComponent().getContentDensityClass());
			}

		});

	}
);
