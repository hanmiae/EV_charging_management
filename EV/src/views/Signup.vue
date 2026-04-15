<template>
  <div class="signup-page">
    <h1 class="main-title">전기차충전구역 관리 시스템</h1>

    <div class="signup-box glass-card">
      <h2 class="title">회원가입</h2>

      <div class="form-group">
        <div class="input-wrapper">
          <span class="icon">
            <svg viewBox="0 0 24 24" width="22" height="22"><path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/></svg>
          </span>
          <input v-model="loginId" placeholder="아이디">
        </div>

        <div class="input-wrapper">
          <span class="icon">
            <svg viewBox="0 0 24 24" width="22" height="22"><path d="M18 8h-1V6c0-2.76-2.24-5-5-5S7 3.24 7 6v2H6c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V10c0-1.1-.9-2-2-2zM9 6c0-1.66 1.34-3 3-3s3 1.34 3 3v2H9V6zm9 14H6V10h12v10zm-6-3c1.1 0 2-.9 2-2s-.9-2-2-2-2 .9-2 2 .9 2 2 2z"/></svg>
          </span>
          <input v-model="password" type="password" placeholder="비밀번호">
        </div>

        <div class="input-wrapper">
          <span class="icon">
            <svg viewBox="0 0 24 24" width="22" height="22"><path d="M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-7 3c1.93 0 3.5 1.57 3.5 3.5S13.93 13 12 13s-3.5-1.57-3.5-3.5S10.07 6 12 6zm5 11H7v-1.5c0-1.67 3.33-2.5 5-2.5s5 .83 5 2.5V17z"/></svg>
          </span>
          <input v-model="name" placeholder="이름">
        </div>

        <div class="input-wrapper">
          <span class="icon">
            <svg viewBox="0 0 24 24" width="22" height="22"><path d="M6.62 10.79a15.153 15.153 0 0 0 6.59 6.59l2.2-2.2c.27-.27.67-.36 1.02-.24 1.12.37 2.33.57 3.57.57.55 0 1 .45 1 1V20c0 .55-.45 1-1 1-9.39 0-17-7.61-17-17 0-.55.45-1 1-1h3.5c.55 0 1 .45 1 1 0 1.25.2 2.45.57 3.57.11.35.03.74-.25 1.02l-2.2 2.2z"/></svg>
          </span>
          <input v-model="phone" placeholder="전화번호">
        </div>

        <div class="row">
          <div class="input-wrapper select-wrap">
            <span class="icon">
              <svg viewBox="0 0 24 24" width="22" height="22"><path d="M12 3L2 12h3v8h6v-6h2v6h6v-8h3L12 3zm5 15h-2v-6H9v6H7v-7.81l5-4.5 5 4.5V18z"/></svg>
            </span>
            <select v-model="building">
              <option value="" disabled selected>동 선택</option>
              <option value="A">A</option>
              <option value="B">B</option>
             
            </select>
          </div>
          <div class="input-wrapper unit-wrap">
            <input v-model="unit" placeholder="호수">
          </div>
        </div>

        <div class="input-wrapper">
          <span class="icon">
            <svg viewBox="0 0 24 24" width="22" height="22"><path d="M18.92 6.01C18.72 5.42 18.16 5 17.5 5h-11c-.66 0-1.21.42-1.42 1.01L3 12v8c0 .55.45 1 1 1h1c.55 0 1-.45 1-1v-1h12v1c0 .55.45 1 1 1h1c.55 0 1-.45 1-1v-8l-2.08-5.99zM6.85 7h10.29l1.08 3.11H5.77L6.85 7zM19 17H5v-5h14v5z"/><circle cx="7.5" cy="14.5" r="1.5"/><circle cx="16.5" cy="14.5" r="1.5"/></svg>
          </span>
          <input v-model="plateNumber" placeholder="차량번호">
        </div>

        <div class="input-wrapper">
          <span class="icon">
            <svg viewBox="0 0 24 24" width="22" height="22"><path d="M7 2v11h3v9l7-12h-4l4-8z"/></svg>
          </span>
          <select v-model="vehicleType">
            <option value="" disabled selected>차량종류 선택</option>
            <option value="EV">EV</option>
            <option value="NORMAL">NORMAL</option>
          </select>
        </div>
      </div>

      <button class="signup-btn" @click="signup">
        가입하기
      </button>

      <p class="back" @click="$router.push('/')">
        로그인으로 돌아가기
      </p>
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

