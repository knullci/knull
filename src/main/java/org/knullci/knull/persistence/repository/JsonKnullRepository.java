package org.knullci.knull.persistence.repository;

import lombok.SneakyThrows;
import tools.jackson.databind.ObjectMapper;

import java.io.*;
import java.util.*;

public class JsonKnullRepository<T> implements KnullRepository<T> {

    private final String directory;
    private final Class<T> type;
    private final ObjectMapper mapper;

    @SneakyThrows
    public JsonKnullRepository(String directory, Class<T> type) {
        this.directory = directory;
        this.type = type;

        new File(directory).mkdirs();

        this.mapper = new ObjectMapper();
    }

    @Override
    @SneakyThrows
    public void save(String fileName, T object) {
        mapper.writeValue(new File(directory + "/" + fileName + ".json"), object);
    }

    @Override
    @SneakyThrows
    public T getByFileName(String fileName) {
        return mapper.readValue(new File(directory + "/" + fileName + ".json"), type);
    }

    @Override
    @SneakyThrows
    public List<T> getAll() {

        List<T> result = new ArrayList<>();

        File folder = new File(directory);
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".json"));

        if (files == null) return result;

        for (File file : files) {
            if (file.isFile()) {
                result.add(this.getByFileName(file.getName()));
            }
        }

        return result;
    }

    @Override
    @SneakyThrows
    public void deleteByFileName(String fileName) {
        File file = new File(directory + "/" + fileName + ".json");
        if (!file.delete()) {
            throw new RuntimeException("Failed to delete file: " + fileName);
        }
    }

    @Override
    public Long getNextFileId() {
        File folder = new File(directory);
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".json"));

        if (files == null) return 1L;

        Optional<Long> lastFile = Arrays.stream(files).map(file -> file.getName().replace(".json", ""))
                .map(Long::parseLong)
                .sorted(Comparator.reverseOrder())
                .limit(1)
                .findFirst();

        return lastFile.map(integer -> integer + 1).orElse(1L);
    }
}
