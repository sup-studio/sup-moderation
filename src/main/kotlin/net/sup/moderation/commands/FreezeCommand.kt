package net.sup.moderation.commands

import net.sup.moderation.utils.FreezeManager
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class FreezeCommand : CommandExecutor {

    companion object {
        const val PERMISSION = "sup.moderation.freeze"
    }

    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): Boolean {

        if (!sender.hasPermission(PERMISSION)) {
            sender.sendMessage("§cDu hast keine Berechtigung für diesen Befehl.")
            return true
        }

        if (args.isEmpty()) {
            sender.sendMessage("§cVerwendung: /freeze <Spieler>")
            return true
        }

        val target = Bukkit.getPlayerExact(args[0])

        if (target == null) {
            sender.sendMessage("§cSpieler nicht gefunden oder nicht online.")
            return true
        }

        if (sender.name.equals(target.name, true)) {
            sender.sendMessage("§cDu kannst dich nicht selbst einfrieren.")
            return true
        }

        if (FreezeManager.isFrozen(target.uniqueId)) {

            FreezeManager.unfreeze(target.uniqueId)

            sender.sendMessage("§a${target.name} wurde entfreezt.")
            target.sendMessage("§aDu wurdest entfreezt.")

        } else {
            FreezeManager.freeze(target.uniqueId)

            sender.sendMessage("§e${target.name} wurde eingefroren.")
            target.sendMessage("§cDu wurdest eingefroren! Bitte bewege dich nicht.")
        }

        return true
    }
}