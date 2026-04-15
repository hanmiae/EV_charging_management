package com.example.ev_charging_system1.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.File;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class PythonService {

    @Value("${ev.python.exe}")
    private String pythonExe;

    @Value("${ev.python.script-dir}")
    private String scriptDir;

    public void runIdentification() {
        List<String> scriptPaths = Arrays.asList(
                "Platenumber_A01.py",
                "Platenumber_A02.py",
                "Platenumber_B01.py",
                "Platenumber_B02.py"
        );

        File workDir = new File(scriptDir).getAbsoluteFile();

        for (String fileName : scriptPaths) {
            try {
                ProcessBuilder pb = new ProcessBuilder(pythonExe, fileName);
                pb.directory(workDir);
                pb.inheritIO();
                pb.start();
                log.info("🚀 파이썬 엔진 가동 중: {} (dir={})", fileName, workDir);
            } catch (Exception e) {
                log.error("❌ 실행 실패 ({}): {}", fileName, e.getMessage());
            }
        }
    }
}
