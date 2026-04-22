<template>
  <div class="mypage-wrapper">
    <div class="bg-overlay"></div>

    <nav class="top-nav">
      <div class="header-main-group">
        <h1 class="header-title">마이페이지</h1>
        <span class="datetime white-important">{{ currentTime }}</span>
      </div>
      <div class="header-right-group">
        <button class="nav-pill white-important" @click="$router.push('/EVUserDashboard')">← 대시보드</button>
        <button class="nav-pill logout white-important" @click="handleLogout">로그아웃</button>
      </div>
    </nav>

    <div class="main-content">
      <aside class="panel left-panel">
        <div class="glass-card profile-card">
          <div class="avatar-circle">{{ initial }}</div>
          <div class="profile-name-row">
            <span class="p-name white-important">{{ profile.name || '사용자' }}</span>
            <small class="tag">{{ profile.role === 'ADMIN' ? '관리자' : '입주민' }}</small>
          </div>
          <p class="p-id white-important">@{{ profile.loginId || 'user' }}</p>
        </div>

        <div class="glass-card">
          <h3 class="card-label white-important">등록 정보</h3>
          <div class="info-row"><span class="k white-important">이름</span><span class="v white-important">{{ profile.name || '-' }}</span></div>
          <div class="info-row"><span class="k white-important">아이디</span><span class="v white-important">{{ profile.loginId || '-' }}</span></div>
          <div class="info-row"><span class="k white-important">전화번호</span><span class="v white-important">{{ profile.phone }}</span></div>
          <div class="info-row"><span class="k white-important">동 / 호수</span><span class="v white-important">{{ profile.building }}동 {{ profile.unit }}호</span></div>
          <div class="info-row"><span class="k white-important">차량번호</span><span class="v green-text">{{ profile.plateNumber }}</span></div>
          <div class="info-row"><span class="k white-important">차종</span><span class="v white-important">{{ profile.vehicleType }}</span></div>
          <div class="info-row"><span class="k white-important">가입일</span><span class="v white-important">{{ profile.joinedAt }}</span></div>
        </div>
      </aside>

      <section class="panel right-panel">
        <div class="glass-card">
          <h3 class="card-label white-important">충전 통계</h3>
          <div class="stat-grid">
            <div class="stat-box">
              <div class="stat-val white-important">{{ stats.totalSessions }}</div>
              <div class="stat-lbl white-important">총 충전 횟수</div>
            </div>
            <div class="stat-box">
              <div class="stat-val gold-text">{{ stats.totalKwh.toFixed(1) }}</div>
              <div class="stat-lbl white-important">총 사용량 (kWh)</div>
            </div>
            <div class="stat-box">
              <div class="stat-val green-text">{{ stats.totalFee.toLocaleString() }}</div>
              <div class="stat-lbl white-important">누적 요금 (원)</div>
            </div>
            <div class="stat-box">
              <div class="stat-val white-important">{{ stats.favoriteStation }}</div>
              <div class="stat-lbl white-important">최다 이용 충전소</div>
            </div>
          </div>
        </div>

        <div class="glass-card">
          <h3 class="card-label white-important">최근 활동</h3>
          <ul class="activity-list">
            <li v-for="(a, i) in activities" :key="i" class="activity-row">
              <span class="a-time white-important">{{ a.time }}</span>
              <span class="a-msg white-important">{{ a.msg }}</span>
            </li>
          </ul>
        </div>

        <div class="glass-card">
          <h3 class="card-label white-important">설정</h3>
          <div class="settings-row">
            <button class="action-btn" @click="notify('비밀번호 변경 기능은 준비 중입니다.')">비밀번호 변경</button>
            <button class="action-btn cancel" @click="notify('방문차량 등록 기능은 준비 중입니다.')">방문차량등록</button>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { mockUserProfile } from '../demo/mockData'

const router = useRouter()
const currentTime = ref('')

const profile = ref({
  name: localStorage.getItem('name') || mockUserProfile.name,
  loginId: localStorage.getItem('loginId') || 'user',
  role: localStorage.getItem('role') || 'USER',
  phone: '010-1234-5678',
  building: 'A',
  unit: '1204',
  plateNumber: mockUserProfile.plateNumber,
  vehicleType: 'EV',
  joinedAt: '2025-11-22',
})

const initial = computed(() => (profile.value.name || 'U').charAt(0))

const stats = ref({
  totalSessions: 42,
  totalKwh: 612.3,
  totalFee: 104091,
  favoriteStation: 'A-01',
})

const activities = ref([
  { time: '오늘 13:04', msg: 'A-01 충전 시작 (63러2314)' },
  { time: '오늘 09:30', msg: '로그인 (Chrome · Windows)' },
  { time: '어제 22:15', msg: 'B-02 충전 완료 — 38.4 kWh' },
  { time: '어제 18:10', msg: '대기열 등록 취소 (A-02)' },
  { time: '4/13', msg: '방문차량 등록 승인' },
])

let timer = null
let tickTimer = null

