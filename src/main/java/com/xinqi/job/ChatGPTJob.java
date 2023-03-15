package com.xinqi.job;

import com.xinqi.api.ChatGPTApi;
import com.xinqi.api.TelegramBotApi;
import com.xinqi.bean.ConfigEnum;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @author XinQi
 */
//单线程执行
@DisallowConcurrentExecution
public class ChatGPTJob implements Job {

    static Logger logger = LoggerFactory.getLogger(ChatGPTJob.class.getName());

    @Override
    public void execute(JobExecutionContext jobExecutionContext) {
        //接收TelegramBot消息;
        String telegramBotToken = ConfigEnum.TELEGRAM_BOT_TOKEN.getValue().toString();
        Map<String, String> botMessage;
        try {
            botMessage = TelegramBotApi.getUpdates(telegramBotToken, logger);
        } catch (Exception e) {
            logger.error("接收 TelegramBot 消息失败，请检查网络条件和配置文件中 telegram_bot_token 的内容是否正确，错误打印：{}", e.getMessage());
            return;
        }

        //如果TelegramBot没有新消息，直接返回
        if (botMessage == null) {
            return;
        }

        //得到ChatGPT回复
        String chatGptMessage;
        try {
            chatGptMessage = ChatGPTApi.getMessage(ConfigEnum.CHATGPT_API.getValue().toString(), botMessage, logger);
        } catch (Exception e) {
            logger.error("接收 ChatGPT 回复内容失败，请检查网络条件和配置文件中 chatgpt_api 的内容是否正确，错误打印：{}", e.getMessage());
            try {
                TelegramBotApi.sendMessage(telegramBotToken, botMessage.get("chat_id"), ConfigEnum.CHATGPT_ERROR_MSG.getValue().toString(), logger);
            } catch (Exception ex) {
                logger.error("接收 TelegramBot 消息失败，请检查网络条件和配置文件中 telegram_bot_token 的内容是否正确，错误打印：{}", e.getMessage());
            }
            return;
        }

        //发送TelegramBot消息
        try {
            TelegramBotApi.sendMessage(telegramBotToken, botMessage.get("chat_id"), chatGptMessage, logger);
        } catch (Exception e) {
            logger.error("发送 TelegramBot 消息失败，请检查网络条件和配置文件中 telegram_bot_token 的内容是否正确，错误打印：{}", e.getMessage());
        }
    }
}
