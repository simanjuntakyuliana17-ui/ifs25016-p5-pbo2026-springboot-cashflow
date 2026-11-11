package org.delcom.ifs25016springbootcashflow.domain.exception;

/** Entitas dengan ID tertentu tidak ditemukan. */
public class EntityNotFoundException extends DomainException {
    private final int id;

    public EntityNotFoundException(String entity, int id) {
        super(entity + " dengan ID " + id + " tidak ditemukan.");
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
