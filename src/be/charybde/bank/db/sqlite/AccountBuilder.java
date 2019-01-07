package be.charybde.bank.db.sqlite;

import be.charybde.bank.entities.Account;

import java.sql.ResultSet;
import java.sql.SQLException;

public class AccountBuilder {

    public static Account buildAccountFromResultSet(ResultSet resultSet){
        try {
            return new Account(resultSet.getString("name"),
                    null,
                    resultSet.getBoolean("notif"),
                    resultSet.getString("color"),
                    resultSet.getDouble("amount"),
                    resultSet.getString("bank"),
                    false
            );
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
