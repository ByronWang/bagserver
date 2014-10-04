[#ftl/]
[#import "./lib/controls.ftl" as nc]
[#import "./lib/forms.ftl" as nf]
[#import "./lib/layouts.ftl" as nl]

[@nl.article title="${attachedType.displayName} - {{attachedData.ID}}" type=type]
[@nl.txAttachedHome attachedType=attachedType type=type]
			
	<table class="table table-striped table-bordered table-hover">
		<thead>
			<tr>
				<th class="id">#</th>
				[#list type.fields as field][#if !field.array && !field.ignorable && !(field.attrs.Attach?? && field.name ==attachedType.name)]
					[#switch field.refer]
					[#case "ByVal"]
						[#if !field.key]
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
			<td><a href="#/d/${attachedType.name}/{{attachedData.ID}}/${type.name}/{{data.${field.name}}}">{{data["${field.name}"]}}</a></td>
						[#assign keyfieldname]${field.name}[/#assign]			
					[#elseif field.core]	
			<td><a href="#/d/${attachedType.name}/{{attachedData.ID}}/${type.name}/{{data.${keyfieldname}}}">{{data["${field.name}"]}}</a></td>
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
					<td>[#if field.unique]<a href="#/d/${attachedType.name}/{{attachedData.ID}}/${type.name}/{{data.${keyfieldname}}}">[/#if]
					
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
[/@nl.txAttachedHome]
[/@nl.article]
