// Demo-safe fallback payloads. Shapes mirror what each Spring controller returns
// so the views render with realistic values when the backend is offline, slow,
// or missing data. Keep strings short + plausible — this is shown at portfolio
// time. No template/CSS coupling.

// Plate numbers match the actual YOLO-recognized plates from the demo MP4s:
//   A-01 car100.mp4 → 63러2314 (BMW iX3, EV)
//   A-02 car9.mp4   → 386마1144 (red hatchback, non-EV)
//   B-01 car104.mp4 → 56나1425 (KIA EV3, EV)
//   B-02 car105.mp4 → 152가5012 (green EV SUV)
export const mockUserProfile = {
  name: '홍길동',
  plateNumber: '63러2314',
  role: 'USER',
}

export const mockHistory = [
  { stationNumber: 'A-01', startTime: '2026-04-12T09:14:00', status: '완료' },
  { stationNumber: 'B-01', startTime: '2026-04-10T18:41:00', status: '완료' },
  { stationNumber: 'B-02', startTime: '2026-04-07T21:05:00', status: '완료' },
]

export const mockQueueWaiting = [
  { stationNumber: 'A-01', status: 'waiting', estimatedMinutes: 2 },
  { stationNumber: 'B-01', status: 'waiting', estimatedMinutes: 7 },
]

export const mockUserNotifications = [
  'A-01 충전이 완료되었습니다.',
  'B-01 대기열이 자동 갱신되었습니다.',
]

// /api/detection/list response
export const mockDetectionList = {
  chargingStatusList: [
    { station: 'A-01', plate: '63러2314',  isEV: 'Yes', entryTime: '13:04', status: '정상',  chargeTime: 12, warning: false, warningMsg: null },
    { station: 'A-02', plate: '386마1144', isEV: 'No',  entryTime: '13:09', status: '비정상', chargeTime:  8, warning: true,  warningMsg: '일반 차량' },
    { station: 'B-01', plate: '56나1425',  isEV: 'Yes', entryTime: '13:02', status: '정상',  chargeTime: 24, warning: false, warningMsg: null },
    { station: 'B-02', plate: '152가5012', isEV: 'Yes', entryTime: '12:51', status: '정상',  chargeTime: 36, warning: false, warningMsg: null },
  ],
  alertLogs: [
    { id: 1, time: '13:09', msg: '[A-02] 일반 차량 감지!', type: 'danger' },
    { id: 2, time: '13:04', msg: '[A-01] 충전 시작', type: 'warning' },
  ],
}

// /api/admin/queue/waiting response
export const mockAdminQueue = [
  { chargerId: 'A-01', rank: 1, carNumber: '87로5521', estimatedMinutes:  5 },
  { chargerId: 'B-01', rank: 2, carNumber: '21구1109', estimatedMinutes: 12 },
  { chargerId: 'B-02', rank: 3, carNumber: '60무8802', estimatedMinutes: 18 },
]

// /api/dashboard (video board)
export const mockDashboard = {
  cctv: [
    { station: 'A-01', plate: '63러2314',  status: '정상',   warningMsg: null,       imageUrl: '' },
    { station: 'A-02', plate: '386마1144', status: '비정상', warningMsg: '일반 차량', imageUrl: '' },
    { station: 'B-01', plate: '56나1425',  status: '정상',   warningMsg: null,       imageUrl: '' },
    { station: 'B-02', plate: '152가5012', status: '정상',   warningMsg: null,       imageUrl: '' },
  ],
  summary: {
    todayCharge: 24,
    illegalCount: 3,
    waitingCount: 5,
    occupancyRate: 72,
  },
  machines: [
    { stationNumber: 'A-01', status: 'charging',    usage: 42.1 },
    { stationNumber: 'A-02', status: 'available',   usage:  0   },
    { stationNumber: 'B-01', status: 'maintenance', usage:  0   },
    { stationNumber: 'B-02', status: 'charging',    usage: 18.4 },
  ],
  charts: {
    powerLabels: ['09', '10', '11', '12', '13', '14', '15', '16', '17', '18'],
    powerData:   [ 32,   45,   58,   64,   71,   60,   55,   48,   52,   70 ],
    charging: 2, complete: 5, waiting: 3,
    normal:   18, violation: 3,
    occupancy: [0.4, 0.55, 0.62, 0.7, 0.72, 0.68, 0.6, 0.58, 0.6, 0.75],
  },
}

// Live-looking /api/dashboard. Each call nudges values so the portfolio demo
// has motion: bar chart values ripple, occupancy waves, today-charge grows
// slowly, waiting/charging counts drift within plausible ranges.
let _dashTick = 0
const _dashState = {
  todayCharge: mockDashboard.summary.todayCharge,
  illegalCount: mockDashboard.summary.illegalCount,
  normalTotal: mockDashboard.charts.normal,
  violationTotal: mockDashboard.charts.violation,
  powerData: [...mockDashboard.charts.powerData],
  machines: mockDashboard.machines.map(m => ({ ...m })),
  alertLog: [
    { time: '13:02:20', msg: '미등록 차량 감지 (386마 1144)', type: 'danger' },
    { time: '13:05:01', msg: '63러 2314 충전 시작',             type: 'info' },
    { time: '12:50:11', msg: '62서 9811 충전 완료',             type: 'success' },
  ],
}

