package com.example.ev_charging_system1.service;

import com.example.ev_charging_system1.dto.queue.AdminQueueDTO;
import com.example.ev_charging_system1.dto.queue.QueueRequestDTO;
import com.example.ev_charging_system1.dto.queue.QueueResponseDTO;
import com.example.ev_charging_system1.entity.ChargingQueue;
import com.example.ev_charging_system1.entity.ChargingStation;
import com.example.ev_charging_system1.entity.Vehicle;
import com.example.ev_charging_system1.repository.ChargingQueueRepository;
import com.example.ev_charging_system1.repository.ChargingStationRepository;
import com.example.ev_charging_system1.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.ev_charging_system1.repository.ChargingHistoryRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.time.LocalDateTime;
import java.util.List;
import com.example.ev_charging_system1.entity.ChargingHistory;

@Service
@RequiredArgsConstructor // 이게 있어야 historyRepository가 자동으로 연결됩니다.
public class QueueService {
    private final ChargingQueueRepository queueRepository;
    private final VehicleRepository vehicleRepository;
    private final ChargingStationRepository chargingStationRepository;
    private final ChargingHistoryRepository historyRepository;
    // ✅ 대기 등록
    // ✅ 대기 등록 (이 코드로 통째로 바꾸세요)
    @Transactional
    public void registerQueue(QueueRequestDTO dto) {

        // 1. 로그인한 사용자(userPk)로 차량 찾기
        Vehicle vehicle = vehicleRepository.findByUserPk(dto.getUserPk())
                .orElseThrow(() -> new RuntimeException("등록된 차량이 없습니다."));

        // 2. 이미 대기 중인지 체크
        boolean exists = queueRepository
                .existsByVehicleIdAndStatus(vehicle.getVehiclePk(), "waiting");

        if (exists) {
            throw new RuntimeException("이미 대기열에 있음");
        }

        // 3. 사용자가 선택한 충전기(A-01 등) 찾기
        // 🔥 여기서 station을 먼저 찾아야 아래에서 사용할 수 있습니다!
        ChargingStation station = chargingStationRepository.findByStationNumber(dto.getChargerId())
                .orElseThrow(() -> new RuntimeException("해당 충전기를 찾을 수 없습니다: " + dto.getChargerId()));

        // 4. 대기열 저장
        ChargingQueue queue = new ChargingQueue();
        queue.setVehicleId(vehicle.getVehiclePk());
        queue.setStationId(station.getStationPk());
        queue.setRequestTime(LocalDateTime.now());
        queue.setStatus("waiting");
        queueRepository.save(queue);

        // ✅ 5. [실시간 반영의 핵심] 세션 기록(History)에도 즉시 저장!
        // station을 위에서 찾았기 때문에 이제 빨간색이 뜨지 않습니다.
        ChargingHistory history = new ChargingHistory();
        history.setUserPk(dto.getUserPk());
        history.setStationNumber(station.getStationNumber());
        history.setStartTime(LocalDateTime.now());
        history.setStatus("대기중");

// ✅ 아래 두 줄을 반드시 넣어주세요 (DB NOT NULL 에러 방지)
        history.setEnergyUsed(0.0);
        history.setCost(0);

        historyRepository.save(history);
    }

    // ✅ 대기 목록 조회 (프론트용 DTO 반환)
    public List<QueueResponseDTO> getWaitingList() {

        List<ChargingQueue> list = queueRepository.findByStatusOrderByRequestTimeAsc("waiting");

        System.out.println("🔥 waiting 개수: " + list.size());

        return list.stream().map(queue -> {

            ChargingStation station = chargingStationRepository
                    .findById(queue.getStationId())
                    .orElseThrow(() -> new RuntimeException("충전기 정보 없음"));

            Vehicle vehicle = vehicleRepository
                    .findById(queue.getVehicleId())
                    .orElseThrow(() -> new RuntimeException("차량 정보 없음"));

            return new QueueResponseDTO(
                    queue.getQueueId(),
                    station.getStationNumber(),   // A-01
                    vehicle.getPlateNumber(),     // 차량번호
                    queue.getRequestTime(),
                    queue.getStatus()
            );

        }).toList();
    }

    // ✅ 10분 지난 데이터 삭제
    @Transactional
    public void removeExpiredQueue() {
        LocalDateTime time = LocalDateTime.now().minusMinutes(10);
        queueRepository.deleteExpiredQueue(time);
    }

    @Transactional
    public void cancelQueue(QueueRequestDTO dto) {

        // 1. 사용자 차량 찾기
        Vehicle vehicle = vehicleRepository.findByUserPk(dto.getUserPk())
                .orElseThrow(() -> new RuntimeException("차량 없음"));

        // 2. waiting 상태인 최신 대기열 찾기
        ChargingQueue queue = queueRepository
                .findTopByVehicleIdAndStatusOrderByRequestTimeDesc(
                        vehicle.getVehiclePk(), "waiting"
                )
                .orElseThrow(() -> new RuntimeException("취소할 대기열 없음"));

        // 3. 상태 변경
        queue.setStatus("cancelled");

        queueRepository.save(queue);
    }

    public boolean isMyQueue(Long userPk) {

        Vehicle vehicle = vehicleRepository.findByUserPk(userPk)
                .orElseThrow(() -> new RuntimeException("차량 없음"));

        return queueRepository.existsByVehicleIdAndStatus(
                vehicle.getVehiclePk(), "waiting"
        );
    }

    public List<AdminQueueDTO> getWaitingQueueForAdmin() {

        List<ChargingQueue> list = queueRepository.findByStatusOrderByRequestTimeAsc("waiting");

        List<AdminQueueDTO> result = new ArrayList<>();
        Map<String, Integer> chargerRankMap = new HashMap<>();

        for (ChargingQueue queue : list) {

            ChargingStation station = chargingStationRepository
                    .findById(queue.getStationId())
                    .orElseThrow(() -> new RuntimeException("충전기 정보 없음"));

            Vehicle vehicle = vehicleRepository
                    .findById(queue.getVehicleId())
                    .orElseThrow(() -> new RuntimeException("차량 정보 없음"));

            String chargerId = station.getStationNumber(); // A-01

            int rank = chargerRankMap.getOrDefault(chargerId, 0) + 1;
            chargerRankMap.put(chargerId, rank);

            int estimatedMinutes = (rank - 1) * 10;

            result.add(new AdminQueueDTO(
                    chargerId,
                    rank,
                    vehicle.getPlateNumber(),
                    estimatedMinutes
            ));
        }

        return result;
    }

}