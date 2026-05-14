/*
 * Copyright (c) 2024 PJSC VimpelCom
 */

package ru.beeline.capability.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.beeline.capability.domain.EnumCriteria;
import ru.beeline.capability.dto.PutEnumCriteriaDTO;
import ru.beeline.capability.exception.ForbiddenException;
import ru.beeline.capability.repository.CriteriaRepository;

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

    @Autowired
    private CriteriaRepository criteriaRepository;

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
            if (dto.getType() != null) {
                entity.setType(dto.getType());
            }
            if (dto.getThreshold() != null) {
                entity.setThreshold(dto.getThreshold());
            }
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

    private static void assertAdministrator(HttpServletRequest request) {
        List<String> roles = parseRolesHeader(request.getHeader(USER_ROLES_HEADER));
        if (!roles.contains("ADMINISTRATOR")) {
            throw new ForbiddenException("Доступ запрещен");
        }
    }

    private static List<String> parseRolesHeader(String value) {
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
}
