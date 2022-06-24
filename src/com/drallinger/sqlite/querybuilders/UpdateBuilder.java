package com.drallinger.sqlite.querybuilders;

import java.util.LinkedHashMap;
import java.util.Set;

public class UpdateBuilder extends QueryBuilder {
    private final LinkedHashMap<String, String> valuesMap;
    private String tableName;
    private String where;
    private int limit;

    private UpdateBuilder(){
        valuesMap = new LinkedHashMap<>();
    }

    public static UpdateBuilder createBuilder(){
        return new UpdateBuilder();
    }

    public UpdateBuilder setTableName(String tableName){
        this.tableName = tableName;
        return this;
    }

    public UpdateBuilder addValue(String column, short value){
        valuesMap.put(column, String.valueOf(value));
        return this;
    }

    public UpdateBuilder addValue(String column, int value){
        valuesMap.put(column, String.valueOf(value));
        return this;
    }

    public UpdateBuilder addValue(String column, long value){
        valuesMap.put(column, String.valueOf(value));
        return this;
    }

    public UpdateBuilder addValue(String column, float value){
        valuesMap.put(column, String.valueOf(value));
        return this;
    }

    public UpdateBuilder addValue(String column, double value){
        valuesMap.put(column, String.valueOf(value));
        return this;
    }

    public UpdateBuilder addValue(String column, String value, boolean includeQuotes){
        if(includeQuotes){
            valuesMap.put(column, "\"" + value + "\"");
        }else{
            valuesMap.put(column, value);
        }
        return this;
    }

    public UpdateBuilder addValue(String column, String value){
        return addValue(column, value, true);
    }

    public UpdateBuilder addPreparedValue(String column){
        valuesMap.put(column, "?");
        return this;
    }

    public UpdateBuilder setWhere(String where){
        this.where = where;
        return this;
    }

    public UpdateBuilder setLimit(int limit){
        this.limit = limit;
        return this;
    }

    @Override
    public String build() throws InvalidSQLQueryException{
        if(tableName == null || tableName.isEmpty()){
            throw new InvalidSQLQueryException("Missing table name");
        }
        if(valuesMap.isEmpty()){
            throw new InvalidSQLQueryException("No values given");
        }
        StringBuilder query = new StringBuilder("UPDATE ");
        query.append(tableName).append(" SET ");
        Set<String> columns = valuesMap.keySet();
        for(String column : columns){
            String value = valuesMap.get(column);
            query.append(column).append(" = ").append(value).append(",");
        }
        query.setCharAt(query.length() - 1, ' ');
        if(where != null && !where.isEmpty()){
            query.append("WHERE ").append(where).append(" ");
        }
        if(limit > 0){
            query.append("LIMIT ").append(limit).append(" ");
        }
        query.setCharAt(query.length() - 1, ';');
        return query.toString();
    }

    @Override
    public UpdateBuilder clone(){
        UpdateBuilder builder = UpdateBuilder.createBuilder()
            .setTableName(tableName)
            .setWhere(where)
            .setLimit(limit);
        if(!valuesMap.isEmpty()){
            Set<String> keys = valuesMap.keySet();
            for(String key : keys){
                builder.addValue(key, valuesMap.get(key), false);
            }
        }
        return builder;
    }
}
