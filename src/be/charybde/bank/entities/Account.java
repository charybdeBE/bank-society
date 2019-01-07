package be.charybde.bank.entities;

import be.charybde.bank.BCC;
import be.charybde.bank.Utils;
import be.charybde.bank.Vault;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.PluginCommand;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by laurent on 19.04.17.
 */
public class Account implements Entity {
    private String name;
    private String bankName;
    public List<String> authorizedPlayers;
    private Boolean notif;
    public String color;
    private double amount;


    //For serializing
    public Account() {

    }

    //Kept for legacy section
    public Account(String n, List<String> auth, boolean notif, boolean save) {
        this(n, auth, notif, null, 0, "BCC", save);
    }

    public Account(String n, List<String> auth, boolean notif) {
        this(n, auth, notif, true);
    }

    public Account(String n) {
        this(n, new ArrayList<>(), false, true);
    }

    //end section
    public Account(String name, List<String> auth, Boolean notif, String color, double amount, String bankName, boolean save) {
        this.name = name;
        this.authorizedPlayers = auth;
        this.notif = notif;
        this.color = color;
        this.bankName = bankName;
        this.amount = amount;
        if (save) {
            this.save();
        }
    }

    public static Account fetch(String name) {
        Account acc = BCC.db.readAccountByName(name);
        return acc;
    }

    public boolean pay(double amount, String player) {
        return this.pay(amount, player, "");
    }

    public boolean pay(double amount, String player, String communication) {
        if (amount < 0.0D || Vault.getEconomy().getBalance(player) < amount)
            return false;

        addMoney(amount);
        Vault.getEconomy().withdrawPlayer(player, amount);
        if (notif) {
            Map<String, String> message = new HashMap<>();
            message.put("account", this.displayName());
            message.put("money", Utils.formatDouble(amount));
            message.put("person", player);
            if (!communication.equals("")) {
                communication = ChatColor.GREEN + "(" + ChatColor.WHITE + communication + ChatColor.GREEN + ")";
            }
            message.put("motif", communication);
            sendNotification(Utils.formatMessage("notiftextIn", message));
        }

        Utils.logTransaction(player, this.name, "pay", Double.toString(amount), communication);
        return true;
    }

    //Nb the check for player auth should have been done before
    public boolean withdraw(double amount, String player) {
        return this.withdraw(amount, player, "");
    }

    public boolean withdraw(double amount, String player, String communication) {
        if (amount > 0.0D && this.getBalance() >= amount) {
            Vault.getEconomy().depositPlayer(player, amount);
            this.addMoney(amount * -1);
            if (notif) {
                Map<String, String> message = new HashMap<>();
                message.put("account", this.displayName());
                message.put("money", Utils.formatDouble(amount));
                message.put("person", player);
                if (!communication.equals("")) {
                    communication = ChatColor.GREEN + "(" + ChatColor.WHITE + communication + ChatColor.GREEN + ")";
                }
                message.put("motif", communication);
                sendNotification(Utils.formatMessage("notiftextOut", message));
            }

            Utils.logTransaction(player, this.name, "withdraw", Double.toString(amount), communication);
            return true;
        }

        return false;
    }

    public List<String> getAuthorizedPlayers() {
        if (this.authorizedPlayers == null) {
            this.authorizedPlayers = BCC.db.getOwners(this.getName());
        }
        return this.authorizedPlayers;
    }

    public boolean isAllowed(String player) {
        return BCC.db.hasAccess(player, this.name);
    }

    public double getBalance() {
        return this.amount;
    }

    public void setBalance(double input) {
        this.amount = input;
    }

    public void save() {
        BCC.db.writeAccount(this);
    }

    public void save(boolean existingAccount) {
        if (!existingAccount) {
            this.save();
        } else {
            BCC.db.updateAccount(this);
        }
    }

    public String getName() {
        return this.name;
    }

    public void setName(String s) {
        this.name = s;
    }

    public void setNotif(boolean what) {
        notif = what;
    }

    public void updateNotif(boolean what) {
        this.setNotif(what);
        this.save(true);
    }

    public Boolean getNotif() {
        return notif;
    }


    public String getColor() {
        return color;
    }

    public boolean setColor(String c) {
        if (c.startsWith("&")) {
            String newS = ChatColor.translateAlternateColorCodes('&', c);
            this.color = newS.substring(1);
        } else {
            try {
                ChatColor cc = ChatColor.valueOf(c.toUpperCase());
                System.out.println(cc);
                this.color = String.valueOf(cc.getChar());
            } catch (IllegalArgumentException e) {
                return false;
            }
        }
        return true;
    }

    public boolean updateColor(String c) {
        boolean result = this.setColor(c);
        this.save(true);
        return result;
    }

    private void sendNotification(String s) {
        PluginCommand mail = Bukkit.getPluginCommand("mail");
        String mail_args[] = new String[3];
        mail_args[0] = "send";
        mail_args[1] = "";
        mail_args[2] = s;
        if (this.authorizedPlayers == null) {
            this.authorizedPlayers = this.getAuthorizedPlayers();
        }
        for (String own : this.authorizedPlayers) {
            mail_args[1] = own;
            mail.getExecutor().onCommand(Bukkit.getConsoleSender(), mail, "mail", mail_args);
        }
    }

    public String displayName() {
        if (this.color != null) {
            return ChatColor.getByChar(this.color) + "" + this.name + ChatColor.GREEN;
        } else
            return this.name;
    }

    public void setBank(String s) {
        this.bankName = s;
    }

    public String getBank() {
        return bankName;
    }


//    public Bank getBank() {
//        return Bank.fetch(this.bankName);
//    }

    private void addMoney(double d) {
        this.amount += d;
        this.save(true);
    }
}
