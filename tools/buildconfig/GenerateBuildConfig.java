import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

class GenerateBuildConfig {
    static String quote(String value) {
        StringBuilder result = new StringBuilder("\"");
        for (char c : value.toCharArray()) {
            if (c == '\\' || c == '"' || c == '$') result.append('\\').append(c);
            else if (c < 32 || c > 126) result.append(String.format("\\u%04x", (int) c));
            else result.append(c);
        }
        return result.append('"').toString();
    }

    static Properties load(Path path) throws Exception {
        Properties properties = new Properties();
        if (Files.exists(path)) {
            try (var input = Files.newInputStream(path)) {
                properties.load(input);
            }
        }
        return properties;
    }

    static String generate(Properties properties) {
        StringBuilder result = new StringBuilder("package Kudos.core.network\n\ninternal object BuildConfig {\n");
        for (String key : new String[] {"SUPABASE_URL", "SUPABASE_ANON_KEY"}) {
            result.append("    const val ").append(key).append(" = ")
                .append(quote(properties.getProperty(key, ""))).append('\n');
        }
        return result.append("}\n").toString();
    }

    static void selfTest() throws Exception {
        Path directory = Files.createTempDirectory("buildconfig-test");
        try {
            if (!generate(load(directory.resolve("missing.properties"))).contains("SUPABASE_URL = \"\""))
                throw new AssertionError("Missing file default");
            Properties properties = new Properties();
            properties.load(new ByteArrayInputStream((
                "SUPABASE_URL : https\\://example.test/\\\n  path\\uD55C\n" +
                "SUPABASE_ANON_KEY=a\\=b\\:c\\t\\n\\r\\f\\\\\"$\n"
            ).getBytes(StandardCharsets.ISO_8859_1)));
            if (!properties.getProperty("SUPABASE_URL").equals("https://example.test/path한"))
                throw new AssertionError("Unicode or continuation");
            if (!properties.getProperty("SUPABASE_ANON_KEY").equals("a=b:c\t\n\r\f\\\"$"))
                throw new AssertionError("Properties escapes");
            if (!quote("\"\\$\n\t\r\b\f한").equals("\"\\\"\\\\\\$\\u000a\\u0009\\u000d\\u0008\\u000c\\ud55c\""))
                throw new AssertionError("Kotlin escapes");
        } finally {
            Files.delete(directory);
        }
        System.out.println("BuildConfig self-test passed");
    }

    public static void main(String[] args) {
        try {
            if (args.length == 1 && args[0].equals("--self-test")) selfTest();
            else if (args.length == 2) Files.writeString(Path.of(args[1]), generate(load(Path.of(args[0]))));
            else throw new IllegalArgumentException();
        } catch (Exception exception) {
            // Properties errors can include input text; never expose it in build logs.
            System.err.println("BuildConfig generation failed; check local.properties syntax and file permissions.");
            System.exit(1);
        }
    }
}
