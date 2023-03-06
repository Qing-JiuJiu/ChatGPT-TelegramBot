package com.xinqi.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xinqi.Main;
import com.xinqi.util.HttpsClientUtil;
import org.slf4j.Logger;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;

/**
 * @author XinQi
 */
public class TelegramBotApi {

    /**
     * 得到消息的偏移数据
     */
    static int update_id = 0;

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

        //将收到消息的第一个update_id设置为偏移量并再次获取该重复消息以便将该消息偏移去除
        if (update_id == 0) {
            update_id = result.get(0).get("update_id").asInt();
            return getUpdates(botApi, logger);
        }
        logger.info("循环调用 TelegramBot API 得到新消息，请求地址: {}，接口返回内容为: {}，当前 update_id 为: {}", url, jsonNode, update_id);

        //更新偏移量
        update_id++;

        //得到两个内容，一个是消息内容，一个是消息发送者的id
        String message = result.get(0).get("message").get("text").asText();
        String chatId = result.get(0).get("message").get("from").get("id").asText();

        //获取配置文件里的白名单列表
        @SuppressWarnings("unchecked") List<String> whitelist = (List<String>) Main.config.get("whitelist");
        if (!whitelist.contains("*") && !whitelist.contains(chatId)){
            logger.info("用户 {} 不在白名单内，正在调用 TelegramBot API 发送不在白名单内的消息", chatId);
            sendMessage(botApi, chatId, Main.config.get("not_whitelist_msg").toString(), logger);
            return null;
        }

        //判断用户是否是发送了 /start 指令，如果是则发送新建对话消息并删除该用户的ChatGPT对话数据，最终返回Null取消后续操作
        if ("/new".equalsIgnoreCase(message)){
            logger.info("用户 {} 发送了 /new 指令，正在调用 TelegramBot API 发送新建对话消息", chatId);
            ChatGPTApi.chatGptData.remove(chatId);
            sendMessage(botApi, chatId, "你已新建对话（该消息由TelegramBot发出）", logger);
            return null;
        }

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
