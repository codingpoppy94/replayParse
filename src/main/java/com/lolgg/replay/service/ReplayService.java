package com.lolgg.replay.service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(ReplayService.class);

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
        logger.info(result);
    }

    // 기본
    public JsonNode parseReplay(String fileUrl) throws Exception{
        byte[] bytes = getInputStreamDiscordFile(fileUrl);
        try {
            return parseReplayData(bytes);
        } finally {
            // bytes.close();
        }
    }

    // 리플레이 파일 데이터 파싱
    public JsonNode parseReplayData(byte[] byteArrays) throws Exception{
        String startIndex = "{\"gameLength\":";
        String endIndex = "\\\"}]\"}";
        int bytesToRead = 65536; 

        if(byteArrays == null || byteArrays.length == 0){
            throw new Exception("파싱 데이터가 없습니다");
        }

        try {
            int fileSize = byteArrays.length;

            long startPosition = Math.max(fileSize - bytesToRead, 0);
            int actualBytesToRead = (int) Math.min(bytesToRead, fileSize);

            byte[] bytes = new byte[actualBytesToRead];

            System.arraycopy(byteArrays, (int) startPosition, bytes, 0, actualBytesToRead);

            // inputStream.skip(startPosition);
            // inputStream.read(bytes);

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
            
            String StringData = hexData.toString().replace("\\"+"\"", "\"");
            StringData = StringData.replace("\"[", "[").replace("]\"", "]");

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(StringData);
            JsonNode statsArray = rootNode.get("statsJson");

            return statsArray;
        } catch (IOException e) {
            e.printStackTrace();
            throw new Exception("파싱에러");
        }
    }

    // 디스코드에 올린 파일 데이터 가져오기
    private byte[] getInputStreamDiscordFile(String fileUrl) throws IOException, InterruptedException {
        // HttpClient 생성
        HttpClient httpClient = HttpClient.newHttpClient();

        // HTTP 요청 생성
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(fileUrl))
            .build();

        HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
        return changeByteArray(response.body());
    }

    // byte[] 변환
    public byte[] changeByteArray(InputStream inputStream) {
        int nRead;
        byte[] data = new byte[1024]; 
        try (ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
            while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                // 읽은 데이터의 각 바이트를 로그로 출력
                // for (int i = 0; i < nRead; i++) {
                    // System.out.printf("%02X ", data[i]);
                // }
                buffer.write(data, 0, nRead);
            }
                // 버퍼 플러시
            buffer.flush();
            return buffer.toByteArray();
        } catch (Exception e) {
            logger.info(e.getMessage());
            return null;
        }
    }
}
