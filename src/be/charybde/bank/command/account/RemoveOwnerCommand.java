package be.charybde.bank.command.account;

import be.charybde.bank.BCC;
import be.charybde.bank.command.commandUtil;
import be.charybde.bank.db.SettableCallable;
import be.charybde.bank.entities.Account;
import be.charybde.bank.Utils;
import be.charybde.bank.Vault;
import be.charybde.bank.command.ICommandHandler;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by laurent on 19.04.17.
 */
public class RemoveOwnerCommand implements ICommandHandler {
    private static ICommandHandler instance = new RemoveOwnerCommand();

    private RemoveOwnerCommand() {

    }

    public static ICommandHandler getInstance() {
        return instance;
    }

    @Override
    public boolean handle(String command, String[] args, Player player) {
        if(args.length < 1)
            return false;

        BCC.db.readAccountByName(args[0], new SettableCallable<Account>() {
            @Override
            public Void call() {
                if (result == null) {
                    commandUtil.sendToPlayerOrConsole(Utils.formatMessage("notfound"), player);
                    return null;
                }
                if (Vault.getPermission() != null && player != null && !Vault.getPermission().has(player, "bcc.admin") && !result.isAllowed(player.getName())) {
                    commandUtil.sendToPlayerOrConsole(Utils.formatMessage("notallowed2"), player);
                    return null;
                }

                List<String> toAdd = new ArrayList<>();
                for (int i = 1; i < args.length; ++i) {
                    toAdd.add(args[i].toLowerCase());
                    Map<String, String> message = new HashMap<>();
                    message.put("account", result.displayName());
                    message.put("person", args[i]);
                    commandUtil.sendToPlayerOrConsole(Utils.formatMessage("ownerless", message), player);
                }
                BCC.db.removeOwners(args[0], toAdd);

                return null;
            }
        });
        return true;
    }
}
