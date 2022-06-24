package com.drallinger.sqlite.querybuilders;

public class ColumnBuilder extends QueryBuilder {
    public static final String DATA_TYPE_NULL = "NULL";
    public static final String DATA_TYPE_INTEGER = "INTEGER";
    public static final String DATA_TYPE_REAL = "REAL";
    public static final String DATA_TYPE_TEXT = "TEXT";
    public static final String DATA_TYPE_BLOB = "BLOB";
    private String name;
    private String dataType;
    private String defaultValue;
    private boolean notNull;
    private boolean primaryKey;

    private ColumnBuilder(){}

    public static ColumnBuilder createBuilder(){
        return new ColumnBuilder();
    }

    public ColumnBuilder setName(String name){
        this.name = name;
        return this;
    }

    public ColumnBuilder setDataType(String dataType){
        this.dataType = dataType;
        return this;
    }

    public ColumnBuilder setDefaultValue(short defaultValue){
        this.defaultValue = String.valueOf(defaultValue);
        return this;
    }

    public ColumnBuilder setDefaultValue(int defaultValue){
        this.defaultValue = String.valueOf(defaultValue);
        return this;
    }

    public ColumnBuilder setDefaultValue(long defaultValue){
        this.defaultValue = String.valueOf(defaultValue);
        return this;
    }

    public ColumnBuilder setDefaultValue(float defaultValue){
        this.defaultValue = String.valueOf(defaultValue);
        return this;
    }

    public ColumnBuilder setDefaultValue(double defaultValue){
        this.defaultValue = String.valueOf(defaultValue);
        return this;
    }

    public ColumnBuilder setDefaultValue(String defaultValue){
        this.defaultValue = defaultValue;
        return this;
    }

    public ColumnBuilder isNotNull(boolean notNull){
        this.notNull = notNull;
        return this;
    }

    public ColumnBuilder isNotNull(){
        return isNotNull(true);
    }

    public ColumnBuilder isPrimaryKey(boolean primaryKey){
        this.primaryKey = primaryKey;
        return this;
    }

    public ColumnBuilder isPrimaryKey(){
        return isPrimaryKey(true);
    }

    @Override
    public String build() throws InvalidSQLQueryException{
        if(name == null || name.isEmpty()){
            throw new InvalidSQLQueryException("Missing column name");
        }
        if(dataType == null || dataType.isEmpty()){
            throw new InvalidSQLQueryException("Missing data type");
        }
        StringBuilder column = new StringBuilder(name);
        column.append(" ").append(dataType).append(" ");
        if(defaultValue != null && !defaultValue.isEmpty()){
            column.append("DEFAULT ");
            if(dataType.equals(DATA_TYPE_TEXT) || dataType.equals(DATA_TYPE_BLOB)){
                column.append("\"").append(defaultValue).append("\" ");
            }else{
                column.append(defaultValue).append(" ");
            }
        }
        if(notNull){
            column.append("NOT NULL ");
        }
        if(primaryKey){
            column.append("PRIMARY KEY ");
        }
        column.setCharAt(column.length() - 1, ',');
        return column.toString();
    }

    @Override
    public ColumnBuilder clone(){
        return ColumnBuilder.createBuilder()
            .setName(name)
            .setDataType(dataType)
            .setDefaultValue(defaultValue)
            .isNotNull(notNull)
            .isPrimaryKey(primaryKey);
    }
}
