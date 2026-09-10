package com.donnnsleep.hogwarts.model;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;

/**
 * Bốn Nhà của Hogwarts, giữ nguyên tên và đặc trưng theo nguyên tác.
 */
public enum House {

    GRYFFINDOR("Gryffindor", "Sư Tử", NamedTextColor.RED, Material.RED_BANNER,
            new String[]{"can đảm", "gan dạ", "hào hiệp", "liều lĩnh"}),

    HUFFLEPUFF("Hufflepuff", "Lửng", NamedTextColor.YELLOW, Material.YELLOW_BANNER,
            new String[]{"trung thành", "kiên nhẫn", "công bằng", "chăm chỉ"}),

    RAVENCLAW("Ravenclaw", "Đại Bàng", NamedTextColor.BLUE, Material.BLUE_BANNER,
            new String[]{"thông thái", "sáng tạo", "ham học", "sắc sảo"}),

    SLYTHERIN("Slytherin", "Rắn", NamedTextColor.GREEN, Material.GREEN_BANNER,
            new String[]{"tham vọng", "mưu lược", "quyết đoán", "tháo vát"});

    private final String displayName;
    private final String animal;
    private final NamedTextColor color;
    private final Material banner;
    private final String[] traits;

    House(String displayName, String animal, NamedTextColor color, Material banner, String[] traits) {
        this.displayName = displayName;
        this.animal = animal;
        this.color = color;
        this.banner = banner;
        this.traits = traits;
    }

    public String getDisplayName() { return displayName; }
    public String getAnimal() { return animal; }
    public NamedTextColor getColor() { return color; }
    public Material getBanner() { return banner; }
    public String[] getTraits() { return traits; }

    public String getLegacyColor() {
        return switch (this) {
            case GRYFFINDOR -> "&c";
            case HUFFLEPUFF -> "&e";
            case RAVENCLAW  -> "&9";
            case SLYTHERIN  -> "&a";
        };
    }

    public static House fromString(String s) {
        if (s == null) return null;
        for (House h : values()) {
            if (h.name().equalsIgnoreCase(s)) return h;
        }
        return null;
    }
}
