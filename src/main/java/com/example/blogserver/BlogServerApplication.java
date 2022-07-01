package com.example.blogserver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication
public class BlogServerApplication implements CommandLineRunner{
	@Autowired
	private JdbcService service;
	
	public static void main(String[] args) {
		SpringApplication.run(BlogServerApplication.class, args);
	}

	@Override
	public void run(String... args)  {
		//service.insertArticles();
	}

}
