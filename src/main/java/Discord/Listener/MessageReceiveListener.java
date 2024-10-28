package Discord.Listener;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.EmbedType;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.internal.entities.channel.concrete.PrivateChannelImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;

public class MessageReceiveListener extends ListenerAdapter {

    private final String prefix = "!";

    private Logger log = LogManager.getLogger(MessageReceiveListener.class);

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        // We don't want to respond to other bot accounts, including ourself
        Message message = event.getMessage();
        String content = message.getContentRaw();
        // getContentRaw() is an atomic getter
        // getContentDisplay() is a lazy getter which modifies the content for e.g. console view (strip discord formatting)
        if (content.equals(prefix + "ping")) {
            MessageChannel channel = event.getChannel();
//            channel.sendMessage("Pong!").queue(); // Important to call .queue() on the RestAction returned by sendMessage(...)
//            MessageChannel channel = new PrivateChannelImpl(jda, Long.parseLong("1126910245990584424"), null);
            EmbedBuilder embedBuilder = new EmbedBuilder();

            embedBuilder.setTitle("Test title", "https://youtu.be/3HiwHYM-0Ow");

            embedBuilder.setColor(Color.GREEN);

            embedBuilder.setDescription("https://youtu.be/3HiwHYM-0Ow");

            embedBuilder.setAuthor("Nyaruko166", "https://www.facebook.com/nyaruko166", "https://i.imgur.com/aINmd3V.jpg");

            embedBuilder.setFooter("seg", event.getAuthor().getAvatarUrl());

            channel.sendMessageEmbeds(embedBuilder.build()).queue();

//            channel.sendMessageEmbeds(new MessageEmbed("","","", EmbedType.VIDEO))
        }
        if (content.equals(prefix + "test")) {
            MessageChannel channel = event.getChannel();

//            EmbedBuilder embedBuilder = new EmbedBuilder();
//
//            embedBuilder.setTitle("Test title", "https://youtu.be/3HiwHYM-0Ow");
//
//            embedBuilder.setColor(Color.GREEN);
//
//            embedBuilder.setDescription("https://youtu.be/3HiwHYM-0Ow");
//
//            embedBuilder.setAuthor("Nyaruko166", "https://www.facebook.com/nyaruko166", "https://i.imgur.com/aINmd3V.jpg");
//
//            embedBuilder.setFooter("seg", event.getAuthor().getAvatarUrl());
            log.info("Info");
            log.debug("Debug");
            channel.sendMessage("https://youtu.be/3HiwHYM-0Ow").queue();
        }
    }


}
