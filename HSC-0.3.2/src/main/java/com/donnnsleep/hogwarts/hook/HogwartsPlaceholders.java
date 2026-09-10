package com.donnnsleep.hogwarts.hook;

import com.donnnsleep.hogwarts.HogwartsStoryCore;
import com.donnnsleep.hogwarts.model.House;
import com.donnnsleep.hogwarts.model.StudentProfile;
import com.donnnsleep.hogwarts.story.StoryManager;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Placeholder cho AetheriaTAB / TAB / scoreboard.
 *
 *  %hogwarts_house%          Slytherin
 *  %hogwarts_house_colored%  &aSlytherin
 *  %hogwarts_house_color%    &a
 *  %hogwarts_year%           I
 *  %hogwarts_level%          11
 *  %hogwarts_xp%             40
 *  %hogwarts_xp_next%        150
 *  %hogwarts_mana%           85
 *  %hogwarts_max_mana%       100
 *  %hogwarts_wand%           Holly, lông đuôi Phượng hoàng, 11.00 inch, dẻo vừa
 *  %hogwarts_spell%          Expelliarmus
 *  %hogwarts_spells_count%   7
 *  %hogwarts_points%         điểm Nhà của người chơi
 *  %hogwarts_points_<nhà>%   điểm của một Nhà cụ thể
 *  %hogwarts_leader%         Nhà đang dẫn đầu
 */
public class HogwartsPlaceholders extends PlaceholderExpansion {

    private final HogwartsStoryCore plugin;

    public HogwartsPlaceholders(HogwartsStoryCore plugin) {
        this.plugin = plugin;
    }

    @Override public @NotNull String getIdentifier() { return "hogwarts"; }
    @Override public @NotNull String getAuthor() { return "DonnnSleep"; }
    @Override public @NotNull String getVersion() { return plugin.getPluginMeta().getVersion(); }
    @Override public boolean persist() { return true; }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        if (params.startsWith("points_")) {
            House h = House.fromString(params.substring(7));
            return h == null ? "0" : String.valueOf(plugin.getPoints().get(h));
        }
        if (params.equals("leader")) {
            House h = plugin.getPoints().leader();
            return h.getLegacyColor() + h.getDisplayName();
        }

        if (player == null) return "";
        StudentProfile p = plugin.getProfiles().get(player);
        if (p == null) return "";

        return switch (params) {
            case "house" -> p.getHouse() == null ? "Chưa phân loại" : p.getHouse().getDisplayName();
            case "house_colored" -> p.getHouse() == null ? "&8Chưa phân loại"
                    : p.getHouse().getLegacyColor() + p.getHouse().getDisplayName();
            case "house_color" -> p.getHouse() == null ? "&7" : p.getHouse().getLegacyColor();
            case "year" -> StoryManager.roman(p.getYear());
            case "year_number" -> String.valueOf(p.getYear());
            case "level" -> String.valueOf(p.getMagicLevel());
            case "xp" -> String.valueOf(p.getMagicXp());
            case "xp_next" -> String.valueOf(p.xpToNextLevel());
            case "mana" -> String.valueOf((int) p.getMana());
            case "max_mana" -> String.valueOf((int) p.effectiveMaxMana());
            case "wand" -> p.hasWand() ? p.getWand().describe() : "chưa có";
            case "wand_core" -> p.hasWand() ? p.getWand().getCore().getVi() : "-";
            case "spell" -> {
                var s = plugin.getSpells().get(p.getActiveSpell());
                yield s == null ? "-" : s.incantation();
            }
            case "spells_count" -> String.valueOf(p.getLearnedSpells().size());
            case "points" -> p.getHouse() == null ? "0" : String.valueOf(plugin.getPoints().get(p.getHouse()));
            case "contributed" -> String.valueOf(p.getContributedPoints());
            case "sorted" -> p.isSorted() ? "true" : "false";
            default -> null;
        };
    }
}
