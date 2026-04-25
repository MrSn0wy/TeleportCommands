package dev.mrsnowy.teleport_commands.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.mrsnowy.teleport_commands.Constants;
import dev.mrsnowy.teleport_commands.TeleportCommands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Language {
    record languageObject(@Nullable JsonObject jsonObject, int lastUsedTick) {}

    private static final Map<String, languageObject> LANGUAGE_CACHE = new HashMap<>();
    private static final Pattern NUMBER_PATTERN = Pattern.compile("%(\\d+)%");

    /// Attempts to retrieve the JSON file for the language, then caches it in memory.
    private static @Nullable JsonObject getLanguageJson(String language) {
        int currentTick = TeleportCommands.INSTANCE.server.getTickCount();

        if (LANGUAGE_CACHE.containsKey(language)) {
            languageObject cache = LANGUAGE_CACHE.get(language);
            LANGUAGE_CACHE.put(language, new languageObject(cache.jsonObject, currentTick));
            return cache.jsonObject;
        }

        String filePath = String.format("/assets/%s/lang/%s.json", Constants.MOD_ID, language);
        JsonObject value = null;
        try {
            InputStream stream = TeleportCommands.class.getResourceAsStream(filePath);
            if (stream != null) {
                Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
                value = JsonParser.parseReader(reader).getAsJsonObject();
            }
        } catch (Exception e) {
            Constants.LOGGER.info("Error while reading language {}, maybe it's not available", language);
        }

        if (value == null) {
            LANGUAGE_CACHE.put(language, new languageObject(null, currentTick));
        }

        return value;
    }

    // I love rustifying things
    record TranslationResult(@Nullable String value, @Nullable String error) {}

    /// Gets the translation from a key of a language
    private static TranslationResult getTranslation(String language, String key) {
        try {
            JsonObject json = getLanguageJson(language);
            if (json == null) {
                throw new Exception("Error getting the json file for the translation");
            }

            JsonElement translation = json.get(key);
            if (translation == null) {
                throw new Exception("Error getting the key for the translation");
            }
            String translationString = translation.getAsString();
            if (translationString.isEmpty()) {
                throw new Exception("Empty translation key!");
            }

            return new TranslationResult(translationString, null);

        } catch (Exception e) {
            return new TranslationResult(null, e.getMessage());
        }
    }

    // Gets the translated text for each player based on their language, this is fully server side and actually works (UNLIKE MOJANG'S TRANSLATED KEY'S WHICH ARE CLIENT SIDE) (I'm not mad, I swear!)
    public static MutableComponent getTranslation(String key, ServerPlayer player, MutableComponent... args) {
        String language = player.clientInformation().language().toLowerCase();
        TranslationResult translationResult = getTranslation(language, key);

        if (translationResult.value == null) {
            if (!language.equals("en_us")) {

                translationResult = getTranslation("en_us", key);
                if (translationResult.value == null) {
                    Constants.LOGGER.error("Error while fall-backing to default translation from \"{}\": \"{}\". Returning raw json to user.", language, translationResult.error);
                    return Component.literal(key);
                }
            } else {
                Constants.LOGGER.error("Error while loading translation: \"{}\". Returning raw json to user.", translationResult.error);
                return Component.literal(key);
            }
        }

        String translationString = translationResult.value;

        if (args.length == 0) {
            return Component.literal(translationString);
        }

        try {
            // Adds the optional MutableComponents in the correct places
            Matcher matcher = NUMBER_PATTERN.matcher(translationString);
            MutableComponent component = Component.empty();
            int lastIndex = 0;

            while (matcher.find()) {
                int index = Integer.parseInt(matcher.group(1));

                component.append(Component.literal(translationString.substring(lastIndex, matcher.start())));
                component.append(args[index]);

                lastIndex = matcher.end();
            }

            component.append(translationString.substring(lastIndex));
            return component;

        } catch (Exception e) {
            Constants.LOGGER.error("Error while replacing dynamic sections in translation: \"{}\". Returning raw string to user.", e.getMessage());
            return Component.literal(translationString);
        }
    }

    /// Cleans languages out of the cache if they haven't been used for some time.
    // TODO! hook this up!
    public static void cacheCleaner(int currentTick, int tps) {
        LANGUAGE_CACHE.entrySet().removeIf(entry -> {
            if (entry.getValue().lastUsedTick + (120 * tps) < currentTick) {
                return true;
            }
            return false;
        });
    }
}
