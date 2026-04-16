package com.example.ev_charging_system1.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.io.File;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class PythonService {

    public void runIdentification() {
        // [수정 1] 본인의 실제 파이썬 설치 경로(절대경로)를 쓰는 것이 가장 안전합니다.
        String pythonExe = "C:/Users/mbc/AppData/Local/Programs/Python/Python310/python.exe";

        List<String> scriptPaths = Arrays.asList(
                "Platenumber_A01.py",
                "Platenumber_A02.py",
                "Platenumber_B01.py",
                "Platenumber_B02.py"
        );

        String scriptDir = "C:\\Vue\\EV_Project14\\EV_Project14\\Springboot\\ev_charging_system1\\src\\main\\resources\\python";

        for (String fileName : scriptPaths) {
            try {
                ProcessBuilder pb = new ProcessBuilder(pythonExe, fileName);
                // [수정 2] 파이썬 파일이 있는 폴더를 '작업 디렉토리'로 설정 (경로 꼬임 방지)
                pb.directory(new File(scriptDir));
                pb.inheritIO();
                pb.start();
                log.info("🚀 파이썬 엔진 가동 중: " + fileName);
            } catch (Exception e) {
                log.error("❌ 실행 실패 (" + fileName + "): " + e.getMessage());
            }
        }
    }
}