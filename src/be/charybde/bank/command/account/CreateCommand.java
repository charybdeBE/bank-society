package be.charybde.bank.command.account;

import be.charybde.bank.command.commandUtil;
import be.charybde.bank.db.SettableCallable;
import be.charybde.bank.db.sqlite.SQLite;
import be.charybde.bank.entities.Account;
import be.charybde.bank.BCC;
import be.charybde.bank.Utils;
import be.charybde.bank.Vault;
import be.charybde.bank.command.ICommandHandler;
import be.charybde.bank.entities.Bank;
import be.charybde.bank.entities.Entities;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by laurent on 19.04.17.
 */
public class CreateCommand implements ICommandHandler {
    private static ICommandHandler instance = new CreateCommand();

    private CreateCommand() {

    }

    public static ICommandHandler getInstance() {
        return instance;
    }


    public boolean handle(String command, String[] args, Player player) {
        if(args.length < 1)
            return false;

        if(Vault.getPermission() != null && player != null && !Vault.getPermission().has(player, "bcc.create")  && !Vault.getPermission().has(player, "bcc.admin")){
            commandUtil.sendToPlayerOrConsole(Utils.formatMessage("notallowed2"), player);
            return true;
        }

        SettableCallable<List<Account>> callback = new SettableCallable<List<Account>>() {
            @Override
            public Void call() {
                for(Account e : result){
                    if(e.getName().equals(args[0])){
                        commandUtil.sendToPlayerOrConsole(Utils.formatMessage("alreadyexist"), player);
                        return null;
                    }
                }
                Bank organisation = Bank.fetchFromPlayer(player.getName().toLowerCase());
                String banker = "bcc";
                if(organisation != null){
                    banker = organisation.getName();
                }

                String name = "CONSOLE";
                if(player != null){
                    name = player.getName();
                }
                if (args.length == 1){
                    new Account(args[0], new ArrayList<>(), false, null, 0, banker, true);
                }
                else {
                    ArrayList<String> owners = new ArrayList<>();
                    for(int i = 1; i < args.length; ++i)
                        owners.add(args[i].toLowerCase());
                    new Account(args[0], owners, false, null,0,  banker, true);
                }
                Map<String, String> message = new HashMap<>();
                message.put("account", args[0]);
                commandUtil.sendToPlayerOrConsole(Utils.formatMessage("create", message), player);
                Utils.logTransaction(name, args[0], "create", "", "");
                return null;
            }
        };

        BCC.db.readAllAccounts(callback);

        return true;
    }
}
