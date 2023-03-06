package com.xinqi.bean;

import java.util.Collections;

/**
 * @author XinQi
 */
public enum ConfigEnum {

    CHATGPT_API("chatgpt_api",null),
    TELEGRAM_BOT_TOKEN("telegram_bot_token",null),
    START_MSG("start_msg","你已新建对话（该消息由TelegramBot发出）"),
    WHITELIST("whitelist",Collections.singletonList("*")),
    NOT_WHITELIST_MSG("not_whitelist_msg","你不在白名单内，无法使用机器人"),
    END_MEG("end_meg","你从上一条 ChatGPT 回复开始，已有段时间未使用 ChatGPT，已自动删除 ChatGPT 对话数据（该消息由 TelegramBot 发出）");

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
