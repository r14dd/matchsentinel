package com.matchsentinel.cases.util;

import java.util.Collections;
import java.util.List;

public final class ReasonsCodec {
    private static final String DELIMITER = "|";

    private ReasonsCodec() {
    }

    public static String encode(List<String> reasons) {
        if (reasons == null || reasons.isEmpty()) {
            return null;
        }
        return String.join(DELIMITER, reasons);
    }

    public static List<String> decode(String reasons) {
        if (reasons == null || reasons.isBlank()) {
            return Collections.emptyList();
        }
        return List.of(reasons.split("\\Q" + DELIMITER + "\\E"));
    }
}
