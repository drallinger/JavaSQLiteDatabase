package com.drallinger.sqlite.querybuilders;

import java.util.ArrayList;

public class CreateTableBuilder extends QueryBuilder {
    private final ArrayList<ColumnBuilder> columns;
    private String tableName;
    private boolean ifNotExists;

    private CreateTableBuilder(){
        columns = new ArrayList<>();
    }

    public static CreateTableBuilder createBuilder(){
        return new CreateTableBuilder();
    }

    public CreateTableBuilder setTableName(String tableName){
        this.tableName = tableName;
        return this;
    }

    public CreateTableBuilder ifNotExists(boolean ifNotExists){
        this.ifNotExists = ifNotExists;
        return this;
    }

    public CreateTableBuilder ifNotExists(){
        return ifNotExists(true);
    }

    public CreateTableBuilder addColumn(ColumnBuilder columnBuilder){
        columns.add(columnBuilder);
        return this;
    }

    @Override
    public String build() throws InvalidSQLQueryException{
        if(tableName == null || tableName.isEmpty()){
            throw new InvalidSQLQueryException("Missing table name");
        }
        if(columns.isEmpty()){
            throw new InvalidSQLQueryException("No columns given");
        }
        StringBuilder query = new StringBuilder("CREATE TABLE ");
        if(ifNotExists){
            query.append("IF NOT EXISTS ");
        }
        query.append(tableName).append(" (");
        for(ColumnBuilder columnBuilder : columns){
            query.append(columnBuilder.build());
        }
        query.deleteCharAt(query.length() - 1);
        query.append(");");
        return query.toString();
    }

    @Override
    public CreateTableBuilder clone(){
        CreateTableBuilder builder = CreateTableBuilder.createBuilder()
            .setTableName(tableName)
            .ifNotExists(ifNotExists);
        if(!columns.isEmpty()){
            for(ColumnBuilder columnBuilder : columns){
                builder.addColumn(columnBuilder);
            }
        }
        return builder;
    }
}
