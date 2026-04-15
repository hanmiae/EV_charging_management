<template>
  <div class="dashboard-wrapper">
    <div class="bg-overlay"></div>

    <nav class="top-nav">
      <div class="header-main-group">
        <h1 class="header-title">EV Charging Zone Monitoring</h1>
        <div class="header-meta">
          <span class="datetime white-important">{{ currentTime }}</span>
          <div class="noti-box" @click.stop="toggleNoti">
            <span class="bell">🔔</span>
            <span class="badge" v-if="displayBadgeCount > 0">{{ displayBadgeCount }}</span>
            <Transition name="fade">
              <div v-if="showNoti" class="noti-popup-horizontal glass-effect">
                <div class="noti-header-mini">실시간 알림</div>
                <div v-if="notifications.length === 0" class="noti-item-row">새로운 알림이 없습니다.</div>
                <div v-for="(noti, idx) in notifications" :key="idx" class="noti-item-row">{{ noti }}</div>
              </div>
            </Transition>
          </div>
        </div>
      </div>
      <div class="header-right-group">
        <button class="nav-pill white-important" @click="router.push('/mypage')">마이페이지</button>
        <button class="nav-pill logout white-important" @click="handleLogout">로그아웃</button>
      </div>
    </nav>

    <div class="main-content">
      <aside class="panel left-panel">
        <div class="glass-card decorated-info-card">
          <div class="profile-content">
            <div class="profile-details">
              <div class="p-row flex-between">
                <span class="p-value white-important p-name">
                  {{ userProfile.name || '불러오는 중...' }}
                  <small class="tag">{{ userProfile.role === 'ADMIN' ? '관리자' : '입주민' }}</small>
                </span>
                <button class="btn-visit-reg" @click="openVisitReg">방문차량등록</button>
              </div>
              <div class="p-row">
                <span class="p-value green-text p-car-id">{{ userProfile.plateNumber || '차량 정보 없음' }}</span>
              </div>
            </div>
          </div>
        </div>

        <div class="glass-card">
          <h3 class="card-label white-important">현재 충전 상태</h3>
          <div class="charge-flex">
            <div class="ring-gauge white-important">{{ chargePercent }}%</div>
            <div class="charge-stats">
              <p class="kw white-important kw-size">0 kW</p>
              <p class="min white-important min-size">{{ chargePercent >= 100 ? '충전 완료' : '예상 15분 남음' }}</p>
            </div>
          </div>
        </div>

        <div class="glass-card chart-card">
          <h3 class="card-label white-important">월별 충전량 비교</h3>
          <div class="bar-chart-12">
            <div v-for="(val, i) in monthlyData" :key="i" class="bar-unit">
              <div class="bar-pillar" :style="{ height: (val / 220) * 100 + '%' }" :class="{ active: i === 2 }">
                <span class="bar-pop white-important">{{ val }}</span>
              </div>
              <span class="bar-month white-important">{{ i + 1 }}월</span>
            </div>
          </div>
        </div>

        <div class="glass-card usage-fee-card">
          <h3 class="card-label white-important">ENERGY USAGE REPORT</h3>
          <div class="fee-content">
            <div class="fee-row">
              <span class="f-label white-important">이번 달 사용량</span>
              <span class="f-value white-important f-val-size">145.2 kWh</span>
            </div>
            <div class="f-divider"></div>
            <div class="fee-row">
              <span class="f-label white-important">예상 요금</span>
              <span class="f-value gold-text f-val-size f-bold">24,500 원</span>
            </div>
          </div>
        </div>
      </aside>

      <section class="center-spacer"></section>

      <aside class="panel right-panel">
        <div class="glass-card charger-section">
          <div class="card-header-between">
            <h3 class="card-label white-important">충전기 실시간 상태</h3>
            <div class="mini-tabs">
              <button :class="{ active: chargerTab === 'A' }" @click="chargerTab = 'A'; selectedCharger = 'A-01'">A동</button>
              <button :class="{ active: chargerTab === 'B' }" @click="chargerTab = 'B'; selectedCharger = 'B-01'">B동</button>
            </div>
          </div>
          <div class="charger-status-container">
            <div v-for="s in filteredStations" :key="s.id" 
                 class="charger-status-card" :class="[s.status, { 'selected-border': selectedCharger === s.id }]"
                 @click="selectedCharger = s.id">
              <div class="charger-icon">⚡</div>
              <div class="charger-details">
                <div class="charger-id white-important">{{ s.id }}</div>
                <div class="charger-type">{{ s.b === 'A' ? '급속' : '완속' }}</div>
              </div>
              <div class="charger-status-badge">{{ s.statusText }}</div>
            </div>
          </div>
        </div>

        <div class="glass-card queue-section">
          <div class="card-header-between">
            <h3 class="card-label white-important">전체 대기열 정보 (QUEUE)</h3>
          </div>
          <div class="queue-container">
            <div class="queue-mini-list">
               <div v-if="allQueueData.length === 0" class="q-row" style="justify-content: center; padding: 10px;">
                 <span class="white-important">대기 중인 차량이 없습니다.</span>
               </div>
               <div v-for="q in allQueueData" :key="q.rank" class="q-row">
                 <span class="q-idx">{{ q.rank }}위</span> 
                 <span class="q-target-badge">{{ q.chargerId }}</span> 
                 <span class="white-important">{{ q.status }}</span> 
                 <span class="q-timer">{{ q.time }}</span>
               </div>
            </div>
            <p v-if="!isQueued" style="text-align:right; font-size:0.8rem; margin-bottom:5px; color:#22c55e;">
              선택됨: {{ selectedCharger }}
            </p>
            <button class="action-btn" :class="isQueued ? 'cancel' : 'go'" @click="isQueued ? cancelQueue() : joinQueue()">
              {{ isQueued ? '대기 등록 취소' : `${selectedCharger} 대기등록` }}
            </button>
          </div>
        </div>

        <div class="glass-card history-fixed-3">
          <h3 class="card-label white-important">나의 충전 세션 기록</h3>
          <div class="scroll-area-3">
            <table class="white-important">
              <thead>
                <tr>
                  <th class="table-header-bg">충전소</th>
                  <th class="table-header-bg">날짜</th>
                  <th class="table-header-bg">완료</th>
                </tr>
              </thead>
              <tbody>
                <tr v-if="historyData.length === 0">
                  <td colspan="3" style="padding: 20px; opacity: 0.5;">기록이 없습니다.</td>
                </tr>
                <tr v-for="(h, idx) in historyData" :key="idx">
                  <td>{{ h.stationNumber }}</td>
                  <td>{{ h.formattedTime }}</td>
                  <td class="bold-green">{{ h.status }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </aside>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed, watch } from 'vue';
