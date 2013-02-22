<html>
<head>
<title>Start a process</title>
</head>
<body>
<% String taskId = request.getParameter("taskId") == null ? "" : request.getParameter("taskId"); %>
    <p>Create New Content</p>
    <form method="post" action="process">
        <p>
            <textarea rows="10" cols="40" name="message"></textarea>
        </p>
        <p>
            <input type="submit" value="POST" />
            <input type="hidden" name="taskId" value="<%= taskId %>" />
        </p>
    </form>
</body>
</html>