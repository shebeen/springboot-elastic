package com.Elastic.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.http.HttpHost;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping(value = { "/elastic" })
public class ElasticController {
	@Autowired
	RestHighLevelClient restHighLevelClient;

	@PostMapping(value = "/")
	public ResponseEntity<?> elasticSearch(@RequestParam("file") MultipartFile uploadfile) throws IOException {
		Workbook workbook = null;
		byte[] bytes = uploadfile.getBytes();
		Path path = Paths.get("/home/shebeen/Documents/" + uploadfile.getOriginalFilename());
		Files.write(path, bytes);
		File spreadSheet = new File("/home/shebeen/Documents/" + uploadfile.getOriginalFilename());
		InputStream inputStream = new FileInputStream(spreadSheet);
		if (uploadfile.isEmpty()) {
			return new ResponseEntity("please select a file!", HttpStatus.OK);
		}
		String fileExtensionName = uploadfile.getOriginalFilename()
				.substring(uploadfile.getOriginalFilename().indexOf("."));

		if (fileExtensionName.equals(".xlsx")) {

			workbook = new XSSFWorkbook(inputStream);

		}

		// Check condition if the file is xls file

		else if (fileExtensionName.equals(".xls")) {

			// If it is xls file then create object of HSSFWorkbook class

			workbook = new HSSFWorkbook(inputStream);

		}

		// Read sheet inside the workbook by its name
		int sheets = workbook.getNumberOfSheets();
		for (int i = 0; i < sheets; i++) {

			Sheet sheet = workbook.getSheetAt(i);

			// Find number of rows in excel file

			int rowCount = sheet.getLastRowNum() - sheet.getFirstRowNum();

			// Create a loop over all the rows of excel file to read it
			Map<Integer,String> columnNames = new HashMap<Integer,String>();
			Map<String, List<Object>> userCallsMap = new HashMap<String, List<Object>>();
			// First iteration for selecting user one by one.
			for (int j = 0; j < rowCount + 1; j++) {
				Row row = sheet.getRow(j);
				if (row == null) {
					continue;
				}
				if(j==0) {
					for(int c = 0;c<row.getLastCellNum();c++) {
						columnNames.put(c, row.getCell(c).getStringCellValue());
					}
					continue;
				}
				if (userCallsMap.containsKey(row.getCell(0).getStringCellValue().toLowerCase())) {
					continue;
				}
				String selectedUser = row.getCell(0).getStringCellValue();
				List<Object> callList = new ArrayList<Object>();
				// Second iteration searching for the selected user
				for (int k = 0; k < rowCount + 1; k++) {
					Row tempRow = sheet.getRow(k);
					if (tempRow.getCell(0).getStringCellValue().toLowerCase().equals(selectedUser.toLowerCase())) {
						ArrayList cellList = new ArrayList();
						for (int l = 0; l < tempRow.getLastCellNum(); l++) {
							if (tempRow.getCell(l).getCellType() == Cell.CELL_TYPE_STRING) {
								cellList.add("{\""+columnNames.get(l)+"\":\""+tempRow.getCell(l).getStringCellValue()+"\"}");
							} else if (tempRow.getCell(l).getCellType() == Cell.CELL_TYPE_NUMERIC) {
								cellList.add("{\""+columnNames.get(l)+"\":"+tempRow.getCell(l).getNumericCellValue()+"}");
							} 
						}
						callList.add(cellList);
					}
				}
				userCallsMap.put(selectedUser.toLowerCase(), callList);
			}
			if (!userCallsMap.isEmpty()) {
				int idCount = 0;
				for (Map.Entry entry : userCallsMap.entrySet()) {
					ObjectMapper mapper = new ObjectMapper();
					String json = mapper.writeValueAsString(entry.getValue());
					restHighLevelClient = new RestHighLevelClient(
							RestClient.builder(new HttpHost("localhost", 9200, "http")));
					String jsonString = "{\"user\":\""+entry.getKey()+"\",\"callist\":"+json+"}";
					System.out.print(jsonString);
					IndexRequest request = new IndexRequest(""+entry.getKey());
//					request.id(""+idCount);
					idCount++;
					request.source(jsonString, XContentType.JSON);
					IndexResponse response = restHighLevelClient.index(request, RequestOptions.DEFAULT);
					String id = response.getId();
					String index = response.getIndex();
					String type = response.getType();
					long version = response.getVersion();
					System.out.print(
							"id : " + id + "\n index : " + index + "\n type : " + type + "\n version : " + version);
				}
			}
		}

//-------------------------------------------------------------------------------------------------------------------------------

		return new ResponseEntity("File data successfully pushed to elastic - " + uploadfile.getOriginalFilename(),
				new HttpHeaders(), HttpStatus.OK);
	}
}
