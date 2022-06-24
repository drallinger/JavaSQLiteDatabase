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

    public void executeUpdate(String query){
        try{
            Statement statement = connection.createStatement();
            statement.executeUpdate(query);
        }catch(SQLException e){
            handleError("Failed to execute update in database: " + e.getMessage());
        }
    }

    public void executeUpdate(QueryBuilder builder){
        try{
            executeUpdate(builder.build());
        }catch(InvalidSQLQueryException e){
            handleError("Invalid SQLite query: " + e.getMessage());
        }
    }

    public ResultSet executeUpdateAndGetIDs(String query){
        try{
            Statement statement = connection.createStatement();
            statement.executeUpdate(query, Statement.RETURN_GENERATED_KEYS);
            return statement.getGeneratedKeys();
        }catch(SQLException e){
            handleError("Failed to execute update in database: " + e.getMessage());
        }
        return null;
    }

    public ResultSet executeUpdateAndGetIDs(QueryBuilder builder){
        try{
            return executeUpdateAndGetIDs(builder.build());
        }catch(InvalidSQLQueryException e){
            handleError("Invalid SQLite query: " + e.getMessage());
        }
        return null;
    }

    public ResultSet executeQuery(String query){
        try{
            Statement statement = connection.createStatement();
            return statement.executeQuery(query);
        }catch(SQLException e){
            handleError("Failed to execute query in database: " + e.getMessage());
        }
        return null;
    }

    public ResultSet executeQuery(QueryBuilder builder){
        try{
            return executeQuery(builder.build());
        }catch(InvalidSQLQueryException e){
            handleError("Invalid SQLite query: " + e.getMessage());
        }
        return null;
    }

    public void saveQuery(String name, QueryBuilder builder, ResultSetHandler handler, boolean returnCreatedIDs){
        try{
            savedQueries.put(name, new SavedQuery(name, builder, handler, returnCreatedIDs));
        }catch(InvalidSQLQueryException e){
            handleError("Invalid SQLite query: " + e.getMessage());
        }
    }

    public void saveQuery(String name, QueryBuilder builder, ResultSetHandler handler){
        try{
            savedQueries.put(name, new SavedQuery(name, builder, handler));
        }catch(InvalidSQLQueryException e){
            handleError("Invalid SQLite query: " + e.getMessage());
        }
    }

    public void saveQuery(String name, QueryBuilder builder, boolean returnCreatedIDs){
        try{
            savedQueries.put(name, new SavedQuery(name, builder, returnCreatedIDs));
        }catch(InvalidSQLQueryException e){
            handleError("Invalid SQLite query: " + e.getMessage());
        }
    }

    public void saveQuery(String name, QueryBuilder builder){
        try{
            savedQueries.put(name, new SavedQuery(name, builder));
        }catch(InvalidSQLQueryException e){
            handleError("Invalid SQLite query: " + e.getMessage());
        }
    }

    public void saveQuery(SavedQuery query){
        savedQueries.put(query.getName(), query);
    }

    public void saveQueries(SavedQuery... queries){
        for(SavedQuery query : queries){
            saveQuery(query);
        }
    }

    public void prepareQueries(String... queryNames){
        try{
            for(String queryName : queryNames){
                if(savedQueries.containsKey(queryName)){
                    SavedQuery query = savedQueries.get(queryName);
                    PreparedStatement statement;
                    if(query.returnCreatedIDs()){
                        statement = connection.prepareStatement(query.getQuery(), Statement.RETURN_GENERATED_KEYS);
                    }else{
                        statement = connection.prepareStatement(query.getQuery());
                    }
                    preparedStatements.put(queryName, statement);
                }else{
                    handleError("Query \"" + queryName + "\" has not been saved");
                }
            }
        }catch(SQLException e){
            handleError("Failed to prepare queries: " + e.getMessage());
        }
    }

    public Optional<ResultSet> executePreparedUpdate(String queryName, QueryValue<?>... values){
        try{
            if(preparedStatements.containsKey(queryName)){
                PreparedStatement statement = preparedStatements.get(queryName);
                addValuesToQuery(statement, values);
                SavedQuery savedQuery = savedQueries.get(queryName);
                statement.executeUpdate();
                if(savedQuery.returnCreatedIDs()){
                    return Optional.of(statement.getGeneratedKeys());
                }
            }else{
                handleError("Query \"" + queryName + "\" has not been prepared");
            }
        }catch(SQLException e){
            handleError("Failed to execute prepared query: " + e);
        }
        return Optional.empty();
    }

    public QueryResult<?> executePreparedQuery(String queryName, QueryValue<?>... values){
        try{
            if(preparedStatements.containsKey(queryName)){
                PreparedStatement statement = preparedStatements.get(queryName);
                addValuesToQuery(statement, values);
                SavedQuery savedQuery = savedQueries.get(queryName);
                ResultSet resultSet = statement.executeQuery();
                if(savedQuery.hasHandler()){
                    return savedQuery.getHandler().handleResultSet(resultSet);
                }else{
                    return new QueryResult<>(resultSet);
                }
            }else{
                handleError("Query \"" + queryName + "\" has not been prepared");
            }
        }catch(SQLException e){
            handleError("Failed to execute prepared query: " + e);
        }
        return null;
    }

    private void addValuesToQuery(PreparedStatement statement, QueryValue<?>[] values) throws SQLException{
        for(int i = 0; i < values.length; i++){
            QueryValue<?> queryValue = values[i];
            int j = i + 1;
            switch(queryValue.getType()){
                case INTEGER:
                    statement.setInt(j, (Integer) queryValue.getValue());
                    break;
                case REAL:
                    statement.setDouble(j, (Double) queryValue.getValue());
                    break;
                case TEXT:
                    statement.setString(j, (String) queryValue.getValue());
                    break;
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
