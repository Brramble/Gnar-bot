package xyz.gnarbot.gnar.commands.executors.general

import com.google.common.collect.Lists
import xyz.gnarbot.gnar.Bot
import xyz.gnarbot.gnar.commands.Category
import xyz.gnarbot.gnar.commands.Command
import xyz.gnarbot.gnar.commands.CommandExecutor
import xyz.gnarbot.gnar.utils.Context
import xyz.gnarbot.gnar.utils.ln

@Command(
        id = 48,
        aliases = arrayOf("shards", "shard", "shardinfo"),
        description = "Get shard information.",
        category = Category.NONE
)
class ShardInfoCommand : CommandExecutor() {
    override fun execute(context: Context, label: String, args: Array<String>) {
        var page = if (args.isNotEmpty()) {
            args[0].toIntOrNull() ?: 1
        } else {
            1
        }

        context.send().embed("Shard Information") {
            val pages = Lists.partition(Bot.getShards().toList(), 12)

            if (page >= pages.size) page = pages.size
            else if (page <= 0) page = 1

            val shards = pages[page - 1]

            shards.forEach {
                field("Shard ${it.id}", true) {
                    buildString {
                        append("Status: ").append(it.jda.status).ln()
                        append("Guilds: ").append(it.jda.guilds.size).ln()
                        append("Users: ").append(it.jda.users.size).ln()
                        append("Requests: ").append(it.requests).ln()
                    }
                }
            }

            setFooter("Page [$page/${pages.size}]", null)
        }.action().queue()
    }
}