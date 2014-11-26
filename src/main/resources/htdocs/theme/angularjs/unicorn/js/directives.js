'use strict';

/* Directives */
function PopupListCtrl($scope,$resource,urlParams){
	if(urlParams.attachedtypename){
		$scope.datalist = $resource('/d/:attachedtypename/:attachedid/:typename/', urlParams, {
			query: {method:'GET', params:{}, isArray:true}
		}).query({});		
	} else{
		$scope.datalist = $resource('/d/:typename/', urlParams, {
			query: {method:'GET', params:{}, isArray:true}
		}).query({});		
	}
	$scope.$returnData = function(index){
		return $scope.datalist[index];
	};
}


var PathMatchProvider = function(when) {
    // TODO(i): this code is convoluted and inefficient, we should construct the route matching
    //   regex only once and then reuse it
    var regex = '^' + when.replace(/([\.\\\(\)\^\$])/g, "\\$1") + '$',
        params = [],
        paramMatchs = [],
        dst = {};
    	jQuery.each(when.split(/\W/), function(index,param) {
      if (param) {
        var paramRegExp = new RegExp(":" + param + "([\\W])");
        if (regex.match(paramRegExp)) {
          regex = regex.replace(paramRegExp, "([^\\/]*)$1");
          params.push(param);
          paramMatchs.push(new RegExp(":" + param));
        }
      }
    });
    
    var regexp = new RegExp(regex);
    
    return {
    	check:function(on){
    		
            var match = on.match(regexp);
	        if (match) {
	        	jQuery.each(params, function(index,name) {
	            dst[name] = match[index + 1];
	          });
	        }
	        return match ? dst : null;
    	},
    	replace:function(on,url){
            var match = on.match(regexp);
	        if (match) {
	        	jQuery.each(params, function(index,name) {
	            url = url.replace(paramMatchs[index],match[index + 1]);
	          });
	        }
	        return url;        		
    	}
  }
} 

var neFromListDirective = [ 
   		'$http',
		'$templateCache',
		'$route',
		'$anchorScroll',
		'$compile',
		'$controller',
		'$interpolate',
		function($http, $templateCache, $route, $anchorScroll, $compile,
				$controller, $interpolate) {
	return {
		restrict : 'A',
		terminal : false,
	    replace: true,
	    transclude: true,
		template: '<div ng-transclude></div>',
		link : function(scope, element, attrs) {
			var beforePopupExp = attrs.beforepopup || '';
			var afterPopupExp = attrs.afterpopup || '';
			
			if(element.parents("form table").length<1){
				element.wrap('<div class="input-append"></div>').after('<a class="addon btn"><i class="icon-search"></i></a>').next().click(function(){
					var inparams = {};
					popup(attrs.popup,inparams,function(retData){
						scope.ret = retData;
						scope.$eval(afterPopupExp);
					});
				});
			}else{
				element.wrap('<div class="input-append"></div>').after('<a class="addon btn"><i class="icon-search"></i></a>').next().click(function(){
				//element.addClass("inlinePopup").after('<a class="inlinePopup"><i class="icon-search"></i></a>').click(function(){
					var inparams = {};
					
					popup(attrs.popup,inparams,function(retData){
						scope.ret = retData;
						scope.$eval(afterPopupExp);
					});
				});
			}
			
			function preparePopupWin(){
				if(jQuery("#popup-window").length<1){
					jQuery('body').append('<div id="popup-window" class="modal" style="display:none;">')
								  .append('</div>');
				}
			}
			
			preparePopupWin();

			
			var changeCounter = 0, lastScope, lastTemplate;
			function destroyLastScope() {
				if (lastScope) {
					lastScope.$destroy();
					lastScope = null;
				}
			}
			
			function popup(reqUrl,params,onReturn){
				var popupwin = jQuery("#popup-window");
				var popupwinBody = popupwin;
				var thisChangeId = ++changeCounter;
				popupwinBody.html('');

				function clearContent() {
					// ignore callback if another route change occured
					// since
					if (thisChangeId === changeCounter) {
						popupwinBody.html('');
						destroyLastScope();
					}
				}
				
				var matcher = PathMatchProvider("/d/:typename/");
				var urlParams = matcher.check(reqUrl);
				var template;
				if(urlParams){
					template = matcher.replace(reqUrl,"/theme/angularjs/unicorn/:typename-popup.html");					
				}else{
					matcher = PathMatchProvider("/d/:attachedtypename/:attachedid/:typename/");
					urlParams = matcher.check(reqUrl);	
					template = matcher.replace(reqUrl,"/theme/angularjs/unicorn/:typename-popup.html");				
				}
				if(template){
					popupwin.fadeIn(300);
					

					
					$http.get(template).success(function(response) {
						if (thisChangeId === changeCounter) {							
							popupwinBody.html(response);
							
							destroyLastScope();
							var link = $compile(popupwinBody.contents()),controller;

							lastScope = scope.$new();
							lastScope.template = template;
							lastScope.link = link;
							
							if (PopupListCtrl) {
								controller = $controller(PopupListCtrl,
										{
											urlParams : urlParams,
											$scope : lastScope
										});
								
								lastScope.$ret = function(index){
									$('#mask , #popup-window').fadeOut(300 , function() {
										$('#mask').remove();  
									});
									
									var data =lastScope.$returnData(index);
									onReturn(data);
								}

								lastScope.$retData = function(data){
									$('#mask , #popup-window').fadeOut(300 , function() {
										$('#mask').remove();  
									});
									
//									var data =lastScope.$returnData(index);
									onReturn(data);
								}
								
								popupwinBody.contents().data(
												'$ngControllerController',
												controller);
							}

							link(lastScope);
							
							//lastScope.$emit('$popupContentLoaded');
							//lastScope.$eval(onloadExp);
						}	
					}).error(clearContent);
				} else {
					clearContent();
				}
				
				// Add the mask to body
				$('body').append('<div id="mask"></div>');
				$('#mask').fadeIn(300);
				
				// When clicking on the button close or the mask layer the popup closed
				$('.close, #mask,.popup-close').live('click', function() { 
					$('#mask , #popup-window').fadeOut(300 , function() {
						$('#mask').remove();  
					});
				});	
			}
		}
	};
} ];

