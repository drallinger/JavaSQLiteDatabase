package com.drallinger.sqlite;

import java.sql.ResultSet;
import java.sql.SQLException;

@FunctionalInterface
public interface ResultSetHandler {
    QueryResult.Builder<?> handleResultSet(ResultSet rs) throws SQLException;
}
