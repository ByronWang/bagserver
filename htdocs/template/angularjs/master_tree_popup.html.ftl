[#ftl/]
[#import "./lib/controls.ftl" as nc]
[#import "./lib/forms.ftl" as nf]
[#import "./lib/layouts.ftl" as nl]

<div class="modal-header">
	<button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
	<h3>${type.displayName} List</h3>
	<div class="ctrls">
		<input type="text" x-ng-model="query" class="input-medium search-query ctrl" placeholder="Filter"/>
	</div>
</div>

<div class="modal-body">


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
												
					    	<li select="$retData(this.item)" parentName="${parentName}" idName="${idName}">[@compress single_line=true]
							[#assign keyfieldname][/#assign]
							[#list type.fields as field][#if !field.array  && !field.ignorable]
								[#switch field.refer]
								[#case "ByVal"]
									[#if field.key]
										[#if field.type.name!="ID"]
							{{item["${field.name}"]}}&nbsp;
										[/#if]
										[#assign keyfieldname]${field.name}[/#assign]			
									[#elseif field.core]	
							{{item["${field.name}"]}}&nbsp;
									[#else]	
							{{item["${field.name}"]}}&nbsp;
									[/#if]
									[#break]
								[/#switch]
							[/#if][/#list]
			
					    	[/@compress]</li>
						</ul>
</div>
<div class="modal-footer">
	<button class="btn popup-close" data-dismiss="modal" aria-hidden="true">Close</button>
</div>