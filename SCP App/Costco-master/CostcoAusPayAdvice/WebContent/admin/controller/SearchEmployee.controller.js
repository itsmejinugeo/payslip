/**
 * @module Administrator Employee Search
 */
sap.ui.define([ "com/costco/au/admin/controller/BaseController" ], function(BaseController) {
    "use strict";
    var that;
    return BaseController.extend("com.costco.au.admin.controller.SearchEmployee", {
	/**
	 * Controller's initialization
	 * 
	 * @memberof module:Administrator Employee Search
	 */
	onInit : function() {
	    that = this;
	    that.runningSearchCount = 0;

	    var displayModel = new sap.ui.model.json.JSONModel({
		"globalFilter" : "",
		"showApp" : false,
		"count" : 0
	    });
	    this.getView().setModel(displayModel, "display");
	    
	    var splashScreen = document.getElementsByClassName("splash-screen-div");
	    if (splashScreen[0] !== undefined) {
		splashScreen[0].style.display = "inline";
	    }
	    
	    this.getView().addEventDelegate({
		onAfterShow : function(oEvent) {
		    // focus handling
		    that.getView().byId("searchInput").focus();
		}
	    });
	    
	    
	    var delayInMilliseconds = 1500; //1.5 second

	    setTimeout(function() {

		    displayModel.setData({
			"showApp" : true,
			"count" : 0
		    });
		    that.getView().setModel(displayModel, "display");
		    var splashScreen = document.getElementsByClassName("splash-screen-div");

		    if (splashScreen[0] !== undefined) {
			splashScreen[0].style.display = "none";
		    }
		    
	    }, delayInMilliseconds);
	    
	    
	},

	/**
	 * Event handler for {@link sap.m.List} when list item is pressed
	 * 
	 * @memberof module:Administrator Employee Search
	 * 
	 * @param {sap.ui.base.Event}
	 *                oEvent
	 */
	onListItemPressed : function(event) {

	    var oItem, oCtx, oEmployee;

	    oItem = event.getSource();
	    oCtx = oItem.getBindingContext();
	    oEmployee = this.getView().getModel().getObject(oCtx.getPath());
	    
	    
		var promise = this._retrievePayAdvicesForUser(oEmployee);
		var oRouter = sap.ui.core.UIComponent.getRouterFor(this);
		oRouter.initialize();

		promise.done(function(data) {

			if (data !== undefined && data.error !== undefined && data.error === true) {
				// do nothing - error dialog should have been shown
			} else {
				oRouter.navTo("payadviceList", {
					userId : oEmployee.userId

				});
			}
		});
		
	},

	/**
	 * Retrieve pay advice list for selected employee
	 * 
	 * @memberof module:Administrator Employee Search
	 * @private
	 * @param {string}
	 *            userId
	 */
	_retrievePayAdvicesForUser : function(oSelectedEmployee) {
		var def = new jQuery.Deferred();

		var dialogTitleModel = new sap.ui.model.json.JSONModel();

		this.getView().setModel(dialogTitleModel, "dialogTitle");
		dialogTitleModel.setProperty("/selectedEmployeeId", oSelectedEmployee.userId);
		dialogTitleModel.setProperty("/selectedEmployeeName", oSelectedEmployee.defaultFullName);
		
		if (!this._dialogLoad) {
			this._dialogLoad = sap.ui.xmlfragment("com.costco.au.admin.view.fragments.LoadingEmployeePayAdviceDialog", this);
			this.getView().addDependent(this._dialogLoad);
		}

		// open dialog
		jQuery.sap.syncStyleClass("sapUiSizeCompact", this.getView(), this._dialogLoad);
		this._dialogLoad.open();

		
		jQuery.ajax({
			type : "GET",
			url : this.getUri("/a/adminPayadviceList/" + oSelectedEmployee.userId),
			dataType : 'json',
			async : true,

			success : function(data) {
				var oSelectedEmployeeModel = new sap.ui.model.json.JSONModel();
				oSelectedEmployeeModel.setData({
					selectedEmployee: data
				});
				sap.ui.getCore().setModel(oSelectedEmployeeModel, "selectedEmployee");
				that.getView().setModel(oSelectedEmployeeModel, "selectedEmployee");
				def.resolve("success");
			},
			complete : function() {
				that._dialogLoad.close();
			},
			error : function(data, textStatus, jqXHR) {
				sap.m.MessageToast.show(textStatus);
				def.resolve(jqXHR.response);
				
//				this.getRouter().navTo("searchEmployee");
//					return;
			}
		});

		return def.promise();
	},
	
	/**
	 * Event handler for {@link sap.m.SearchField} to clear input field
	 * 
	 * @memberof module:Administrator Employee Search
	 * 
	 * @param {sap.ui.base.Event}
	 *                oEvent
	 */
	handleInputClear : function(event) {

	    if (event.getParameter("clearButtonPressed")) {
		var oEmployeeListModel = new sap.ui.model.json.JSONModel();
		var oView = this.getView();
		oEmployeeListModel.setData({
		    searchEmployeeList : []
		});
		oView.setModel(oEmployeeListModel);
	    }

	},

	/**
	 * Event handler for {@link sap.m.SearchField} for search input field
	 * 
	 * @memberof module:Administrator Employee Search
	 * 
	 * @param {sap.ui.base.Event}
	 *                oEvent
	 */
	handleEmployeeInput : function(oEvent) {
	    var numbersOnly = /^\d+$/;
	    var filter = oEvent.getParameter("newValue");
	    var input = oEvent.getSource();

	    if (filter.match(numbersOnly)) {
		// Filter by userId
		if (filter.length > 6) {
		    this._searchEmployee(filter, input);
		}
	    } else {
		// Filter by name
		if (filter.length > 3) {
		    this._searchEmployee(filter, input);
		}
	    }

	},

	/**
	 * Search employee
	 * 
	 * @memberof module:Administrator Employee Search
	 * @private
	 * @param {string}
	 *                filter
	 */
	_searchEmployee : function(filter, input) {

	    var url = "/a/adminEmployeeSearch" + "?filter=" + filter;
	    var oEmployeeListModel = new sap.ui.model.json.JSONModel();
	    var oView = this.getView();

	    that.runningSearchCount++;
	    that.byId("searchIndicator").setVisible(true);
	    jQuery.ajax({
		type : "GET",
		url : this.getUri(url),
		dataType : 'json',
		async : true,

		success : function(data) {
		    if (filter === input.getValue()) {
			oEmployeeListModel.setData({
			    searchEmployeeList : data
			});
			oView.setModel(oEmployeeListModel);
		    }
		},
		error : function(data, textStatus, jqXHR) {

		    oEmployeeListModel.setData({
			searchEmployeeList : data
		    });
		    oView.setModel(oEmployeeListModel)
		    sap.m.MessageToast.show(textStatus);
		},
		complete : function(jqXHR, testStatus) {
		    that.runningSearchCount--;
		    if (that.runningSearchCount == 0) {
			that.byId("searchIndicator").setVisible(false);
		    }
		}
	    });
	},

    });
});