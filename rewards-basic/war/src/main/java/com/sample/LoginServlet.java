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

@WebServlet(name = "LoginServlet", urlPatterns = { "/login" })
public class LoginServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        final String name = req.getParameter("name");
        final HttpSession session = req.getSession();
        ServletContext context = this.getServletContext();

        if ("john".equals(name)) {
            session.setAttribute("loginName", "john");

            RequestDispatcher dispatcher = context
                    .getRequestDispatcher("/index.jsp");
            dispatcher.forward(req, res);
            return;
        } else if ("mary".equals(name)) {
            session.setAttribute("loginName", "mary");

            RequestDispatcher dispatcher = context
                    .getRequestDispatcher("/index.jsp");
            dispatcher.forward(req, res);
            return;
        } else if ("Administrator".equals(name)) {
            session.setAttribute("loginName", "Administrator");

            RequestDispatcher dispatcher = context
                    .getRequestDispatcher("/index.jsp");
            dispatcher.forward(req, res);
            return;
        } else {
            RequestDispatcher dispatcher = context
                    .getRequestDispatcher("/login.jsp");
            dispatcher.forward(req, res);
            return;
        }
    }

}
