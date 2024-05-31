sap.ui.define([], function () {
	"use strict";
	return {
		formatDate : function(value) {

			if (value !== null && typeof value !== "undefined") {
				var newDate = new Date(value).toLocaleDateString("en-AU");
				return newDate;
			} else {
				return " ";
			}
		},
		
		jsDate : function(value) {

			var newDate = new Date(value);
			return newDate;
		}
		
	};
});
		