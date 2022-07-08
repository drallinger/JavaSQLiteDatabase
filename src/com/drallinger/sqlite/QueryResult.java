package com.drallinger.sqlite;

import java.util.ArrayList;

public class QueryResult<T> {
    private final T value;
    private final ArrayList<T> values;

    private QueryResult(Builder<T> builder){
        value = builder.value;
        values = new ArrayList<>(builder.values);
    }

    public static QueryResult<?> empty(){
        return (new QueryResult.Builder<>().build());
    }

    public static class Builder<T>{
        private final ArrayList<T> values = new ArrayList<>();
        private T value;

        public Builder setValue(T value){
            this.value = value;
            return this;
        }

        public Builder addValue(T value){
            values.add(value);
            return this;
        }

        public QueryResult<T> build(){
            return new QueryResult<>(this);
        }
    }

    public boolean isEmpty(){
        return (value == null && values.isEmpty());
    }

    public T getValue(){
        return value;
    }

    public ArrayList<T> getValues(){
        return new ArrayList<>(values);
    }
}
