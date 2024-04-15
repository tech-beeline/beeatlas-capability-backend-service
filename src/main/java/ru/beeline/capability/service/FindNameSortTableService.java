package ru.beeline.capability.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.beeline.capability.domain.EntityType;
import ru.beeline.capability.domain.FindNameSortTable;
import ru.beeline.capability.repository.EntityTypeRepository;
import ru.beeline.capability.repository.FindNameSortTableRepository;

import static ru.beeline.capability.utils.Constants.ENTITY_TYPE_TECH_CAPABILITY;

@Service
@Transactional
public class FindNameSortTableService {

    @Autowired
    private FindNameSortTableRepository findNameSortTableRepository;

    @Autowired
    private EntityTypeRepository entityTypeRepository;

    public void updateVector(Long id, String name, String description, String code) {
        if(description == null) description = name;
        String vector = String.join("<!!!>", new String[] {
                name,
                description,
                code
        });
        EntityType entityType = entityTypeRepository.findByName(ENTITY_TYPE_TECH_CAPABILITY);
        FindNameSortTable findNameSortTableItem = findNameSortTableRepository.findByRefIdAndType(id, entityType);
        if(findNameSortTableItem == null) {
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
}
