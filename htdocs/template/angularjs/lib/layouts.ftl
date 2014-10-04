[#ftl/]
<!--  article -->
[#macro article title type]
<article class="module width_full">
	<header>
		<h1 class="tabs_involved">${title}</h1>
		[#if _spec="admin"]
		<div class="btn-toolbar">
			<div class="btn-group">
				<a href="#/d/Type/${type.name}" class="btn">ByImport Define</a>
				<button class="btn dropdown-toggle" data-toggle="dropdown">
					<span class="caret"></span>
				</button>
				<ul class="dropdown-menu">
					<li><a tabindex="-1" href="#/t${_path}">View Template</a></li>
				</ul>
			</div>
		</div>
		[/#if]
	</header>
	<div id="breadcrumb">
		<a href="/" title="Go to Home" class="tip-bottom"><i class="icon-home"></i> Home</a> <a href="#" class="tip-bottom">Form elements</a> <a href="#"
			class="current"
		>Common elements</a>
	</div>
	<div class="container-fluid">[#nested]</div>
</article>
[/#macro]
<!--  article -->
[#macro simple title]
<div class="row-fluid">
	<div class="span12">
		<div class="widget-box">
			<div class="widget-title">
				<span class="icon"> <i class="icon-align-justify"></i>
				</span>
				<h5>${title}</h5>
			</div>
			<div class="widget-content nopadding">[#nested]</div>
		</div>
	</div>
</div>
[/#macro]

[#macro simpleAttachedHome attachedType type]
		<div class="row-fluid">
			<div class="span12">
				<div class="widget-box">
					<div class="widget-title">
						<span class="icon"> <i class="icon-align-justify"></i>
						</span>
						<ul class="nav nav-tabs">
				  			<li class="active"><a tabindex="-1" href="#/d/${attachedType.name}/{{data.Name}}">概述</a></li>
	[#list attachedType.attachedBy as atby][#if atby.standalone=="Transaction"]
		[#if atby.name == type.name]
				  			<li><a tabindex="-1" href="#/d/${attachedType.name}/{{data.Name}}/${atby.name}/">${atby.displayName}</a></li>
		[#else]
				  			<li><a tabindex="-1" href="#/d/${attachedType.name}/{{data.Name}}/${atby.name}/">${atby.displayName}</a></li>
		[/#if]
	[/#if][/#list]
							<li><a tabindex="-1" href="#/d/${attachedType.name}/{{data.Name}}/setting/info">Settings</a></li>
						</ul>
						
						<!-- <div class="buttons btn-toolbar" x-ng-show="data.standealone='Master'">
							<input type="text" x-ng-model="query" class="input-medium search-query ctrl" placeholder="Filter">	
							<a href="#/d/${type.name}/!new" class="btn btn-small btn-success ctrl"><i class="icon-plus icon-white"></i> 新建</a>
						</div> -->
					</div>
					<div class="widget-content nopadding">[#nested]</div>
		</div>
	</div>
</div>
[/#macro]

[#macro txAttachedHome attachedType type]
		<div class="row-fluid">
			<div class="span12">
				<div class="widget-box">
					<div class="widget-title">
						<span class="icon"> <i class="icon-align-justify"></i>
						</span>
						<ul class="nav nav-tabs">
				  			<li class="active"><a tabindex="-1" href="#/d/${attachedType.name}/{{data.Name}}">概述</a></li>
	[#list attachedType.attachedBy as atby][#if atby.standalone=="Transaction"]
		[#if atby.name == type.name]
				  			<li><a tabindex="-1" href="#/d/${attachedType.name}/{{data.ID}}/${atby.name}/">${atby.displayName}</a></li>
		[#else]
				  			<li><a tabindex="-1" href="#/d/${attachedType.name}/{{data.ID}}/${atby.name}/">${atby.displayName}</a></li>
		[/#if]
	[/#if][/#list]
							<li><a tabindex="-1" href="#/d/${attachedType.name}/{{data.ID}}/setting/info">Settings</a></li>
						</ul>
						
						<!-- <div class="buttons btn-toolbar" x-ng-show="data.standealone='Master'">
							<input type="text" x-ng-model="query" class="input-medium search-query ctrl" placeholder="Filter">	
							<a href="#/d/${type.name}/!new" class="btn btn-small btn-success ctrl"><i class="icon-plus icon-white"></i> 新建</a>
						</div> -->
					</div>
					<div class="widget-content nopadding">[#nested]</div>
		</div>
	</div>
</div>
[/#macro]
[#macro simpleAttached attachedType type]
		<div class="row-fluid">
			<div class="span12">
				<div class="widget-box">
					<div class="widget-title">
						<span class="icon"> <i class="icon-align-justify"></i>
						</span>
						<ul class="nav nav-tabs">
				  			<li><a tabindex="-1" href="#/d/${attachedType.name}/{{attachedData.Name}}">概述</a></li>
	[#list attachedType.attachedBy as atby][#if atby.standalone=="Transaction"]
		[#if atby.name == type.name]
				  			<li class="active"><a tabindex="-1" href="#/d/${attachedType.name}/{{attachedData.Name}}/${atby.name}/">${atby.displayName}</a></li>
		[#else]
				  			<li><a tabindex="-1" href="#/d/${attachedType.name}/{{attachedData.Name}}/${atby.name}/">${atby.displayName}</a></li>
		[/#if]
	[/#if][/#list]
							<li><a tabindex="-1" href="#/d/${attachedType.name}/{{attachedData.Name}}/setting/info">Settings</a></li>
						</ul>
						
						<!-- <div class="buttons btn-toolbar" x-ng-show="data.standealone='Master'">
							<input type="text" x-ng-model="query" class="input-medium search-query ctrl" placeholder="Filter">	
							<a href="#/d/${type.name}/!new" class="btn btn-small btn-success ctrl"><i class="icon-plus icon-white"></i> 新建</a>
						</div> -->
					</div>
					<div class="widget-content nopadding">[#nested]</div>
		</div>
	</div>
</div>
[/#macro]

[#macro simpleAttachedList attachedType type]
		<div class="row-fluid">
			<div class="span12">
				<div class="widget-box">
					<div class="widget-title">
						<span class="icon"> <i class="icon-align-justify"></i>
						</span>
						<ul class="nav nav-tabs">
				  			<li><a tabindex="-1" href="#/d/${attachedType.name}/{{attachedData.Name}}">概述</a></li>
	[#list attachedType.attachedBy as atby][#if atby.standalone=="Transaction"]
		[#if atby.name == type.name]
				  			<li class="active"><a tabindex="-1" href="#/d/${attachedType.name}/{{attachedData.Name}}/${atby.name}/">${atby.displayName}</a></li>
		[#else]
				  			<li><a tabindex="-1" href="#/d/${attachedType.name}/{{attachedData.Name}}/${atby.name}/">${atby.displayName}</a></li>
		[/#if]
	[/#if][/#list]
							<li><a tabindex="-1" href="#/d/${attachedType.name}/{{attachedData.Name}}/setting/info">Settings</a></li>
						</ul>
						
						<div class="buttons btn-toolbar pull-right" x-ng-show="data.standealone='Master'">
							<input type="text" x-ng-model="query" class="input-medium search-query ctrl" placeholder="Filter">	
							<a href="#/d/${attachedType.name}/{{attachedData.Name}}/${type.name}/!new" class="btn btn-small btn-success ctrl"><i class="icon-plus icon-white"></i> 新建</a>
						</div>
					</div>
					<div class="widget-content nopadding">[#nested]</div>
		</div>
	</div>
</div>
[/#macro]

[#macro simplelist type title="" query="query"]
<div class="row-fluid">
	<div class="span12">
		<div class="widget-box">
			<div class="widget-title">
				<span class="icon"> <i class="icon-align-justify"></i>
				</span>
				<h5>${type.displayName}</h5>			
				<div class="buttons btn-toolbar" x-ng-show="data.standealone='Master'">
					<input type="text" x-ng-model="${query}" class="input-medium search-query ctrl" placeholder="Filter">	
					<a href="#/d/${type.name}/!new" class="btn btn-small btn-success ctrl"><i class="icon-plus icon-white"></i> 新建</a>
				</div>
			</div>
			<div class="widget-content nopadding">[#nested]</div>
		</div>
	</div>
</div>
[/#macro]
