package com.donnnsleep.hogwarts.command;

import com.donnnsleep.hogwarts.HogwartsStoryCore;
import com.donnnsleep.hogwarts.magic.Spell;
import com.donnnsleep.hogwarts.magic.SpellManager;
import com.donnnsleep.hogwarts.model.StudentProfile;
import com.donnnsleep.hogwarts.util.Msg;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * /spell &lt;tên phép&gt;        -> chọn phép làm phép đang dùng
 * /spell cast &lt;tên phép&gt;   -> niệm ngay lập tức
 */
public class SpellCommand implements CommandExecutor, TabCompleter {

    private final HogwartsStoryCore plugin;

    public SpellCommand(HogwartsStoryCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return true;
        StudentProfile p = plugin.getProfiles().get(player);
        if (p == null) {
            player.sendMessage(Msg.msg("&cKhông đọc được hồ sơ của bạn. Hãy thoát ra vào lại server."));
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(Msg.msg("&7Dùng: &f/spell <tên phép> &7hoặc &f/spell cast <tên phép>"));
            return true;
        }

        boolean castNow = args[0].equalsIgnoreCase("cast");
        String id = castNow ? (args.length > 1 ? args[1] : "") : args[0];

        Spell spell = plugin.getSpells().get(id);
        if (spell == null) spell = plugin.getSpells().byIncantation(String.join(" ", args));
        if (spell == null) {
            player.sendMessage(Msg.msg("&cKhông có câu thần chú nào tên như vậy."));
            return true;
        }

        if (!p.hasLearned(spell.id()) && !player.hasPermission("hogwarts.spell.bypass")) {
            player.sendMessage(Msg.msg("&cCon chưa học &f" + spell.incantation() + "&c."));
            return true;
        }

        if (castNow) {
            SpellManager.Result r = plugin.getSpellManager().cast(player, spell.id());
            if (r != SpellManager.Result.OK) {
                player.sendMessage(Msg.msg("&c" + switch (r) {
                    case NO_MANA -> "Không đủ ma lực.";
                    case COOLDOWN -> "Đũa phép chưa hồi.";
                    case NO_WAND -> "Con chưa có đũa phép.";
                    case YEAR_LOCKED -> "Phép này chưa tới năm học của con.";
                    case DISABLED -> "Phép này bị cấm trên server.";
                    default -> "Không niệm được phép.";
                }));
            }
        } else {
            p.setActiveSpell(spell.id());
            player.sendMessage(Msg.msg("&7Phép đang chọn: " + spell.displayColor() + spell.incantation()
                    + " &8· &7" + (int) spell.manaCost() + " ma lực"));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd,
                                      @NotNull String alias, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return List.of();
        StudentProfile p = plugin.getProfiles().get(player);
        if (p == null) return List.of();
        if (args.length == 1) {
            return p.getLearnedSpells().stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase())).toList();
        }
        return List.of();
    }
}
