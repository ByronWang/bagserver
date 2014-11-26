'use strict';


// Declare app level module which depends on filters, and services
angular.module('nebula', [
  'ngRoute','ngResource','ngAnimate','ngCookies','angularTree',
  'nebula.filters',
  'nebula.services',
  'nebula.directives',
  'nebula.controllers'
]).config(['$routeProvider', function($routeProvider) {
	/* Template edit */
	$routeProvider.when('/t/template/:typename-:cat.html.ftl', {templateUrl: '/theme/angularjs/unicorn/Theme-edit.html', controller: "FreeMarkerCtrl"});
	$routeProvider.when('/t/theme/:theme/:skin/:typename-:cat.html', {templateUrl: '/theme/angularjs/unicorn/Theme-edit.html', controller: "AngularJSCtrl"});

	/* Type edit */
	$routeProvider.when('/d/Type/', {templateUrl: '/theme/angularjs/unicorn/Type-list.html',   controller: "TypeListCtrl"});
	$routeProvider.when('/d/Type/!new', {templateUrl: '/theme/angularjs/unicorn/Type-detail.html', controller: "NewTypeCtrl"});
	$routeProvider.when('/d/Type/:id', {templateUrl: '/theme/angularjs/unicorn/Type-detail.html', controller: "TypeCtrl"});

	/* Attached Entity edit */
	$routeProvider.when('/d/:typename/:id/setting/info', {templateUrlWP: '/theme/angularjs/unicorn/setting-{{typename}}-basic-info.html',   controller: "EntityCtrl"});
	$routeProvider.when('/d/:attachedtypename/:attachedid/setting/:typename/', {templateUrlWP: '/theme/angularjs/unicorn/setting-{{attachedtypename}}-{{typename}}-basic-list.html',   controller: "AttachedEntityListCtrl"});
	$routeProvider.when('/d/:attachedtypename/:attachedid/:typename/', {templateUrlWP: '/theme/angularjs/unicorn/{{attachedtypename}}-{{typename}}-basic-list.html',   controller: "AttachedEntityListCtrl"});

	/* Basic Entity edit */
	$routeProvider.when('/d/:typename', {templateUrlWP: '/theme/angularjs/unicorn/{{typename}}-list.html',   controller: "EntityListCtrl"});
	$routeProvider.when('/d/:typename/!new', {templateUrlWP: '/theme/angularjs/unicorn/{{typename}}-detail.html', controller: "NewEntityCtrl"});
	$routeProvider.when('/d/:typename/:id', {templateUrlWP: '/theme/angularjs/unicorn/{{typename}}-detail.html', controller: "EntityCtrl"});

	$routeProvider.when('/welcome', {templateUrl: '/theme/angularjs/unicorn/welcome.html', controller: "DoNothingCtrl"});
	$routeProvider.otherwise({redirectTo: '/welcome'});
}]).run(function($rootScope, $location,  $interpolate) {
 	$rootScope.$on('$routeChangeStart', function(event, next, last) {
		if (next.templateUrlWP) {
			next.templateUrl = $interpolate(next.templateUrlWP)(next.pathParams);
		}
 	});
});
