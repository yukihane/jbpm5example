package com.sample;

import java.io.IOException;
import java.util.List;

import javax.ejb.EJB;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sample.TaskBean.MyTask;

@WebServlet(urlPatterns="/task")
public class TaskServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @EJB
    private TaskLocal taskService;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        String cmd = req.getParameter("cmd");

        if (cmd.equals("list")) {
            String user = req.getParameter("user");

            final List<MyTask> taskList = taskService.retrieveTaskList(user);

            req.setAttribute("taskList", taskList);
            ServletContext context = this.getServletContext();
            RequestDispatcher dispatcher = context
                    .getRequestDispatcher("/task.jsp");
            dispatcher.forward(req, res);
            return;
        } else if (cmd.equals("approve")) {
            final String loginUser = (String) req.getSession().getAttribute("loginName");

            long taskId = Long.parseLong(req.getParameter("taskId"));
            try {
                taskService.approveTask(loginUser, taskId);
            } catch (Exception e) {
                throw new ServletException(e);
            }
            req.setAttribute("message", "Task (id = " + taskId
                    + ") has been completed by " + loginUser);
            ServletContext context = this.getServletContext();
            RequestDispatcher dispatcher = context
                    .getRequestDispatcher("/index.jsp");
            dispatcher.forward(req, res);
            return;
        }

    }
}