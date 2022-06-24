package com.drallinger.sqlite;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface ResultSetHandler {
    QueryResult<?> handleResultSet(ResultSet resultSet) throws SQLException;
}
