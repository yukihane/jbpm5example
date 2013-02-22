<%@page import="com.sample.TaskBean.MyTask"%>
<%@ page import="org.jbpm.task.query.TaskSummary" %>
<%@ page import="java.util.List" %>
<html>
<head>
<title>Task management</title>
</head>
<body>
<% String user = request.getParameter("user"); %>
<p><%= user %>'s Task</p>
<table border="1">
<tr>
<th>Actual Owner</th>
<th>Create By</th>
<th>Create On</th>
<th>Content</th>
<th>Task Name</th>
<th>Task Id</th>
<th>ProcessInstance Id</th>
<th>Action(mary)</th>
<th>Action(john)</th>
</tr>
<%
for (MyTask mt : (List<MyTask>)request.getAttribute("taskList")) {
    final TaskSummary task = mt.getTask();
%>
<tr>
<td><%= task.getActualOwner() %></td>
<td><%= task.getCreatedBy() %></td>
<td><%= task.getCreatedOn() %></td>
<td><%= mt.getContent().getMessage() %></td>
<td><%= task.getName() %></td>
<td><%= task.getId() %></td>
<td><%= task.getProcessInstanceId() %></td>
<td><a href="task?user=<%= user %>&taskId=<%= task.getId() %>&cmd=approve">Approve</a></td>
<td><a href="task?user=<%= user %>&taskId=<%= task.getId() %>&cmd=modify">Modify</a></td>
</tr>
<%
}
%>
</table>
</body>
</html>