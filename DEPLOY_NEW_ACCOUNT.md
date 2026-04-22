# 📦 EV 충전소 관리 시스템 — 한미애 계정 배포 가이드

> 이 문서는 **팀장(한미애) 본인이 직접 배포**하기 위한 단계별 체크리스트입니다.
> > Render(백엔드) + Vercel(프론트엔드) **무료 플랜** 기준입니다.
> >
> > ---
> >
> > ## 📌 프로젝트 구조
> >
> > | 영역 | 경로 | 스택 | 배포 플랫폼 |
> > |------|------|------|------------|
> > | 프론트엔드 | `EV/` | Vue 3 + Vite | Vercel |
> > | 백엔드 | `Springboot/ev_charging_system1/` | Spring Boot + PostgreSQL | Render (Docker) |
> > | 블루프린트 | `render.yaml` (루트), `EV/vercel.json` | - | - |
> >
> > ---
> >
> > ## ⏱️ 예상 소요 시간
> >
> > - Render 배포: 약 15~20분 (무료 빌드 속도)
> > - - Vercel 배포: 약 3~5분
> >   - - CORS 왕복 + 스모크 테스트: 약 10분
> >     - - **총 30~40분**
> >      
> >       - ---
> >
> > ## ✅ 사전 준비 체크리스트
> >
> > - [ ] `hanmiae` GitHub 계정에 레포 존재 확인
> > - [ ]   - URL: `https://github.com/hanmiae/EV_charging_management`
> > - [ ]     - PR #1 merge 완료 여부 확인 (배포 파일 `render.yaml`, `vercel.json`이 merge되어야 함)
> > - [ ] - [ ] Render 계정 (무료) — GitHub 로그인으로 가입 가능
> > - [ ] - [ ] Vercel 계정 (무료/Hobby) — GitHub 로그인으로 가입 가능
> > - [ ] - [ ] 로컬 터미널 사용 가능 (JWT_SECRET 생성용)
> >
> > - [ ] ---
> >
> > - [ ] ## 🔑 JWT_SECRET 생성 (가장 먼저)
> >
> > - [ ] **중요:** JWT_SECRET은 외부에 절대 노출되면 안 됩니다. 채팅/이메일/스크린샷에 포함 금지.
> >
> > - [ ] ### macOS / Linux
> > - [ ] ```bash
> > - [ ] openssl rand -base64 48
> > - [ ] ```
> >
> > - [ ] ### Windows (PowerShell)
> > - [ ] ```powershell
> > - [ ] [Convert]::ToBase64String((1..48 | ForEach-Object { Get-Random -Maximum 256 }))
> > - [ ] ```
> >
> > - [ ] ### 생성 후
> > - [ ] - 결과물 (약 64자 문자열)을 **본인만 볼 수 있는 곳**에 임시 저장
> > - [ ] - 예: 메모장(저장하지 않음), 1Password/Bitwarden 등 패스워드 매니저
> > - [ ] - **Render 환경변수 `JWT_SECRET`에 붙여넣을 때만 사용**
> >
> > - [ ] ---
> >
> > - [ ] ## 🟪 STEP 1 — Render 백엔드 배포
> >
> > - [ ] ### 1-1. Render 로그인 & Blueprint 생성
> >
> > - [ ] 1. [https://dashboard.render.com](https://dashboard.render.com) 접속
> > - [ ] 2. **한미애 GitHub 계정**으로 로그인 (처음이면 회원가입)
> > - [ ] 3. 우측 상단 **`New +`** → **`Blueprint`** 선택
> > - [ ] 4. **Connect a repository** 단계:
> > - [ ]    - GitHub 연동 권한 승인 (첫 사용 시 팝업)
> > - [ ]       - 레포 목록에서 **`hanmiae/EV_charging_management`** 선택
> > - [ ]   5. Blueprint 파일 경로: **`render.yaml`** (자동 감지됨)
> > - [ ]   6. **Apply** 클릭
> >
> > - [ ]   ### 1-2. 자동 생성되는 서비스 확인
> >
> > - [ ]   `render.yaml`에 의해 다음 2개 서비스가 자동 생성됩니다:
> >
> > - [ ]   | 서비스명 | 타입 | 플랜 | 리전 |
> > - [ ]   |---------|------|------|------|
> > - [ ]   | `ev-charging-backend` | Docker Web Service | Free | Singapore |
> > - [ ]   | `ev-charging-db` | PostgreSQL | Free | Singapore |
> >
> > - [ ]   ⚠️ **Free 플랜 주의사항:**
> > - [ ]   - 15분 미사용 시 자동 슬립 (첫 요청 시 30~60초 지연)
> > - [ ]   - Postgres Free: **90일 후 자동 삭제** → 정기 백업 필요
> > - [ ]   - 월 750시간 무료
> >
> > - [ ]   ### 1-3. 환경변수 추가 (백엔드 서비스)
> >
> > - [ ]   `ev-charging-backend` 서비스 → **Environment** 탭에서 다음 값을 **수동 추가**:
> >
> > - [ ]   | Key | 값 | 비고 |
> > - [ ]   |-----|---|------|
> > - [ ]   | `SPRING_PROFILES_ACTIVE` | `prod` | 운영 프로파일 |
> > - [ ]   | `JWT_SECRET` | (위에서 생성한 48자 base64) | **외부 노출 금지** |
> > - [ ]   | `JWT_EXPIRATION_MS` | `86400000` | 24시간 |
> > - [ ]   | `APP_CORS_ALLOWED_ORIGINS` | (STEP 2 완료 후 Vercel 도메인으로 채움) | 일단 비워두거나 `*` 임시 사용 |
> >
> > - [ ]   ⚠️ **DB 관련 환경변수** (`SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`)는 `render.yaml`에서 **자동 주입**되므로 수동 추가하지 않습니다.
> >
> > - [ ]   ### 1-4. 배포 & 헬스체크
> >
> > - [ ]   1. **Manual Deploy** → **Deploy latest commit** 클릭
> > - [ ]   2. **Logs** 탭에서 빌드 진행 확인 (10~15분 소요)
> > - [ ]   3. 빌드 완료 후 상단 URL 복사 (예: `https://ev-charging-backend-xxxx.onrender.com`)
> > - [ ]   4. 터미널에서 헬스체크:
> > - [ ]      ```bash
> > - [ ]     curl https://ev-charging-backend-xxxx.onrender.com/api/dashboard
> > - [ ]    ```
> > - [ ]       → HTTP 200 또는 JSON 응답이면 정상
> >
> > - [ ]   ### 1-5. Render URL 기록
> >
> > - [ ]   - **Render 백엔드 URL:** `https://___________________________.onrender.com`
> > - [ ]     (이 값을 STEP 2에서 사용)
> >
> > - [ ] ---
> >
> > - [ ] ## 🟦 STEP 2 — Vercel 프론트엔드 배포
> >
> > - [ ] ### 2-1. vercel.json 백엔드 URL 업데이트
> >
> > - [ ] 로컬에서 `EV/vercel.json` 파일을 열어 **2곳**의 하드코딩된 URL을 STEP 1의 새 Render URL로 교체:
> >
> > - [ ] **변경 전:**
> > - [ ] ```json
> > - [ ] "destination": "https://ev-charging-backend-5yw3.onrender.com/api/$1"
> > - [ ] "destination": "https://ev-charging-backend-5yw3.onrender.com/images/$1"
> > - [ ] ```
> >
> > - [ ] **변경 후:**
> > - [ ] ```json
> > - [ ] "destination": "https://<새Render URL>/api/$1"
> > - [ ] "destination": "https://<새Render URL>/images/$1"
> > - [ ] ```
> >
> > - [ ] 커밋 & push:
> > - [ ] ```bash
> > - [ ] git add EV/vercel.json
> > - [ ] git commit -m "chore: update backend URL in vercel.json for new deployment"
> > - [ ] git push origin main
> > - [ ] ```
> >
> > - [ ] ### 2-2. Vercel 프로젝트 생성
> >
> > - [ ] 1. [https://vercel.com](https://vercel.com) 접속 → 한미애 GitHub 계정으로 로그인
> > - [ ] 2. **Add New...** → **Project** 클릭
> > - [ ] 3. GitHub 연동 → **`hanmiae/EV_charging_management`** 레포 Import
> > - [ ] 4. **Configure Project** 설정:
> >
> > - [ ] | 항목 | 값 |
> > - [ ] |------|---|
> > - [ ] | Framework Preset | **Vite** (자동 감지) |
> > - [ ] | Root Directory | **`EV`** ← 반드시 지정 |
> > - [ ] | Build Command | `npm run build` (기본값) |
> > - [ ] | Output Directory | `dist` (기본값) |
> > - [ ] | Install Command | `npm install` (기본값) |
> >
> > - [ ] ### 2-3. 환경변수 설정
> >
> > - [ ] **Environment Variables** 섹션에서 추가:
> >
> > - [ ] | Key | 값 | 환경 |
> > - [ ] |-----|---|------|
> > - [ ] | `VITE_API_URL` | `https://<새 Render URL>` | Production, Preview, Development |
> > - [ ] | `VITE_USE_MOCK` | `false` | Production |
> > - [ ] | `VITE_STREAM_A01` | (MJPEG URL 있으면 입력, 없으면 공란) | Production |
> > - [ ] | `VITE_STREAM_A02` | (동일) | Production |
> > - [ ] | `VITE_STREAM_B01` | (동일) | Production |
> > - [ ] | `VITE_STREAM_B02` | (동일) | Production |
> >
> > - [ ] ### 2-4. 배포 실행
> >
> > - [ ] 1. **Deploy** 버튼 클릭
> > - [ ] 2. 3~5분 대기
> > - [ ] 3. 배포 완료 후 도메인 확인:
> > - [ ]    - Production: `https://<project-name>.vercel.app`
> > - [ ]       - Preview: `https://<project-name>-git-main-<team>.vercel.app`
> >
> > - [ ]   ### 2-5. Vercel URL 기록
> >
> > - [ ]   - **Vercel 프론트엔드 URL:** `https://___________________________.vercel.app`
> >
> > - [ ]   ---
> >
> > - [ ]   ## 🟩 STEP 3 — CORS 마무리 (매우 중요)
> >
> > - [ ]   STEP 2에서 확보한 Vercel 도메인을 Render 백엔드 CORS 화이트리스트에 등록합니다.
> >
> > - [ ]   ### 3-1. Vercel 도메인 목록 확보
> >
> > - [ ]   Vercel 대시보드 → 프로젝트 → **Settings** → **Domains** 에서 모든 도메인 복사:
> > - [ ]   - `https://<project>.vercel.app` (Production)
> > - [ ]   - `https://<project>-git-main-<team>.vercel.app` (Git Preview)
> >
> > - [ ]   ### 3-2. Render 환경변수 업데이트
> >
> > - [ ]   1. Render 대시보드 → `ev-charging-backend` → **Environment** 탭
> > - [ ]   2. `APP_CORS_ALLOWED_ORIGINS` 값을 다음과 같이 **콤마로 구분**:
> > - [ ]      ```
> > - [ ]     https://<project>.vercel.app,https://<project>-git-main-<team>.vercel.app
> > - [ ]    ```
> > - [ ]    3. **Save Changes** 클릭 → 자동 재배포 시작
> > - [ ]    4. 재배포 완료 대기 (약 5~10분)
> >
> > - [ ]    ---
> >
> > - [ ]    ## 🟨 STEP 4 — 스모크 테스트
> >
> > - [ ]    ### 4-1. 페이지별 동작 확인
> >
> > - [ ]    브라우저(Chrome 권장)로 Vercel URL 접속 후 다음 페이지 확인:
> >
> > - [ ]    | 경로 | 기대 동작 | Pass/Fail |
> > - [ ]    |------|----------|-----------|
> > - [ ]    | `/` | Login 페이지 정상 표시 | ☐ |
> > - [ ]    | `/signup` | 회원가입 폼 정상 | ☐ |
> > - [ ]    | `/EvChargingZoneMonitoring` | 충전구역 모니터링 로드 | ☐ |
> > - [ ]    | `/EVVideoBoard` | 영상 보드 로드 (스트림 URL 설정 시) | ☐ |
> > - [ ]    | `/EVUserDashboard` | 유저 대시보드 로드 | ☐ |
> > - [ ]    | `/EvDatabaseUsage` | DB 사용량 페이지 로드 | ☐ |
> >
> > - [ ]    ### 4-2. DevTools 확인
> >
> > - [ ]    각 페이지에서 `F12` → **Console** / **Network** 탭:
> > - [ ]    - [ ] **Console:** 빨간색 에러 없음 (CORS 에러 주의)
> > - [ ]    - [ ] **Network:** 모든 API 요청이 200/304 (4xx/5xx 없음)
> > - [ ]    - [ ] **Network:** API 요청 URL이 새 Render URL로 가고 있는지 확인
> >
> > - [ ]    ### 4-3. 주요 기능 확인
> >
> > - [ ]    - [ ] 회원가입 → 로그인 플로우 정상
> > - [ ]    - [ ] 로그인 후 JWT 토큰 발급 (Network 탭 `/api/auth/login` 응답 확인)
> > - [ ]    - [ ] 대시보드 데이터 API 호출 정상
> >
> > - [ ]    ---
> >
> > - [ ]    ## 🚨 문제 해결 (Troubleshooting)
> >
> > - [ ]    ### Render 백엔드가 500 에러
> >
> > - [ ]    - Logs 확인 → DB 연결 실패가 대부분
> > - [ ]    - `SPRING_DATASOURCE_URL` 자동 주입 확인 (render.yaml 참조)
> > - [ ]    - Postgres 서비스가 Available 상태인지 확인
> >
> > - [ ]    ### Vercel에서 API 호출 시 CORS 에러
> >
> > - [ ]    - STEP 3의 `APP_CORS_ALLOWED_ORIGINS` 값 확인
> > - [ ]    - **반드시 `https://` 포함, 끝에 `/` 없이** 입력
> > - [ ]    - 콤마 앞뒤 공백 없음
> > - [ ]    - Render 재배포 완료 여부 확인
> >
> > - [ ]    ### Render 서비스가 느림 (첫 요청 30초 이상)
> >
> > - [ ]    - Free 플랜 자동 슬립 특성 (정상)
> > - [ ]    - 유료 플랜($7/월) 전환 시 상시 가동
> > - [ ]    - 또는 Cron-job.org 등으로 14분마다 ping 처리 (워크어라운드)
> >
> > - [ ]    ### Postgres 데이터 날아감
> >
> > - [ ]    - Free 플랜은 **90일 후 DB 자동 삭제**
> > - [ ]    - 정기 `pg_dump` 백업 필수
> > - [ ]    - 장기 운영 시 유료 플랜 또는 외부 DB(Supabase 등) 전환
> >
> > - [ ]    ---
> >
> > - [ ]    ## 🚫 절대 금지 사항
> >
> > - [ ]    - ❌ `.env`, `.env.local`, `application.properties` (실운영 비밀번호 포함) **커밋 금지**
> > - [ ]    - ❌ `JWT_SECRET`, DB 비밀번호, Vercel 토큰 **채팅/로그/스크린샷에 노출 금지**
> > - [ ]    - ❌ 구 Render/Vercel 서비스는 STEP 4 검증 통과 **전 삭제 금지** (롤백용)
> > - [ ]    - ❌ `APP_CORS_ALLOWED_ORIGINS`에 `*` **운영 환경 사용 금지** (테스트 시에만 임시 허용)
> >
> > - [ ]    ---
> >
> > - [ ]    ## 📋 최종 보고서 양식
> >
> > - [ ]    배포 완료 후 다음 정보를 팀에 공유:
> >
> > - [ ]    ```
> > - [ ]    ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
> > - [ ]    📦 EV 충전소 관리 시스템 배포 완료 보고
> > - [ ]    ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
> >
> > - [ ]    🟪 Render 백엔드
> > - [ ]      URL: https://_________________________.onrender.com
> > - [ ]    헬스체크: /api/dashboard → 200 OK
> > - [ ]      리전: Singapore / Free Plan
> >
> > - [ ]  🟦 Vercel 프론트엔드
> > - [ ]    Production: https://_________________________.vercel.app
> > - [ ]      Preview:    https://_________________________-git-main-xxx.vercel.app
> >
> > - [ ]  🟩 CORS 화이트리스트
> > - [ ]    https://____.vercel.app
> > - [ ]      https://____-git-main-xxx.vercel.app
> >
> > - [ ]  🟨 스모크 테스트 결과
> > - [ ]    [ ] / (Login)                      Pass / Fail
> > - [ ]      [ ] /signup                        Pass / Fail
> > - [ ]    [ ] /EvChargingZoneMonitoring      Pass / Fail
> > - [ ]      [ ] /EVVideoBoard                  Pass / Fail
> > - [ ]    [ ] /EVUserDashboard               Pass / Fail
> > - [ ]      [ ] /EvDatabaseUsage               Pass / Fail
> >
> > - [ ]    Console 에러: 없음 / 있음 (상세: _______)
> > - [ ]      Network 에러: 없음 / 있음 (상세: _______)
> >
> > - [ ]  배포 일시: YYYY-MM-DD HH:MM
> > - [ ]  배포자: hanmiae
> > - [ ]  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
> > - [ ]  ```
> >
> > - [ ]  ---
> >
> > - [ ]  ## 📞 문의
> >
> > - [ ]  - 배포 중 막히는 부분 있으면 팀 채널에 스크린샷 + 에러 메시지 공유
> > - [ ]  - JWT_SECRET 등 **민감 정보는 절대 공유 금지** (DM으로도 금지)
> >
> > - [ ]  ---
> >
> > - [ ]  _Last updated: 2026-04-22_
> > - [ ]  _작성: rhlfur2055-prog (팀원)_
> > - [ ]  _대상: hanmiae (팀장)_
> > - [ ]  
