/**
 * @module Display Pay Advice List
 */
sap.ui.define([ 
	"com/costco/admin/controller/BaseController",
	"sap/ui/model/json/JSONModel",
	"com/costco/admin/model/formatter",
	"jquery.sap.global",
	"sap/ui/model/Filter",
	"sap/ui/model/FilterOperator",
	"sap/m/PDFViewer",
	"sap/m/MessageBox"
], function(BaseController, JSONModel, formatter, jQuery, Filter, FilterOperator, PDFViewer, MessageBox) {
	"use strict";
	
	var that;
	
    return BaseController.extend("com.costco.payadvice.controller.DisplayPayAdvice", {

		formatter : formatter,

		onInit: function() {
			that = this;
			this.getRouter().getRoute('DisplayPayAdvice').attachMatched(this._onRouteMatched, this);
			this.getView().setModel(new JSONModel());
			this.getView().setModel(new JSONModel({
				busy: true
			}), "display");
		},

		_onRouteMatched: function() {
			var proxyUserId = this.getProxyUserId();
			if (!proxyUserId) {
				this.navToHomepage();
			} else {
				this._updateEmployeePayAdvice(proxyUserId);
			}
		},

		_updateEmployeePayAdvice: function(userId) {
			this._setBusy(true);
			this._loadEmployeePayAdvice(userId)
			.then((payAdvice) => {
				this.getModel().setProperty('/', payAdvice);
			})
			.catch((err) => {
				MessageBox.error(err.message, {
					onClose: () => {
						this.navToHomepage();
					}
				});
			})
			.then(() => {
				this._setBusy(false);
			});
		},

		_setBusy: function(busy) {
			this.getModel('display').setProperty('/busy', busy);
		},

		_loadEmployeePayAdvice: function(userId) {
			var url = `/a/admin/payadviceList/${userId}`;
			return this.httpGet(this.getUri(url));
			// TODO: replace mock
			// return this.httpGet(this.getUri(url)).then((payAdviceData) => {
			// 	payAdviceData.payadviceList = this._mockPayAdviceItems();
			// 	return payAdviceData;
			// });
		},

		_mockPayAdviceItems: function() {
			var now = new Date();
			var list = [];
			for (var i = 1; i <= 16; ++i) {
				var effectiveStartDate = new Date();
				effectiveStartDate.setMonth(now.getMonth() - i);
				list.push({
					inboxName: `inbox name ${i}`,
					externalName: `external user ${i}`,
					filename: `pay advice ${i} custPeriodEnd ${i + 1}`,
					effectiveStartDate: effectiveStartDate,
					custPeriodEnd: `custPeriodEnd ${i + 1}`
				});
			}
			return list;
		},

		onPayAdviceTableUpdateFinished: function(evt) {
			var showingItemsTotal = evt.getParameter('total');
			var itemsTotal = showingItemsTotal;
			try {
				 itemsTotal = evt.getSource().getModel().getProperty('/payadviceList').length;
			} catch(e) {
				console.trace(e);
			}
			var payAdvicesText = this.getOwnerComponent().getModel("i18n").getProperty('txtPayAdvices');
			var newTitle = `${payAdvicesText} (${showingItemsTotal}/${itemsTotal})`;
			var pageSection = evt.getSource().getParent().getParent();
			pageSection.setTitle(newTitle);
		},

		updateFilter: function(evt) {
			var query = evt.getParameter('query');
			this._filterPayAdvice(query);
		},

		_filterPayAdvice: function(query) {
			var custPeriodEndFilter = new Filter('custPeriodEnd', FilterOperator.Contains, query);
			var externalNameFilter = new Filter('externalName', FilterOperator.Contains, query);
			var combinedFilter = new Filter([custPeriodEndFilter, externalNameFilter], false);
			this.byId('payAdviceTable').getBinding('items').filter(combinedFilter);
		},

		/**
		* Event handler when {@link sap.m.ColumnListItem} request is selected
		* 
		* @memberof module:module:Display Pay Advice List
		* 
		* @param {sap.ui.base.Event}
		*                oEvent
		*/
		onListItemPress: function(evt) {
			var bindingContext = evt.getSource().getBindingContext();
			var externalCode = bindingContext.getProperty('externalCode');
			var effectiveStartDate = Date.parse(bindingContext.getProperty('effectiveStartDate'));
			var userId = this.getProxyUserId();
			var filename =  bindingContext.getProperty('filename');
			filename = filename.replace(/\.[^/.]+$/, '');
			var url = `/a/admin/payadvicePdf/${externalCode}/${effectiveStartDate}/${userId}/${filename}`;
			var payadviceName =  bindingContext.getProperty('externalName');
			this._displayPdf(this.getUri(url), payadviceName);
		},
		
		/**
		* A PDF viewer opening as a popup dialog
		* 
		* @memberof module:Display Pay Advice List
		* @private
		* @param {string} url for pay advice pdf api
		* @param {string} title of selected pay advice
		*/
		_displayPdf: function(url, title) {
			this._pdfViewer = new PDFViewer();
			this.getView().addDependent(this._pdfViewer);
			this._pdfViewer.setTitle(title);
			this._pdfViewer.setSource(url);
			this._pdfViewer.setShowDownloadButton(false);
			this._pdfViewer.open();
		}

    });

});