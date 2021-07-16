
package com.example;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Map;

@Controller
@SpringBootApplication
public class Main {

  @Value("${spring.datasource.url}")
  private String dbUrl;

  @Autowired
  private DataSource dataSource;

  public static void main(String[] args) throws Exception {
    SpringApplication.run(Main.class, args);
  }

  @RequestMapping("/")
  String index() {
    return "home";
  }

  @GetMapping(path = "/get/{n}")
  public String getUserData(Map<String, Object> model, @PathVariable String n) {

    try (Connection connection = dataSource.getConnection()) {
      Statement stmt = connection.createStatement();
      String sql = "SELECT name,color,width,height FROM rectangles WHERE name = '" + n + "' ";

      ResultSet rs2 = stmt.executeQuery(sql);
      System.out.println(rs2);
      ArrayList<Rectangle> d = new ArrayList<Rectangle>();

      while (rs2.next()) {
        Rectangle o = new Rectangle();
        o.setName(rs2.getString("name"));
        o.setColor(rs2.getString("color"));
        o.setWidth(rs2.getInt("width"));
        o.setHeight(rs2.getInt("height"));
        d.add(o);
      }
      model.put("r", d);
      return "dbb";
    } catch (Exception e) {
      return "error";
    }
  }

  @GetMapping(path = "/d/{n}")
  public String deleteUserData(Map<String, Object> model, @PathVariable String n) {

    try (Connection connection = dataSource.getConnection()) {
      Statement stmt = connection.createStatement();
      String sql = "DELETE FROM rectangles WHERE name = '" + n + "' ";

      stmt.executeUpdate(sql);
      return "success";
    } catch (Exception e) {
      return "error";
    }
  }

  @GetMapping(path = "/rect")
  public String getRectangleForm(Map<String, Object> model) {
    Rectangle rectangle = new Rectangle();
    model.put("rectangle", rectangle);
    return "rect";
  }

  @PostMapping(path = "/rect", consumes = { MediaType.APPLICATION_FORM_URLENCODED_VALUE })

  public String handleRectangleSubmit(Map<String, Object> model, Rectangle rectangle) throws Exception {

    try (Connection connection = dataSource.getConnection()) {
      Statement stmt = connection.createStatement();
      stmt.executeUpdate(
          "CREATE TABLE IF NOT EXISTS Rectangles (id serial, name varchar(10), color varchar(10), width varchar(50),height varchar(50))");
      stmt.executeUpdate("CREATE TABLE IF NOT EXISTS info (Attr varchar(10), Val varchar(10))");

      String sql = "INSERT INTO Rectangles (name,color,width,height) VALUES ('" + rectangle.getName() + "','"
          + rectangle.getColor() + "','" + rectangle.getWidth() + "','" + rectangle.getHeight() + "')";

      String sql1 = "INSERT INTO info(Attribute,Value) VALUES ('Name','" + rectangle.getName() + "')";

      String sql2 = "INSERT INTO info(Attribute,Value) VALUES ('Color','" + rectangle.getColor() + "')";

      stmt.executeUpdate(sql);

      ResultSet rs = stmt.executeQuery(("SELECT * FROM Rectangles"));
      ArrayList<Rectangle> data = new ArrayList<Rectangle>();

      while (rs.next()) {
        Rectangle o = new Rectangle();
        o.setName(rs.getString("name"));
        o.setColor(rs.getString("color"));
        o.setWidth(rs.getInt("width"));
        o.setHeight(rs.getInt("height"));
        data.add(o);
      }
      model.put("records", data);
      return "db";
    } catch (Exception e) {
      model.put("message", e.getMessage());
      return "error";
    }
  }

  @RequestMapping("/db")
  String db(Map<String, Object> model) {
    try (Connection connection = dataSource.getConnection()) {
      Statement stmt = connection.createStatement();
      stmt.executeUpdate("CREATE TABLE IF NOT EXISTS ticks (tick timestamp)");
      stmt.executeUpdate("INSERT INTO ticks VALUES (now())");
      ResultSet rs = stmt.executeQuery("SELECT tick FROM ticks");

      ArrayList<String> output = new ArrayList<String>();
      while (rs.next()) {
        output.add("Read from DB: " + rs.getTimestamp("tick"));
      }

      model.put("records", output);
      return "db";
    } catch (Exception e) {
      model.put("message", e.getMessage());
      return "error";
    }
  }

  @Bean
  public DataSource dataSource() throws SQLException {
    if (dbUrl == null || dbUrl.isEmpty()) {
      return new HikariDataSource();
    } else {
      HikariConfig config = new HikariConfig();
      config.setJdbcUrl(dbUrl);
      return new HikariDataSource(config);
    }
  }

}
