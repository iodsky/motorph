package com.iodsky.motorph.common;

import org.springframework.data.domain.Page;

import java.util.function.Function;

public class PageMapper {
    private PageMapper() {}

    public static <T, U>PageDto<U> map(Page<T> page, Function<T, U> mapper) {
        return PageDto.<U>builder()
                .content(page.getContent().stream().map(mapper).toList())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }

}