const loginId = ref('')
const password = ref('')
const name = ref('')
const phone = ref('')
const building = ref('')
const unit = ref('')
const plateNumber = ref('')
const vehicleType = ref('')

const signup = async () => {
  if (!loginId.value || !password.value || !name.value) {
    alert("아이디, 비밀번호, 이름은 필수 입력 사항입니다.")
    return
  }

  try {
    const response = await axios.post('/api/users/signup?v=1', {
      loginId: loginId.value,
      password: password.value,
      name: name.value,
      phone: phone.value,
      building: building.value,
      unit: unit.value,
      plateNumber: plateNumber.value,
      vehicleType: vehicleType.value
    })

    if (response.status >= 200 && response.status < 300) {
      alert("회원가입이 완료되었습니다! 로그인 페이지로 이동합니다.")
      router.push('/')
    }
  } catch (error) {
    const errorMsg = error.response?.data?.message || "양식을 확인해주세요.";
    alert("회원가입 실패: " + errorMsg);
  }
}
</script>

<style scoped>
/* 로그인 페이지 배경 스타일과 동일하게 설정 */
.signup-page { 
  height: 100vh; 
  display: flex; 
  flex-direction: column; 
  justify-content: center; 
  align-items: center; 
  padding-top: 5px; 
  background: linear-gradient(135deg, #ffffff 0%, #fcfcfc 100%); 
  font-family: 'Helvetica Neue', Arial, sans-serif; 
}

.main-title { 
  font-size: 32px; 
  font-weight: 800; 
  margin-bottom: 25px; 
  color: #0d2b1f; 
}

/* 로그인 박스와 동일한 디자인 */
.signup-box { 
  width: 380px; 
  padding: 40px; 
  border-radius: 24px; 
  text-align: center; 
  background: rgba(255, 255, 255, 0.1); 
  backdrop-filter: blur(15px); 
  -webkit-backdrop-filter: blur(35px); 
  border: 1px solid rgba(255, 255, 255, 0.8); 
  box-shadow: 0 10px 30px rgba(13, 43, 31, 0.08); 
  color: #0d2b1f; 
}

.title {
  font-size: 1.5rem;
  font-weight: 700;
  margin-bottom: 25px;
}

.form-group { 
  display: flex; 
  flex-direction: column; 
  gap: 12px; 
  margin-bottom: 25px; 
}

/* 하얀색 둥근 입력창 박스 */
.input-wrapper { 
  display: flex; 
  align-items: center; 
  background: #ffffff; 
  border-radius: 12px; 
  border: 1px solid #e1e6e4; 
  padding: 0 15px; 
  box-shadow: 0 2px 5px rgba(0,0,0,0.02); 
  transition: border-color 0.3s; 
}

.input-wrapper:focus-within { 
  border-color: #0d2b1f; 
}

.icon { 
  display: flex; 
  align-items: center; 
  justify-content: center; 
  margin-right: 12px; 
}

.icon svg { 
  fill: #9eaba5; 
}

input, select { 
  width: 100%; 
  padding: 12px 0; 
  background: transparent; 
  border: none; 
  color: #0d2b1f; 
  font-weight: 500; 
  font-size: 1rem; 
  outline: none; 
}

input::placeholder { 
  color: #9eaba5; 
  font-weight: 400; 
}

.row {
  display: flex;
  gap: 10px;
}
.row .select-wrap { flex: 6; }
.row .unit-wrap { flex: 4; }

/* 버튼 디자인 */
.signup-btn { 
  width: 100%; 
  padding: 14px; 
  border-radius: 30px; 
  border: none; 
  background: #0d2b1f; 
  color: #ffffff; 
  font-weight: bold; 
  font-size: 1rem; 
  cursor: pointer; 
  box-shadow: 0 4px 15px rgba(13, 43, 31, 0.2); 
  transition: transform 0.2s, background 0.2s; 
}

.signup-btn:hover { 
  background: #091e15; 
  transform: translateY(-2px); 
}

.back { 
  margin-top: 20px; 
  font-size: 0.9rem; 
  color: #0d2b1f; 
  cursor: pointer; 
  text-decoration: underline; 
  font-weight: 500; 
}

/* 로고 영역 */
.logo-area { 
  margin-top: 18px; 
  display: flex; 
  gap: 40px; 
}

.logo-area img { 
  width: 160px; 
  height: 80px; 
  object-fit: contain; 
}
</style>