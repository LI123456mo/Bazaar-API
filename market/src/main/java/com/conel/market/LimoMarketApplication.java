package com.conel.market;

import com.conel.market.models.Category;
import com.conel.market.models.Product;
import com.conel.market.models.User;
import com.conel.market.repositories.CategoryRepository;
import com.conel.market.repositories.ProductRepository;
import com.conel.market.repositories.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;

@EnableJpaAuditing
@SpringBootApplication
public class LimoMarketApplication {

	public static void main(String[] args) {
		SpringApplication.run(LimoMarketApplication.class, args);
	}
}