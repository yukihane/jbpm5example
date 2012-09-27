<html>
<head>
<title>Rewards Basic example</title>
</head>
<body>
<p>Rewards Basic example</p>
<p>Hello, <%= request.getSession().getAttribute("loginName") %> </p>
<p><%= request.getAttribute("message") == null ? "" : request.getAttribute("message") %></p>
<ul>
<li><a href="startProcess.jsp">Create New Content (John's task)</a></li>
<li><a href="task?user=mary&cmd=list">Approval (Mary's Task)</a></li>
</ul>
</body>
</html>