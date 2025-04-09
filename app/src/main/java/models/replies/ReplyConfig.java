package models.replies;

import language.ru.command.settings.DefaultReply;
import org.checkerframework.checker.nullness.qual.NonNull;


public class ReplyConfig {
    @NonNull
    public final static ReplySettings DEFAULT_REPLY_SETTINGS = new ReplySettings(
            DefaultReply.COMMAND_NAME,
            DefaultReply.COMMAND_DESCRIPTION
    );
}
