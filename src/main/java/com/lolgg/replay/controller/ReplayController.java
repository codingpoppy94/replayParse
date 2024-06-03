package com.lolgg.replay.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.JsonNode;
import com.lolgg.replay.service.ReplayService;

/**
 * replay controller
 * @author codingpoppy94
 * @version 1.0
 */
@RestController
@RequestMapping("/replay")
public class ReplayController {

    private static final Logger logger = LoggerFactory.getLogger(ReplayController.class);
        
    public final ReplayService replayService;
    
    public ReplayController(ReplayService replayService){
        this.replayService = replayService;
    }
    
    @PostMapping("/parse")
    public JsonNode parse(@RequestBody JsonNode fileUrl) throws Exception {
        // logger.info(fileUrl.get("fileUrl").asText());
        return replayService.parseReplay(fileUrl.get("fileUrl").asText());
    }

    // 14.11 패치 이후
    @PostMapping("/parseFromApi")
    public String multiParse(@RequestBody List<MultipartFile> files) throws IOException, Exception {
        for(MultipartFile file : files) {
            Map<String,Object> datas = new HashMap<String,Object>();
            byte[] bytes = replayService.changeByteArray(file.getInputStream());
            JsonNode jsonNode = replayService.parseReplayData(bytes);
            datas.put("fileNameWithExt", file.getOriginalFilename());
            datas.put("createUser", "api");
            datas.put("body", jsonNode);
            replayService.sendData(datas);
        }
        return "성공";
    }

    // 14.10 패치 이전
    @PostMapping("/parseFromApiBefore")
    public String parseFromApiBefore(@RequestBody List<MultipartFile> files) throws IOException, Exception {
        for(MultipartFile file : files) {
            Map<String,Object> datas = new HashMap<String,Object>();
            byte[] bytes = replayService.changeByteArray(file.getInputStream());
            JsonNode jsonNode = replayService.parseReplayDataBefore(bytes);
            datas.put("fileNameWithExt", file.getOriginalFilename());
            datas.put("createUser", "api");
            datas.put("body", jsonNode);
            replayService.sendData(datas);
        }
        return "성공";
    }
}
