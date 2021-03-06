package xyz.gnarbot.gnar.commands.executors.general;

import net.dv8tion.jda.core.entities.Guild;
import xyz.gnarbot.gnar.commands.Command;
import xyz.gnarbot.gnar.commands.CommandExecutor;
import xyz.gnarbot.gnar.utils.Context;
import xyz.gnarbot.gnar.utils.Utils;

@Command(
        id = 16,
        aliases = {"guild", "server"},
        description = "Get information this guild."
)
public class GuildInfoCommand extends CommandExecutor {
    @Override
    public void execute(Context context, String label, String[] args) {
        Guild guild = context.getGuild();

        context.send().embed("Guild Information")
                .setThumbnail(context.getGuild().getIconUrl())
                .field("Name", true, guild.getName())
                .field("ID", true, guild.getId())
                .field("Creation Time", true, guild.getCreationTime())
                .field("Owner", true, guild.getOwner().getAsMention())
                .field("Text Channels", true, guild.getTextChannels().size())
                .field("Voice Channels", true, guild.getVoiceChannels().size())
                .field("Members", true, guild.getMembers().size())
                .field("Default Channel", true, guild.getPublicChannel().getAsMention())
                .field("Premium", true, context.getGuildOptions().isPremium()
                        ? "Premium status expires in `" + Utils.getTime(context.getGuildOptions().remainingPremium()) + "`."
                        : "This guild does not have the premium status.\nVisit our __**[Patreon](https://www.patreon.com/gnarbot)**__ to find out more.")
                .action().queue();
    }
}