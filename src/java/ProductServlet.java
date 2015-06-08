/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.simple.JSONValue;

/**
 *
 * @author c0646039
 */
@WebServlet("/products")
public class ProductServlet extends HttpServlet {
    public static Connection connection() {
        Connection con=null;
        try {
            Class.forName("com.ibm.as400.access.AS400JDBCDriver");
        } catch (ClassNotFoundException e) {
            System.out.println("class not found exception!" + e.getMessage());
        }
        String url = "jdbc:AS400://174.79.32.158";

        try {
             con = DriverManager.getConnection(url, "IBM70", "IBM70");

        } catch (SQLException e) {
            System.out.println("SQL Issue!" + e.getMessage());
            e.printStackTrace();
        }
        return con;
    }

    public static Connection getConnection() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
            
    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setHeader("Content-Type", "text/plain-text");
        try {
            PrintWriter output = response.getWriter();
            String query = "SELECT * FROM product;";
            if (!request.getParameterNames().hasMoreElements()) {
                output.println(resultMethod(query));
            } else {
                int id = Integer.parseInt(request.getParameter("ProductID"));
                output.println(resultMethod("SELECT * FROM product WHERE ProductID= ?", String.valueOf(id)));
            }

        } catch (IOException ex) {
            System.err.println("Input output Exception: " + ex.getMessage());
        }
    }

    /**
     * Handles the HTTP <code>POST</code> method.doGet Method took two arguments
     * It will insert data from product table Also Handle Exception
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Set<String> keyValues = request.getParameterMap().keySet();

        try {
            PrintWriter output = response.getWriter();
            if (keyValues.contains("product_id") && keyValues.contains("name") && keyValues.contains("description")
                    && keyValues.contains("quantity")) {
                String ProductID = request.getParameter("ProductID");
                String name = request.getParameter("name");
                String description = request.getParameter("description");
                String quantity = request.getParameter("quantity");
                doUpdate("INSERT INTO product (product_id,name,description,quantity) VALUES (?, ?, ?, ?)", ProductID, name, description, quantity);

            } else {
                response.setStatus(500);
                output.println("Error: Not data found for this input. Please use a URL of the form /servlet?name=XYZ&age=XYZ");
            }

        } catch (IOException ex) {
            System.err.println("Input Output Issue in doPost Method: " + ex.getMessage());
        }

    }

    /**
     * doPut Method took two Arguments and This method Will update Entries in
     * product Table. This will also catch SQLException and Display an error
     * message.
     *
     * @param request
     * @param response
     */
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) {

        Set<String> keySet = request.getParameterMap().keySet();
        try (PrintWriter out = response.getWriter()) {
            if (keySet.contains("product_id") && keySet.contains("name") && keySet.contains("description") && keySet.contains("quantity")) {
                String ProductID = request.getParameter("product_id");
                String name = request.getParameter("name");
                String description = request.getParameter("description");
                String quantity = request.getParameter("quantity");
                doUpdate("update product set ProductID = ?, name = ?, description = ?, quantity = ? where ProductID = ?", ProductID, name, description, quantity, ProductID);
            } else {
                out.println("Error: Not data found for this input. Please use a URL of the form /products?id=xx&name=XXX&description=XXX&quantity=xx");
            }
        } catch (IOException ex) {
            response.setStatus(500);
            System.out.println("Error in writing output: " + ex.getMessage());
        }
    }

    /**
     * doDelete Method took two Arguments and This method Will delete Entries
     * from product Table This will also catch SQLException and Display an error
     * message
     *
     * @param request
     * @param response
     * @throws IOException
     */
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Set<String> keySet = request.getParameterMap().keySet();
        try (PrintWriter out = response.getWriter()) {
            Connection conn = connection();
            if (keySet.contains("ProductID")) {
                PreparedStatement pstmt = conn.prepareStatement("DELETE FROM `product` WHERE `ProductID`=" + request.getParameter("ProductID"));
                try {
                    pstmt.executeUpdate();
                } catch (SQLException ex) {
                    System.err.println("SQL Exception Error in Update prepared Statement: " + ex.getMessage());
                    out.println("Error in deleting entry.");

                }
            } else {
                out.println("Error: Not enough data in table to delete");

            }
        } catch (SQLException ex) {
            System.err.println("SQL Exception Error: " + ex.getMessage());
        }
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

    /**
     * resultMethod accepts two arguments It executes the Query get ProductID,
     * name, description, quantity
     *
     * @param query
     * @param params
     * @throws SQLException
     * @return
     */
    private String resultMethod(String query, String... params) {
        StringBuilder sb = new StringBuilder();
        String jsonString = "";
        try (Connection conn = connection()) {
            PreparedStatement pstmt = conn.prepareStatement(query);
            for (int i = 1; i <= params.length; i++) {
                pstmt.setString(i, params[i - 1]);
            }
            ResultSet jResult = pstmt.executeQuery();
            List l1 = new LinkedList();
            while (jResult.next()) {
                //Refernce Example 5-2 - Combination of JSON primitives, Map and List
                //https://code.google.com/p/json-simple/wiki/EncodingExamples
                Map m1 = new LinkedHashMap();
                m1.put("ProductID", jResult.getInt("ProductID"));
                m1.put("name", jResult.getString("name"));
                m1.put("description", jResult.getString("description"));
                m1.put("quantity", jResult.getInt("quantity"));
                l1.add(m1);

            }

            jsonString = JSONValue.toJSONString(l1);
        } catch (SQLException ex) {
            System.err.println("SQL Exception Error: " + ex.getMessage());
        }
        return jsonString.replace("},", "},\n");
    }

    /**
     * doUpdate Method accepts two arguments Update the entries in the table
     * 'product'
     *
     * @param query
     * @param params
     * @return numChanges
     */
    private int doUpdate(String query, String... params) {
        int numberUpdate = 0;
        try (Connection conn = connection()) {
            PreparedStatement pstmt = conn.prepareStatement(query);
            for (int i = 1; i <= params.length; i++) {
                pstmt.setString(i, params[i - 1]);
            }
            numberUpdate = pstmt.executeUpdate();
        } catch (SQLException ex) {
            System.err.println("SQL EXception in doUpdate Method" + ex.getMessage());
        }
        return numberUpdate;
    }

   
}
