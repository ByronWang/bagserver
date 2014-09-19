'use strict';


// Declare app level module which depends on filters, and services
var nebulaModule = angular.module('test-app', ['nebulaDirectives','angularTree','ngResource','ngCookies']);

nebulaModule.config(['$routeProvider', function($routeProvider,$cookies) {
	$routeProvider.otherwise({redirectTo: '/main'});
}]);

