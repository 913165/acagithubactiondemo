package com.example.sbazureappdemo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;

@SpringBootApplication

public class SbazureappdemoApplication  implements CommandLineRunner {
	public static void main(String[] args) {
		SpringApplication.run(SbazureappdemoApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		// Create the folder
		String folderPath = "/var/tmp/empdata";
		File folder = new File(folderPath);
		if (!folder.exists()) {
			folder.mkdir();
			System.out.println("Folder created at " + folderPath);
		}
	}
}
