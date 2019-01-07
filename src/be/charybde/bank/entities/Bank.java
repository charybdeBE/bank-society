package be.charybde.bank.entities;

import be.charybde.bank.BCC;
import be.charybde.bank.Utils;
import be.charybde.bank.Vault;

import java.util.ArrayList;
import java.util.List;


//Note client account not computed by default
public class Bank implements Entity {

    private String name;
    private double money; // Note the amount store in the db is just the difference between deposits and reserve
    private String owner;
    private ArrayList<String> employees; //TODO
    private double deposits;
    private boolean depositsFetch = false;

    // Constructor/fetcher

    public Bank(String _name, String _owner, double money, boolean save){
        this.name = _name;
        this.owner = _owner;
        this.employees = new ArrayList<>();
        this.employees.add(owner);
        this.money = money;
        if(save)
            this.save();
    }

    public static Bank fetch(String name){
        return BCC.db.getBank(name);
    }

    public static Bank fetchFromPlayer(String player){
        return BCC.db.getBankFromOwner(player);
    }


    // Public section

    public boolean withdraw(String player, double amount, String reason){
        if(amount > 0.0D && this.money >= amount && amount <= this.getCapacity()){
            Vault.getEconomy().depositPlayer(player, amount);
            this.money -= amount;
            this.update();
            Utils.logTransaction(player, this.name, "Bank-withdraw", Double.toString(amount), reason);
            return true;
        }

        return false;
    }

    public boolean pay(String player, double amount, String reason){
        if(amount > 0.0D && Vault.getEconomy().getBalance(player) >= amount){
            Vault.getEconomy().withdrawPlayer(player, amount);
            this.money += amount;
            this.update();
            Utils.logTransaction(player, this.name, "Bank-depot", Double.toString(amount), reason);
            return true;
        }

        return false;
    }

    public String getOwner() {
        return owner;
    }

    @Override
    public void save() {
        BCC.db.saveBank(this);
    }

    public void update() {
        BCC.db.updateBank(this);
    }

    public boolean hasEmployee(String player){
        return this.employees.contains(player);
    }

    public String getName(){
        return this.name;
    }

    public double getMoney(){
        return this.money;
    }

    public List<Account> fetchClients(){
        return BCC.db.getClientFromBank(this);
    }

    public double getCapacity(){
        if(!this.depositsFetch){
            this.deposits = BCC.db.getDeposits(this);
            this.depositsFetch = true;
        }
        return this.money  + deposits * 0.5;
    }
}
