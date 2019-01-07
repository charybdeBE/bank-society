package be.charybde.bank.command.account;

import be.charybde.bank.db.SettableCallable;
import be.charybde.bank.entities.Account;
import be.charybde.bank.BCC;
import be.charybde.bank.Utils;
import be.charybde.bank.Vault;
import be.charybde.bank.command.ICommandHandler;
import be.charybde.bank.command.commandUtil;
import be.charybde.bank.entities.Bank;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * Created by laurent on 20.04.17.
 */
public class ListCommand implements ICommandHandler {
    private static ICommandHandler instance = new ListCommand();

    private ListCommand() {

    }

    public static ICommandHandler getInstance() {
        return instance;
    }

    @Override
    public boolean handle(String command, String[] args, Player player) {
        boolean all = false;
        Bank bank = null;
        if(player != null){
            bank = Bank.fetchFromPlayer(player.getName().toLowerCase());
        }
        if (player != null && args.length == 1 && args[0].equals("all")) {
            if (Vault.getPermission() == null || Vault.getPermission().has(player, "bcc.list")) {
                all = true;
            } else if (bank != null) {
                List<Account> clients = bank.fetchClients();
                for (Account it : clients) {
                    Map<String, String> message = new HashMap<>();
                    message.put("account", it.displayName());
                    message.put("money", Utils.formatDouble(it.getBalance()));
                    message.put("bank", bank.getName());
                    commandUtil.sendToPlayerOrConsole(Utils.formatMessage("balance", message), player);
                }
                return true;
            } else {
                commandUtil.sendToPlayerOrConsole(Utils.formatMessage("notallowed2"), player);
                return true;
            }
        }

        SettableCallable<List<Account>> callback = new SettableCallable<List<Account>>() {
            @Override
            public Void call() {
                commandUtil.sendToPlayerOrConsole(Utils.formatMessage("list"), player);
                for (Account e : result) {
                    Map<String, String> message = new HashMap<>();
                    String bankName = e.getBank();
                    message.put("account", e.displayName());
                    message.put("bank", bankName);
                    message.put("money", Utils.formatDouble(e.getBalance()));
                    commandUtil.sendToPlayerOrConsole(Utils.formatMessage("balance", message), player);
                }
                return null;
            }
        };

        if (all || player == null) {
            BCC.db.readAllAccounts(callback);
        } else {
            BCC.db.readAccountByAuth(player.getName().toLowerCase(), callback);
        }

        return true;
    }

}
