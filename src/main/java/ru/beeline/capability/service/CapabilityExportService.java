package ru.beeline.capability.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.beeline.capability.client.DocumentClient;
import ru.beeline.capability.domain.BusinessCapability;
import ru.beeline.capability.domain.TechCapability;
import ru.beeline.capability.dto.CapabilityExportDTO;
import ru.beeline.capability.mapper.BusinessCapabilityMapper;
import ru.beeline.capability.mapper.TechCapabilityMapper;
import ru.beeline.capability.repository.BusinessCapabilityRepository;
import ru.beeline.capability.repository.TechCapabilityRepository;
import ru.beeline.capability.dto.PutBusinessCapabilityDTO;
import ru.beeline.capability.dto.PutTechCapabilityDTO;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
public class CapabilityExportService {

    @Autowired
    BusinessCapabilityRepository businessCapabilityRepository;

    @Autowired
    TechCapabilityRepository techCapabilityRepository;

    @Autowired
    BusinessCapabilityMapper businessCapabilityMapper;

    @Autowired
    TechCapabilityMapper techCapabilityMapper;

    @Autowired
    DocumentClient documentClient;

    public CapabilityExportDTO postExportBusinessCapabilities(Integer docId) {
        List<BusinessCapability> businessCapabilities = businessCapabilityRepository.findByDeletedDateIsNull();
        String fileName = "export_business_capability_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";
        List<String> headers = List.of("code", "name", "description", "parents", "isDomain", "status", "author", "link", "owner");
        String sheetName = "Business Capabilities";
        Workbook workbook = createWorkbookWithHeaders(headers, sheetName);
        createBcCell(businessCapabilities, workbook.getSheet(sheetName));
        System.gc();
        File tempFile = writeWorkbookToFile(workbook, fileName);
        if (tempFile != null) {
            documentClient.patchExcelFile(docId, tempFile, fileName);
            tempFile.delete();
        }
        return CapabilityExportDTO.builder().docId(docId).build();
    }

    public CapabilityExportDTO postExportTechCapabilities(Integer docId) {
        List<TechCapability> techCapabilities = techCapabilityRepository.findByDeletedDateIsNull();
        String fileName = "export_tech_capability_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";
        List<String> headers = List.of("code", "name", "description", "parents", "status", "author", "link", "owner");
        String sheetName = "Tech Capabilities";
        Workbook workbook = createWorkbookWithHeaders(headers, sheetName);
        createTcCell(techCapabilities, workbook.getSheet(sheetName));
        System.gc();
        File tempFile = writeWorkbookToFile(workbook, fileName);
        if (tempFile != null) {
            documentClient.patchExcelFile(docId, tempFile, fileName);
            tempFile.delete();
        }
        return CapabilityExportDTO.builder().docId(docId).build();
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

    private void createBcCell(List<BusinessCapability> businessCapabilities, Sheet sheet) {
        int rowNum = 1;
        for (BusinessCapability businessCapability : businessCapabilities) {
            PutBusinessCapabilityDTO dto = businessCapabilityMapper.convertToPutCapabilityDTO(businessCapability);
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

    private void createTcCell(List<TechCapability> techCapabilities, Sheet sheet) {
        int rowNum = 1;
        for (TechCapability techCapability : techCapabilities) {
            PutTechCapabilityDTO dto = techCapabilityMapper.convertToPutTechCapabilityDTO(techCapability);
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(dto.getCode());
            row.createCell(1).setCellValue(dto.getName());
            row.createCell(2).setCellValue(dto.getDescription());
            row.createCell(3).setCellValue(String.join(", ", dto.getParents()));
            row.createCell(4).setCellValue(dto.getStatus());
            row.createCell(5).setCellValue(dto.getAuthor());
            row.createCell(6).setCellValue(dto.getLink());
            row.createCell(7).setCellValue(dto.getOwner());
        }
    }
}

