package com.Elastic.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpHost;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class ElsService {
	@Autowired
	RestHighLevelClient restHighLevelClient;
	
	@Value("${elastic.host}")
	String host;
	
	@Value("${elastic.port}")
	int port;

	public void publishToElastic(Map<String, List<Object>> userCallsMap) throws IOException {
		if (!userCallsMap.isEmpty()) {
			int idCount = 0;
			for (Map.Entry<String, List<Object>> entry : userCallsMap.entrySet()) {
				ObjectMapper mapper = new ObjectMapper();
				String json = mapper.writeValueAsString(entry.getValue());
//				restHighLevelClient = new RestHighLevelClient(
//						RestClient.builder(new HttpHost("localhost", 9200, "http")));
				String jsonString = "{\"user\":\"" + entry.getKey() + "\",\"callist\":" + json + "}";
				System.out.print(jsonString);
				IndexRequest request = new IndexRequest("" + entry.getKey());
				request.id("" + idCount);
				idCount++;
				request.source(jsonString, XContentType.JSON);
				restHighLevelClient.index(request, RequestOptions.DEFAULT);
			}
		}
	}
}