import { useRouter } from 'vue-router';
import axios from 'axios';

const router = useRouter();
const currentTime = ref('');
const isQueued = ref(false);
const loginUserPk = ref(null);
const showNoti = ref(false);
const notifications = ref([]);
const displayBadgeCount = ref(0);

const chargePercent = ref(85); 
const notifiedFull = ref(false);
const chargerTab = ref('A'); 
const selectedCharger = ref('A-01');

const userProfile = ref({ name: '', plateNumber: '', role: '' });
const queueData = ref([]);
const historyData = ref([]); 

const stations = [
  { id: 'A-01', b: 'A', status: 'busy', statusText: '사용 중' },
  { id: 'A-02', b: 'A', status: 'free', statusText: '여유' },
  { id: 'B-01', b: 'B', status: 'free', statusText: '여유' },
  { id: 'B-02', b: 'B', status: 'busy', statusText: '사용 중' }, 
];

const filteredStations = computed(() => stations.filter(s => s.b === chargerTab.value));

const allQueueData = computed(() => {
  if (!queueData.value || queueData.value.length === 0) return [];
  return queueData.value.map((item, index) => ({
    rank: index + 1,
    chargerId: item.stationNumber || '번호 없음', 
    status: item.status === 'waiting' ? '대기 중' : '충전 중',
    time: '0분'
  }));
});

const toggleNoti = () => {
  showNoti.value = !showNoti.value;
  if (showNoti.value) displayBadgeCount.value = 0;
};

watch(chargePercent, (newVal) => {
  if (newVal >= 100 && !notifiedFull.value) {
    notifications.value.unshift(`✅ 충전이 완료되었습니다! (100%)`);
    displayBadgeCount.value++;
    notifiedFull.value = true;
    fetchHistory();
  }
});

const fetchUserProfile = async () => {
  if (!loginUserPk.value) return;
  try {
    const res = await axios.get('http://localhost:8080/api/users/profile', { params: { userPk: loginUserPk.value } });
    userProfile.value = res.data;
  } catch (err) { console.error('프로필 로드 실패'); }
};

const fetchHistory = async () => {
  if (!loginUserPk.value) return;
  try {
    const res = await axios.get('http://localhost:8080/api/history/my', { params: { userPk: loginUserPk.value } });
    historyData.value = res.data.map(item => ({
      stationNumber: item.stationNumber,
      formattedTime: item.startTime.substring(5, 16).replace('T', ' '),
      status: item.status || '완료'
    }));
  } catch (err) { console.error('기록 로드 실패'); }
};

const fetchQueue = async () => {
  try {
    const res = await axios.get('http://localhost:8080/api/queue/waiting');
    queueData.value = res.data;
  } catch (err) { console.error(err); }
};

