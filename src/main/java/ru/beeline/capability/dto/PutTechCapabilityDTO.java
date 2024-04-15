package ru.beeline.capability.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.beeline.capability.domain.TechCapability;
import ru.beeline.capability.domain.TechCapabilityRelations;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PutTechCapabilityDTO {
    private String code;
    private String name;
    private String description;
    private String author;
    private String link;
    private String owner;
    private String status;
    private List<String> parents;


    public boolean equals(TechCapability o) {
        return Objects.equals(code, o.getCode())
                && Objects.equals(name,o.getName())
                && Objects.equals(description,o.getStatus())
                && Objects.equals(author,o.getAuthor())
                && Objects.equals(link,o.getLink())
                && Objects.equals(owner,o.getOwner())
                && Objects.equals(status,o.getName())
                && checkEquals(parents, o.getParents());
    }

    private boolean checkEquals(List<String> parents, List<TechCapabilityRelations> parents1) {
        return parents1.stream().map(relations -> relations.getBusinessCapability().getId().toString()).collect(Collectors.toList()).equals(parents);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, name, description, status, author, link, owner, parents);
    }
}