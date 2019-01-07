package be.charybde.bank.command.account;

import be.charybde.bank.db.SettableCallable;
import be.charybde.bank.entities.Account;
import be.charybde.bank.BCC;
import be.charybde.bank.Utils;
import be.charybde.bank.Vault;
import be.charybde.bank.command.ICommandHandler;
import be.charybde.bank.command.commandUtil;
import be.charybde.bank.entities.Entities;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by laurent on 39.06.17.
 */
public class DeleteAccountCommand implements ICommandHandler {
    private static ICommandHandler instance = new DeleteAccountCommand();

    private DeleteAccountCommand() {

    }

    public static ICommandHandler getInstance() {
        return instance;
    }


    public boolean handle(String command, String[] args, Player player) {
        if(args.length < 1)
            return false;

        if(player != null && !Vault.getPermission().has(player, "bcc.admin")){
            commandUtil.sendToPlayerOrConsole(Utils.formatMessage("notallowed2"), player);
            return true;
        }

        SettableCallable<List<Account>> callback = new SettableCallable<List<Account>>() {
            @Override
            public Void call() {
                Account found = null;
                for(Account e : result){
                    if(e.getName().equals(args[0])){
                        found = e;
                        break;
                    }
                }
                if(found == null){
                    commandUtil.sendToPlayerOrConsole(Utils.formatMessage("alreadyexist"), player);
                    return null;
                }

                String name = "CONSOLE";
                if(player != null){
                    name = player.getName();
                }

                BCC.db.deleteAccount(found.getName());

                Map<String, String> message = new HashMap<>();
                message.put("account", args[0]);
                message.put("money", String.valueOf(found.getBalance()));
                commandUtil.sendToPlayerOrConsole(Utils.formatMessage("delete", message), player);
                Utils.logTransaction(name, args[0], "delete", Double.toString(found.getBalance()), "");
                return null;
            }
        };

        BCC.db.readAllAccounts(callback);


        return true;
    }
}
