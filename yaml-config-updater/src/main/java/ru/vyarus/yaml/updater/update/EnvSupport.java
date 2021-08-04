package ru.vyarus.yaml.updater.update;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Environment variables could be used to personalize updating yaml file. For example:
 * <pre>
 * some:
 *    - prop: #{var}
 * </pre>
 * <p>
 * Such syntax allows yaml parser to treat variable as a comment (if it would not be replaced). Also, variables
 * in format {@code ${va}} are often used directly in the configuration (to be replaced at runtime with environment
 * variables).
 * <p>
 * Variable names are case sensitive. Unknown variables are not replaced. Null values replaced with empty.
 *
 * @author Vyacheslav Rusakov
 * @since 08.06.2021
 */
public final class EnvSupport {
    private static final Logger LOGGER = LoggerFactory.getLogger(EnvSupport.class);

    private EnvSupport() {
    }

    /**
     * Replace variables in the provided text.
     *
     * @param text text to process
     * @param env  variables map
     * @return text with replaced known variables
     */
    public static String apply(final String text,
                               final Map<String, String> env) {
        // non standard vars identity used because:
        // - real config could rely on environment variables
        // - not processed file would remain valid yaml because placeholder would be interpreted as comment
        return apply(text, "#{", "}", env);
    }

    /**
     * Replace variables in the provided text.
     *
     * @param text    text to process
     * @param prefix  variable prefix
     * @param postfix variable postfix
     * @param env     variables map
     * @return text with replaced known variables
     */
    public static String apply(final String text,
                               final String prefix,
                               final String postfix,
                               final Map<String, String> env) {
        if (text == null || text.isEmpty() || env == null || env.isEmpty()) {
            return text;
        }
        if (prefix == null) {
            throw new IllegalArgumentException("Variable prefix required");
        }
        if (postfix == null) {
            throw new IllegalArgumentException("Variable postfix required");
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Replacing variables in format '{}name{}' from: \n{}", prefix, postfix,
                    env.entrySet().stream()
                            .map(entry -> "    " + entry.getKey() + "=" + entry.getValue())
                            .collect(Collectors.joining("\n")));
        }
        return replace(text, prefix, postfix, env);
    }

    @SuppressWarnings("checkstyle:IllegalIdentifierName")
    private static String replace(final String text,
                                  final String prefix,
                                  final String postfix,
                                  final Map<String, String> env) {
        String res = text;
        for (Map.Entry<String, String> entry : env.entrySet()) {
            final String var = prefix + entry.getKey() + postfix;
            String value = entry.getValue();
            if (value == null) {
                value = "";
            }

            final Matcher matcher = Pattern.compile(var, Pattern.LITERAL).matcher(res);
            final String replacement = Matcher.quoteReplacement(value);
            int cntr = 0;
            while (matcher.find()) {
                cntr++;
            }
            if (cntr > 0) {
                LOGGER.debug("    {} ({}) replaced with: {}", var, cntr, replacement);
                res = matcher.replaceAll(replacement);
            }
        }
        return res;
    }
}