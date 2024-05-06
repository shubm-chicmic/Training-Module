package com.chicmic.trainingModule.Entity.Filters;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Filters {
    Set<String> teamsFilter = new HashSet<>();
}
