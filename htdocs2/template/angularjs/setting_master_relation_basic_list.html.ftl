[#ftl/]
[#import "./lib/controls.ftl" as nc]
[#import "./lib/forms.ftl" as nf]
[#import "./lib/layouts.ftl" as nl]

[@nl.article title="${attachedType.displayName} - {{attachedData.Name}}" type=type]

		<div class="row-fluid">
			<div class="span12">
				<div class="widget-box">
					<div class="widget-title">
						<span class="icon"> <i class="icon-align-justify"></i>
						</span>
						<ul class="nav nav-tabs">
				  			<li><a tabindex="-1" href="#/d/${attachedType.name}/{{attachedData.Name}}">概述</a></li>
	[#list attachedType.attachedBy as atby][#if atby.standalone=="Transaction"]
				  			<li><a tabindex="-1" href="#/d/${attachedType.name}/{{attachedData.Name}}/${atby.name}/">${atby.displayName}</a></li>
	[/#if][/#list]
							<li class="active"><a tabindex="-1" href="#/d/${attachedType.name}/{{attachedData.Name}}/setting/info">Settings</a></li>
						</ul>
					</div>
					
					<div class="widget-content nopadding">
					
						<ul class="nav nav-tabs">
				  			<li><a tabindex="-1" href="#/d/${attachedType.name}/{{attachedData.Name}}/setting/info">信息</a></li>
	[#list attachedType.attachedBy as atby][#if atby.standalone!="Transaction"]
		[#if atby.name == type.name]
				  			<li class="active"><a tabindex="-1" href="#/d/${attachedType.name}/{{attachedData.Name}}/setting/${atby.name}/">${atby.displayName}</a></li>
		[#else]
				  			<li><a tabindex="-1" href="#/d/${attachedType.name}/{{attachedData.Name}}/setting/${atby.name}/">${atby.displayName}</a></li>
		[/#if]
	[/#if][/#list]
	
						<div class="buttons btn-toolbar pull-right" x-ng-show="data.standealone='Master'">
							<input type="text" x-ng-model="query" class="input-medium search-query ctrl" placeholder="Filter">	
							<a href="#/d/${attachedType.name}/{{attachedData.Name}}/${type.name}/!new" class="btn btn-small btn-success ctrl"><i class="icon-plus icon-white"></i> 新建</a>
						</div>
						
						</ul>
						
			
	<table class="table table-striped table-bordered table-hover">
		<thead>
			<tr>
				<th class="id">#</th>
				[#list type.fields as field][#if !field.array && !field.ignorable && !(field.attrs.Attach?? && field.name ==attachedType.name)]
					[#switch field.refer]
					[#case "ByVal"]
						[#if !field.key || field.type.name!="ID"]
				<th>${field.displayName}</th>
						[/#if]
						[#break]
						
					[#case "Inline"]
						[#if field.key || field.core][#list field.type.fields as rF][#if field.key && rF.key]
				<th>${field.displayName}&nbsp;${rF.displayName}</th>
						[/#if][/#list][/#if]
						[#break]
						
					[#case "ByRef"]
				<th>${field.displayName}</th>
						[#break]
						
					[#case "Cascade"]
				<th>${field.displayName}</th>
						[#break]
					[/#switch]
				[/#if][/#list]
			</tr>
		</thead>
		<tbody>
			<tr x-ng-repeat="data in datalist | filter:query | orderBy:orderProp">
				[#assign keyfieldname][/#assign]
			[#list type.fields as field][#if !field.array  && !field.ignorable && !(field.attrs.Attach?? && field.name ==attachedType.name)]
				[#switch field.refer]
				[#case "ByVal"]
					[#if field.key]
			<td><a href="#/d/${attachedType.name}/{{attachedData.Name}}/${type.name}/{{data.${field.name}}}">{{data["${field.name}"]}}</a></td>
						[#assign keyfieldname]${field.name}[/#assign]			
					[#elseif field.core]	
			<td><a href="#/d/${attachedType.name}/{{attachedData.Name}}/${type.name}/{{data.${keyfieldname}}}">{{data["${field.name}"]}}</a></td>
					[#else]	
			<td>{{data["${field.name}"]}}</td>
					[/#if]
					[#break]
				[#case "Inline"]
					[#if field.key || field.core][#list field.type.fields as rF][#if field.key && rF.key]
						<td>{{ data["${field.name}${rF.name}"] }}</td>
					[/#if][/#list][/#if]
					[#break]
				[#case "ByRef"]
				[#case "Cascade"]
					<td>[#if field.unique]<a href="#/d/${attachedType.name}/{{attachedData.Name}}/${type.name}/{{data.${keyfieldname}}}">[/#if]
					
					[#list field.type.fields as rF]
						[#if field.key && rF.key && rF.name!="ID"]
						{{ data["${field.name}${rF.name}"] }}&nbsp;
						[#elseif rF.key && rF.key && rF.name!="ID"]
						{{ data["${field.name}${rF.name}"] }}&nbsp;
						[#elseif rF.core]
						{{ data["${field.name}${rF.name}"] }}&nbsp;
						[/#if]
					[/#list]
					[#if field.core]</a>[/#if]</td>
					[#break]
				[/#switch]
			[/#if][/#list]
			</tr>
		</tbody>
	</table>
					</div>
				</div>
			</div>
		</div>
[/@nl.article]
