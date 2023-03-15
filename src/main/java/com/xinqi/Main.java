package com.xinqi;

import com.xinqi.bean.ConfigEnum;
import com.xinqi.job.ChatGPTJob;
import com.xinqi.job.UserDeleteJob;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author XinQi
 */
public class Main {

    static Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws IOException, SchedulerException {
        //得到配置文件的目录
        //获取类当前路径
        String configPath = Main.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        //处理路径
        //去除多余的路径，如：classes
        configPath = configPath.replace(new File(configPath).getName(), "");
        //将字符集转成UTF-8，以去除特殊字符
        configPath = (URLDecoder.decode(configPath, "UTF-8") + "config.yaml");
        //转换成一个完整且规范的路径
        configPath = new File(configPath).getPath();

        //读取配置文件
        Map<String, Object> config = new HashMap<>();
        try {
            config = new Yaml().load(Files.newInputStream(Paths.get(configPath)));
        } catch (IOException e) {
            logger.error("无法从 {} 该路径下获取配置文件，请检查该路径是否存在配置文件，配置文件可通过解压 jar 包获得，错误打印：{}", configPath, e.getMessage());
            System.exit(0);
        }

        //循环读取枚举，将配置文件里内容并写入至枚举中，并检查严重性标签是否缺失内容
        ConfigEnum[] configEnums = ConfigEnum.values();
        List<String> configNoKeyList = new ArrayList<>();
        List<String> configNoKeyByMust = new ArrayList<>();
        for (ConfigEnum configEnum : configEnums) {
            String configEnumKey = configEnum.getKey();
            Object configNode = config.get(configEnumKey);
            if (configNode != null && !("").equals(configNode.toString())) {
                configEnum.setValue(configNode);
            } else if ("chatgpt_api".equals(configEnumKey) || "telegram_bot_token".equals(configEnumKey)) {
                configNoKeyByMust.add(configEnumKey);
            } else {
                configNoKeyList.add(configEnumKey);
            }
        }

        //判断是否有必须标签丢失
        if (!configNoKeyByMust.isEmpty()) {
            for (String configNoKeyTag : configNoKeyByMust) {
                logger.error("配置文件缺少标签或标签内容为空：{}，该标签为严重性标签，程序将无法正常运行，请检查配置文件", configNoKeyTag);
            }
            System.exit(0);
        }

        //判断是否有标签丢失
        if (!configNoKeyList.isEmpty()) {
            for (String configNoKeyTag : configNoKeyList) {
                logger.warn("配置文件缺少标签：{}，该标签将使用默认值", configNoKeyTag);
            }
        }

        //创建调度器工厂
        SchedulerFactory factory = new StdSchedulerFactory();
        // 1.创建调度器 Scheduler
        Scheduler scheduler = factory.getScheduler();

        //构建自动消息任务
        // 2.创建JobDetail实例，并与MyJob类绑定(Job执行内容)
        JobDetail chatGptJob = JobBuilder.newJob(ChatGPTJob.class).withIdentity("job1", "chatGptJob").build();
        // 3.构建Trigger实例,每隔1秒执行一次
        Trigger chatGptTrigger = TriggerBuilder.newTrigger().withIdentity("trigger1", "chatGptTrigger").startNow().withSchedule(CronScheduleBuilder.cronSchedule("0/1 * * * * ? *")).build();

        //构建用户数据结束任务
        JobDetail userDeleteJob = JobBuilder.newJob(UserDeleteJob.class).withIdentity("job1", "userDeleteJob").build();
        Trigger userDeleteTrigger = TriggerBuilder.newTrigger().withIdentity("trigger1", "userDeleteTrigger").startNow().withSchedule(CronScheduleBuilder.cronSchedule("0 0/5 * * * ? *")).build();

        // 4.执行，开启调度器
        scheduler.scheduleJob(chatGptJob, chatGptTrigger);
        scheduler.scheduleJob(userDeleteJob, userDeleteTrigger);
        scheduler.start();
        logger.info("已成功开启 ChatGPT-TelegramBot，请确保对应 API 参数正确和网络能正常访问相关服务");
    }
}
