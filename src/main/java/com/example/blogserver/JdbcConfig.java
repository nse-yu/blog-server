package com.example.blogserver;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.util.Objects;

@Configuration
@PropertySource("classpath:/connection.properties")
public class JdbcConfig {
	@Autowired
	Environment env;
	
	@Bean
	public DataSource dataSource() {
		DriverManagerDataSource ds = new DriverManagerDataSource();
		ds.setDriverClassName(Objects.requireNonNull(env.getProperty("jdbc.driverClassName")));
		ds.setUrl(env.getProperty("jdbc.url"));
		ds.setUsername(env.getProperty("jdbc.userName"));
		ds.setPassword(env.getProperty("jdbc.password"));
		return ds;
	}
}
