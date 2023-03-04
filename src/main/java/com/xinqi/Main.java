package com.xinqi;

import com.xinqi.job.ChatGPTJob;

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
import java.util.HashMap;
import java.util.Map;

/**
 * @author XinQi
 */
public class Main {

    static Logger logger = LoggerFactory.getLogger(Main.class);

    public static Map<String, Object> config = new HashMap<>();

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
        try {
            config = new Yaml().load(Files.newInputStream(Paths.get(configPath)));
        } catch (IOException e) {
            logger.error("无法从" + configPath + "该路径下获取配置文件，请检查该路径是否存在配置文件，配置文件可通过解压jar包获得");
            e.printStackTrace();
            System.exit(0);
        }

        //创建调度器工厂
        SchedulerFactory factory = new StdSchedulerFactory();
        // 1.创建调度器 Scheduler
        Scheduler scheduler = factory.getScheduler();
        // 2.创建JobDetail实例，并与MyJob类绑定(Job执行内容)
        JobDetail job = JobBuilder.newJob(ChatGPTJob.class).withIdentity("job1", "group1").usingJobData("configPath", configPath).build();
        // 3.构建Trigger实例,每隔1秒执行一次
        Trigger trigger = TriggerBuilder.newTrigger().withIdentity("trigger1", "group1").startNow().withSchedule(CronScheduleBuilder.cronSchedule("0/1 * * * * ? *")).build();
        // 4.执行，开启调度器
        scheduler.scheduleJob(job, trigger);
        scheduler.start();

        logger.info("已成功开启ChatGPT，每隔1秒将会自动获取Telegram机器人消息，请确保对应 API 参数正确且网络能正常访问相关服务，否则将会出现大量报错");
    }
}