const checkMyQueue = async () => {
  if (!loginUserPk.value) return;
  try {
    const res = await axios.get('http://localhost:8080/api/queue/my', { params: { userPk: loginUserPk.value } });
    isQueued.value = res.data;
  } catch (err) { console.error(err); }
};

const refreshAllData = () => {
  fetchQueue();
  checkMyQueue();
  fetchHistory();
  fetchUserProfile();
};

const joinQueue = async () => {
  try {
    if (!loginUserPk.value) return;
    await axios.post('http://localhost:8080/api/queue/join', {
      userPk: Number(loginUserPk.value),
      chargerId: selectedCharger.value 
    });
    notifications.value.unshift(`🔔 ${selectedCharger.value} 대기 등록 완료!`);
    displayBadgeCount.value++;
    isQueued.value = true;
    setTimeout(refreshAllData, 500); 
  } catch (err) { alert('등록 실패'); }
};

const cancelQueue = async () => {
  try {
    await axios.post('http://localhost:8080/api/queue/cancel', { 
      userPk: Number(loginUserPk.value),
      chargerId: selectedCharger.value 
    });
    notifications.value.unshift(`❌ 대기열 취소 완료`);
    displayBadgeCount.value++;
    isQueued.value = false;
    refreshAllData();
  } catch (err) { console.error(err); }
};

const handleLogout = () => {
  if (confirm('로그아웃 하시겠습니까?')) {
    localStorage.removeItem('userPk');
    loginUserPk.value = null;
    router.push('/');
  }
};

const openVisitReg = () => {
  alert('마이페이지로 이동하거나 팝업을 띄웁니다.');
};

let pollingTimer = null;

onMounted(() => {
  currentTime.value = new Date().toLocaleString('ko-KR');
  loginUserPk.value = localStorage.getItem('userPk'); 
  refreshAllData();
  
  setInterval(() => { currentTime.value = new Date().toLocaleString('ko-KR'); }, 1000);
  pollingTimer = setInterval(refreshAllData, 5000);
  setTimeout(() => { if (chargePercent.value < 100) chargePercent.value = 100; }, 3000);
});

onUnmounted(() => { if (pollingTimer) clearInterval(pollingTimer); });

const monthlyData = [120, 185, 145, 90, 60, 110, 130, 80, 150, 170, 100, 140];
</script>

