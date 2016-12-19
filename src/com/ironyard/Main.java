package com.ironyard;

import org.h2.tools.Server;
import spark.ModelAndView;
import spark.Session;
import spark.Spark;
import spark.template.mustache.MustacheTemplateEngine;

import java.nio.file.attribute.UserPrincipalNotFoundException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Main {

    public static void main(String[] args) throws SQLException {
        //connect to server
        Server.createWebServer().start();
        Connection conn = DriverManager.getConnection("jdbc:h2:./main");

        createTables(conn);

        addTestUsers(conn);
        selectTestUsers(conn);
        addTestPosts(conn);
        selectTestPosts(conn);
        selectTestArray(conn);
        //updateTestPost(conn);
        //deleteTestPost(conn);

        Spark.init();

        //get() method to identify user and/or add new user to HashMap
        Spark.get(
                "/",

                ((request, response) -> {
                    Session session = request.session();
                    String userName = session.attribute("loginName");
                    String userPassword = session.attribute("loginPassword");
                    User user = selectUser(conn, userName);

                    HashMap n = new HashMap();
                    //ArrayList<User>  users = selectUsers(conn, name);

                    //create new user/password
                    n.put("loginName", userName);
                    n.put("loginPassword", userPassword);
                    //n.put("createPost", user.twitterEntries);
                    return new ModelAndView(n, "home.html");
                }),
                new MustacheTemplateEngine()
        );//end Spark.get "/"

        Spark.post(
                "/login",
                ((request, response) -> {
                    Session session = request.session();
                    String name = request.queryParams("loginName");
                    String password = request.queryParams("loginPassword");

                    //exception for no name or password
                    if (name == null || password == null) {
                        throw new Exception("Please enter name and password.");
                    }

                    //create object with name & password
                    User user = selectUser(conn, name);
                    if (user == null) {
                        insertUser(conn, name, "");
                    } else if (!user.password.equals(password)) {
                        throw new Exception("Wrong password.");
                    }

                    session.attribute("loginName", name);
                    session.attribute("loginPassword", password);
                    response.redirect("/");
                    return "";
                })
        );//end Spark.post /login

        Spark.post(
                "/createPost",

                ((request, response) -> {
                    Session session = request.session();
                    String name = session.attribute("loginName");

//                    if(user == null){
//                        throw new Exception("Please log in first");
//                    }

                    String text = request.queryParams("createPost");
                    //String replyId = request.queryParams("replyId");

                    //int replyIdNum = Integer.valueOf(replyId);

                    User user = selectUser(conn, name);

                    //insertPost(conn, 1, replyIdNum, text);

                    response.redirect("/");
                    return "";
                })
        );//end Spark.post /createPost

        //edit post
        Spark.post(
                "/updatePost",

                ((request, response) -> {
                    Session session = request.session();
                    String name = session.attribute("loginName");

                    User user = selectUser(conn, name);

                    String num = request.queryParams("num");
                    int x = Integer.parseInt(num);

                    //specifies which post to select and edit
                    selectPost(conn, (x - 1));

                    //repost edited post
                    insertPost(conn, (x - 1), "Alice", "");

                    response.redirect("/");
                    return "";
                }
                ));//end Spark.post /editPost

//        //delete post
//        Spark.post(
//                "/deletePost",
//                ((request, response) -> {
//                    Session session = request.session();
//                    String name = session.attribute("loginName");
//
//                    User user = selectUser(conn, name);
//
//                    String deletePost = request.queryParams("deletePost");
//
//                    int x = Integer.parseInt(deletePost);
//                    selectPost(conn, (x - 1));
//                    deletePost();
//
//                    response.redirect("/");
//                    return "";
//                })
//        );//end Spark.post /deletePost

        Spark.post(
                "/logout",
                ((request, response) -> {
                    Session session = request.session();
                    session.invalidate();
                    response.redirect("/");
                    return "";
                })
        );//end Spark /logout

    }//end main()

    public static void addTestUsers(Connection conn) throws SQLException {
        insertUser(conn, "Alice", "");
        insertUser(conn, "Bob", "");
        insertUser(conn, "Charlie", "");
    }

    public static void selectTestUsers(Connection conn) throws SQLException {
        selectUser(conn, "Alice");
        selectUser(conn, "Bob");
        selectUser(conn, "Charlie");

    }

    static void addTestPosts(Connection conn) throws SQLException {
        insertPost(conn, 1, "Alice", "Hello world!");
        insertPost(conn, 2, "Bob", "This is another thread.");
        insertPost(conn, 3, "Charlie", "Cool thread, Alice!");
        insertPost(conn, 1, "Alice", "Thanks");
    }

    public static void selectTestPosts(Connection conn) throws SQLException {
        selectPost(conn, 1);
        selectPost(conn, 2);
        selectPost(conn, 3);
    }

    public static void selectTestArray(Connection conn) throws SQLException {
        selectPosts(conn, 1);
        selectPosts(conn, 2);
        selectPosts(conn, 3);
    }


    public static void createTables(Connection conn) throws SQLException {
        Statement stmt = conn.createStatement();
        stmt.execute("CREATE TABLE IF NOT EXISTS users (id IDENTITY, name VARCHAR, password VARCHAR)");
        stmt.execute("CREATE TABLE IF NOT EXISTS posts (id IDENTITY, userId INT, author VARCHAR, text VARCHAR)");
    }

    public static void insertUser(Connection conn, String name, String password) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO users VALUES (NULL, ?, ?)");
        stmt.setString(1, name);
        stmt.setString(2, password);
        stmt.execute();
    }

    public static User selectUser(Connection conn, String name) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users WHERE name = ?");
        stmt.setString(1, name);
        ResultSet results = stmt.executeQuery();
        if (results.next()) {
            int id = results.getInt("id");
            String password = results.getString("password");
            return new User(id, name, password);
        }
        return null;
    }

    public static void insertPost(Connection conn, int userId, String author, String text) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO messages VALUES (NULL, ?, ?, ?)");
        stmt.setInt(1, userId);
        stmt.setString(2, author);
        stmt.setString(3, text);
        stmt.execute();
    }

    public static Twitter selectPost(Connection conn, int id) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM messages " +
                "INNER JOIN users ON messages.userID = users.id WHERE messages.id = ?");
        stmt.setInt(1, id);
        ResultSet results = stmt.executeQuery();
        if (results.next()) {
            int replyId = results.getInt("posts.id");  //qualified as we're using 2 tables
            String name = results.getString("users.name");
            String text = results.getString("posts.text");
            return new Twitter(id, replyId, name, text);
        }
        return null;
    }

//    public static Twitter deletePost(Connection conn, int id) throws SQLException {
//        PreparedStatement stmt = conn.prepareStatement("REMOVE FROM posts VALUES (?)");
//        stmt.execute();
//    }
//    return null;
//}

    public static ArrayList<User> selectUsers (Connection conn, String name) throws SQLException {
        ArrayList<User> users = new ArrayList<>();

        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM home" +
                "INNER JOIN users ON u");

        return users;
    }

    public static ArrayList<Twitter> selectPosts (Connection conn, int id) throws SQLException {
        ArrayList<Twitter> posts = new ArrayList<>();

        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM posts " +
                "INNER JOIN users ON posts.userID = users.id WHERE posts.reply_Id = ?");
        stmt.setInt(1, id);
        ResultSet results = stmt.executeQuery();

        while(results.next()){
            int idNum = results.getInt("posts.id");  //qualified as we're using 2 tables
            String name = results.getString("users.name");
            String text = results.getString("posts.text");
            Twitter post = new Twitter (idNum, id, name, text);
            posts.add(post);
        }
        return posts;
    }

}//end class Main
