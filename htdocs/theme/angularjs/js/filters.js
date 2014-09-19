'use strict';

/* Filters */

angular.module('nebula.filters', []).filter('checkmark', function() {
  return function(input) {
    return input ? '\u2713' : '\u2718';
  };
}).filter('replace', function() {
	  return function(input,regexp,replacement) {
		    return input.replace(regexp,replacement);
		  };
}).filter('startFrom', function() {
    return function(input, start) {
        start = +start; //parse to int
        return input.slice(start);
    }
}).filter('select', function() {
    return function(input,valueWhenTrue, valueWhenFalse) {
        return input?valueWhenTrue:valueWhenFalse;
    }
});
