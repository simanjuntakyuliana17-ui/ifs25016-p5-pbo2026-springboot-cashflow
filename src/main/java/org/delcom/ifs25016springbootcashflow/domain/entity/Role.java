package org.delcom.ifs25016springbootcashflow.domain.entity;

/** Peran pengguna. ADMIN dapat meninjau data pengguna lain; USER hanya data sendiri. */
public enum Role {
    ADMIN("Admin"),
    USER("Pengguna");

    private final String label;

    Role(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
