package com.donnnsleep.hogwarts.model;

import java.util.Random;

/**
 * Đũa phép theo chuẩn Ollivander: loại gỗ + lõi + chiều dài + độ dẻo.
 * Lõi ảnh hưởng tới chỉ số phép thuật của người chơi.
 */
public class Wand {

    public enum Core {
        //                tên tiếng Việt              mana  sức   điều  màu chính  màu phụ    hạt
        PHOENIX_FEATHER("Lông đuôi Phượng hoàng", 1.15, 1.00, 1.10, "#FF9E2C", "#FFD98A", "&#FF9E2C"),
        DRAGON_HEARTSTRING("Sợi tim Rồng",        1.00, 1.25, 0.90, "#D63A2F", "#FF8A7A", "&#D63A2F"),
        UNICORN_HAIR("Lông Kỳ lân",               1.10, 0.90, 1.20, "#9FD8F0", "#E8F7FF", "&#9FD8F0");

        private final String vi;
        private final double manaMult;   // hệ số mana tối đa
        private final double powerMult;  // hệ số sát thương phép
        private final double controlMult;// hệ số giảm cooldown
        private final String hexChinh;   // màu đậm
        private final String hexPhu;     // màu nhạt
        private final String maMau;      // mã màu dùng trong chuỗi

        Core(String vi, double manaMult, double powerMult, double controlMult,
             String hexChinh, String hexPhu, String maMau) {
            this.vi = vi;
            this.manaMult = manaMult;
            this.powerMult = powerMult;
            this.controlMult = controlMult;
            this.hexChinh = hexChinh;
            this.hexPhu = hexPhu;
            this.maMau = maMau;
        }

        public String getVi() { return vi; }
        public String getHexChinh() { return hexChinh; }
        public String getHexPhu() { return hexPhu; }
        public String getMaMau() { return maMau; }

        /** Màu hạt phép bắn ra từ đầu đũa. */
        public org.bukkit.Color getMauHat() {
            return switch (this) {
                case PHOENIX_FEATHER -> org.bukkit.Color.fromRGB(255, 158, 44);
                case DRAGON_HEARTSTRING -> org.bukkit.Color.fromRGB(214, 58, 47);
                case UNICORN_HAIR -> org.bukkit.Color.fromRGB(159, 216, 240);
            };
        }
        public double getManaMult() { return manaMult; }
        public double getPowerMult() { return powerMult; }
        public double getControlMult() { return controlMult; }
    }

    /** Các loại gỗ làm đũa trong nguyên tác, kèm tên tiếng Việt. */
    public static final String[][] WOODS = {
            {"Holly",      "Nhựa ruồi"},
            {"Vine",       "Dây nho"},
            {"Yew",        "Thuỷ tùng"},
            {"Elder",      "Cơm cháy"},
            {"Willow",     "Liễu"},
            {"Oak",        "Sồi"},
            {"Walnut",     "Óc chó"},
            {"Ash",        "Tần bì"},
            {"Hawthorn",   "Táo gai"},
            {"Hornbeam",   "Trăn"},
            {"Chestnut",   "Hạt dẻ"},
            {"Cherry",     "Anh đào"},
            {"Ebony",      "Mun"},
            {"Cypress",    "Bách"},
            {"Maple",      "Phong"},
            {"Rosewood",   "Cẩm lai"},
            {"Blackthorn", "Mận gai"},
            {"Alder",      "Tổng quán sủi"},
            {"Beech",      "Dẻ gai"},
            {"Elm",        "Du"}
    };

    /** Trả về tên tiếng Việt của một loại gỗ, kèm tên gốc trong ngoặc. */
    public static String woodDisplay(String wood) {
        for (String[] w : WOODS) {
            if (w[0].equalsIgnoreCase(wood)) return w[1] + " (" + w[0] + ")";
        }
        return wood;
    }

    private static final String[] FLEX = {
            "cứng", "hơi cứng", "dẻo vừa", "dẻo", "rất dẻo", "giòn"
    };

    private final String wood;
    private final Core core;
    private final double length;      // inch
    private final String flexibility;

    public Wand(String wood, Core core, double length, String flexibility) {
        this.wood = wood;
        this.core = core;
        this.length = length;
        this.flexibility = flexibility;
    }

    public static Wand random(Random rng) {
        String wood = WOODS[rng.nextInt(WOODS.length)][0];
        Core core = Core.values()[rng.nextInt(Core.values().length)];
        double length = Math.round((9.0 + rng.nextDouble() * 5.0) * 4) / 4.0; // 9.00 - 14.00
        String flex = FLEX[rng.nextInt(FLEX.length)];
        return new Wand(wood, core, length, flex);
    }

    public String getWood() { return wood; }
    public Core getCore() { return core; }
    public double getLength() { return length; }
    public String getFlexibility() { return flexibility; }

    /** Ví dụ: "Nhựa ruồi (Holly), lông đuôi Phượng hoàng, 11.00 inch, dẻo vừa" */
    public String describe() {
        return String.format("%s, %s, %.2f inch, %s",
                woodDisplay(wood), core.getVi(), length, flexibility);
    }

    public String serialize() {
        return wood + ";" + core.name() + ";" + length + ";" + flexibility;
    }

    public static Wand deserialize(String s) {
        if (s == null || s.isEmpty()) return null;
        String[] p = s.split(";");
        if (p.length < 4) return null;
        try {
            return new Wand(p[0], Core.valueOf(p[1]), Double.parseDouble(p[2]), p[3]);
        } catch (Exception e) {
            return null;
        }
    }
}
