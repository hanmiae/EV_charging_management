<template>
  <div class="dashboard-container">
    <header class="dashboard-header">
      <div class="logo">EV Charging Zone Video Server</div>
      <div class="header-right">
        <button @click="router.push({ name: 'EvChargingZoneMonitoring' })" class="nav-back-btn">
          <span class="icon">📊</span> 모니터링 대시보드
        </button>
      </div>
    </header>

    <main class="dashboard-main">
      <aside class="side-panel left">
        <div class="side-datetime-area">
          <div class="side-date">{{ currentDate }}</div>
          <div class="side-time">{{ currentTimeOnly }}</div>
        </div>
        
        <div class="stat-group">
          <section class="stat-card">
            <span class="stat-label">오늘의 충전 현황</span>
            <div class="stat-value">{{ summary.todayCharge || 0 }}<span class="unit">대</span></div>
          </section>
          <section class="stat-card">
            <span class="stat-label">불법 차량 감지</span>
            <div class="stat-value alert-text-red">{{ summary.illegalCount || 0 }} <span class="unit">건</span></div>
          </section>
          <section class="stat-card">
            <span class="stat-label">대기차량</span>
            <div class="stat-value">{{ summary.waitingCount || 0 }} <span class="unit">대</span></div>
          </section>
        </div>
        
        <div class="spacer"></div> 

        <div class="mini-chart-box">
          <div class="mini-chart-header">
            <h4>시간대별 점유율</h4>
            <span class="live-text">{{ summary.occupancyRate || 0 }}%</span>
          </div>
          <div class="canvas-container"><canvas id="miniChart"></canvas></div>
        </div>
      </aside>

      <section class="main-content">
        <div class="video-grid">
          <div v-for="cctv in cctvList" :key="cctv.station" class="video-item">
            <div class="video-header">
              CCTV {{ cctv.station }} <span class="status-dot"></span>
            </div>
            <img 
              v-if="cctv && cctv.station"
              :src="`http://localhost:${getPythonPort(cctv.station)}/stream`" 
              class="video-img"
              alt="LIVE VIDEO"
            />
            <div v-else class="video-placeholder-text">VIDEO STREAM</div>

            <div class="ai-overlay">
              <div v-if="cctv.plateNumber" class="plate-num">{{ cctv.plateNumber }}</div>
              <div 
                v-if="cctv.status"
                class="status-badge"
                :class="getStatusClass(cctv.status)"
              >
                {{ getStatusText(cctv.status) }}
              </div>
            </div>
          </div>
        </div>

        <div class="graph-section">
          <div class="graph-card">
            <h3 class="graph-title">시간대별 전력 부하(kW)</h3>
            <div class="graph-canvas"><canvas id="mainChart1"></canvas></div>
          </div>
          <div class="graph-card">
            <h3 class="graph-title">충전기 실시간 상태</h3>
            <div class="graph-canvas"><canvas id="mainChart2"></canvas></div>
          </div>
          <div class="graph-card">
            <h3 class="graph-title">주차 구역 위반 현황</h3>
            <div class="graph-canvas"><canvas id="mainChart3"></canvas></div>
          </div>
        </div>
      </section>

      <aside class="right-panel">
        <div class="right-section log-section">
          <div class="section-header">
            <h3>이벤트 로그</h3>
            <div class="live-indicator"><span class="blink-dot"></span>LIVE</div>
          </div>
          <div class="log-container">
            <div class="log-item danger">
              <span class="log-time">13:02:20</span>
              <span class="log-msg">미등록 차량 감지 (386마 1144)</span>
            </div>
            <div class="log-item info">
              <span class="log-time">13:05:01</span>
              <span class="log-msg">63러 2314 충전 시작</span>
            </div>
            <div class="log-item success">
              <span class="log-time">12:50:11</span>
              <span class="log-msg">62서 9811 충전 완료</span>
            </div>
          </div>
        </div>
        
        <div class="right-spacer"></div>

        <div class="right-section machine-section">
          <div class="section-header">
            <h3>충전소 기계 상태</h3>
          </div>
          <div class="machine-container">
            <div v-for="m in machines" :key="m.stationNumber" class="machine-card">
              <div class="card-row">
                <div class="status-indicator">
                  <span :class="['machine-dot', m.status]"></span>
                  <span :class="['status-label', m.status]">{{ getStatusKr(m.status) }}</span>
                </div>
                <div class="usage-text">
                  <span class="val">{{ m.usage }}</span><span class="unit">kWh</span>
                </div>
              </div>
              <div class="card-footer">
                <span class="machine-id">{{ m.stationNumber }}</span>
                <span class="machine-num-label">급속 충전기</span>
              </div>
            </div>
          </div>
        </div>
      </aside>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import Chart from 'chart.js/auto'
