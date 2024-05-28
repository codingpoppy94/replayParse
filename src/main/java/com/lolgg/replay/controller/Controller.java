package com.lolgg.replay.controller;

import java.io.IOException;
import java.util.List;

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
public class Controller {
        
    public final ReplayService replayService;
    
    public Controller(ReplayService replayService){
        this.replayService = replayService;
    }
    
    @PostMapping("/parse")
    public JsonNode parse(@RequestBody String fileUrl) throws Exception {
        return replayService.parseReplay(fileUrl);
    }

    @PostMapping("/parseFromFiles")
    public String multiParse(@RequestBody List<MultipartFile> files) throws IOException, Exception {
        for(MultipartFile file : files) {
            JsonNode jsonNode = replayService.parseReplayData(file.getInputStream());
            replayService.sendData(jsonNode);
        }
        return "성공";
    }
}
