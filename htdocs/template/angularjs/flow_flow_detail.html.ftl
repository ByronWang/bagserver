[#ftl/]
[#import "./lib/controls.ftl" as nc]
[#import "./lib/forms.ftl" as nf]
[#import "./lib/layouts.ftl" as nl]

[@nl.article title="${type.displayName}&nbsp;" + '#' + "{{data.flow.ID}}" type=type]

[@nl.simple title="${type.displayName} - {{data.flow.curStep}}"]
<div  x-ng-controller="FlowHistoryCtrl"   x-ng-show="'Begin'!=data.step.ActualCurrentStep" >
	<form name="form"  class="form-horizontal" novalidate>
	<div x-ng-repeat="step in datafull.steps">
[#list type.steps as step][#t]
		<fieldset   x-ng-show="'${step.name}'==step.StepName && '${step.name}'!=$parent.data.step.ActualCurrentStep" >
		<legend>${step.displayName}</legend>
		[#include "flow_flow_detail_viewform.html.ftl"]
		</fieldset>
[/#list]		
	</div>
		<div class="form-actions" x-ng-show="'End'==data.step.ActualCurrentStep" >
	  		<a href="" class="btn" x-ng-click="$back()">返回</a>
		</div>
	</form>
</div>

[#list type.steps as step][#t][#if step.name!="End"]

<!-- Start Form -->
<div  x-ng-show="'${step.name}'==data.step.ActualCurrentStep" >
	<form name="form${step.name}'"  class="form-horizontal" novalidate>
		<fieldset>
		<legend>${step.displayName}</legend>		
		[#include "flow_flow_detail_editform.html.ftl"]
		</fieldset>
		<div class="form-actions">
			[#assign primary]btn-primary[/#assign]
			[#list step.type.actions as a][#if !a.internal][#t]
	  		<input type="submit" class="btn ${primary}" x-ng-disabled="form.$invalid"   x-ng-click="$save('${a.name}')"  value="${a.displayName}">
				[#assign primary][/#assign]
			[/#if][/#list]
	  		<a href="" class="btn" x-ng-click="$back()">返回</a>
			<!-- button type="button" class="btn">Cancel</button--> 
		</div>
		<!-- End Form -->
	</form>
</div>
	[/#if]
			[/#list]

[/@nl.simple]
[/@nl.article]
