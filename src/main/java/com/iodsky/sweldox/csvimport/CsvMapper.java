package com.iodsky.sweldox.csvimport;

import org.springframework.stereotype.Component;

@Component
public interface CsvMapper<T, K> {

    T toEntity(K k);

}
