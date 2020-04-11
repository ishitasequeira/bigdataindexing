package com.bigdataindexing.project;

import org.apache.http.HttpHost;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.transport.TransportClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import redis.clients.jedis.JedisPool;

@SpringBootApplication
public class BigDataIndexingProjectApplication {

	//The config parameters for the connection
	private static final String HOST = "localhost";
	private static final int PORT_ONE = 9200;
	private static final String SCHEME = "http";

	@Value(value = "${redis.hostname}")
	private String redisHostname;

	@Value(value = "${redis.port}")
	private int redisPort;

	@Bean
	public JedisPool getJedisPool(){
		return new JedisPool(redisHostname,redisPort);
	}

	@Bean
	private static synchronized RestHighLevelClient makeConnection() {
//		return new RestHighLevelClient(
//					RestClient.builder(
//							new HttpHost(HOST, PORT_ONE, SCHEME)));

		Client client = new TransportClient()
				.addTransportAddress(new InetSocketTransportAddress("localhost", 9300);

	}


	public static void main(String[] args) {
		SpringApplication.run(BigDataIndexingProjectApplication.class, args);
	}

}
