package Discord;

import Discord.Listener.MessageReceiveListener;
import Util.Config;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;

import java.util.EnumSet;

public class Bot {

    private static final String DISCORD_TOKEN = Config.getInstance().getProperty().getDiscord_token();

    public static void main(String[] args) {
        JDA jda = JDABuilder.createLight(DISCORD_TOKEN, EnumSet.of(GatewayIntent.GUILD_MESSAGES,
                        GatewayIntent.GUILD_MESSAGE_REACTIONS, GatewayIntent.MESSAGE_CONTENT))
                .setActivity(Activity.of(Activity.ActivityType.WATCHING, "Just a random bot passing through."))
                .addEventListeners(new MessageReceiveListener())
//                .addEventListeners(new SlashCommandListener())
                .build();

        // Register your commands to make them visible globally on Discord:
        CommandListUpdateAction commands = jda.updateCommands();

        // Add all your commands on this action instance
        commands.addCommands(
                Commands.slash("say", "Makes the bot say what you tell it to")
                        .addOption(OptionType.STRING, "content", "What the bot should say", true), // Accepting a user input
                Commands.slash("leave", "Makes the bot leave the server")
                        .setGuildOnly(true) // this doesn't make sense in DMs
                        .setDefaultPermissions(DefaultMemberPermissions.DISABLED) // only admins should be able to use this command.
        );

        // Then finally send your commands to discord using the API
        commands.queue();
    }

}
