<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue'
import axios from 'axios'
import { useRouter } from 'vue-router'

// ⏰ 실시간 날짜 및 시간 로직
const currentDate = ref('');
const currentTime = ref('');
const router = useRouter()
const updateDateTime = () => {
  const now = new Date();
  currentDate.value = `${now.getFullYear()}년 ${now.getMonth() + 1}월 ${now.getDate()}일`;
  currentTime.value = now.toLocaleTimeString('ko-KR', { 
    hour12: false, 
    hour: '2-digit', 
    minute: '2-digit', 
    second: '2-digit' 
  });
};

const viewMode = ref('dashboard');
let timer;
let waitingTimer;

onMounted(() => {
  console.log("🔥 실행됨")

  updateDateTime();
  timer = setInterval(updateDateTime, 1000);

  // 기존 데이터
  fetchData(selectedBuilding.value);

  // 대기열
  fetchWaitingList();

  waitingTimer = setInterval(() => {
    fetchWaitingList();
  }, 5000);
});

onUnmounted(() => {
  clearInterval(timer);
  clearInterval(waitingTimer);
});

// 📊 상태 및 데이터 관리
const selectedBuilding = ref('A동');
const isWaitingListOpen = ref(false);

const toggleWaitingList = () => {
  isWaitingListOpen.value = !isWaitingListOpen.value;
};

const chargingStatusList = ref([]);
const alertLogs = ref([]);
const waitingList = ref([]);

const goToVideoBoard = () => {
  router.push('/video-board'); 
};

/**
 * ⭐ [수정] 경고 라벨 판별 함수
 * 백엔드에서 보내주는 warningMsg를 최우선으로 사용하도록 변경
 */
const getWarningLabel = (item) => {
  if (item.warning && item.warningMsg) return item.warningMsg; // 백엔드 위반 메시지 우선
  if (item.chargeTime > 60) return '장기간 주차';
  return null;
};

const fetchData = async (building) => {
  try {
    console.log("🔥 API 호출됨", building)

    const res = await axios.get("http://localhost:8080/api/detection/list", {
      params: { building }
    });
    
    console.log("🔥 응답:", res.data);

    chargingStatusList.value = res.data.chargingStatusList;
    
    // 🔔 [실시간 알림 로직] 백엔드에서 온 warningMsg를 기반으로 알림 생성
    const newAlerts = chargingStatusList.value
      .filter(item => getWarningLabel(item) !== null)
      .map(item => ({
        id: Date.now() + item.station,
        time: currentTime.value,
        msg: `[${item.station}] ${getWarningLabel(item)} 감지!`,
        type: item.status === '비정상' ? 'danger' : 'warning'
      }));

    // 기존 백엔드 알림 로그와 새로 감지된 알림을 합침 (최신순 10개 유지)
    alertLogs.value = [...newAlerts, ...(res.data.alertLogs || [])].slice(0, 10);

  } catch (err) {
    console.error("데이터 불러오기 실패", err);
  }
};

watch(selectedBuilding, (newBuilding) => fetchData(newBuilding));

const fetchWaitingList = async () => {
  console.log("🔥 관리자 대기열 호출됨")

  try {
    const res = await axios.get('http://localhost:8080/api/admin/queue/waiting')
    console.log("🔥 관리자 queue:", res.data)

    waitingList.value = res.data.map(item => ({
      station: item.chargerId,          // A-01
      rank: item.rank,                  // 순번
      plate: item.carNumber,            // 차량번호
      time: item.estimatedMinutes + '분' // 예상시간
    }));

  } catch (err) {
    console.error('관리자 대기열 불러오기 실패', err);
  }
};
</script>

