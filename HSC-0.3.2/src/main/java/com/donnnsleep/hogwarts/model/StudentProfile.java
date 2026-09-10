package com.donnnsleep.hogwarts.model;

import java.util.*;

/**
 * Hồ sơ học sinh Hogwarts. Đây là "sổ điểm" của mỗi người chơi:
 * Nhà, năm học, cấp phép thuật, mana, đũa phép, phép đã học, tiến độ cốt truyện.
 */
public class StudentProfile {

    private final UUID uuid;
    private String name;

    private House house;               // null = chưa Phân Loại
    private int year = 1;              // 1..7
    private int magicLevel = 1;
    private int magicXp = 0;

    private double mana = 100;
    private double maxMana = 100;

    private Wand wand;                 // null = chưa có đũa

    private final Set<String> learnedSpells = new LinkedHashSet<>();
    private String activeSpell;

    /** Cột mốc cốt truyện đã hoàn thành, ví dụ "year1.letter", "year1.sorting". */
    private final Set<String> storyFlags = new LinkedHashSet<>();

    /** Điểm cá nhân đã đóng góp cho Nhà trong năm học hiện tại. */
    private int contributedPoints = 0;

    public StudentProfile(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
    }

    // ---- getters / setters ----
    public UUID getUuid() { return uuid; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public House getHouse() { return house; }
    public void setHouse(House house) { this.house = house; }
    public boolean isSorted() { return house != null; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = Math.max(1, Math.min(7, year)); }

    public int getMagicLevel() { return magicLevel; }
    public void setMagicLevel(int magicLevel) { this.magicLevel = magicLevel; }

    public int getMagicXp() { return magicXp; }
    public void setMagicXp(int magicXp) { this.magicXp = magicXp; }

    public double getMana() { return mana; }
    public void setMana(double mana) { this.mana = Math.max(0, Math.min(maxMana, mana)); }

    public double getMaxMana() { return maxMana; }
    public void setMaxMana(double maxMana) { this.maxMana = maxMana; }

    public Wand getWand() { return wand; }
    public void setWand(Wand wand) { this.wand = wand; }
    public boolean hasWand() { return wand != null; }

    public Set<String> getLearnedSpells() { return learnedSpells; }
    public boolean hasLearned(String spellId) { return learnedSpells.contains(spellId.toLowerCase()); }
    public boolean learn(String spellId) { return learnedSpells.add(spellId.toLowerCase()); }
    public boolean unlearn(String spellId) { return learnedSpells.remove(spellId.toLowerCase()); }

    public String getActiveSpell() { return activeSpell; }
    public void setActiveSpell(String activeSpell) { this.activeSpell = activeSpell; }

    public Set<String> getStoryFlags() { return storyFlags; }
    public boolean hasFlag(String flag) { return storyFlags.contains(flag); }
    public void addFlag(String flag) { storyFlags.add(flag); }
    public void removeFlag(String flag) { storyFlags.remove(flag); }

    public int getContributedPoints() { return contributedPoints; }
    public void addContributedPoints(int p) { this.contributedPoints += p; }
    public void setContributedPoints(int p) { this.contributedPoints = p; }

    // ---- XP / Level ----
    /** XP cần để lên level tiếp theo. Tăng dần theo level. */
    public int xpToNextLevel() {
        return 100 + (magicLevel - 1) * 50;
    }

    /** Cộng XP, trả về số level đã lên. */
    public int addXp(int amount) {
        this.magicXp += amount;
        int levelsGained = 0;
        while (magicXp >= xpToNextLevel()) {
            magicXp -= xpToNextLevel();
            magicLevel++;
            levelsGained++;
            maxMana += 10; // mỗi level +10 mana tối đa
        }
        return levelsGained;
    }

    /** Mana tối đa thực tế sau khi tính lõi đũa phép. */
    public double effectiveMaxMana() {
        double mult = wand != null ? wand.getCore().getManaMult() : 1.0;
        return maxMana * mult;
    }
}
