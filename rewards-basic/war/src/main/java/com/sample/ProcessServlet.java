package com.sample;

import java.io.IOException;

import javax.ejb.EJB;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sample.entity.MyContent;

@WebServlet(urlPatterns="/process")
public class ProcessServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @EJB
    private ProcessLocal processService;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        
        long processInstanceId = -1;
        try {
            final String loginUser = (String) req.getSession().getAttribute("loginName");
            final String message = req.getParameter("message");
            final String taskId = req.getParameter("taskId");
//            final MyContent cont = new MyContent();
//            cont.setMessage(message);
            
            processInstanceId = processService.editContent(loginUser, taskId, message);
        } catch (Exception e) {
            throw new ServletException(e);
        }

        req.setAttribute("message", "process instance (id = "
                + processInstanceId + ") has been started.");

        ServletContext context = this.getServletContext();
        RequestDispatcher dispatcher = context
                .getRequestDispatcher("/index.jsp");
        dispatcher.forward(req, res);
    }
}