/*
 * Copyright (c) 2024 PJSC VimpelCom
 */

package ru.beeline.capability.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.beeline.capability.domain.*;
import ru.beeline.capability.dto.criteria.CriteriaRecordResponseDTO;
import ru.beeline.capability.dto.criteria.PostCriteriaRecordDTO;
import ru.beeline.capability.dto.criteria.PutEnumCriteriaDTO;
import ru.beeline.capability.exception.ForbiddenException;
import ru.beeline.capability.exception.NotFoundException;
import ru.beeline.capability.repository.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static ru.beeline.capability.utils.Constants.USER_ROLES_HEADER;

@Transactional
@Service
public class CriteriaService {

    private static final String TYPE_BUSINESS_CAPABILITY = "business_capability";
    private static final String TYPE_TECH_CAPABILITY = "tech_capability";

    @Autowired
    private CriteriaRepository criteriaRepository;

    @Autowired
    private BusinessCapabilityRepository businessCapabilityRepository;

    @Autowired
    private TechCapabilityRepository techCapabilityRepository;

    @Autowired
    private CriteriaBcRepository criteriaBcRepository;

    @Autowired
    private CriteriaTcRepository criteriaTcRepository;


    public List<EnumCriteria> getCriteria(String filter) {
        List<EnumCriteria> result = null;
        if ("bc".equalsIgnoreCase(filter)) {
            result = criteriaRepository.findAllByBcCriteria();
        } else if ("tc".equalsIgnoreCase(filter)) {
            result = criteriaRepository.findAllByTcCriteria();
        } else {
            result = criteriaRepository.findAll();
        }
        return result;
    }

    public EnumCriteria upsertCriteria(PutEnumCriteriaDTO dto, HttpServletRequest request) {
        assertAdministrator(request);
        validateRequired(dto);
        boolean revers = Boolean.TRUE.equals(dto.getRevers());
        String name = dto.getName().trim();
        Optional<EnumCriteria> existing = criteriaRepository.findByNameIgnoreCase(name);
        EnumCriteria entity;
        if (existing.isPresent()) {
            entity = existing.get();
            entity.setName(name);
            entity.setDescription(dto.getDescription());
            entity.setInterval(dto.getInterval());
            entity.setRevers(revers);
            entity.setMinDesc(dto.getMinDesc());
            entity.setMaxDesc(dto.getMaxDesc());
            entity.setThreshold(dto.getThreshold());
            entity.setType(dto.getType());
        } else {
            entity = EnumCriteria.builder()
                    .name(name)
                    .description(dto.getDescription())
                    .type(dto.getType())
                    .interval(dto.getInterval())
                    .threshold(dto.getThreshold())
                    .revers(revers)
                    .minDesc(dto.getMinDesc())
                    .maxDesc(dto.getMaxDesc())
                    .build();
        }
        return criteriaRepository.save(entity);
    }

    public CriteriaRecordResponseDTO upsertCriteriaRecord(PostCriteriaRecordDTO dto) {
        validatePostCriteriaRecord(dto);
        String typeNorm = dto.getType().trim();
        if (!TYPE_BUSINESS_CAPABILITY.equalsIgnoreCase(typeNorm) && !TYPE_TECH_CAPABILITY.equalsIgnoreCase(typeNorm)) {
            throw new IllegalArgumentException("Невозможный тип сущности для критерия");
        }
        if (TYPE_BUSINESS_CAPABILITY.equalsIgnoreCase(typeNorm)) {
            BusinessCapability bc = businessCapabilityRepository
                    .findByCodeIgnoreCaseAndDeletedDateIsNull(dto.getCode().trim())
                    .orElseThrow(() -> new IllegalArgumentException("BC с таким кодом не существует"));
            EnumCriteria criterion = criteriaRepository.findByNameIgnoreCase(dto.getCriterionName().trim())
                    .orElseThrow(() -> new IllegalArgumentException("Критерий с таким названием не существует"));
            return upsertCriteriaBc(bc.getId(), criterion.getId(), dto);
        }
        TechCapability tc = techCapabilityRepository
                .findByCodeIgnoreCaseAndDeletedDateIsNull(dto.getCode().trim())
                .orElseThrow(() -> new IllegalArgumentException("TC с таким кодом не существует"));
        EnumCriteria criterion = criteriaRepository.findByNameIgnoreCase(dto.getCriterionName().trim())
                .orElseThrow(() -> new IllegalArgumentException("Критерий с таким названием не существует"));
        return upsertCriteriaTc(tc.getId(), criterion.getId(), dto);
    }

