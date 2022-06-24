package com.drallinger.sqlite.querybuilders;

public abstract class QueryBuilder {
    public abstract String build() throws InvalidSQLQueryException;
    public abstract QueryBuilder clone();
}
