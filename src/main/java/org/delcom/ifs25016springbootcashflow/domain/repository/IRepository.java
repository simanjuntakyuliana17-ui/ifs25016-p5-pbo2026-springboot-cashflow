package org.delcom.ifs25016springbootcashflow.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Port generik CRUD.
 *
 * @param <T>  tipe entitas
 * @param <ID> tipe identifier
 */
public interface IRepository<T, ID> {
    List<T> findAll();

    Optional<T> findById(ID id);

    /** Menyimpan entitas baru dan mengembalikan entitas tersimpan (dengan ID final). */
    T save(T entity);

    boolean deleteById(ID id);

    void update(T entity);

    List<T> findBy(Predicate<T> predicate);
}
