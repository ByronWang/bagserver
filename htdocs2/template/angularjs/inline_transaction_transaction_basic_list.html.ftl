[#ftl/]

[#assign queryName="query" + type.name]
[@nl.simplelist type=type query=queryName]
	<ul class="unstyled recent-posts"  x-jsonDataUrl="/d/${attachedType.name}/{{data.ID}}/${type.name}"  jsonArray="true">
		<li x-ng-repeat="item in jsonData | filter: ${queryName}">
				{{item.Comment}}
		</li>
	</ul>
[/@nl.simplelist]

