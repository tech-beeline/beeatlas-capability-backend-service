/*
 * Copyright (c) 2024 PJSC VimpelCom
 */

package ru.beeline.capability.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.beeline.capability.domain.FuzzySearchCapabilityResult;
import ru.beeline.capability.dto.SearchCapabilityDTO;
import ru.beeline.capability.exception.TooManyResultsException;
import ru.beeline.capability.repository.FindNameSortTableRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class SearchCapabilityService {

    @Autowired
    private FindNameSortTableRepository findNameSortTableRepository;

    public List<SearchCapabilityDTO> searchCapability(String search, String findBy) {
        String typeString = (findBy != null && !findBy.equalsIgnoreCase("all")) ? findBy : null;
        List<Object> functionResult = findNameSortTableRepository.callFuzzySearchCapability(search, typeString);
        if (functionResult.size() == 100) {
            throw new TooManyResultsException("Too many results");
        }
        List<FuzzySearchCapabilityResult> searchResult = functionResult.stream().map(this::getRowData).collect(Collectors.toList());
        List<SearchCapabilityDTO> result = new ArrayList<>();
        searchResult.forEach(row -> {
            String[] splitRow = row.getVector().split("<!!!>");
            result.add(SearchCapabilityDTO.builder()
                    .name(splitRow[0])
                    .description(splitRow[1])
                    .code(splitRow[2])
                    .type(row.getEntityName())
                    .id(row.getId())
                    .build());
        });
        return result;
    }

    private FuzzySearchCapabilityResult getRowData(Object object) {
        Object[] row = (Object[]) object;
        return new FuzzySearchCapabilityResult((Integer) row[0], row[1].toString(), row[2].toString());
    }
}
