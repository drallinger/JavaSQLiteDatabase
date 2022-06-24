package com.drallinger.sqlite.querybuilders;

public class DropTableBuilder extends QueryBuilder {
    private String tableName;
    private boolean ifExists;

    private DropTableBuilder(){}

    public static DropTableBuilder createBuilder(){
        return new DropTableBuilder();
    }

    public DropTableBuilder setTableName(String tableName){
        this.tableName = tableName;
        return this;
    }

    public DropTableBuilder ifExists(boolean ifExists){
        this.ifExists = ifExists;
        return this;
    }

    public DropTableBuilder ifExists(){
        return ifExists(true);
    }

    @Override
    public String build() throws InvalidSQLQueryException{
        if(tableName == null || tableName.isEmpty()){
            throw new InvalidSQLQueryException("Missing table name");
        }
        StringBuilder query = new StringBuilder("DROP TABLE ");
        if(ifExists){
            query.append("IF EXISTS ");
        }
        query.append(tableName).append(";");
        return query.toString();
    }

    @Override
    public DropTableBuilder clone(){
        return DropTableBuilder.createBuilder()
            .setTableName(tableName)
            .ifExists(ifExists);
    }
}
