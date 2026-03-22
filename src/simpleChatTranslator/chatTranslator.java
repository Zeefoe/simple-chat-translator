package simpleChatTranslator;

import arc.Core;
import arc.Events;
import arc.util.CommandHandler;
import arc.util.Log;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.gen.Player;
import mindustry.mod.Mod;
import mindustry.core.NetClient;

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

    private static final String LANGUAGE_CODES =
        "[scarlet]Translation failed or no changes made.\n\n" +
        "[accent]Language codes:[white]\n" +
        "[green]af[] - Afrikaans,  [green]ak[] - Akan,  [green]sq[] - Albanian,  [green]am[] - Amharic,  [green]ar[] - Arabic,\n" +
        "[green]hy[] - Armenian,   [green]az[] - Azerbaijani,  [green]eu[] - Basque,    [green]be[] - Belarusian,\n" +
        "[green]bem[] - Bemba,     [green]bn[] - Bengali,     [green]bh[] - Bihari,    [green]bs[] - Bosnian,\n" +
        "[green]br[] - Breton,     [green]bg[] - Bulgarian,   [green]km[] - Cambodian,  [green]ca[] - Catalan,\n" +
        "[green]chr[] - Cherokee,  [green]ny[] - Chichewa,    [green]zh-CN[] - Chinese(Simple),\n" +
        "[green]zh-TW[] - Chinese(Traditional),  [green]co[] - Corsican,  [green]hr[] - Croatian,  [green]cs[] - Czech,\n" +
        "[green]da[] - Danish,      [green]nl[] - Dutch,        [green]en[] - English,   [green]eo[] - Esperanto,\n" +
        "[green]et[] - Estonian,   [green]ee[] - Ewe,          [green]fo[] - Faroese,   [green]tl[] - Filipino,\n" +
        "[green]fi[] - Finnish,    [green]fr[] - French,      [green]fy[] - Frisian,   [green]gaa[] - Ga,\n" +
        "[green]gl[] - Galician,   [green]ka[] - Georgian,    [green]de[] - German,    [green]el[] - Greek,\n" +
        "[green]gn[] - Guarani,    [green]gu[] - Gujarati,    [green]ht[] - Haitian,   [green]ha[] - Hausa,\n" +
        "[green]haw[] - Hawaiian,  [green]iw[] - Hebrew,      [green]hi[] - Hindi,     [green]hu[] - Hungarian,\n" +
        "[green]is[] - Icelandic,  [green]ig[] - Igbo,        [green]id[] - Indonesian,  [green]ia[] - Interlingua,\n" +
        "[green]ga[] - Irish,     [green]it[] - Italian,     [green]ja[] - Japanese,   [green]jw[] - Javanese,\n" +
        "[green]kn[] - Kannada,   [green]kk[] - Kazakh,      [green]rw[] - Kinyarwanda,  [green]rn[] - Kirundi,\n" +
        "[green]kg[] - Kongo,     [green]ko[] - Korean,      [green]kri[] - Krio,     [green]ku[] - Kurdish,\n" +
        "[green]ckb[] - Kurdish(Soranî),  [green]ky[] - Kyrgyz,  [green]lo[] - Laothian,  [green]la[] - Latin,\n" +
        "[green]lv[] - Latvian,   [green]ln[] - Lingala,     [green]lt[] - Lithuanian,  [green]loz[] - Lozi,\n" +
        "[green]lg[] - Luganda,   [green]ach[] - Luo,        [green]mk[] - Macedonian,  [green]mg[] - Malagasy,\n" +
        "[green]ms[] - Malay,     [green]ml[] - Malayalam,  [green]mt[] - Maltese,   [green]mi[] - Maori,\n" +
        "[green]mr[] - Marathi,   [green]mfe[] - Mauritian,  [green]mo[] - Moldavian,  [green]mn[] - Mongolian,\n" +
        "[green]sr-ME[] - Montenegrin,  [green]ne[] - Nepali,  [green]pcm[] - NigerianPidgin,\n" +
        "[green]nso[] - NorthernSotho,  [green]no[] - Norwegian,  [green]nn[] - NorwegianNynorsk,\n" +
        "[green]oc[] - Occitan,   [green]or[] - Oriya,       [green]om[] - Oromo,     [green]ps[] - Pashto,\n" +
        "[green]fa[] - Persian,   [green]pl[] - Polish,      [green]pt-BR[] - Portuguese(BR),\n" +
        "[green]pt-PT[] - Portuguese(PT),  [green]pa[] - Punjabi,  [green]qu[] - Quechua,\n" +
        "[green]ro[] - Romanian,  [green]rm[] - Romansh,     [green]nyn[] - Runyakitara,  [green]ru[] - Russian,\n" +
        "[green]gd[] - ScotsGaelic,  [green]sr[] - Serbian,  [green]sh[] - SerboCroatian,\n" +
        "[green]st[] - Sesotho,   [green]tn[] - Setswana,    [green]crs[] - Seychellois,  [green]sn[] - Shona,\n" +
        "[green]sd[] - Sindhi,    [green]si[] - Sinhalese,  [green]sk[] - Slovak,    [green]sl[] - Slovenian,\n" +
        "[green]so[] - Somali,    [green]es[] - Spanish,     [green]es-419[] - Spanish(LATAM),\n" +
        "[green]su[] - Sundanese, [green]sw[] - Swahili,    [green]sv[] - Swedish,    [green]tg[] - Tajik,\n" +
        "[green]ta[] - Tamil,     [green]tt[] - Tatar,      [green]te[] - Telugu,    [green]th[] - Thai,\n" +
        "[green]ti[] - Tigrinya,  [green]to[] - Tonga,      [green]lua[] - Tshiluba,  [green]tum[] - Tumbuka,\n" +
        "[green]tr[] - Turkish,   [green]tk[] - Turkmen,    [green]tw[] - Twi,        [green]ug[] - Uighur,\n" +
        "[green]uk[] - Ukrainian, [green]ur[] - Urdu,      [green]uz[] - Uzbek,     [green]vi[] - Vietnamese,\n" +
        "[green]cy[] - Welsh,     [green]wo[] - Wolof,      [green]xh[] - Xhosa,     [green]yi[] - Yiddish,\n" +
        "[green]yo[] - Yoruba,    [green]zu[] - Zulu";

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

    @Override
    public void registerClientCommands(CommandHandler handler) {
        handler.<Player>register("trs", "<lang-code> <message...>", "Translate and send a message", (args, player) -> {
            sendTranslatedMessage(args, player, false);
        });

        handler.<Player>register("ttrs", "<lang-code> <message...>", "Translate and send a message to team", (args, player) -> {
            sendTranslatedMessage(args, player, true);
        });
    }

    private void sendTranslatedMessage(String[] args, Player player, boolean toTeam) {
        String langCode = args[0];
        String message = args[1];

        for (int i = 2; i < args.length; i++) {
            message += " " + args[i];
        }

        final String finalMessage = message;
        final String finalLangCode = langCode;

        new Thread(() -> {
            String translated = translateText(finalMessage, "auto", finalLangCode);
            if (translated != null && !finalMessage.equals(translated)) {
                Core.app.post(() -> {
                    String sendMessage = toTeam ? "/t " + translated : translated;
                    NetClient.sendChatMessage(player, sendMessage);
                });
            } else {
                Core.app.post(() -> {
                    player.sendMessage(LANGUAGE_CODES);
                });
            }
        }).start();
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
    
    private String translateText(String text, String sourceLang, String targetLang) {
        try {
            String encodedText = URLEncoder.encode(text, StandardCharsets.UTF_8.name());
            String urlStr = "https://translate.google.com/m?tl=" + targetLang + "&sl=" + sourceLang + "&q=" + encodedText;
            
            URL url = new URL(urlStr);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            
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
                    return unescapeHtml(matcher.group(1));
                }
            }
        } catch (Exception e) {
            Log.err("[Translator] Error translating outgoing message", e);
        }
        return text;
    }

    private String unescapeHtml(String text) {
        if (text == null) return null;
        return text.replace("&amp;", "&").replace("&lt;", "<").replace("&gt;", ">").replace("&quot;", "\"").replace("&#39;", "'").replace("&apos;", "'");
    }
}

