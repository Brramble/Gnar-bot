package xyz.gnarbot.gnar.commands.executors.music

import net.dv8tion.jda.core.EmbedBuilder
import xyz.gnarbot.gnar.Bot
import xyz.gnarbot.gnar.commands.Category
import xyz.gnarbot.gnar.commands.Command
import xyz.gnarbot.gnar.commands.CommandExecutor
import xyz.gnarbot.gnar.commands.Scope
import xyz.gnarbot.gnar.utils.Context
import xyz.gnarbot.gnar.utils.b
import xyz.gnarbot.gnar.utils.desc
import xyz.gnarbot.gnar.utils.ln
import java.util.concurrent.TimeUnit

@Command(
        id = 75,
        aliases = arrayOf("voteskip"),
        description = "Vote to skip the current music track.",
        scope = Scope.VOICE,
        category = Category.MUSIC
)
class VoteSkipCommand : CommandExecutor() {
    override fun execute(context: Context, label: String, args: Array<String>) {
        val manager = Bot.getPlayers().getExisting(context.guild)
        if (manager == null) {
            context.send().error("There's no music player in this guild.\n$PLAY_MESSAGE").queue()
            return
        }

        val member = context.guild.getMember(context.message.author)

        val botChannel = context.guild.selfMember.voiceState.channel
        if (botChannel == null) {
            context.send().error("The bot is not currently in a channel.\n$PLAY_MESSAGE").queue()
            return
        }

        if (member.voiceState.channel != botChannel) {
            context.send().error("You're not in the same channel as the bot.").queue()
            return
        }

        if (manager.player.playingTrack == null) {
            context.send().error("There isn't a song playing.").queue()
            return
        }

        if (member.voiceState.isDeafened) {
            context.send().error("You actually have to be listening to the song to start a vote.").queue()
            return
        }
        if (manager.isVotingToSkip) {
            context.send().error("There is already a vote going on!").queue()
            return
        }
        if (System.currentTimeMillis() - manager.lastVoteTime < Bot.CONFIG.voteSkipCooldown.toMillis()) {
            context.send().error("You must wait ${Bot.CONFIG.voteSkipCooldownText} before starting a new vote.").queue()
            return
        }
        if (manager.player.playingTrack.duration - manager.player.playingTrack.position <= Bot.CONFIG.voteSkipDuration.toMillis()) {
            context.send().error("By the time the vote finishes in ${Bot.CONFIG.voteSkipDurationText}, the song will be over.").queue()
            return
        }

        manager.lastVoteTime = System.currentTimeMillis()
        manager.isVotingToSkip = true

        context.send().embed("Vote Skip") {
            desc {
                buildString {
                    append(b(context.message.author.name))
                    append(" has voted to **skip** the current track!")
                    append(" React with :thumbsup: or :thumbsdown:").ln()
                    append("Whichever has the most votes in ${Bot.CONFIG.voteSkipDurationText} will win!")
                }
            }
        }.action().queue {
            it.addReaction("👍").queue()
            it.addReaction("👎").queue()

            it.editMessage(EmbedBuilder(it.embeds[0]).apply {
                desc { "Voting has ended! Check the newer messages for results." }
                clearFields()
            }.build()).queueAfter(Bot.CONFIG.voteSkipDuration.seconds, TimeUnit.SECONDS) {
                var skip = 0
                var stay = 0

                it.reactions.forEach {
                    if (it.emote.name == "👍") skip = it.count - 1
                    if (it.emote.name == "👎") stay = it.count - 1
                }

                context.send().embed("Vote Skip") {
                    desc {
                        buildString {
                            if (skip > stay) {
                                appendln("The vote has passed! The song has been skipped.")
                                manager.scheduler.nextTrack()
                            } else {
                                appendln("The vote has failed! The song will stay.")
                            }
                        }
                    }
                    field("Results") {
                        "__$skip Skip Votes__ — __$stay Stay Votes__"
                    }
                }.action().queue()
                manager.isVotingToSkip = false
            }
        }
    }
}