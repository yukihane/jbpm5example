package com.sample;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet(urlPatterns = "/login")
public class LoginServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        final String name = req.getParameter("name");
        final HttpSession session = req.getSession();

        if ("john".equals(name)) {
            session.setAttribute("loginName", "john");

            res.sendRedirect("index.jsp");
            return;
        } else if ("mary".equals(name)) {
            session.setAttribute("loginName", "mary");

            res.sendRedirect("index.jsp");
            return;
        } else if ("Administrator".equals(name)) {
            session.setAttribute("loginName", "Administrator");

            res.sendRedirect("index.jsp");
            return;
        } else {
            req.setAttribute("message", "incorrect name.");
            res.sendRedirect("login.jsp");
            return;
        }
    }
    
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.sendRedirect("login.jsp");
    }

}
