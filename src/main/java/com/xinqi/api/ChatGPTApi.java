package com.xinqi.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mchange.v2.lang.StringUtils;
import com.xinqi.bean.User;
import com.xinqi.util.HttpsClientUtil;
import org.slf4j.Logger;

import java.nio.charset.StandardCharsets;
import java.util.Map;


/**
 * @author XinQi
 */
public class ChatGPTApi {

    public static String getMessage(String chatGptApi, Map<String, String> messageMap, Logger logger) throws Exception {
        logger.info("正在准备调用 ChatGPT API 获取聊天内容，ChatGPT API所需的编码格式为ISO_8859_1，日志打印的编码格式为 UTF-8，请注意区分");

        //判断用户是不是新建对话，如果是新建对话，就不需要把上一次的聊天内容加进去，并且记录此次Json用于再次响应
        String chatId = messageMap.get("chat_id");
        String message = messageMap.get("message");
        User user = TelegramBotApi.userChatData.get(chatId);
        String userMessage = user.getMessage();
        String chatGptMessage = user.getReMessage();

        if (StringUtils.nonEmptyString(userMessage)) {
            if (!"/response".equals(message)) {
                chatGptMessage = userMessage + ",{\"role\": \"user\", \"content\": \"" + message.replace("\"", "\\\"").replace("\\\\\"", "\\\\\\\"") + "\"}";
            }
        } else {
            chatGptMessage = "{\"role\": \"user\", \"content\": \"" + message.replace("\"", "\\\"").replace("\\\\\"", "\\\\\\\"") + "\"}";
        }
        user.setReMessage(chatGptMessage);

        //准备请求地址和json
        String url = "https://api.openai.com/v1/chat/completions";
        String jsonData = "{\"model\": \"gpt-3.5-turbo\",\"messages\": [" + chatGptMessage + "]}";

        //发送请求
        logger.info("正在调用 ChatGPT API 获取聊天内容，请求地址: {}，请求参数: {}", url, jsonData);
        byte[] response = HttpsClientUtil.httpsPostChatGpt(url, new String(jsonData.getBytes(), StandardCharsets.ISO_8859_1), chatGptApi);

        //解析返回的json数据
        JsonNode jsonNode = new ObjectMapper().readTree(response);
        logger.info("调用 ChatGPT API 获取聊天内容返回结果: {}", jsonNode);

        //获取ChatGPT回复片段
        JsonNode chatGptResponse = jsonNode.get("choices").get(0).get("message");

        //存入用户对话Map，用于持续对话
        chatGptMessage = chatGptMessage + "," + chatGptResponse.toString();
        user.setMessage(chatGptMessage);
        logger.info("已保存用户 {} 的 ChatGPT 聊天内容为: {}", user.getUserName(), chatGptMessage);
        TelegramBotApi.userChatData.put(chatId, user);

        //返回ChatGPT回复内容
        return chatGptResponse.get("content").asText();
    }
}
