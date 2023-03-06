package com.xinqi.job;

import com.xinqi.api.TelegramBotApi;
import com.xinqi.bean.ConfigEnum;
import com.xinqi.bean.User;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author XinQi
 */
//单线程执行
@DisallowConcurrentExecution
public class UserDeleteJob implements Job {

    static Logger logger = LoggerFactory.getLogger(UserDeleteJob.class.getName());

    @Override
    public void execute(JobExecutionContext jobExecutionContext) {
        logger.info("开始检查用户数据");

        String endMessage = ConfigEnum.END_MEG.getValue().toString();
        String telegramBotToken = ConfigEnum.TELEGRAM_BOT_TOKEN.getValue().toString();
        long currentTimeMillis = System.currentTimeMillis();

        //判断每个用户系统当前时间跟结束时间是否超过5分钟,如果超过5分钟,删除用户数据
        for (User user : TelegramBotApi.userChatData.values()) {
            if (currentTimeMillis - user.getEndTime() > 60 * 5 * 1000) {
                String chatId = user.getChatId();
                try {
                    TelegramBotApi.sendMessage(telegramBotToken, chatId, endMessage, logger);
                } catch (Exception e) {
                    logger.error("发送 TelegramBot 消息失败，请检查网络条件和配置文件中 telegram_bot_token 的内容是否正确");
                }
                TelegramBotApi.userChatData.remove(chatId);
                logger.info("已删除用户 {} 的数据: {}", user.getUserName(), user);
            }
        }

        //判断每个用户系统开始时间跟结束时间是否超过60分钟，如果超过60分钟，发送警告日志
        TelegramBotApi.userChatData.forEach((chatId, user) -> {
            if (user.getEndTime() - user.getStartTime() > 60 * 60 * 1000) {
                logger.warn("用户 {} 持续使用 ChatGPT 超过1个钟，用户数据为：{}", user.getUserName(), user);
            }
        });

        logger.info("检查用户数据结束");
    }
}