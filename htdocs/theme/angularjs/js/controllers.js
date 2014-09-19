/* Controllers */

angular.module('nebula.controllers', []).
  controller('MyCtrl1', ['$scope','$routeParams','$resource',function($scope,$routeParams,$resource) {
		var MenuResource = $resource('css/app.css', $routeParams);
		$scope.MenuData = MenuResource.get($routeParams);
}]).controller('DoNothingCtrl', ['$scope', '$routeParams',function($scope, $routeParams) {
	'use strict';
	$scope.$routeParams = $routeParams;
}]).controller('AppControl', ['$scope', '$cookies','$location',function($scope, $cookies, $location) {
	'use strict';
	$scope.base = $location;
	$scope.LoginUserID = $cookies.LoginUserID;

}]).controller('PaginationCtrl', ['$scope', function($scope){
    $scope.currentPage = 1;
    $scope.pageSize = 20;
    $scope.$setCurrentPage = function(cur){
    	$scope.currentPage = cur;
    }
}]).controller('TreeListCtrl', ['$scope', function($scope){
	$scope.data = null;
	$scope.selectData = function(data,selected){
		if(selected)$scope.data = data;
	}
}]).controller('TypeQuickEditCtrl', ['$scope','$resource','$routeParams', '$location',
                                     function($scope, $resource, $routeParams, $location) {

	'use strict';
	/*
	 * $scope.typename = $routeParams.typename; var DataResource =
	 * $scope.resource = $resource('/d/:typename/:id', $routeParams, {'save':
	 * {method: 'PUT'}}); $scope.data = DataResource.get($routeParams);
	 * $scope.update = true; $scope.$save = function () {
	 * $scope.data.$save(function (u, getResponseHeaders) {
	 * $location.url($interpolate('d/{{typename}}/')($routeParams)); }); };
	 * $scope.$back = function () {
	 * $location.url($interpolate('d/{{typename}}/')($routeParams)); };
	 */

	$scope.$addMenu = function(menuBar, menuName, menuIcon, menuUrl) {
		var MenuResource = $resource('/d/MenuBar/' + menuBar + '',
				$routeParams, {
					'save' : {
						method : 'PUT'
					}
				});
		var MenuData = MenuResource.get($routeParams);
		$.extend(MenuData, {
			Menu : [ {
				"Name" : menuName,
				"IconName" : "icon-adjust",
				"URL" : menuUrl
			} ]
		});
		MenuData.$save();
	}
}]).controller('NewUserCtrl', ['$scope', '$resource','$routeParams', '$location',
                               function($scope, $resource, $routeParams, $location) {
	'use strict';
	$scope.typename = $routeParams.typename;
	var DataResource = $resource('/d/User/', $routeParams);
	$scope.data = new DataResource();
	$scope.$save = function() {
		$scope.data.$save(function(u, getResponseHeaders) {
			$location.url("/login.html");
		});
	};
}]).controller('AttachedEntityListCtrl', ['$scope', '$route','$resource','$routeParams',
                                          function($scope, $route, $resource, $routeParams) {
	'use strict';
	$scope.attachedTypename = $routeParams.attachedtypename;
	$scope.attachedID = $routeParams.attachedid;
	var DataResource = $scope.resource = $resource('/d/:attachedtypename/:attachedid',$routeParams, {});
	$scope.attachedData = DataResource.get($routeParams,function() {
		//$scope.$dataReady();
	});
	
	$scope.typename = $routeParams.typename;
	var DataResource = $resource('/d/:attachedtypename/:attachedid/:typename/', $routeParams, {
		query : {
			method : 'GET',
			params : {},
			isArray : true
		}
	});
	$scope.datalist = DataResource.query(function() {
		//$scope.$dataReady();
	});
	$scope.$reload = function() {
		$scope.datalist = DataResource.query(function() {
			//$scope.$dataReady();
		});
	};
}]).controller('AttachedNewEntityCtrl', ['$scope', '$route','$resource','$routeParams', '$location','$interpolate',
                                         function($scope, $route, $resource, $routeParams, $location,$interpolate) {
	'use strict';
	$scope.attachedTypename = $routeParams.attachedtypename;
	$scope.attachedID = $routeParams.attachedid;	
	$scope.typename = $routeParams.typename;
	
	var DataResource = $resource('/d/:attachedtypename/:attachedid/:typename/', $routeParams,{});
	$scope.data = new DataResource();
		
	var initData = $resource('/d/:attachedtypename/:attachedid/:typename/!new', $routeParams).get($routeParams,function() {
		angular.extend($scope.data,initData);
	});
	
	angular.extend($scope.data,$routeParams);

	$scope.attachedData = $resource('/d/:attachedtypename/:attachedid',$routeParams, {}).get($routeParams,function() {
		angular.forEach($scope.attachedData || {}, function(value, key) {
			$scope.data[$scope.attachedTypename + key] = value;
		});
	});
			
	$scope.$save = function() {
		$scope.data.$save(function(u, getResponseHeaders) {
			$location.url($interpolate('/d/{{attachedtypename}}/{{attachedid}}/{{typename}}/')($routeParams));
		});
	};
}]).controller('AttachedEntityCtrl', ['$scope', '$route','$resource','$routeParams', '$location','$interpolate',
                                      function($scope, $route, $resource, $routeParams, $location,$interpolate) {
	'use strict';
	$scope.attachedTypename = $routeParams.attachedtypename;
	$scope.attachedID = $routeParams.attachedid;
	var DataResource = $scope.resource = $resource('/d/:attachedtypename/:attachedid',$routeParams, {});
	$scope.attachedData = DataResource.get($routeParams,function() {
		//$scope.$dataReady();
	});
	
	$scope.typename = $routeParams.typename;
	var DataResource = $scope.resource = $resource('/d/:typename/:id',
			$routeParams, {
				'save' : {
					method : 'PUT'
				}
			});
	$scope.data = DataResource.get($routeParams,function() {
		//$scope.$dataReady();
	});
	$scope.update = true;
	$scope.$save = function() {
		$scope.data.$save(function(u, getResponseHeaders) {
			$location.url($interpolate('/d/{{attachedtypename}}/{{attachedid}}/{{typename}}/')($routeParams));
		});
	};
	$scope.$back = function() {
		$location.url($interpolate('/d/{{attachedtypename}}/{{attachedid}}/{{typename}}/')($routeParams));
	};
}]).controller('EntityListCtrl', ['$scope', '$route','$resource','$routeParams',
                                  function($scope, $route, $resource, $routeParams) {
	'use strict';
	$scope.typename = $routeParams.typename;
	var DataResource = $resource('/d/:typename/', $routeParams, {
		query : {
			method : 'GET',
			params : {},
			isArray : true
		}
	});
	$scope.datalist = DataResource.query(function() {
		//$scope.$dataReady();
	});
	$scope.$reload = function() {
		$scope.datalist = DataResource.query(function() {
			//$scope.$dataReady();
		});
	};
}]).controller('NewEntityCtrl', ['$scope', '$route','$resource','$routeParams', '$location','$interpolate',
                                 function($scope, $route, $resource, $routeParams,$location, $interpolate) {
	'use strict';
	$scope.typename = $routeParams.typename;

	var DataResource = $resource('/d/:typename/', $routeParams);
	$scope.data = new DataResource();

	var initData = $resource('/d/:typename/!new', $routeParams).get($routeParams,function() {
		angular.extend($scope.data,initData);
	});
	
	$scope.$save =function(action) {
		var $action = 'save';
		if(action){
			$action = action;
		}
		$scope.data.$save({$action: $action},function(u, getResponseHeaders) {
			$location.url($interpolate('/d/{{typename}}/')($routeParams));
		});
	};
	$scope.$back = function() {
		$location.url($interpolate('/d/{{typename}}/')($routeParams));
	};
}]).controller('EntityCtrl', ['$scope', '$resource', '$routeParams', '$location', '$interpolate',
                              function($scope, $resource, $routeParams, $location, $interpolate) {
	'use strict';
	$scope.typename = $routeParams.typename;
	var DataResource = $scope.resource = $resource('/d/:typename/:id',
			$routeParams, {
				'save' : {
					method : 'PUT'
				}
			});
	$scope.data = DataResource.get($routeParams,function() {
		//$scope.$dataReady();
	});
	
	$scope.$ajaxCall = function(params,onSucceed){
		var callData = DataResource.get(params,function() {
			onSucceed($scope,callData);
		});
	};
	
	$scope.update = true;
	$scope.$save = function(action) {
		var $action = 'save';
		if(action){
			$action = action;
		}
		$scope.data.$save({$action: $action},function(u, getResponseHeaders) {
			$location.url($interpolate('/d/{{typename}}/')($routeParams));
		});
	};
	$scope.$back = function() {
		$location.url($interpolate('/d/{{typename}}/')($routeParams));
	};
}]).controller('TypeListCtrl', ['$scope', '$route','$resource','$routeParams',
                                function($scope, $route, $resource, $routeParams) {
	'use strict';
	$scope.typename = "Type";
	var DataResource = $resource('/d/Type/', $routeParams, {
		query : {
			method : 'GET',
			params : $routeParams,
			isArray : true
		}
	});
	$scope.datalist = DataResource.query(function() {
		//$scope.$dataReady();
	});
}]).controller('FlowHistoryCtrl', ['$scope','$resource',
                                function($scope, $resource) {
	'use strict';
	$scope.$watch('$parent.data.flow', function(newValue, oldValue) {
		if(newValue){
			var DataResource = $resource('/d/:typename/:id', {"typename" : $scope.$parent.typename, "id" : newValue.ID}, {
				query : {
					method : 'GET',
					params : {"history" : "history"},
					isArray : false
				}
			});
			$scope.datafull = DataResource.query(function() {
				//$scope.$dataReady();
			});
		}
	});
}]).controller('NewTypeCtrl', ['$scope', '$route','$resource','$routeParams','$http', '$location','$interpolate','$templateCache',
                               function($scope, $route, $resource, $routeParams, $http,$location,$interpolate,$templateCache) {
	'use strict';
	$scope.typename = "Type";
	var DataResource = $resource('/d/Type/', $routeParams);
	$scope.data = {};
	$scope.data.$save = DataResource.prototype.$save;
	$scope.$save = function() {
		$http.post('/d/Type/', $scope.data.Code).success(
				function(data, status, headers, config) {
					$templateCache.removeAll();
					$location.url(data);
				});
	};
	$scope.$back = function() {
		$location.url($interpolate('d/Type/')($routeParams));
	};
}]).controller('TypeCtrl', ['$scope', '$resource','$routeParams','$http', '$location','$interpolate','$templateCache',
                            function($scope, $resource, $routeParams, $http, $location,	$interpolate, $templateCache) {
	'use strict';
	$scope.typename = "Type";
	$scope.resourcename = $interpolate('/d/Type/{{id}}')($routeParams);
	var DataResource = $scope.resource = $resource('/d/Type/:id', $routeParams,
			{
				'save' : {
					method : 'PUT'
				}
			});
	$scope.data = DataResource.get($routeParams,function() {
		//$scope.$dataReady();
	});
	$scope.update = true;
	$scope.$save = function() {
		$http.put($scope.resourcename, $scope.data.Code).success(
				function(data, status, headers, config) {
					$templateCache.removeAll();
					$scope.data = DataResource.get($routeParams);
				});
	};
	$scope.$back = function() {
		$location.url($interpolate('d/Type/')($routeParams));
	};
}]).controller('AttributeEditCtrl', ['$scope','$resource',function($scope, $resource) {
	'use strict';
	$scope.$parent.$loadChild = function(name) {
		var AttributeDataResource = $scope.resource = $resource('/d/Attribute/'
				+ name, {}, {
			'save' : {
				method : 'PUT'
			}
		});
		$scope.entityData = {};
		$scope.entityData = AttributeDataResource.get({},function() {
			//$scope.$dataReady();
		});
		$scope.update = true;
		$scope.$save = function() {
			$scope.entityData.$save(function(u, getResponseHeaders) {
				$scope.$parent.$reload();
				$scope.$parent.$loadChild(name);
			});
			return false;
		};
	};

	$scope.$parent.$newChild = function() {
		var AttributeDataResource = $resource('/d/Attribute/', {});
		$scope.entityData = {};
		$scope.update = false;
		$scope.entityData.Values = [];
		$scope.entityData.$save = AttributeDataResource.prototype.$save;
		$scope.$save = function() {
			$scope.entityData.$save(function(u, getResponseHeaders) {
				$scope.$parent.$reload();
				$scope.$parent.$newChild();
			});
		};
	};
	$scope.$parent.$newChild();
}]).controller('AngularJSCtrl',  ['$scope', '$route','$location', '$http','$routeParams','$templateCache',
                                  function($scope, $route, $location, $http, $routeParams,$templateCache) {
	'use strict';
	$scope.resourcename = extractParams(
			"/theme/:theme/:skin/:typename-:cat.html", $routeParams);
	$http.get($scope.resourcename).success(
			function(data, status, headers, config) {
				$scope.data = {
					code : data
				};
			});
	$scope.newcode = "";
	$scope.$save = function() {
		$http.put($scope.resourcename, $scope.data.code).success(
				function(data, status, headers, config) {
					$scope.data = {
						code : data
					};
					$templateCache.removeAll();
				});
	};
}]).controller('FreeMarkerCtrl', ['$scope', '$route','$location', '$http','$routeParams','$templateCache',
                                  function($scope, $route, $location, $http, $routeParams,$templateCache) {
	'use strict';
	$scope.resourcename = extractParams("/template/:typename-:cat.html.ftl",
			$routeParams);
	$http.get($scope.resourcename).success(
			function(data, status, headers, config) {
				$scope.data = {
					code : data
				};
			});
	$scope.newcode = "";
	$scope.$save = function() {
		$http.put($scope.resourcename, $scope.data.code).success(
				function(data, status, headers, config) {
					$scope.data = {
						code : data
					};
					$templateCache.removeAll();
				});
	};
}]).controller('ContactRecordsCtrl', ['$scope', '$resource',function($scope, $resource) {
	'use strict';
	$scope.data = {};

	$scope.companyList = $resource('/d/Company/', {}, {
		query : {
			method : 'GET',
			params : {"SCMRole": "客户"},
			isArray : true
		}
	}).query(function() {
		$scope.data.CompanyName = $scope.companyList[0].Name;
		$scope.refresh();
	});

	var DataResource = $resource('/d/ContactRecord/', {}, {
		query : {
			method : 'GET',
			params : {
				"Company" : $scope.data.CompanyName
			},
			isArray : true
		}
	});

	$scope.getActive = function(v) {
		if (v == $scope.data.CompanyName)
			return "active";
		else
			return "";
	}

	$scope.refresh = function() {
		$scope.contactRecords = $resource('/d/ContactRecord/', {}, {
			query : {
				method : 'GET',
				params : {
					"Company" : $scope.data.CompanyName
				},
				isArray : true
			}
		}).query();
	};

	$scope.$watch('data.CompanyName', function(newValue, oldValue) {
		if ($scope.data.CompanyName) {
			$scope.personList = $resource('/d/Person/', {}, {
				query : {
					method : 'GET',
					params : {
						"Company" : $scope.data.CompanyName
					},
					isArray : true
				}
			}).query();
		}
	});

	$scope.remotedata = {};

	$scope.refresh();

	$scope.call = function(o) {
		alert(o);
	}

	$scope.remotedata.$save = DataResource.prototype.$save;

	$scope.addContactRecord = function() {
		$.extend($scope.data, {
			ID : new Date().getTime(),
			Done : false,
			LastUpdated : new Date().format("yyyy-MM-dd hh:mm:ss.S")
		});

		$.extend($scope.remotedata, $scope.data);
		$scope.remotedata.$save();

		$scope.contactRecords.push($scope.data);

		$scope.data = $.extend({}, $scope.data, {
			Content : ""
		});
	};

	$scope.remaining = function() {
		var count = 0;
		angular.forEach($scope.contactRecords, function(contactRecord) {
			count += contactRecord.Done ? 0 : 1;
		});
		return count;
	};

	$scope.archive = function() {
		var oldContactRecords = $scope.contactRecords;
		$scope.contactRecords = [];
		angular.forEach(oldContactRecords, function(contactRecord) {
			if (!contactRecord.Done)
				$scope.contactRecords.push(contactRecord);
		});
	};

	$scope.remove = function(ID) {
		var Record = $resource('/d/ContactRecord/:ID', {
			ID : '@ID'
		});
		var record = Record.get({
			ID : ID
		}, function() {
			record.$remove();
		});
	}
}]);

Date.prototype.format = function(format) {
	'use strict';
	var o = {
		"M+" : this.getMonth() + 1, // month
		"d+" : this.getDate(), // day
		"h+" : this.getHours(), // hour
		"m+" : this.getMinutes(), // minute
		"s+" : this.getSeconds(), // second
		"q+" : Math.floor((this.getMonth() + 3) / 3), // quarter
		"S" : this.getMilliseconds()
	// millisecond
	};

	if (/(y+)/.test(format)) {
		format = format.replace(RegExp.$1, (this.getFullYear() + "")
				.substr(4 - RegExp.$1.length));
	}
	for ( var k in o) {
		if (new RegExp("(" + k + ")").test(format)) {
			format = format.replace(RegExp.$1, RegExp.$1.length == 1 ? o[k]
					: ("00" + o[k]).substr(("" + o[k]).length));
		}
	}
	return format;
}
// PhoneDetailCtrl.$inject = ['$scope', '$routeParams', 'Phone'];
function extractParams(url, params) {
		'use strict';
		var newurl = url;
		angular.forEach(params || {}, function(value, key) {
			newurl = newurl.replace(":" + key, value);
		});
		return newurl;
}
	