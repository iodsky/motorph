package com.iodsky.sweldox.organization;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface PositionRepository extends JpaRepository<Position, String> {

    List<Position> findByTitleIn(Collection<String> titles);

}
