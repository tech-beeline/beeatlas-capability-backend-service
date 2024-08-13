package ru.beeline.capability.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.beeline.capability.domain.EnumCriteria;
import ru.beeline.capability.repository.CriteriaRepository;

import java.util.List;

@Service
public class CriteriaService {
    @Autowired
    private CriteriaRepository criteriaRepository;

    public List<EnumCriteria> getCriteria() {
        return criteriaRepository.findAll();
    }
}
