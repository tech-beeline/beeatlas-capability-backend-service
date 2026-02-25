/*
 * Copyright (c) 2024 PJSC VimpelCom
 */

package ru.beeline.capability.dto.aitooldto;

import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageDTO {

    private String role;
    private String content;
}
