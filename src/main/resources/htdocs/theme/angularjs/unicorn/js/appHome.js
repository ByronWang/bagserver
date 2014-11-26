'use strict';


/* App Module */
angular.module('nebula', ['nebulaFilters', 'nebulaServices', 'nebulaDirectives','angularTree','ngResource','ngCookies']).
	config(['$routeProvider', function($routeProvider,$cookies) {
		$routeProvider.
			when('/d/ContactRecord', {templateUrl: '/theme/angularjs/unicorn/ContactRecord-list.html', controller: ContactRecordsCtrl}).

			/* Attached Entity edit */
			when('/d/:typename/:id/setting/info', {templateUrlWP: '/theme/angularjs/unicorn/setting-{{typename}}-basic-info.html',   controller: EntityCtrl}).
			when('/d/:attachedtypename/:attachedid/setting/:typename/', {templateUrlWP: '/theme/angularjs/unicorn/setting-{{attachedtypename}}-{{typename}}-basic-list.html',   controller: AttachedEntityListCtrl}).
			when('/d/:attachedtypename/:attachedid/:typename/!new', {templateUrlWP: '/theme/angularjs/unicorn/{{attachedtypename}}-{{typename}}-basic-home.html',   controller: AttachedNewEntityCtrl}).
			when('/d/:attachedtypename/:attachedid/:typename/', {templateUrlWP: '/theme/angularjs/unicorn/{{attachedtypename}}-{{typename}}-basic-list.html',   controller: AttachedEntityListCtrl}).
			when('/d/:attachedtypename/:attachedid/:typename/:id', {templateUrlWP: '/theme/angularjs/unicorn/{{attachedtypename}}-{{typename}}-basic-home.html',   controller: AttachedEntityCtrl}).

			/* Basic Entity edit */
			when('/d/:typename', {templateUrlWP: '/theme/angularjs/unicorn/{{typename}}-listview.html',   controller: EntityListCtrl}).
			when('/d/:typename/!new', {templateUrlWP: '/theme/angularjs/unicorn/{{typename}}-home.html', controller: NewEntityCtrl}).
			when('/d/:typename/:id', {templateUrlWP: '/theme/angularjs/unicorn/{{typename}}-home.html', controller: EntityCtrl}).

			when('/welcome', {templateUrl: '/theme/angularjs/unicorn/welcome.html', controller: DoNothingCtrl}).
			otherwise({redirectTo: '/welcome'});
	}])	.run(function($rootScope, $location,  $interpolate) {
	 	$rootScope.$on('$routeChangeStart', function(event,next,last) {
			if(next.$route && next.$route.templateUrlWP){
	 			next.$route.templateUrl = $interpolate(next.$route.templateUrlWP)(next.pathParams);
	 		}
	});
});

(function($) { 
	$("input[required]").parents(".control-group").find(".control-label").addClass("required");
})(jQuery);


