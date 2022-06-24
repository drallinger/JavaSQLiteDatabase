package com.drallinger.sqlite;

import java.util.ArrayList;

public class QueryResult<T> {
    private final ArrayList<T> values;
    private T value;

    public QueryResult(){
        values = new ArrayList<>();
    }

    public QueryResult(T value){
        this();
        this.value = value;
    }

    public boolean isEmpty(){
        return (value == null && values.isEmpty());
    }

    public void setValue(T value){
        this.value = value;
    }

    public T getValue(){
        return value;
    }

    public void addValue(T value){
        values.add(value);
    }

    public ArrayList<T> getValues(){
        return new ArrayList<>(values);
    }
}