<template>
  <header class="top-header">
    <div class="left-section">
      <h1 class="title">EV Charging Zone Monitoring</h1>
      <div class="header-info-inline">
        <span class="date-text">{{ currentDate }}</span>
        <span class="live-time">{{ currentTime }}</span>
        <span class="divider">|</span>
        <select v-model="selectedBuilding" class="building-select">
          <option value="A동">A동</option>
          <option value="B동">B동</option>
        </select>
      </div>
    </div>

    <div class="right-section">
      <button @click="$router.push('/EvDatabaseUsage')" class="nav-btn-blue">
        📊 DB 사용량
      </button>
      
      <button @click="$router.push('/EVVideoBoard')" class="nav-btn-green">
        <span class="live-dot"></span> 영상 대시보드
      </button>
    </div>
  </header>

  <main class="dashboard-container">
    <section class="camera-section">
      <div class="camera-contents">
        <div class="camera-group" v-for="i in 2" :key="i">
          <span class="zone-label big-label">
            {{ selectedBuilding === 'A동' ? 'A' : 'B' }}-0{{i}}
          </span>
          <div class="parking-img-box">
            <img 
              v-if="chargingStatusList[i-1]?.imageUrl"
              :src="`http://localhost:8080/images/${chargingStatusList[i-1].imageUrl.replace('/images/', '')}`"
              class="parking-img"
            />
            <div v-else class="placeholder-img">Parking View</div>
          </div>

          <div class="plate-img-box">
            <img 
              v-if="chargingStatusList[i-1]?.plateImageUrl"
              :src="`http://localhost:8080/images/${chargingStatusList[i-1].plateImageUrl.replace('/images/', '')}`"
              class="plate-img"
            />
            <div v-else class="placeholder-img small">Plate View</div>
          </div>
        </div>
      </div>
    </section>

    <section class="status-section">
      <div class="status-split-container">
        <div class="table-area">
          <h2 class="section-title">충전 구역 현황</h2>
          <div class="content-card">
            <table class="main-status-table">
              <thead>
                <tr>
                  <th>충전소</th><th>차량번호</th><th>EV여부</th><th>입차시간</th><th>상태</th><th>경과시간</th><th>경고</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="item in chargingStatusList" :key="item.station">
                  <td>{{ item.station }}</td>
                  <td>{{ item.plate }}</td>
                  <td><span :class="['ev-text', item.isEV === 'Yes' ? 'ev-yes' : 'ev-no']">{{ item.isEV }}</span></td>
                  <td>{{ item.entryTime }}</td>
                  <td :class="{'ev-no': item.status === '비정상'}">{{ item.status }}</td>
                  <td>{{ item.chargeTime > 0 ? item.chargeTime + '분' : '-' }}</td>
                  <td>
                    <span v-if="getWarningLabel(item)" class="pill-warning">{{ getWarningLabel(item) }}</span>
                    <span v-else class="normal-dash">-</span>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <div class="alert-area">
          <h2 class="section-title">실시간 알림</h2>
          <div class="content-card alert-scroll">
            <div v-for="log in alertLogs" :key="log.id" :class="['alert-item', log.type]">
              <span class="log-time">{{ log.time }}</span>
              <span class="log-msg">{{ log.msg }}</span>
            </div>
          </div>
        </div>
      </div>
    </section>
  </main>

  <button @click="toggleWaitingList" class="waiting-btn-yellow">실시간 대기열 현황 확인</button>

  <div v-if="isWaitingListOpen" class="waiting-popup">
    <div class="popup-header">
      <span class="popup-title">실시간 대기 명단</span>
      <button @click="toggleWaitingList" class="close-btn">×</button>
    </div>
    <div class="popup-body">
      <table class="waiting-table">
        <thead>
          <tr>
            <th>충전소</th>
            <th>순번</th>
            <th>차량 번호</th>
            <th>예상 시간</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in waitingList" :key="item.rank">
            <td>{{ item.station }}</td>
            <td>{{ item.rank }}</td>
            <td>{{ item.plate }}</td>
            <td>
              <span class="time-highlight">{{ item.time }}</span>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<style>
