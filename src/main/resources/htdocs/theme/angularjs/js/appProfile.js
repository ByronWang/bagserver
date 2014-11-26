'use strict';

/* App Module */
angular.module('nebula', [
  'ngRoute','ngResource','ngAnimate','ngCookies','angularTree',
  'nebula.filters',
  'nebula.services',
  'nebula.directives',
  'nebula.controllers'
]).config(['$routeProvider',function($routeProvider) {
		$routeProvider.when('/d/:typename', {templateUrlWP: '/theme/angularjs/unicorn/{{typename}}-list.html',   controller: 'EntityListCtrl'});
		$routeProvider.when('/d/:typename/!new', {templateUrlWP: '/theme/angularjs/unicorn/{{typename}}-detail.html', controller: 'NewEntityCtrl'});
		$routeProvider.when('/d/:typename/:id', {templateUrlWP: '/theme/angularjs/unicorn/{{typename}}-detail.html', controller: 'EntityCtrl'});
		$routeProvider.when('/welcome', {templateUrl: '/theme/angularjs/unicorn/welcome.html', controller: 'DoNothingCtrl'});
		$routeProvider.otherwise({redirectTo: '/welcome'});

}]).run(function($rootScope, $location,  $interpolate) {
 	$rootScope.$on('$routeChangeStart', function(event, next, last) {
		if (next.templateUrlWP) {
			next.templateUrl = $interpolate(next.templateUrlWP)(next.pathParams);
		}
 	});
});