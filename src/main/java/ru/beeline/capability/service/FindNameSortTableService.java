package ru.beeline.capability.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.beeline.capability.domain.EntityType;
import ru.beeline.capability.domain.FindNameSortTable;
import ru.beeline.capability.repository.EntityTypeRepository;
import ru.beeline.capability.repository.FindNameSortTableRepository;

@Service
@Transactional
public class FindNameSortTableService {

    @Autowired
    private FindNameSortTableRepository findNameSortTableRepository;

    @Autowired
    private EntityTypeRepository entityTypeRepository;

    public void updateVector(Long id, String name, String description, String code, String enType) {
        if (description == null) description = name;
        String vector = "";
        vector = concatStr(name, vector);
        vector = concatStr(description, vector);
        vector = concatStr(code, vector);

        EntityType entityType = entityTypeRepository.findByName(enType);
        FindNameSortTable findNameSortTableItem = findNameSortTableRepository.findByRefIdAndType(id, entityType);
        if (findNameSortTableItem == null) {
            findNameSortTableItem = FindNameSortTable.builder()
                    .vector(vector)
                    .type(entityType)
                    .refId(id)
                    .build();
        } else {
            findNameSortTableItem.setVector(vector);
        }
        findNameSortTableRepository.save(findNameSortTableItem);
    }

    private static String concatStr(String field, String vector) {
        if (field != null && !field.isBlank()) {
            if (!vector.isBlank()) {
                vector = vector.concat("!!!" + field);
            }
            vector = vector.concat(field);
        }
        return vector;
    }
}
