package com.xinqi.job;

import com.xinqi.Main;
import com.xinqi.api.ChatGPTApi;
import com.xinqi.api.TelegramBotApi;
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
    public void execute(JobExecutionContext jobExecutionContext){
        //接收TelegramBot消息
        String telegramBotToken = (String) Main.config.get("telegram_bot_token");
        Map<String,String> botMessage;
        try {
            botMessage = TelegramBotApi.getUpdates(telegramBotToken,logger);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        //如果TelegramBot没有新消息，直接返回
        if (botMessage == null) {
            return;
        }

        //得到ChatGPT回复
        String chatGptApi = (String) Main.config.get("chatgpt_api");
        String chatGptMessage;
        try {
            chatGptMessage = ChatGPTApi.getMessage(chatGptApi, botMessage, logger);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        //发送TelegramBot消息
        try {
            TelegramBotApi.sendMessage(telegramBotToken, botMessage.get("chat_id"), chatGptMessage, logger);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
