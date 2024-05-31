/**
 * @module Display Address Base
 */
sap.ui.define([ "sap/ui/core/mvc/Controller" ], function(Controller) {
	"use strict";

	return Controller.extend("com.costco.address.controller.BaseController", {
		/**
		 * Convenience method for accessing the router.
		 * 
		 * @memberof module:Display Pay Advice Base
		 * 
		 * 
		 * @returns {sap.ui.core.routing.Router} the router for this component
		 */
		getRouter : function() {
			return sap.ui.core.UIComponent.getRouterFor(this);
		},

		/**
		 * Convenience method for getting the view model by name.
		 * 
		 * @memberof module:PDisplay Pay Advice Base
		 * 
		 * 
		 * @param {string}
		 *            [sName] the model name
		 * @returns {sap.ui.model.Model} the model instance
		 */
		getModel : function(sName) {
			return this.getView().getModel(sName);
		},

		/**
		 * Convenience method for setting the view model.
		 * 
		 * @memberof module:Display Pay Advice Base
		 * 
		 * @param {sap.ui.model.Model}
		 *            oModel the model instance
		 * @param {string}
		 *            sName the model name
		 * @returns {sap.ui.mvc.View} the view instance
		 */
		setModel : function(oModel, sName) {
			return this.getView().setModel(oModel, sName);
		},

		/**
		 * Getter for the resource bundle.
		 * 
		 * @memberof module:Display Pay Advice Base
		 * 
		 * @returns {sap.ui.model.resource.ResourceModel} the resourceModel of
		 *          the component
		 */
		getResourceBundle : function() {
			return this.getOwnerComponent().getModel("i18n").getResourceBundle();
		},

		/**
		 * Getter for base URI.
		 * 
		 * @memberof module:Display Pay Advice Base
		 * @param {string}
		 *            uribase
		 * @returns {string} new uri
		 */
		getUri : function(uribase) {
			if (sap.ushell === undefined) {
				return "/globalterminatedemp" + uribase;
			}
			if (sap.ushell.Container === undefined) {
				return "/globalterminatedemp" + uribase;
			}
			if (sap.flp.FaasInitUtils.getSiteType() === "flp") {
			   //fiori launchpad
			    var componentURI = sap.ushell.services.AppConfiguration.getCurrentApplication().applicationDependencies.html5AppName;
			    return "/"+componentURI +uribase;
			    
			}
			return sap.ushell.Container.getService("URLHelper").createComponentURI(this.getOwnerComponent().getId(), "/.." + uribase);

		}

	});

});
