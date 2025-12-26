package ru.beeline.capability.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.beeline.capability.domain.Promt;
import ru.beeline.capability.dto.PromtDTO;
import ru.beeline.capability.exception.NotFoundException;
import ru.beeline.capability.repository.PromtRepository;

@Slf4j
@Service
public class PromtService {

    @Autowired
    private PromtRepository promtRepository;

    public PromtDTO getPromtByAlias(String alias) {
        Promt promt = promtRepository.findByAlias(alias);
        if (promt == null){
            throw new NotFoundException("Промт с данным alias не найден.");
        }
        return PromtDTO.builder()
                .id(promt.getId())
                .model(promt.getModel())
                .alias(promt.getAlias())
                .promt(promt.getPromt())
                .build();
    }
}
