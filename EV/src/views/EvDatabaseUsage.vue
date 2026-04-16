<template>
  <div class="dashboard-wrapper">
    <header class="main-header">
      <h1 class="header-title">EV 데이터베이스 사용량</h1>
      <div class="header-right">
        <div class="button-group">
          <button @click="goToMonitoring" class="nav-back-btn">
            <span class="icon">📊</span> 모니터링 대시보드
          </button>
          <button class="logout-pill" @click="handleLogout">로그아웃</button>
        </div>
      </div>
    </header>

    <main class="content-body">
      <section class="summary-grid">
        <div class="info-card" v-for="item in summaryData" :key="item.label">
          <div class="card-top">
            <span class="label">{{ item.label }}</span>
            <span class="card-icon">{{ item.icon }}</span>
          </div>
          <h2 class="value" :class="item.colorClass">{{ item.value }}</h2>
          <p class="sub-label" :class="{ 'red-text': item.error }">{{ item.sub }}</p>
        </div>
      </section>

      <section class="chart-container">
        <div class="chart-header">
          <h3>테이블스페이스 사용 현황 (MB)</h3>
          <div class="chart-legend">
            <div class="legend-item"><span class="dot used"></span> 실사용량</div>
          </div>
        </div>

        <div class="line-chart-area">
          <canvas ref="lineChartRef"></canvas>
        </div>
      </section>

      <section class="table-container">
        <table class="simple-table">
          <thead>
            <tr>
              <th>테이블스페이스</th>
              <th>전체 (MB)</th>
              <th>사용 (MB)</th>
              <th>여유 (MB)</th>
              <th>상태</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="db in databaseList" :key="db.name">
              <td class="bold-name">{{ db.name }}</td>
              <td class="num-text">
                {{ Number(db.total).toLocaleString(undefined, { minimumFractionDigits: 0, maximumFractionDigits: 2 }) }}
              </td>
              <td class="num-text">
                {{ Number(db.used).toLocaleString(undefined, { minimumFractionDigits: 0, maximumFractionDigits: 2 }) }}
              </td>
              <td class="num-text">
                {{ (Number(db.total) - Number(db.used)).toLocaleString(undefined, { minimumFractionDigits: 0, maximumFractionDigits: 2 }) }}
              </td>
              <td>
                <span :class="['status-tag', Number(db.percent) > 90 ? 'warn' : 'ok']">
                  {{ Number(db.percent) > 90 ? '경고' : '안정' }}
                </span>
              </td>
            </tr>
          </tbody>
        </table>
      </section>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue';
import { useRouter } from 'vue-router';
import axios from 'axios';
import Chart from 'chart.js/auto';

const router = useRouter();
const goToMonitoring = () => { router.push({ name: 'EvChargingZoneMonitoring' }); };
const handleLogout = () => { if (confirm('로그아웃 하시겠습니까?')) router.push('/'); };

const summaryData = ref([]);
const databaseList = ref([]);
const lineChartRef = ref(null);

let refreshTimer;
let usageChart = null;

const renderChart = () => {
  if (!lineChartRef.value) return;

  const labels = databaseList.value.map(db => db.name);
  const usedData = databaseList.value.map(db => Number(db.used) || 0);
  const maxUsed = Math.max(...usedData, 0);

  if (usageChart) {
    usageChart.destroy();
    usageChart = null;
  }

  usageChart = new Chart(lineChartRef.value, {
    type: 'line',
    data: {
      labels,
      datasets: [
        {
          label: '실사용량(MB)',
          data: usedData,
          borderColor: '#ffcd56',
          backgroundColor: 'rgba(255, 205, 86, 0.18)',
          borderWidth: 3,
          fill: true,
          tension: 0.3,
          pointRadius: 4,
          pointHoverRadius: 6,
          pointBackgroundColor: '#ffcd56'
        }
      ]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      animation: false,
      plugins: {
        legend: {
          display: true,
          labels: {
            color: '#444',
            font: { size: 12, weight: '700' }
          }
        }
      },
      scales: {
        y: {
          beginAtZero: true,
          suggestedMax: maxUsed > 0 ? Math.ceil(maxUsed * 1.2) : 1,
          ticks: {
            color: '#777',
            font: { size: 11, weight: '700' }
          },
          grid: { color: '#eee' }
        },
        x: {
          ticks: {
            color: '#555',
            font: { size: 11, weight: '700' }
          },
          grid: { display: false }
        }
      }
    }
  });
};

const fetchDbUsage = async () => {
  try {
    const res = await axios.get("http://localhost:8080/api/db/usage");
    databaseList.value = Array.isArray(res.data.databaseList) ? res.data.databaseList : [];
    summaryData.value = [
      { label: '인식 정확도', value: res.data.accuracy || '0.0%', sub: '', colorClass: 'success-text' },
      { label: '인식 오차율', value: res.data.errorRate || '100.0%', sub: '', colorClass: 'error-text', error: true },
      { label: '전체 할당 용량', value: res.data.totalCapacity || '0 MB', sub: 'Oracle 21c 기준', icon: '' },
      { label: 'DB 연결 상태', value: res.data.dbStatus || '연결 오류', sub: '서버 연결 상태', colorClass: 'healthy-text' }
    ];
    await nextTick();
    renderChart();
  } catch (err) {
    console.error("❌ DB 사용량 데이터 호출 실패:", err);
    summaryData.value = [
      { label: '인식 정확도', value: '0.0%', sub: 'confidence 기준 실시간 DB 집계', colorClass: 'success-text' },
      { label: '인식 오차율', value: '100.0%', sub: '100 - 인식정확도', colorClass: 'error-text', error: true },
      { label: '전체 할당 용량', value: '0 MB', sub: 'Oracle 21c 기준', icon: '' },
      { label: 'DB 연결 상태', value: '연결 오류', sub: '서버 연결 상태', colorClass: 'error-text', error: true }
    ];
    databaseList.value = [];
    if (usageChart) {
      usageChart.destroy();
      usageChart = null;
    }
  }
};

