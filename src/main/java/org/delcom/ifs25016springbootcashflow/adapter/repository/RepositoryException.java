package org.delcom.ifs25016springbootcashflow.adapter.repository;

/** Kegagalan teknis penyimpanan (mis. SQLException) yang dibungkus agar tidak bocor ke domain. */
public class RepositoryException extends RuntimeException {
    public RepositoryException(String message, Throwable cause) {
        super(message, cause);
    }
}
