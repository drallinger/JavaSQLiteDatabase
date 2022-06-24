package com.drallinger.sqlite.querybuilders;

import java.sql.SQLException;

public class InvalidSQLQueryException extends SQLException {
    public InvalidSQLQueryException(String message){
        super(message);
    }
}
