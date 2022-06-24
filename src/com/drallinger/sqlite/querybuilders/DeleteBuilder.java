package com.drallinger.sqlite.querybuilders;

public class DeleteBuilder extends QueryBuilder {
    private String tableName;
    private String where;
    private int limit;

    private DeleteBuilder(){}

    public static DeleteBuilder createBuilder(){
        return new DeleteBuilder();
    }

    public DeleteBuilder setTableName(String tableName){
        this.tableName = tableName;
        return this;
    }

    public DeleteBuilder setWhere(String where){
        this.where = where;
        return this;
    }

    public DeleteBuilder setLimit(int limit){
        this.limit = limit;
        return this;
    }

    @Override
    public String build() throws InvalidSQLQueryException{
        if(tableName == null || tableName.isEmpty()){
            throw new InvalidSQLQueryException("Missing table name");
        }
        StringBuilder query = new StringBuilder("DELETE FROM ");
        query.append(tableName).append(" ");
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
    public DeleteBuilder clone(){
        return DeleteBuilder.createBuilder()
            .setTableName(tableName)
            .setWhere(where)
            .setLimit(limit);
    }
}