onMounted(() => {
  fetchDbUsage();
  refreshTimer = setInterval(fetchDbUsage, 10000);
});

onUnmounted(() => {
  clearInterval(refreshTimer);
  if (usageChart) {
    usageChart.destroy();
    usageChart = null;
  }
});
</script>

<style scoped>
/* 🚀 전체 높이 고정 및 스크롤 방지 */
.dashboard-wrapper { 
  background-color: #f0f9f4; 
  height: 100vh; 
  font-family: 'Pretendard', sans-serif; 
  overflow: hidden; 
  display: flex;
  flex-direction: column;
}

/* 🚀 헤더와 버튼은 기존 크기 유지 */
.main-header {
  background-color: #0d2b1f; height: 60px; padding: 0 40px;
  display: flex; justify-content: space-between; align-items: center;
  flex-shrink: 0;
}
.header-title { margin: 0; font-size: 1.8rem !important; font-weight: 900; color:#ffffff; }
.button-group { display: flex; align-items: center; gap: 10px; }

/* 🚀 내부 컨텐츠 여백 및 높이 비율 조정 */
.content-body { 
  flex: 1;
  padding: 10px 25px 20px 25px !important; 
  max-width: 1450px; 
  margin: 0 auto; 
  display: flex;
  flex-direction: column;
  overflow: hidden;
  width: 100%;
  box-sizing: border-box;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 10px;
  margin-bottom: 10px;
  flex-shrink: 0;
}

.info-card {
  background: white;
  padding: 10px 13px;
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.02);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  min-height: 90px;
  box-sizing: border-box;
}

.value {
  font-size: 28px;
  font-weight: 900;
  margin: 5px 0px;
  line-height: 1;
}

.chart-container {
  background: white;
  padding: 10px 25px;
  border-radius: 15px;
  margin-bottom: 10px;
  flex: 1; /* 차트가 남는 공간에 맞게 자동 조절됨 */
  display: flex;
  flex-direction: column;
  min-height: 0;
}
.chart-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 5px; }
.chart-header h3 { font-size: 1rem; font-weight: 800; margin: 0; color: #333; }

.line-chart-area {
  flex: 1;
  position: relative;
  min-height: 0;
}

/* 🚀 테이블 컨테이너 여백 축소 */
.table-container { 
  background: white; 
  padding: 10px 20px; 
  border-radius: 15px; 
  box-shadow: 0 4px 10px rgba(0,0,0,0.02);
  flex-shrink: 0;
}
.simple-table { width: 100%; border-collapse: collapse; }
.simple-table th { padding: 5px; border-bottom: 2px solid #f0f9f4; color: #0d2b1f; font-size: 0.85rem; font-weight: 800; text-align: center; }
.simple-table td { padding: 6px 10px; border-bottom: 1px solid #f0f9f4; font-size: 0.8rem; color: #666; text-align: center; }

/* 기존 스타일 유지 */
.label { color: #383737; font-size: 0.9rem; font-weight: 700; }
.sub-label { font-size: 0.75rem; color: #888; margin: 0; line-height: 1.2; }
.bold-name { font-weight: 800; color: #0d2b1f; font-size: 0.85rem; }
.num-text { font-weight: 600; color: #777; font-size: 0.8rem; }
.status-tag { padding: 1px 6px; border-radius: 4px; font-size: 0.7rem; font-weight: 800; display: inline-block; }
.status-tag.ok { background: #e8f8f0; color: #2ecc71; }
.status-tag.warn { background: #fff1f0; color: #ff4d4f; }
.nav-back-btn:hover {
  background-color: rgba(255, 255, 255, 0.1);
  border-color: #ffffff; /* 메인 테마색인 민트색으로 포인트 */
  color: #ffffff;
}
.nav-back-btn { background-color: rgba(255, 255, 255, 0.05); color: #ffffff; border: 1px solid rgba(255, 255, 255, 0.2); padding: 6px 14px; border-radius: 6px; font-size: 16px; font-weight: 600; cursor: pointer; transition: all 0.2s ease; display: flex; align-items: center; gap: 8px; }
.logout-pill { background: transparent; border: 1.5px solid #ffffff; color: #ffffff; padding: 5px 18px; border-radius: 50px; cursor: pointer; font-size: 0.9rem; font-weight: 700; transition: all 0.2s ease; }
.logout-pill:hover { background-color: #ffffff; color: #0d2b1f !important; }
.success-text { color: #2ecc71; }
.error-text { color: #ff4d4f; }
.healthy-text { color: #3498db; }
.dot { display: inline-block; width: 8px; height: 8px; border-radius: 2px; }
.dot.used { background: #ffcd56; }
</style>