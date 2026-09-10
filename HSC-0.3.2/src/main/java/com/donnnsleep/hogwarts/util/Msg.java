package com.donnnsleep.hogwarts.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public final class Msg {

    /**
     * Bộ chuyển đổi hỗ trợ cả mã màu cũ (&a, &c...) lẫn mã hex (&#FFD54F).
     */
    private static final LegacyComponentSerializer LEGACY =
            LegacyComponentSerializer.builder()
                    .character('&')
                    .hexCharacter('#')
                    .hexColors()
                    .build();

    private Msg() {}

    /** Chuyển chuỗi có mã màu &amp; hoặc hex &amp;#RRGGBB thành Component. */
    public static Component color(String s) {
        return LEGACY.deserialize(s == null ? "" : s);
    }

    /** Dòng kẻ trang trí. */
    public static Component line() {
        return color("&8&m                                                  ");
    }

    public static String prefix() {
        return "&5[&dHogwarts&5] &r";
    }

    public static Component msg(String s) {
        return color(prefix() + s);
    }
}
