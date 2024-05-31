sap.ui.define([ 
	"com/costco/admin/controller/BaseController", 
	"jquery.sap.global",
    "sap/ui/model/json/JSONModel",
    "sap/m/BusyDialog",
    "sap/m/MessageBox"
], function(BaseController, jQuery, JSONModel, BusyDialog, MessageBox) {
    "use strict";

    var that;

    return BaseController.extend("com.costco.admin.controller.Homepage", {

        onInit : function() {
        	that = this;
            this.getRouter().getRoute("Root").attachMatched(this._onRouteMatched, this);
        },

        _onRouteMatched: function() {
            if (!this._getModel('proxy').getProperty('/isProxied')) {
                this._initProxyModel();
            }
        },

        _initProxyModel: function() {
        	var instructionUser = {
                defaultFullName: "Click to search and proxy as employee",
                profilePhotoURL: "sap-icon://employee-lookup",
                isFakeUser: true
            }
            this._updateProxyUser(instructionUser);
        },
        
        _updateProxyUser: function(employee) {
            var proxyModel = this._getModel('proxy');
            proxyModel.setProperty('/user', employee);
            if (employee && !employee.isFakeUser) {
                proxyModel.setProperty('/isProxied', true);
                this.setProxyUserId(employee.userId);
                this.setProxyEmpId(employee.empId);
            } else {
                proxyModel.setProperty('/isProxied', false);
                this.setProxyUserId(undefined);
                this.setProxyEmpId(undefined);
            }
        },

        _getModel: function(name) {
            var model = this.getView().getModel(name);
            if (!model) {
                model = new JSONModel();
                this.getView().setModel(model, name);
            }
            return model;
        },
        
        onOpenEmployeeSearchDialog: function() {
            if (!this.employeeSearchDialog) {
				this.employeeSearchDialog = sap.ui.xmlfragment(
                    "com.costco.admin.view.fragments.EmployeeSearchDialog", this);
				this.getView().addDependent(this.employeeSearchDialog);
			}
			this.employeeSearchDialog.open();
        },

        onCloseEmployeeSearchDialog: function() {
            if (this.employeeSearchDialog) {
                this.employeeSearchDialog.close()
            }
            this._updateEmployeeSearchModel([]);
        },

        onEmployeeNameSubmit: function(evt) {
        	var inputControl = evt.getSource();
            var input = evt.getParameter('value');
            if (input && input.length > 2) {
                inputControl.setBusy(true);
            	that._searchEmployeeByName(input)
            	.then((employees) => {
            		that._updateEmployeeSearchModel(employees);
            	})
            	.catch((err) => {
            		sap.m.MessageToast.show('Error searching employee, please try again later');
            		console.error(err);
                })
                .then(() => {
                    inputControl.setBusy(false);
                });
            } else {
                sap.m.MessageToast.show('At least type something...');
            }
        },

        onSearchButtonClick: function(evt) {
            var inputControl = evt.getSource().getParent()
                    .getControlsByFieldGroupId('SearchInput')[0];
            var input = inputControl.getValue();
            inputControl.fireSubmit({
                value: input
            });
        },
        
        _searchEmployeeByName: function(name) {
            var queryStr = '?name=' + name;
            return this.httpGet(this.getUri('/a/admin/searchTerminatedEmployee' + queryStr));
        },
        
        _updateEmployeeSearchModel: function(employees) {
        	var employeeSearchModel = this._getModel('employeeSearch');
        	employeeSearchModel.setProperty('/', employees);
        },
        
        onEmployeeSelected: function(evt) {
        	var path = evt.getSource().getBindingContextPath();
        	var model = evt.getSource().getModel('employeeSearch');
        	var selectedEmployee = model.getProperty(path);
            that._updateProxyUser(selectedEmployee);
            that.onCloseEmployeeSearchDialog();
        },

        onMyAddressClick: function() {
            this.getRouter().navTo('AdminAddress');
        },

        onPayAdviceClick: function() {
            this.getRouter().navTo('DisplayPayAdvice');
        },

        onPayRemStatementClick: function() {
            this._navToExternalApp('PayRemApp');
        },

        onTaxFormClick: function() {
            this._navToExternalApp('TaxFormApp');
        },

        _navToExternalApp: function(appName) {
            var proxyEmpId = this.getProxyEmpId();
            this._setBusy(true);
            this._getExternalApp(appName, proxyEmpId)
            .then((externalApp) => {
                this._openUrlInNewTab(externalApp.url);
            })
            .catch((err) => {
                console.error(err);
                MessageBox.error(err.message);
            })
            .then(() => {
                this._setBusy(false);
            });
        },

        _getExternalApp: function(appName, empId) {
            var url = `/a/admin/externalApp?appName=${appName}&empId=${empId}`;
            return this.httpGet(this.getUri(url));
        },

        _openUrlInNewTab: function(url) {
            window.open(url, '_blank');
        },

        _setBusy: function(isBusy) {
            if (!this._busyDialog) {
                this._busyDialog = new BusyDialog();
                this.getView().addDependent(this._busyDialog, this);
            }
            if (isBusy) {
                this._busyDialog.open();
            } else {
                this._busyDialog.close();
            }
        }

    });
});
