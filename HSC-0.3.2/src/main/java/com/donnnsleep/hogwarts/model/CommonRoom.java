package com.donnnsleep.hogwarts.model;

import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

/**
 * Phòng Sinh hoạt chung của một Nhà.
 * Mỗi Nhà có một cách vào riêng, đúng như trong nguyên tác.
 */
public class CommonRoom {

    /** Cách mở cửa vào phòng. */
    public enum EntryType {
        /** Gryffindor — chân dung Bà Béo hỏi mật khẩu. */
        PASSWORD,
        /** Slytherin — bức tường đá trong hầm ngục, cũng dùng mật khẩu. */
        PASSWORD_WALL,
        /** Ravenclaw — con đại bàng đồng hỏi một câu đố. Trả lời đúng là vào được. */
        RIDDLE,
        /** Hufflepuff — gõ vào thùng gỗ đúng số nhịp của "Hel-ga Huf-fle-puff". */
        KNOCK
    }

    private final House house;
    private EntryType entryType;

    private Location entrance;   // khối cần nhấp chuột phải
    private Location spawn;      // điểm dịch chuyển vào bên trong
    private double radius = 3.0; // bán kính quanh cửa vào

    private String password;
    private long passwordSetAt;

    private int knockCount = 7;               // Hufflepuff
    private final List<Riddle> riddles = new ArrayList<>(); // Ravenclaw

    /** Vùng bên trong phòng, dùng để đuổi người Nhà khác ra. */
    private Location regionMin;
    private Location regionMax;

    public record Riddle(String question, List<String> answers) {}

    public CommonRoom(House house, EntryType entryType) {
        this.house = house;
        this.entryType = entryType;
    }

    public House getHouse() { return house; }

    public EntryType getEntryType() { return entryType; }
    public void setEntryType(EntryType entryType) { this.entryType = entryType; }

    public Location getEntrance() { return entrance; }
    public void setEntrance(Location entrance) { this.entrance = entrance; }

    public Location getSpawn() { return spawn; }
    public void setSpawn(Location spawn) { this.spawn = spawn; }

    public double getRadius() { return radius; }
    public void setRadius(double radius) { this.radius = radius; }

    public String getPassword() { return password; }
    public void setPassword(String password) {
        this.password = password;
        this.passwordSetAt = System.currentTimeMillis();
    }
    public long getPasswordSetAt() { return passwordSetAt; }
    public void setPasswordSetAt(long t) { this.passwordSetAt = t; }

    public int getKnockCount() { return knockCount; }
    public void setKnockCount(int knockCount) { this.knockCount = knockCount; }

    public List<Riddle> getRiddles() { return riddles; }

    public Location getRegionMin() { return regionMin; }
    public Location getRegionMax() { return regionMax; }
    public void setRegion(Location a, Location b) {
        if (a == null || b == null) { this.regionMin = null; this.regionMax = null; return; }
        this.regionMin = new Location(a.getWorld(),
                Math.min(a.getX(), b.getX()), Math.min(a.getY(), b.getY()), Math.min(a.getZ(), b.getZ()));
        this.regionMax = new Location(a.getWorld(),
                Math.max(a.getX(), b.getX()), Math.max(a.getY(), b.getY()), Math.max(a.getZ(), b.getZ()));
    }

    public boolean hasRegion() { return regionMin != null && regionMax != null; }

    public boolean contains(Location loc) {
        if (!hasRegion() || loc == null || loc.getWorld() == null) return false;
        if (!loc.getWorld().equals(regionMin.getWorld())) return false;
        return loc.getX() >= regionMin.getX() && loc.getX() <= regionMax.getX()
                && loc.getY() >= regionMin.getY() && loc.getY() <= regionMax.getY()
                && loc.getZ() >= regionMin.getZ() && loc.getZ() <= regionMax.getZ();
    }

    public boolean isReady() {
        return entrance != null && spawn != null;
    }

    /** Tên người/vật gác cửa, dùng cho lời thoại. */
    public String getGuardianName() {
        return switch (house) {
            case GRYFFINDOR -> "&dBà Béo";
            case SLYTHERIN  -> "&aBức tường đá";
            case RAVENCLAW  -> "&9Con đại bàng đồng";
            case HUFFLEPUFF -> "&eDãy thùng gỗ";
        };
    }
}