import axios from 'axios'

const getPythonPort = (stationName) => {
  if (!stationName) return 5001;
  const num = parseInt(stationName.replace(/[^0-9]/g, ''));
  const isB = stationName.includes('B');
  return 5000 + (isB ? num + 2 : num);
}

const summary = ref({})
const cctvList = ref([])
const router = useRouter()
const machines = ref([])
const chartData = ref({})

let chart1, chart2, chart3, miniChart
const currentDate = ref(''); const currentTimeOnly = ref('')
let timer

const getStatusText = (status) => {
  const map = {
    charging: '충전 중', warning: '장시간 주차', waiting: '대기중',
    normal: '정상', '정상': '정상', 'violation': '위반 차량',
    'danger': '위반 차량', '비정상': '위반 차량', '일반 차량': '일반 차량', '위반 차량': '위반 차량'
  }
  return map[status] || status;
}

const getStatusClass = (status) => {
  const dangerKeys = ['일반 차량', '위반 차량', 'danger', '비정상', 'violation'];
  const normalKeys = ['정상', 'normal'];
  if (dangerKeys.includes(status)) return 'danger';
  if (normalKeys.includes(status)) return 'normal';
  return { charging: 'charging', warning: 'warning', waiting: 'waiting' }[status] || 'waiting';
}

const updateTime = () => {
  const now = new Date()
  currentDate.value = now.toLocaleDateString('ko-KR', { year: 'numeric', month: '2-digit', day: '2-digit', weekday: 'short' })
  currentTimeOnly.value = now.toLocaleTimeString('ko-KR', { hour: '2-digit', minute: '2-digit', second: '2-digit', hour12: false })
}

const getStatusKr = (status) => {
  const map = { 'available': '사용 가능', 'charging': '충전 중', 'maintenance': '점검 중', 'error': '점검 중' };
  return map[status] || status;
};

const initCharts = () => {
  if (chart1) chart1.destroy(); if (chart2) chart2.destroy(); if (chart3) chart3.destroy(); if (miniChart) miniChart.destroy();
  const commonOptions = { responsive: true, maintainAspectRatio: false, plugins: { legend: { display: true, position: 'bottom', labels: { color: '#ffffff', font: { size: 10, weight: 'bold' } } } }, scales: { y: { grid: { color: 'rgba(255,255,255,0.05)' }, ticks: { color: '#ffffff' } }, x: { grid: { display: false }, ticks: { color: '#ffffff' } } } }

  const c1 = document.getElementById('mainChart1');
  const c2 = document.getElementById('mainChart2');
  const c3 = document.getElementById('mainChart3');
  const cm = document.getElementById('miniChart');

  if (c1) chart1 = new Chart(c1, {type: 'bar', data: {labels: chartData.value.powerLabels, datasets: [{ label: 'kW', data: chartData.value.powerData, backgroundColor: '#76c7a0'}]}, options: commonOptions });
  if (c2) chart2 = new Chart(c2, { type: 'doughnut', data: { labels: ['충전중', '완료', '대기'], datasets: [{ data: [ chartData.value.charging || 0, chartData.value.complete || 0, chartData.value.waiting || 0], backgroundColor: ['#76c7a0', '#5fbaf9', '#3d4c43'], borderWidth: 0 }] }, options: { ...commonOptions, cutout: '80%' } });
  // 🚀 [에러 수정] 중복된 options: 제거 완료
  if (c3) chart3 = new Chart(c3, { type: 'pie', data: { labels: ['정상', '위반'], datasets: [{ data: [chartData.value.normal || 0, chartData.value.violation || 0], backgroundColor: ['#76c7a0', '#ff6b6b'], borderWidth: 0 }] }, options: commonOptions });
  if (cm) miniChart = new Chart(cm, { type: 'line', data: { labels: chartData.value.powerLabels, datasets: [{ label: '점유율', data: chartData.value.occupancy, borderColor: '#76c7a0', borderWidth: 2, pointRadius: 2, fill: true, backgroundColor: 'rgba(118, 199, 160, 0.1)', tension: 0.4 }] }, options: { responsive: true, maintainAspectRatio: false, plugins: { legend: { display: false } }, scales: { x: { display: false }, y: { display: false } } } });
}

