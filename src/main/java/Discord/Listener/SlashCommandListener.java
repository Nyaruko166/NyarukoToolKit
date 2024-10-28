package Discord.Listener;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

public class SlashCommandListener extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        switch (event.getName()) {
            case "say" -> {
                String content = event.getOption("content", OptionMapping::getAsString);
                event.reply(content).queue();
            }
            case "leave" -> {
                event.reply("I'm leaving the server now!").setEphemeral(true) // this message is only visible to the command user
                        .flatMap(m -> event.getGuild().leave()) // append a follow-up action using flatMap
                        .queue(); // enqueue both actions to run in sequence (send message -> leave guild)
            }
            default -> {
                return;
            }
        }
    }

}
