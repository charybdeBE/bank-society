package be.charybde.bank.db.sqlite;

import be.charybde.bank.db.SettableCallable;
import be.charybde.bank.entities.Account;
import be.charybde.bank.entities.Bank;
import org.bukkit.plugin.java.JavaPlugin;
import org.sqlite.util.StringUtils;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class SQLite {
    private ConnectionHandler database;

    //TODO move requests into class of its own

    // Account
    //INSERT
    private static final String INSERT_ACCOUNT = "INSERT INTO accounts(name, bank, notif, color, amount) VALUES (?, ?, ?, ?, ?)";
    private static final String INSERT_OWNER = "INSERT INTO accountsOwner(name, owner) VALUES (?, ?)";

    //UPDATE
    private static final String UPDATE_ACCOUNT = "UPDATE accounts " +
            "SET bank=?, notif=?, color=?, amount=? " +
            "WHERE name=?";

    //DELETE
    private static final String DELETE_OWNER = "DELETE FROM accountsOwner WHERE name=? AND owner IN (#)";
    private static final String DELETE_ACCOUNT = "DELETE FROM accounts WHERE name=?; DELETE FROM accountsOwner WHERE name=?";

    //SELECT
    private static final String SELECT_ALL_ACCOUNTS = "SELECT * FROM accounts";
    private static final String SELECT_SPECIFIC_ACCOUNT = "SELECT * FROM accounts WHERE name = ?";
    private static final String SELECT_ALL_ACCOUNTS_FROM = "SELECT * FROM accounts " +
            "INNER JOIN accountsOwner ON accounts.name =  accountsOwner.name " +
            "WHERE accountsOwner.owner=?";
    private static final String SELECT_ALL_OWNERS_FROM = "SELECT owner FROM accountsOwner WHERE name = ?";
    private static final String HAS_ACCESS = "SELECT COUNT(owner) AS access FROM accountsOwner WHERE name=? AND owner=?";

    // Bank

    // INSERT
    private static final String INSERT_BANK = "INSERT INTO bank(name, amount, owner) VALUES (?, 0, ?)";

    // UPDATE
    private static final String UPDATE_BANK = "UPDATE bank " +
            "SET amount=? " +
            "WHERE name=?";

    // SELECT
    private static final String SELECT_BANK_OWNER = "SELECT * from bank WHERE owner=?";
    private static final String SELECT_BANK = "SELECT * from bank WHERE name=?";
    private static final String SELECT_ACCOUNT_BANK = "SELECT * from accounts WHERE bank=?";
    private static final String SELECT_RESERVE = "SELECT SUM(amount) as reserve FROM accounts WHERE bank=?";

    public SQLite(JavaPlugin plugin) {
        this.database = new ConnectionHandler(plugin.getDataFolder() + "/db.db");
    }

    public void writeAccount(Account acc) {
        database.connect();

        //TODO async
        try {
            PreparedStatement pstmt = database.getConnection().prepareStatement(INSERT_ACCOUNT);

            // just setting the class name
            pstmt.setString(1, acc.getName());
            pstmt.setString(2, acc.getBank());
            pstmt.setBoolean(3, acc.getNotif());
            pstmt.setString(4, acc.getColor());
            pstmt.setDouble(5, acc.getBalance());
            pstmt.executeUpdate();

            this.addOwners(acc.getName(), acc.getAuthorizedPlayers());
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            database.close();
        }
    }

    public void readAccountByAuth(String player, SettableCallable<List<Account>> callback) {
        this.database.connect();
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = this.database.getConnection().prepareStatement(SELECT_ALL_ACCOUNTS_FROM);
            preparedStatement.setString(1, player.toLowerCase());
            //TODO from there go async
            ResultSet resultSet = preparedStatement.executeQuery();
            List<Account> result = new ArrayList<>();
            try {
                while (resultSet.next()) {
                    result.add(AccountBuilder.buildAccountFromResultSet(resultSet));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            callback.setResult(result);
            callback.call();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            database.close();
        }
    }

    public void readAllAccounts(SettableCallable<List<Account>> callback) {
        this.database.connect();
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = this.database.getConnection().prepareStatement(SELECT_ALL_ACCOUNTS);
            //TODO from there go async
            ResultSet resultSet = preparedStatement.executeQuery();
            List<Account> result = new ArrayList<>();
            try {
                while (resultSet.next()) {
                    result.add(AccountBuilder.buildAccountFromResultSet(resultSet));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            callback.setResult(result);
            callback.call();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            database.close();
        }
    }

    public Account readAccountByName(String name) {
        return this.readAccountByName(name, null);
    }

    public Account readAccountByName(String name, SettableCallable<Account> callback) {
        this.database.connect();
        PreparedStatement preparedStatement = null;
        Account res = null;
        try {
            preparedStatement = this.database.getConnection().prepareStatement(SELECT_SPECIFIC_ACCOUNT);
            preparedStatement.setString(1, name.toLowerCase());
            //TODO from there go async
            ResultSet resultSet = preparedStatement.executeQuery();
            try {
                while (resultSet.next()) {
                    res = AccountBuilder.buildAccountFromResultSet(resultSet);
                    if (callback != null) {
                        callback.setResult(res);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            if (callback != null) {
                callback.call();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            database.close();
        }
        return res;
    }

    public void addOwners(String accountName, List<String> owners) { //TODO check if it exists
        database.connect();

        // TODO async + one single query
        try {
            for (String owner : owners) {
                PreparedStatement pstmt = database.getConnection().prepareStatement(INSERT_OWNER);
                pstmt.setString(1, accountName);
                pstmt.setString(2, owner.toLowerCase());
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            database.close();
        }
    }

    public boolean hasAccess(String player, String account) {
        database.connect();
        boolean result = false;
        try (PreparedStatement preparedStatement = database.getConnection().prepareStatement(HAS_ACCESS)) {
            preparedStatement.setString(1, account);
            preparedStatement.setString(2, player.toLowerCase());
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                int count = resultSet.getInt("access");
                result = count >= 1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            database.close();
        }
        return result;
    }

    public void removeOwners(String accountName, List<String> owners) {
        database.connect();

        try {
            PreparedStatement preparedStatement = database.getConnection().prepareStatement(makeAList(DELETE_OWNER, owners.size()));
            preparedStatement.setString(1, accountName);
            setListString(preparedStatement, owners, 2);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            database.close();
        }
    }

    public void updateAccount(Account account) {
        database.connect();
        try {
            PreparedStatement preparedStatement = database.getConnection().prepareStatement(UPDATE_ACCOUNT);
            preparedStatement.setString(1, account.getBank());
            preparedStatement.setBoolean(2, account.getNotif());
            preparedStatement.setString(3, account.getColor());
            preparedStatement.setDouble(4, account.getBalance());
            preparedStatement.setString(5, account.getName());

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            database.close();
        }
    }


    //MOve in the helper (connectionhandler)
    private String makeAList(String originalQuery, int size) {
        ArrayList<String> list = new ArrayList<>();
        for (int i = 0; i < size; ++i) {
            list.add("?");
        }
        String toInsert = StringUtils.join(list, ",");
        return originalQuery.replace("#", toInsert);
    }

    private void setListString(PreparedStatement preparedStatement, List<String> obj, int startFrom) throws SQLException {
        for (int i = 0; i < obj.size(); ++i) {
            preparedStatement.setString(startFrom + i, obj.get(i));
        }
    }

    public List<String> getOwners(String account) {
        database.connect();
        List<String> result = new ArrayList<>();
        try {
            PreparedStatement preparedStatement = database.getConnection().prepareStatement(SELECT_ALL_OWNERS_FROM);
            preparedStatement.setString(1, account);
            ResultSet set = preparedStatement.executeQuery();
            while (set.next()) {
                result.add(set.getString("owner"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            database.close();
        }
        return result;
    }

    public void deleteAccount(String account) {
        database.connect();

        try {
            PreparedStatement preparedStatement = database.getConnection().prepareStatement(DELETE_ACCOUNT);
            preparedStatement.setString(1, account);
            preparedStatement.setString(2, account);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            database.close();
        }

    }


    public Bank getBank(String bank) {
        Bank toRet = null;
        database.connect();

        try {
            PreparedStatement preparedStatement = this.database.getConnection().prepareStatement(SELECT_BANK);
            preparedStatement.setString(1, bank);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                toRet = new Bank(bank, resultSet.getString("owner"), resultSet.getDouble("amount"),false);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            database.close();
        }

        return toRet;
    }

    public Bank getBankFromOwner(String player) {
        Bank toRet = null;
        database.connect();

        try {
            PreparedStatement preparedStatement = this.database.getConnection().prepareStatement(SELECT_BANK_OWNER);
            preparedStatement.setString(1, player);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                toRet = new Bank(resultSet.getString("name"), player, resultSet.getDouble("amount"),false);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            database.close();
        }

        return toRet;
    }

    public void saveBank(Bank bank) {
        database.connect();

        try {
            PreparedStatement pstmt = database.getConnection().prepareStatement(INSERT_BANK);
            pstmt.setString(1, bank.getName());
            pstmt.setString(2, bank.getOwner());
            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            database.close();
        }
    }

    public void updateBank(Bank bank) {
        database.connect();
        try {
            PreparedStatement preparedStatement = database.getConnection().prepareStatement(UPDATE_BANK);
            preparedStatement.setDouble(1, bank.getMoney());
            preparedStatement.setString(2, bank.getName());

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            database.close();
        }
    }


    public List<Account> getClientFromBank(Bank bank) {
        this.database.connect();
        List<Account> result = new ArrayList<>();
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = this.database.getConnection().prepareStatement(SELECT_ACCOUNT_BANK);
            preparedStatement.setString(1, bank.getName());

            ResultSet resultSet = preparedStatement.executeQuery();
            try {
                while (resultSet.next()) {
                    result.add(AccountBuilder.buildAccountFromResultSet(resultSet));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            database.close();
        }

        return result;
    }

    public double getDeposits(Bank bank) {
        double toRet = 0;
        database.connect();

        try {
            PreparedStatement preparedStatement = this.database.getConnection().prepareStatement(SELECT_RESERVE);
            preparedStatement.setString(1, bank.getName());
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                toRet = resultSet.getDouble("reserve");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            database.close();
        }
        return toRet;
    }
}
