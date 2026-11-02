package com.duoc.eventos_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class EventosServiceApplication {

	public static void main(String[] args) {
		// Forzamos la región a nivel de sistema para que AWS no chille
		System.setProperty("aws.region", "us-east-1");
		
		// Limpieza de proxies
		System.setProperty("java.net.useSystemProxies", "false");
		System.clearProperty("http.proxyHost");
		System.clearProperty("http.proxyPort");

		SpringApplication.run(EventosServiceApplication.class, args);
	}

}