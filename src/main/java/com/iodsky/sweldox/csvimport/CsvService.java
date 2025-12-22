package com.iodsky.sweldox.csvimport;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.HeaderColumnNameMappingStrategy;
import lombok.RequiredArgsConstructor;

import java.io.*;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class CsvService<T, K> {

    private final CsvMapper<T, K> csvMapper;

    final public LinkedHashSet<CsvResult<T, K>> parseCsv(InputStream stream, Class<K> type) throws IOException {
        try (Reader reader = new BufferedReader(new InputStreamReader(stream))) {
            HeaderColumnNameMappingStrategy<K> strategy = new HeaderColumnNameMappingStrategy<>();
            strategy.setType(type);

            CsvToBean<K> csvToBean = new CsvToBeanBuilder<K>(reader)
                    .withMappingStrategy(strategy)
                    .withIgnoreEmptyLine(true)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build();

            return csvToBean.parse()
                    .stream()
                    .map(csv -> new CsvResult<>(csvMapper.toEntity(csv), csv))
                    .collect(Collectors.toCollection(LinkedHashSet::new));

        }
    }

}
