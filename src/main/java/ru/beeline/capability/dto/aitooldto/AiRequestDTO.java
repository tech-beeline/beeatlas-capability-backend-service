/*
 * Copyright (c) 2024 PJSC VimpelCom
 */

package ru.beeline.capability.dto.aitooldto;


import lombok.*;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiRequestDTO {

    private List<MessageDTO> messages;
    private String model;
    private Boolean stream;
}
