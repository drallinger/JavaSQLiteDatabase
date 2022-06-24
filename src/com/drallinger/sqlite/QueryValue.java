package com.drallinger.sqlite;

public class QueryValue<T> {
    public enum ValueType{
        INTEGER,
        REAL,
        TEXT
    }
    private final T value;
    private final ValueType type;

    private QueryValue(T value, ValueType type){
        this.value = value;
        this.type = type;
    }

    public static QueryValue<Integer> integerValue(int value){
        return new QueryValue<>(value, ValueType.INTEGER);
    }

    public static QueryValue<Double> realValue(double value){
        return new QueryValue<>(value, ValueType.REAL);
    }

    public static QueryValue<String> textValue(String value){
        return new QueryValue<>(value, ValueType.TEXT);
    }

    public T getValue(){
        return value;
    }

    public ValueType getType(){
        return type;
    }
}
