package com.xinqi.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xinqi.util.HttpsClientUtil;
import org.slf4j.Logger;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;


/**
 * @author XinQi
 */
public class ChatGPTApi {

    //用于存储用户的JSON数据，方便下次调用，用于聊天
    static Map<String, String> chatGptData = new HashMap<>();

    public static String getMessage(String chatGptApi, Map<String, String> messageMap, Logger logger) throws Exception {
        logger.info("正在准备调用 ChatGPT API 获取聊天内容，ChatGPT API所需的编码格式为ISO_8859_1，日志打印的编码格式为 UTF-8，请注意区分");

        //判断用户是不是新建对话，如果是新建对话，就不需要把上一次的聊天内容加进去
        String chatId = messageMap.get("chat_id");
        String message = chatGptData.get(chatId);
        String chatGptMessage;
        if (message == null) {
            chatGptMessage = "{\"role\": \"user\", \"content\": \"" + messageMap.get("message").replace("\"","\\\"") + "\"}";
        } else {
            chatGptMessage = message + ",{\"role\": \"user\", \"content\": \"" + messageMap.get("message").replace("\"","\\\"") + "\"}";
        }

        //准备请求地址和json
        String url = "https://api.openai.com/v1/chat/completions";
        String jsonData = "{\"model\": \"gpt-3.5-turbo\",\"messages\": ["+ chatGptMessage +"]}";

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
        chatGptData.put(chatId, chatGptMessage);

        //返回ChatGPT回复内容
        return chatGptResponse.get("content").asText();
    }
}
