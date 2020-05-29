package com.lantian.base.mongl;

/*
 * Mongol Code
 *
 * Updated for Unicode 10.0 standards
 * http://unicode.org/charts/PDF/U1800.pdf
 * Deviating from Unicode 10.0 for
 *    - MONGOLIAN LETTER GA first and second form final (matching DS01, needed to
 *     break context) (So words with only I default to feminine. Menksoft also
 *     does this.)
 *    - MONGOLIAN LETTER I, third medial form. Undefined in Unicode 10. This is a
 *     single tooth I after a vowel. Needed to break context. Menksoft also
 *     does this.
 *
 * The purpose of this class is to render Unicode text into glyphs
 * that can be displayed on all versions of Android. It solves the
 * problem of Mongolian script not being supported before Android 6.0,
 * and problems with Unicode rendering after Android 6.0.
 *
 * Current version needs to be used with Menksoft font glyphs located
 * in the PUA starting at \uE234. It is recommended that all external
 * text use Unicode. However, Menksoft code can also be converted back
 * into Unicode.
 */
@SuppressWarnings({"unused", "WeakerAccess", "SwitchStatementWithTooFewBranches"})
public final class MongolCode {

    // this is a singleton class (should it just be a static class?)
    public final static MongolCode INSTANCE = new MongolCode();
    private final static char SPACE = ' ';

    public enum Location {
        ISOLATE, INITIAL, MEDIAL, FINAL
    }

    public enum Gender {
        MASCULINE, FEMININE, NEUTER
    }

    // Constructor
    private MongolCode() {
    }

    public String unicodeToMenksoft(CharSequence inputString) {
        String menksoftWithSpacingChars = unicodeToMenksoftSameIndex(inputString);
        return stripControlChars(menksoftWithSpacingChars);
    }