/**
 * @ngdoc event
 * @name ng.directive:ngView#$viewContentLoaded
 * @eventOf ng.directive:ngView
 * @eventType emit on the current ngView scope
 * @description
 * Emitted every time the ngView content is reloaded.
 */
var nbViewDirective = ['$http', '$templateCache', '$route', '$anchorScroll', '$compile',
                       '$controller',
               function($http,   $templateCache,   $route,   $anchorScroll,   $compile,
                        $controller) {
  return {
    restrict: 'ECA',
    terminal: true,
    link: function(scope, rawElement, attr) {
      var lastScope,
          onloadExp = attr.onload || '';
      var rawHtml = rawElement.html();
      rawElement.html("<div class='innerPanel row-fluid'></div><div  class='innerPanel row-fluid' style='visible=hidden'></div>");
      var lastElement = jQuery(rawElement.children()[0]);
      var element = jQuery(rawElement.children()[1]);
      lastElement.html(rawHtml);
      element.html(rawHtml);
      var mask = jQuery("<div id='mask' class='mask opacity'></div>").appendTo(rawElement);
	  
      update();
      
      scope.$on('$routeChangeSuccess', update);

      function destroyLastScope() {
        if (lastScope) {
          lastScope.$destroy();
          lastScope = null;
        }
      }

      function clearContent() {
        element.html(rawHtml);
        destroyLastScope();
      }
      var isLoading = false;
      function prepareLoad(){
    	  isLoading = true;

          setTimeout(function(){
        	  if(isLoading){
        		  mask.show()
        	  }    	  
          },300);
      }
      function finishLoad(){
    	  if(isLoading){
        	  mask.hide(3);
        	  var old = lastElement;
        	  lastElement = element;
        	  lastElement.show();
        	  element = old;
        	  element.hide(3);    	
              isLoading = false;	  
    	  }
      }

      function update() {
        var locals = $route.current && $route.current.locals,
            template = locals && locals.$template;
        
        if (template) {
        	prepareLoad();
          element.html(template);
          element.find("input[required]").parents(".control-group").addClass("required");
          element.find("select[required]").parents(".control-group").addClass("required");
          element.find("input.datepicker").datepicker();
          
          destroyLastScope();

          var link = $compile(element.contents()),
              current = $route.current,
              controller;

          lastScope = current.scope = scope.$new();
          if (current.controller) {
            locals.$scope = lastScope;
            controller = $controller(current.controller, locals);
            element.contents().data('$ngControllerController', controller);
          }

          link(lastScope);
          lastScope.$emit('$viewContentLoaded');
          lastScope.$eval(onloadExp);
          lastScope.$dataReady = function(){
        	  finishLoad();
          }
          setTimeout(finishLoad,300);
          
          // $anchorScroll might listen on event...
          $anchorScroll();
        } else {
          // clearContent();
        }
      }
    }
  };
}];


/**
 * @ngdoc event
 * @name ng.directive:ngView#$viewContentLoaded
 * @eventOf ng.directive:ngView
 * @eventType emit on the current ngView scope
 * @description
 * Emitted every time the ngView content is reloaded.
 */
var nbJsonDataDirective = ['$http', '$controller','$resource',
               function($http, $controller,$resource) {
  return {
	scope:true, 
    restrict: 'A',
    link: function(scope, element, attr,controller) {
     var at = attr; 
     scope.jsonData = $resource(attr.jsondataurl).get({},function(){
      	var d = 	 scope.jsonData;
     });
    }
  };
}];
var nbInlineShowDataDirective = [ '$http', '$controller', '$resource', function($http, $controller, $resource) {
	return {
		restrict : 'A',
		require : '?ngModel',
		link : function(scope, element, attr, ctrl) {
			scope.$watch(attr.ngModel + "ID", function(newValue, oldValue) {
				if(scope.$eval(attr.ngModel + "ID" + "==" + attr.ngModel + ".ID"  + "")){
				}else{
					$.each(scope[attr.id + "values"],function(i,o){
						if(o.ID == scope.$eval(attr.ngModel + "ID") ){
							scope.$eval(attr.ngModel + "=" + attr.id + "values"+ "[" + i+ "]"  + ";");
						}
					});
				}
			});

			scope.$watch(attr.ngModel, function(newValue, oldValue) {
				if (newValue) {
					if(scope.$eval(attr.ngModel + "ID" + "==" + attr.ngModel + ".ID"  + "")){
						
					}else{
						scope.$eval(attr.ngModel + "ID" + "=" + attr.ngModel + ".ID"  + ";");
						scope.$eval(attr.ngModel + "Name" + "=" + attr.ngModel + ".Name"  + ";");						
					}
				} else {
					scope.$eval(attr.ngModel + "ID" + "='';");
					scope.$eval(attr.ngModel + "Name" + "='';");
				}
			});
		}
	};
} ];

angular.module('nebulaDirectives', [])
.directive('nbView', nbViewDirective)
.directive('popup', neFromListDirective)
.directive('jsondataurl', nbJsonDataDirective)
.directive('inlineshow', nbInlineShowDataDirective);
