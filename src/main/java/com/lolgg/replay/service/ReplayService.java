package com.lolgg.replay.service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * replay service
 * @author codingpoppy94
 * @version 1.0
 */
@Service
public class ReplayService {

    @Value("${api.league-url}")
    private String leagueUrl;

    // 리플파일 데이터 restclient 전송
    public void sendData(Map<String,Object> body){
        RestClient restClient = RestClient.create();
        String result = restClient.post()
        .uri(leagueUrl)
        .contentType(MediaType.APPLICATION_JSON)
        .body(body)
        .retrieve()
        .body(String.class);
        System.out.println(result);
    }

    // 기본
    public JsonNode parseReplay(String fileUrl) throws Exception{
        InputStream inputStream = getInputStreamDiscordFile(fileUrl);
        try {
            return parseReplayData(inputStream);
        } finally {
            inputStream.close();
        }
    }

    // 리플레이 파일 데이터 파싱
    public JsonNode parseReplayData(InputStream inputStream) throws Exception{
        String startIndex = "{\"gameLength\":";
        String endIndex = "\\\"}]\"}";
        int bytesToRead = 65536; 

        try {
            // 파일 열기
            long fileSize = inputStream.available();
            System.out.println(fileSize);

            long startPosition = Math.max(fileSize - bytesToRead, 0);
            int actualBytesToRead = (int) Math.min(bytesToRead, fileSize);

            byte[] bytes = new byte[actualBytesToRead];

            inputStream.skip(startPosition);
            inputStream.read(bytes);

            String data = new String(bytes, "UTF-8");

            StringBuilder hexData = new StringBuilder();

            for (int i = 0; i < data.length(); i++) {
                hexData.append(data.charAt(i));

                if (hexData.toString().endsWith(startIndex)) {
                    hexData.setLength(0);
                    hexData.append(startIndex);
                }
                if (hexData.toString().endsWith(endIndex)) {
                    break;
                }
            }
            
            if(hexData.isEmpty()){
                throw new Exception("파싱 데이터가 없습니다");
            }

            String StringData = hexData.toString().replace("\\"+"\"", "\"");
            StringData = StringData.replace("\"[", "[").replace("]\"", "]");

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(StringData);
            JsonNode statsArray = rootNode.get("statsJson");

            // System.out.println("파싱완료");
            // 파일 닫기
            inputStream.close();
            return statsArray;
        } catch (IOException e) {
            e.printStackTrace();
            throw new Exception("파싱에러");
        }
    }

    // 디스코드에 올린 파일 데이터 가져오기
    private InputStream getInputStreamDiscordFile(String fileUrl) throws IOException, InterruptedException {
        // HttpClient 생성
        HttpClient httpClient = HttpClient.newHttpClient();

        // HTTP 요청 생성
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(fileUrl))
            .build();

        HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());

        return response.body();
    }
}
