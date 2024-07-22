package ru.beeline.capability.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.beeline.capability.domain.FuzzySearchCapabilityResult;
import ru.beeline.capability.dto.SearchCapabilityDTO;
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
        List<Object> functionResult = findNameSortTableRepository.callFuzzySearchCapability(search);
        List<FuzzySearchCapabilityResult> searchResult = functionResult.stream().map(this::getRowData).collect(Collectors.toList());
        if (findBy != null && !findBy.equalsIgnoreCase("all")) {
            searchResult = searchResult.stream().filter(row -> row.getEntityName().equals(findBy)).collect(Collectors.toList());
        }
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