const fetchDashboard = async () => {
  try {
    const res = await axios.get('http://localhost:8080/api/dashboard');
    const data = res.data;

    if (data.cctv) {
      cctvList.value = data.cctv.map(item => {
        return {
          station: item.station,
          plateNumber: item.plate || item.plateNumber || "인식중...",
          // 🚀 [연동] 관리자 화면의 경고 메시지가 있으면 우선적으로 뱃지에 표시
          status: (item.warningMsg && item.warningMsg !== "-") ? item.warningMsg : (item.status || "waiting"),
          imageUrl: item.imageUrl
        };
      });
    }

    // 🚀 [복구] 하단 데이터 및 기계 상태 매핑 유지
    summary.value = data.summary || summary.value;
    machines.value = data.machines || machines.value; 
    chartData.value = data.charts || chartData.value;

    await nextTick();
    if (chartData.value.powerLabels) initCharts();
  } catch (err) {
    console.error("데이터 연동 실패:", err);
  }
}

onMounted(() => {
  updateTime();
  timer = setInterval(updateTime, 1000);
  fetchDashboard();
  setInterval(fetchDashboard, 5000);
})

onUnmounted(() => { clearInterval(timer); })
</script>

<style scoped>
/* 스타일 원본 100% 보존 */
.dashboard-container { width: 100vw; height: 100vh; background: #0d2b1e; color: #dbf5ca; display: flex; flex-direction: column; overflow: hidden; font-family: 'Pretendard', sans-serif; }
.dashboard-header { height: 60px; padding: 0 25px; display: flex; align-items: center; justify-content: space-between; border-bottom: 1px solid #1e3b2f; flex-shrink: 0; }
.logo { margin: 0; font-size: 1.8em !important; font-weight: 900; color:#ffffff; }
.dashboard-main { flex: 1; display: flex; padding: 15px; gap: 15px; overflow: hidden; }
.side-panel { width: 240px; display: flex; flex-direction: column; gap: 15px; }
.right-panel { width: 280px; display: flex; flex-direction: column; gap: 15px; overflow-y: auto; padding-right: 5px; }
.right-panel::-webkit-scrollbar { width: 5px; }
.right-panel::-webkit-scrollbar-thumb { background: #1e3b2f; border-radius: 10px; }
.right-section, .stat-card, .mini-chart-box, .graph-card { background: #11211b; border: 1px solid #1e3b2f; border-radius: 6px; padding: 15px; display: flex; flex-direction: column; }
.spacer, .right-spacer { flex: 1; }
.mini-chart-box h4{color: #ffff;}
.alert-text-red { color: #ff6b6b !important; font-weight: 600; font-size: 24px; }
.log-container { display: flex; flex-direction: column; gap: 12px; margin-top: 5px; height: 200px;}
.video-img { width: 100%; height: 100%; object-fit: cover; }
.log-item { display: flex; flex-direction: column; gap: 2px; padding-bottom: 8px; border-bottom: 1px solid rgba(255, 255, 255, 0.05); }
.log-time { font-size: 13px; color: #edf5f1; font-weight: bold; }
.log-msg { font-size: 14px; color: #dbf5ca; }
.section-header h3 { color: white; }
.machine-container { display: flex; flex-direction: column; gap: 10px; }
.machine-card { background: rgba(255, 255, 255, 0.04); border: 1px solid #1e3b2f; border-radius: 8px; padding: 12px; }
.card-row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.status-indicator { display: flex; align-items: center; gap: 10px; }
.status-label { font-size: 14px; font-weight: 800; min-width: 65px; }
.machine-dot { width: 10px; height: 10px; border-radius: 50%; display: inline-block; }
.machine-dot.available { background: #76c7a0; box-shadow: 0 0 8px #76c7a0; }
.status-label.available { color: #76c7a0; }
.machine-dot.charging { background: #5fbaf9; box-shadow: 0 0 8px #5fbaf9; }
.status-label.charging { color: #5fbaf9; }
.machine-dot.maintenance, .machine-dot.error { background: #ff6b6b; box-shadow: 0 0 8px #ff6b6b; }
.status-label.maintenance, .status-label.error { color: #ff6b6b; }
.usage-text .val { font-size: 19px; color: #ffffff; font-weight: 800; }
.usage-text .unit { font-size: 11px; color: #dbf5ca; margin-left: 3px; }
.card-footer { display: flex; justify-content: space-between; align-items: center; }
.machine-id { font-size: 15px; color: #ffffff; font-weight: bold; }
.machine-num-label { font-size: 11px; color: #6a8c7e; }
.live-text { color: #76c7a0; font-weight: bold; font-size: 18px; }
.main-content { flex: 1; display: flex; flex-direction: column; gap: 15px; min-width: 0; }
.video-grid { flex: 0 0 68%; display: grid; grid-template-columns: 1fr 1fr; grid-template-rows: 1fr 1fr; gap: 12px; }
.video-item { background: #000000; border-radius: 6px; position: relative; border: 1px solid #1e3b2f; overflow: hidden; width: 100%; height: 280px; }
.video-header { position: absolute; top: 10px; left: 10px; font-size: 17px; background: rgba(0,0,0,0.6); padding: 4px 8px; border-radius: 4px; z-index: 2; color: #dcf1e2; }
.video-placeholder-text { width: 100%; height: 100%; display: flex; align-items: center; justify-content: center; color: #162a22; font-size: 14px; font-weight: 900; letter-spacing: 4px; }
.ai-overlay { position: absolute; bottom: 12px; left: 12px; z-index: 2; display: flex; gap: 8px; align-items: center; }
.plate-num { background: #ffffff; color: #0d2b1f; padding: 2px 10px; font-size: 13px; font-weight: 800; border-radius: 4px; }
.status-badge { background: #000000 !important; padding: 4px 12px; font-size: 12px; font-weight: 800; border-radius: 5px; display: inline-block; line-height: 1.4; }
.status-badge.charging { color: #1895ee !important; } 
.status-badge.danger { color: #f7b7b7 !important; } 
.status-badge.warning { color: #ffcc5c !important; } 
.status-badge.normal { color: #9ddabd !important; } 
.status-badge.waiting { color: #ffffff !important; } 
.graph-section { flex: 0 0 28.5%; display: flex; gap: 12px; min-height: 0; }
.graph-title { font-size: 20px; color: #f3f3f3; margin-bottom: 10px; font-weight: bold; }
.graph-card { flex: 1; min-width: 0; padding-top: 0px !important; }
.graph-canvas { flex: 1; position: relative; min-height: 0; height: 200px;}
.stat-value { font-size: 24px; font-weight: 600; color: #ffffff; }
.side-time { font-size: 29px; font-weight: 600; color: #ffffff; font-family: monospace; }
.blink-dot { width: 5px; height: 5px; background: #e95454; border-radius: 50%; animation: blink 1.5s infinite; display: inline-block; margin-right: 5px; }
.live-indicator{color: #e95454;}
@keyframes blink { 0%, 100% { opacity: 1; } 50% { opacity: 0.3; } }
.header-right { display: flex; align-items: center; gap: 20px; }
.nav-back-btn { background-color: rgba(255, 255, 255, 0.05); color: #ffffff; border: 1px solid rgba(255, 255, 255, 0.2); padding: 6px 14px; border-radius: 6px; font-size: 16px; font-weight: 600; cursor: pointer; transition: all 0.2s ease; display: flex; align-items: center; gap: 8px; }
</style>