onMounted(() => {
  currentTime.value = new Date().toLocaleString('ko-KR')
  timer = setInterval(() => { currentTime.value = new Date().toLocaleString('ko-KR') }, 1000)

  // Demo live-feel: stats slowly grow.
  tickTimer = setInterval(() => {
    if (Math.random() < 0.15) stats.value.totalSessions += 1
    stats.value.totalKwh += Math.random() * 0.3
    stats.value.totalFee = Math.round(stats.value.totalKwh * 170)
  }, 3000)
})
onUnmounted(() => { clearInterval(timer); clearInterval(tickTimer) })

const notify = (msg) => { alert(msg) }

const handleLogout = () => {
  if (confirm('로그아웃 하시겠습니까?')) {
    localStorage.removeItem('userPk')
    localStorage.removeItem('accessToken')
    localStorage.removeItem('loginId')
    localStorage.removeItem('name')
    localStorage.removeItem('role')
    router.push('/')
  }
}
</script>

<style scoped>
.mypage-wrapper { position: relative; width: 100%; height: 100vh; color: white; font-family: 'Pretendard', sans-serif; display: flex; flex-direction: column; overflow: hidden; }
.bg-overlay { position: fixed; inset: 0; background: linear-gradient(rgba(0,0,0,0.55), rgba(0,0,0,0.75)), url('https://images.unsplash.com/photo-1593941707882-a5bba14938c7?q=80&w=2072'); background-size: cover; background-position: center; z-index: -1; }
.white-important { color: #ffffff !important; }
.green-text { color: #22c55e !important; font-weight: 700; }
.gold-text { color: #facc15 !important; font-weight: 800; }

.top-nav { background-color: #0d2b1f; height: 60px; display: flex; justify-content: space-between; align-items: center; padding: 0 40px; border-bottom: 1px solid rgba(255,255,255,0.1); }
.header-main-group { display: flex; align-items: center; gap: 20px; }
.header-title { margin: 0; font-size: 1.8em !important; font-weight: 900; color:#ffffff; }
.datetime { font-size: 0.95rem; }
.header-right-group { display: flex; gap: 10px; }
.nav-pill { background: transparent; border: 1.5px solid rgba(255, 255, 255, 0.6); color: #ffffff; padding: 5px 18px; border-radius: 50px; cursor: pointer; font-size: 0.85rem; font-weight: 700; transition: all 0.2s ease; }
.nav-pill:hover { background-color: rgba(255, 255, 255, 0.1); border-color: #ffffff; }
.nav-pill.logout:hover { background-color: #ffffff; color: #0d2b1f !important; }

.main-content { display: flex; padding: 18px 40px; gap: 22px; flex: 1 1 0; min-height: 0; overflow: hidden; box-sizing: border-box; }
.panel { display: flex; flex-direction: column; gap: 12px; min-height: 0; overflow: hidden; }
.left-panel { flex: 0.4; }
.right-panel { flex: 0.6; }
.glass-card { background: rgba(255, 255, 255, 0.08); backdrop-filter: blur(20px); border: 1px solid rgba(255, 255, 255, 0.15); border-radius: 20px; padding: 14px 20px; flex-shrink: 0; }
.card-label { font-size: 1rem; font-weight: 800; margin: 0 0 10px; display: block; text-transform: uppercase; }

.profile-card { display: flex; flex-direction: column; align-items: center; padding: 18px 20px; gap: 6px; }
.avatar-circle { width: 68px; height: 68px; border-radius: 50%; background: linear-gradient(135deg, #22c55e, #0d2b1f); display: flex; align-items: center; justify-content: center; font-size: 1.8rem; font-weight: 900; color: #fff; }
.profile-name-row { display: flex; align-items: center; gap: 8px; margin-top: 8px; }
.p-name { font-size: 1.4rem; font-weight: 900; }
.tag { background: #22c55e; color: #fff; padding: 3px 10px; border-radius: 4px; font-size: 0.75rem; }
.p-id { opacity: 0.7; font-size: 0.9rem; margin: 0; }

.info-row { display: flex; justify-content: space-between; padding: 6px 0; border-bottom: 1px solid rgba(255,255,255,0.08); font-size: 0.88rem; }
.info-row:last-child { border-bottom: none; }
.k { opacity: 0.7; }
.v { font-weight: 700; }

.stat-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }
.stat-box { background: rgba(0,0,0,0.25); border-radius: 12px; padding: 12px; text-align: center; }
.stat-val { font-size: 1.5rem; font-weight: 900; line-height: 1; }
.stat-lbl { font-size: 0.8rem; opacity: 0.85; margin-top: 4px; }

.activity-list { list-style: none; padding: 0; margin: 0; }
.activity-row { display: flex; gap: 14px; padding: 6px 0; border-bottom: 1px solid rgba(255,255,255,0.06); font-size: 0.88rem; }
.activity-row:last-child { border-bottom: none; }
.a-time { opacity: 0.6; min-width: 90px; }

.settings-row { display: flex; gap: 12px; }
.action-btn { flex: 1; border: none; padding: 10px; border-radius: 12px; font-weight: 800; cursor: pointer; background: #22c55e; color: #fff; font-size: 0.95rem; transition: transform 0.15s; }
.action-btn:hover { transform: translateY(-1px); }
.action-btn.cancel { background: rgba(255,255,255,0.1); border: 1px solid rgba(255,255,255,0.3); }
</style>
