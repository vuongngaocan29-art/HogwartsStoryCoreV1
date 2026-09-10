package com.donnnsleep.hogwarts.magic;

import com.donnnsleep.hogwarts.HogwartsStoryCore;
import com.donnnsleep.hogwarts.model.StudentProfile;
import com.donnnsleep.hogwarts.util.Msg;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

/**
 * Điều khiển đũa phép:
 *   Chuột phải          -> niệm phép đang chọn
 *   Shift + chuột phải  -> chuyển sang phép đã học kế tiếp
 */
public class WandListener implements Listener {

    private final HogwartsStoryCore plugin;

    public WandListener(HogwartsStoryCore plugin) {
        this.plugin = plugin;
    }

    private boolean isWand(ItemStack is) {
        if (is == null || is.getItemMeta() == null) return false;
        return is.getItemMeta().getPersistentDataContainer()
                .has(plugin.getWandKey(), PersistentDataType.STRING);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getHand() != org.bukkit.inventory.EquipmentSlot.HAND) return;
        if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (!isWand(e.getItem())) return;

        e.setCancelled(true);
        Player player = e.getPlayer();
        StudentProfile p = plugin.getProfiles().get(player);
        if (p == null) return;

        if (player.isSneaking()) {
            cycleSpell(player, p);
            return;
        }

        // bị Silencio / Langlock
        Long until = plugin.getSilenced().get(player.getUniqueId());
        if (until != null && System.currentTimeMillis() < until) {
            player.sendActionBar(Msg.color("&8Lưỡi con cứng lại — không niệm chú được!"));
            return;
        }

        String active = p.getActiveSpell();
        if (active == null) {
            player.sendActionBar(Msg.color("&cChưa chọn phép. Shift + chuột phải để chọn."));
            return;
        }

        SpellManager.Result r = plugin.getSpellManager().cast(player, active);
        Spell s = plugin.getSpells().get(active);
        switch (r) {
            case OK -> {}
            case NO_WAND -> player.sendActionBar(Msg.color("&cCon chưa có đũa phép."));
            case NOT_LEARNED -> player.sendActionBar(Msg.color("&cCon chưa học phép này."));
            case YEAR_LOCKED -> player.sendActionBar(Msg.color("&cPhép này chỉ dạy từ Năm "
                    + com.donnnsleep.hogwarts.story.StoryManager.roman(s.year()) + "."));
            case NO_MANA -> {
                player.sendActionBar(Msg.color("&9Không đủ ma lực."));
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.6f, 0.6f);
            }
            case COOLDOWN -> {
                long left = plugin.getSpellManager().remainingCooldown(player.getUniqueId(), active);
                player.sendActionBar(Msg.color("&7Đũa phép chưa hồi &8(" + (left / 1000 + 1) + "s)"));
            }
            case DISABLED -> player.sendActionBar(Msg.color("&4Phép này bị cấm trên server."));
            case UNKNOWN_SPELL -> player.sendActionBar(Msg.color("&cKhông có phép này."));
        }
    }

    private void cycleSpell(Player player, StudentProfile p) {
        List<String> known = new ArrayList<>(p.getLearnedSpells());
        if (known.isEmpty()) {
            player.sendActionBar(Msg.color("&cCon chưa học phép nào."));
            return;
        }
        int idx = known.indexOf(p.getActiveSpell());
        String next = known.get((idx + 1) % known.size());
        p.setActiveSpell(next);

        Spell s = plugin.getSpells().get(next);
        player.sendActionBar(Msg.color("&7Phép đang chọn: " + s.displayColor() + s.incantation()
                + " &8· &7" + (int) s.manaCost() + " ma lực"));
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.5f);
    }
}
