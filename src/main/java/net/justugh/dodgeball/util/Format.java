package net.justugh.dodgeball.util;

import net.md_5.bungee.api.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Format {

    private static final Pattern hexPattern = Pattern.compile("%hex_(#.+?)%");

    /**
     * Format a string with color & placeholders.
     *
     * @param string       The string being formatted.
     * @param replacements The replacements being applied.
     * @return The formatted string.
     */
    public static String format(String string, Object... replacements) {
        String formattedMessage = String.format(string, replacements);

        Matcher matcher = hexPattern.matcher(formattedMessage);
        while (matcher.find()) {
            formattedMessage = formattedMessage.replace(matcher.group(), ChatColor.of(matcher.group(1)).toString());
        }

        return ChatColor.translateAlternateColorCodes('&', formattedMessage);
    }

}
