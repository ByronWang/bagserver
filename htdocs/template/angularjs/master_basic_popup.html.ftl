[#ftl/]
[#import "./lib/controls.ftl" as nc]
[#import "./lib/forms.ftl" as nf]
[#import "./lib/layouts.ftl" as nl]

<div class="modal-header">
	<button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
	<h3>${type.displayName}</h3>
	<div class="buttons">
		<input type="text" x-ng-model="query" class="input-medium search-query ctrl" placeholder="Filter"/>
	</div>
</div>

<div class="modal-body">


			
	<table class="table table-striped table-bordered table-hover">
		<thead>
			<tr>
				<th class="id">#</th>
				[#list type.fields as field][#if !field.array && (field.key || field.core)]
					[#switch field.refer]
					[#case "ByVal"]
						[#if !field.key || field.type.name!="ID" || field.core]
				<th>${field.name}</th>
						[/#if]
						[#break]
						
					[#case "Inline"]
						[#if field.key || field.core][#list field.type.fields as rF][#if field.key && rF.key]
				<th>${field.name}&nbsp;${rF.name}</th>
						[/#if][/#list][/#if]
						[#break]
						
					[#case "ByRef"]
				<th>${field.name}</th>
						[#break]
					[#case "Cascade"]
				<th>${field.name}</th>
						[#break]
					[/#switch]
				[/#if][/#list]
			</tr>
		</thead>
		<tbody>
			<tr ng-repeat="data in datalist | filter:query | orderBy:orderProp" ng-click="$retData(this.data)">
			<td class="id">{{$index+1}}</td>
			  
			[#list type.fields as field][#if !field.array && (field.key || field.core)]
				[#switch field.refer]
				[#case "ByVal"]
					[#if field.type.name!="ID"]
			<td>{{data.${field.name}}}</td>
					[/#if]
					[#break]
				[#case "Inline"]
					[#if field.key || field.core][#list field.type.fields as rF][#if field.key && rF.key]
						<td>{{ data["${field.name}${rF.name}"] }}</td>
					[/#if][/#list][/#if]
					[#break]
				[#case "ByRef"]
				[#case "Cascade"]
					<td>[#list field.type.fields as rF]
						[#if field.key && rF.key && rF.type.name!="ID"]
						{{ data["${field.name}${rF.name}"] }}&nbsp;
						[#elseif rF.key && rF.type.name!="ID"]
						{{ data["${field.name}${rF.name}"] }}&nbsp;
						[#elseif rF.core]
						{{ data["${field.name}${rF.name}"] }}&nbsp;
						[/#if]
					[/#list]</td>
					[#break]
				[/#switch]
			[/#if][/#list]
			</tr>
		</tbody>
	</table>
</div>
<div class="modal-footer">
	<button class="btn popup-close" data-dismiss="modal" aria-hidden="true">Close</button>
</div>