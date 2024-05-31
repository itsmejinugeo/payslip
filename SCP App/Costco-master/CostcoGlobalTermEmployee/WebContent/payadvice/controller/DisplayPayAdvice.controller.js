/**
 * @module Display Pay Advice List
 */
sap.ui.define([ "com/costco/payadvice/controller/BaseController", "sap/ui/model/json/JSONModel", "com/costco/payadvice/model/formatter", "jquery.sap.global", "sap/ui/core/routing/History",
	"sap/ui/core/format/DateFormat", "sap/ui/model/Filter", "sap/ui/model/FilterOperator",	"sap/m/PDFViewer" ], function(BaseController, JSONModel, formatter, jQuery, History, DateFormat, Filter, FilterOperator, PDFViewer) {
    "use strict";
    var that;
    return BaseController.extend("com.costco.payadvice.controller.DisplayPayAdvice", {

	formatter : formatter,

	/**
	 * Controller's initialization
	 * 
	 * @memberof module:Display Pay Advice List
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
	    this._oGlobalFilter = null;

	    var oRouter = this.getRouter();

	    oRouter.attachRouteMatched(function(oEvent) {
		var sRouteName;
		sRouteName = oEvent.getParameter("name");
		if (sRouteName === "payadviceList") {
		    this._onRouteMatched(oEvent);
		}
	    }, this);

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

	    this._loadPayAdvice("/a/payadviceList");
	},

	/**
	 * Event handler for {@link sap.m.SearchField} to filter
	 * 
	 * @memberof module:Display Pay Advice List
	 * 
	 * @param {sap.ui.base.Event}
	 *                oEvent
	 */
	filterGlobally : function(oEvent) {
	    var sQuery = oEvent.getParameter("query");
	    this._oGlobalFilter = null;

	    if (sQuery) {
		this._oGlobalFilter = new Filter([ new Filter("custPeriodEnd", FilterOperator.Contains, sQuery), new Filter("externalName", FilterOperator.Contains, sQuery) ], false);
	    }

	    this._filter();
	    this._updateTableCounter();

	},

	/**
	 * Filter pay advice list
	 * 
	 * @memberof module:Display Pay Advice List
	 * @private
	 */	
	_filter : function() {
	    var oFilter = null;

	    if (this._oGlobalFilter) {
		oFilter = new sap.ui.model.Filter([ this._oGlobalFilter ], true);
	    } else if (this._oGlobalFilter) {
		oFilter = this._oGlobalFilter;
	    }

	    this.byId("payadvicetable").getBinding("items").filter(oFilter, "Application");
	},

	/**
	 * Update number of pay advices 
	 * 
	 * @memberof module:Display Pay Advice List
	 * @private
	 */
	_updateTableCounter : function() {
	    var count = this.byId("payadvicetable").getBinding("items").getLength();
	    var displayModel = this.getView().getModel("display");
	    displayModel.getData().count = count;
	    displayModel.refresh();
	},

	/**
	 * Retrieve pay advice and data binding
	 * 
	 * @memberof module:Display Pay Advice List
	 * @private
	 * @param {string} url for pay advice list api
	 */
	_loadPayAdvice : function(url) {

	    var oView = this.getView();
	    var oPayAdviceModel = new sap.ui.model.json.JSONModel();
	    sap.ui.getCore().setModel(oPayAdviceModel, "payadvicelist");
	    var displayModel = this.getView().getModel("display");
	    displayModel.setData({
		"showApp" : false,
		"count" : 0
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
		    oPayAdviceModel.setProperty("/overview", data);
		    var count = data.payadviceList.length;
		    oView.setModel(oPayAdviceModel);
		    displayModel.setData({
			"showApp" : true,
			"count" : count
		    });
		    var splashScreen = document.getElementsByClassName("splash-screen-div");

		    if (splashScreen[0] !== undefined) {
			splashScreen[0].style.display = "none";
		    }
		}
	    });

	    this.getView().setModel(oPayAdviceModel);
	},

	/**
	 * Event handler when {@link sap.m.ColumnListItem} request is selected
	 * 
	 * @memberof module:module:Display Pay Advice List
	 * 
	 * @param {sap.ui.base.Event}
	 *                oEvent
	 */
	onListItemPress : function(oEvent) {
	    var externalCode = oEvent.getSource().getBindingContext().getProperty("externalCode");
	    var payadviceName =  oEvent.getSource().getBindingContext().getProperty("externalName");
	    var filename =  oEvent.getSource().getBindingContext().getProperty("filename");
	    var effectiveStartDateStr = oEvent.getSource().getBindingContext().getProperty("effectiveStartDate");
	    var effectiveStartDate = Date.parse(effectiveStartDateStr);
	    var url = "/a/payadvicepdf/" + externalCode +"/" +effectiveStartDate+"/" +filename.replace(/\.[^/.]+$/, "");
	    this._displayPdf(this.getUri(url),payadviceName);
	},
	
	/**
	 * A PDF viewer opening as a popup dialog
	 * 
	 * @memberof module:Display Pay Advice List
	 * @private
	 * @param {string} url for pay advice pdf api
	 * @param {string} title of selected pay advice
	 */
	_displayPdf : function(url,title) {
	    this._pdfViewer = new PDFViewer();
	    this.getView().addDependent(this._pdfViewer);
	    this._pdfViewer.setTitle(title);
	    this._pdfViewer.setSource(url);
	    this._pdfViewer.setShowDownloadButton(false);
	    this._pdfViewer.open();
	}

    });

});