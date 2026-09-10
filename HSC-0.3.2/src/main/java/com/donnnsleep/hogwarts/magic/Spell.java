package com.donnnsleep.hogwarts.magic;

/**
 * Một câu thần chú. Tên và công dụng giữ đúng nguyên tác.
 */
public record Spell(
        String id,            // định danh nội bộ: "expelliarmus"
        String incantation,   // câu chú đọc lên: "Expelliarmus"
        String viName,        // tên tiếng Việt: "Bùa Tước Vũ Khí"
        String description,   // mô tả ngắn
        int year,             // năm học được phép học
        String subject,       // môn học: Charms / DADA / Transfiguration / Potions
        double manaCost,
        long cooldownMs,
        boolean unforgivable  // Lời nguyền Không thể Tha thứ
) {
    public String displayColor() {
        if (unforgivable) return "&4";
        return switch (subject) {
            case "Charms" -> "&b";
            case "DADA" -> "&d";
            case "Transfiguration" -> "&6";
            case "Potions" -> "&2";
            default -> "&f";
        };
    }
}
