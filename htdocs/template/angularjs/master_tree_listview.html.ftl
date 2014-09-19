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
												
					    	<li parentName="${parentName}" idName="${idName}">[@compress single_line=true]
							[#assign keyfieldname][/#assign]
							[#list type.fields as field][#if !field.array  && !field.ignorable]
								[#switch field.refer]
								[#case "ByVal"]
									[#if field.key]
										[#if field.type.name!="ID"]
							<a href="#/d/${type.name}/{{item.${field.name}}}">{{item["${field.name}"]}}</a>
										[/#if]
										[#assign keyfieldname]${field.name}[/#assign]			
									[#elseif field.core]
							<br><a href="#/d/${type.name}/{{item.${keyfieldname}}}">{{item["${field.name}"]}}</a>
									[/#if]
									[#break]
								[/#switch]
							[/#if][/#list]
			
					    	[/@compress]</li>
						</ul>
					</div>
				</div>
			</div>
			
		</div>

[/@nl.article]
