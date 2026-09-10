package com.donnnsleep.hogwarts.magic;

import com.donnnsleep.hogwarts.HogwartsStoryCore;
import com.donnnsleep.hogwarts.model.StudentProfile;
import com.donnnsleep.hogwarts.util.Msg;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Bộ não của việc niệm chú: kiểm tra đũa phép, năm học, phép đã học,
 * mana, cooldown rồi mới thực thi hiệu ứng.
 */
public class SpellManager {

    private final HogwartsStoryCore plugin;
    private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();

    public SpellManager(HogwartsStoryCore plugin) {
        this.plugin = plugin;
    }

    public enum Result {
        OK, NO_WAND, NOT_LEARNED, YEAR_LOCKED, NO_MANA, COOLDOWN, DISABLED, UNKNOWN_SPELL
    }

    public Result cast(Player player, String spellId) {
        StudentProfile p = plugin.getProfiles().get(player);
        if (p == null) return Result.UNKNOWN_SPELL;

        Spell spell = plugin.getSpells().get(spellId);
        if (spell == null) return Result.UNKNOWN_SPELL;

        if (spell.unforgivable() && !plugin.getConfig().getBoolean("magic.allow-unforgivable", false)) {
            return Result.DISABLED;
        }

        if (!p.hasWand()) return Result.NO_WAND;
        if (!p.hasLearned(spell.id()) && !player.hasPermission("hogwarts.spell.bypass")) {
            return Result.NOT_LEARNED;
        }
        if (p.getYear() < spell.year() && !player.hasPermission("hogwarts.spell.bypass")) {
            return Result.YEAR_LOCKED;
        }

        long now = System.currentTimeMillis();
        Map<String, Long> cd = cooldowns.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>());
        Long ready = cd.get(spell.id());
        if (ready != null && now < ready) return Result.COOLDOWN;

        double cost = spell.manaCost();
        if (p.getMana() < cost) return Result.NO_MANA;

        // ---- thực thi ----
        p.setMana(p.getMana() - cost);
        double control = p.hasWand() ? p.getWand().getCore().getControlMult() : 1.0;
        cd.put(spell.id(), now + (long) (spell.cooldownMs() / control));

