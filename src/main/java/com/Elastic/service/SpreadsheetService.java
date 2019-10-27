package com.Elastic.service;

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

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class SpreadsheetService {
	public InputStream multipartToInputStream(MultipartFile file) throws IOException {
		byte[] bytes = file.getBytes();
		Path path = Paths.get("/home/shebeen/Documents/" + file.getOriginalFilename());
		Files.write(path, bytes);
		File spreadSheet = new File("/home/shebeen/Documents/" + file.getOriginalFilename());
		return new FileInputStream(spreadSheet);
	}

	public Map<String, List<Object>> readSpreadSheet(InputStream spreadsheet,String extention) throws IOException {
		Workbook workbook = null;
		Map<String, List<Object>> userCallsMap = new HashMap<String, List<Object>>();
		if (extention.equals(".xlsx")) {
			workbook = new XSSFWorkbook(spreadsheet);
		}
		else if (extention.equals(".xls")) {
			workbook = new HSSFWorkbook(spreadsheet);
		}
		int sheets = workbook.getNumberOfSheets();
		for (int i = 0; i < sheets; i++) {
			Sheet sheet = workbook.getSheetAt(i);
			int rowCount = sheet.getLastRowNum() - sheet.getFirstRowNum();

			Map<Integer,String> columnNames = new HashMap<Integer,String>();
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
				for (int k = 0; k < rowCount + 1; k++) {
					Row tempRow = sheet.getRow(k);
					if (tempRow.getCell(0).getStringCellValue().toLowerCase().equals(selectedUser.toLowerCase())) {
						ArrayList<String> cellList = new ArrayList<String>();
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
		}
		return userCallsMap;
	}
}
