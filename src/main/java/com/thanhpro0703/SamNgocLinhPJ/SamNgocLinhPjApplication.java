package com.thanhpro0703.SamNgocLinhPJ;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {
		"com.thanhpro0703.SamNgocLinhPJ",
		"com.thanhpro0703.SamNgocLinhPJ.controller",
		"com.thanhpro0703.SamNgocLinhPJ.config", 
		"com.thanhpro0703.SamNgocLinhPJ.configs",
		"com.thanhpro0703.SamNgocLinhPJ.filters",
		"com.thanhpro0703.SamNgocLinhPJ.utils",
		"com.thanhpro0703.SamNgocLinhPJ.util"
})
@EnableJpaRepositories(basePackages = "com.thanhpro0703.SamNgocLinhPJ.repository")
public class SamNgocLinhPjApplication {

	public static void main(String[] args) {
		SpringApplication.run(SamNgocLinhPjApplication.class, args);
	}

}