    private String stripControlChars(String stringWithControlChars) {
        int length = stringWithControlChars.length();
        StringBuilder strippedString = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            if (!shouldBeStripped(stringWithControlChars, i)) {
                strippedString.append(stringWithControlChars.charAt(i));
            }
        }
        return strippedString.toString();
    }

    private boolean shouldBeStripped(String text, int index) {
        char thisChar = text.charAt(index);
        //noinspection SimplifiableIfStatement
        if (isNonPrintingChar(thisChar)) {
            // old Menksoft code (in PUA region) context doesn't need
            // Unicode control characters. But keep control characters
            // in the context of Unicode text. (This allows font to render
            // TodoScript text.)
            return (index != 0 && isPuaChar(text.charAt(index - 1)) ||
                    index != text.length() - 1 && isPuaChar(text.charAt(index + 1)));
        }
        return false;
    }

    private boolean isPuaChar(char character) {
        return character >= '\uE000' && character <= '\uF8FF';
    }

    private boolean isNonPrintingChar(char character) {
        return character == Uni.MVS ||
                character == Uni.ZWJ ||
                character == Uni.ZWNJ ||
                character == Uni.WJ ||
                character == Uni.FVS1 ||
                character == Uni.FVS2 ||
                character == Uni.FVS3;
    }

    String unicodeToMenksoftSameIndex(CharSequence inputString) {

        if (inputString == null || inputString.length() == 0) return "";

        StringBuilder outputString = new StringBuilder();
        StringBuilder mongolWord = new StringBuilder();

        // Loop through characters in string
        int length = inputString.length();
        for (int i = 0; i < length; i++) {
            final char character = inputString.charAt(i);
            if (isMongolian(character)) {
                mongolWord.append(character);
                continue;
            }

            if (mongolWord.length() > 0) {
                appendMongolWord(outputString, mongolWord);
                mongolWord.setLength(0);
            }

            // NNBS starts a new Mongol word but is not itself a Mongol char
            if (character == Uni.NNBS) {
                mongolWord.append(Uni.NNBS);
                continue;
            }

            if (isConvertiblePunctuation(character)) {
                char menksoftPunctuation = MongolWord.convertPunctuationToMenksoftCode(character);
                outputString.append(menksoftPunctuation);
                continue;
            }

            // non-Mongol character
            outputString.append(character);
        }

        // Add any final substring
        if (mongolWord.length() > 0)
            appendMongolWord(outputString, mongolWord);

        return outputString.toString();
    }

    private void appendMongolWord(StringBuilder outputString, StringBuilder mongolWord) {
        String renderedWord = new MongolWord(mongolWord).convertToMenksoftCode();
        outputString.append(renderedWord);
    }

    public String menksoftToUnicode(String inputString) {

        if (inputString == null || inputString.length() == 0) return "";

        StringBuilder outputString = new StringBuilder();
        StringBuilder menksoftWord = new StringBuilder();

        // Loop through characters in string
        int length = inputString.length();
        for (int i = 0; i < length; i++) {
            final char character = inputString.charAt(i);
            if (isMenksoft(character) && !isMenksoftSpace(character)) {
                menksoftWord.append(character);
                continue;
            }

            if (menksoftWord.length() > 0) {
                appendMenksoftWord(outputString, menksoftWord);
                menksoftWord.setLength(0);
            }

            // A space starts a new Mongol word but is not itself a Mongol char
            if (isMenksoftSpace(character)) {
                menksoftWord.append(character);
                continue;
            }

            // non-Menksoft character
            outputString.append(character);
        }

        // Add any final substring
        if (menksoftWord.length() > 0)
            appendMenksoftWord(outputString, menksoftWord);

        return outputString.toString();
    }

    private boolean isMenksoftSpace(char character) {
        return character == Glyph.SUFFIX_SPACE
                || character == Glyph.UNKNOWN_SPACE
                || character == SPACE;
    }

    private void appendMenksoftWord(StringBuilder outputString, StringBuilder menksoftWord) {
        String unicodeWord = new MenksoftWord(menksoftWord).convertToUnicode();
        outputString.append(unicodeWord);
    }

    public static Location getLocation(CharSequence textBefore, CharSequence textAfter) {

        // TODO should we be using this in convertWordToMenksoftCode?

        if (textBefore == null) textBefore = "";
        if (textAfter == null) textAfter = "";

        boolean beforeIsMongolian = false;
        boolean afterIsMongolian = false;

        int length = textBefore.length();
        if (length > 0 && isMongolian(textBefore.charAt(length - 1))) {
            beforeIsMongolian = true;
        }

        length = textAfter.length();

        for (int i = 0; i < length; i++) {
            char currentChar = textAfter.charAt(i);
            if (isFVS(currentChar) || currentChar == Uni.MVS) {
                continue;
            } else if (isMongolian(currentChar)) {
                afterIsMongolian = true;
            }
            break;
        }

        if (beforeIsMongolian && afterIsMongolian) return Location.MEDIAL;
        else if (!beforeIsMongolian && afterIsMongolian) return Location.INITIAL;
        else if (beforeIsMongolian) return Location.FINAL;
        else return Location.ISOLATE;
    }

    public static boolean isMenksoft(char character) {
        return character >= Glyph.MENKSOFT_START && character <= Glyph.MENKSOFT_END;
    }

    private boolean isConvertiblePunctuation(char character) {
        return isVerticalPresentationForm(character)
                || isMongolianPunctuation(character)
                || isMongolianDigit(character)
                || character == Uni.MIDDLE_DOT
                || character == Uni.REFERENCE_MARK
                || character == Uni.QUESTION_EXCLAMATION_MARK
                || character == Uni.EXCLAMATION_QUESTION_MARK
                || character == Uni.PUNCTUATION_X;
    }

    private boolean isVerticalPresentationForm(char character) {
        return (character >= Uni.VERTICAL_COMMA
                && character <= Uni.VERTICAL_RIGHT_SQUARE_BRACKET);
    }

    private boolean isMongolianPunctuation(char character) {
        return (character >= Uni.MONGOLIAN_BIRGA
                && character <= Uni.MONGOLIAN_MANCHU_FULL_STOP);
    }

    private boolean isMongolianDigit(char character) {
        return (character >= Uni.MONGOLIAN_DIGIT_ZERO
                && character <= Uni.MONGOLIAN_DIGIT_NINE);
    }

    public static boolean isVowel(char character) {
        return (character >= Uni.A && character <= Uni.EE);
    }

    public static boolean isMasculineVowel(char character) {
        return (character == Uni.A || character == Uni.O || character == Uni.U);
    }

    public static boolean isFeminineVowel(char character) {
        return (character == Uni.E || character == Uni.EE || character == Uni.OE || character == Uni.UE);
    }

    public static boolean isConsonant(char character) {
        return (character >= Uni.NA && character <= Uni.CHI);
    }

    public static boolean isFVS(char character) {
        return (character >= Uni.FVS1 && character <= Uni.FVS3);
    }

    /**
     * Test if character is Mongolian
     * Sibe/Manchu/Aligali are currently undefined (may or may not be handled in the future)
     *
     * @param character the character to test
     * @return true if Mongolian/TodoScript letters, MVS, FVS1-3, NIRUGU, ZWJ, ZWNJ, (but not NNBS)
     */
    public static boolean isMongolian(char character) {
        return (isBasicMongolianAlphabet(character) || isTodoAlphabet(character)
                || (character >= Uni.MONGOLIAN_NIRUGU && character <= Uni.MVS)
                || character == Uni.ZWJ || character == Uni.ZWNJ);
    }

    private static boolean isBasicMongolianAlphabet(char character) {
        return character >= Uni.A && character <= Uni.CHI;
    }

    private static boolean isTodoAlphabet(char character) {
        return character >= Uni.TODO_LONG_VOWEL_SIGN && character <= Uni.TODO_DZA;
    }

    private static boolean isBGDRS(char character) {
        return (character == Uni.BA || character == Uni.GA || character == Uni.DA
                || character == Uni.RA || character == Uni.SA);
    }

    /**
     * An MVS (Mongolian Vowel Separator) only appears before an A or E and after certain
     * characters (usually consonants but could come after O as in CHINO_A (wolf)). This
     * method tests the preceding character to see whether an MVS could follow it.
     *
     * @param character the character to text
     * @return whether an MVS could follow the given character
     */
    public static boolean isMvsPrecedingChar(char character) {
        return (character == Uni.NA || character == Uni.QA || character == Uni.GA
                || character == Uni.MA || character == Uni.LA || character == Uni.JA
                || character == Uni.YA || character == Uni.RA || character == Uni.WA
                || character == Uni.O || character == Uni.U || character == Uni.OE
                || character == Uni.UE);
    }

    // YIN comes after a vowel, UN comes after a consonant, U comes after N.
    public static String getSuffixYinUnU(Gender previousWordGender, char previousWordLastChar) {
        if (isVowel(previousWordLastChar)) {
            return Suffix.YIN;
        } else if (previousWordLastChar == Uni.NA) {
            if (previousWordGender == Gender.MASCULINE) {
                return Suffix.U;
            } else {
                return Suffix.UE;
            }
        } else {
            if (previousWordGender == Gender.MASCULINE) {
                return Suffix.UN;
            } else {
                return Suffix.UEN;
            }
        }
    }

    // TU after B, G, D, R, S. Others are DU.
    public static String getSuffixTuDu(Gender previousWordGender, char previousWordLastChar) {
        if (isBGDRS(previousWordLastChar)) {
            if (previousWordGender == Gender.MASCULINE) {
                return Suffix.TU;
            } else {
                return Suffix.TUE;
            }
        } else {
            if (previousWordGender == Gender.MASCULINE) {
                return Suffix.DU;
            } else {
                return Suffix.DUE;
            }
        }
    }

    public static String getSuffixTaganDagan(Gender previousWordGender, char previousWordLastChar) {
        if (isBGDRS(previousWordLastChar)) {
            if (previousWordGender == Gender.MASCULINE) {
                return Suffix.TAGAN;
            } else {
                return Suffix.TEGEN;
            }
        } else {
            if (previousWordGender == Gender.MASCULINE) {
                return Suffix.DAGAN;
            } else {
                return Suffix.DEGEN;
            }
        }
    }

    public static String getSuffixTaqiDaqi(Gender previousWordGender, char previousWordLastChar) {
        if (isBGDRS(previousWordLastChar)) {
            if (previousWordGender == Gender.MASCULINE) {
                return Suffix.TAQI;
            } else {
                return Suffix.TEQI;
            }
        } else {
            if (previousWordGender == Gender.MASCULINE) {
                return Suffix.DAQI;
            } else {
                return Suffix.DEQI;
            }
        }
    }

    // Yi comes after a vowel, I comes after a consonant.
    public static String getSuffixYiI(char previousWordLastChar) {
        if (isVowel(previousWordLastChar)) {
            return Suffix.YI;
        }
        return Suffix.I;
    }

    // BAR comes after a vowel, IYAR comes after a consonant.
    public static String getSuffixBarIyar(Gender previousWordGender, char previousWordLastChar) {
        if (isVowel(previousWordLastChar)) {
            if (previousWordGender == Gender.MASCULINE) {
                return Suffix.IYAR;
            } else {
                return Suffix.IYER;
            }
        } else {
            if (previousWordGender == Gender.MASCULINE) {
                return Suffix.BAR;
            } else {
                return Suffix.BER;
            }
        }
    }

    // BAN comes after a vowel, IYAN comes after a consonant.
    public static String getSuffixBanIyan(Gender previousWordGender, char previousWordLastChar) {
        if (isVowel(previousWordLastChar)) {
            if (previousWordGender == Gender.MASCULINE) {
                return Suffix.IYAN;
            } else {
                return Suffix.IYEN;
            }
        } else {
            if (previousWordGender == Gender.MASCULINE) {
                return Suffix.BAN;
            } else {
                return Suffix.BEN;
            }
        }
    }

    public static String getSuffixAchaEche(Gender previousWordGender) {
        if (previousWordGender == Gender.MASCULINE) {
            return Suffix.ACHA;
        } else {
            return Suffix.ECHE;
        }
    }

    public static String getSuffixTaiTei(Gender previousWordGender) {
        if (previousWordGender == Gender.MASCULINE) {
            return Suffix.TAI;
        } else {
            return Suffix.TEI;
        }
    }

    public static String getSuffixUu(Gender previousWordGender) {
        if (previousWordGender == Gender.MASCULINE) {
            return Suffix.UU;
        } else {
            return Suffix.UEUE;
        }
    }

    public static String getSuffixUd(Gender previousWordGender) {
        if (previousWordGender == Gender.MASCULINE) {
            return Suffix.UD;
        } else {
            return Suffix.UED;
        }
    }

    public static String getSuffixNugud(Gender previousWordGender) {
        if (previousWordGender == Gender.MASCULINE) {
            return Suffix.NUGUD;
        } else {
            return Suffix.NUEGUED;
        }
    }

    public static String getSuffixChu(Gender previousWordGender) {
        if (previousWordGender == Gender.MASCULINE) {
            return Suffix.CHU;
        } else {
            return Suffix.CHUE;
        }
    }

    // Starts at the end of the word and works up
    // if mixed genders only reports the first one from the bottom
    // returns null if word does not end in a valid Mongolian character
    public static Gender getWordGender(String word) {
        return MongolWord.getGender(word);
    }

    @SuppressWarnings({"unused", "WeakerAccess"})
    public class Uni {


        public static final char WJ = '\u2060'; // Word joiner (replaces deprecated Zero-width no-break space)
        //public static final char ZWS = '\u200B'; // Zero-width space
        public static final char ZWNJ = '\u200C'; // Zero-width non joiner
        public static final char ZWJ = '\u200D'; // Zero-width joiner
        public static final char NNBS = '\u202F'; // Narrow No-Break Space

        // vertical presentation forms
        public static final char VERTICAL_COMMA = '\uFE10';
        public static final char VERTICAL_IDEOGRAPHIC_COMMA = '\uFE11';
        public static final char VERTICAL_IDEOGRAPHIC_FULL_STOP = '\uFE12';
        public static final char VERTICAL_COLON = '\uFE13';
        public static final char VERTICAL_SEMICOLON = '\uFE14';
        public static final char VERTICAL_EXCLAMATION_MARK = '\uFE15';
        public static final char VERTICAL_QUESTION_MARK = '\uFE16';
        public static final char VERTICAL_LEFT_WHITE_LENTICULAR_BRACKET = '\uFE17';
        public static final char VERTICAL_RIGHT_WHITE_LENTICULAR_BRAKCET = '\uFE18';
        public static final char VERTICAL_HORIZONTAL_ELLIPSIS = '\uFE19';
        public static final char VERTICAL_TWO_DOT_LEADER = '\uFE30';
        public static final char VERTICAL_EM_DASH = '\uFE31';
        public static final char VERTICAL_EN_DASH = '\uFE32';
        public static final char VERTICAL_LOW_LINE = '\uFE33';
        public static final char VERTICAL_WAVY_LOW_LINE = '\uFE34';
        public static final char VERTICAL_LEFT_PARENTHESIS = '\uFE35';
        public static final char VERTICAL_RIGHT_PARENTHESIS = '\uFE36';
        public static final char VERTICAL_LEFT_CURLY_BRACKET = '\uFE37';
        public static final char VERTICAL_RIGHT_CURLY_BRACKET = '\uFE38';
        public static final char VERTICAL_LEFT_TORTOISE_SHELL_BRACKET = '\uFE39';
        public static final char VERTICAL_RIGHT_TORTOISE_SHELL_BRACKET = '\uFE3A';
        public static final char VERTICAL_LEFT_BLACK_LENTICULAR_BRACKET = '\uFE3B';
        public static final char VERTICAL_RIGHT_BLACK_LENTICULAR_BRACKET = '\uFE3C';
        public static final char VERTICAL_LEFT_DOUBLE_ANGLE_BRACKET = '\uFE3D';
        public static final char VERTICAL_RIGHT_DOUBLE_ANGLE_BRACKET = '\uFE3E';
        public static final char VERTICAL_LEFT_ANGLE_BRACKET = '\uFE3F';
        public static final char VERTICAL_RIGHT_ANGLE_BRACKET = '\uFE40';
        public static final char VERTICAL_LEFT_CORNER_BRACKET = '\uFE41';
        public static final char VERTICAL_RIGHT_CORNER_BRACKET = '\uFE42';
        public static final char VERTICAL_LEFT_WHITE_CORNER_BRACKET = '\uFE43';
        public static final char VERTICAL_RIGHT_WHITE_CORNER_BRACKET = '\uFE44';
        public static final char VERTICAL_LEFT_SQUARE_BRACKET = '\uFE47';
        public static final char VERTICAL_RIGHT_SQUARE_BRACKET = '\uFE48';

        // Other
        public static final char MIDDLE_DOT = '\u00B7';
        public static final char REFERENCE_MARK = '\u203B';
        public static final char DOUBLE_EXCLAMATION_MARK = '\u203C';
        public static final char DOUBLE_QUESTION_MARK = '\u2047';
        public static final char QUESTION_EXCLAMATION_MARK = '\u2048';
        public static final char EXCLAMATION_QUESTION_MARK = '\u2049';
        public static final char PUNCTUATION_X = '\u00D7'; // TODO is this right?


        // Unicode Mongolian Values
        public static final char MONGOLIAN_BIRGA = '\u1800';
        public static final char MONGOLIAN_ELLIPSIS = '\u1801';
        public static final char MONGOLIAN_COMMA = '\u1802';
        public static final char MONGOLIAN_FULL_STOP = '\u1803';
        public static final char MONGOLIAN_COLON = '\u1804';
        public static final char MONGOLIAN_FOUR_DOTS = '\u1805';
        public static final char MONGOLIAN_TODO_SOFT_HYPHEN = '\u1806';
        public static final char MONGOLIAN_SIBE_SYLLABLE_BOUNDARY_MARKER = '\u1807';
        public static final char MONGOLIAN_MANCHU_COMMA = '\u1808';
        public static final char MONGOLIAN_MANCHU_FULL_STOP = '\u1809';
        public static final char MONGOLIAN_NIRUGU = '\u180a';
        public static final char FVS1 = '\u180b';
        public static final char FVS2 = '\u180c';
        public static final char FVS3 = '\u180d';
        public static final char MVS = '\u180e'; // MONGOLIAN_VOWEL_SEPARATOR
        public static final char MONGOLIAN_DIGIT_ZERO = '\u1810';
        public static final char MONGOLIAN_DIGIT_ONE = '\u1811';
        public static final char MONGOLIAN_DIGIT_TWO = '\u1812';
        public static final char MONGOLIAN_DIGIT_THREE = '\u1813';
        public static final char MONGOLIAN_DIGIT_FOUR = '\u1814';
        public static final char MONGOLIAN_DIGIT_FIVE = '\u1815';
        public static final char MONGOLIAN_DIGIT_SIX = '\u1816';
        public static final char MONGOLIAN_DIGIT_SEVEN = '\u1817';
        public static final char MONGOLIAN_DIGIT_EIGHT = '\u1818';
        public static final char MONGOLIAN_DIGIT_NINE = '\u1819';
        public static final char A = '\u1820'; // MONGOLIAN_LETTER_xx
        public static final char E = '\u1821';
        public static final char I = '\u1822';
        public static final char O = '\u1823';
        public static final char U = '\u1824';
        public static final char OE = '\u1825';
        public static final char UE = '\u1826';
        public static final char EE = '\u1827';
        public static final char NA = '\u1828';
        public static final char ANG = '\u1829';
        public static final char BA = '\u182A';
        public static final char PA = '\u182B';
        public static final char QA = '\u182C';
        public static final char GA = '\u182D';
        public static final char MA = '\u182E';
        public static final char LA = '\u182F';
        public static final char SA = '\u1830';
        public static final char SHA = '\u1831';
        public static final char TA = '\u1832';
        public static final char DA = '\u1833';
        public static final char CHA = '\u1834';
        public static final char JA = '\u1835';
        public static final char YA = '\u1836';
        public static final char RA = '\u1837';
        public static final char WA = '\u1838';
        public static final char FA = '\u1839';
        public static final char KA = '\u183A';
        public static final char KHA = '\u183B';
        public static final char TSA = '\u183C';
        public static final char ZA = '\u183D';
        public static final char HAA = '\u183E';
        public static final char ZRA = '\u183F';
        public static final char LHA = '\u1840';
        public static final char ZHI = '\u1841';
        public static final char CHI = '\u1842';

        // Unicode TodoScript Mongolian Values
        public static final char TODO_LONG_VOWEL_SIGN = '\u1843';
        public static final char TODO_E = '\u1844';
        public static final char TODO_I = '\u1845';
        public static final char TODO_O = '\u1846';
        public static final char TODO_U = '\u1847';
        public static final char TODO_OE = '\u1848';
        public static final char TODO_UE = '\u1849';
        public static final char TODO_ANG = '\u184A';
        public static final char TODO_BA = '\u184B';
        public static final char TODO_PA = '\u184C';
        public static final char TODO_QA = '\u184D';
        public static final char TODO_GA = '\u184E';
        public static final char TODO_MA = '\u184F';
        public static final char TODO_TA = '\u1850';
        public static final char TODO_DA = '\u1851';
        public static final char TODO_CHA = '\u1852';
        public static final char TODO_JA = '\u1853';
        public static final char TODO_TSA = '\u1854';
        public static final char TODO_YA = '\u1855';
        public static final char TODO_WA = '\u1856';
        public static final char TODO_KA = '\u1857';
        public static final char TODO_GAA = '\u1858';
        public static final char TODO_HAA = '\u1859';
        public static final char TODO_JIA = '\u185A';
        public static final char TODO_NIA = '\u185B';
        public static final char TODO_DZA = '\u185C';
    }

    @SuppressWarnings({"unused", "WeakerAccess"})
    public class Suffix {
        public static final String YIN = "\u202F\u1836\u1822\u1828";
        public static final String UN = "\u202F\u1824\u1828";
        public static final String UEN = "\u202F\u1826\u1828";
        public static final String U = "\u202F\u1824";
        public static final String UE = "\u202F\u1826";
        public static final String I = "\u202F\u1822";
        public static final String YI = "\u202F\u1836\u1822";
        public static final String DU = "\u202F\u1833\u1824";
        public static final String DUE = "\u202F\u1833\u1826";
        public static final String TU = "\u202F\u1832\u1824";
        public static final String TUE = "\u202F\u1832\u1826";
        public static final String DUR = "\u202F\u1833\u1824\u1837";
        public static final String DUER = "\u202F\u1833\u1826\u1837";
        public static final String TUR = "\u202F\u1832\u1824\u1837";
        public static final String TUER = "\u202F\u1832\u1826\u1837";
        public static final String DAQI = "\u202F\u1833\u1820\u182C\u1822";
        public static final String DEQI = "\u202F\u1833\u1821\u182C\u1822";
        public static final String TAQI = "\u202F\u1832\u1820\u182C\u1822";
        public static final String TEQI = "\u202F\u1832\u1821\u182C\u1822";
        public static final String ACHA = "\u202F\u1820\u1834\u1820";
        public static final String ECHE = "\u202F\u1821\u1834\u1821";
        public static final String BAR = "\u202F\u182A\u1820\u1837";
        public static final String BER = "\u202F\u182A\u1821\u1837";
        public static final String IYAR = "\u202F\u1822\u1836\u1820\u1837";
        public static final String IYER = "\u202F\u1822\u1836\u1821\u1837";
        public static final String TAI = "\u202F\u1832\u1820\u1822";
        public static final String TEI = "\u202F\u1832\u1821\u1822";
        public static final String LUGA = "\u202F\u182F\u1824\u182D\u180E\u1820";
        public static final String LUEGE = "\u202F\u182F\u1826\u182D\u1821";
        public static final String BAN = "\u202F\u182A\u1820\u1828";
        public static final String BEN = "\u202F\u182A\u1821\u1828";
        public static final String IYAN = "\u202F\u1822\u1836\u1820\u1828";
        public static final String IYEN = "\u202F\u1822\u1836\u1821\u1828";
        public static final String YUGAN = "\u202F\u1836\u1824\u182D\u1820\u1828";
        public static final String YUEGEN = "\u202F\u1836\u1826\u182D\u1821\u1828";
        public static final String DAGAN = "\u202F\u1833\u1820\u182D\u1820\u1828";
        public static final String DEGEN = "\u202F\u1833\u1821\u182D\u1821\u1828";
        public static final String TAGAN = "\u202F\u1832\u1820\u182D\u1820\u1828";
        public static final String TEGEN = "\u202F\u1832\u1821\u182D\u1821\u1828";
        public static final String ACHAGAN = "\u202F\u1820\u1834\u1820\u182D\u1820\u1828";
        public static final String ECHEGEN = "\u202F\u1821\u1834\u1821\u182D\u1821\u1828";
        public static final String TAIGAN = "\u202F\u1832\u1820\u1822\u182D\u1820\u1828";
        public static final String TEIGEN = "\u202F\u1832\u1821\u1822\u182D\u1821\u1828";
        public static final String UD = "\u202F\u1824\u1833";
        public static final String UED = "\u202F\u1826\u1833";
        public static final String NUGUD = "\u202F\u1828\u1824\u182D\u1824\u1833";
        public static final String NUEGUED = "\u202F\u1828\u1826\u182D\u1826\u1833";
        public static final String NAR = "\u202F\u1828\u1820\u1837";
        public static final String NER = "\u202F\u1828\u1821\u1837";
        public static final String UU = "\u202F\u1824\u1824";
        public static final String UEUE = "\u202F\u1826\u1826";
        public static final String DA = "\u202F\u1833\u1820";
        public static final String DE = "\u202F\u1833\u1821";
        public static final String CHU = "\u202F\u1834\u1824";
        public static final String CHUE = "\u202F\u1834\u1826";

        // TODO should we add others?
        // urugu?
    }

    @SuppressWarnings("unused")
    class Glyph {

        static final char MENKSOFT_START = '\uE234';
        static final char MENKSOFT_END = '\uE34F';

        // Private Use Area glyph values
        static final char BIRGA = '\uE234';
        static final char ELLIPSIS = '\uE235';
        static final char COMMA = '\uE236';
        static final char FULL_STOP = '\uE237';
        static final char COLON = '\uE238';
        static final char FOUR_DOTS = '\uE239';
        static final char TODO_SOFT_HYPHEN = '\uE23A';
        static final char SIBE_SYLLABLE_BOUNDARY_MARKER = '\uE23B';
        static final char MANCHU_COMMA = '\uE23C';
        static final char MANCHU_FULL_STOP = '\uE23D';
        static final char NIRUGU = '\uE23E';
        static final char BIRGA_WITH_ORNAMENT = '\uE23F';
        static final char ROTATED_BIRGA = '\uE240';
        static final char DOUBLE_BIRGA_WITH_ORNAMENT = '\uE241';
        static final char TRIPLE_BIRGA_WITH_ORNAMENT = '\uE242';
        static final char MIDDLE_DOT = '\uE243';
        static final char ZERO = '\uE244';
        static final char ONE = '\uE245';
        static final char TWO = '\uE246';
        static final char THREE = '\uE247';
        static final char FOUR = '\uE248';
        static final char FIVE = '\uE249';
        static final char SIX = '\uE24A';
        static final char SEVEN = '\uE24B';
        static final char EIGHT = '\uE24C';
        static final char NINE = '\uE24D';
        static final char QUESTION_EXCLAMATION = '\uE24E';
        static final char EXCLAMATION_QUESTION = '\uE24F';
        static final char EXCLAMATION = '\uE250';
        static final char QUESTION = '\uE251';
        static final char SEMICOLON = '\uE252';
        static final char LEFT_PARENTHESIS = '\uE253';
        static final char RIGHT_PARENTHESIS = '\uE254';
        static final char LEFT_ANGLE_BRACKET = '\uE255';
        static final char RIGHT_ANGLE_BRACKET = '\uE256';
        static final char LEFT_TORTOISE_SHELL_BRACKET = '\uE257';
        static final char RIGHT_TORTOISE_SHELL_BRACKET = '\uE258';
        static final char LEFT_DOUBLE_ANGLE_BRACKET = '\uE259';
        static final char RIGHT_DOUBLE_ANGLE_BRACKET = '\uE25A';
        static final char LEFT_WHITE_CORNER_BRACKET = '\uE25B';
        static final char RIGHT_WHITE_CORNER_BRACKET = '\uE25C';
        static final char FULL_WIDTH_COMMA = '\uE25D';
        static final char X = '\uE25E';
        static final char REFERENCE_MARK = '\uE25F';                   // 0x203b
        static final char EN_DASH = '\uE260'; // TODO is that what this is?
        static final char EM_DASH = '\uE261'; // TODO is that what this is?
        static final char UNKNOWN_SPACE = '\uE262'; // TODO what is this?
        static final char SUFFIX_SPACE = '\uE263';

        // These are in the order of the Unicode 9 specs sheet
        // BP = looks better after B, P (and other rounded like Q, G, F, K, KH)
        // MVS = final glyph variant for MVS
        // gv = glyph variant, same basic glyph form as the one it follows.
        // TOOTH = the ending of this character matches a following character that slants left (for example, a tooth)
        // STEM = the ending of this character matches a following character that starts with a vertical stem
        // ROUND = the ending of this character matches a round following character (feminine QG)
        static final char A_START = '\uE264';
        static final char ISOL_A = '\uE264';
        static final char INIT_A = '\uE266';
        static final char MEDI_A = '\uE26C';
        static final char MEDI_A_BP = '\uE26D'; // gv
        static final char FINA_A = '\uE268';
        static final char FINA_A_BP = '\uE26B'; // final A following BPKF
        static final char ISOL_A_FVS1 = '\uE265';
        static final char MEDI_A_FVS1 = '\uE26E';
        static final char FINA_A_FVS1 = '\uE269';
        static final char FINA_A_MVS = '\uE26A'; // gv for MVS + A
        static final char MEDI_A_FVS2 = '\uE267'; // A of ACHA suffix
        static final char MEDI_A_UNKNOWN = '\uE26F';

        static final char E_START = '\uE270';
        static final char ISOL_E = '\uE270';
        static final char INIT_E = '\uE271';
        static final char MEDI_E = '\uE276';
        static final char MEDI_E_BP = '\uE277';
        static final char FINA_E = '\uE273';
        static final char FINA_E_BP = '\uE275'; // final E following BPKF
        static final char INIT_E_FVS1 = '\uE272';
        static final char FINA_E_FVS1 = '\uE269'; // no E glyph so using A
        static final char FINA_E_MVS = '\uE274'; // gv for MVS + E
        static final char MEDI_E_UNKNOWN = '\uE278';

        static final char I_START = '\uE279';
        static final char ISOL_I = '\uE279';
        static final char ISOL_I_SUFFIX = '\uE282';
        static final char INIT_I = '\uE27A';
        static final char MEDI_I = '\uE27E';
        static final char MEDI_I_SUFFIX = '\uE280';
        static final char MEDI_I_BP = '\uE27F'; // gv
        static final char MEDI_I_DOUBLE_TOOTH = '\uE281'; // gv
        static final char FINA_I = '\uE27B';
        static final char FINA_I_BP = '\uE27C'; // gv
        static final char MEDI_I_FVS1 = '\uE27D'; //

        static final char O_START = '\uE283';
        static final char ISOL_O = '\uE283';
        static final char INIT_O = '\uE284';
        static final char MEDI_O = '\uE289';
        static final char MEDI_O_BP = '\uE28A';
        static final char FINA_O = '\uE285';
        static final char FINA_O_BP = '\uE287'; // gv
        static final char MEDI_O_FVS1 = '\uE288';
        static final char FINA_O_FVS1 = '\uE286';

        static final char U_START = '\uE28B';  // Using Init U gliph
        static final char ISOL_U = '\uE28C';  // Using Init U gliph
        static final char INIT_U = '\uE28C';
        static final char MEDI_U = '\uE291';
        static final char MEDI_U_BP = '\uE292'; // gv
        static final char FINA_U = '\uE28D';
        static final char FINA_U_BP = '\uE28F'; // gv
        static final char MEDI_U_FVS1 = '\uE290';
        static final char FINA_U_FVS1 = '\uE28E';  // FIXME not defined in Unicode 10.0

        static final char OE_START = '\uE293';
        static final char ISOL_OE = '\uE293';
        static final char ISOL_OE_FVS1 = '\uE294'; // not defined in unicode
        static final char INIT_OE = '\uE295';
        static final char MEDI_OE = '\uE29E';
        static final char MEDI_OE_BP = '\uE29F'; // gv
        static final char FINA_OE = '\uE296';
        static final char FINA_OE_BP = '\uE29A'; // gv
        static final char MEDI_OE_FVS1 = '\uE29C';
        static final char MEDI_OE_FVS1_BP = '\uE29D';
        static final char FINA_OE_FVS1 = '\uE297';
        static final char FINA_OE_FVS1_BP = '\uE298'; // gv
        static final char FINA_OE_FVS2 = '\uE299'; // undefined in Unicode
        static final char MEDI_OE_FVS2 = '\uE29B';

        static final char UE_START = '\uE2A0';
        static final char ISOL_UE = '\uE2A2'; // Using initial glyph
        static final char INIT_UE = '\uE2A2';
        static final char MEDI_UE = '\uE2AB';
        static final char MEDI_UE_BP = '\uE2AC';
        static final char FINA_UE = '\uE2A3';
        static final char FINA_UE_BP = '\uE2A7';
        static final char ISOL_UE_FVS1 = '\uE2A1';
        static final char MEDI_UE_FVS1 = '\uE2A9';
        static final char MEDI_UE_FVS1_BP = '\uE2AA';
        static final char FINA_UE_FVS1 = '\uE2A4';
        static final char FINA_UE_FVS1_BP = '\uE2A5';
        static final char FINA_UE_FVS2 = '\uE2A6'; // undefined in unicode
        static final char MEDI_UE_FVS2 = '\uE2A8';

        static final char EE_START = '\uE2AD';
        static final char ISOL_EE = '\uE2AD';
        static final char INIT_EE = '\uE2AE';
        static final char MEDI_EE = '\uE2B0';
        static final char FINA_EE = '\uE2AF';

        static final char NA_START = '\uE2B1';
        static final char ISOL_NA = '\uE2B3';
        static final char INIT_NA_TOOTH = '\uE2B1';
        static final char INIT_NA_STEM = '\uE2B3';
        static final char MEDI_NA_TOOTH = '\uE2B8';
        static final char MEDI_NA_STEM = '\uE2BA';
        static final char MEDI_NA_NG = '\uE2C0';
        static final char FINA_NA = '\uE2B5';
        static final char INIT_NA_FVS1_TOOTH = '\uE2B2';
        static final char INIT_NA_FVS1_STEM = '\uE2B4';
        static final char MEDI_NA_FVS1_TOOTH = '\uE2B7';
        static final char MEDI_NA_FVS1_STEM = '\uE2B9';
        static final char MEDI_NA_FVS1_NG = '\uE2BF';
        static final char MEDI_NA_FVS2 = '\uE2B6'; // MVS
        static final char MEDI_NA_FVS3 = '\uE2B7'; // Tod Mongol N; FIXME: no glyph, substituting medial dotted n

        static final char ANG_START = '\uE2BB';
        static final char ISOL_ANG = '\uE2BC';
        static final char INIT_ANG_TOOTH = '\uE2BC';
        static final char INIT_ANG_ROUND = '\uE2BD';
        static final char INIT_ANG_STEM = '\uE2BE';
        static final char MEDI_ANG_TOOTH = '\uE2BC'; // good for following tooth
        static final char MEDI_ANG_ROUND = '\uE2BD'; // good for following round letter like B P H K
        static final char MEDI_ANG_STEM = '\uE2BE'; // good for following stem letter like J CH R
        static final char FINA_ANG = '\uE2BB';
        static final char ANG_END = '\uE2BE';

        static final char BA_START = '\uE2C1';
        static final char ISOL_BA = '\uE2C1';
        static final char INIT_BA = '\uE2C1';
        static final char INIT_BA_OU = '\uE2C2';
        static final char INIT_BA_STEM = '\uE2C7'; // using medial stem
        static final char MEDI_BA_TOOTH = '\uE2C5';
        static final char MEDI_BA_OU = '\uE2C6';
        static final char MEDI_BA_STEM = '\uE2C7';
        static final char FINA_BA = '\uE2C3';
        static final char FINA_BA_FVS1 = '\uE2C4';

        static final char PA_START = '\uE2C8';
        static final char ISOL_PA = '\uE2C8';
        static final char INIT_PA = '\uE2C8';
        static final char INIT_PA_OU = '\uE2C9';
        static final char INIT_PA_STEM = '\uE2CD'; // using medial stem
        static final char MEDI_PA_TOOTH = '\uE2CB';
        static final char MEDI_PA_OU = '\uE2CC';
        static final char MEDI_PA_STEM = '\uE2CD';
        static final char FINA_PA = '\uE2CA';

        static final char QA_START = '\uE2CE';
        static final char ISOL_QA = '\uE2D2';
        static final char INIT_QA_TOOTH = '\uE2CE';
        static final char INIT_QA_STEM = '\uE2D2';
        static final char INIT_QA_FEM = '\uE2D0'; // GV FEMININE
        static final char INIT_QA_FEM_OU = '\uE2D4';
        static final char MEDI_QA_TOOTH = '\uE2D8';
        static final char MEDI_QA_STEM = '\uE2DC';
        static final char MEDI_QA_FEM = '\uE2DA';
        static final char MEDI_QA_FEM_CONSONANT = '\uE2DF';
        static final char MEDI_QA_FEM_CONSONANT_DOTTED = '\uE2E0';
        static final char MEDI_QA_FEM_OU = '\uE2DD';
        static final char FINA_QA = '\uE2D6';
        static final char INIT_QA_FVS1_TOOTH = '\uE2CF';
        static final char INIT_QA_FVS1_STEM = '\uE2D3';
        static final char INIT_QA_FVS1_FEM = '\uE2D1';
        static final char INIT_QA_FVS1_FEM_OU = '\uE2D5';
        static final char MEDI_QA_FVS1 = '\uE2D9';
        static final char MEDI_QA_FVS1_FEM = '\uE2DB';
        static final char MEDI_QA_FVS1_FEM_OU = '\uE2DE';
        static final char ISOL_QA_FVS1 = '\uE2D1'; // feminine with 2 dots
        static final char MEDI_QA_FVS2 = '\uE2D7';
        static final char MEDI_QA_FVS3 = '\uE2D6';

        static final char GA_START = '\uE2E1';
        static final char ISOL_GA = '\uE2E4';
        static final char INIT_GA_TOOTH = '\uE2E1';
        static final char INIT_GA_STEM = '\uE2E4';
        static final char INIT_GA_FEM = '\uE2E3';
        static final char INIT_GA_FEM_OU = '\uE2E6';
        static final char MEDI_GA = '\uE2EE';
        static final char MEDI_GA_FEM = '\uE2EB';
        static final char MEDI_GA_FEM_OU = '\uE2ED';
        static final char FINA_GA = '\uE2E7';
        static final char INIT_GA_FVS1_TOOTH = '\uE2E2';
        static final char INIT_GA_FVS1_STEM = '\uE2E5';
        static final char MEDI_GA_FVS1_TOOTH = '\uE2EA';
        static final char MEDI_GA_FVS1_STEM = '\uE2EC';
        // This deviation is necessary to override context rules.
        // This follows the WG2 decision: https://r12a.github.io/mongolian-variants/
        static final char FINA_GA_FVS1 = '\uE2E7'; // masculine context override FIXME Deviating from Unicode 10.0 !!!
        static final char FINA_GA_FVS2 = '\uE2E8'; // feminine final form FIXME Deviating from Unicode 10.0 !!!
        static final char MEDI_GA_FVS2 = '\uE2E9';
        static final char MEDI_GA_FVS3_TOOTH = '\uE2EF';
        static final char MEDI_GA_FVS3_STEM = '\uE2F0';

        static final char MA_START = '\uE2F1';
        static final char ISOL_MA = '\uE2F2';
        static final char INIT_MA_TOOTH = '\uE2F1';
        static final char INIT_MA_STEM_LONG = '\uE2F2';
        static final char MEDI_MA_TOOTH = '\uE2F4';
        static final char MEDI_MA_STEM_LONG = '\uE2F5'; // long stem GV, use this if M or L follows
        static final char MEDI_MA_BP = '\uE2F6'; // GV use this if following a B, P, H, K, etc.
        static final char FINA_MA = '\uE2F3';

        static final char LA_START = '\uE2F7';
        static final char ISOL_LA = '\uE2F8';
        static final char INIT_LA_TOOTH = '\uE2F7';
        static final char INIT_LA_STEM_LONG = '\uE2F8';
        static final char MEDI_LA_TOOTH = '\uE2FA';
        static final char MEDI_LA_STEM_LONG = '\uE2FB'; // long stem GV, use this if M or L follows
        static final char MEDI_LA_BP = '\uE2FC'; // GV use this if following a B, P, H, K, etc.
        static final char FINA_LA = '\uE2F9';

        static final char SA_START = '\uE2FD';
        static final char ISOL_SA = '\uE2FE';
        static final char INIT_SA_TOOTH = '\uE2FD';
        static final char INIT_SA_STEM = '\uE2FE';
        static final char MEDI_SA_TOOTH = '\uE301';
        static final char MEDI_SA_STEM = '\uE302';
        static final char FINA_SA = '\uE2FF';
        static final char FINA_SA_FVS1 = '\uE300';
        static final char FINA_SA_FVS2 = '\uE2FF'; //0x100CE; FIXME: glyph is not in Menksoft PUA, substituting first form

        static final char SHA_START = '\uE303';
        static final char ISOL_SHA = '\uE304';
        static final char INIT_SHA_TOOTH = '\uE303';
        static final char INIT_SHA_STEM = '\uE304';
        static final char MEDI_SHA_TOOTH = '\uE306';
        static final char MEDI_SHA_STEM = '\uE307';
        static final char FINA_SHA = '\uE305';

        static final char TA_START = '\uE308';
        static final char ISOL_TA = '\uE309';
        static final char INIT_TA_TOOTH = '\uE308';
        static final char INIT_TA_STEM = '\uE309';
        static final char MEDI_TA = '\uE30B';
        static final char FINA_TA = '\uE30A';
        static final char MEDI_TA_FVS1_TOOTH = '\uE30C';
        static final char MEDI_TA_FVS1_STEM = '\uE30D';

        static final char DA_START = '\uE30E';
        static final char ISOL_DA = '\uE310';
        static final char INIT_DA_TOOTH = '\uE30E';
        static final char INIT_DA_STEM = '\uE30F';
        static final char MEDI_DA = '\uE314';
        static final char FINA_DA = '\uE311';
        static final char INIT_DA_FVS1 = '\uE310';
        static final char MEDI_DA_FVS1 = '\uE313';
        static final char FINA_DA_FVS1 = '\uE312';

        static final char CHA_START = '\uE315';
        static final char ISOL_CHA = '\uE315';
        static final char INIT_CHA = '\uE315';
        static final char MEDI_CHA = '\uE317';
        static final char FINA_CHA = '\uE316';

        static final char JA_START = '\uE318';
        static final char ISOL_JA = '\uE318';
        static final char INIT_JA_TOOTH = '\uE319';
        static final char INIT_JA_STEM = '\uE31A';
        static final char MEDI_JA = '\uE31D';
        static final char FINA_JA = '\uE31B';
        static final char MEDI_JA_FVS1 = '\uE31C'; // MVS

        static final char YA_START = '\uE31E';
        static final char ISOL_YA = '\uE31E';
        static final char INIT_YA = '\uE31E';
        //static final char MEDI_YA = '\uE320'; // hooked (Unicode 9.0)
        static final char MEDI_YA = '\uE321'; // straight (Unicode 10.0)
        static final char FINA_YA = '\uE31F';
        static final char INIT_YA_FVS1 = '\uE321';
        //static final char MEDI_YA_FVS1 = '\uE321'; // straight (Unicode 9.0)
        static final char MEDI_YA_FVS1 = '\uE320'; // hooked (Unicode 10.0)
        static final char MEDI_YA_FVS2 = '\uE31F';

        static final char RA_START = '\uE322';
        static final char ISOL_RA = '\uE322';
        static final char INIT_RA_TOOTH = '\uE323';
        static final char INIT_RA_STEM = '\uE322';
        static final char MEDI_RA_TOOTH = '\uE327';
        static final char MEDI_RA_STEM = '\uE326';
        static final char FINA_RA = '\uE325';

        static final char WA_START = '\uE329';
        static final char ISOL_WA = '\uE329';
        static final char INIT_WA = '\uE329';
        static final char MEDI_WA = '\uE32C';
        static final char FINA_WA = '\uE32A';
        static final char FINA_WA_FVS1 = '\uE32B'; // MVS

        static final char FA_START = '\uE32D';
        static final char ISOL_FA = '\uE32D';
        static final char INIT_FA = '\uE32D';
        static final char INIT_FA_OU = '\uE32E';
        static final char INIT_FA_STEM = '\uE332'; // using medial stem
        static final char MEDI_FA_TOOTH = '\uE330';
        static final char MEDI_FA_OU = '\uE331';
        static final char MEDI_FA_STEM = '\uE332';
        static final char FINA_FA = '\uE32F';

        static final char KA_START = '\uE333';
        static final char ISOL_KA = '\uE333';
        static final char INIT_KA = '\uE333';
        static final char INIT_KA_OU = '\uE334';
        static final char MEDI_KA_TOOTH = '\uE336';
        static final char MEDI_KA_OU = '\uE337';
        static final char MEDI_KA_STEM = '\uE338';
        static final char FINA_KA = '\uE335';

        static final char KHA_START = '\uE339';
        static final char ISOL_KHA = '\uE339';
        static final char INIT_KHA = '\uE339';
        static final char INIT_KHA_OU = '\uE33A';
        static final char MEDI_KHA_TOOTH = '\uE33C';
        static final char MEDI_KHA_OU = '\uE33D';
        static final char MEDI_KHA_STEM = '\uE33E';
        static final char FINA_KHA = '\uE33B';

        static final char TSA_START = '\uE33F';
        static final char ISOL_TSA = '\uE33F';
        static final char INIT_TSA = '\uE33F';
        static final char MEDI_TSA = '\uE341';
        static final char FINA_TSA = '\uE340';

        static final char ZA_START = '\uE342';
        static final char ISOL_ZA = '\uE342';
        static final char INIT_ZA = '\uE342';
        static final char MEDI_ZA = '\uE344';
        static final char FINA_ZA = '\uE343';

        static final char HAA_START = '\uE345';
        static final char ISOL_HAA = '\uE345';
        static final char INIT_HAA = '\uE345';
        static final char MEDI_HAA = '\uE347';
        static final char FINA_HAA = '\uE346';

        static final char ZRA_START = '\uE348';
        static final char ISOL_ZRA = '\uE348';
        static final char INIT_ZRA = '\uE348';
        static final char MEDI_ZRA = '\uE349';
        static final char FINA_ZRA = '\uE34A';

        static final char LHA_START = '\uE34B';
        static final char ISOL_LHA = '\uE34B';
        static final char INIT_LHA = '\uE34B';
        static final char MEDI_LHA = '\uE34C';
        static final char MEDI_LHA_BP = '\uE34D';
        static final char FINA_LHA = '\uE34C';
        static final char FINA_LHA_BP = '\uE34D';

        static final char ZHI_START = '\uE34E';
        static final char ISOL_ZHI = '\uE34E';
        static final char INIT_ZHI = '\uE34E';
        static final char MEDI_ZHI = '\uE34E';
        static final char FINA_ZHI = '\uE34E';

        static final char CHI_START = '\uE34F';
        static final char ISOL_CHI = '\uE34F';
        static final char INIT_CHI = '\uE34F';
        static final char MEDI_CHI = '\uE34F';
        static final char FINA_CHI = '\uE34F';

    }


    private static class MongolWord {

        // strange exception where the first UE does not get a long tooth
        private static final String BUU_EXCEPTION = "\u182A\u1826\u1826";

        private Gender gender;
        private Location location;
        private int length;
        private boolean isSuffix;
        private CharSequence inputWord;
        private char fvs;
        private Shape glyphShapeBelow;

        MongolWord(CharSequence mongolWord) {
            this.inputWord = mongolWord;
            this.gender = Gender.NEUTER;
            this.length = mongolWord.length();
            this.isSuffix = (mongolWord.charAt(0) == Uni.NNBS);
            this.fvs = 0;
            this.glyphShapeBelow = Shape.STEM;
        }

        private void updateLocation(int positionInWord, char charBelow) {
            if (positionInWord == 0) {
                if (length == 1 || (length == 2 && fvs > 0)) {
                    location = Location.ISOLATE;
                } else {
                    location = Location.INITIAL;
                }
            } else if (positionInWord == length - 1 || (positionInWord == length - 2 && fvs > 0)) {
                if (positionInWord == 1 && isSuffix) {
                    location = Location.ISOLATE;
                } else {
                    location = Location.FINAL;
                }
            } else {
                if (positionInWord == 1 && isSuffix) {
                    location = Location.INITIAL;
                } else if (charBelow == Uni.MVS) {
                    // treat character above MVS as a final by default
                    location = Location.FINAL;
                } else {
                    location = Location.MEDIAL;
                }
            }
        }

        private enum Shape {
            TOOTH,     // glyph slants to the left like a tooth (includes medial T/D, R, W, etc)
            STEM,      // glyph starts with a vertical stem (includes B, O/U, CH, etc)
            ROUND      // glyph top is round (includes feminine Q/G)
        }

        static char convertPunctuationToMenksoftCode(char punctuationChar) {
            switch (punctuationChar) {
                case Uni.VERTICAL_COMMA:
                    return Glyph.FULL_WIDTH_COMMA;
                case Uni.VERTICAL_COLON:
                    return Glyph.COLON;
                case Uni.VERTICAL_SEMICOLON:
                    return Glyph.SEMICOLON;
                case Uni.VERTICAL_EXCLAMATION_MARK:
                    return Glyph.EXCLAMATION;
                case Uni.VERTICAL_QUESTION_MARK:
                    return Glyph.QUESTION;
                case Uni.VERTICAL_HORIZONTAL_ELLIPSIS:
                    return Glyph.ELLIPSIS;
                case Uni.VERTICAL_EM_DASH:
                    return Glyph.EM_DASH;
                case Uni.VERTICAL_EN_DASH:
                    return Glyph.EN_DASH;
                case Uni.VERTICAL_LEFT_PARENTHESIS:
                    return Glyph.LEFT_PARENTHESIS;
                case Uni.VERTICAL_RIGHT_PARENTHESIS:
                    return Glyph.RIGHT_PARENTHESIS;
                case Uni.VERTICAL_LEFT_TORTOISE_SHELL_BRACKET:
                    return Glyph.LEFT_TORTOISE_SHELL_BRACKET;
                case Uni.VERTICAL_RIGHT_TORTOISE_SHELL_BRACKET:
                    return Glyph.RIGHT_TORTOISE_SHELL_BRACKET;
                case Uni.VERTICAL_LEFT_DOUBLE_ANGLE_BRACKET:
                    return Glyph.LEFT_DOUBLE_ANGLE_BRACKET;
                case Uni.VERTICAL_RIGHT_DOUBLE_ANGLE_BRACKET:
                    return Glyph.RIGHT_DOUBLE_ANGLE_BRACKET;
                case Uni.VERTICAL_LEFT_ANGLE_BRACKET:
                    return Glyph.LEFT_ANGLE_BRACKET;
                case Uni.VERTICAL_RIGHT_ANGLE_BRACKET:
                    return Glyph.RIGHT_ANGLE_BRACKET;
                case Uni.VERTICAL_LEFT_WHITE_CORNER_BRACKET:
                    return Glyph.LEFT_WHITE_CORNER_BRACKET;
                case Uni.VERTICAL_RIGHT_WHITE_CORNER_BRACKET:
                    return Glyph.RIGHT_WHITE_CORNER_BRACKET;
                case Uni.MIDDLE_DOT:
                    return Glyph.MIDDLE_DOT;
                case Uni.REFERENCE_MARK:
                    return Glyph.REFERENCE_MARK;
                case Uni.QUESTION_EXCLAMATION_MARK:
                    return Glyph.QUESTION_EXCLAMATION;
                case Uni.EXCLAMATION_QUESTION_MARK:
                    return Glyph.EXCLAMATION_QUESTION;
                case Uni.MONGOLIAN_BIRGA:
                    return Glyph.BIRGA;
                case Uni.MONGOLIAN_ELLIPSIS:
                    return Glyph.ELLIPSIS;
                case Uni.MONGOLIAN_COMMA:
                    return Glyph.COMMA;
                case Uni.MONGOLIAN_FULL_STOP:
                    return Glyph.FULL_STOP;
                case Uni.MONGOLIAN_COLON:
                    return Glyph.COLON;
                case Uni.MONGOLIAN_FOUR_DOTS:
                    return Glyph.FOUR_DOTS;
                case Uni.MONGOLIAN_TODO_SOFT_HYPHEN:
                    return Glyph.TODO_SOFT_HYPHEN;
                case Uni.MONGOLIAN_SIBE_SYLLABLE_BOUNDARY_MARKER:
                    return Glyph.SIBE_SYLLABLE_BOUNDARY_MARKER;
                case Uni.MONGOLIAN_MANCHU_COMMA:
                    return Glyph.MANCHU_COMMA;
                case Uni.MONGOLIAN_MANCHU_FULL_STOP:
                    return Glyph.MANCHU_FULL_STOP;
                case Uni.MONGOLIAN_DIGIT_ZERO:
                    return Glyph.ZERO;
                case Uni.MONGOLIAN_DIGIT_ONE:
                    return Glyph.ONE;
                case Uni.MONGOLIAN_DIGIT_TWO:
                    return Glyph.TWO;
                case Uni.MONGOLIAN_DIGIT_THREE:
                    return Glyph.THREE;
                case Uni.MONGOLIAN_DIGIT_FOUR:
                    return Glyph.FOUR;
                case Uni.MONGOLIAN_DIGIT_FIVE:
                    return Glyph.FIVE;
                case Uni.MONGOLIAN_DIGIT_SIX:
                    return Glyph.SIX;
                case Uni.MONGOLIAN_DIGIT_SEVEN:
                    return Glyph.SEVEN;
                case Uni.MONGOLIAN_DIGIT_EIGHT:
                    return Glyph.EIGHT;
                case Uni.MONGOLIAN_DIGIT_NINE:
                    return Glyph.NINE;
                case Uni.PUNCTUATION_X:
                    return Glyph.X;
                default:
                    return punctuationChar;
            }
        }

        String convertToMenksoftCode() {

            StringBuilder renderedWord = new StringBuilder();
            char charBelow = 0;
            char charBelowFvs = 0;

            // start at the bottom of the word and work up
            for (int i = length - 1; i >= 0; i--) {

                char charAbove;
                char currentChar = inputWord.charAt(i);

                // get the location
                updateLocation(i, charBelow);

                charAbove = (i > 0) ? inputWord.charAt(i - 1) : 0;

                // handle each letter separately
                switch (currentChar) {

                    case Uni.A:
                        handleA(renderedWord, charAbove);
                        break;
                    case Uni.E:
                        handleE(renderedWord, charAbove);
                        break;
                    case Uni.I:
                        handleI(renderedWord, i, charAbove, charBelow);
                        break;
                    case Uni.O:
                        handleO(renderedWord, charAbove);
                        break;
                    case Uni.U:
                        handleU(renderedWord, charAbove);
                        break;
                    case Uni.OE:
                        handleOE(renderedWord, i, charAbove);
                        break;
                    case Uni.UE:
                        handleUE(renderedWord, i, charAbove);
                        break;
                    case Uni.EE:
                        handleEE(renderedWord);
                        break;
                    case Uni.NA:
                        handleNA(renderedWord, i, charBelow, charBelowFvs);
                        break;
                    case Uni.ANG:
                        handleANG(renderedWord);
                        break;
                    case Uni.BA:
                        handleBA(renderedWord, charBelow);
                        break;
                    case Uni.PA:
                        handlePA(renderedWord, charBelow);
                        break;
                    case Uni.QA:
                        handleQA(renderedWord, i, charAbove, charBelow);
                        break;
                    case Uni.GA:
                        handleGA(renderedWord, i, charAbove, charBelow);
                        break;
                    case Uni.MA:
                        handleMA(renderedWord, i, charAbove, charBelow);
                        break;
                    case Uni.LA:
                        handleLA(renderedWord, i, charAbove, charBelow);
                        break;
                    case Uni.SA:
                        handleSA(renderedWord);
                        break;
                    case Uni.SHA:
                        handleSHA(renderedWord);
                        break;
                    case Uni.TA:
                        handleTA(renderedWord);
                        break;
                    case Uni.DA:
                        handleDA(renderedWord, charBelow);
                        break;
                    case Uni.CHA:
                        handleCHA(renderedWord);
                        break;
                    case Uni.JA:
                        handleJA(renderedWord, charBelow);
                        break;
                    case Uni.YA:
                        handleYA(renderedWord, i, charAbove, charBelow);
                        break;
                    case Uni.RA:
                        handleRA(renderedWord);
                        break;
                    case Uni.WA:
                        handleWA(renderedWord, charBelow);
                        break;
                    case Uni.FA:
                        handleFA(renderedWord, charBelow);
                        break;
                    case Uni.KA:
                        handleKA(renderedWord, charBelow);
                        break;
                    case Uni.KHA:
                        handleKHA(renderedWord, charBelow);
                        break;
                    case Uni.TSA:
                        handleTSA(renderedWord);
                        break;
                    case Uni.ZA:
                        handleZA(renderedWord);
                        break;
                    case Uni.HAA:
                        handleHAA(renderedWord);
                        break;
                    case Uni.ZRA:
                        handleZRA(renderedWord);
                        break;
                    case Uni.LHA:
                        handleLHA(renderedWord, i, charAbove);
                        break;
                    case Uni.ZHI:
                        handleZHI(renderedWord);
                        break;
                    case Uni.CHI:
                        handleCHI(renderedWord);
                        break;
                    case Uni.NNBS:
                        handleNNBS(renderedWord);
                        break;
                    case Uni.MONGOLIAN_NIRUGU:
                        handleNirugu(renderedWord);
                        break;
                    case Uni.ZWJ:
                    case Uni.ZWNJ:
                    case Uni.MVS:
                        handleNonPrintingChar(renderedWord);
                        break;
                    case Uni.FVS1:
                    case Uni.FVS2:
                    case Uni.FVS3:
                        handleNonPrintingChar(renderedWord);
                        fvs = currentChar;
                        continue;
                    default:

                        // don't render TodoScript words, the font can do that
                        if (isTodoAlphabet(currentChar))
                            return this.inputWord.toString();

                        // catch any other characters and just insert them directly
                        renderedWord.insert(0, currentChar);
                }

                charBelow = currentChar;
                charBelowFvs = fvs;
                fvs = 0;
            }

            return renderedWord.toString();
        }

        private void handleA(StringBuilder renderedWord, char charAbove) {
            gender = Gender.MASCULINE;
            switch (location) {
                case ISOLATE:
                    if (fvs == Uni.FVS1) {
                        renderedWord.insert(0, Glyph.ISOL_A_FVS1);     // left sweeping tail
                    } else {
                        renderedWord.insert(0, Glyph.ISOL_A);          // normal
                    }
                    break;
                case INITIAL:
                    if (isSuffix) {
                        renderedWord.insert(0, Glyph.MEDI_A_FVS2);     // A of ACHA   *** suffix rule ***
                    } else {
                        renderedWord.insert(0, Glyph.INIT_A);          // normal
                    }
                    break;
                case MEDIAL:
                    if (fvs == Uni.FVS1) {
                        renderedWord.insert(0, Glyph.MEDI_A_FVS1);     // 2 teeth
                    } else if (fvs == Uni.FVS2) {
                        renderedWord.insert(0, Glyph.MEDI_A_FVS2);     // A of ACHA suffix
                    } else {
                        if (isRoundLetter(charAbove)) {
                            renderedWord.insert(0, Glyph.MEDI_A_BP);   // After BPFK
                        } else {
                            renderedWord.insert(0, Glyph.MEDI_A);      // normal
                        }
                    }
                    glyphShapeBelow = Shape.TOOTH;
                    break;
                case FINAL:

                    if (fvs == Uni.FVS1) {
                        renderedWord.insert(0, Glyph.FINA_A_FVS1);     // left sweeping tail
                        glyphShapeBelow = Shape.STEM;
                    } else {
                        if (isRoundLetter(charAbove)) {
                            renderedWord.insert(0, Glyph.FINA_A_BP);   // after BPFK
                            glyphShapeBelow = Shape.TOOTH;
                        } else if (charAbove == Uni.MVS) {
                            renderedWord.insert(0, Glyph.FINA_A_MVS);  // MVS
                            glyphShapeBelow = Shape.STEM;
                        } else {
                            renderedWord.insert(0, Glyph.FINA_A);      // normal
                            glyphShapeBelow = Shape.STEM;
                        }
                    }
                    break;
            }
        }

        private void handleE(StringBuilder renderedWord, char charAbove) {
            gender = Gender.FEMININE;
            switch (location) {
                case ISOLATE:
                    renderedWord.insert(0, Glyph.ISOL_E);              // normal
                    break;
                case INITIAL:
                    if (fvs == Uni.FVS1) {
                        renderedWord.insert(0, Glyph.INIT_E_FVS1);     // double tooth
                    } else {
                        renderedWord.insert(0, Glyph.INIT_E);          // normal
                    }
                    break;
                case MEDIAL:
                    if (isRoundLetterIncludingQG(charAbove)) {
                        renderedWord.insert(0, Glyph.MEDI_E_BP);       // After BPFK
                    } else {
                        renderedWord.insert(0, Glyph.MEDI_E);          // normal
                    }
                    glyphShapeBelow = Shape.TOOTH;
                    break;
                case FINAL:

                    if (fvs == Uni.FVS1) {
                        renderedWord.insert(0, Glyph.FINA_E_FVS1);     // left sweeping tail
                        glyphShapeBelow = Shape.STEM;
                    } else {
                        if (isRoundLetterIncludingQG(charAbove)) {
                            renderedWord.insert(0, Glyph.FINA_E_BP);   // after BPFK
                            glyphShapeBelow = Shape.TOOTH;
                        } else if (charAbove == Uni.MVS) {
                            renderedWord.insert(0, Glyph.FINA_E_MVS);  // MVS
                            glyphShapeBelow = Shape.STEM;
                        } else {
                            renderedWord.insert(0, Glyph.FINA_E);      // normal
                            glyphShapeBelow = Shape.STEM;
                        }
                    }
                    break;
            }
        }

        private void handleI(StringBuilder renderedWord,
                             int positionInWord,
                             char charAbove,
                             char charBelow) {
            switch (location) {
                case ISOLATE:
                    if (isSuffix) {
                        renderedWord.insert(0, Glyph.ISOL_I_SUFFIX);           // I  *** suffix rule ***
                    } else {
                        renderedWord.insert(0, Glyph.ISOL_I);                  // normal
                    }
                    break;
                case INITIAL:
                    if (isSuffix && charBelow == Uni.YA) {
                        renderedWord.insert(0, Glyph.MEDI_I_SUFFIX);           // I of IYEN   *** suffix rule ***
                    } else {
                        renderedWord.insert(0, Glyph.INIT_I);                  // normal
                    }
                    break;
                case MEDIAL:

                    // FVS 1: one short, one long tooth
                    if (fvs == Uni.FVS1) {
                        renderedWord.insert(0, Glyph.MEDI_I_FVS1);
                        break;
                    }

                    // FVS 2:  Used to override context for NAIMA single tooth I
                    // (Undefined in Unicode 10.0)
                    if (fvs == Uni.FVS2) {
                        renderedWord.insert(0, Glyph.MEDI_I);    // normal
                        break;
                    }

                    // After BPFK
                    if (isRoundLetterIncludingQG(charAbove)) {
                        renderedWord.insert(0, Glyph.MEDI_I_BP);
                        break;
                    }

                    // *** AI, EI, OI, UI, OEI, UEI
                    // medial double tooth I diphthong rule ***
                    if (contextCallsForDoubleToothI(positionInWord, charAbove, charBelow)) {
                        renderedWord.insert(0, Glyph.MEDI_I_DOUBLE_TOOTH); // double tooth
                        break;
                    }

                    // normal single tooth I
                    renderedWord.insert(0, Glyph.MEDI_I);
                    break;
                case FINAL:
                    if (isRoundLetterIncludingQG(charAbove)) {
                        renderedWord.insert(0, Glyph.FINA_I_BP);               // after BPFK
                    } else {
                        renderedWord.insert(0, Glyph.FINA_I);                  // normal
                    }
                    break;
            }
            glyphShapeBelow = Shape.TOOTH;
        }

        private boolean contextCallsForDoubleToothI(int positionInWord, char charAbove, char charBelow) {
            if (charBelow == Uni.I) return false;
            if (charAbove == Uni.A ||
                    charAbove == Uni.E ||
                    charAbove == Uni.O ||
                    charAbove == Uni.U) return true;
            // or non long toothed OE/UE
            return ((charAbove == Uni.OE ||
                    charAbove == Uni.UE) &&
                    !needsLongToothU(inputWord, positionInWord - 1));
        }

        private void handleO(StringBuilder renderedWord, char charAbove) {
            gender = Gender.MASCULINE;
            switch (location) {
                case ISOLATE:
                    if (isSuffix) {
                        renderedWord.insert(0, Glyph.FINA_O);                  // O suffix   *** suffix rule ***
                    } else {
                        renderedWord.insert(0, Glyph.ISOL_O);                  // normal
                    }
                    break;
                case INITIAL:
                    if (isSuffix) {
                        renderedWord.insert(0, Glyph.MEDI_O_BP);               // O of OO suffix   *** suffix rule ***
                    } else {
                        renderedWord.insert(0, Glyph.INIT_O);                  // normal
                    }
                    break;
                case MEDIAL:
                    if (fvs == Uni.FVS1) {
                        renderedWord.insert(0, Glyph.MEDI_O_FVS1);             // tooth + O
                    } else {
                        if (isRoundLetter(charAbove)) {
                            renderedWord.insert(0, Glyph.MEDI_O_BP);           // After BPFK
                        } else {
                            renderedWord.insert(0, Glyph.MEDI_O);              // normal
                        }
                    }
                    break;
                case FINAL:
                    if (fvs == Uni.FVS1) {
                        renderedWord.insert(0, Glyph.FINA_O_FVS1);             // round o
                    } else {
                        if (isRoundLetter(charAbove)) {
                            renderedWord.insert(0, Glyph.FINA_O_BP);           // After BPFK
                        } else {
                            renderedWord.insert(0, Glyph.FINA_O);              // normal
                        }
                    }
                    break;
            }
            glyphShapeBelow = Shape.STEM;
        }

        private void handleU(StringBuilder renderedWord, char charAbove) {
            gender = Gender.MASCULINE;
            switch (location) {
                case ISOLATE:
                    if (isSuffix) {
                        renderedWord.insert(0, Glyph.FINA_U);                  // O suffix   *** suffix rule ***
                    } else {
                        renderedWord.insert(0, Glyph.ISOL_U);                  // normal
                    }
                    break;
                case INITIAL:
                    if (isSuffix) {
                        renderedWord.insert(0, Glyph.MEDI_U_BP);               // U of UU suffix   *** suffix rule ***
                    } else {
                        renderedWord.insert(0, Glyph.INIT_U);                  // normal
                    }
                    break;
                case MEDIAL:
                    if (fvs == Uni.FVS1) {
                        renderedWord.insert(0, Glyph.MEDI_U_FVS1);             // tooth + O
                    } else {
                        if (isRoundLetter(charAbove)) {
                            renderedWord.insert(0, Glyph.MEDI_U_BP);           // After BPFK
                        } else {
                            renderedWord.insert(0, Glyph.MEDI_U);              // normal
                        }
                    }
                    break;
                case FINAL:
                    if (fvs == Uni.FVS1) {
                        renderedWord.insert(0, Glyph.FINA_U_FVS1);             // round o
                    } else {
                        if (isRoundLetter(charAbove)) {
                            renderedWord.insert(0, Glyph.FINA_U_BP);           // After BPFK
                        } else {
                            renderedWord.insert(0, Glyph.FINA_U);              // normal
                        }
                    }
                    break;
            }
            glyphShapeBelow = Shape.STEM;
        }

        private void handleOE(StringBuilder renderedWord, int positionInWord, char charAbove) {
            gender = Gender.FEMININE;
            switch (location) {
                case ISOLATE:
                    if (isSuffix) {
                        renderedWord.insert(0, Glyph.FINA_OE);                 // O suffix   *** suffix rule ***
                    } else {
                        renderedWord.insert(0, Glyph.ISOL_OE);                 // normal
                    }
                    break;
                case INITIAL:
                    if (isSuffix) {
                        renderedWord.insert(0, Glyph.MEDI_OE_BP);              // O of OO suffix   *** suffix rule ***
                    } else {
                        renderedWord.insert(0, Glyph.INIT_OE);                 // normal
                    }
                    break;
                case MEDIAL:
                    if (fvs == Uni.FVS1) {
                        if (isRoundLetterIncludingQG(charAbove)) {
                            renderedWord.insert(0, Glyph.MEDI_OE_FVS1_BP);     // first syllable long tooth OE after BPFK
                        } else {
                            renderedWord.insert(0, Glyph.MEDI_OE_FVS1);        // first syllable long tooth OE
                        }
                    } else if (fvs == Uni.FVS2) {
                        renderedWord.insert(0, Glyph.MEDI_OE_FVS2);            // extra tooth for 2 part name
                    } else {
                        if (needsLongToothU(inputWord, positionInWord)) {
                            // *** first syllable long tooth rule (except in suffix) ***
                            if (isRoundLetterIncludingQG(charAbove)) {
                                renderedWord.insert(0, Glyph.MEDI_OE_FVS1_BP); // first syllable long tooth UE after BPFK
                            } else {
                                renderedWord.insert(0, Glyph.MEDI_OE_FVS1);    // first syllable long tooth UE
                            }
                        } else if (isRoundLetterIncludingQG(charAbove)) {
                            renderedWord.insert(0, Glyph.MEDI_OE_BP);          // After BPFK
                        } else {
                            renderedWord.insert(0, Glyph.MEDI_OE);             // normal
                        }
                    }
                    break;
                case FINAL:
                    if (fvs == Uni.FVS1) {
                        if (isRoundLetterIncludingQG(charAbove)) {
                            renderedWord.insert(0, Glyph.FINA_OE_FVS1_BP);     // round o with tail after BPFK
                        } else {
                            renderedWord.insert(0, Glyph.FINA_OE_FVS1);        // round o with tail
                        }
                    } else {
                        if (isRoundLetterIncludingQG(charAbove)) {
                            renderedWord.insert(0, Glyph.FINA_OE_BP);          // After BPFK
                        } else {
                            renderedWord.insert(0, Glyph.FINA_OE);             // normal
                        }
                    }
                    break;
            }
            glyphShapeBelow = Shape.STEM;
        }

        private void handleUE(StringBuilder renderedWord, int positionInWord, char charAbove) {
            gender = Gender.FEMININE;
            switch (location) {
                case ISOLATE:
                    if (isSuffix) {
                        renderedWord.insert(0, Glyph.FINA_UE);                 // O suffix   *** suffix rule ***
                    } else if (fvs == Uni.FVS1) {
                        renderedWord.insert(0, Glyph.ISOL_UE_FVS1);            // like E+UE
                    } else {
                        renderedWord.insert(0, Glyph.ISOL_UE);                 // normal
                    }
                    break;
                case INITIAL:
                    if (isSuffix) {
                        renderedWord.insert(0, Glyph.MEDI_UE_BP);              // U of UU suffix   *** suffix rule ***
                    } else {
                        renderedWord.insert(0, Glyph.INIT_UE);                 // normal
                    }
                    break;
                case MEDIAL:
                    if (fvs == Uni.FVS1) {
                        if (isRoundLetterIncludingQG(charAbove)) {
                            renderedWord.insert(0, Glyph.MEDI_UE_FVS1_BP);     // first syllable long tooth UE after BPFK
                        } else {
                            renderedWord.insert(0, Glyph.MEDI_UE_FVS1);        // first syllable long tooth UE
                        }
                    } else if (fvs == Uni.FVS2) {
                        renderedWord.insert(0, Glyph.MEDI_UE_FVS2);            // extra tooth for 2 part name
                    } else {
                        if (needsLongToothU(inputWord, positionInWord)) {
                            // *** first syllable long tooth rule (except in suffix) ***
                            if (isRoundLetterIncludingQG(charAbove)) {
                                renderedWord.insert(0, Glyph.MEDI_UE_FVS1_BP); // first syllable long tooth UE after BPFK
                            } else {
                                renderedWord.insert(0, Glyph.MEDI_UE_FVS1);    // first syllable long tooth UE
                            }
                        } else if (isRoundLetterIncludingQG(charAbove)) {
                            renderedWord.insert(0, Glyph.MEDI_UE_BP);          // After BPFK
                        } else {
                            renderedWord.insert(0, Glyph.MEDI_UE);             // normal
                        }
                    }
                    break;
                case FINAL:
                    if (fvs == Uni.FVS1) {
                        if (isRoundLetterIncludingQG(charAbove)) {
                            renderedWord.insert(0, Glyph.FINA_UE_FVS1_BP);     // round o with tail after BPFK
                        } else {
                            renderedWord.insert(0, Glyph.FINA_UE_FVS1);        // round o with tail
                        }
                    } else {
                        if (isRoundLetterIncludingQG(charAbove)) {
                            renderedWord.insert(0, Glyph.FINA_UE_BP);          // After BPFK
                        } else {
                            renderedWord.insert(0, Glyph.FINA_UE);             // normal
                        }
                    }
                    break;
            }
            glyphShapeBelow = Shape.STEM;
        }

        private void handleEE(StringBuilder renderedWord) {
            gender = Gender.FEMININE;
            switch (location) {
                case ISOLATE:
                    renderedWord.insert(0, Glyph.ISOL_EE);                      // normal
                    break;
                case INITIAL:
                    renderedWord.insert(0, Glyph.INIT_EE);                      // normal
                    break;
                case MEDIAL:
                    renderedWord.insert(0, Glyph.MEDI_EE);                      // normal
                    break;
                case FINAL:
                    renderedWord.insert(0, Glyph.FINA_EE);                      // normal
                    break;
            }
            glyphShapeBelow = Shape.TOOTH;
        }

        private void handleNA(StringBuilder renderedWord, int positionInWord,
                              char charBelow, char charBelowFvs) {

            switch (location) {
                case ISOLATE:
                    renderedWord.insert(0, Glyph.ISOL_NA);                      // normal
                    break;
                case INITIAL:
                    if (fvs == Uni.FVS1) {
                        renderedWord.insert(0, Glyph.INIT_NA_FVS1_STEM);        // non-dotted
                    } else {
                        if (glyphShapeBelow == Shape.STEM) {
                            renderedWord.insert(0, Glyph.INIT_NA_STEM);        // normal stem
                        } else {
                            renderedWord.insert(0, Glyph.INIT_NA_TOOTH);       // normal tooth
                        }
                    }
                    break;
                case MEDIAL:

                    if (fvs == Uni.FVS1) {
                        if (glyphShapeBelow == Shape.STEM) {
                            renderedWord.insert(0, Glyph.MEDI_NA_FVS1_STEM);    // dotted stem
                        } else {
                            renderedWord.insert(0, Glyph.MEDI_NA_FVS1_TOOTH);   // dotted tooth
                        }
                        glyphShapeBelow = Shape.TOOTH;
                    } else if (fvs == Uni.FVS2) {
                        renderedWord.insert(0, Glyph.MEDI_NA_FVS2);             // MVS
                        glyphShapeBelow = Shape.STEM;
                    } else if (fvs == Uni.FVS3) {
                        renderedWord.insert(0, Glyph.MEDI_NA_FVS3);             // tod script
                        glyphShapeBelow = Shape.TOOTH;
                    } else {
                        // *** dot N before vowel rule ***
                        if (isVowel(charBelow)) {
                            // *** don't dot N if final letter before vowel of compound name ***
                            if (positionInWord < length - 3 && // the next char should not be final either (ie, KINO)
                                    isFVS(inputWord.charAt(positionInWord + 2)) &&
                                    isTwoPartNameInitialVowel(charBelow, charBelowFvs)) {
                                // This will work for names whose second part starts with
                                // A, I, O, U, OE, and UE. But it won't work if it starts
                                // with E or EE because there are no second medial (FVS1)
                                // forms for these letters. A user could insert a ZWJ but
                                // they are unlikely to know that.
                                if (glyphShapeBelow == Shape.STEM) {
                                    renderedWord.insert(0, Glyph.MEDI_NA_STEM);    // non-dotted stem
                                } else {
                                    renderedWord.insert(0, Glyph.MEDI_NA_TOOTH);   // non-dotted tooth
                                }
                            } else {
                                if (glyphShapeBelow == Shape.STEM) {
                                    renderedWord.insert(0, Glyph.MEDI_NA_FVS1_STEM);    // dotted stem
                                } else {
                                    renderedWord.insert(0, Glyph.MEDI_NA_FVS1_TOOTH);   // dotted tooth
                                }
                            }
                        } else {
                            if (glyphShapeBelow == Shape.STEM) {
                                renderedWord.insert(0, Glyph.MEDI_NA_STEM);    // normal non-dotted stem
                            } else {
                                renderedWord.insert(0, Glyph.MEDI_NA_TOOTH);   // normal non-dotted tooth
                            }
                        }
                        glyphShapeBelow = Shape.TOOTH;
                    }
                    break;
                case FINAL:
                    if (charBelow == Uni.MVS) {
                        renderedWord.insert(0, Glyph.MEDI_NA_FVS2);             // MVS
                    } else {
                        renderedWord.insert(0, Glyph.FINA_NA);                  // normal
                    }
                    glyphShapeBelow = Shape.STEM;
                    break;
            }
        }

        private void handleANG(StringBuilder renderedWord) {
            switch (location) {
                case ISOLATE:
                    renderedWord.insert(0, Glyph.ISOL_ANG);                      // normal
                    break;
                case INITIAL:
                    if (glyphShapeBelow == Shape.ROUND) {
                        renderedWord.insert(0, Glyph.INIT_ANG_ROUND);            // before round
                    } else if (glyphShapeBelow == Shape.STEM) {
                        renderedWord.insert(0, Glyph.INIT_ANG_STEM);             // before stem
                    } else {
                        renderedWord.insert(0, Glyph.INIT_ANG_TOOTH);            // tooth tooth
                    }
                    break;
                case MEDIAL:
                    if (glyphShapeBelow == Shape.ROUND) {
                        renderedWord.insert(0, Glyph.MEDI_ANG_ROUND);            // before round
                    } else if (glyphShapeBelow == Shape.STEM) {
                        renderedWord.insert(0, Glyph.MEDI_ANG_STEM);             // before stem
                    } else {
                        renderedWord.insert(0, Glyph.MEDI_ANG_TOOTH);            // tooth tooth
                    }
                    break;
                case FINAL:
                    renderedWord.insert(0, Glyph.FINA_ANG);                      // normal
                    break;
            }
            glyphShapeBelow = Shape.TOOTH;
        }

        private void handleBA(StringBuilder renderedWord, char charBelow) {
            switch (location) {
                case ISOLATE:
                    renderedWord.insert(0, Glyph.ISOL_BA);                       // normal
                    break;
                case INITIAL:
                    if (isOuVowel(charBelow)) {
                        renderedWord.insert(0, Glyph.INIT_BA_OU);                // OU
                    } else if (glyphShapeBelow == Shape.STEM) {
                        renderedWord.insert(0, Glyph.INIT_BA_STEM);              // stem
                    } else {
                        renderedWord.insert(0, Glyph.INIT_BA);                   // normal
                    }
                    break;
                case MEDIAL:
                    if (isOuVowel(charBelow)) {
                        renderedWord.insert(0, Glyph.MEDI_BA_OU);                // OU
                    } else if (glyphShapeBelow == Shape.STEM) {
                        renderedWord.insert(0, Glyph.MEDI_BA_STEM);              // stem
                    } else {
                        renderedWord.insert(0, Glyph.MEDI_BA_TOOTH);                   // normal
                    }
                    break;
                case FINAL:
                    if (fvs == Uni.FVS1) {
                        renderedWord.insert(0, Glyph.FINA_BA_FVS1);              // o with left sweep
                    } else {
                        renderedWord.insert(0, Glyph.FINA_BA);                   // normal
                    }
                    break;
            }
            glyphShapeBelow = Shape.STEM;
        }

        private void handlePA(StringBuilder renderedWord, char charBelow) {
            switch (location) {
                case ISOLATE:
                    renderedWord.insert(0, Glyph.ISOL_PA);                       // normal
                    break;
                case INITIAL:
                    if (isOuVowel(charBelow)) {
                        renderedWord.insert(0, Glyph.INIT_PA_OU);                // OU
                    } else if (glyphShapeBelow == Shape.STEM) {
                        renderedWord.insert(0, Glyph.INIT_PA_STEM);              // stem
                    } else {
                        renderedWord.insert(0, Glyph.INIT_PA);                   // normal
                    }
                    break;
                case MEDIAL:
                    if (isOuVowel(charBelow)) {
                        renderedWord.insert(0, Glyph.MEDI_PA_OU);                // OU
                    } else if (glyphShapeBelow == Shape.STEM) {
                        renderedWord.insert(0, Glyph.MEDI_PA_STEM);              // stem
                    } else {
                        renderedWord.insert(0, Glyph.MEDI_PA_TOOTH);                   // normal
                    }
                    break;
                case FINAL:
                    renderedWord.insert(0, Glyph.FINA_PA);                       // normal
                    break;
            }
            glyphShapeBelow = Shape.STEM;
        }

        private void handleQA(StringBuilder renderedWord, int positionInWord, char charAbove, char charBelow) {
            switch (location) {
                case ISOLATE:
                    if (fvs == Uni.FVS1) {
                        renderedWord.insert(0, Glyph.ISOL_QA_FVS1);             // dotted feminine
                    } else {
                        renderedWord.insert(0, Glyph.ISOL_QA);                  // normal
                    }
                    break;
                case INITIAL:
                    if (fvs == Uni.FVS1) {
                        if (isFeminineVowel(charBelow) || charBelow == Uni.I) {
                            if (isOuVowel(charBelow)) {
                                renderedWord.insert(0, Glyph.INIT_QA_FVS1_FEM_OU);   // dotted feminine for OU
                            } else {
                                renderedWord.insert(0, Glyph.INIT_QA_FVS1_FEM);      // dotted feminine
                            }
                        } else {
                            if (glyphShapeBelow == Shape.STEM) {
                                renderedWord.insert(0, Glyph.INIT_QA_FVS1_STEM);     // dotted masculine stem
                            } else {
                                renderedWord.insert(0, Glyph.INIT_QA_FVS1_TOOTH);    // dotted masculine tooth
                            }
                        }
                    } else {
                        if (isFeminineVowel(charBelow) || charBelow == Uni.I) {
                            if (isOuVowel(charBelow)) {
                                renderedWord.insert(0, Glyph.INIT_QA_FEM_OU);   // feminine for OU
                            } else {
                                renderedWord.insert(0, Glyph.INIT_QA_FEM);      // feminine
                            }
                        } else {
                            if (glyphShapeBelow == Shape.STEM) {
                                renderedWord.insert(0, Glyph.INIT_QA_STEM);     // normal (masculine) stem
                            } else {
                                renderedWord.insert(0, Glyph.INIT_QA_TOOTH);    // normal (masculine) tooth
                            }
                        }
                    }
                    break;
                case MEDIAL:

                    if (fvs == Uni.FVS1) {
                        if (isFeminineVowel(charBelow) || charBelow == Uni.I) {
                            if (isOuVowel(charBelow)) {
                                renderedWord.insert(0, Glyph.MEDI_QA_FVS1_FEM_OU);   // dotted feminine for OU
                            } else {
                                renderedWord.insert(0, Glyph.MEDI_QA_FVS1_FEM);      // dotted feminine
                            }
                            glyphShapeBelow = Shape.ROUND;
                        } else if (isMasculineVowel(charBelow)) {
                            renderedWord.insert(0, Glyph.MEDI_QA_FVS1);         // dotted double tooth masculine
                            glyphShapeBelow = Shape.TOOTH;
                        } else { // consonant
                            if (gender == Gender.NEUTER) {
                                gender = getWordGenderAboveIndex(positionInWord, inputWord);
                            }
                            if (gender == Gender.FEMININE) {
                                renderedWord.insert(0, Glyph.MEDI_QA_FEM_CONSONANT_DOTTED);   // dotted feminine final before consonant
                            } else {
                                renderedWord.insert(0, Glyph.MEDI_QA_FVS1);     // dotted double tooth masculine
                            }
                            glyphShapeBelow = Shape.TOOTH;
                        }
                    } else if (fvs == Uni.FVS2) {
                        renderedWord.insert(0, Glyph.MEDI_QA_FVS2);             // dotted MVS
                        glyphShapeBelow = Shape.TOOTH;
                    } else if (fvs == Uni.FVS3) {
                        renderedWord.insert(0, Glyph.MEDI_QA_FVS3);             // MVS
                        glyphShapeBelow = Shape.TOOTH;
                    } else {
                        if (isFeminineVowel(charBelow) || charBelow == Uni.I) {
                            if (isOuVowel(charBelow)) {
                                renderedWord.insert(0, Glyph.MEDI_QA_FEM_OU);   // feminine for OU
                            } else {
                                renderedWord.insert(0, Glyph.MEDI_QA_FEM);      // feminine
                            }
                            glyphShapeBelow = Shape.ROUND;
                        } else if (isMasculineVowel(charBelow)) {
                            if (glyphShapeBelow == Shape.STEM) {
                                renderedWord.insert(0, Glyph.MEDI_QA_STEM);     // normal stem (masculine double tooth)
                            } else {
                                renderedWord.insert(0, Glyph.MEDI_QA_TOOTH);    // normal tooth (masculine double tooth)
                            }
                            glyphShapeBelow = Shape.TOOTH;
                        } else { // consonant
                            // does medial QA before a consonant ever happen
                            // in a real word?
                            if (gender == Gender.NEUTER) {
                                gender = getWordGenderAboveIndex(positionInWord, inputWord);
                            }
                            if (gender == Gender.FEMININE ||
                                    (gender == Gender.NEUTER
                                            && charAbove == Uni.I)) {
                                renderedWord.insert(0, Glyph.MEDI_QA_FEM_CONSONANT);   // feminine final before consonant
                            } else {
                                if (glyphShapeBelow == Shape.STEM) {
                                    renderedWord.insert(0, Glyph.MEDI_QA_STEM);        // normal stem (masculine double tooth)
                                } else {
                                    renderedWord.insert(0, Glyph.MEDI_QA_TOOTH);       // normal tooth (masculine double tooth)
                                }
                            }
                            glyphShapeBelow = Shape.TOOTH;
                        }
                    }
                    break;
                case FINAL:
                    renderedWord.insert(0, Glyph.FINA_QA);                       // normal
                    glyphShapeBelow = Shape.TOOTH;
                    break;
            }
        }

        private void handleGA(StringBuilder renderedWord, int positionInWord, char charAbove, char charBelow) {

            switch (location) {
                case ISOLATE:
                    renderedWord.insert(0, Glyph.ISOL_GA);                      // normal
                    break;
                case INITIAL:
                    if (fvs == Uni.FVS1) {
                        if (glyphShapeBelow == Shape.STEM) {
                            renderedWord.insert(0, Glyph.INIT_GA_FVS1_STEM);    // undotted masculine stem
                        } else {
                            renderedWord.insert(0, Glyph.INIT_GA_FVS1_TOOTH);   // undotted masculine tooth
                        }
                        // TODO feminine forms are not handled.
                        // What are they supposed to look like?
                    } else {
                        if (isFeminineVowel(charBelow) || charBelow == Uni.I) {
                            if (isOuVowel(charBelow)) {
                                renderedWord.insert(0, Glyph.INIT_GA_FEM_OU);   // feminine for OU
                            } else {
                                renderedWord.insert(0, Glyph.INIT_GA_FEM);      // feminine
                            }
                        } else {
                            if (isConsonant(charBelow)) {
                                // *** feminine form before consonant in foreign words ***
                                renderedWord.insert(0, Glyph.INIT_GA_FEM);      // feminine
                            } else if (glyphShapeBelow == Shape.STEM) {
                                renderedWord.insert(0, Glyph.INIT_GA_STEM);     // normal (masculine) stem
                            } else {
                                renderedWord.insert(0, Glyph.INIT_GA_TOOTH);    // normal (masculine) tooth
                            }
                        }
                    }
                    break;
                case MEDIAL:

                    if (fvs == Uni.FVS1) {
                        if (glyphShapeBelow == Shape.STEM) {
                            renderedWord.insert(0, Glyph.MEDI_GA_FVS1_STEM);    // dotted masculine stem
                        } else {
                            renderedWord.insert(0, Glyph.MEDI_GA_FVS1_TOOTH);   // dotted masculine tooth
                        }
                        glyphShapeBelow = Shape.TOOTH;
                    } else if (fvs == Uni.FVS2) {
                        renderedWord.insert(0, Glyph.MEDI_GA_FVS2);             // MVS
                        glyphShapeBelow = Shape.TOOTH;
                    } else if (fvs == Uni.FVS3) {
                        if (glyphShapeBelow == Shape.STEM) {
                            renderedWord.insert(0, Glyph.MEDI_GA_FVS3_STEM);    // feminine before consonant stem
                        } else {
                            renderedWord.insert(0, Glyph.MEDI_GA_FVS3_TOOTH);   // feminine before consonant tooth
                        }
                        glyphShapeBelow = Shape.TOOTH;
                    } else { // no FVS, just apply context rules
                        if (isFeminineVowel(charBelow) || charBelow == Uni.I) {
                            // *** feminine GA rule ***
                            if (isOuVowel(charBelow)) {
                                renderedWord.insert(0, Glyph.MEDI_GA_FEM_OU);   // feminine for OU
                            } else {
                                renderedWord.insert(0, Glyph.MEDI_GA_FEM);      // feminine
                            }
                            glyphShapeBelow = Shape.ROUND;
                        } else if (isMasculineVowel(charBelow)) {
                            // *** dotted masculine GA rule ***
                            if (glyphShapeBelow == Shape.STEM) {
                                renderedWord.insert(0, Glyph.MEDI_GA_FVS1_STEM);   // dotted masculine stem
                            } else {
                                renderedWord.insert(0, Glyph.MEDI_GA_FVS1_TOOTH);  // dotted masculine tooth
                            }
                            glyphShapeBelow = Shape.TOOTH;
                        } else { // consonant
                            if (gender == Gender.NEUTER) {
                                gender = getWordGenderAboveIndex(positionInWord, inputWord);
                            }
                            // *** medial GA before consonant rule ***
                            if (gender == Gender.FEMININE ||
                                    // Defaulting to feminine form for I
                                    (gender == Gender.NEUTER
                                            && charAbove == Uni.I) ||
                                    // treat a G between two consonants as feminine (as in ANGGLI)
                                    // (but not after Y because Y is like I)
                                    (charAbove != Uni.YA &&
                                            (isConsonant(charAbove) ||
                                                    charAbove == Uni.ZWJ))) {

                                if (charBelow == Uni.NA ||
                                        charBelow == Uni.MA ||
                                        charBelow == Uni.LA ) {
                                    char renderedCharBelow = renderedWord.charAt(0);
                                    if (renderedCharBelow == Glyph.FINA_MA ||
                                            renderedCharBelow == Glyph.FINA_LA ||
                                            renderedCharBelow == Glyph.FINA_NA ||
                                            renderedCharBelow == Glyph.MEDI_NA_FVS2) {
                                        // make exception for words like CHECHEGM_A
                                        renderedWord.insert(0, Glyph.MEDI_GA_FVS3_STEM);    // feminine before consonant stem
                                    } else {
                                        renderedWord.insert(0, Glyph.MEDI_GA_FEM);      // BIG Fem G looks better for medial N, M, L
                                    }
                                } else if (glyphShapeBelow == Shape.STEM) {
                                    renderedWord.insert(0, Glyph.MEDI_GA_FVS3_STEM);    // feminine before consonant stem
                                } else {
                                    renderedWord.insert(0, Glyph.MEDI_GA_FVS3_TOOTH);   // feminine before consonant tooth
                                }
                                glyphShapeBelow = Shape.ROUND;
                            } else {
                                renderedWord.insert(0, Glyph.MEDI_GA);       // normal (undotted masculine)
                                glyphShapeBelow = Shape.TOOTH;
                            }

                        }
                    }
                    break;
                case FINAL:
                    if (fvs == Uni.FVS1) {
                        renderedWord.insert(0, Glyph.FINA_GA_FVS1);          // masculine context override (same as default)
                    } else if (fvs == Uni.FVS2) {
                        renderedWord.insert(0, Glyph.FINA_GA_FVS2);          // feminine
                    } else if (charBelow == Uni.MVS) {
                        renderedWord.insert(0, Glyph.MEDI_GA_FVS2);          // MVS
                    } else {
                        gender = getWordGenderAboveIndex(positionInWord, inputWord);
                        if (gender == Gender.MASCULINE ||
                                charAbove == Uni.ZWJ) {
                            renderedWord.insert(0, Glyph.FINA_GA);           // masculine
                        } else {
                            // Defaulting to feminine form for I
                            renderedWord.insert(0, Glyph.FINA_GA_FVS2);      // feminine
                        }
                    }
                    glyphShapeBelow = Shape.TOOTH;
                    break;
            }
        }

        private void handleMA(StringBuilder renderedWord, int positionInWord,
                              char charAbove, char charBelow) {
            switch (location) {
                case ISOLATE:
                    renderedWord.insert(0, Glyph.ISOL_MA);                       // normal
                    break;
                case INITIAL:
                    if (glyphShapeBelow == Shape.STEM) {
                        renderedWord.insert(0, Glyph.INIT_MA_STEM_LONG);         // stem
                    } else {
                        renderedWord.insert(0, Glyph.INIT_MA_TOOTH);             // tooth
                    }
                    break;
                case MEDIAL:
                    if (isRoundLetter(charAbove) ||
                            charAbove == Uni.ANG) {
                        renderedWord.insert(0, Glyph.MEDI_MA_BP);                // tail extended for round letter
                    } else if (charAbove == Uni.GA) {
                        if (gender == Gender.NEUTER) {
                            gender = getWordGenderAboveIndex(positionInWord, inputWord);
                        }
                        if (gender != Gender.MASCULINE ||
                                // feminine G when between consonants
                                (positionInWord > 1 &&
                                        (isConsonant(inputWord.charAt(positionInWord - 2)) ||
                                                inputWord.charAt(positionInWord - 2) == Uni.ZWJ))) {
                            renderedWord.insert(0, Glyph.MEDI_MA_BP);            // tail extended for round letter
                        } else {
                            renderedWord.insert(0, Glyph.MEDI_MA_TOOTH);         // tooth
                        }
                    } else if (glyphShapeBelow != Shape.TOOTH ||
                            // use the longer stem if M/L is below
                            charBelow == Uni.MA || charBelow == Uni.LA ||
                            charBelow == Uni.LHA) {
                        renderedWord.insert(0, Glyph.MEDI_MA_STEM_LONG);         // stem
                    } else {
                        renderedWord.insert(0, Glyph.MEDI_MA_TOOTH);             // tooth
                    }
                    glyphShapeBelow = Shape.TOOTH;
                    break;
                case FINAL:
                    renderedWord.insert(0, Glyph.FINA_MA);                       // normal
                    glyphShapeBelow = Shape.STEM;
                    break;
            }
        }

        private void handleLA(StringBuilder renderedWord, int positionInWord,
                              char charAbove, char charBelow) {

            switch (location) {
                case ISOLATE:
                    renderedWord.insert(0, Glyph.ISOL_LA);                       // normal
                    break;
                case INITIAL:
                    if (glyphShapeBelow == Shape.STEM) {
                        renderedWord.insert(0, Glyph.INIT_LA_STEM_LONG);         // stem
                    } else {
                        renderedWord.insert(0, Glyph.INIT_LA_TOOTH);             // tooth
                    }
                    break;
                case MEDIAL:
                    if (isRoundLetter(charAbove) ||
                            charAbove == Uni.ANG) {
                        renderedWord.insert(0, Glyph.MEDI_LA_BP);                // tail extended for round letter
                    } else if (charAbove == Uni.GA) {
                        if (gender == Gender.NEUTER) {
                            gender = getWordGenderAboveIndex(positionInWord, inputWord);
                        }
                        if (gender != Gender.MASCULINE ||
                                // feminine G when between consonants
                                (positionInWord > 1 &&
                                        (isConsonant(inputWord.charAt(positionInWord - 2)) ||
                                                inputWord.charAt(positionInWord - 2) == Uni.ZWJ))) {
                            renderedWord.insert(0, Glyph.MEDI_LA_BP);            // tail extended for round letter
                        } else {
                            renderedWord.insert(0, Glyph.MEDI_LA_TOOTH);         // tooth
                        }
                    } else if (glyphShapeBelow != Shape.TOOTH ||
                            // use the longer stem if M/L is below
                            charBelow == Uni.MA || charBelow == Uni.LA ||
                            charBelow == Uni.LHA) {
                        renderedWord.insert(0, Glyph.MEDI_LA_STEM_LONG);         // stem
                    } else {
                        renderedWord.insert(0, Glyph.MEDI_LA_TOOTH);             // tooth
                    }
                    glyphShapeBelow = Shape.TOOTH;
                    break;
                case FINAL:
                    renderedWord.insert(0, Glyph.FINA_LA);                       // normal
                    glyphShapeBelow = Shape.STEM;
                    break;
            }
        }

        private void handleSA(StringBuilder renderedWord) {
            switch (location) {

                case ISOLATE:
                    renderedWord.insert(0, Glyph.ISOL_SA);                       // normal
                    break;
                case INITIAL:
                    if (glyphShapeBelow == Shape.STEM) {
                        renderedWord.insert(0, Glyph.INIT_SA_STEM);              // stem
                    } else {
                        renderedWord.insert(0, Glyph.INIT_SA_TOOTH);             // tooth
                    }
                    break;
                case MEDIAL:
                    if (glyphShapeBelow == Shape.STEM) {
                        renderedWord.insert(0, Glyph.MEDI_SA_STEM);              // stem
                    } else {
                        renderedWord.insert(0, Glyph.MEDI_SA_TOOTH);             // tooth
                    }
                    glyphShapeBelow = Shape.TOOTH;
                    break;
                case FINAL:
                    glyphShapeBelow = Shape.TOOTH;
                    if (fvs == Uni.FVS1) {
                        renderedWord.insert(0, Glyph.FINA_SA_FVS1);              // short tail
                        glyphShapeBelow = Shape.STEM;
                    } else if (fvs == Uni.FVS2) {
                        renderedWord.insert(0, Glyph.FINA_SA_FVS2);              // (missing glyph)
                    } else {
                        renderedWord.insert(0, Glyph.FINA_SA);                   // normal
                    }
                    break;
            }
        }

        private void handleSHA(StringBuilder renderedWord) {
            switch (location) {
                case ISOLATE:
                    renderedWord.insert(0, Glyph.ISOL_SHA);                       // normal
                    break;
                case INITIAL:
                    if (glyphShapeBelow == Shape.STEM) {
                        renderedWord.insert(0, Glyph.INIT_SHA_STEM);              // stem
                    } else {
                        renderedWord.insert(0, Glyph.INIT_SHA_TOOTH);             // tooth
                    }
                    break;
                case MEDIAL:
                    if (glyphShapeBelow == Shape.STEM) {
                        renderedWord.insert(0, Glyph.MEDI_SHA_STEM);              // stem
                    } else {
                        renderedWord.insert(0, Glyph.MEDI_SHA_TOOTH);             // tooth
                    }
                    break;
                case FINAL:
                    renderedWord.insert(0, Glyph.FINA_SHA);                       // normal
                    break;
            }
            glyphShapeBelow = Shape.TOOTH;
        }

        private void handleTA(StringBuilder renderedWord) {
            switch (location) {
                case ISOLATE:
                    renderedWord.insert(0, Glyph.ISOL_TA);                       // normal
                    break;
                case INITIAL:
                    if (glyphShapeBelow == Shape.STEM) {
                        renderedWord.insert(0, Glyph.INIT_TA_STEM);              // stem
                    } else {
                        renderedWord.insert(0, Glyph.INIT_TA_TOOTH);             // tooth
                    }
                    break;
                case MEDIAL:
                    if (fvs == Uni.FVS1) {
                        if (glyphShapeBelow == Shape.STEM) {
                            renderedWord.insert(0, Glyph.MEDI_TA_FVS1_STEM);     // stem
                        } else {
                            renderedWord.insert(0, Glyph.MEDI_TA_FVS1_TOOTH);    // tooth
                        }
                        glyphShapeBelow = Shape.STEM;
                    } else {
                        renderedWord.insert(0, Glyph.MEDI_TA);                   // normal
                        glyphShapeBelow = Shape.TOOTH;
                    }
                    break;
                case FINAL:
                    renderedWord.insert(0, Glyph.FINA_TA);                       // normal
                    glyphShapeBelow = Shape.STEM;
                    break;
            }
        }

        private void handleDA(StringBuilder renderedWord, char charBelow) {
            switch (location) {
                case ISOLATE:
                    renderedWord.insert(0, Glyph.ISOL_DA);                       // normal
                    break;
                case INITIAL:
                    if (fvs == Uni.FVS1 || isSuffix) {
                        renderedWord.insert(0, Glyph.INIT_DA_FVS1);              // left slanting
                    } else {
                        if (glyphShapeBelow == Shape.STEM) {
                            renderedWord.insert(0, Glyph.INIT_DA_STEM);          // stem
                        } else {
                            renderedWord.insert(0, Glyph.INIT_DA_TOOTH);         // tooth
                        }
                    }
                    break;
                case MEDIAL:
                    if (fvs == Uni.FVS1) {
                        renderedWord.insert(0, Glyph.MEDI_DA_FVS1);              // left slanting
                        glyphShapeBelow = Shape.TOOTH;
                    } else {
                        if (isVowel(charBelow)) {
                            renderedWord.insert(0, Glyph.MEDI_DA_FVS1);          // left slanting
                            glyphShapeBelow = Shape.TOOTH;
                        } else {
                            renderedWord.insert(0, Glyph.MEDI_DA);               // normal (before consonant)
                            glyphShapeBelow = Shape.STEM;
                        }
                    }
                    break;
                case FINAL:
                    if (fvs == Uni.FVS1) {
                        renderedWord.insert(0, Glyph.FINA_DA_FVS1);              // left slanting
                        glyphShapeBelow = Shape.TOOTH;
                    } else {
                        renderedWord.insert(0, Glyph.FINA_DA);                   // normal (like o-n)
                        glyphShapeBelow = Shape.STEM;
                    }
                    break;
            }
        }

        private void handleCHA(StringBuilder renderedWord) {
            switch (location) {
                case ISOLATE:
                    renderedWord.insert(0, Glyph.ISOL_CHA);                       // normal
                    break;
                case INITIAL:
                    renderedWord.insert(0, Glyph.INIT_CHA);                       // normal
                    break;
                case MEDIAL:
                    renderedWord.insert(0, Glyph.MEDI_CHA);                       // normal
                    break;
                case FINAL:
                    renderedWord.insert(0, Glyph.FINA_CHA);                       // normal
                    break;
            }
            glyphShapeBelow = Shape.STEM;
        }

        private void handleJA(StringBuilder renderedWord, char charBelow) {

            switch (location) {
                case ISOLATE:
                    renderedWord.insert(0, Glyph.ISOL_JA);                       // normal
                    break;
                case INITIAL:
                    if (charBelow == Uni.MVS) {
                        renderedWord.insert(0, Glyph.MEDI_JA_FVS1);              // MVS
                    } else if (glyphShapeBelow == Shape.STEM) {
                        renderedWord.insert(0, Glyph.INIT_JA_STEM);              // stem
                    } else { // tooth
                        // The Qimad font seems to be broken here
                        // so temporarily disabling this glyph
                        // TODO fix the font, or remove it, or just use
                        // this alternate glyph.
                        //renderedWord.insert(0, Glyph.INIT_JA_TOOTH);                 // tooth
                        renderedWord.insert(0, Glyph.INIT_JA_STEM);
                    }
                    break;
                case MEDIAL:
                    if (fvs == Uni.FVS1) {
                        renderedWord.insert(0, Glyph.MEDI_JA_FVS1);              // MVS
                        glyphShapeBelow = Shape.TOOTH;
                    } else {
                        renderedWord.insert(0, Glyph.MEDI_JA);                   // normal (before consonant)
                        glyphShapeBelow = Shape.STEM;
                    }
                    break;
                case FINAL:
                    if (charBelow == Uni.MVS) {
                        renderedWord.insert(0, Glyph.MEDI_JA_FVS1);              // MVS
                        glyphShapeBelow = Shape.TOOTH;
                    } else {
                        renderedWord.insert(0, Glyph.FINA_JA);                   // normal
                        glyphShapeBelow = Shape.STEM;
                    }
                    break;
            }
        }

        private void handleYA(StringBuilder renderedWord, int positionInWord,
                              char charAbove, char charBelow) {

            switch (location) {
                case ISOLATE:
                    renderedWord.insert(0, Glyph.ISOL_YA);                  // normal
                    break;
                case INITIAL:
                    if (isSuffix && charBelow == Uni.I) {
                        renderedWord.insert(0, Glyph.MEDI_YA);         // suffix - no hook
                    } else if (fvs == Uni.FVS1) {
                        renderedWord.insert(0, Glyph.INIT_YA_FVS1);         // no hook
                    } else {
                        renderedWord.insert(0, Glyph.INIT_YA);              // hook
                    }
                    break;
                case MEDIAL:
                    if (fvs == Uni.FVS1) {
                        renderedWord.insert(0, Glyph.MEDI_YA_FVS1);         // hook
                    } else if (isSuffix && charAbove == Uni.I) {
                        // *** no hook after Y (as in IYEN and IYER) ***
                        renderedWord.insert(0, Glyph.MEDI_YA);             // suffix - no hook
                    } else {

                        // *** AYI, EYI, OYI, UYI, OEYI, UEYI
                        // medial double tooth YI diphthong rule ***
                        // Also do this for consonant below.
                        if (needsLongToothU(inputWord, positionInWord - 1) || charAbove == Uni.I) {
                            if (charBelow == Uni.I || isConsonant(charBelow)) {
                                renderedWord.insert(0, Glyph.MEDI_YA);           // no hook
                            } else {
                                renderedWord.insert(0, Glyph.MEDI_YA_FVS1);      // hook
                            }
                        } else if (isVowel(charAbove)) {
                            if (charBelow == Uni.I) {
                                renderedWord.insert(0, Glyph.MEDI_YA);          // no hook
                            } else if (isConsonant(charBelow)) {
                                renderedWord.insert(0, Glyph.MEDI_I_DOUBLE_TOOTH); // double tooth
                            } else {
                                renderedWord.insert(0, Glyph.MEDI_YA_FVS1);          // hook
                            }
                        } else {
                            renderedWord.insert(0, Glyph.MEDI_YA_FVS1);          // hook
                        }
                    }
                    break;
                case FINAL:
                    renderedWord.insert(0, Glyph.FINA_YA);                  // normal
                    break;
            }
            glyphShapeBelow = Shape.TOOTH;
        }

        private void handleRA(StringBuilder renderedWord) {
            switch (location) {
                case ISOLATE:
                    renderedWord.insert(0, Glyph.ISOL_RA);                  // normal
                    break;
                case INITIAL:
                    if (glyphShapeBelow == Shape.STEM) {
                        renderedWord.insert(0, Glyph.INIT_RA_STEM);         // stem
                    } else {
                        renderedWord.insert(0, Glyph.INIT_RA_TOOTH);        // tooth
                    }
                    break;
                case MEDIAL:
                    if (glyphShapeBelow == Shape.STEM) {
                        renderedWord.insert(0, Glyph.MEDI_RA_STEM);         // stem
                    } else {
                        renderedWord.insert(0, Glyph.MEDI_RA_TOOTH);        // tooth
                    }
                    glyphShapeBelow = Shape.TOOTH;
                    break;
                case FINAL:
                    renderedWord.insert(0, Glyph.FINA_RA);                  // normal
                    glyphShapeBelow = Shape.STEM;
                    break;
            }
        }

        private void handleWA(StringBuilder renderedWord, char charBelow) {
            switch (location) {
                case ISOLATE:
                    renderedWord.insert(0, Glyph.ISOL_WA);                  // normal
                    break;
                case INITIAL:
                    renderedWord.insert(0, Glyph.INIT_WA);                  // normal
                    break;
                case MEDIAL:
                    renderedWord.insert(0, Glyph.MEDI_WA);              // normal
                    glyphShapeBelow = Shape.TOOTH;
                    break;
                case FINAL:
                    if (fvs == Uni.FVS1) {
                        renderedWord.insert(0, Glyph.FINA_WA_FVS1);         // round like final o
                        glyphShapeBelow = Shape.STEM;
                    } else if (charBelow == Uni.MVS) {
                        renderedWord.insert(0, Glyph.FINA_WA_FVS1);         // MVS
                        glyphShapeBelow = Shape.STEM;
                    } else {
                        renderedWord.insert(0, Glyph.FINA_WA);              // normal
                        glyphShapeBelow = Shape.TOOTH;
                    }
                    break;
            }
        }

        private void handleFA(StringBuilder renderedWord, char charBelow) {
            switch (location) {
                case ISOLATE:
                    renderedWord.insert(0, Glyph.ISOL_FA);                       // normal
                    break;
                case INITIAL:
                    if (isOuVowel(charBelow)) {
                        renderedWord.insert(0, Glyph.INIT_FA_OU);                // OU
                    } else if (glyphShapeBelow == Shape.STEM) {
                        renderedWord.insert(0, Glyph.INIT_FA_STEM);              // stem
                    } else {
                        renderedWord.insert(0, Glyph.INIT_FA);                   // normal
                    }
                    break;
                case MEDIAL:
                    if (isOuVowel(charBelow)) {
                        renderedWord.insert(0, Glyph.MEDI_FA_OU);                // OU
                    } else if (glyphShapeBelow == Shape.STEM) {
                        renderedWord.insert(0, Glyph.MEDI_FA_STEM);              // stem
                    } else {
                        renderedWord.insert(0, Glyph.MEDI_FA_TOOTH);             // normal
                    }
                    break;
                case FINAL:
                    renderedWord.insert(0, Glyph.FINA_FA);                       // normal
                    break;
            }
            glyphShapeBelow = Shape.STEM;
        }

        private void handleKA(StringBuilder renderedWord, char charBelow) {
            switch (location) {
                case ISOLATE:
                    renderedWord.insert(0, Glyph.ISOL_KA);                       // normal
                    break;
                case INITIAL:
                    if (isOuVowel(charBelow)) {
                        renderedWord.insert(0, Glyph.INIT_KA_OU);                // OU
                    } else {
                        renderedWord.insert(0, Glyph.INIT_KA);                   // normal
                    }
                    break;
                case MEDIAL:
                    if (isOuVowel(charBelow)) {
                        renderedWord.insert(0, Glyph.MEDI_KA_OU);                // OU
                    } else if (glyphShapeBelow == Shape.STEM) {
                        renderedWord.insert(0, Glyph.MEDI_KA_STEM);              // stem
                    } else {
                        renderedWord.insert(0, Glyph.MEDI_KA_TOOTH);                   // normal
                    }
                    break;
                case FINAL:
                    renderedWord.insert(0, Glyph.FINA_KA);                       // normal
                    break;
            }
            glyphShapeBelow = Shape.STEM;
        }

        private void handleKHA(StringBuilder renderedWord, char charBelow) {
            switch (location) {
                case ISOLATE:
                    renderedWord.insert(0, Glyph.ISOL_KHA);                       // normal
                    break;
                case INITIAL:
                    if (isOuVowel(charBelow)) {
                        renderedWord.insert(0, Glyph.INIT_KHA_OU);                // OU
                    } else {
                        renderedWord.insert(0, Glyph.INIT_KHA);                   // normal
                    }
                    break;
                case MEDIAL:
                    if (isOuVowel(charBelow)) {
                        renderedWord.insert(0, Glyph.MEDI_KHA_OU);                // OU
                    } else if (glyphShapeBelow == Shape.STEM) {
                        renderedWord.insert(0, Glyph.MEDI_KHA_STEM);              // stem
                    } else {
                        renderedWord.insert(0, Glyph.MEDI_KHA_TOOTH);             // normal
                    }
                    break;
                case FINAL:
                    renderedWord.insert(0, Glyph.FINA_KHA);                       // normal
                    break;
            }
            glyphShapeBelow = Shape.STEM;
        }

        private void handleTSA(StringBuilder renderedWord) {
            switch (location) {
                case ISOLATE:
                    renderedWord.insert(0, Glyph.ISOL_TSA);                       // normal
                    break;
                case INITIAL:
                    renderedWord.insert(0, Glyph.INIT_TSA);                       // normal
                    break;
                case MEDIAL:
                    renderedWord.insert(0, Glyph.MEDI_TSA);                       // normal
                    break;
                case FINAL:
                    renderedWord.insert(0, Glyph.FINA_TSA);                       // normal
                    break;
            }
            glyphShapeBelow = Shape.STEM;
        }

        private void handleZA(StringBuilder renderedWord) {
            switch (location) {
                case ISOLATE:
                    renderedWord.insert(0, Glyph.ISOL_ZA);                        // normal
                    break;
                case INITIAL:
                    renderedWord.insert(0, Glyph.INIT_ZA);                        // normal
                    break;
                case MEDIAL:
                    renderedWord.insert(0, Glyph.MEDI_ZA);                        // normal
                    break;
                case FINAL:
                    renderedWord.insert(0, Glyph.FINA_ZA);                        // normal
                    break;
            }
            glyphShapeBelow = Shape.STEM;
        }

        private void handleHAA(StringBuilder renderedWord) {
            switch (location) {
                case ISOLATE:
                    renderedWord.insert(0, Glyph.ISOL_HAA);                        // normal
                    break;
                case INITIAL:
                    renderedWord.insert(0, Glyph.INIT_HAA);                        // normal
                    break;
                case MEDIAL:
                    renderedWord.insert(0, Glyph.MEDI_HAA);                        // normal
                    break;
                case FINAL:
                    renderedWord.insert(0, Glyph.FINA_HAA);                        // normal
                    break;
            }
            glyphShapeBelow = Shape.TOOTH;
        }

        private void handleZRA(StringBuilder renderedWord) {
            switch (location) {
                case ISOLATE:
                    renderedWord.insert(0, Glyph.ISOL_ZRA);                        // normal
                    break;
                case INITIAL:
                    renderedWord.insert(0, Glyph.INIT_ZRA);                        // normal
                    break;
                case MEDIAL:
                    renderedWord.insert(0, Glyph.MEDI_ZRA);                        // normal
                    break;
                case FINAL:
                    renderedWord.insert(0, Glyph.FINA_ZRA);                        // normal
                    break;
            }
            glyphShapeBelow = Shape.STEM; // ROUND didn't look very good
        }

        private void handleLHA(StringBuilder renderedWord, int positionInWord, char charAbove) {
            switch (location) {
                case ISOLATE:
                    renderedWord.insert(0, Glyph.ISOL_LHA);                       // normal
                    break;
                case INITIAL:
                    renderedWord.insert(0, Glyph.INIT_LHA);                       // normal
                    break;
                case MEDIAL:
                    if (isRoundLetter(charAbove) ||
                            charAbove == Uni.ANG) {
                        renderedWord.insert(0, Glyph.MEDI_LHA_BP);                // tail extended for round letter
                    } else if (charAbove == Uni.QA ||
                            charAbove == Uni.GA) {
                        if (gender == Gender.NEUTER) {
                            gender = getWordGenderAboveIndex(positionInWord, inputWord);
                        }
                        if (gender == Gender.FEMININE) {
                            renderedWord.insert(0, Glyph.MEDI_LHA_BP);            // tail extended for round letter
                        } else {
                            renderedWord.insert(0, Glyph.MEDI_LHA);               // normal
                        }
                    } else {
                        renderedWord.insert(0, Glyph.MEDI_LHA);                   // normal
                    }
                    break;
                case FINAL:
                    renderedWord.insert(0, Glyph.FINA_LHA);                       // normal
                    break;
            }
            glyphShapeBelow = Shape.TOOTH;
        }

        private void handleZHI(StringBuilder renderedWord) {
            switch (location) {
                case ISOLATE:
                    renderedWord.insert(0, Glyph.ISOL_ZHI);                        // normal
                    break;
                case INITIAL:
                    renderedWord.insert(0, Glyph.INIT_ZHI);                        // normal
                    break;
                case MEDIAL:
                    renderedWord.insert(0, Glyph.MEDI_ZHI);                        // normal
                    break;
                case FINAL:
                    renderedWord.insert(0, Glyph.FINA_ZHI);                        // normal
                    break;
            }
            glyphShapeBelow = Shape.TOOTH;
        }

        private void handleCHI(StringBuilder renderedWord) {
            switch (location) {
                case ISOLATE:
                    renderedWord.insert(0, Glyph.ISOL_CHI);                        // normal
                    break;
                case INITIAL:
                    renderedWord.insert(0, Glyph.INIT_CHI);                        // normal
                    break;
                case MEDIAL:
                    renderedWord.insert(0, Glyph.MEDI_CHI);                        // normal
                    break;
                case FINAL:
                    renderedWord.insert(0, Glyph.FINA_CHI);                        // normal
                    break;
            }
            glyphShapeBelow = Shape.STEM;
        }

        private void handleNNBS(StringBuilder renderedWord) {
            renderedWord.insert(0, Glyph.SUFFIX_SPACE);
        }

        private void handleNirugu(StringBuilder renderedWord) {
            renderedWord.insert(0, Glyph.NIRUGU);
            glyphShapeBelow = Shape.STEM;
        }

        private void handleNonPrintingChar(StringBuilder renderedWord) {
            renderedWord.insert(0, Uni.WJ);
        }

        private static boolean needsLongToothU(CharSequence word, int uIndex) {

            if (uIndex < 0) return false;

            if (word.charAt(uIndex) != Uni.OE
                    && word.charAt(uIndex) != Uni.UE) return false;

            if (uIndex == 0) return true;

            if (uIndex == 1) {
                if (isConsonant(word.charAt(0))) {
                    // strange BUU exception
                    return !BUU_EXCEPTION.contentEquals(word);
                }
            }

            //noinspection SimplifiableIfStatement
            if (uIndex == 2) {
                return isConsonant(word.charAt(0)) && isFVS(word.charAt(1));
            }

            return false;
        }

        private boolean isRoundLetterIncludingQG(char character) {
            return (character == Uni.BA || character == Uni.PA || character == Uni.QA ||
                    character == Uni.GA || character == Uni.FA || character == Uni.KA ||
                    character == Uni.KHA);
        }

        private boolean isRoundLetter(char character) {
            return (character == Uni.BA || character == Uni.PA || character == Uni.FA ||
                    character == Uni.KA || character == Uni.KHA);
        }

        private boolean isTwoPartNameInitialVowel(char vowel, char fvs) {
            // XXX There is no way to recognize initial E or EE
            return (vowel == Uni.A && fvs == Uni.FVS1) ||
                    (vowel == Uni.I && fvs == Uni.FVS1) ||
                    (vowel == Uni.O && fvs == Uni.FVS1) ||
                    (vowel == Uni.U && fvs == Uni.FVS1) ||
                    (vowel == Uni.OE && fvs == Uni.FVS2) ||
                    (vowel == Uni.UE && fvs == Uni.FVS2);
        }

        private static boolean isOuVowel(char character) {
            return (character >= Uni.O && character <= Uni.UE);
        }

        // Starts at the end of the word and works up
        // if mixed genders only reports the first one from the bottom
        // returns null if word does not end in a valid Mongolian character
        static Gender getGender(CharSequence word) {
            // check that word is valid mongolian
            if (word == null || word.length() == 0) return null;
            int length = word.length();
            char lastChar = word.charAt(length - 1);
            if (!isMongolian(lastChar)) return null;
            return getWordGenderAboveIndex(length, word);
        }

        // assumes that word is valid mongolian
        // this starts at the index and works up
        // If there are mixed genders then only the first will be reported
        // (could add a Gender.MIXED form)
        private static Gender getWordGenderAboveIndex(int index, CharSequence word) {
            for (int i = index - 1; i >= 0; i--) {
                if (isMasculineVowel(word.charAt(i))) {
                    return Gender.MASCULINE;
                } else if (isFeminineVowel(word.charAt(i))) {
                    return Gender.FEMININE;
                }
            }
            return Gender.NEUTER;
        }
    }

    private static class MenksoftWord {

        final static char SPACE = ' ';

        private CharSequence inputWord;
        private Location location;

        MenksoftWord(CharSequence menksoftWord) {
            this.inputWord = menksoftWord;
        }

        private void updateLocation(char charAbove, char charBelow) {

            boolean isTop = !isMenksoftLetter(charAbove);
            boolean isBottom = !isMenksoftLetter(charBelow);
            if (isTop) {
                if (isBottom) {
                    location = Location.ISOLATE;
                } else {
                    location = Location.INITIAL;
                }
            } else {
                if (isBottom) {
                    location = Location.FINAL;
                } else {
                    location = Location.MEDIAL;
                }
            }
        }

        private boolean isMenksoftLetter(char character) {
            return character >= Glyph.A_START && character <= Glyph.MENKSOFT_END;
        }

        private boolean isMenksoftConsonant(char character) {
            return character >= Glyph.NA_START && character <= Glyph.FINA_CHI;
        }

        private boolean isMenksoftVowel(char character) {
            return isMenksoftLetter(character) && !isMenksoftConsonant(character);
        }

        String convertToUnicode() {
            StringBuilder outputString = new StringBuilder();

            if (inputWord == null || inputWord.length() == 0) {
                return "";
            }

            char charAbove = 0;
            char currentChar = inputWord.charAt(0);
            final int length = inputWord.length();
            for (int i = 0; i < length; i++) {

                char charBelow = (i < length - 1) ? inputWord.charAt(i + 1) : 0;

                updateLocation(charAbove, charBelow);

                if (isMenksoftSpaceChar(currentChar)) {                    // space
                    handleSpace(outputString, currentChar, charBelow);
                } else if (currentChar < Glyph.A_START) {                  // punctuation
                    handlePunctuation(outputString, currentChar);
                } else if (currentChar < Glyph.E_START) {                  // A
                    handleA(outputString, currentChar);
                } else if (currentChar < Glyph.I_START) {                  // E
                    handleE(outputString, currentChar);
                } else if (currentChar < Glyph.O_START) {                  // I
                    handleI(outputString, currentChar, charAbove, charBelow);
                } else if (currentChar < Glyph.U_START) {                  // O
                    handleO(outputString, currentChar);
                } else if (currentChar < Glyph.OE_START) {                 // U
                    handleU(outputString, currentChar);
                } else if (currentChar < Glyph.UE_START) {                 // OE
                    handleOE(outputString, currentChar);
                } else if (currentChar < Glyph.EE_START) {                 // UE
                    handleUE(outputString, currentChar);
                } else if (currentChar < Glyph.NA_START) {                 // EE
                    handleEE(outputString, currentChar, charAbove, charBelow);
                } else if (isANG(currentChar)) {                           // ANG
                    // handling ANG before NA because NA is appears
                    // before and after ANG
                    handleAng(outputString, currentChar);
                } else if (currentChar < Glyph.BA_START) {                 // NA
                    handleNa(outputString, currentChar, charAbove, charBelow);
                } else if (currentChar < Glyph.PA_START) {                 // BA
                    handleBa(outputString, currentChar);
                } else if (currentChar < Glyph.QA_START) {                 // PA
                    handlePa(outputString, currentChar);
                } else if (currentChar < Glyph.GA_START) {                 // QA
                    handleQa(outputString, currentChar, charBelow);
                } else if (currentChar < Glyph.MA_START) {                 // GA
                    handleGa(outputString, currentChar);
                } else if (currentChar < Glyph.LA_START) {                 // MA
                    handleMa(outputString, currentChar);
                } else if (currentChar < Glyph.SA_START) {                 // LA
                    handleLa(outputString, currentChar);
                } else if (currentChar < Glyph.SHA_START) {                // SA
                    handleSa(outputString, currentChar);
                } else if (currentChar < Glyph.TA_START) {                 // SHA
                    handleSha(outputString, currentChar);
                } else if (currentChar < Glyph.DA_START) {                 // TA
                    handleTa(outputString, currentChar);
                } else if (currentChar < Glyph.CHA_START) {                // DA
                    handleDa(outputString, currentChar);
                } else if (currentChar < Glyph.JA_START) {                 // CHA
                    handleCha(outputString, currentChar);
                } else if (currentChar < Glyph.YA_START) {                 // JA
                    handleJa(outputString, currentChar);
                } else if (currentChar < Glyph.RA_START) {                 // YA
                    handleYa(outputString, currentChar, charAbove, charBelow);
                } else if (currentChar < Glyph.WA_START) {                 // RA
                    handleRa(outputString, currentChar);
                } else if (currentChar < Glyph.FA_START) {                 // WA
                    handleWa(outputString, currentChar, charAbove, charBelow);
                } else if (currentChar < Glyph.KA_START) {                 // FA
                    handleFa(outputString, currentChar);
                } else if (currentChar < Glyph.KHA_START) {                // KA
                    handleKa(outputString, currentChar);
                } else if (currentChar < Glyph.TSA_START) {                // KHA
                    handleKha(outputString, currentChar);
                } else if (currentChar < Glyph.ZA_START) {                 // TSA
                    handleTsa(outputString, currentChar);
                } else if (currentChar < Glyph.HAA_START) {                // ZA
                    handleZa(outputString, currentChar);
                } else if (currentChar < Glyph.ZRA_START) {                // HAA
                    handleHaa(outputString, currentChar);
                } else if (currentChar < Glyph.LHA_START) {                // ZRA
                    handleZra(outputString, currentChar);
                } else if (currentChar < Glyph.ZHI_START) {                // LHA
                    handleLha(outputString, currentChar);
                } else if (currentChar < Glyph.CHI_START) {                // ZHI
                    handleZhi(outputString);
                } else if (currentChar <= Glyph.MENKSOFT_END) {            // CHI
                    handleChi(outputString);
                }

                charAbove = currentChar;

                // fix missing space
                if (isMenksoftFinalIsolateGlyph(currentChar)
                        && isMenksoftInitialIsolateGlyph(charBelow)) {
                    outputString.append(SPACE);
                    charAbove = 0;
                }

                currentChar = charBelow;
            }

            return outputString.toString();
        }

        private boolean isMenksoftInitialIsolateGlyph(char character) {
            //noinspection SimplifiableIfStatement
            if (character == 0) return false;
            return character == Glyph.ISOL_A ||
                    character == Glyph.ISOL_A_FVS1 ||
                    character == Glyph.INIT_A ||
                    character == Glyph.MEDI_A_FVS2 ||
                    character == Glyph.ISOL_E ||
                    character == Glyph.INIT_E ||
                    character == Glyph.INIT_E_FVS1 ||
                    character == Glyph.ISOL_I ||
                    character == Glyph.ISOL_I_SUFFIX ||
                    character == Glyph.INIT_I ||
                    character == Glyph.INIT_O ||
                    character == Glyph.ISOL_O ||
                    character == Glyph.ISOL_U ||
                    character == Glyph.U_START ||
                    character == Glyph.ISOL_OE ||
                    character == Glyph.INIT_OE ||
                    character == Glyph.ISOL_OE_FVS1 ||
                    character == Glyph.INIT_UE ||
                    character == Glyph.UE_START ||
                    character == Glyph.ISOL_UE_FVS1 ||
                    character == Glyph.ISOL_EE ||
                    character == Glyph.INIT_EE ||
                    character == Glyph.INIT_NA_STEM ||
                    character == Glyph.INIT_NA_TOOTH ||
                    character == Glyph.INIT_NA_FVS1_STEM ||
                    character == Glyph.INIT_NA_FVS1_TOOTH ||
                    character == Glyph.INIT_BA ||
                    character == Glyph.INIT_BA_OU ||
                    character == Glyph.INIT_BA_STEM ||
                    character == Glyph.INIT_PA ||
                    character == Glyph.INIT_PA_OU ||
                    character == Glyph.INIT_PA_STEM ||
                    character == Glyph.INIT_QA_FEM ||
                    character == Glyph.INIT_QA_FEM_OU ||
                    character == Glyph.INIT_QA_FVS1_FEM ||
                    character == Glyph.INIT_QA_FVS1_FEM_OU ||
                    character == Glyph.INIT_QA_FVS1_STEM ||
                    character == Glyph.INIT_QA_FVS1_TOOTH ||
                    character == Glyph.INIT_QA_STEM ||
                    character == Glyph.INIT_QA_TOOTH ||
                    character == Glyph.INIT_GA_FEM ||
                    character == Glyph.INIT_GA_FEM_OU ||
                    character == Glyph.INIT_GA_FVS1_STEM ||
                    character == Glyph.INIT_GA_FVS1_TOOTH ||
                    character == Glyph.INIT_GA_STEM ||
                    character == Glyph.INIT_GA_TOOTH ||
                    character == Glyph.INIT_MA_TOOTH ||
                    character == Glyph.INIT_MA_STEM_LONG ||
                    character == Glyph.INIT_LA_TOOTH ||
                    character == Glyph.INIT_LA_STEM_LONG ||
                    character == Glyph.INIT_SA_STEM ||
                    character == Glyph.INIT_SA_TOOTH ||
                    character == Glyph.INIT_SHA_STEM ||
                    character == Glyph.INIT_SHA_TOOTH ||
                    character == Glyph.INIT_TA_STEM ||
                    character == Glyph.INIT_TA_TOOTH ||
                    character == Glyph.INIT_DA_FVS1 ||
                    character == Glyph.INIT_DA_STEM ||
                    character == Glyph.INIT_DA_TOOTH ||
                    character == Glyph.INIT_CHA ||
                    character == Glyph.INIT_JA_STEM ||
                    character == Glyph.INIT_JA_TOOTH ||
                    character == Glyph.INIT_YA ||
                    character == Glyph.INIT_YA_FVS1 ||
                    character == Glyph.INIT_RA_STEM ||
                    character == Glyph.INIT_RA_TOOTH ||
                    character == Glyph.INIT_WA ||
                    character == Glyph.INIT_FA ||
                    character == Glyph.INIT_FA_OU ||
                    character == Glyph.INIT_FA_STEM ||
                    character == Glyph.INIT_KA ||
                    character == Glyph.INIT_KA_OU ||
                    character == Glyph.INIT_KHA ||
                    character == Glyph.INIT_KHA_OU ||
                    character == Glyph.INIT_TSA ||
                    character == Glyph.INIT_ZA ||
                    character == Glyph.INIT_HAA ||
                    character == Glyph.INIT_ZRA ||
                    character == Glyph.INIT_LHA;
        }

        private boolean isMenksoftFinalIsolateGlyph(char character) {
            //noinspection SimplifiableIfStatement
            if (character == 0) return false;
            return character == Glyph.ISOL_A ||
                    character == Glyph.ISOL_A_FVS1 ||
                    character == Glyph.FINA_A ||
                    character == Glyph.FINA_A_BP ||
                    character == Glyph.FINA_A_FVS1 ||
                    character == Glyph.FINA_A_MVS ||
                    character == Glyph.ISOL_E ||
                    character == Glyph.FINA_E ||
                    character == Glyph.FINA_E_BP ||
                    character == Glyph.FINA_E_MVS ||
                    character == Glyph.ISOL_I ||
                    character == Glyph.ISOL_I_SUFFIX ||
                    character == Glyph.FINA_I ||
                    character == Glyph.FINA_I_BP ||
                    character == Glyph.ISOL_O ||
                    character == Glyph.FINA_O ||
                    character == Glyph.FINA_O_FVS1 ||
                    character == Glyph.ISOL_U ||
                    character == Glyph.FINA_U ||
                    character == Glyph.FINA_U_BP ||
                    character == Glyph.FINA_U_FVS1 ||
                    character == Glyph.ISOL_OE ||
                    character == Glyph.ISOL_OE_FVS1 ||
                    character == Glyph.FINA_OE ||
                    character == Glyph.FINA_OE_BP ||
                    character == Glyph.FINA_OE_FVS1 ||
                    character == Glyph.FINA_OE_FVS1_BP ||
                    character == Glyph.FINA_OE_FVS2 ||
                    character == Glyph.ISOL_UE ||
                    character == Glyph.ISOL_UE_FVS1 ||
                    character == Glyph.FINA_UE ||
                    character == Glyph.FINA_UE_BP ||
                    character == Glyph.FINA_UE_FVS1 ||
                    character == Glyph.FINA_UE_FVS1_BP ||
                    character == Glyph.FINA_UE_FVS2 ||
                    character == Glyph.ISOL_EE ||
                    character == Glyph.FINA_EE ||
                    character == Glyph.FINA_NA ||
                    character == Glyph.FINA_ANG ||
                    character == Glyph.FINA_BA ||
                    character == Glyph.FINA_BA_FVS1 ||
                    character == Glyph.FINA_PA ||
                    character == Glyph.FINA_QA ||
                    character == Glyph.FINA_GA ||
                    character == Glyph.FINA_GA_FVS2 ||
                    character == Glyph.FINA_MA ||
                    character == Glyph.FINA_LA ||
                    character == Glyph.FINA_SA ||
                    character == Glyph.FINA_SA_FVS1 ||
                    character == Glyph.FINA_SHA ||
                    character == Glyph.FINA_TA ||
                    character == Glyph.FINA_DA ||
                    character == Glyph.FINA_DA_FVS1 ||
                    character == Glyph.FINA_CHA ||
                    character == Glyph.FINA_JA ||
                    character == Glyph.FINA_YA ||
                    character == Glyph.FINA_RA ||
                    character == Glyph.FINA_WA ||
                    character == Glyph.FINA_WA_FVS1 ||
                    character == Glyph.FINA_FA ||
                    character == Glyph.FINA_KA ||
                    character == Glyph.FINA_KHA ||
                    character == Glyph.FINA_TSA ||
                    character == Glyph.FINA_ZA ||
                    character == Glyph.FINA_HAA ||
                    character == Glyph.FINA_ZRA;
        }

        private boolean isANG(char currentChar) {
            return (currentChar >= Glyph.ANG_START &&
                    currentChar <= Glyph.ANG_END);
        }

        private boolean startsWithNnbsSuffix(StringBuilder outputString) {
            return (outputString.length() != 0)
                    && outputString.charAt(0) == Uni.NNBS;
        }

        private void handleA(StringBuilder outputString, char currentChar) {
            switch (location) {
                case ISOLATE:
                    switch (currentChar) {
                        case Glyph.ISOL_A_FVS1:
                            outputString.append(Uni.A);
                            outputString.append(Uni.FVS1);
                            break;
                        case Glyph.INIT_A:
                            outputString.append(Uni.A);
                            outputString.append(Uni.ZWJ);
                            break;
                        case Glyph.MEDI_A_FVS2:
                            outputString.append(Uni.A);
                            outputString.append(Uni.FVS2);
                            outputString.append(Uni.ZWJ);
                            break;
                        case Glyph.FINA_A:
                            outputString.append(Uni.ZWJ);
                            outputString.append(Uni.A);
                            break;
                        case Glyph.FINA_A_BP:
                        case Glyph.FINA_A_FVS1:
                        case Glyph.FINA_A_MVS:
                            outputString.append(Uni.ZWJ);
                            outputString.append(Uni.A);
                            outputString.append(Uni.FVS1);
                            break;
                        case Glyph.MEDI_A:
                        case Glyph.MEDI_A_BP:
                        case Glyph.MEDI_A_UNKNOWN:
                            outputString.append(Uni.ZWJ);
                            outputString.append(Uni.A);
                            outputString.append(Uni.ZWJ);
                            break;
                        case Glyph.MEDI_A_FVS1:
                            outputString.append(Uni.ZWJ);
                            outputString.append(Uni.A);
                            outputString.append(Uni.FVS1);
                            outputString.append(Uni.ZWJ);
                            break;
                        default:
                            outputString.append(Uni.A);
                    }
                    break;
                case INITIAL:
                    switch (currentChar) {
                        case Glyph.MEDI_A:
                        case Glyph.MEDI_A_BP:
                        case Glyph.MEDI_A_UNKNOWN:
                            outputString.append(Uni.ZWJ);
                            outputString.append(Uni.A);
                            break;
                        case Glyph.MEDI_A_FVS1:
                            outputString.append(Uni.ZWJ);
                            outputString.append(Uni.A);
                            outputString.append(Uni.FVS1);
                            break;
                        default:
                            outputString.append(Uni.A);
                    }
                    break;
                case MEDIAL:
                    outputString.append(Uni.A);
                    if (currentChar == Glyph.MEDI_A_FVS1) {
                        outputString.append(Uni.FVS1);
                    }
                    break;
                case FINAL:
                    switch (currentChar) {
                        case Glyph.FINA_A_MVS:
                            outputString.append(Uni.MVS);
                            outputString.append(Uni.A);
                            break;
                        case Glyph.MEDI_A:
                        case Glyph.MEDI_A_BP:
                        case Glyph.MEDI_A_UNKNOWN:
                            outputString.append(Uni.A);
                            outputString.append(Uni.ZWJ);
                            break;
                        case Glyph.MEDI_A_FVS1:
                            outputString.append(Uni.A);
                            outputString.append(Uni.FVS1);
                            outputString.append(Uni.ZWJ);
                            break;
                        default:
                            outputString.append(Uni.A);
                    }
                    break;
            }
        }

        private void handleE(StringBuilder outputString, char currentChar) {

            switch (location) {
                case ISOLATE:
                    switch (currentChar) {
                        case Glyph.INIT_E:
                            outputString.append(Uni.E);
                            outputString.append(Uni.ZWJ);
                            break;
                        case Glyph.INIT_E_FVS1:
                            outputString.append(Uni.E);
                            outputString.append(Uni.FVS1);
                            outputString.append(Uni.ZWJ);
                            break;
                        case Glyph.FINA_E:
                            outputString.append(Uni.ZWJ);
                            outputString.append(Uni.E);
                            break;
                        case Glyph.FINA_E_BP:
                        case Glyph.FINA_E_FVS1:
                        case Glyph.FINA_E_MVS:
                            outputString.append(Uni.ZWJ);
                            outputString.append(Uni.E);
                            outputString.append(Uni.FVS1);
                            break;
                        case Glyph.MEDI_E:
                        case Glyph.MEDI_E_BP:
                        case Glyph.MEDI_E_UNKNOWN:
                            outputString.append(Uni.ZWJ);
                            outputString.append(Uni.E);
                            outputString.append(Uni.ZWJ);
                            break;
                        default:
                            outputString.append(Uni.E);
                    }
                    break;
                case INITIAL:
                    switch (currentChar) {
                        case Glyph.INIT_E_FVS1:
                            outputString.append(Uni.E);
                            outputString.append(Uni.FVS1);
                            break;
                        case Glyph.MEDI_E:
                        case Glyph.MEDI_E_BP:
                        case Glyph.MEDI_E_UNKNOWN:
                            outputString.append(Uni.ZWJ);
                            outputString.append(Uni.E);
                            break;
                        default:
                            outputString.append(Uni.E);
                    }
                    break;
                case MEDIAL:
                    outputString.append(Uni.E);
                    break;
                case FINAL:
                    switch (currentChar) {
                        case Glyph.FINA_E_MVS:
                            outputString.append(Uni.MVS);
                            outputString.append(Uni.E);
                            break;
                        case Glyph.MEDI_E:
                        case Glyph.MEDI_E_BP:
                        case Glyph.MEDI_E_UNKNOWN:
                            outputString.append(Uni.E);
                            outputString.append(Uni.ZWJ);
                            break;
                        default:
                            outputString.append(Uni.E);
                    }
                    break;
            }
        }

        private void handleI(StringBuilder outputString,
                             char currentChar, char charAbove, char charBelow) {

            switch (location) {
                case ISOLATE:
                    switch (currentChar) {
                        case Glyph.INIT_I:
                            outputString.append(Uni.I);
                            outputString.append(Uni.ZWJ);
                            break;
                        case Glyph.FINA_I:
                        case Glyph.FINA_I_BP:
                        case Glyph.ISOL_I_SUFFIX:
                            if (!startsWithNnbsSuffix(outputString)) {
                                outputString.append(Uni.ZWJ);
                            }
                            outputString.append(Uni.I);
                            break;
                        case Glyph.MEDI_I:
                        case Glyph.MEDI_I_BP:
                        case Glyph.MEDI_I_SUFFIX:
                            outputString.append(Uni.ZWJ);
                            outputString.append(Uni.I);
                            outputString.append(Uni.ZWJ);
                            break;
                        case Glyph.MEDI_I_FVS1:
                            outputString.append(Uni.ZWJ);
                            outputString.append(Uni.I);
                            outputString.append(Uni.FVS1);
                            outputString.append(Uni.ZWJ);
                            break;
                        case Glyph.MEDI_I_DOUBLE_TOOTH:
                            outputString.append(Uni.ZWJ);
                            outputString.append(Uni.YA);
                            outputString.append(Uni.I);
                            outputString.append(Uni.ZWJ);
                            break;
                        default:
                            outputString.append(Uni.I);
                    }
                    break;
                case INITIAL:
                    switch (currentChar) {
                        case Glyph.MEDI_I:
                        case Glyph.MEDI_I_BP:
                        case Glyph.MEDI_I_SUFFIX:
                            if (!startsWithNnbsSuffix(outputString)) {
                                outputString.append(Uni.ZWJ);
                            }
                            outputString.append(Uni.I);
                            break;
                        case Glyph.MEDI_I_FVS1:
                            outputString.append(Uni.ZWJ);
                            outputString.append(Uni.I);
                            outputString.append(Uni.FVS1);
                            break;
                        case Glyph.MEDI_I_DOUBLE_TOOTH:
                            outputString.append(Uni.ZWJ);
                            outputString.append(Uni.YA);
                            outputString.append(Uni.I);
                            break;
                        default:
                            outputString.append(Uni.I);
                    }
                    break;
                case MEDIAL:
                    switch (currentChar) {
                        case Glyph.MEDI_I:
                        case Glyph.MEDI_I_BP:
                            outputString.append(Uni.I);
                            if (isLikeIofNaima(charAbove, charBelow)) {
                                // override double tooth for words like NAIMA
                                outputString.append(Uni.FVS2);
                            }
                            break;
                        case Glyph.MEDI_I_FVS1:
                            outputString.append(Uni.I);
                            outputString.append(Uni.FVS1);
                            break;
                        default:
                            outputString.append(Uni.I);
                    }
                    break;
                case FINAL:
                    switch (currentChar) {
                        case Glyph.MEDI_I:
                        case Glyph.MEDI_I_BP:
                        case Glyph.MEDI_I_SUFFIX:
                            outputString.append(Uni.I);
                            outputString.append(Uni.ZWJ);
                            break;
                        case Glyph.MEDI_I_FVS1:
                            outputString.append(Uni.I);
                            outputString.append(Uni.FVS1);
                            outputString.append(Uni.ZWJ);
                            break;
                        case Glyph.MEDI_I_DOUBLE_TOOTH:
                            outputString.append(Uni.YA);
                            outputString.append(Uni.I);
                            outputString.append(Uni.ZWJ);
                            break;
                        default:
                            outputString.append(Uni.I);
                    }
                    break;
            }
        }

        private boolean isLikeIofNaima(char charAbove, char charBelow) {
            return isMenksoftA(charAbove) && isMenksoftM(charBelow);
        }

        private boolean isMenksoftA(char character) {
            return character >= Glyph.A_START && character <= Glyph.MEDI_A_UNKNOWN;
        }

        private boolean isMenksoftM(char character) {
            return character >= Glyph.INIT_MA_TOOTH && character <= Glyph.MEDI_MA_BP;
        }

        private void handleO(StringBuilder outputString, char currentChar) {
            switch (location) {
                case ISOLATE:
                    switch (currentChar) {
                        case Glyph.INIT_O:
                            outputString.append(Uni.O);
                            outputString.append(Uni.ZWJ);
                            break;
                        case Glyph.FINA_O:
                            if (startsWithNnbsSuffix(outputString)) {
                                // substituting more standard U suffix
                                outputString.append(Uni.U);
                            } else {
                                outputString.append(Uni.ZWJ);
                                outputString.append(Uni.O);
                            }
                            break;
                        case Glyph.FINA_O_FVS1:
                        case Glyph.FINA_O_BP:
                            outputString.append(Uni.ZWJ);
                            outputString.append(Uni.O);
                            outputString.append(Uni.FVS1);
                            break;
                        case Glyph.MEDI_O_FVS1:
                            outputString.append(Uni.ZWJ);
                            outputString.append(Uni.O);
                            outputString.append(Uni.FVS1);
                            outputString.append(Uni.ZWJ);
                            break;
                        case Glyph.MEDI_O:
                        case Glyph.MEDI_O_BP:
                            outputString.append(Uni.ZWJ);
                            outputString.append(Uni.O);
                            outputString.append(Uni.ZWJ);
                            break;
                        default:
                            outputString.append(Uni.O);
                    }
                    break;
                case INITIAL:
                    switch (currentChar) {
                        case Glyph.MEDI_O_FVS1:
                            outputString.append(Uni.ZWJ);
                            outputString.append(Uni.O);
                            outputString.append(Uni.FVS1);
                            break;
                        case Glyph.MEDI_O:
                        case Glyph.MEDI_O_BP:
                            outputString.append(Uni.ZWJ);
                            outputString.append(Uni.O);
                            break;
                        default:
                            outputString.append(Uni.O);
                    }
                    break;
                case MEDIAL:
                    switch (currentChar) {
                        case Glyph.MEDI_O_FVS1:
                            outputString.append(Uni.O);
                            outputString.append(Uni.FVS1);
                            break;
                        default:
                            outputString.append(Uni.O);
                    }
                    break;
                case FINAL:
                    outputString.append(Uni.O);
                    switch (currentChar) {
                        case Glyph.FINA_O_FVS1:
                            outputString.append(Uni.FVS1);
                            break;
                        case Glyph.MEDI_O_FVS1:
                            outputString.append(Uni.FVS1);
                            outputString.append(Uni.ZWJ);
                            break;
                        case Glyph.MEDI_O:
                        case Glyph.MEDI_O_BP:
                            outputString.append(Uni.ZWJ);
                            break;
                    }
                    break;
            }
        }

        private void handleU(StringBuilder outputString, char currentChar) {
            switch (location) {
                case ISOLATE:
                    switch (currentChar) {
                        case Glyph.INIT_U:
                            outputString.append(Uni.U);
                            outputString.append(Uni.ZWJ);
                            break;
                        case Glyph.FINA_U:
                            if (!startsWithNnbsSuffix(outputString)) {
                                outputString.append(Uni.ZWJ);
                            }
                            outputString.append(Uni.U);
                            break;
                        case Glyph.FINA_U_FVS1:
                        case Glyph.FINA_U_BP:
                            outputString.append(Uni.ZWJ);
                            outputString.append(Uni.U);
                            outputString.append(Uni.FVS1);
                            break;
                        case Glyph.MEDI_U_FVS1:
                            outputString.append(Uni.ZWJ);
                            outputString.append(Uni.U);
                            outputString.append(Uni.FVS1);
                            outputString.append(Uni.ZWJ);
                            break;
                        case Glyph.MEDI_U:
                        case Glyph.MEDI_U_BP:
                            outputString.append(Uni.ZWJ);
                            outputString.append(Uni.U);
                            outputString.append(Uni.ZWJ);
                            break;
                        default:
                            outputString.append(Uni.U);
                    }
                    break;
                case INITIAL:
                    switch (currentChar) {
                        case Glyph.MEDI_U_FVS1:
                            outputString.append(Uni.ZWJ);
                            outputString.append(Uni.U);
                            outputString.append(Uni.FVS1);
                            break;
                        case Glyph.MEDI_U:
                        case Glyph.MEDI_U_BP:
                            if (!startsWithNnbsSuffix(outputString)) {
                                outputString.append(Uni.ZWJ);
                            }
                            outputString.append(Uni.U);
                            break;
                        default:
                            outputString.append(Uni.U);
                    }
                    break;
                case MEDIAL:
                    switch (currentChar) {
                        case Glyph.MEDI_U_FVS1:
                            outputString.append(Uni.U);
                            outputString.append(Uni.FVS1);
                            break;
                        default:
                            outputString.append(Uni.U);
                    }
                    break;
                case FINAL:
                    outputString.append(Uni.U);
                    switch (currentChar) {
                        case Glyph.FINA_U_FVS1:
                            outputString.append(Uni.FVS1);
                            break;
                        case Glyph.MEDI_U_FVS1:
                            outputString.append(Uni.FVS1);
                            outputString.append(Uni.ZWJ);
                            break;
                        case Glyph.MEDI_U:
                        case Glyph.MEDI_U_BP:
                            outputString.append(Uni.ZWJ);
                            break;
                    }
                    break;
            }
        }

        private void handleOE(StringBuilder outputString, char currentChar) {
            switch (location) {
                case ISOLATE:
                    switch (currentChar) {
                        case Glyph.ISOL_OE_FVS1:
                            // substituting UE because it is defined in Unicode
                            outputString.append(Uni.UE);
                            outputString.append(Uni.FVS1);
                            break;
                        case Glyph.INIT_OE:
                            outputString.append(Uni.OE);
                            outputString.append(Uni.ZWJ);
                            break;
                        case Glyph.FINA_OE:
                            if (startsWithNnbsSuffix(outputString)) {
                                // substituting more standard UE suffix
                                outputString.append(Uni.UE);
                            } else {
                                outputString.append(Uni.ZWJ);
                                outputString.append(Uni.OE);
                            }
                            break;
                        case Glyph.FINA_OE_FVS1:
                        case Glyph.FINA_OE_FVS1_BP:
                            outputString.append(Uni.ZWJ);
                            outputString.append(Uni.OE);
                            outputString.append(Uni.FVS1);
                            break;
                        case Glyph.FINA_OE_FVS2:
                        case Glyph.FINA_OE_BP:
                            outputString.append(Uni.ZWJ);
                            // substituting because undefined in unicode
                            outputString.append(Uni.O);
                            outputString.append(Uni.FVS1);
                            break;
                        case Glyph.MEDI_OE_FVS2:
                            outputString.append(Uni.ZWJ);
                            outputString.append(Uni.OE);
                            outputString.append(Uni.FVS2);
                            outputString.append(Uni.ZWJ);
                            break;
                        case Glyph.MEDI_OE:
                        case Glyph.MEDI_OE_BP:
                            outputString.append(Uni.ZWJ);
                            outputString.append(Uni.OE);
                            outputString.append(Uni.ZWJ);
                            break;
                        case Glyph.MEDI_OE_FVS1:
                        case Glyph.MEDI_OE_FVS1_BP:
                            outputString.append(Uni.ZWJ);
                            outputString.append(Uni.OE);
                            outputString.append(Uni.FVS1);
                            outputString.append(Uni.ZWJ);
                            break;
                        default:
                            outputString.append(Uni.OE);
                    }
                    break;
                case INITIAL:
                    switch (currentChar) {
                        case Glyph.MEDI_OE_FVS2:
                            outputString.append(Uni.ZWJ);
                            outputString.append(Uni.OE);
                            outputString.append(Uni.FVS2);
                            break;
                        case Glyph.MEDI_OE:
                        case Glyph.MEDI_OE_BP:
                            outputString.append(Uni.ZWJ);
                            outputString.append(Uni.OE);
                            break;
                        case Glyph.MEDI_OE_FVS1:
                        case Glyph.MEDI_OE_FVS1_BP:
                            outputString.append(Uni.ZWJ);
                            outputString.append(Uni.OE);
                            outputString.append(Uni.FVS1);
                            break;
                        default:
                            outputString.append(Uni.OE);
                    }
                    break;
                case MEDIAL:
                    switch (currentChar) {
                        case Glyph.MEDI_OE_FVS2:
                            outputString.append(Uni.OE);
                            outputString.append(Uni.FVS2);
                            break;
                        default:
                            outputString.append(Uni.OE);
                    }
                    break;
                case FINAL:
                    outputString.append(Uni.OE);
                    switch (currentChar) {
                        case Glyph.FINA_OE_FVS1:
                        case Glyph.FINA_OE_FVS1_BP:
                            outputString.append(Uni.FVS1);
                            break;
                        case Glyph.MEDI_OE_FVS2:
                            outputString.append(Uni.FVS2);
                            outputString.append(Uni.ZWJ);
                            break;
                        case Glyph.MEDI_OE:
                        case Glyph.MEDI_OE_BP:
                            outputString.append(Uni.ZWJ);
                            break;
                        case Glyph.MEDI_OE_FVS1:
                        case Glyph.MEDI_OE_FVS1_BP:
                            outputString.append(Uni.FVS1);
                            outputString.append(Uni.ZWJ);
                            break;
                    }
                    break;
            }
        }

        private void handleUE(StringBuilder outputString, char currentChar) {
            switch (location) {
                case ISOLATE:
                    switch (currentChar) {
                        case Glyph.ISOL_UE_FVS1:
                            outputString.append(Uni.UE);
                            outputString.append(Uni.FVS1);
                            break;
                        case Glyph.INIT_UE:
                            outputString.append(Uni.UE);
                            outputString.append(Uni.ZWJ);
                            break;
                        case Glyph.FINA_UE:
                            if (!startsWithNnbsSuffix(outputString)) {
                                outputString.append(Uni.ZWJ);
                            }
                            outputString.append(Uni.UE);
                            break;
                        case Glyph.FINA_UE_FVS1:
                        case Glyph.FINA_UE_FVS1_BP:
                            outputString.append(Uni.ZWJ);
                            outputString.append(Uni.UE);
                            outputString.append(Uni.FVS1);
                            break;
                        case Glyph.FINA_UE_FVS2:
                        case Glyph.FINA_UE_BP:
                            outputString.append(Uni.ZWJ);
                            // substituting because undefined in unicode
                            outputString.append(Uni.U);
                            outputString.append(Uni.FVS1);
                            break;
                        case Glyph.MEDI_UE_FVS2:
                            outputString.append(Uni.ZWJ);
                            outputString.append(Uni.UE);
                            outputString.append(Uni.FVS2);
                            outputString.append(Uni.ZWJ);
                            break;
                        case Glyph.MEDI_UE:
                        case Glyph.MEDI_UE_BP:
                            outputString.append(Uni.ZWJ);
                            outputString.append(Uni.UE);
                            outputString.append(Uni.ZWJ);
                            break;
                        case Glyph.MEDI_UE_FVS1:
                        case Glyph.MEDI_UE_FVS1_BP:
                            outputString.append(Uni.ZWJ);
                            outputString.append(Uni.UE);
                            outputString.append(Uni.FVS1);
                            outputString.append(Uni.ZWJ);
                            break;
                        default:
                            outputString.append(Uni.UE);
                    }
                    break;
                case INITIAL:
                    switch (currentChar) {
                        case Glyph.MEDI_UE_FVS2:
                            outputString.append(Uni.ZWJ);
                            outputString.append(Uni.UE);
                            outputString.append(Uni.FVS2);
                            break;
                        case Glyph.MEDI_UE:
                        case Glyph.MEDI_UE_BP:
                            if (!startsWithNnbsSuffix(outputString)) {
                                outputString.append(Uni.ZWJ);
                            }
                            outputString.append(Uni.UE);
                            break;
                        case Glyph.MEDI_UE_FVS1:
                        case Glyph.MEDI_UE_FVS1_BP:
                            outputString.append(Uni.ZWJ);
                            outputString.append(Uni.UE);
                            outputString.append(Uni.FVS1);
                            break;
                        default:
                            outputString.append(Uni.UE);
                    }
                    break;
                case MEDIAL:
                    switch (currentChar) {
                        case Glyph.MEDI_UE_FVS1:
                            outputString.append(Uni.UE);
                            int index = outputString.length() - 1;
                            if (!MongolWord.needsLongToothU(outputString, index)) {
                                outputString.append(Uni.FVS1);
                            }
                            break;
                        case Glyph.MEDI_UE_FVS2:
                            outputString.append(Uni.UE);
                            outputString.append(Uni.FVS2);
                            break;
                        default:
                            outputString.append(Uni.UE);
                    }
                    break;
                case FINAL:
                    outputString.append(Uni.UE);
                    switch (currentChar) {
                        case Glyph.FINA_UE_FVS1:
                        case Glyph.FINA_UE_FVS1_BP:
                            outputString.append(Uni.FVS1);
                            break;
                        case Glyph.MEDI_UE_FVS2:
                            outputString.append(Uni.FVS2);
                            outputString.append(Uni.ZWJ);
                            break;
                        case Glyph.MEDI_UE:
                        case Glyph.MEDI_UE_BP:
                            outputString.append(Uni.ZWJ);
                            break;
                        case Glyph.MEDI_UE_FVS1:
                        case Glyph.MEDI_UE_FVS1_BP:
                            outputString.append(Uni.FVS1);
                            outputString.append(Uni.ZWJ);
                            break;
                    }
                    break;
            }
        }

        private void handleEE(StringBuilder outputString,
                              char currentChar, char charAbove, char charBelow) {
            switch (location) {
                case ISOLATE:
                    switch (currentChar) {
                        case Glyph.INIT_EE:
                            outputString.append(Uni.EE);
                            outputString.append(Uni.ZWJ);
                            break;
                        case Glyph.MEDI_EE:
                            outputString.append(Uni.ZWJ);
                            outputString.append(Uni.EE);
                            outputString.append(Uni.ZWJ);
                            break;
                        case Glyph.FINA_EE:
                            outputString.append(Uni.ZWJ);
                            outputString.append(Uni.EE);
                            break;
                        default:
                            outputString.append(Uni.EE);
                            break;
                    }
                    break;
                case INITIAL:
                    switch (currentChar) {
                        case Glyph.MEDI_EE:
                            outputString.append(Uni.ZWJ);
                            outputString.append(Uni.EE);
                            break;
                        default:
                            outputString.append(Uni.EE);
                            break;
                    }
                    break;
                case MEDIAL:
                    // replace EE between two vowels with W
                    if (isMenksoftVowel(charAbove)
                            && isMenksoftVowel(charBelow))
                        outputString.append(Uni.WA);
                    else
                        outputString.append(Uni.EE);
                    break;
                case FINAL:
                    switch (currentChar) {
                        case Glyph.MEDI_EE:
                            outputString.append(Uni.EE);
                            outputString.append(Uni.ZWJ);
                            break;
                        default:
                            outputString.append(Uni.EE);
                            break;
                    }
                    break;
            }


        }

        private void handleAng(StringBuilder outputString, char currentChar) {
            if (location == Location.ISOLATE && currentChar == Glyph.FINA_ANG) {
                outputString.append(Uni.ZWJ);
            }
            outputString.append(Uni.ANG);
        }

        private void handleNa(StringBuilder outputString, char currentChar,
                              char charAbove, char charBelow) {
            switch (location) {
                case ISOLATE:
                    switch (currentChar) {
                        case Glyph.INIT_NA_FVS1_STEM:
                        case Glyph.INIT_NA_FVS1_TOOTH:
                            outputString.append(Uni.NA);
                            outputString.append(Uni.FVS1);
                            outputString.append(Uni.ZWJ);
                            break;
                        case Glyph.FINA_NA:
                            outputString.append(Uni.ZWJ);
                            outputString.append(Uni.NA);
                            break;
                        case Glyph.MEDI_NA_FVS2:
                            outputString.append(Uni.ZWJ);
                            outputString.append(Uni.NA);
                            outputString.append(Uni.FVS2);
                            outputString.append(Uni.ZWJ);
                            break;
                        case Glyph.MEDI_NA_STEM:
                        case Glyph.MEDI_NA_TOOTH:
                        case Glyph.MEDI_NA_NG:
                            outputString.append(Uni.ZWJ);
                            outputString.append(Uni.NA);
                            outputString.append(Uni.ZWJ);
                            break;
                        case Glyph.MEDI_NA_FVS1_STEM:
                        case Glyph.MEDI_NA_FVS1_TOOTH:
                        case Glyph.MEDI_NA_FVS1_NG:
                            outputString.append(Uni.ZWJ);
                            outputString.append(Uni.NA);
                            outputString.append(Uni.FVS1);
                            outputString.append(Uni.ZWJ);
                            break;
                        default:
                            outputString.append(Uni.NA);
                            break;
                    }
                    break;
                case INITIAL:
                    switch (currentChar) {
                        case Glyph.INIT_NA_FVS1_STEM:
                        case Glyph.INIT_NA_FVS1_TOOTH:
                            outputString.append(Uni.NA);
                            outputString.append(Uni.FVS1);
                            break;
                        default:
                            outputString.append(Uni.NA);
                            break;
                    }
                    break;
                case MEDIAL:
                    switch (currentChar) {
                        case Glyph.MEDI_NA_NG:
                        case Glyph.MEDI_NA_STEM:
                        case Glyph.MEDI_NA_TOOTH:
                            outputString.append(Uni.NA);
                            if (isMenksoftVowel(charAbove)
                                    && isMenksoftVowel(charBelow)) {
                                outputString.append(Uni.ZWJ);
                            }
                            break;
                        case Glyph.MEDI_NA_FVS1_NG:
                        case Glyph.MEDI_NA_FVS1_STEM:
                        case Glyph.MEDI_NA_FVS1_TOOTH:
                            outputString.append(Uni.NA);
                            if (isMenksoftConsonant(charBelow)) {
                                outputString.append(Uni.FVS1);
                            }
                            break;
                        default:
                            outputString.append(Uni.NA);
                            break;
                    }
                    break;
                case FINAL:
                    switch (currentChar) {
                        case Glyph.MEDI_NA_FVS2:
                            outputString.append(Uni.NA);
                            outputString.append(Uni.FVS2);
                            outputString.append(Uni.ZWJ);
                            break;
                        case Glyph.MEDI_NA_STEM:
                        case Glyph.MEDI_NA_TOOTH:
                        case Glyph.MEDI_NA_NG:
                            outputString.append(Uni.NA);
                            outputString.append(Uni.ZWJ);
                            break;
                        case Glyph.MEDI_NA_FVS1_STEM:
                        case Glyph.MEDI_NA_FVS1_TOOTH:
                        case Glyph.MEDI_NA_FVS1_NG:
                            outputString.append(Uni.NA);
                            outputString.append(Uni.FVS1);
                            outputString.append(Uni.ZWJ);
                            break;
                        default:
                            outputString.append(Uni.NA);
                            break;
                    }
                    break;
            }
        }

        private void handleBa(StringBuilder outputString, char currentChar) {
            switch (location) {
                case ISOLATE:
                    switch (currentChar) {
                        case Glyph.FINA_BA:
                            outputString.append(Uni.ZWJ);
                            outputString.append(Uni.BA);
                            break;
                        case Glyph.FINA_BA_FVS1:
                            outputString.append(Uni.ZWJ);
                            outputString.append(Uni.BA);
                            outputString.append(Uni.FVS1);
                            break;
                        default:
                            outputString.append(Uni.BA);
                            break;
                    }
                    break;
                case INITIAL:
                    outputString.append(Uni.BA);
                    break;
                case MEDIAL:
                    outputString.append(Uni.BA);
                    break;
                case FINAL:
                    outputString.append(Uni.BA);
                    break;
            }
        }

        private void handlePa(StringBuilder outputString, char currentChar) {
            if (location == Location.ISOLATE && currentChar == Glyph.FINA_PA) {
                outputString.append(Uni.ZWJ);
            }
            outputString.append(Uni.PA);
        }

        private void handleQa(StringBuilder outputString, char currentChar, char charBelow) {
            switch (location) {
                case ISOLATE:
                    switch (currentChar) {
                        case Glyph.ISOL_QA_FVS1:
                        case Glyph.INIT_QA_FVS1_FEM_OU:
                        case Glyph.MEDI_QA_FVS1_FEM:
                        case Glyph.MEDI_QA_FVS1_FEM_OU:
                        case Glyph.MEDI_QA_FEM_CONSONANT_DOTTED:
                            outputString.append(Uni.QA);
                            outputString.append(Uni.FVS1);
                            break;
                        case Glyph.INIT_QA_FVS1_STEM:
                        case Glyph.INIT_QA_FVS1_TOOTH:
                            // treat the dotted masculine Q as a G
                            outputString.append(Uni.GA);
                            break;
                        case Glyph.FINA_QA:
                            outputString.append(Uni.ZWJ);
                            outputString.append(Uni.QA);
                            break;
                        case Glyph.MEDI_QA_FEM_CONSONANT:
                            outputString.append(Uni.ZWJ);
                            outputString.append(Uni.GA);
                            outputString.append(Uni.FVS3);
                            outputString.append(Uni.ZWJ);
                            break;
                        case Glyph.MEDI_QA_FVS1:
                            // treat the dotted masculine Q as a G
                            outputString.append(Uni.ZWJ);
                            outputString.append(Uni.GA);
                            outputString.append(Uni.ZWJ);
                            break;
                        case Glyph.MEDI_QA_FVS2:
                            // treat the dotted masculine Q as a G
                            outputString.append(Uni.ZWJ);
                            outputString.append(Uni.GA);
                            outputString.append(Uni.FVS2);
                            outputString.append(Uni.ZWJ);
                            break;
                        case Glyph.MEDI_QA_STEM:
                        case Glyph.MEDI_QA_TOOTH:
                            outputString.append(Uni.ZWJ);
                            outputString.append(Uni.QA);
                            outputString.append(Uni.ZWJ);
                            break;
                        default:
                            outputString.append(Uni.QA);
                            break;
                    }
                    break;
                case INITIAL:
                    switch (currentChar) {
                        case Glyph.INIT_QA_FVS1_STEM:
                        case Glyph.INIT_QA_FVS1_TOOTH:
                            // treat the dotted masculine Q as a G
                            outputString.append(Uni.GA);
                            break;
                        case Glyph.MEDI_QA_STEM:
                        case Glyph.MEDI_QA_TOOTH:
                            outputString.append(Uni.ZWJ);
                            outputString.append(Uni.QA);
                            break;
                        default:
                            outputString.append(Uni.QA);
                            break;
                    }
                    break;
                case MEDIAL:
                    switch (currentChar) {
                        case Glyph.MEDI_QA_STEM:
                        case Glyph.MEDI_QA_TOOTH:
                        case Glyph.MEDI_QA_FEM_CONSONANT:
                            // If a medial Q is being used like a G before
                            // a consonant, then interpret it as a G.
                            if (isMenksoftConsonant(charBelow)) {
                                outputString.append(Uni.GA);
                            } else {
                                outputString.append(Uni.QA);
                            }
                            break;
                        case Glyph.MEDI_QA_FVS1:
                        case Glyph.MEDI_QA_FVS2:
                            // treat the dotted masculine Q as a G
                            outputString.append(Uni.GA);
                            break;
                        default:
                            outputString.append(Uni.QA);
                            break;
                    }
                    break;
                case FINAL:
                    switch (currentChar) {
                        case Glyph.MEDI_QA_FEM_CONSONANT:
                            outputString.append(Uni.GA);
                            outputString.append(Uni.FVS3);
                            outputString.append(Uni.ZWJ);
                            break;
                        case Glyph.MEDI_QA_FVS1:
                            // treat the dotted masculine Q as a G
                            outputString.append(Uni.GA);
                            outputString.append(Uni.ZWJ);
                            break;
                        case Glyph.MEDI_QA_FVS2:
                            // treat the dotted masculine Q as a G
                            outputString.append(Uni.GA);
                            outputString.append(Uni.FVS2);
                            outputString.append(Uni.ZWJ);
                            break;
                        case Glyph.MEDI_QA_STEM:
                        case Glyph.MEDI_QA_TOOTH:
                            outputString.append(Uni.QA);
                            outputString.append(Uni.ZWJ);
                            break;
                        default:
                            outputString.append(Uni.QA);
                            break;
                    }
                    break;
            }
        }

        private void handleGa(StringBuilder outputString, char currentChar) {
            Gender gender = getWordGender(outputString.toString());
            switch (location) {
                case ISOLATE:
                    switch (currentChar) {
                        case Glyph.INIT_GA_FVS1_STEM:
                        case Glyph.INIT_GA_FVS1_TOOTH:
                            // treat the undotted masculine G as a Q
                            outputString.append(Uni.QA);
                            break;
                        case Glyph.FINA_GA:
                            outputString.append(Uni.ZWJ);
                            outputString.append(Uni.GA);
                            break;
                        case Glyph.FINA_GA_FVS2:
                            outputString.append(Uni.ZWJ);
                            outputString.append(Uni.GA);
                            outputString.append(Uni.FVS2);
                            break;
                        case Glyph.MEDI_GA:
                            outputString.append(Uni.ZWJ);
                            outputString.append(Uni.GA);
                            outputString.append(Uni.ZWJ);
                            break;
                        case Glyph.MEDI_GA_FVS1_STEM:
                        case Glyph.MEDI_GA_FVS1_TOOTH:
                            outputString.append(Uni.ZWJ);
                            outputString.append(Uni.GA);
                            outputString.append(Uni.FVS1);
                            outputString.append(Uni.ZWJ);
                            break;
                        case Glyph.MEDI_GA_FVS2:
                            outputString.append(Uni.ZWJ);
                            outputString.append(Uni.GA);
                            outputString.append(Uni.FVS2);
                            outputString.append(Uni.ZWJ);
                            break;
                        case Glyph.MEDI_GA_FVS3_STEM:
                        case Glyph.MEDI_GA_FVS3_TOOTH:
                            outputString.append(Uni.ZWJ);
                            outputString.append(Uni.GA);
                            outputString.append(Uni.FVS3);
                            outputString.append(Uni.ZWJ);
                            break;
                        default:
                            outputString.append(Uni.GA);
                            break;
                    }
                    break;
                case INITIAL:
                    switch (currentChar) {
                        case Glyph.INIT_GA_FVS1_STEM:
                        case Glyph.INIT_GA_FVS1_TOOTH:
                            // treat the undotted masculine G as a Q
                            outputString.append(Uni.QA);
                            break;
                        default:
                            outputString.append(Uni.GA);
                            break;
                    }
                    break;
                case MEDIAL:
                    switch (currentChar) {
                        case Glyph.MEDI_GA_FVS3_TOOTH:
                        case Glyph.MEDI_GA_FVS3_STEM:
                            outputString.append(Uni.GA);
                            if (gender == Gender.MASCULINE) {
                                outputString.append(Uni.FVS3);
                            }
                            break;
                        default:
                            outputString.append(Uni.GA);
                            break;
                    }
                    break;
                case FINAL:
                    outputString.append(Uni.GA);
                    switch (currentChar) {
                        case Glyph.FINA_GA_FVS1:
                            if (gender == Gender.NEUTER) {
                                outputString.append(Uni.FVS1);
                            }
                            break;
                        case Glyph.FINA_GA_FVS2:
                            if (gender == Gender.MASCULINE) {
                                outputString.append(Uni.FVS2);
                            }
                            break;
                        case Glyph.MEDI_GA:
                            outputString.append(Uni.ZWJ);
                            break;
                        case Glyph.MEDI_GA_FVS1_STEM:
                        case Glyph.MEDI_GA_FVS1_TOOTH:
                            outputString.append(Uni.FVS1);
                            outputString.append(Uni.ZWJ);
                            break;
                        case Glyph.MEDI_GA_FVS2:
                            outputString.append(Uni.FVS2);
                            outputString.append(Uni.ZWJ);
                            break;
                        case Glyph.MEDI_GA_FVS3_STEM:
                        case Glyph.MEDI_GA_FVS3_TOOTH:
                            outputString.append(Uni.FVS3);
                            outputString.append(Uni.ZWJ);
                            break;
                    }
                    break;
            }
        }

        private void handleMa(StringBuilder outputString, char currentChar) {
            switch (location) {
                case ISOLATE:
                    switch (currentChar) {
                        case Glyph.FINA_MA:
                            outputString.append(Uni.ZWJ);
                            outputString.append(Uni.MA);
                            break;
                        case Glyph.MEDI_MA_BP:
                        case Glyph.MEDI_MA_STEM_LONG:
                        case Glyph.MEDI_MA_TOOTH:
                            outputString.append(Uni.ZWJ);
                            outputString.append(Uni.MA);
                            outputString.append(Uni.ZWJ);
                            break;
                        default:
                            outputString.append(Uni.MA);
                            break;
                    }
                    break;
                case INITIAL:
                    switch (currentChar) {
                        case Glyph.MEDI_MA_BP:
                        case Glyph.MEDI_MA_STEM_LONG:
                        case Glyph.MEDI_MA_TOOTH:
                            outputString.append(Uni.ZWJ);
                            outputString.append(Uni.MA);
                            break;
                        default:
                            outputString.append(Uni.MA);
                            break;
                    }
                    break;
                case MEDIAL:
                    outputString.append(Uni.MA);
                    break;
                case FINAL:
                    switch (currentChar) {
                        case Glyph.MEDI_MA_BP:
                        case Glyph.MEDI_MA_STEM_LONG:
                        case Glyph.MEDI_MA_TOOTH:
                            outputString.append(Uni.MA);
                            outputString.append(Uni.ZWJ);
                            break;
                        default:
                            outputString.append(Uni.MA);
                            break;
                    }
                    break;
            }
        }

        private void handleLa(StringBuilder outputString, char currentChar) {
            switch (location) {
                case ISOLATE:
                    switch (currentChar) {
                        case Glyph.FINA_LA:
                            outputString.append(Uni.ZWJ);
                            outputString.append(Uni.LA);
                            break;
                        case Glyph.MEDI_LA_BP:
                        case Glyph.MEDI_LA_STEM_LONG:
                        case Glyph.MEDI_LA_TOOTH:
                            outputString.append(Uni.ZWJ);
                            outputString.append(Uni.LA);
                            outputString.append(Uni.ZWJ);
                            break;
                        default:
                            outputString.append(Uni.LA);
                            break;
                    }
                    break;
                case INITIAL:
                    switch (currentChar) {
                        case Glyph.MEDI_LA_BP:
                        case Glyph.MEDI_LA_STEM_LONG:
                        case Glyph.MEDI_LA_TOOTH:
                            outputString.append(Uni.ZWJ);
                            outputString.append(Uni.LA);
                            break;
                        default:
                            outputString.append(Uni.LA);
                            break;
                    }
                    break;
                case MEDIAL:
                    outputString.append(Uni.LA);
                    break;
                case FINAL:
                    switch (currentChar) {
                        case Glyph.MEDI_LA_BP:
                        case Glyph.MEDI_LA_STEM_LONG:
                        case Glyph.MEDI_LA_TOOTH:
                            outputString.append(Uni.LA);
                            outputString.append(Uni.ZWJ);
                            break;
                        default:
                            outputString.append(Uni.LA);
                            break;
                    }
                    break;
            }
        }

        private void handleSa(StringBuilder outputString, char currentChar) {
            switch (location) {
                case ISOLATE:
                    switch (currentChar) {
                        case Glyph.FINA_SA:
                            outputString.append(Uni.ZWJ);
                            outputString.append(Uni.SA);
                            break;
                        case Glyph.FINA_SA_FVS1:
                            outputString.append(Uni.ZWJ);
                            outputString.append(Uni.SA);
                            outputString.append(Uni.FVS1);
                            break;
                        case Glyph.MEDI_SA_STEM:
                        case Glyph.MEDI_SA_TOOTH:
                            outputString.append(Uni.ZWJ);
                            outputString.append(Uni.SA);
                            outputString.append(Uni.ZWJ);
                            break;
                        default:
                            outputString.append(Uni.SA);
                            break;
                    }
                    break;
                case INITIAL:
                    switch (currentChar) {
                        case Glyph.MEDI_SA_STEM:
                        case Glyph.MEDI_SA_TOOTH:
                            outputString.append(Uni.ZWJ);
                            outputString.append(Uni.SA);
                            break;
                        default:
                            outputString.append(Uni.SA);
                            break;
                    }
                    break;
                case MEDIAL:
                    outputString.append(Uni.SA);
                    break;
                case FINAL:
                    switch (currentChar) {
                        case Glyph.MEDI_SA_STEM:
                        case Glyph.MEDI_SA_TOOTH:
                            outputString.append(Uni.SA);
                            outputString.append(Uni.ZWJ);
                            break;
                        default:
                            outputString.append(Uni.SA);
                            break;
                    }
                    break;
            }
        }

        private void handleSha(StringBuilder outputString, char currentChar) {
            switch (location) {
                case ISOLATE:
                    switch (currentChar) {
                        case Glyph.FINA_SHA:
                            outputString.append(Uni.ZWJ);
                            outputString.append(Uni.SHA);
                            break;
                        case Glyph.MEDI_SHA_STEM:
                        case Glyph.MEDI_SHA_TOOTH:
                            outputString.append(Uni.ZWJ);
                            outputString.append(Uni.SHA);
                            outputString.append(Uni.ZWJ);
                            break;
                        default:
                            outputString.append(Uni.SHA);
                            break;
                    }
                    break;
                case INITIAL:
                    switch (currentChar) {
                        case Glyph.MEDI_SHA_STEM:
                        case Glyph.MEDI_SHA_TOOTH:
                            outputString.append(Uni.ZWJ);
                            outputString.append(Uni.SHA);
                            break;
                        default:
                            outputString.append(Uni.SHA);
                            break;
                    }
                    break;
                case MEDIAL:
                    outputString.append(Uni.SHA);
                    break;
                case FINAL:
                    switch (currentChar) {
                        case Glyph.MEDI_SHA_STEM:
                        case Glyph.MEDI_SHA_TOOTH:
                            outputString.append(Uni.SHA);
                            outputString.append(Uni.ZWJ);
                            break;
                        default:
                            outputString.append(Uni.SHA);
                            break;
                    }
                    break;
            }
        }

        private void handleTa(StringBuilder outputString, char currentChar) {
            switch (location) {
                case ISOLATE:
                    switch (currentChar) {
                        case Glyph.FINA_TA:
                            outputString.append(Uni.ZWJ);
                            outputString.append(Uni.TA);
                            break;
                        case Glyph.MEDI_TA:
                            outputString.append(Uni.ZWJ);
                            outputString.append(Uni.TA);
                            outputString.append(Uni.ZWJ);
                            break;
                        case Glyph.MEDI_TA_FVS1_STEM:
                        case Glyph.MEDI_TA_FVS1_TOOTH:
                            outputString.append(Uni.ZWJ);
                            outputString.append(Uni.TA);
                            outputString.append(Uni.FVS1);
                            outputString.append(Uni.ZWJ);
                            break;
                        default:
                            outputString.append(Uni.TA);
                            break;
                    }
                    break;
                case INITIAL:
                    switch (currentChar) {
                        case Glyph.MEDI_TA:
                            outputString.append(Uni.ZWJ);
                            outputString.append(Uni.TA);
                            break;
                        case Glyph.MEDI_TA_FVS1_STEM:
                        case Glyph.MEDI_TA_FVS1_TOOTH:
                            outputString.append(Uni.ZWJ);
                            outputString.append(Uni.TA);
                            outputString.append(Uni.FVS1);
                            break;
                        default:
                            outputString.append(Uni.TA);
                            break;
                    }
                    break;
                case MEDIAL:
                    switch (currentChar) {
                        case Glyph.MEDI_TA_FVS1_STEM:
                        case Glyph.MEDI_TA_FVS1_TOOTH:
                            outputString.append(Uni.TA);
                            outputString.append(Uni.FVS1);
                            break;
                        default:
                            outputString.append(Uni.TA);
                            break;
                    }
                    break;
                case FINAL:
                    switch (currentChar) {
                        case Glyph.MEDI_TA:
                            outputString.append(Uni.TA);
                            outputString.append(Uni.ZWJ);
                            break;
                        case Glyph.MEDI_TA_FVS1_STEM:
                        case Glyph.MEDI_TA_FVS1_TOOTH:
                            outputString.append(Uni.TA);
                            outputString.append(Uni.FVS1);
                            outputString.append(Uni.ZWJ);
                            break;
                        default:
                            outputString.append(Uni.TA);
                            break;
                    }
                    break;
            }
        }

        private void handleDa(StringBuilder outputString, char currentChar) {
            switch (location) {
                case ISOLATE:
                    switch (currentChar) {
                        case Glyph.INIT_DA_STEM:
                        case Glyph.INIT_DA_TOOTH:
                            // replace isolated DA that looks like TA with actual TA
                            outputString.append(Uni.TA);
                            break;
                        case Glyph.FINA_DA:
                            outputString.append(Uni.ZWJ);
                            outputString.append(Uni.DA);
                            break;
                        case Glyph.FINA_DA_FVS1:
                            outputString.append(Uni.ZWJ);
                            outputString.append(Uni.DA);
                            outputString.append(Uni.FVS1);
                            break;
                        case Glyph.MEDI_DA:
                            outputString.append(Uni.ZWJ);
                            outputString.append(Uni.DA);
                            outputString.append(Uni.ZWJ);
                            break;
                        case Glyph.MEDI_DA_FVS1:
                            outputString.append(Uni.ZWJ);
                            outputString.append(Uni.DA);
                            outputString.append(Uni.FVS1);
                            outputString.append(Uni.ZWJ);
                            break;
                        default:
                            outputString.append(Uni.DA);
                            break;
                    }
                    break;
                case INITIAL:
                    switch (currentChar) {
                        case Glyph.MEDI_DA:
                            outputString.append(Uni.ZWJ);
                            outputString.append(Uni.DA);
                            break;
                        case Glyph.INIT_DA_FVS1:
                        case Glyph.MEDI_DA_FVS1:
                            outputString.append(Uni.DA);
                            if (!startsWithNnbsSuffix(outputString)) {
                                outputString.append(Uni.FVS1);
                            }
                            break;
                        default:
                            outputString.append(Uni.DA);
                            break;
                    }
                    break;
                case MEDIAL:
                    outputString.append(Uni.DA);
                    break;
                case FINAL:
                    switch (currentChar) {
                        case Glyph.MEDI_DA:
                            outputString.append(Uni.DA);
                            outputString.append(Uni.ZWJ);
                            break;
                        case Glyph.MEDI_DA_FVS1:
                            outputString.append(Uni.DA);
                            outputString.append(Uni.FVS1);
                            outputString.append(Uni.ZWJ);
                            break;
                        default:
                            outputString.append(Uni.DA);
                            break;
                    }
                    break;
            }
        }

        private void handleCha(StringBuilder outputString, char currentChar) {
            switch (location) {
                case ISOLATE:
                    switch (currentChar) {
                        case Glyph.FINA_CHA:
                            outputString.append(Uni.ZWJ);
                            outputString.append(Uni.CHA);
                            break;
                        default:
                            outputString.append(Uni.CHA);
                            break;
                    }
                    break;
                case INITIAL:
                    outputString.append(Uni.CHA);
                    break;
                case MEDIAL:
                    outputString.append(Uni.CHA);
                    break;
                case FINAL:
                    switch (currentChar) {
                        case Glyph.MEDI_CHA:
                            outputString.append(Uni.CHA);
                            outputString.append(Uni.ZWJ);
                            break;
                        default:
                            outputString.append(Uni.CHA);
                            break;
                    }
                    break;
            }
        }

        private void handleJa(StringBuilder outputString, char currentChar) {
            switch (location) {
                case ISOLATE:
                    switch (currentChar) {
                        case Glyph.FINA_JA:
                            outputString.append(Uni.ZWJ);
                            outputString.append(Uni.JA);
                            break;
                        case Glyph.MEDI_JA:
                            outputString.append(Uni.ZWJ);
                            outputString.append(Uni.JA);
                            outputString.append(Uni.ZWJ);
                            break;
                        case Glyph.MEDI_JA_FVS1:
                            // ignoring ancient form,
                            // it looks like a final I so make it one
                            outputString.append(Uni.ZWJ);
                            outputString.append(Uni.I);
                            break;
                        default:
                            outputString.append(Uni.JA);
                            break;
                    }
                    break;
                case INITIAL:
                    switch (currentChar) {
                        case Glyph.INIT_JA_STEM:
                        case Glyph.INIT_JA_TOOTH:
                            // if user used a JA to write a YA suffix then replace it
                            if (startsWithNnbsSuffix(outputString))
                                outputString.append(Uni.YA);
                            else
                                outputString.append(Uni.JA);
                            break;
                        case Glyph.MEDI_JA:
                            outputString.append(Uni.ZWJ);
                            outputString.append(Uni.JA);
                            break;
                        default:
                            outputString.append(Uni.JA);
                            break;
                    }
                    break;
                case MEDIAL:
                    outputString.append(Uni.JA);
                    break;
                case FINAL:
                    switch (currentChar) {
                        case Glyph.MEDI_JA:
                            outputString.append(Uni.JA);
                            outputString.append(Uni.ZWJ);
                            break;
                        case Glyph.MEDI_JA_FVS1:
                            // ignoring ancient form,
                            // it looks like a final I so make it one
                            outputString.append(Uni.I);
                            break;
                        default:
                            outputString.append(Uni.JA);
                            break;
                    }
            }
        }

        private void handleYa(StringBuilder outputString,
                              char currentChar, char charAbove, char charBelow) {
            switch (location) {
                case ISOLATE:
                    switch (currentChar) {
                        case Glyph.FINA_YA:
                            outputString.append(Uni.ZWJ);
                            outputString.append(Uni.YA);
                            break;
                        case Glyph.MEDI_YA:
                            outputString.append(Uni.ZWJ);
                            outputString.append(Uni.YA);
                            outputString.append(Uni.ZWJ);
                            break;
                        default:
                            outputString.append(Uni.YA);
                            break;
                    }
                    break;
                case INITIAL:
                    switch (currentChar) {
                        case Glyph.MEDI_YA:
                            outputString.append(Uni.YA);
                            if (!startsWithNnbsSuffix(outputString)) {
                                outputString.append(Uni.FVS1);
                            }
                            break;
                        default:
                            outputString.append(Uni.YA);
                            break;
                    }
                    break;
                case MEDIAL:
                    switch (currentChar) {
                        case Glyph.MEDI_YA_FVS1:
                            outputString.append(Uni.YA);
                            if (isMenksoftVowel(charAbove) && isMenksoftI(charBelow)) {
                                // override context rule that would make a normal Y straight
                                outputString.append(Uni.FVS1);
                            }
                            break;
                        default:
                            outputString.append(Uni.YA);
                            break;
                    }
                    break;
                case FINAL:
                    switch (currentChar) {
                        case Glyph.MEDI_YA:
                            outputString.append(Uni.YA);
                            outputString.append(Uni.ZWJ);
                            break;
                        default:
                            outputString.append(Uni.YA);
                            break;
                    }
                    break;
            }
        }

        private boolean isMenksoftI(char character) {
            return character >= Glyph.ISOL_I && character <= Glyph.ISOL_I_SUFFIX;
        }

        private void handleRa(StringBuilder outputString, char currentChar) {
            switch (location) {
                case ISOLATE:
                    switch (currentChar) {
                        case Glyph.FINA_RA:
                            outputString.append(Uni.ZWJ);
                            outputString.append(Uni.RA);
                            break;
                        default:
                            outputString.append(Uni.RA);
                            break;
                    }
                    break;
                case INITIAL:
                    outputString.append(Uni.RA);
                    break;
                case MEDIAL:
                    outputString.append(Uni.RA);
                    break;
                case FINAL:
                    switch (currentChar) {
                        case Glyph.MEDI_RA_STEM:
                        case Glyph.MEDI_RA_TOOTH:
                            outputString.append(Uni.RA);
                            outputString.append(Uni.ZWJ);
                            break;
                        default:
                            outputString.append(Uni.RA);
                            break;
                    }
                    break;
            }
        }

        private void handleWa(StringBuilder outputString,
                              char currentChar, char charAbove, char charBelow) {
            switch (location) {
                case ISOLATE:
                    switch (currentChar) {
                        case Glyph.FINA_WA:
                            outputString.append(Uni.ZWJ);
                            outputString.append(Uni.WA);
                            break;
                        case Glyph.FINA_WA_FVS1:
                            // an isolate final WA looks like a final U so make it one
                            outputString.append(Uni.ZWJ);
                            outputString.append(Uni.U);
                            break;
                        default:
                            outputString.append(Uni.WA);
                            break;
                    }
                    break;
                case INITIAL:
                    outputString.append(Uni.WA);
                    break;
                case MEDIAL:
                    // replace W between two consonants with EE
                    if (isMenksoftConsonant(charAbove)
                            && isMenksoftConsonant(charBelow))
                        outputString.append(Uni.EE);
                    else
                        outputString.append(Uni.WA);
                    break;
                case FINAL:
                    switch (currentChar) {
                        case Glyph.FINA_WA_FVS1:
                            outputString.append(Uni.WA);
                            outputString.append(Uni.FVS1);
                            break;
                        case Glyph.MEDI_WA:
                            outputString.append(Uni.WA);
                            outputString.append(Uni.ZWJ);
                            break;
                        default:
                            outputString.append(Uni.WA);
                            break;
                    }
                    break;
            }
        }

        private void handleFa(StringBuilder outputString, char currentChar) {
            if (location == Location.ISOLATE && currentChar == Glyph.FINA_FA) {
                outputString.append(Uni.ZWJ);
            }
            outputString.append(Uni.FA);
        }

        private void handleKa(StringBuilder outputString, char currentChar) {
            if (location == Location.ISOLATE && currentChar == Glyph.FINA_KA) {
                outputString.append(Uni.ZWJ);
            }
            outputString.append(Uni.KA);
        }

        private void handleKha(StringBuilder outputString, char currentChar) {
            if (location == Location.ISOLATE && currentChar == Glyph.FINA_KHA) {
                outputString.append(Uni.ZWJ);
            }
            outputString.append(Uni.KHA);
        }

        private void handleTsa(StringBuilder outputString, char currentChar) {
            switch (location) {
                case ISOLATE:
                    switch (currentChar) {
                        case Glyph.FINA_TSA:
                            outputString.append(Uni.ZWJ);
                            outputString.append(Uni.TSA);
                            break;
                        default:
                            outputString.append(Uni.TSA);
                            break;
                    }
                    break;
                case INITIAL:
                    outputString.append(Uni.TSA);
                    break;
                case MEDIAL:
                    outputString.append(Uni.TSA);
                    break;
                case FINAL:
                    switch (currentChar) {
                        case Glyph.MEDI_TSA:
                            outputString.append(Uni.TSA);
                            outputString.append(Uni.ZWJ);
                            break;
                        default:
                            outputString.append(Uni.TSA);
                            break;
                    }
                    break;
            }
        }

        private void handleZa(StringBuilder outputString, char currentChar) {
            switch (location) {
                case ISOLATE:
                    switch (currentChar) {
                        case Glyph.FINA_ZA:
                            outputString.append(Uni.ZWJ);
                            outputString.append(Uni.ZA);
                            break;
                        default:
                            outputString.append(Uni.ZA);
                            break;
                    }
                    break;
                case INITIAL:
                    outputString.append(Uni.ZA);
                    break;
                case MEDIAL:
                    outputString.append(Uni.ZA);
                    break;
                case FINAL:
                    switch (currentChar) {
                        case Glyph.MEDI_ZA:
                            outputString.append(Uni.ZA);
                            outputString.append(Uni.ZWJ);
                            break;
                        default:
                            outputString.append(Uni.ZA);
                            break;
                    }
                    break;
            }
        }

        private void handleHaa(StringBuilder outputString, char currentChar) {
            switch (location) {
                case ISOLATE:
                    switch (currentChar) {
                        case Glyph.FINA_HAA:
                            outputString.append(Uni.ZWJ);
                            outputString.append(Uni.HAA);
                            break;
                        case Glyph.MEDI_HAA:
                            outputString.append(Uni.ZWJ);
                            outputString.append(Uni.HAA);
                            outputString.append(Uni.ZWJ);
                            break;
                        default:
                            outputString.append(Uni.HAA);
                            break;
                    }
                    break;
                case INITIAL:
                    switch (currentChar) {
                        case Glyph.MEDI_HAA:
                            outputString.append(Uni.ZWJ);
                            outputString.append(Uni.HAA);
                            break;
                        default:
                            outputString.append(Uni.HAA);
                            break;
                    }
                    break;
                case MEDIAL:
                    outputString.append(Uni.HAA);
                    break;
                case FINAL:
                    switch (currentChar) {
                        case Glyph.MEDI_HAA:
                            outputString.append(Uni.HAA);
                            outputString.append(Uni.ZWJ);
                            break;
                        default:
                            outputString.append(Uni.HAA);
                            break;
                    }
                    break;
            }
        }

        private void handleZra(StringBuilder outputString, char currentChar) {
            switch (location) {
                case ISOLATE:
                    switch (currentChar) {
                        case Glyph.FINA_ZRA:
                            outputString.append(Uni.ZWJ);
                            outputString.append(Uni.ZRA);
                            break;
                        default:
                            outputString.append(Uni.ZRA);
                            break;
                    }
                    break;
                case INITIAL:
                    outputString.append(Uni.ZRA);
                    break;
                case MEDIAL:
                    outputString.append(Uni.ZRA);
                    break;
                case FINAL:
                    switch (currentChar) {
                        case Glyph.MEDI_ZRA:
                            outputString.append(Uni.ZRA);
                            outputString.append(Uni.ZWJ);
                            break;
                        default:
                            outputString.append(Uni.ZRA);
                            break;
                    }
                    break;
            }
        }

        private void handleLha(StringBuilder outputString, char currentChar) {
            switch (location) {
                case ISOLATE:
                    switch (currentChar) {
                        case Glyph.MEDI_LHA:
                        case Glyph.MEDI_LHA_BP:
                            outputString.append(Uni.ZWJ);
                            outputString.append(Uni.LHA);
                            break;
                        default:
                            outputString.append(Uni.LHA);
                            break;
                    }
                    break;
                case INITIAL:
                    switch (currentChar) {
                        case Glyph.MEDI_LHA:
                        case Glyph.MEDI_LHA_BP:
                            outputString.append(Uni.ZWJ);
                            outputString.append(Uni.LHA);
                            break;
                        default:
                            outputString.append(Uni.LHA);
                            break;
                    }
                    break;
                default:
                    outputString.append(Uni.LHA);
                    break;
            }
        }

        private void handleZhi(StringBuilder outputString) {
            outputString.append(Uni.ZHI);
        }

        private void handleChi(StringBuilder outputString) {
            outputString.append(Uni.CHI);
        }

        private void handleSpace(StringBuilder outputString, char currentChar, char charBelow) {

            if (currentChar == Glyph.SUFFIX_SPACE) {
                outputString.append(Uni.NNBS);
                return;
            }

            switch (charBelow) {
                case Glyph.MEDI_A_FVS2:
                case Glyph.FINA_I:
                case Glyph.MEDI_I:
                case Glyph.MEDI_I_SUFFIX:
                case Glyph.ISOL_I_SUFFIX:
                case Glyph.MEDI_O:
                case Glyph.MEDI_O_BP:
                case Glyph.FINA_O:
                case Glyph.MEDI_U:
                case Glyph.MEDI_U_BP:
                case Glyph.FINA_U:
                case Glyph.MEDI_OE:
                case Glyph.MEDI_OE_BP:
                case Glyph.FINA_OE:
                case Glyph.MEDI_UE:
                case Glyph.MEDI_UE_BP:
                case Glyph.FINA_UE:
                case Glyph.FINA_YA:
                case Glyph.INIT_YA_FVS1:
                    outputString.append(Uni.NNBS);
                    break;
                default:
                    outputString.append(SPACE);
            }
        }

        private void handlePunctuation(StringBuilder outputString, char currentChar) {
            switch (currentChar) {
                case Glyph.BIRGA:
                    outputString.append(Uni.MONGOLIAN_BIRGA);
                    break;
                case Glyph.ELLIPSIS:
                    outputString.append(Uni.MONGOLIAN_ELLIPSIS);
                    break;
                case Glyph.COMMA:
                    outputString.append(Uni.MONGOLIAN_COMMA);
                    break;
                case Glyph.FULL_STOP:
                    outputString.append(Uni.MONGOLIAN_FULL_STOP);
                    break;
                case Glyph.COLON:
                    outputString.append(Uni.MONGOLIAN_COLON);
                    break;
                case Glyph.FOUR_DOTS:
                    outputString.append(Uni.MONGOLIAN_FOUR_DOTS);
                    break;
                case Glyph.TODO_SOFT_HYPHEN:
                    outputString.append(Uni.MONGOLIAN_TODO_SOFT_HYPHEN);
                    break;
                case Glyph.SIBE_SYLLABLE_BOUNDARY_MARKER:
                    outputString.append(Uni.MONGOLIAN_SIBE_SYLLABLE_BOUNDARY_MARKER);
                    break;
                case Glyph.MANCHU_COMMA:
                    outputString.append(Uni.MONGOLIAN_MANCHU_COMMA);
                    break;
                case Glyph.MANCHU_FULL_STOP:
                    outputString.append(Uni.MONGOLIAN_MANCHU_FULL_STOP);
                    break;
                case Glyph.NIRUGU:
                    outputString.append(Uni.MONGOLIAN_NIRUGU);
                    break;
                case Glyph.BIRGA_WITH_ORNAMENT:
                    outputString.append("\uD805\uDE60"); // U+11660
                    break;
                case Glyph.ROTATED_BIRGA:
                    outputString.append("\uD805\uDE61"); // U+11661
                    break;
                case Glyph.DOUBLE_BIRGA_WITH_ORNAMENT:
                    outputString.append("\uD805\uDE62"); // U+11662
                    break;
                case Glyph.TRIPLE_BIRGA_WITH_ORNAMENT:
                    outputString.append("\uD805\uDE63"); // U+11663
                    break;
                case Glyph.MIDDLE_DOT:
                    outputString.append(Uni.MIDDLE_DOT);
                    break;
                case Glyph.ZERO:
                    outputString.append(Uni.MONGOLIAN_DIGIT_ZERO);
                    break;
                case Glyph.ONE:
                    outputString.append(Uni.MONGOLIAN_DIGIT_ONE);
                    break;
                case Glyph.TWO:
                    outputString.append(Uni.MONGOLIAN_DIGIT_TWO);
                    break;
                case Glyph.THREE:
                    outputString.append(Uni.MONGOLIAN_DIGIT_THREE);
                    break;
                case Glyph.FOUR:
                    outputString.append(Uni.MONGOLIAN_DIGIT_FOUR);
                    break;
                case Glyph.FIVE:
                    outputString.append(Uni.MONGOLIAN_DIGIT_FIVE);
                    break;
                case Glyph.SIX:
                    outputString.append(Uni.MONGOLIAN_DIGIT_SIX);
                    break;
                case Glyph.SEVEN:
                    outputString.append(Uni.MONGOLIAN_DIGIT_SEVEN);
                    break;
                case Glyph.EIGHT:
                    outputString.append(Uni.MONGOLIAN_DIGIT_EIGHT);
                    break;
                case Glyph.NINE:
                    outputString.append(Uni.MONGOLIAN_DIGIT_NINE);
                    break;
                case Glyph.QUESTION_EXCLAMATION:
                    outputString.append(Uni.QUESTION_EXCLAMATION_MARK);
                    break;
                case Glyph.EXCLAMATION_QUESTION:
                    outputString.append(Uni.EXCLAMATION_QUESTION_MARK);
                    break;
                case Glyph.EXCLAMATION:
                    outputString.append(Uni.VERTICAL_EXCLAMATION_MARK);
                    break;
                case Glyph.QUESTION:
                    outputString.append(Uni.VERTICAL_QUESTION_MARK);
                    break;
                case Glyph.SEMICOLON:
                    outputString.append(Uni.VERTICAL_SEMICOLON);
                    break;
                case Glyph.LEFT_PARENTHESIS:
                    outputString.append(Uni.VERTICAL_LEFT_PARENTHESIS);
                    break;
                case Glyph.RIGHT_PARENTHESIS:
                    outputString.append(Uni.VERTICAL_RIGHT_PARENTHESIS);
                    break;
                case Glyph.LEFT_ANGLE_BRACKET:
                    outputString.append(Uni.VERTICAL_LEFT_ANGLE_BRACKET);
                    break;
                case Glyph.RIGHT_ANGLE_BRACKET:
                    outputString.append(Uni.VERTICAL_RIGHT_ANGLE_BRACKET);
                    break;
                case Glyph.LEFT_TORTOISE_SHELL_BRACKET:
                    outputString.append(Uni.VERTICAL_LEFT_TORTOISE_SHELL_BRACKET);
                    break;
                case Glyph.RIGHT_TORTOISE_SHELL_BRACKET:
                    outputString.append(Uni.VERTICAL_RIGHT_TORTOISE_SHELL_BRACKET);
                    break;
                case Glyph.LEFT_DOUBLE_ANGLE_BRACKET:
                    outputString.append(Uni.VERTICAL_LEFT_DOUBLE_ANGLE_BRACKET);
                    break;
                case Glyph.RIGHT_DOUBLE_ANGLE_BRACKET:
                    outputString.append(Uni.VERTICAL_RIGHT_DOUBLE_ANGLE_BRACKET);
                    break;
                case Glyph.LEFT_WHITE_CORNER_BRACKET:
                    outputString.append(Uni.VERTICAL_LEFT_WHITE_CORNER_BRACKET);
                    break;
                case Glyph.RIGHT_WHITE_CORNER_BRACKET:
                    outputString.append(Uni.VERTICAL_RIGHT_WHITE_CORNER_BRACKET);
                    break;
                case Glyph.FULL_WIDTH_COMMA:
                    outputString.append(Uni.VERTICAL_COMMA);
                    break;
                case Glyph.X:
                    outputString.append('\u00D7'); // FIXME using the multiplication sign?
                    break;
                case Glyph.REFERENCE_MARK:
                    outputString.append(Uni.REFERENCE_MARK);
                    break;
                case Glyph.EN_DASH:
                    outputString.append(Uni.VERTICAL_EN_DASH);
                    break;
                case Glyph.EM_DASH:
                    outputString.append(Uni.VERTICAL_EM_DASH);
                    break;
                default:
                    outputString.append(currentChar);
            }
        }

        private boolean isMenksoftSpaceChar(char character) {
            return character == SPACE
                    || character == Glyph.SUFFIX_SPACE
                    || character == Glyph.UNKNOWN_SPACE;
        }
    }

}
