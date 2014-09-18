'use strict';


/* App Module */
angular.module('nebula', [
  'ngRoute','ngResource','ngAnimate','ngCookies','angularTree',
  'nebula.filters',
  'nebula.services',
  'nebula.directives',
  'nebula.controllers'
]).config(['$routeProvider',function($routeProvider) {
			$routeProvider.when('/d/ContactRecord', {templateUrl: '/theme/angularjs/unicorn/ContactRecord-list.html', controller: 'ContactRecordsCtrl'});
			/* Attached Entity edit */
			$routeProvider.when('/d/:typename/:id/setting/info', {templateUrlWP: '/theme/angularjs/unicorn/setting-{{typename}}-basic-info.html',   controller: 'EntityCtrl'});
			$routeProvider.when('/d/:attachedtypename/:attachedid/setting/:typename/', {templateUrlWP: '/theme/angularjs/unicorn/setting-{{attachedtypename}}-{{typename}}-basic-list.html',   controller: 'AttachedEntityListCtrl'});
			$routeProvider.when('/d/:attachedtypename/:attachedid/:typename/!new', {templateUrlWP: '/theme/angularjs/unicorn/{{attachedtypename}}-{{typename}}-basic-home.html',   controller: 'AttachedNewEntityCtrl'});
			$routeProvider.when('/d/:attachedtypename/:attachedid/:typename/', {templateUrlWP: '/theme/angularjs/unicorn/{{attachedtypename}}-{{typename}}-basic-list.html',   controller: 'AttachedEntityListCtrl'});
			$routeProvider.when('/d/:attachedtypename/:attachedid/:typename/:id', {templateUrlWP: '/theme/angularjs/unicorn/{{attachedtypename}}-{{typename}}-basic-home.html',   controller: 'AttachedEntityCtrl'});
			/* Basic Entity edit */
			$routeProvider.when('/d/:typename', {templateUrlWP: '/theme/angularjs/unicorn/{{typename}}-listview.html',   controller: 'EntityListCtrl'});
			$routeProvider.when('/d/:typename/!new', {templateUrlWP: '/theme/angularjs/unicorn/{{typename}}-home.html', controller: 'NewEntityCtrl'});
			$routeProvider.when('/d/:typename/:id', {templateUrlWP: '/theme/angularjs/unicorn/{{typename}}-home.html', controller: 'EntityCtrl'});
			$routeProvider.when('/welcome', {templateUrl: '/theme/angularjs/unicorn/welcome.html', controller: 'DoNothingCtrl'});
			$routeProvider.otherwise({redirectTo: '/welcome'});
			
}]).run(function($rootScope, $location,  $interpolate) {
 	$rootScope.$on('$routeChangeStart', function(event, next, last) {
		if (next.templateUrlWP) {
			next.templateUrl = $interpolate(next.templateUrlWP)(next.pathParams);
		}
 	});
});