const _sampleEvents = [
  { msg: '63러 2314 충전 시작',             type: 'info' },
  { msg: '63러 2314 충전 완료',             type: 'success' },
  { msg: '56나 1425 충전 시작',             type: 'info' },
  { msg: '56나 1425 충전 완료',             type: 'success' },
  { msg: '152가 5012 충전 시작',            type: 'info' },
  { msg: '152가 5012 충전 완료',            type: 'success' },
  { msg: '미등록 차량 감지 (386마 1144)',   type: 'danger' },
  { msg: '장시간 주차 감지 (A-02)',         type: 'danger' },
  { msg: '충전 대기열 자동 진행 (A-01)',    type: 'info' },
  { msg: '충전기 점검 알림 (B-01)',         type: 'danger' },
]

export const makeLiveDashboard = () => {
  _dashTick++

  // Power bar — each bar wobbles smoothly with a per-bar phase offset.
  const powerData = _dashState.powerData.map((v, i) => {
    const target = 40 + 25 * Math.sin((_dashTick + i * 2) / 5) + (Math.random() - 0.5) * 4
    const next = v * 0.7 + target * 0.3
    _dashState.powerData[i] = next
    return Number(next.toFixed(1))
  })

  // Charger state snapshot: totals stay near 10 stations.
  const occupied = 2 + Math.round(Math.abs(Math.sin(_dashTick / 4)) * 3)  // 2~5
  const complete = 3 + Math.round(Math.abs(Math.cos(_dashTick / 3)) * 3)  // 3~6
  const waiting  = 2 + Math.round(Math.abs(Math.sin(_dashTick / 5 + 1)) * 3) // 2~5

  // Violation ratio trends upward slowly.
  if (Math.random() < 0.25) _dashState.normalTotal += 1
  if (Math.random() < 0.08) _dashState.violationTotal += 1

  // Occupancy waveform: 10 points sliding each call.
  const occupancy = Array.from({ length: 10 }, (_, i) =>
    0.55 + 0.20 * Math.sin((_dashTick + i) / 3.2) + (Math.random() - 0.5) * 0.05
  )
  const powerLabels = Array.from({ length: 10 }, (_, i) => {
    const h = 9 + i
    return h < 10 ? '0' + h : '' + h
  })

  // Summary grows + drifts.
  if (Math.random() < 0.25) _dashState.todayCharge += 1
  if (Math.random() < 0.05) _dashState.illegalCount += 1
  const waitingCount = 3 + Math.round(Math.abs(Math.sin(_dashTick / 6)) * 5)
  const occupancyRate = Math.round(occupancy[occupancy.length - 1] * 100)

  // Machines: charging stations swap status occasionally, kWh drifts.
  const machines = _dashState.machines.map(m => {
    let { stationNumber, status, usage } = m
    if (status === 'charging') {
      usage = Math.max(0, usage + (Math.random() - 0.3) * 1.2)   // mostly grows
      if (Math.random() < 0.04) { status = 'available'; usage = 0 } // finish
    } else if (status === 'available') {
      if (Math.random() < 0.06) { status = 'charging'; usage = 5 + Math.random() * 8 } // start
    } else if (status === 'maintenance') {
      if (Math.random() < 0.02) { status = 'available'; usage = 0 } // repair done
    }
    m.status = status; m.usage = usage
    return { stationNumber, status, usage: Number(usage.toFixed(1)) }
  })

  // Event log: 12% chance per tick to push a new event onto the top of the log.
  if (Math.random() < 0.45) {
    const ev = _sampleEvents[Math.floor(Math.random() * _sampleEvents.length)]
    const now = new Date()
    const hh = String(now.getHours()).padStart(2, '0')
    const mm = String(now.getMinutes()).padStart(2, '0')
    const ss = String(now.getSeconds()).padStart(2, '0')
    _dashState.alertLog.unshift({ time: `${hh}:${mm}:${ss}`, msg: ev.msg, type: ev.type })
    _dashState.alertLog = _dashState.alertLog.slice(0, 8)
  }

  return {
    cctv: mockDashboard.cctv,
    machines,
    eventLog: _dashState.alertLog.slice(),
    summary: {
      todayCharge:   _dashState.todayCharge,
      illegalCount:  _dashState.illegalCount,
      waitingCount,
      occupancyRate,
    },
    charts: {
      powerLabels,
      powerData,
      charging: occupied,
      complete,
      waiting,
      normal:    _dashState.normalTotal,
      violation: _dashState.violationTotal,
      occupancy,
    },
  }
}

