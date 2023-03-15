package com.xinqi.bean;

import java.util.Collections;

/**
 * @author XinQi
 */
public enum ConfigEnum {

    CHATGPT_API("chatgpt_api",null),
    TELEGRAM_BOT_TOKEN("telegram_bot_token",null),
    START_MSG("start_msg","已新建 ChatGPT 对话，你可以使用 /response 重新回到上一次对话（该消息由 TelegramBot 发出）"),
    CONFIGURE_DEFAULT_MENU("configure_default_menu",false),
    WHITELIST("whitelist",Collections.singletonList("*")),
    NOT_WHITELIST_MSG("not_whitelist_msg","你不在白名单内，无法使用机器人（该消息由 TelegramBot 发出）"),
    CHATGPT_ERROR_MSG("chatgpt_error_msg","ChatGPT 响应失败，请使用 /response 重试，若频繁出现此问题，请联系管理员（该消息由 TelegramBot 发出）"),
    END_MSG("end_msg","从上一条 ChatGPT 回复开始，已有段时间未使用 ChatGPT，已自动删除 ChatGPT 对话数据，如需要继续上一次对话请发送 /history（该消息由 TelegramBot 发出）"),
    HISTORY_MSG("history_msg","已回到上一次对话（该消息由 TelegramBot 发出）"),
    NO_HISTORY_MSG("no_history_msg","没有上一次对话（该消息由 TelegramBot 发出）");

    private final String key;
    private Object value;

    ConfigEnum(String key, Object value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
