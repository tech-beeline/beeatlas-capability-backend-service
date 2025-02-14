package ru.beeline.capability.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.beeline.capability.cleint.DocumentClient;
import ru.beeline.capability.domain.BusinessCapability;
import ru.beeline.capability.mapper.BusinessCapabilityMapper;
import ru.beeline.capability.repository.BusinessCapabilityRepository;
import ru.beeline.fdmlib.dto.capability.PutBusinessCapabilityDTO;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CapabilityExportService {

    @Autowired
    BusinessCapabilityRepository businessCapabilityRepository;

    @Autowired
    BusinessCapabilityMapper businessCapabilityMapper;

    @Autowired
    DocumentClient documentClient;

    public String getExportBusinessCapabilities(Integer docId) {
        List<BusinessCapability> businessCapabilities = businessCapabilityRepository.findByDeletedDateIsNull();
        List<PutBusinessCapabilityDTO> capabilityDTOS = businessCapabilities.stream()
                .map(businessCapabilityMapper::convertToPutCapabilityDTO)
                .collect(Collectors.toList());
        String fileName = "export_business_capability_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";
        List<String> headers = List.of("code", "name", "description", "parents", "isDomain", "status", "author", "link", "owner");
        String sheetName = "Business Capabilities";
        Workbook workbook = createWorkbookWithHeaders(headers, sheetName);
        createBcCell(capabilityDTOS, workbook.getSheet(sheetName));
        File tempFile = writeWorkbookToFile(workbook, fileName);
        if (tempFile != null) {
            documentClient.patchExcelFile(docId, tempFile, fileName);
            tempFile.delete();
        }
        return "docId: " + docId.toString();
    }

    private Workbook createWorkbookWithHeaders(List<String> headers, String sheetName) {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet(sheetName);
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.size(); i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers.get(i));
        }
        return workbook;
    }

    private File writeWorkbookToFile(Workbook workbook, String fileName) {
        File tempFile = null;
        try (FileOutputStream fileOut = new FileOutputStream(tempFile = File.createTempFile(fileName, ".xlsx"))) {
            workbook.write(fileOut);
            log.info("Excel file created successfully: {}", tempFile.getAbsolutePath());
        } catch (IOException e) {
            log.error("Error while writing to Excel file: {}", e.getMessage(), e);
        } finally {
            try {
                workbook.close();
            } catch (IOException e) {
                log.error("Error while closing the workbook: {}", e.getMessage(), e);
            }
        }
        return tempFile;
    }

    private void createBcCell(List<PutBusinessCapabilityDTO> capabilityDTOS, Sheet sheet) {
        int rowNum = 1;
        for (PutBusinessCapabilityDTO dto : capabilityDTOS) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(dto.getCode());
            row.createCell(1).setCellValue(dto.getName());
            row.createCell(2).setCellValue(dto.getDescription());
            row.createCell(3).setCellValue(dto.getParent());
            row.createCell(4).setCellValue(String.valueOf(dto.getIsDomain()));
            row.createCell(5).setCellValue(dto.getStatus());
            row.createCell(6).setCellValue(dto.getAuthor());
            row.createCell(7).setCellValue(dto.getLink());
            row.createCell(8).setCellValue(dto.getOwner());
        }
    }
}