        player.sendMessage(Msg.color("&f&o" + spell.incantation() + "!"));
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ILLUSIONER_CAST_SPELL, 1f, 1.4f);

        execute(player, p, spell);

        // báo cho plugin khác biết (BetonQuest, addon riêng...)
        Bukkit.getPluginManager().callEvent(new SpellCastEvent(player, spell));
        runCastHooks(player, spell);

        // niệm chú thành công cũng cho một chút XP
        int gained = p.addXp(plugin.getConfig().getInt("magic.xp-per-cast", 2));
        if (gained > 0) {
            player.sendMessage(Msg.color("&d✦ Cấp phép thuật của bạn đã lên &f" + p.getMagicLevel() + "&d!"));
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.6f);
        }
        return Result.OK;
    }

    /** Hiệu ứng thực tế trong game của từng phép. */
    private void execute(Player player, StudentProfile p, Spell spell) {
        double power = p.hasWand() ? p.getWand().getCore().getPowerMult() : 1.0;
        Location eye = player.getEyeLocation();
        World w = player.getWorld();

        switch (spell.id()) {
            case "lumos" -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 20 * 300, 0, true, false));
                player.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 20 * 300, 0, true, false));
                wandTip(player, Particle.END_ROD, 20);
            }
            case "nox" -> {
                player.removePotionEffect(PotionEffectType.NIGHT_VISION);
                player.removePotionEffect(PotionEffectType.GLOWING);
            }
            case "lumos-maxima" -> {
                w.spawnParticle(Particle.FLASH, eye.clone().add(eye.getDirection().multiply(6)), 3);
                nearbyTargets(player, 8).forEach(e -> {
                    if (e instanceof LivingEntity le)
                        le.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 100, 0));
                });
            }
            case "wingardium-leviosa" -> {
                Entity t = rayTarget(player, 20);
                if (t instanceof LivingEntity le) {
                    le.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 60, 1));
                } else {
                    Block b = rayBlock(player, 12);
                    if (b != null && b.getType().isBlock() && !b.getType().isAir()) {
                        FallingBlock fb = w.spawnFallingBlock(b.getLocation().add(0.5, 0, 0.5), b.getBlockData());
                        fb.setVelocity(new Vector(0, 0.6, 0));
                        b.setType(Material.AIR);
                    }
                }
                wandTip(player, Particle.ENCHANT, 30);
            }
            case "alohomora" -> {
                Block b = rayBlock(player, 6);
                if (b != null && b.getBlockData() instanceof org.bukkit.block.data.Openable op) {
                    op.setOpen(!op.isOpen());
                    b.setBlockData(op);
                    w.playSound(b.getLocation(), Sound.BLOCK_IRON_DOOR_OPEN, 1f, 1f);
                }
            }
            case "incendio" -> {
                Location target = eye.clone().add(eye.getDirection().multiply(5));
                w.spawnParticle(Particle.FLAME, target, 40, 0.4, 0.4, 0.4, 0.02);
                nearbyAt(target, 3).forEach(e -> {
                    if (e instanceof LivingEntity le && !le.equals(player))
                        le.setFireTicks((int) (60 * power));
                });
            }
            case "confringo" -> {
                Location target = eye.clone().add(eye.getDirection().multiply(8));
                w.createExplosion(target, (float) (2.0 * power), false,
                        plugin.getConfig().getBoolean("magic.spell-block-damage", false), player);
            }
            case "petrificus-totalus" -> {
                if (rayTarget(player, 20) instanceof LivingEntity le) {
                    le.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 255));
                    le.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 100, 200));
                    le.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 100, 0));
                    w.spawnParticle(Particle.CRIT, le.getLocation().add(0, 1, 0), 30);
                }
            }
            case "locomotor-mortis" -> {
                if (rayTarget(player, 20) instanceof LivingEntity le) {
                    le.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 140, 4));
                    le.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 140, 200));
                }
            }
            case "expelliarmus", "expelliarmus-maxima" -> {
                boolean maxima = spell.id().endsWith("maxima");
                if (rayTarget(player, 25) instanceof LivingEntity le) {
                    if (le instanceof Player victim) {
                        var hand = victim.getInventory().getItemInMainHand();
                        if (hand != null && !hand.getType().isAir()) {
                            victim.getWorld().dropItemNaturally(victim.getLocation(), hand.clone());
                            victim.getInventory().setItemInMainHand(null);
                        }
                    }
                    le.setVelocity(eye.getDirection().multiply(maxima ? 1.6 : 0.9).setY(0.35));
                    w.spawnParticle(Particle.SWEEP_ATTACK, le.getLocation().add(0, 1, 0), 5);
                    w.playSound(le.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.5f, 2f);
                }
            }
            case "stupefy" -> {
                if (rayTarget(player, 25) instanceof LivingEntity le) {
                    le.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 120, 3));
                    le.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 120, 1));
                    le.damage(4 * power, player);
                    w.spawnParticle(Particle.CRIT, le.getLocation().add(0, 1, 0), 25);
                }
            }
            case "impedimenta" -> {
                if (rayTarget(player, 25) instanceof LivingEntity le) {
                    le.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 160, 5));
                    le.setVelocity(new Vector(0, 0, 0));
                }
            }
            case "rictusempra" -> {
                if (rayTarget(player, 20) instanceof LivingEntity le) {
                    le.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 200, 1));
                    le.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 200, 0));
                }
            }
            case "densaugeo" -> {
                if (rayTarget(player, 20) instanceof LivingEntity le) {
                    le.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 200, 1));
                    le.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 200, 2));
                }
            }
            case "silencio", "langlock" -> {
                if (rayTarget(player, 20) instanceof Player victim) {
                    plugin.getSilenced().put(victim.getUniqueId(),
                            System.currentTimeMillis() + 15000);
                    victim.sendMessage(Msg.color("&8Bạn không thể niệm chú lúc này..."));
                }
            }
            case "levicorpus" -> {
                if (rayTarget(player, 20) instanceof LivingEntity le) {
                    le.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 100, 2));
                    le.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 100, 1));
                }
            }
            case "liberacorpus" -> {
                if (rayTarget(player, 20) instanceof LivingEntity le) {
                    le.removePotionEffect(PotionEffectType.LEVITATION);
                }
            }
            case "immobulus" -> nearbyTargets(player, 10).forEach(e -> {
                if (e instanceof LivingEntity le && !le.equals(player)) {
                    le.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 120, 255));
                    le.setAI(false);
                    Bukkit.getScheduler().runTaskLater(plugin, () -> le.setAI(true), 120L);
                }
            });
            case "reducto" -> {
                Block b = rayBlock(player, 15);
                if (b != null && plugin.getConfig().getBoolean("magic.spell-block-damage", false)) {
                    b.breakNaturally();
                }
                w.spawnParticle(Particle.EXPLOSION, eye.clone().add(eye.getDirection().multiply(6)), 3);
                nearbyAt(eye.clone().add(eye.getDirection().multiply(6)), 3).forEach(e -> {
                    if (e instanceof LivingEntity le && !le.equals(player)) le.damage(6 * power, player);
                });
            }
            case "protego" -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 200, 2));
                player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 200, 1));
                circle(player, Particle.ENCHANTED_HIT, 1.8);
            }
            case "protego-totalum", "salvio-hexia", "cave-inimicum" -> {
                nearbyTargets(player, 12).forEach(e -> {
                    if (e instanceof Player ally)
                        ally.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 600, 1));
                });
                player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 600, 1));
                circle(player, Particle.END_ROD, 4.0);
            }
            case "expecto-patronum" -> {
                circle(player, Particle.END_ROD, 3.0);
                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 200, 1));
                player.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 200, 0));
                nearbyTargets(player, 15).forEach(e -> {
                    if (e instanceof Monster m) {
                        Vector away = m.getLocation().toVector()
                                .subtract(player.getLocation().toVector()).normalize().multiply(1.5);
                        m.setVelocity(away.setY(0.4));
                        m.damage(4 * power, player);
                    }
                });
                w.playSound(player.getLocation(), Sound.ENTITY_ALLAY_AMBIENT_WITH_ITEM, 1f, 1f);
            }
            case "riddikulus" -> {
                nearbyTargets(player, 8).forEach(e -> {
                    if (e instanceof Monster m) {
                        m.damage(8 * power, player);
                        m.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, m.getLocation().add(0, 1, 0), 20);
                    }
                });
                w.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_CELEBRATE, 1f, 1.5f);
            }
            case "accio" -> {
                nearbyTargets(player, 15).forEach(e -> {
                    if (e instanceof Item item) {
                        Vector pull = player.getLocation().toVector()
                                .subtract(item.getLocation().toVector()).normalize().multiply(0.8);
                        item.setVelocity(pull);
                    }
                });
                if (rayTarget(player, 20) instanceof LivingEntity le && !(le instanceof Player)) {
                    Vector pull = player.getLocation().toVector()
                            .subtract(le.getLocation().toVector()).normalize().multiply(1.2);
                    le.setVelocity(pull.setY(0.3));
                }
            }
            case "reparo" -> {
                var hand = player.getInventory().getItemInMainHand();
                if (hand.getItemMeta() instanceof org.bukkit.inventory.meta.Damageable dmg && dmg.hasDamage()) {
                    dmg.setDamage(0);
                    hand.setItemMeta((org.bukkit.inventory.meta.ItemMeta) dmg);
                    player.sendMessage(Msg.color("&aVật phẩm đã được hàn gắn."));
                }
            }
            case "episkey", "ferula" -> {
                var maxHp = player.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH);
                double cap = maxHp != null ? maxHp.getValue() : 20.0;
                player.setHealth(Math.min(player.getHealth() + 6 * power, cap));
                player.getWorld().spawnParticle(Particle.HEART, player.getLocation().add(0, 2, 0), 8);
            }
            case "impervius" -> player.addPotionEffect(
                    new PotionEffect(PotionEffectType.WATER_BREATHING, 600, 0));
            case "aguamenti" -> {
                Location target = eye.clone().add(eye.getDirection().multiply(4));
                w.spawnParticle(Particle.SPLASH, target, 60, 0.5, 0.5, 0.5, 0.1);
                nearbyAt(target, 3).forEach(e -> {
                    if (e instanceof LivingEntity le) le.setFireTicks(0);
                });
            }
            case "muffliato" -> nearbyTargets(player, 12).forEach(e -> {
                if (e instanceof Player other && !other.equals(player))
                    other.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 100, 0));
            });
            case "serpensortia" -> {
                Location spawn = eye.clone().add(eye.getDirection().multiply(3));
                spawn.setY(player.getLocation().getY());
                Entity snake = w.spawnEntity(spawn, EntityType.CAVE_SPIDER);
                snake.customName(Msg.color("&2Rắn triệu hồi"));
                snake.setCustomNameVisible(true);
            }
            case "sectumsempra" -> {
                if (rayTarget(player, 20) instanceof LivingEntity le) {
                    le.damage(10 * power, player);
                    le.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 100, 1));
                    w.spawnParticle(Particle.DUST, le.getLocation().add(0, 1, 0), 30,
                            new Particle.DustOptions(Color.RED, 1.5f));
                }
            }
            case "finite-incantatem" -> {
                player.getActivePotionEffects().forEach(pe -> player.removePotionEffect(pe.getType()));
                plugin.getSilenced().remove(player.getUniqueId());
            }
            case "point-me" -> player.sendMessage(Msg.color(
                    "&b➤ Đũa phép chỉ về hướng Bắc. Bạn đang quay mặt: &f" + facing(player)));
            case "fiendfyre" -> {
                Location target = eye.clone().add(eye.getDirection().multiply(6));
                w.spawnParticle(Particle.FLAME, target, 200, 2, 2, 2, 0.1);
                nearbyAt(target, 6).forEach(e -> {
                    if (e instanceof LivingEntity le && !le.equals(player)) {
                        le.setFireTicks(200);
                        le.damage(12 * power, player);
                    }
                });
                w.playSound(target, Sound.ENTITY_BLAZE_SHOOT, 2f, 0.5f);
            }
            // ---- Lời nguyền Không thể Tha thứ ----
            case "crucio" -> {
                if (rayTarget(player, 20) instanceof LivingEntity le) {
                    le.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 200, 4));
                    le.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 200, 2));
                    le.damage(8 * power, player);
                }
            }
            case "imperio" -> {
                if (rayTarget(player, 20) instanceof LivingEntity le && !(le instanceof Player)) {
                    if (le instanceof Mob mob) mob.setTarget(null);
                    le.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 400, 0));
                }
            }
            case "avada-kedavra" -> {
                if (rayTarget(player, 25) instanceof LivingEntity le) {
                    w.spawnParticle(Particle.DUST, le.getLocation().add(0, 1, 0), 60,
                            new Particle.DustOptions(Color.LIME, 2f));
                    le.setHealth(0);
                }
            }
            default -> wandTip(player, Particle.ENCHANT, 20);
        }
    }

    /**
     * Chạy các lệnh console cấu hình trong config.yml &gt; cast-hooks.
     * Đây là cầu nối sang BetonQuest: mỗi lần niệm phép có thể kích một sự kiện quest.
     */
    private void runCastHooks(Player player, Spell spell) {
        var section = plugin.getConfig().getConfigurationSection("cast-hooks");
        if (section == null) return;

        java.util.List<String> cmds = new java.util.ArrayList<>();
        cmds.addAll(section.getStringList("any"));
        cmds.addAll(section.getStringList(spell.id()));
        if (cmds.isEmpty()) return;

        for (String raw : cmds) {
            String cmd = raw.replace("%player%", player.getName())
                            .replace("%spell%", spell.id())
                            .replace("%incantation%", spell.incantation());
            Bukkit.getScheduler().runTask(plugin, () ->
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd));
        }
    }

    // ---------- tiện ích ----------
    private Entity rayTarget(Player player, double range) {
        RayTraceResult r = player.getWorld().rayTraceEntities(
                player.getEyeLocation(), player.getEyeLocation().getDirection(), range, 1.0,
                e -> !e.equals(player));
        return r == null ? null : r.getHitEntity();
    }

    private Block rayBlock(Player player, double range) {
        RayTraceResult r = player.rayTraceBlocks(range);
        return r == null ? null : r.getHitBlock();
    }

    private java.util.Collection<Entity> nearbyTargets(Player player, double r) {
        return player.getNearbyEntities(r, r, r);
    }

    private java.util.Collection<Entity> nearbyAt(Location loc, double r) {
        return loc.getWorld().getNearbyEntities(loc, r, r, r);
    }

    private void wandTip(Player p, Particle particle, int count) {
        Location tip = p.getEyeLocation().add(p.getEyeLocation().getDirection().multiply(1.2));
        p.getWorld().spawnParticle(particle, tip, count, 0.1, 0.1, 0.1, 0.01);
    }

    private void circle(Player p, Particle particle, double radius) {
        Location c = p.getLocation();
        for (int i = 0; i < 40; i++) {
            double a = 2 * Math.PI * i / 40;
            c.getWorld().spawnParticle(particle,
                    c.clone().add(Math.cos(a) * radius, 1, Math.sin(a) * radius), 1, 0, 0, 0, 0);
        }
    }

    private String facing(Player p) {
        float yaw = (p.getLocation().getYaw() % 360 + 360) % 360;
        if (yaw < 45 || yaw >= 315) return "Nam";
        if (yaw < 135) return "Tây";
        if (yaw < 225) return "Bắc";
        return "Đông";
    }

    public void clear(UUID uuid) {
        cooldowns.remove(uuid);
    }

    public long remainingCooldown(UUID uuid, String spellId) {
        Map<String, Long> cd = cooldowns.get(uuid);
        if (cd == null) return 0;
        Long r = cd.get(spellId.toLowerCase());
        return r == null ? 0 : Math.max(0, r - System.currentTimeMillis());
    }
}

