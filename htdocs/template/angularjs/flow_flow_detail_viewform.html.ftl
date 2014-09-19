[#ftl/]
	[#list step.type.fields as of][#t][#if !of.internal]
				[#if !of.array]
					[#switch of.refer]
					[#case "ByVal"] <!--  Basic Type Field <!--  Type A1-->
						[#if !of.key || of.type.name != "ID"]
			[@nc.controls field=of for="${of.name}" label="${of.displayName}"]
					[@nc.view field=of id="${of.name}"  ngModel="step.StepData.${of.name}" placeholder="${of.name}" 
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
					[@nc.view field=in1f id="${of.name}${in1f.name}" ngModel="step.StepData.${of.name}.${in1f.name}" placeholder="${of.name} ${in1f.name}"
						required=!(of.ignorable || in1f.ignorable)/]
				[/@nc.controls]
									[#break]
								[#case "Inline"] <!--  Type B2-->
									[#list in1f.type.fields as in2f][#t]
										[#if !in2f.array && in2f.refer == "ByVal"] <!--  Type
																										<!--  C1   -->

				[@nc.controls  field=in2f for="${of.name}${in1f.name}${in2f.name}" label="${in1f.displayName}${in2f.displayName}"]
					[@nc.view field=in2f id="${of.name}${in1f.name}${in2f.name}" ngModel="step.StepData.${of.name}.${in1f.name}${in2f.name}" placeholder="${of.name} ${in1f.name} ${in2f.name}"
						required=!(of.ignorable || in1f.ignorable || in2f.ignorable)/]
				[/@nc.controls]
										[/#if] 
									[/#list]
									[#break]
								[#case "ByRef"] <!--  Type B3   -->
								[#case "Cascade"] <!--  Type B4   -->

				[@nc.controls  field=in1f for="${of.name}${in1f.name}" label="${in1f.displayName}"]
					[@nc.popupBox field=in1f pField=in1f id="${of.name}${in1f.name}" ngModel="step.StepData.${of.name}.${in1f.name}" placeholder="${of.name} ${in1f.name}"
						readonly=true required=!(of.ignorable || in1f.ignorable)/]		
				[/@nc.controls]
									[#break]
								[/#switch]
							[#else]
								[#switch in1f.refer]
								[#case "ByVal"] <!--  Type B5   -->
											
			[@nc.controls  field=in1f for="${of.name}${in1f.name}" label="${of.displayName} ${in1f.displayName}"]
					[@nc.view field=in1f id="${of.name}${in1f.name}" ngModel="step.StepData.${of.name}.${in1f.name}" placeholder="${of.name}${in1f.name}"
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
				<tr x-ng-repeat="inlineData in step.StepData.${of.name}${in1f.name} | filter:query | orderBy:orderProp">
					<td>#</td>
									[#list in1f.type.fields as in2f][#t]
										[#if !in2f.array && in2f.refer == "ByVal"] <!--  Type   -->

					<td>{{inlinestep.StepData.${in2f.name}}}</td>
					
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
					[@nc.popupBox field=of pField=of id="${of.name}" ngModel="step.StepData.${of.name}"  placeholder="${of.name}"
						key=(of.key) readonly=true required=!(of.ignorable)/]
				[/@nc.controls]
						[#break]
					[/#switch]
				[#else] <!--  数组不可以是Key   -->
					[#switch of.refer]
					[#case "ByVal"] <!--  Basic Type Field  --> <!--  Type A5   -->
			[@nc.controls  field=of for="${of.name}" label="${of.displayName}"]
					[@nc.view field=of id="${of.name}" ngModel="step.StepData.${of.name}" placeholder="${of.name}"
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
				<tr x-ng-repeat="inlineData in step.StepData.${of.name} | filter:query | orderBy:orderProp" class="dataline-{{inlineData['_STS']}}">
					<td>{{$index+1}}</td>

						[#list of.type.fields as in1f][#t]
							[#if !in1f.array]
								[#switch in1f.refer]
								[#case "ByVal"] <!--  Type E1   -->
									
					<td>{{inlinestep.StepData.${in1f.name}}}</td>
					
									[#break]
								[#case "Inline"]<!--  Type E2   -->
									[#list in1f.type.fields as in2f][#t]
										[#if !in2f.array && in2f.refer == "ByVal"]
										
					<td>{{inlinestep.StepData.${in1f.name}${in2f.name}}}</td>
					
										[/#if]
									[/#list]
									[#break]
								[#case "ByRef"]<!--  Type E3-->
								[#case "Cascade"]<!--  Type E4-->
									[#list in1f.type.fields as in2f][#t]
										[#if !in2f.array && in2f.refer == "ByVal" && (in2f.key || in2f.core)]
										
					<td>{{inlinestep.StepData.${in1f.name}${in2f.name}}}</td>
					
										[/#if] 
									[/#list]
									[#break]
								[/#switch]
							[/#if] 
						[/#list]
					<td><a x-ng-click="inlineData['_STS']='D';$event.preventDefault();"> <i class="icon-minus-sign"> </i> </a></td>
				</tr>
				<tr class="newLine">
					<td>{{step.StepData.${of.name}.length+1}}</td>

						[#list of.type.fields as in1f][#t]
							[#if !in1f.array]
								[#switch in1f.refer]
								[#case "ByVal"] <!--  Type E1   -->
									
					<td>	[@nc.view field=in1f id="${of.name}${in1f.name}_new"  ngModel="step.StepData.${of.name}_new.${in1f.name}" placeholder="${of.name} ${in1f.name}" 
						required=false/]</td>[#--TODO  key=in1f.key  required=!in1f.ignorable--]
					
									[#break]
								[#case "Inline"]<!--  Type E2   -->
									[#list in1f.type.fields as in2f][#t]
										[#if !in2f.array && in2f.refer == "ByVal"]
										
					<td>	[@nc.view field=in1f id="${of.name}_new_${in1f.name}${in2f.name}"  ngModel="step.StepData.${of.name}_new.${in1f.name}${in2f.name}" placeholder="${of.name} ${in1f.name} ${in2f.name}" 
						key=in1f.key required=false/]</td>[#--TODO required=!in1f.ignorable --] 
					
										[/#if]
									[/#list]
									[#break]
								[#case "ByRef"]<!--  Type E3-->
								[#case "Cascade"]<!--  Type E4-->										
					<td>[@nc.popupBox field=in1f pField=in1f id="${of.name}_new_${in1f.name}" ngModel="step.StepData.${of.name}_new.${in1f.name}" placeholder="${of.name} ${in1f.name}"
						key=of.key readonly=true required=false/]</td>[#--  required=!(of.ignorable) --]
									[#break]
								[/#switch]
							[/#if] 
						[/#list]
					<td><a class="btn" x-ng-click="step.StepData.${of.name}.push(step.StepData.${of.name}_new);step.StepData.${of.name}_new={};$event.preventDefault() "> <i class="icon-plus"> </i> </a> </td>
				</tr>
			</tbody>
		</table>

						[#break]
					[#case "ByRef"]
					[#case "Cascade"]
						throw new UnsupportedOperationException("Refer Object cannot has array,must user inline array");
					[/#switch]

				[/#if]

			[/#if][/#list]