html, body { margin: 0; padding: 0; width: 100%; height: 100%; overflow: hidden; }
body { background-color: #f0f9f4; color: #1a2e25; font-family: 'Pretendard', sans-serif; }
</style>

<style scoped>
.top-header {
  position: fixed; top: 0; left: 0; width: 100%; height: 65px;
  background-color: #0d2b1f; color: #fff; display: flex; 
  justify-content: space-between; align-items: center; padding: 0 30px; z-index: 1000; box-sizing: border-box;
}
.left-section { display: flex; align-items: center; gap: 15px; }
.right-section { display: flex; align-items: center; gap: 10px; }

.nav-btn-blue {
  background-color: rgba(255, 255, 255, 0.05); color: #ffffff;
  border: 1px solid rgba(255, 255, 255, 0.2); padding: 6px 14px;
  border-radius: 6px; font-size: 16px; font-weight: 600; cursor: pointer;
  transition: all 0.2s ease; display: flex; align-items: center; gap: 8px;
}
.nav-btn-blue:hover { background-color: rgba(255, 255, 255, 0.1); border-color: #ffffff; }

.nav-btn-green {
  background-color: rgba(255, 255, 255, 0.05); color: #ffffff;
  border: 1px solid rgba(255, 255, 255, 0.2); padding: 6px 14px;
  border-radius: 6px; font-size: 16px; font-weight: 600; cursor: pointer;
  transition: all 0.2s ease; display: flex; align-items: center; gap: 8px;
}
.nav-btn-green:hover { background-color: rgba(255, 255, 255, 0.1); border-color: #ffffff; }

.live-dot { width: 8px; height: 8px; background-color: #ac0505; border-radius: 50%; animation: blink 1.2s infinite; }
@keyframes blink { 0% { opacity: 1; } 50% { opacity: 0.3; } 100% { opacity: 1; } }

.title { margin: 0; font-size: 1.7em !important; font-weight: 900; }
.header-info-inline { background: rgba(255,255,255,0.1); padding: 5px 15px; border-radius: 8px; display: flex; align-items: center; }
.live-time { margin-left: 12px; font-weight: 600; color: #4ade80; font-family: 'monospace'; }
.divider { color: #4ade80; margin: 0 10px; }
.building-select { background: transparent; color: #fff; border: none; font-weight: bold; cursor: pointer; outline: none; }
.building-select option { background-color: #0d2b1f; color: #fff; }

.dashboard-container { 
  margin-top: 70px; height: calc(100vh - 65px); 
  padding: 10px 35px 20px 35px; display: flex; flex-direction: column; gap: 5px; box-sizing: border-box; 
}

.camera-section { flex: 6.5; display: flex; justify-content: center; width: 100%; } 
.camera-contents { display: flex; gap: 70px; width: 100%; max-width: 1300px;}
.camera-group { flex: 1; display: flex; flex-direction: column; gap: 12px; min-width: 0; }
.big-label { font-size: 1.5em !important; color: #0d2b1f; font-weight: 900; text-align: center; margin-bottom: 5px; }

.parking-img-box { flex: none !important; height: 260px !important; overflow: hidden; background: #0d2b1f; border-radius: 12px; }
.plate-img-box { flex: none !important; height: 120px !important; overflow: hidden; background: #0d2b1f; border-radius: 12px; }
.parking-img, .plate-img { width: 100%; height: 100%; object-fit: contain; border-radius: 12px; background-color: black; }

.placeholder-img { color: rgba(255,255,255,0.8); display: flex; justify-content: center; align-items: center; height: 100%; font-weight: bold; font-size: 1.1em; }

.status-section { flex: 4; display: flex; width: 100%; justify-content: center; min-height: 0; padding-bottom: 35px; margin-top: 5px;} 
.status-split-container { display: flex; width: 100%; gap: 25px; max-width: 1300px; }
.table-area { flex: 6.5; display: flex; flex-direction: column; }
.alert-area { flex: 2.5; display: flex; flex-direction: column; }
.section-title { font-size: 1.7em; margin-bottom: 8px; font-weight: 800; color: #0d2b1f; }
.content-card { flex: 1; background: #ffffff; border-radius: 10px; border: 1px solid #d1e2d9; overflow-y: auto; box-shadow: 0 4px 20px rgba(0,0,0,0.05); }

.main-status-table { width: 100%; border-collapse: collapse; text-align: center; font-size: 0.95em; table-layout: fixed;}
.main-status-table th { position: sticky; top: 0; background: #f8faf9; padding: 20px; border-bottom: 2px solid #0f766e; white-space: nowrap; }
.main-status-table td { padding: 20px; border-bottom: 1px solid #eee; }
.ev-yes { color: #0f766e; font-weight: 800; }
.ev-no { color: #c53030; font-weight: 800; }
.status-active { color: #434747; font-weight: 600; }
.pill-warning { background: #fff1f0; color: #ff4d4f; padding: 4px 8px; border-radius: 4px; font-weight: bold; }

/* 🔔 실시간 알림 전용 스타일 (추가됨) */
.alert-scroll { padding: 10px; display: flex; flex-direction: column; gap: 10px; }
.alert-item { padding: 10px; border-radius: 8px; font-size: 0.85em; border-left: 4px solid #cbd5e1; background: #f8fafc; }
.alert-item.danger { background: #fff5f5; color: #c53030; border-left-color: #f56565; }
.alert-item.warning { background: #fffbef; color: #92400e; border-left-color: #f59e0b; }
.log-time { font-size: 0.8em; opacity: 0.7; display: block; margin-bottom: 2px; }
.log-msg { font-weight: bold; }

.waiting-btn-yellow { 
  position: fixed; bottom: 30px; right: 30px; z-index: 1001;
  background: linear-gradient(135deg, #fbc02d, #f9a825); color: #1a2e25;
  padding: 15px 30px; border-radius: 12px; border: none; font-weight: 900; font-size: 1.1em; cursor: pointer;
}

.waiting-popup { 
  position: fixed; bottom: 100px; right: 30px; width: 420px; z-index: 2000;
  background: rgba(255, 255, 255, 0.95); backdrop-filter: blur(15px); border-radius: 30px; 
  box-shadow: 0 20px 45px rgba(0,0,0,0.15); border: 1px solid rgba(255,255,255,0.4); overflow: hidden;
}
.popup-header { background: rgba(245, 158, 11, 0.95) !important; color: #0d2b1f; padding: 12px 25px; display: flex; justify-content: space-between; align-items: center; }
.popup-title { font-size: 1.15em !important; font-weight: 800; }
.close-btn { background: rgba(0,0,0,0.1); border: none; width: 28px; height: 28px; border-radius: 50%; cursor: pointer; font-size: 1.1em; }

.waiting-table { width: 100% !important; border-collapse: collapse !important; table-layout: fixed !important; font-size: 0.82em !important; }
.waiting-table th { color: #64748b; padding-bottom: 10px; border-bottom: 2px solid #0f766e; }
.waiting-table td { text-align: center;padding: 16px 0; border-bottom: 1px solid rgba(0,0,0,0.05); font-weight: 700; color: #1e293b; vertical-align: middle; }
.time-highlight { background: rgba(251, 192, 45, 0.2); color: #d97706; padding: 2px 10px; border-radius: 12px; font-weight: 900; }
</style>