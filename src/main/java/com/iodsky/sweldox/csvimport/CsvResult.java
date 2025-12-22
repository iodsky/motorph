package com.iodsky.sweldox.csvimport;

public record CsvResult<T, K>(T entity, K source) {}