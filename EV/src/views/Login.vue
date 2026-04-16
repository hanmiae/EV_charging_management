<template>
  <div class="login-page">
    <h1 class="main-title">전기차충전구역 관리 시스템</h1>
    <div class="login-box glass-card">
      <h1>로그인</h1>

      <div class="login-tabs">
        <button @click="loginType = 'user'" :class="{ active: loginType === 'user' }">사용자</button>
        <button @click="loginType = 'admin'" :class="{ active: loginType === 'admin' }">관리자</button>
      </div>

      <div v-if="loginType === 'user'" class="form-group">
        <div class="input-wrapper">
          <span class="icon">
            <svg viewBox="0 0 24 24" width="22" height="22"><path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/></svg>
          </span>
          <input v-model="loginId" placeholder="Username" @keyup.enter="login" />
        </div>
        <div class="input-wrapper">
          <span class="icon">
            <svg viewBox="0 0 24 24" width="22" height="22"><path d="M18 8h-1V6c0-2.76-2.24-5-5-5S7 3.24 7 6v2H6c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V10c0-1.1-.9-2-2-2zM9 6c0-1.66 1.34-3 3-3s3 1.34 3 3v2H9V6zm9 14H6V10h12v10zm-6-3c1.1 0 2-.9 2-2s-.9-2-2-2-2 .9-2 2 .9 2 2 2z"/></svg>
          </span>
          <input v-model="password" type="password" placeholder="Password" @keyup.enter="login" />
        </div>
      </div>

      <div v-else class="form-group">
        <div class="input-wrapper">
          <span class="icon">
            <svg viewBox="0 0 24 24" width="22" height="22"><path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/></svg>
          </span>
          <input v-model="adminId" placeholder="Admin ID" @keyup.enter="login" />
        </div>
        <div class="input-wrapper">
          <span class="icon">
            <svg viewBox="0 0 24 24" width="22" height="22"><path d="M18 8h-1V6c0-2.76-2.24-5-5-5S7 3.24 7 6v2H6c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V10c0-1.1-.9-2-2-2zM9 6c0-1.66 1.34-3 3-3s3 1.34 3 3v2H9V6zm9 14H6V10h12v10zm-6-3c1.1 0 2-.9 2-2s-.9-2-2-2-2 .9-2 2 .9 2 2 2z"/></svg>
          </span>
          <input v-model="adminPassword" type="password" placeholder="Password" @keyup.enter="login" />
        </div>
      </div>

      <button class="login-btn btn-primary" @click="login" :disabled="isSubmitting">
        {{ isSubmitting ? '로그인 중...' : '로그인' }}
      </button>

      <p class="signup signup-text" @click="$router.push('/signup')">회원가입</p>
    </div>

    <div class="logo-area">
      <img src="/Logo_ITS-1.png" />
      <img src="/logo_straffic-1.png" />
      <img src="/logo_mbc-1.png" />
      <img src="/logo_teamtwo-1.png" />
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

const router = useRouter()
const loginType = ref('user')

const loginId = ref('')
const password = ref('')
const adminId = ref('')
const adminPassword = ref('')
const isSubmitting = ref(false)

const login = async () => {
  if (isSubmitting.value) return
  isSubmitting.value = true

  try {
    const loginPayload = loginType.value === 'admin' 
      ? { loginId: adminId.value, password: adminPassword.value }
      : { loginId: loginId.value, password: password.value }

    if (!loginPayload.loginId || !loginPayload.password) {
      alert('아이디와 비밀번호를 입력하세요.')
      isSubmitting.value = false
      return
    }

    const response = await axios.post('/api/users/login', loginPayload)

    console.log('로그인 응답 데이터:', response.data)

     if (response.status === 200) {

      // ✅ 로그인 사용자 정보 저장
      localStorage.setItem('userPk', response.data.userPk)
      localStorage.setItem('loginId', response.data.loginId)
      localStorage.setItem('name', response.data.name)
      localStorage.setItem('role', response.data.role)

      alert(loginType.value === 'admin' 
        ? '관리자 로그인 성공!' 
        : `${loginPayload.loginId}님 환영합니다!`
      )

      if (loginType.value === 'admin') {
        router.push('/EvChargingZoneMonitoring')
      } else {
        router.push('/EVUserDashboard')
      }
    }
  } catch (error) {
    // 서버 로그에 찍힌 "비밀번호가 틀렸습니다" 메시지를 알림창에 바로 띄웁니다.
    const errorMsg = error.response?.data?.message || '로그인 실패: 정보를 확인하세요.'
    alert(errorMsg)
    console.error('로그인 에러:', error)
  } finally {
    isSubmitting.value = false
  }
}
</script>

<style scoped>
/* 기존 스타일 그대로 유지 */
.login-page { height: 100vh; display: flex; flex-direction: column; justify-content: center; align-items: center; padding-top: 30px; background: linear-gradient(135deg, #ffffff 0%, #fcfcfc 100%); font-family: 'Helvetica Neue', Arial, sans-serif; }
.main-title { font-size: 32px; font-weight: 800; margin-bottom: 25px; color: #0d2b1f; }
.login-box { width: 360px; padding: 40px; border-radius: 24px; text-align: center; background: rgba(255, 255, 255, 0.1); backdrop-filter: blur(15px); -webkit-backdrop-filter: blur(35px); border: 1px solid rgba(255, 255, 255, 0.8); box-shadow: 0 10px 30px rgba(13, 43, 31, 0.08); color: #0d2b1f; }
.login-box h1 { margin-bottom: 20px; }
.login-tabs { margin-bottom: 25px; }
.login-tabs button { margin: 5px; padding: 8px 16px; border: 1px solid rgba(13, 43, 31, 0.2); border-radius: 20px; cursor: pointer; background: transparent; color: rgba(13, 43, 31, 0.6); transition: all 0.3s ease; }
.login-tabs button.active { background: #0d2b1f; color: #ffffff; font-weight: bold; border-color: #0d2b1f; }
.form-group { display: flex; flex-direction: column; gap: 15px; margin-bottom: 30px; }
.input-wrapper { display: flex; align-items: center; background: #ffffff; border-radius: 12px; border: 1px solid #e1e6e4; padding: 0 15px; box-shadow: 0 2px 5px rgba(0,0,0,0.02); transition: border-color 0.3s; }
.input-wrapper:focus-within { border-color: #0d2b1f; }
.icon { display: flex; align-items: center; justify-content: center; margin-right: 12px; }
.icon svg { fill: #9eaba5; }
input { width: 100%; padding: 14px 0; background: transparent; border: none; color: #0d2b1f; font-weight: 500; font-size: 1rem; outline: none; }
input::placeholder { color: #9eaba5; font-weight: 400; }
.login-btn { width: 100%; padding: 14px; border-radius: 30px; border: none; background: #0d2b1f; color: #ffffff; font-weight: bold; font-size: 1rem; cursor: pointer; box-shadow: 0 4px 15px rgba(13, 43, 31, 0.2); transition: transform 0.2s, background 0.2s; }
.login-btn:hover { background: #091e15; transform: translateY(-2px); }
.login-btn:disabled { background: #9eaba5; cursor: not-allowed; }
.signup { margin-top: 20px; font-size: 0.9rem; color: #0d2b1f; cursor: pointer; text-decoration: underline; font-weight: 500; }
.logo-area { margin-top: 150px; display: flex; gap: 40px; }
.logo-area img { width: 160px; height: 80px; object-fit: contain; }
</style>