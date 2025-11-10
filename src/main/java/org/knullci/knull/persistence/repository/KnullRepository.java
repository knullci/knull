package org.knullci.knull.persistence.repository;

import java.util.List;

public interface KnullRepository<T> {
    void save(String fileName, T object);
    T getByFileName(String fileName);
    List<T> getAll();
    void deleteByFileName(String fileName);
    Long getNextFileId();
}
