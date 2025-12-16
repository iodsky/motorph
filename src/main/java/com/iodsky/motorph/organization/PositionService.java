package com.iodsky.motorph.organization;

import com.iodsky.motorph.common.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PositionService {

    private final PositionRepository positionRepository;

    public Position getPositionById(String id) {
        return positionRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Position " + id + " not found"));
    }

    public Map<String, Position> getPositionsByTitles(Set<String> titles) {
        return positionRepository.findByTitleIn(titles)
                .stream()
                .collect(Collectors.toMap(Position::getTitle, p -> p));
    }

}
