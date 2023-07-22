package com.example.studentformdemo.api;

import com.example.studentformdemo.dto.StudentDTO;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.*;

@WebServlet(urlPatterns = "/student")
public class StudentFormServlet extends HttpServlet {
    Connection connection;
    private static final String SaveStudentData = "INSERT INTO student(name,email,city,level) VALUES (?,?,?,?)";
    @Override
    public void init() throws ServletException {

        try {
            Class.forName(getServletContext().getInitParameter("mysql-driver"));
            String username = getServletContext().getInitParameter("db-user");
            String password = getServletContext().getInitParameter("db-pw");
            String url = getServletContext().getInitParameter("db-url");
            this.connection = DriverManager.getConnection(url,username,password);

        } catch (ClassNotFoundException | SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String origin = req.getHeader("Origin");
        if (origin != null && origin.contains("http://localhost:63342")) {
            resp.setHeader("Access-Control-Allow-Origin", origin);
            resp.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            resp.setHeader("Access-Control-Allow-Headers", "Content-Type");
            resp.setHeader("Access-Control-Expose-Headers", "Content-Type");
            // You can also include the following line if you need to include credentials (e.g., cookies) in the request.
            // resp.setHeader("Access-Control-Allow-Credentials", "true");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse rsp) throws ServletException, IOException {
        System.out.println("do post");
        doOptions(req, rsp);

        if(req.getContentType() == null || req.getContentType().toLowerCase().startsWith("application/json")){
            rsp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        }
        try {

            Jsonb jsonb = JsonbBuilder.create();
            StudentDTO studentObj = jsonb.fromJson(req.getReader(), StudentDTO.class);

            //validation
            if(studentObj.getName() == null || !studentObj.getName().matches("[A-Za-z ]+")){
                throw new RuntimeException("Invalid Name");
            } else if (studentObj.getCity() == null || !studentObj.getCity().matches("[A-Za-z ]+")) {
                throw new RuntimeException("Invalid City");
            } else if (studentObj.getEmail()==null) {
                throw new RuntimeException("Invalid Email");
            } else if (studentObj.getLevel() <= 0) {
                throw new RemoteException("Invalid Level");
            }
            //save data in db

            PreparedStatement ps =
                    connection.prepareStatement(SaveStudentData, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1,studentObj.getName());
            ps.setString(2,studentObj.getCity());
            ps.setString(3,studentObj.getEmail());
            ps.setInt(4,studentObj.getLevel());

            if(ps.executeUpdate() != 1){
                throw new RuntimeException("Save Failed");
            }else{
                System.out.println("Saved");
            }


            ResultSet rst = ps.getGeneratedKeys();
            rst.next();
            rsp.setStatus(HttpServletResponse.SC_CREATED);
            //the created json is sent to frontend
            rsp.setContentType("application/json");
            jsonb.toJson(studentObj,rsp.getWriter());

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        //Todo:Exception Handle

    }
}
