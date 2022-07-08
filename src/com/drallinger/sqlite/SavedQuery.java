package com.drallinger.sqlite;

import com.drallinger.sqlite.querybuilders.QueryBuilder;
import com.drallinger.sqlite.querybuilders.InvalidSQLQueryException;

public class SavedQuery {
    private final String name;
    private final String query;
    private final ResultSetHandler handler;

    private final boolean returnCreatedIDs;

    private SavedQuery(Builder builder){
        name = builder.name;
        query = builder.query;
        handler = builder.handler;
        returnCreatedIDs = builder.returnCreatedIDs;
    }

    public static SavedQuery.Builder createBuilder(){
        return new Builder();
    }

    public static class Builder{
        private String name;
        private String query;
        private QueryBuilder queryBuilder;
        private ResultSetHandler handler;
        private boolean returnCreatedIDs;

        private Builder(){};

        public Builder setName(String name){
            this.name = name;
            return this;
        }

        public Builder setQuery(String query){
            this.query = query;
            return this;
        }

        public Builder setQuery(QueryBuilder queryBuilder){
            this.queryBuilder = queryBuilder;
            return this;
        }

        public Builder setHandler(ResultSetHandler handler){
            this.handler = handler;
            return this;
        }

        public Builder returnCreatedIDs(boolean returnCreatedIDs){
            this.returnCreatedIDs = returnCreatedIDs;
            return this;
        }

        public Builder returnCreatedIDs(){
            return returnCreatedIDs(true);
        }

        public SavedQuery build() throws IllegalArgumentException, InvalidSQLQueryException{
            if(query == null && queryBuilder != null){
                query = queryBuilder.build();
            }
            if(name == null || name.isEmpty()){
                throw new IllegalArgumentException("SavedQuery missing name");
            }
            if(query == null || query.isEmpty()){
                throw new IllegalArgumentException("SavedQuery missing query");
            }
            return new SavedQuery(this);
        }
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
