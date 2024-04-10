package ru.beeline.capability.dto;

import lombok.*;
import ru.beeline.capability.domain.BusinessCapability;

import java.util.Objects;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PutBusinessCapabilityDTO {

    private String code;
    private String name;
    private String description;
    private String status;
    private String author;
    private String link;
    private String owner;
    private String parent;
    private Boolean isDomain;

    public boolean equals(BusinessCapability o) {
        return Objects.equals(code, o.getCode())
                && Objects.equals(name,o.getName())
                && Objects.equals(description,o.getStatus())
                && Objects.equals(status,o.getName())
                && Objects.equals(author,o.getAuthor())
                && Objects.equals(link,o.getLink())
                && Objects.equals(owner,o.getOwner())
                && Objects.equals(parent,o.getParentId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, name, description, status, author, link, owner, parent);
    }
}
