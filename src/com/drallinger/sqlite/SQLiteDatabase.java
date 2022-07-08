package com.drallinger.sqlite;

import com.drallinger.sqlite.querybuilders.InvalidSQLQueryException;
import com.drallinger.sqlite.querybuilders.QueryBuilder;

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Optional;

public class SQLiteDatabase {
    private final String fileName;
    private final HashMap<String, SavedQuery> savedQueries;
    private final HashMap<String, PreparedStatement> preparedStatements;
    private Connection connection;

    public SQLiteDatabase(String fileName){
        this.fileName = fileName;
        savedQueries = new HashMap<>();
        preparedStatements = new HashMap<>();
        connection = null;
    }

    public SQLiteDatabase(){
        this(":memory:");
    }

    public void openConnection(){
        try{
            connection = DriverManager.getConnection("jdbc:sqlite:" + fileName);
        }catch(SQLException e){
            System.err.println("Failed to connect to database: " + e.getMessage());
            System.exit(0);
        }
    }

    public void closeConnection(){
        preparedStatements.clear();
        try{
            connection.close();
        }catch(SQLException e){
            System.err.println("Failed to disconnect from database: " + e.getMessage());
            System.exit(0);
        }
    }

    public boolean isConnectionOpen(){
        try{
            return connection != null && connection.isValid(0);
        }catch(SQLException e){
            handleError("Failed to validate database connection: " + e.getMessage());
        }
        return false;
    }

    public void setAutoCommit(boolean autoCommit){
        try{
            connection.setAutoCommit(autoCommit);
        }catch(SQLException e){
            handleError("Failed to set auto commit for database connection: " + e.getMessage());
        }
    }

    public boolean isAutoCommitEnabled(){
        try{
            return connection.getAutoCommit();
        }catch(SQLException e){
            System.err.println("Failed to check auto commit status for database connection: " + e.getMessage());
            closeConnection();
            System.exit(0);
        }
        return false;
    }

    public void commit(){
        try{
            connection.commit();
        }catch(SQLException e){
            handleError("Failed to commit to database: " + e.getMessage());
        }
    }

    public void rollback(){
        try{
            connection.rollback();
        }catch(SQLException e){
            System.err.println("Failed to rollback database: " + e.getMessage());
            closeConnection();
            System.exit(0);
        }
    }

    public void saveQuery(SavedQuery.Builder builder){
        try{
            SavedQuery query = builder.build();
            savedQueries.put(query.getName(), query);
        }catch(IllegalArgumentException|InvalidSQLQueryException e){
            handleError("Failed to save query: " + e.getMessage());
        }
    }

    public void prepareQueries(String... queryNames){
        try{
            for(String queryName : queryNames){
                if(!savedQueries.containsKey(queryName)){
                    throw new IllegalArgumentException("Query " + queryName + " has not been saved");
                }
                SavedQuery savedQuery = savedQueries.get(queryName);
                PreparedStatement statement;
                if(savedQuery.returnCreatedIDs()){
                    statement = connection.prepareStatement(savedQuery.getQuery(), Statement.RETURN_GENERATED_KEYS);
                }else{
                    statement = connection.prepareStatement(savedQuery.getQuery());
                }
                preparedStatements.put(queryName, statement);
            }
        }catch(SQLException|IllegalArgumentException e){
            handleError("Failed to prepare queries: " + e.getMessage());
        }
    }

    public ResultSet executeQuery(String query){
        try{
            Statement statement = connection.createStatement();
            return statement.executeQuery(query);
        }catch(SQLException e){
            handleError("Failed to execute query: " + e.getMessage());
        }
        return null;
    }

    public ResultSet executeQuery(QueryBuilder builder){
        return executeQuery(buildQuery(builder));
    }

    public void executeUpdate(String query){
        try{
            Statement statement = connection.createStatement();
            statement.executeUpdate(query);
        }catch(SQLException e){
            handleError("Failed to execute update: " + e.getMessage());
        }
    }

    public void executeUpdate(QueryBuilder builder){
        executeUpdate(buildQuery(builder));
    }

    public ResultSet executeUpdateAndGetIDs(String query){
        try{
            Statement statement = connection.createStatement();
            statement.executeUpdate(query);
            return statement.getGeneratedKeys();
        }catch(SQLException e){
            handleError("Failed to execute update: " + e.getMessage());
        }
        return null;
    }

    public ResultSet executeUpdateAndGetIDs(QueryBuilder builder){
        return executeUpdateAndGetIDs(buildQuery(builder));
    }

    public int executeUpdateAndGetIntID(String query){
        try{
            Statement statement = connection.createStatement();
            statement.executeUpdate(query);
            ResultSet resultSet = statement.getGeneratedKeys();
            if(resultSet.next()){
                return resultSet.getInt(1);
            }
        }catch(SQLException e){
            handleError("Failed to execute update: " + e.getMessage());
        }
        return -1;
    }

    public int executeUpdateAndGetIntID(QueryBuilder builder){
        return executeUpdateAndGetIntID(buildQuery(builder));
    }

    public String executeUpdateAndGetStringID(String query){
        try{
            Statement statement = connection.createStatement();
            statement.executeUpdate(query);
            ResultSet resultSet = statement.getGeneratedKeys();
            if(resultSet.next()){
                return resultSet.getString(1);
            }
        }catch(SQLException e){
            handleError("Failed to execute update: " + e.getMessage());
        }
        return null;
    }

    public String executeUpdateAndGetStringID(QueryBuilder builder){
        return executeUpdateAndGetStringID(buildQuery(builder));
    }

