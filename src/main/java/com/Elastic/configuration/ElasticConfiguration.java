package com.Elastic.configuration;

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RequestOptions.Builder;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticConfiguration {
	@Bean
	public RestHighLevelClient restHighLevelClient() {
		try {
			//For connecting to a remote instance of elastic use the commented code.
			/*CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
			credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials("elastic", "password"));	
			RestClientBuilder builder = RestClient.builder(new HttpHost("remote host address", 9243,"https"))
			        .setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
			            @Override
			            public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder) {
			                return httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
			            }
			        });
			RestHighLevelClient client = new RestHighLevelClient(builder);*/
			
			
			RestHighLevelClient client = new RestHighLevelClient(
					RestClient.builder(new HttpHost(new HttpHost("localhost", 9200, "http"))));
			
			return client;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
