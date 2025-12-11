package com.heroku.java;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@SpringBootApplication
@Controller
public class GettingStartedApplication {
    private final DataSource dataSource;

    @Autowired
    public GettingStartedApplication(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/db")
    String database(Map<String, Object> model) {
        System.out.println("Ahmed Rao visited /db");

        try (Connection connection = dataSource.getConnection()) {
            final var statement = connection.createStatement();

            // table with timestamp + random string
            statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS table_timestamp_and_random_string " +
                            "(tick timestamp, random_string varchar(30))");

            // insert one new row on every request
            statement.executeUpdate(
                    "INSERT INTO table_timestamp_and_random_string VALUES " +
                            "(now(), '" + getRandomString() + "')");

            // read rows back
            final var resultSet = statement.executeQuery(
                    "SELECT tick, random_string FROM table_timestamp_and_random_string");
            final var output = new ArrayList<String>();
            while (resultSet.next()) {
                output.add(
                        "Timestamp: " + resultSet.getTimestamp("tick") +
                                " " + resultSet.getString("random_string"));
            }

            model.put("records", output);
            return "database";
        } catch (Throwable t) {
            model.put("message", t.getMessage());
            return "error";
        }


    }

    private String getRandomString() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder(10);
        for (int i = 0; i < 10; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

        @GetMapping("/dbinput")
    public String showDbInputForm(Map<String, Object> model) {
        // optional message text
        return "dbinput";
    }

    @PostMapping("/dbinput")
    public String handleDbInput(
            @RequestParam("userInput") String userInput,
            Map<String, Object> model) {

        System.out.println("User input from /dbinput: " + userInput);

        try (Connection connection = dataSource.getConnection()) {
            final var statement = connection.prepareStatement(
                "INSERT INTO table_timestamp_and_random_string (tick, random_string) " +
                "VALUES (now(), ?)"
            );
            statement.setString(1, userInput);
            statement.executeUpdate();

            model.put("message", "Saved: " + userInput);
        } catch (Throwable t) {
            model.put("message", "Error: " + t.getMessage());
        }

        return "dbinput";
    }


    public static void main(String[] args) {
        SpringApplication.run(GettingStartedApplication.class, args);
    }
}