    public QueryResult<?> executeSavedQuery(String queryName, QueryValue<?>... values){
        try{
            if(!savedQueries.containsKey(queryName)){
                throw new IllegalArgumentException("Query " + queryName + " has not been saved");
            }
            if(!preparedStatements.containsKey(queryName)){
                throw new IllegalArgumentException("Query " + queryName + " has not been prepared");
            }
            SavedQuery savedQuery = savedQueries.get(queryName);
            if(!savedQuery.hasHandler()){
                throw new IllegalArgumentException("Query " + queryName + " is missing a handler");
            }
            PreparedStatement statement = preparedStatements.get(queryName);
            addValuesToStatement(statement, values);
            ResultSet resultSet = statement.executeQuery();
            QueryResult.Builder<?> builder = savedQuery.getHandler().handleResultSet(resultSet);
            return builder.build();
        }catch(SQLException|IllegalArgumentException e){
            handleError("Failed to execute saved query: " + e.getMessage());
        }
        return QueryResult.empty();
    }

    public void executeSavedUpdate(String queryName, QueryValue<?>... values){
        try{
            if(!preparedStatements.containsKey(queryName)){
                throw new IllegalArgumentException("Query " + queryName + " has not been prepared");
            }
            PreparedStatement statement = preparedStatements.get(queryName);
            addValuesToStatement(statement, values);
            statement.executeUpdate();
        }catch(SQLException|IllegalArgumentException e){
            handleError("Failed to execute saved update: " + e.getMessage());
        }
    }

    public QueryResult<?> executeSavedUpdateAndGetIDs(String queryName, QueryValue<?>... values){
        try{
            if(!savedQueries.containsKey(queryName)){
                throw new IllegalArgumentException("Query " + queryName + " has not been saved");
            }
            if(!preparedStatements.containsKey(queryName)){
                throw new IllegalArgumentException("Query " + queryName + " has not been prepared");
            }
            SavedQuery savedQuery = savedQueries.get(queryName);
            if(!savedQuery.returnCreatedIDs()){
                throw new IllegalArgumentException("Query " + queryName + " cannot return created IDs");
            }
            if(!savedQuery.hasHandler()){
                throw new IllegalArgumentException("Query " + queryName + " is missing a handler");
            }
            PreparedStatement statement = preparedStatements.get(queryName);
            addValuesToStatement(statement, values);
            statement.executeUpdate();
            ResultSet resultSet = statement.getGeneratedKeys();
            QueryResult.Builder<?> builder = savedQuery.getHandler().handleResultSet(resultSet);
            return builder.build();
        }catch(SQLException|IllegalArgumentException e){
            handleError("Failed to execute saved update: " + e.getMessage());
        }
        return QueryResult.empty();
    }

    public int executeSavedUpdateAndGetIntID(String queryName, QueryValue<?>... values){
        try{
            if(!savedQueries.containsKey(queryName)){
                throw new IllegalArgumentException("Query " + queryName + " has not been saved");
            }
            if(!preparedStatements.containsKey(queryName)){
                throw new IllegalArgumentException("Query " + queryName + " has not been prepared");
            }
            SavedQuery savedQuery = savedQueries.get(queryName);
            if(!savedQuery.returnCreatedIDs()){
                throw new IllegalArgumentException("Query " + queryName + " cannot return created IDs");
            }
            PreparedStatement statement = preparedStatements.get(queryName);
            addValuesToStatement(statement, values);
            statement.executeUpdate();
            ResultSet resultSet = statement.getGeneratedKeys();
            if(resultSet.next()){
                return resultSet.getInt(1);
            }
        }catch(SQLException|IllegalArgumentException e){
            handleError("Failed to execute saved update: " + e.getMessage());
        }
        return -1;
    }

    public String executeSavedUpdateAndGetStringID(String queryName, QueryValue<?>... values){
        try{
            if(!savedQueries.containsKey(queryName)){
                throw new IllegalArgumentException("Query " + queryName + " has not been saved");
            }
            if(!preparedStatements.containsKey(queryName)){
                throw new IllegalArgumentException("Query " + queryName + " has not been prepared");
            }
            SavedQuery savedQuery = savedQueries.get(queryName);
            if(!savedQuery.returnCreatedIDs()){
                throw new IllegalArgumentException("Query " + queryName + " cannot return created IDs");
            }
            PreparedStatement statement = preparedStatements.get(queryName);
            addValuesToStatement(statement, values);
            statement.executeUpdate();
            ResultSet resultSet = statement.getGeneratedKeys();
            if(resultSet.next()){
                return resultSet.getString(1);
            }
        }catch(SQLException|IllegalArgumentException e){
            handleError("Failed to execute saved update: " + e.getMessage());
        }
        return null;
    }

    private String buildQuery(QueryBuilder builder){
        try{
            return builder.build();
        }catch(InvalidSQLQueryException e){
            handleError("Failed to build query: " + e.getMessage());
        }
        return null;
    }

    private void addValuesToStatement(PreparedStatement statement, QueryValue<?>... values) throws SQLException{
        for(int i = 0; i < values.length; i++){
            QueryValue<?> value = values[i];
            switch(value.getType()){
                case INTEGER -> statement.setInt((i + 1), (Integer) value.getValue());
                case REAL -> statement.setDouble((i + 1), (Double) value.getValue());
                case TEXT -> statement.setString((i + 1), (String) value.getValue());
            }
        }
    }

    private void handleError(String message){
        System.err.println("SQLite ERROR: " + message);
        if(isAutoCommitEnabled()){
            rollback();
        }
        closeConnection();
        System.exit(0);
    }
}
