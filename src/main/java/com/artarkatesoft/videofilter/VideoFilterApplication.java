package com.artarkatesoft.videofilter;

import com.artarkatesoft.videofilter.service.VideoFilterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class VideoFilterApplication implements CommandLineRunner {

	@Autowired
	private VideoFilterService service;


	public static void main(String[] args) {
		SpringApplication.run(VideoFilterApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		service.convert();

	}
}
