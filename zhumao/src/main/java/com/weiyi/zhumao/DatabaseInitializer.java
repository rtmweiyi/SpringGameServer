package com.weiyi.zhumao;

import java.sql.SQLException;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DatabaseInitializer {

	@Autowired
	DataSource dataSource;

	@PostConstruct
	public void init() throws SQLException {
		try (var conn = dataSource.getConnection()) {
			try (var stmt = conn.createStatement()) {
				stmt.executeUpdate("CREATE TABLE IF NOT EXISTS users (" //
						+ "id BIGINT IDENTITY NOT NULL PRIMARY KEY, " //
						+ "name VARCHAR(100) NOT NULL, " //
						+ "token VARCHAR(100) NOT NULL, " //
						+ "createdAt BIGINT NOT NULL)");
				
				stmt.executeUpdate("CREATE TABLE IF NOT EXISTS shop (" //
				+ "id BIGINT NOT NULL PRIMARY KEY, " //
				+ "cars VARCHAR(100) NOT NULL, " //
				+ "currentCar VARCHAR(100) NOT NULL)");
			}
		}
	}
}