    private CriteriaRecordResponseDTO upsertCriteriaBc(Long bcId, Long criterionId, PostCriteriaRecordDTO dto) {
        Integer grade = dto.getGrade() == null ? dto.getValue() : dto.getGrade();
        String comment = dto.getComment();
        Optional<CriteriasBc> existing = criteriaBcRepository.findByBcIdAndCriterionId(bcId, criterionId);
        CriteriasBc entity;
        if (existing.isPresent()) {
            entity = existing.get();
            entity.setValue(dto.getValue());
            entity.setGrade(grade);
            entity.setComment(comment);
        } else {
            entity = CriteriasBc.builder()
                    .bcId(bcId)
                    .criterionId(criterionId)
                    .value(dto.getValue())
                    .grade(grade)
                    .comment(comment)
                    .build();
        }
        entity = criteriaBcRepository.save(entity);
        return CriteriaRecordResponseDTO.builder()
                .id(entity.getId())
                .criterionId(entity.getCriterionId())
                .value(entity.getValue())
                .grade(entity.getGrade())
                .bcId(entity.getBcId())
                .comment(entity.getComment())
                .build();
    }

    private CriteriaRecordResponseDTO upsertCriteriaTc(Long tcId, Long criterionId, PostCriteriaRecordDTO dto) {
        Integer grade = dto.getGrade() == null ? dto.getValue() : dto.getGrade();
        String comment = dto.getComment();
        Optional<CriteriasTc> existing = criteriaTcRepository.findByTcIdAndCriterionId(tcId, criterionId);
        CriteriasTc entity;
        if (existing.isPresent()) {
            entity = existing.get();
            entity.setValue(dto.getValue());
            entity.setGrade(grade);
            entity.setComment(comment);
        } else {
            entity = CriteriasTc.builder()
                    .tcId(tcId)
                    .criterionId(criterionId)
                    .value(dto.getValue())
                    .grade(grade)
                    .comment(comment)
                    .build();
        }
        entity = criteriaTcRepository.save(entity);
        return CriteriaRecordResponseDTO.builder()
                .id(entity.getId())
                .criterionId(entity.getCriterionId())
                .value(entity.getValue())
                .grade(entity.getGrade())
                .tcId(entity.getTcId())
                .comment(entity.getComment())
                .build();
    }

    private void validatePostCriteriaRecord(PostCriteriaRecordDTO dto) {
        if (dto.getType() == null || dto.getType().isBlank()
                || dto.getCode() == null || dto.getCode().isBlank()
                || dto.getCriterionName() == null || dto.getCriterionName().isBlank()
                || dto.getValue() == null) {
            throw new IllegalArgumentException("Не переданы обязательные параметры");
        }
    }


    private void assertAdministrator(HttpServletRequest request) {
        List<String> roles = parseRolesHeader(request.getHeader(USER_ROLES_HEADER));
        if (!roles.contains("ADMINISTRATOR")) {
            throw new ForbiddenException("Доступ запрещен");
        }
    }

    private List<String> parseRolesHeader(String value) {
        if (value == null || value.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.stream(value.split(","))
                .map(str -> str.replaceAll("\"", ""))
                .map(str -> str.replaceAll("]", ""))
                .map(str -> str.replaceAll("\\[", ""))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    private void validateRequired(PutEnumCriteriaDTO dto) {
        if (dto.getName() == null || dto.getName().isBlank()
                || dto.getDescription() == null || dto.getDescription().isBlank()
                || dto.getInterval() == null
                || dto.getMinDesc() == null || dto.getMinDesc().isBlank()
                || dto.getMaxDesc() == null || dto.getMaxDesc().isBlank()) {
            throw new IllegalArgumentException("Не переданы обязательные параметры");
        }
    }

    @Transactional
    public void deleteCriteria(Long id, HttpServletRequest request) {
        assertAdministrator(request);
        validatePositiveId(id);
        if (!criteriaRepository.existsById(id)) {
            throw new NotFoundException("Критерий не найден");
        }
        criteriaTcRepository.deleteAllByCriterionId(id);
        criteriaBcRepository.deleteAllByCriterionId(id);
        criteriaRepository.deleteById(id);
    }

    private void validatePositiveId(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Идентификатор критерия должен быть положительным числом");
        }
    }
}
