package com.bot.common.utils;

import java.util.List;

public class TeListUtil {
    public static <T> boolean isNotEmptyAndNull(List<T> list) {
        return list != null && !list.isEmpty();
    }
}
