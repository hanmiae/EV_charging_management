package com.example.ev_charging_system1.scheduler;

import com.example.ev_charging_system1.service.QueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QueueScheduler {

    private final QueueService queueService;

    @Scheduled(fixedRate = 60000) // 1분마다 실행
    public void run() {
        queueService.removeExpiredQueue();
    }
}