package com.xinqi.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xinqi.bean.ConfigEnum;
import com.xinqi.bean.User;
import com.xinqi.util.HttpsClientUtil;
import org.slf4j.Logger;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author XinQi
 */
public class TelegramBotApi {

    /**
     * 消息的偏移数据
     */
    static int update_id = 0;

    public static Map<String, User> userChatData = new HashMap<>();

    /**
     * @return 得到的消息
     */
    public static HashMap<String, String> getUpdates(String botApi, Logger logger) throws Exception {
        //准备请求地址和json
        String url = "https://api.telegram.org/bot" + botApi + "/getUpdates";
        String jsonData = "{\"offset\":" + update_id + "}";

        //发送请求
        byte[] response = HttpsClientUtil.httpsPost(url, jsonData);

        //解析返回的json数据
        JsonNode jsonNode = new ObjectMapper().readTree(response);
        JsonNode result = jsonNode.get("result");

        //判断是否有收到消息
        if (result.size() == 0) {
            return null;
        }

        //得到各个数据
        JsonNode resultData = result.get(0);
        JsonNode resultMessage = resultData.get("message");
        JsonNode resultFrom = resultMessage.get("from");

        //将收到消息的第一个update_id设置为偏移量并再次获取该重复消息以便将该消息偏移去除
        if (update_id == 0) {
            update_id = resultData.get("update_id").asInt();
            return getUpdates(botApi, logger);
        }
        logger.info("循环调用 TelegramBot API 得到新消息，请求地址: {}，接口返回内容为: {}，当前 update_id 为: {}", url, jsonNode, update_id);

        //更新偏移量
        update_id++;

        //获取配置文件里的白名单列表
        String chatId = resultFrom.get("id").asText();
        @SuppressWarnings("unchecked") List<String> whitelist = (List<String>) ConfigEnum.WHITELIST.getValue();
        if (!whitelist.contains("*") && !whitelist.contains(chatId)) {
            logger.info("用户 {} 不在白名单内，正在调用 TelegramBot API 发送不在白名单内的消息", chatId);
            sendMessage(botApi, chatId, ConfigEnum.NOT_WHITELIST_MSG.getValue().toString(), logger);
            return null;
        }

        //判断用户是否是发送了 /start 指令，如果是则发送新建对话消息并删除该用户的ChatGPT对话数据，最终返回Null取消后续操作
        String username = resultFrom.get("first_name").asText() + " " + resultFrom.get("last_name").asText();
        String message = resultMessage.get("text").asText();
        if ("/start".equalsIgnoreCase(message)) {
            logger.info("用户 {} 发送了 /start 指令，正在调用 TelegramBot API 发送新建对话消息", username);
            userChatData.remove(chatId);
            sendMessage(botApi, chatId, ConfigEnum.START_MSG.getValue().toString(), logger);
            return null;
        }


        //保存用户数据
        User user = userChatData.get(chatId);
        long currentTimeMillis = System.currentTimeMillis();
        if (user == null) {
            user = new User();
            user.setChatId(chatId);
            user.setUserName(username);
            user.setStartTime(currentTimeMillis);
            user.setEndTime(currentTimeMillis);
            logger.info("已新建用户 {} 的数据为: {}", username, user);
        } else {
            user.setEndTime(currentTimeMillis);
            logger.info("已更新用户 {} 的 EndTime 时间为: {}", username, currentTimeMillis);
        }
        userChatData.put(chatId, user);

        //返回消息
        HashMap<String, String> responseMap = new HashMap<>();
        responseMap.put("message", message);
        responseMap.put("chat_id", chatId);
        return responseMap;
    }

    public static void sendMessage(String botApi, String sendId, String message, Logger logger) throws Exception {
        logger.info("正在准备调用 TelegramBot API 发送消息，TelegramBot API所需的编码格式为ISO_8859_1，日志打印的编码格式为 UTF-8，请注意区分");

        //准备请求地址和json
        String url = "https://api.telegram.org/bot" + botApi + "/sendMessage";
        //转义json字符串里的"号，防止与json数据冲突
        message = message.replace("\"", "\\\"");
        String jsonData = ("{\"chat_id\": " + sendId + ",\"text\": \"" + message + "\"}");

        //发送请求
        logger.info("正在调用 TelegramBot API 发送消息，请求地址: {}，请求参数: {}", url, jsonData);
        byte[] response = HttpsClientUtil.httpsPost(url, new String(jsonData.getBytes(), StandardCharsets.ISO_8859_1));

        //解析返回的json数据
        JsonNode jsonNode = new ObjectMapper().readTree(response);
        logger.info("调用 TelegramBot API 发送消息返回结果:: {}", jsonNode);
    }
}
