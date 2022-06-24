package com.drallinger.sqlite;

import com.drallinger.sqlite.querybuilders.QueryBuilder;
import com.drallinger.sqlite.querybuilders.InvalidSQLQueryException;

public class SavedQuery {
    private final String name;
    private final String query;
    private final ResultSetHandler handler;

    private final boolean returnCreatedIDs;

    public SavedQuery(String name, QueryBuilder builder, ResultSetHandler handler, boolean returnCreatedIDs) throws InvalidSQLQueryException{
        this.name= name;
        query = builder.build();
        this.handler = handler;
        this.returnCreatedIDs = returnCreatedIDs;
    }

    public SavedQuery(String name, QueryBuilder builder, boolean returnCreatedIDs) throws InvalidSQLQueryException{
        this(name, builder, null, returnCreatedIDs);
    }

    public SavedQuery(String name, QueryBuilder builder, ResultSetHandler handler) throws InvalidSQLQueryException{
        this(name, builder, handler, false);
    }

    public SavedQuery(String name, QueryBuilder builder) throws InvalidSQLQueryException{
        this(name, builder, null, false);
    }

    public String getName(){
        return name;
    }

    public String getQuery(){
        return query;
    }

    public boolean hasHandler(){
        return handler != null;
    }

    public ResultSetHandler getHandler(){
        return handler;
    }

    public boolean returnCreatedIDs(){
        return returnCreatedIDs;
    }
}
