package com.drallinger.sqlite.querybuilders;

import java.util.ArrayList;

public class SelectBuilder extends QueryBuilder {
    public static final String JOIN_TYPE_INNER = "INNER";
    public static final String JOIN_TYPE_LEFT_OUTER = "LEFT OUTER";
    public static final String JOIN_TYPE_CROSS = "CROSS";
    private final ArrayList<String> joins;
    private String[] columns;
    private String tableName;
    private String where;
    private String orderBy;
    private int limit;
    private boolean distinct = false;

    private SelectBuilder(){
        joins = new ArrayList<>();
    }

    public static SelectBuilder createBuilder(){
        return new SelectBuilder();
    }

    public SelectBuilder setColumn(String column){
        if(column != null && !column.isEmpty()){
            this.columns = new String[]{column};
        }
        return this;
    }

    public SelectBuilder setColumns(String... columns){
        this.columns = columns;
        return this;
    }

    public SelectBuilder setTableName(String tableName){
        this.tableName = tableName;
        return this;
    }

    public SelectBuilder addJoin(String joinType, String tableName, String column1, String column2){
        joins.add(joinType + " JOIN " + tableName + " ON " + column1 + " = " + column2);
        return this;
    }

    public SelectBuilder addJoin(String join){
        joins.add(join);
        return this;
    }

    public SelectBuilder setWhere(String where){
        this.where = where;
        return this;
    }

    public SelectBuilder setOrderBy(String orderBy){
        this.orderBy = orderBy;
        return this;
    }

    public SelectBuilder setLimit(int limit){
        this.limit = limit;
        return this;
    }

    public SelectBuilder isDistinct(boolean distinct){
        this.distinct = distinct;
        return this;
    }

    public SelectBuilder isDistinct(){
        return isDistinct(true);
    }

    @Override
    public String build() throws InvalidSQLQueryException{
        if(columns == null || columns.length <= 0){
            throw new InvalidSQLQueryException("No columns given");
        }
        if(tableName == null || tableName.isEmpty()){
            throw new InvalidSQLQueryException("Missing table name");
        }
        StringBuilder query = new StringBuilder("SELECT ");
        if(distinct){
            query.append("DISTINCT ");
        }
        query.append(String.join(",", columns)).append(" FROM ").append(tableName).append(" ");
        if(joins.size() > 0){
            for(String join : joins){
                query.append(join).append(" ");
            }
        }
        if(where != null && !where.isEmpty()){
            query.append("WHERE ").append(where).append(" ");
        }
        if(orderBy != null && !orderBy.isEmpty()){
            query.append("ORDER BY ").append(orderBy).append(" ");
        }
        if(limit > 0){
            query.append("LIMIT ").append(limit).append(" ");
        }
        query.setCharAt(query.length() - 1, ';');
        return query.toString();
    }

    @Override
    public SelectBuilder clone(){
        SelectBuilder builder = SelectBuilder.createBuilder()
            .setColumns(columns)
            .setTableName(tableName)
            .setWhere(where)
            .setOrderBy(orderBy)
            .setLimit(limit)
            .isDistinct(distinct);
        if(!joins.isEmpty()){
            for(String join : joins){
                builder.addJoin(join);
            }
        }
        return builder;
    }
}