// /api/db/usage — static baseline used as fallback for error cases.
export const mockDbUsage = {
  accuracy: '96.4%',
  errorRate: '3.6%',
  totalCapacity: '512 MB',
  dbStatus: '연결 정상',
  databaseList: [
    { name: 'users',            total:  64, used:  12, percent: 18.75 },
    { name: 'vehicles',         total:  64, used:  14, percent: 21.88 },
    { name: 'charging_station', total:  32, used:   4, percent: 12.50 },
    { name: 'charging_queue',   total:  64, used:  22, percent: 34.38 },
    { name: 'charging_history', total: 128, used:  71, percent: 55.47 },
    { name: 'detection_log',    total: 160, used: 148, percent: 92.50 },
  ],
}

// Live-looking DB usage: each call nudges values so the portfolio demo feels
// alive when the chart refreshes. detection_log grows steadily (cars keep
// being detected), charging_queue/history drift within a band, and accuracy
// jitters ±0.5%. When a table fills up it resets with a slightly larger
// allocation — mimicking autogrow.
let _dbTick = 0
const _dbState = mockDbUsage.databaseList.map(r => ({ ...r }))

const _jitter = (v, amp) => v + (Math.random() * 2 - 1) * amp
const _clamp  = (v, lo, hi) => Math.max(lo, Math.min(hi, v))

export const makeLiveDbUsage = () => {
  _dbTick++
  const rows = _dbState.map(row => {
    let { name, total, used } = row
    if (name === 'detection_log') {
      used = used + (0.4 + Math.random() * 0.6)             // +0.4~1.0 MB / tick
      if (used >= total * 0.98) { total = Math.ceil(total * 1.5); }
    } else if (name === 'charging_history') {
      used = _clamp(_jitter(used, 0.8) + 0.15, 1, total - 1) // mostly grows
    } else if (name === 'charging_queue') {
      used = _clamp(_jitter(used, 1.5), 1, total - 1)        // waves up/down
    } else if (name === 'users' || name === 'vehicles') {
      used = _clamp(used + (Math.random() < 0.25 ? 1 : 0), 0, total - 1)
    } else {
      used = _clamp(_jitter(used, 0.3), 0, total - 1)
    }
    row.total = total; row.used = used
    const percent = Number(((used / total) * 100).toFixed(2))
    return { name, total, used: Number(used.toFixed(2)), percent }
  })

  const totalCap = rows.reduce((s, r) => s + r.total, 0)
  const totalUsed = rows.reduce((s, r) => s + r.used,  0)

  // Accuracy drifts slightly around 96%.
  const baseAcc = 96 + Math.sin(_dbTick / 6) * 0.6 + (Math.random() - 0.5) * 0.3
  const acc = _clamp(baseAcc, 93.5, 98.5)

  return {
    accuracy:      acc.toFixed(1) + '%',
    errorRate:     (100 - acc).toFixed(1) + '%',
    totalCapacity: totalCap.toFixed(0) + ' MB',
    dbStatus:      '연결 정상',
    databaseList:  rows,
  }
}

// Shared helper: true when the portfolio demo should bypass the real backend.
// Defaults to TRUE for this portfolio project so Vercel deploys "just work"
// without env-var gymnastics. To hit a real backend, set VITE_USE_MOCK=false
// explicitly in your .env / Vercel project settings.
export const isDemoMode = () => {
  const v = import.meta.env.VITE_USE_MOCK
  if (v === 'false' || v === false) return false   // explicit opt-out
  return true
}

// Static CCTV stills served by Vite from /public/demo/*.jpg. Used when no
// real MJPEG stream is configured — gives the portfolio demo a realistic
// frame instead of a black placeholder.
// Video clips (used when rendered via <video>) and still frames (used when
// rendered via <img>). Keep the same asset base name so wiring is obvious.
export const DEMO_STREAM_VIDEO = {
  'A-01': '/demo/cctv-a01.mp4',
  'A-02': '/demo/cctv-a02.mp4',
  'B-01': '/demo/cctv-b01.mp4',
  'B-02': '/demo/cctv-b02.mp4',
}
export const DEMO_STREAM_URL = {
  'A-01': '/demo/cctv-a01.jpg',
  'A-02': '/demo/cctv-a02.jpg',
  'B-01': '/demo/cctv-b01.jpg',
  'B-02': '/demo/cctv-b02.jpg',
}
export const DEMO_PLATE_URL = {
  'A-01': '/demo/plate-a01.jpg',
  'A-02': '/demo/plate-a02.jpg',
  'B-01': '/demo/plate-b01.jpg',
  'B-02': '/demo/plate-b02.jpg',
}

// Demo account (offline-safe login)
export const DEMO_USER = {
  loginId: 'user',
  password: '1234',
  response: {
    userPk: 1,
    loginId: 'user',
    name: '홍길동',
    role: 'USER',
    accessToken: 'demo.token.portfolio',
    tokenType: 'Bearer',
  },
}
export const DEMO_ADMIN = {
  loginId: 'admin',
  password: '1234',
  response: {
    userPk: 99,
    loginId: 'admin',
    name: '관리자',
    role: 'ADMIN',
    accessToken: 'demo.token.admin',
    tokenType: 'Bearer',
  },
}
