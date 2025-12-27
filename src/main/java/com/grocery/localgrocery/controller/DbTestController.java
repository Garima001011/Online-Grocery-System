package com.grocery.localgrocery.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;

@RestController
public class DbTestController {

    private final DataSource dataSource;

    public DbTestController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping("/api/db-test")
    public String dbTest() {
        try (Connection c = dataSource.getConnection()) {
            return "DB OK: " + c.getMetaData().getURL();
        } catch (Exception e) {
            return "DB FAIL: " + e.getMessage();
        }
    }
}
