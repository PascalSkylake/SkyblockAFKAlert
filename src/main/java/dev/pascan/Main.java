package dev.pascan;

import net.hypixel.api.HypixelAPI;
import net.hypixel.api.http.HypixelHttpClient;
import net.hypixel.api.reactor.ReactorHttpClient;
import net.hypixel.api.reply.StatusReply;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;

import java.awt.*;
import java.io.FileReader;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class Main {
    static StatusReply previousStatus;
    static long discordUserId = 0;

    public static void main(String[] args) {
        String discordBotApiKey = "";
        String hypixelApiKey = "";
        String playerUUID = "";

        // load api keys and userids from config.properties
        try {
            FileReader propertiesReader = new FileReader("config.properties");
            Properties properties = new Properties();
            properties.load(propertiesReader);
            discordBotApiKey = properties.getProperty("DISCORD_API_KEY");
            hypixelApiKey = properties.getProperty("HYPIXEL_API_KEY");
            playerUUID = properties.getProperty("PLAYER_UUID");
            discordUserId = Long.parseLong(properties.getProperty("DISCORD_USERID"));
            System.out.println(discordUserId);
        } catch (Exception e) {e.printStackTrace();}

        DiscordApi api = new DiscordApiBuilder()
                .setToken(discordBotApiKey).login().join();
        HypixelAPI hypixelApi = new HypixelAPI(new ReactorHttpClient(UUID.fromString(hypixelApiKey)));

        // timer which does the stuff once a minute
        Timer timer = new Timer();
        long finalDiscordUserId = discordUserId; // compiler made me do this
        previousStatus = new StatusReply();
        String finalPlayerUUID = playerUUID;
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                StatusReply status = hypixelApi.getStatus(UUID.fromString(finalPlayerUUID)).join();
                System.out.println(status.toString());
                if (!status.toString().equals(previousStatus.toString())) {
                    previousStatus = status;
                    new MessageBuilder()
                            .setTts(true)
                            .setEmbed(new EmbedBuilder()
                                    .setTitle(":warning: ALERT ALERT :warning:")
                                    .setColor(Color.GREEN)
                                    .setDescription("His ass is on " + (status.getSession().isOnline() ? (status.getSession().getServerType() + " " + status.getSession().getMode()) : "offline")))
                            .send(api.getUserById(discordUserId).join());
                }


            }
        }, 0, 2000);
    }
}