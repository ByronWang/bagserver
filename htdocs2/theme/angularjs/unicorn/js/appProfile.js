'use strict';

/* App Module */
angular.module('nebula', ['nebulaFilters', 'nebulaServices','nebulaDirectives','angularTree','ngResource','ngCookies']).
	config(['$routeProvider', function($routeProvider) {
		$routeProvider.
			when('/d/:typename', {templateUrlWP: '/theme/angularjs/unicorn/{{typename}}-list.html',   controller: EntityListCtrl}).
			when('/d/:typename/!new', {templateUrlWP: '/theme/angularjs/unicorn/{{typename}}-detail.html', controller: NewEntityCtrl}).
			when('/d/:typename/:id', {templateUrlWP: '/theme/angularjs/unicorn/{{typename}}-detail.html', controller: EntityCtrl}).
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
