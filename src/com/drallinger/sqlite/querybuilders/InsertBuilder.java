package com.drallinger.sqlite.querybuilders;

import java.util.LinkedHashMap;
import java.util.Set;

public class InsertBuilder extends QueryBuilder {
    private final LinkedHashMap<String, String> valuesMap;
    private String tableName;

    private InsertBuilder(){
        valuesMap = new LinkedHashMap<>();
    }

    public static InsertBuilder createBuilder(){
        return new InsertBuilder();
    }

    public InsertBuilder setTableName(String tableName){
        this.tableName = tableName;
        return this;
    }

    public InsertBuilder addValue(String column, short value){
        valuesMap.put(column, String.valueOf(value));
        return this;
    }

    public InsertBuilder addValue(String column, int value){
        valuesMap.put(column, String.valueOf(value));
        return this;
    }

    public InsertBuilder addValue(String column, long value){
        valuesMap.put(column, String.valueOf(value));
        return this;
    }

    public InsertBuilder addValue(String column, float value){
        valuesMap.put(column, String.valueOf(value));
        return this;
    }

    public InsertBuilder addValue(String column, double value){
        valuesMap.put(column, String.valueOf(value));
        return this;
    }

    public InsertBuilder addValue(String column, String value, boolean includeQuotes){
        if(includeQuotes){
            valuesMap.put(column, "\"" + value + "\"");
        }else{
            valuesMap.put(column, value);
        }
        return this;
    }

    public InsertBuilder addValue(String column, String value){
        return addValue(column, value, true);
    }

    public InsertBuilder addPreparedValue(String column){
        valuesMap.put(column, "?");
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
        String columns = String.join(",", valuesMap.keySet());
        String values = String.join(",", valuesMap.values());
        return "INSERT INTO " + tableName + " (" + columns + ") VALUES (" + values + ");";
    }

    @Override
    public InsertBuilder clone(){
        InsertBuilder builder = InsertBuilder.createBuilder()
            .setTableName(tableName);
        if(!valuesMap.isEmpty()){
            Set<String> keys = valuesMap.keySet();
            for(String key : keys){
                builder.addValue(key, valuesMap.get(key), false);
            }
        }
        return builder;
    }
}
