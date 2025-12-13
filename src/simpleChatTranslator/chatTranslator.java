package simpleChatTranslator;

import arc.Core;
import arc.Events;
import arc.util.Log;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.gen.Player;
import mindustry.mod.Mod;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class chatTranslator extends Mod {

    private static final Pattern GOOGLE_TRANSLATE_PATTERN = Pattern.compile("(?s)class=\"(?:t0|result-container)\">(.*?)<");

    @Override
    public void init() {
        Events.on(EventType.PlayerChatEvent.class, event -> {
            if (event.player == null) return;

            String originalMessage = event.message;
            if (originalMessage == null || originalMessage.trim().isEmpty()) return;

            String targetLang = Core.settings.getString("locale");

            new Thread(() -> translateMessage(event.player, originalMessage, targetLang, "auto")).start();
        });
    }

    private void translateMessage(Player player, String text, String targetLang, String sourceLang) {
        try {
            String encodedText = URLEncoder.encode(text, StandardCharsets.UTF_8.name());
            String urlStr = "https://translate.google.com/m?tl=" + targetLang + "&sl=" + sourceLang + "&q=" + encodedText;

            URL url = new URL(urlStr);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                StringBuilder response = new StringBuilder();
                try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine);
                    }
                }

                Matcher matcher = GOOGLE_TRANSLATE_PATTERN.matcher(response.toString());

                if (matcher.find()) {
                    String translatedText = unescapeHtml(matcher.group(1));
                    if (translatedText != null && !translatedText.trim().isEmpty() && !text.equalsIgnoreCase(translatedText.trim())) {
                        Core.app.post(() -> {
                            String coloredPlayerName = "[#" + player.color().toString().substring(0, 6) + "]" + player.name() + "[]";
                            Vars.player.sendMessage("[#b5b5b5]tr - [white][[" + coloredPlayerName + "[white]]: " + translatedText.trim());
                        });
                    }
                }
            }
        } catch (UnsupportedEncodingException e) {
            Log.err("[Translator] Unsupported encoding", e);
        } catch (Exception e) {
            Log.err("[Translator] Error during translation request", e);
        }
    }

    private String unescapeHtml(String text) {
        if (text == null) return null;
        return text.replace("&amp;", "&").replace("&lt;", "<").replace("&gt;", ">").replace("&quot;", "\"").replace("&#39;", "'").replace("&apos;", "'");
    }
}

