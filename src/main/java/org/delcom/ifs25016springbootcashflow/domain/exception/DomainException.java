package org.delcom.ifs25016springbootcashflow.domain.exception;

/** Checked exception dasar untuk seluruh kegagalan domain. */
public class DomainException extends Exception {
    public DomainException(String message) {
        super(message);
    }
}
