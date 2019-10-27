package com.Elastic.controller;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.Elastic.service.ElsService;
import com.Elastic.service.SpreadsheetService;
@RestController
@RequestMapping(value = { "/elastic" })
public class ElasticController {
	@Autowired
	RestHighLevelClient restHighLevelClient;
	
	@Autowired
	SpreadsheetService spreadSheetService;
	
	@Autowired
	ElsService elsService;

	@PostMapping(value = "/")
	public ResponseEntity<String> elasticSearch(@RequestParam("file") MultipartFile uploadfile) throws IOException {
		InputStream inputFile = spreadSheetService.multipartToInputStream(uploadfile);
		if (uploadfile.isEmpty()) {
			return new ResponseEntity<String>("please select a file!", HttpStatus.OK);
		}
		String fileExtensionName = uploadfile.getOriginalFilename()
				.substring(uploadfile.getOriginalFilename().indexOf("."));
		Map<String,List<Object>> userCallsMap = spreadSheetService.readSpreadSheet(inputFile, fileExtensionName);
		elsService.publishToElastic(userCallsMap);
		return new ResponseEntity<String>("File data successfully pushed to elastic - " + uploadfile.getOriginalFilename(),
				new HttpHeaders(), HttpStatus.OK);
	}
}
