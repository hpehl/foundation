package org.jboss.hal.dmr;

public class ValueEncoder {

    private static final String[][] SPECIAL_CHARACTERS = new String[][]{
            new String[]{"/", "\\/"},
            new String[]{":", "\\:"},
            new String[]{"=", "\\="},
    };

    public static String encode(String value) {
        String localValue = value;
        if (localValue != null && !localValue.isEmpty()) {
            boolean encode = false;
            for (String[] specialCharacter : SPECIAL_CHARACTERS) {
                if (localValue.contains(specialCharacter[0])) {
                    encode = true;
                    break;
                }
            }
            if (encode) {
                for (String[] special : SPECIAL_CHARACTERS) {
                    localValue = localValue.replace(special[0], special[1]);
                }
            }
        }
        return localValue;
    }

    public static String decode(String value) {
        String localValue = value;
        if (localValue != null && !localValue.isEmpty()) {
            boolean decode = false;
            for (String[] specialCharacter : SPECIAL_CHARACTERS) {
                if (localValue.contains(specialCharacter[1])) {
                    decode = true;
                    break;
                }
            }
            if (decode) {
                for (String[] special : SPECIAL_CHARACTERS) {
                    localValue = localValue.replace(special[1], special[0]);
                }
            }
        }
        return localValue;
    }
}