<style scoped>
.dashboard-wrapper { position: relative; width: 100%; height: 100vh; color: white; overflow: hidden; font-family: 'Pretendard', sans-serif; }
.bg-overlay { position: absolute; inset: 0; background: linear-gradient(rgba(0,0,0,0.5), rgba(0,0,0,0.7)), url('https://images.unsplash.com/photo-1593941707882-a5bba14938c7?q=80&w=2072'); background-size: cover; background-position: center; z-index: -1; }
.white-important { color: #ffffff !important; }
.top-nav { background-color: #0d2b1f; height: 60px; display: flex; justify-content: space-between; align-items: center; padding: 0 40px; border-bottom: 1px solid rgba(255,255,255,0.1); }
.header-main-group { display: flex; align-items: center; gap: 20px; }
.header-title { margin: 0; font-size: 1.8em !important; font-weight: 900; color:#ffffff;}
.header-meta { display: flex; align-items: center; gap: 15px; font-size: 0.95rem; }
.noti-box { position: relative; cursor: pointer; background: rgba(255,255,255,0.1); width: 34px; height: 34px; border-radius: 50%; display: flex; align-items: center; justify-content: center; }
.badge { position: absolute; top: -3px; right: -3px; background: #ef4444; font-size: 0.6rem; padding: 2px 5px; border-radius: 10px; font-weight: 900; }

/* ✅ 네비게이션 버튼 공통 스타일 (로그아웃 & 마이페이지) */
.header-right-group { display: flex; gap: 10px; }
.nav-pill { background: transparent; border: 1.5px solid rgba(255, 255, 255, 0.6); color: #ffffff; padding: 5px 18px; border-radius: 50px; cursor: pointer; font-size: 0.85rem; font-weight: 700; transition: all 0.2s ease; }
.nav-pill:hover { background-color: rgba(255, 255, 255, 0.1); border-color: #ffffff; }
.nav-pill.logout:hover { background-color: #ffffff; color: #0d2b1f !important; }

.noti-popup-horizontal { position: absolute; top: 48px; left: -280px; width: 320px; background: rgba(13, 43, 31, 0.95); backdrop-filter: blur(20px); border: 1px solid rgba(255, 255, 255, 0.2); border-radius: 15px; padding: 15px; z-index: 1000; }
.noti-header-mini { font-size: 0.9rem; font-weight: 800; color: #22c55e; margin-bottom: 10px; border-bottom: 1px solid rgba(255,255,255,0.1); padding-bottom: 5px; }
.noti-item-row { padding: 8px 0; border-bottom: 1px solid rgba(255,255,255,0.05); }
.main-content { display: flex; padding: 20px 40px; height: calc(100vh - 80px); gap: 25px; }
.panel { display: flex; flex-direction: column; gap: 10px; }
.left-panel { flex: 0.5; } .right-panel { flex: 0.5; } .center-spacer { flex: 0.8; } 
.glass-card { background: rgba(255, 255, 255, 0.08); backdrop-filter: blur(20px); border: 1px solid rgba(255, 255, 255, 0.15); border-radius: 20px; padding: 20px 25px 25px 25px; }
.card-label { font-size: 1.2rem; font-weight: 800; margin-bottom: 12px; display: block; text-transform: uppercase; margin-top: 5px; }
.bar-chart-12 { display: flex; align-items: flex-end; justify-content: space-between; height: 120px; padding-top: 35px; }
.bar-unit { flex: 1; display: flex; flex-direction: column; align-items: center; gap: 6px; }
.bar-pillar { width: 40px; background: rgba(255, 255, 255, 0.7); border-radius: 8px; position: relative; }
.bar-pop { position: absolute; top: -18px; left: 50%; transform: translateX(-50%); font-size: 0.75rem; font-weight: 800; }
.charger-status-container { display: flex; flex-direction: column; gap: 10px; }
.charger-status-card { background: rgba(0,0,0,0.2); border-radius: 12px; padding: 16px 20px; display: flex; align-items: center; gap: 16px; border: 1px solid rgba(255,255,255,0.05); cursor: pointer; transition: all 0.2s; }
.charger-id { font-weight: 800; font-size: 1.2rem; }
.charger-status-badge { font-size: 1rem; padding: 4px 14px; border-radius: 10px; margin-left: auto; }
.charger-details { display: flex; align-items: center; gap: 20px; }
.queue-mini-list { background: rgba(0,0,0,0.15); border-radius: 15px; padding: 18px; margin-bottom: 10px; min-height: 80px; }
.q-row { display: flex; justify-content: space-between; font-size: 1rem; margin-bottom: 6px; }
.action-btn { width: 100%; border: none; padding: 15px; border-radius: 12px; font-weight: 800; cursor: pointer; background: #22c55e; color: #fff; font-size: 1.1rem; }
.action-btn.cancel { background: #ef4444; }
.history-fixed-3 { height: 155px; display: flex; flex-direction: column; overflow: hidden; }
.scroll-area-3 { flex: 1; overflow-y: auto; }
table { width: 100%; border-collapse: collapse; font-size: 1rem; }
td { padding: 12px 0; border-bottom: 1px solid rgba(255,255,255,0.05); text-align: center; }
.ring-gauge { width: 80px; height: 80px; border-radius: 50%; border: 6px solid #22c55e; display: flex; align-items: center; justify-content: center; font-weight: 900; font-size: 1.3rem; }
.charge-flex { display: flex; align-items: center; gap: 30px; }
.p-name { font-size: 1.2rem; margin-bottom: 6px; display: inline-block; }
.tag { background: #22c55e; color: #fff; padding: 3px 10px; border-radius: 4px; font-size: 0.8rem; margin-left: 5px; }

/* ✅ 방문차량등록 버튼 스타일 */
.flex-between { display: flex; justify-content: space-between; align-items: center; width: 100%; }
.btn-visit-reg { background: transparent; border: 1px solid rgba(255, 255, 255, 0.4); color: rgba(255, 255, 255, 0.8); padding: 4px 12px; border-radius: 6px; font-size: 0.75rem; cursor: pointer; transition: all 0.2s; }
.btn-visit-reg:hover { background: rgba(255, 255, 255, 0.1); border-color: #ffffff; color: #ffffff; }

.mini-tabs button { background: rgba(255,255,255,0.1); border: none; color: #fff; padding: 6px 18px; border-radius: 8px; cursor: pointer; font-size: 1.05rem; margin-left: 8px; }
.mini-tabs button.active { background: #22c55e; }
.bold-green { color: #22c55e; font-weight: 800; }
.selected-border { border: 2px solid #ffffff !important; background: rgba(214, 241, 224, 0.1) !important; }

.fee-content { margin-top: 10px; }
.fee-row { display: flex; justify-content: space-between; align-items: center; padding: 8px 0; }
.f-divider { height: 1px; background: rgba(255, 255, 255, 0.15); width: 100%; margin: 2px 0; }
.gold-text { color: #facc15 !important; font-weight: 800; }
.f-val-size { font-size: 1.1rem; }
</style>