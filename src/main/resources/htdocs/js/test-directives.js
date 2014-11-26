'use strict';
/*utility */
function isUndefined(value) {
	return typeof value == 'undefined';
}
function isEmpty(value) {
	return isUndefined(value) || value === '' || value === null
			|| value !== value;
}


var nbTree = ['$compile', '$timeout',  function(){
	  return {
		    template: '<ul class="uiTree">{{tree}}<li ng-repeat="node in tree">{{node.Name}}</li></ul>',
		    replace: true,
		    transclude: true,
		    restrict: 'E',
		    scope: {
		      tree: '=ngModel',
		      attrNodeId: "@",
		      loadFn: '=',
		      expandTo: '=',
		      selectedId: '='
		    },
		    controller: function($scope, $element, $attrs,$transclude) {
		    	alert("hello");
		    }
	  };
}];


angular.module('nebulaDirectives', [])
	.directive('nbTree', nbTree)
	;
	/*.directive('popup', neFromListDirective);*/
