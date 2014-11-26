[#ftl/]
[#import "./lib/controls.ftl" as nc]
[#import "./lib/forms.ftl" as nf]
[#import "./lib/layouts.ftl" as nl]

[@nl.article title="${type.displayName}" type=type]
		<div class="row-fluid" x-ng-controller="TreeListCtrl">
			<div class="span4">
				<div class="widget-box">
					<div class="widget-title">
						<span class="icon"> <i class="icon-align-justify"></i>
						</span>
						<div class="buttons btn-toolbar" x-ng-show="data.standealone='Master'">
							<!-- <input type="text" x-ng-model="query" class="input-small search-query ctrl" placeholder="Filter"> -->
							<a href="#/d/${type.name}/!new" class="btn btn-small btn-success ctrl"><i class="icon-plus icon-white"></i> 新建</a>
						</div>
					</div>
					<div class="widget-content nopadding">	
				[#list type.fields as field][#if !field.array && !field.ignorable]
					[#switch field.refer]
					[#case "ByVal"]
						[#if field.key]
							[#assign idName=field.name/]
						[/#if]			
						
					[#case "Cascade"] <!--  Type B4   -->
						[#list field.type.fields as in1f][#t]
							[#if !in1f.array &&  in1f.refer =="ByVal" && in1f.key]
								[#assign parentName="${field.name}${in1f.name}"/]
							[/#if]
						[/#list]	
					[/#switch]	
				[/#if][/#list]
						<ul x-ng-tree="datalist" class="testtree" x-multiple>
												
					    	<li select="selectData(this.item,this.$selected)" parentName="${parentName}" idName="${idName}">[@compress single_line=true]
							[#assign keyfieldname][/#assign]
							[#list type.fields as field][#if !field.array  && !field.ignorable]
								[#switch field.refer]
								[#case "ByVal"]
									[#if field.key]
										[#if field.type.name!="ID"]
							<!-- a href="#/d/${type.name}/{{item.${field.name}}}"> -->{{item["${field.name}"]}}
										[/#if]
										[#assign keyfieldname]${field.name}[/#assign]			
									[#elseif field.core]	
							<!-- <a href="#/d/${type.name}/{{item.${keyfieldname}}}">-->{{item["${field.name}"]}}
									[/#if]
									[#break]
								[/#switch]
							[/#if][/#list]
			
					    	[/@compress]</li>
						</ul>
					</div>
				</div>
			</div>
			<div class="span8">
				<div class="widget-box">
					<div class="widget-title">
						<span class="icon"> <i class="icon-align-justify"></i>
						</span>
						<h5>${type.displayName}</h5>
						<div class="buttons btn-toolbar" x-ng-show="data.standealone='Master'">
							<a href="#/d/${type.name}/{{data.${idName}}}" class="btn btn-small btn-success ctrl" x-ng-show="data.${idName}"><i class="icon-edit icon-white"></i> 编辑</a>
							<a ng-href="#/d/${type.name}/!new?${parentName}={{data.${idName}}}" class="btn btn-small btn-success ctrl"  x-ng-show="data.${idName}"><i class="icon-plus icon-white"></i> 新建子${type.displayName}</a>
						</div>
					</div>
					<div class="widget-content nopadding">
					
<!-- Start Form -->
	<form name="form" x-ng-submit="$save()"  class="form-horizontal form-thin" novalidate>

	[#list type.fields as of][#t]
				[#if !of.array]
					[#switch of.refer]
					[#case "ByVal"] <!--  Basic Type Field <!--  Type A1-->
						[#if !of.key || of.type.name != "ID"]
			[@nc.controls field=of for="${of.name}" label="${of.displayName}"]
					[@nc.inputBox field=of id="${of.name}"  ngModel="data.${of.name}" placeholder="${of.displayName}" 
						key=of.key required=!of.ignorable/]
			[/@nc.controls]
						[/#if]
						[#break]
					[#case "Inline"] <!--  inline object -->
		<fieldset>
		<legend>${of.name}</legend>
						[#list of.type.fields as in1f][#t]
							[#if !in1f.array]<!-- -->
								[#switch in1f.refer]
								[#case "ByVal"] <!--  Type B1-->
				[@nc.controls field=in1f for="${of.name}${in1f.name}" label="${in1f.displayName}"]
					[@nc.inputBox field=in1f id="${of.name}${in1f.name}" ngModel="data.${of.name}.${in1f.name}" placeholder="${of.displayName} ${in1f.displayName}"
						required=!(of.ignorable || in1f.ignorable)/]
				[/@nc.controls]
									[#break]
								[#case "Inline"] <!--  Type B2-->
									[#list in1f.type.fields as in2f][#t]
										[#if !in2f.array && in2f.refer == "ByVal"] <!--  Type
																										<!--  C1   -->

				[@nc.controls  field=in2f for="${of.name}${in1f.name}${in2f.name}" label="${in1f.displayName}${in2f.displayName}"]
					[@nc.inputBox field=in2f id="${of.name}${in1f.name}${in2f.name}" ngModel="data.${of.name}.${in1f.name}${in2f.name}" placeholder="${of.displayName} ${in1f.displayName} ${in2f.displayName}"
						required=!(of.ignorable || in1f.ignorable || in2f.ignorable)/]
				[/@nc.controls]
										[/#if] 
									[/#list]
									[#break]
								[#case "ByRef"] <!--  Type B3   -->
								[#case "Cascade"] <!--  Type B4   -->

				[@nc.controls  field=in1f for="${of.name}${in1f.name}" label="${in1f.displayName}"]
					[@nc.popupBox field=in1f pField=in1f id="${of.name}${in1f.name}" ngModel="data.${of.name}.${in1f.name}" placeholder="${of.displayName} ${in1f.displayName}"
						readonly=true required=!(of.ignorable || in1f.ignorable)/]		
				[/@nc.controls]
									[#break]
								[/#switch]
							[#else]
								[#switch in1f.refer]
								[#case "ByVal"] <!--  Type B5   -->
											
			[@nc.controls  field=in1f for="${of.name}${in1f.name}" label="${of.displayName} ${in1f.displayName}"]
					[@nc.inputBox field=in1f id="${of.name}${in1f.name}" ngModel="data.${of.name}.${in1f.name}" placeholder="${of.displayName}${in1f.displayName}"
						required=!(of.ignorable || in1f.ignorable) /] <!-- ngList -->
			[/@nc.controls]
									[#break]
								[#case "Inline"] <!--  Type B6   -->
								
		<table class="table table-hover table-striped table-bordered table-condensed">
			<caption>${of.name} [{{data['${of.name}'].length}}]</caption>
			<thead>
				<tr>
					<th width="2em">#</th>
									[#list in1f.type.fields as in2f][#t]
										[#if !in2f.array && in2f.refer == "ByVal"] <!--  Type   -->
									
					<th>${in2f.name}</th>
					
										[/#if] 
									[/#list]
				</tr>
			</thead>
			
			<tbody>
				<tr x-ng-repeat="inlineData in data.${of.name}${in1f.name} | filter:query | orderBy:orderProp">
					<td>#</td>
									[#list in1f.type.fields as in2f][#t]
										[#if !in2f.array && in2f.refer == "ByVal"] <!--  Type   -->

					<td>{{inlineData.${in2f.name}}}</td>
					
										[/#if] 
									[/#list]
				</tr>
			</tbody>
		</table>

									[#break]
								[#case "ByRef"] <!--  Type B7   -->
								[#case "Cascade"] <!--  Type B8   -->
									throw new UnsupportedOperationException(
											"Refer Object cannot has array,must user inline array");
								[/#switch]
							[/#if]
						[/#list]
					</fieldset>
						[#break]
					[#case "ByRef"] <!--  Type A3   -->
					[#case "Cascade"] <!--  Type A4   -->
				[@nc.controls field=of for="${of.name}" label="${of.displayName}"]
					[@nc.popupBox field=of pField=of id="${of.name}" ngModel="data.${of.name}"  placeholder="${of.displayName}"
						key=(of.key) readonly=true required=!(of.ignorable)/]
				[/@nc.controls]
						[#break]
					[/#switch]
				[#else] <!--  数组不可以是Key   -->
					[#switch of.refer]
					[#case "ByVal"] <!--  Basic Type Field  --> <!--  Type A5   -->
			[@nc.controls  field=of for="${of.name}" label="${of.displayName}"]
					[@nc.inputBox field=of id="${of.name}" ngModel="data.${of.name}" placeholder="${of.displayName}"
						required=!(of.ignorable) ex="x-ng-list"/] <!-- ngList -->
			[/@nc.controls]
						[#break]
					[#case "Inline"] <!--  inline object --> <!--  Type A6   -->
		<table class="table table-hover table-striped table-bordered table-condensed">
			<caption>${of.name} [{{data['${of.name}'].length}}]</caption>
			<thead>
				<tr>
					<th width="2em">#</th>

						[#list of.type.fields as in1f][#t]
							[#if !in1f.array]
								[#switch in1f.refer]
								[#case "ByVal"] <!--  Type E1   -->
									
					<th>${in1f.name}</th>
					
									[#break]
								[#case "Inline"]<!--  Type E2   -->
									[#list in1f.type.fields as in2f][#t]
										[#if !in2f.array && in2f.refer == "ByVal"]
										
					<th>${in1f.displayName} ${in2f.displayName}</th>
					
										[/#if]
									[/#list]
									[#break]
								[#case "ByRef"]<!--  Type E3-->
								[#case "Cascade"]<!--  Type E4-->
									[#list in1f.type.fields as in2f][#t]
										[#if !in2f.array && in2f.refer == "ByVal" && (in2f.key || in2f.core)]
										
					<th>${in1f.displayName} ${in2f.displayName}</th>
					
										[/#if] 
									[/#list]
									[#break]
								[/#switch]
							[/#if] 
						[/#list]
					<th>Actions</th>
				</tr>
			</thead>
			<tbody>
				<tr x-ng-repeat="inlineData in data.${of.name} | filter:query | orderBy:orderProp" class="dataline-{{inlineData['_STS']}}">
					<td>{{$index+1}}</td>

						[#list of.type.fields as in1f][#t]
							[#if !in1f.array]
								[#switch in1f.refer]
								[#case "ByVal"] <!--  Type E1   -->
									
					<td>{{inlineData.${in1f.name}}}</td>
					
									[#break]
								[#case "Inline"]<!--  Type E2   -->
									[#list in1f.type.fields as in2f][#t]
										[#if !in2f.array && in2f.refer == "ByVal"]
										
					<td>{{inlineData.${in1f.name}${in2f.name}}}</td>
					
										[/#if]
									[/#list]
									[#break]
								[#case "ByRef"]<!--  Type E3-->
								[#case "Cascade"]<!--  Type E4-->
									[#list in1f.type.fields as in2f][#t]
										[#if !in2f.array && in2f.refer == "ByVal" && (in2f.key || in2f.core)]
										
					<td>{{inlineData.${in1f.name}${in2f.name}}}</td>
					
										[/#if] 
									[/#list]
									[#break]
								[/#switch]
							[/#if] 
						[/#list]
					<td><a x-ng-click="inlineData['_STS']='D';$event.preventDefault();"> <i class="icon-minus-sign"> </i> </a></td>
				</tr>
				<tr class="newLine">
					<td>{{data.${of.name}.length+1}}</td>

						[#list of.type.fields as in1f][#t]
							[#if !in1f.array]
								[#switch in1f.refer]
								[#case "ByVal"] <!--  Type E1   -->
									
					<td>	[@nc.inputBox field=in1f id="${of.name}${in1f.name}_new"  ngModel="data.${of.name}_new.${in1f.name}" placeholder="${of.displayName} ${in1f.displayName}" 
						required=false/]</td>[#--TODO  key=in1f.key  required=!in1f.ignorable--]
					
									[#break]
								[#case "Inline"]<!--  Type E2   -->
									[#list in1f.type.fields as in2f][#t]
										[#if !in2f.array && in2f.refer == "ByVal"]
										
					<td>	[@nc.inputBox field=in1f id="${of.name}_new_${in1f.name}${in2f.name}"  ngModel="data.${of.name}_new.${in1f.name}${in2f.name}" placeholder="${of.displayName} ${in1f.displayName} ${in2f.displayName}" 
						key=in1f.key required=false/]</td>[#--TODO required=!in1f.ignorable --] 
					
										[/#if]
									[/#list]
									[#break]
								[#case "ByRef"]<!--  Type E3-->
								[#case "Cascade"]<!--  Type E4-->										
					<td>[@nc.popupBox field=in1f pField=in1f id="${of.name}_new_${in1f.name}" ngModel="data.${of.name}_new.${in1f.name}" placeholder="${of.displayName} ${in1f.displayName}"
						key=of.key readonly=true required=false/]</td>[#--  required=!(of.ignorable) --]
									[#break]
								[/#switch]
							[/#if] 
						[/#list]
					<td><a class="btn" x-ng-click="data.${of.name}.push(data.${of.name}_new);data.${of.name}_new={};$event.preventDefault() "> <i class="icon-plus"> </i> </a> </td>
				</tr>
			</tbody>
		</table>

						[#break]
					[#case "ByRef"]
					[#case "Cascade"]
						throw new UnsupportedOperationException("Refer Object cannot has array,must user inline array");
					[/#switch]

				[/#if]

			[/#list]
		<!-- End Form -->
	</form>
	</div>
	</div>
			</div>
		</div>

[/@nl.article]
