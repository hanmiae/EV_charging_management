# 다른 계정으로 재배포 체크리스트

> 대상 스택: **Render(백엔드 + Postgres)** + **Vercel(프론트엔드 Vue)**
> 기준 파일: `render.yaml`, `EV/vercel.json`
> 모든 경로는 이 **repo 루트** (`EV_Project14/EV_Project14/`) 기준입니다.

---

## 0. 준비물

- [ ] 새 **GitHub 계정** (또는 기존 계정의 새 저장소)
- [ ] 새 **Render 계정** (backend + Postgres)
- [ ] 새 **Vercel 계정** (frontend)
- [ ] 로컬에 Git, Node.js 20+, Docker(선택) 설치
- [ ] 기존 Render Postgres 백업 필요 시 `pg_dump` 로 덤프 확보

---

## 1. 소스 코드 새 저장소로 이전

1. [ ] 새 GitHub 계정에서 빈 repo 생성 (예: `ev-charging-system`)
2. [ ] 로컬 repo 루트 (`EV_Project14/EV_Project14/`) 에서 원격 교체 후 push
   ```bash
   git remote remove origin            # 기존 원격 제거
   git remote add origin https://github.com/<새계정>/<repo>.git
   git branch -M main
   git push -u origin main
   ```
3. [ ] `.env`, `.env.local` 은 **절대 커밋 금지** — 두 개의 `.gitignore`
   (`./.gitignore`, `./EV/.gitignore`) 로 이미 막혀 있음 (검증 완료)
4. [ ] 커밋 이력에 비밀번호/JWT 시크릿이 남아있으면 `git filter-repo` 로 제거

---

## 2. Render — 백엔드 재배포

### 2-1. Blueprint 로 일괄 생성 (권장)

- [ ] Render 대시보드 → **New +** → **Blueprint**
- [ ] 새 GitHub 계정 연동 → `ev-charging-system` repo 선택
- [ ] Blueprint 파일 경로: `render.yaml` (repo 루트)
- [ ] 서비스 2개가 함께 생성됨
  - `ev-charging-backend` (Docker web service, Singapore)
  - `ev-charging-db` (Postgres free, Singapore)

### 2-2. 환경변수 확인 / 추가 (백엔드)

`render.yaml` 에 이미 선언된 항목은 자동 주입됨. 추가로 수동 설정해야 하는 값:

| Key | 값 | 메모 |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | `prod` | 운영 프로파일 |
| `JWT_SECRET` | `openssl rand -base64 48` 결과 | **32자 이상 필수** |
| `JWT_EXPIRATION_MS` | `86400000` | 24h |
| `APP_CORS_ALLOWED_ORIGINS` | (Vercel 도메인 확정 후 입력) | 쉼표 구분, preview 도메인도 포함 |

자동 주입됨 (`render.yaml`):
- `PGHOST`, `PGPORT`, `PGDATABASE`, `PGUSER`, `PGPASSWORD`
- `EV_PLATE_LOGS_DIR=/tmp/plate_logs`
- `SPRING_JPA_HIBERNATE_DDL_AUTO=update`

### 2-3. 배포 후 체크

- [ ] `https://<새서비스이름>.onrender.com/api/dashboard` → 200 응답
- [ ] Render 로그에서 Hibernate 가 **6개 테이블 생성** 확인
  (User, Vehicle, ChargingStation, ChargingHistory, ChargingQueue, DetectionLog)
- [ ] **새 백엔드 URL 기록**: `https://________________.onrender.com`

### 2-4. (선택) 기존 DB 데이터 이관

```bash
# 기존 DB 덤프
pg_dump "postgres://<old_user>:<old_pw>@<old_host>/<old_db>" \
  --no-owner --no-acl -f dump.sql

# 새 DB 에 복원
psql "postgres://<new_user>:<new_pw>@<new_host>/<new_db>" -f dump.sql
```

> ⚠️ Render Free Postgres 는 **생성 후 90일 만료**. 만료 전 유료 전환 또는 재덤프 필요.

---

## 3. Vercel — 프론트엔드 재배포

### 3-1. 프로젝트 생성

- [ ] Vercel 대시보드 → **Add New → Project**
- [ ] 새 GitHub repo 선택 → **Root Directory**: `EV`
- [ ] Framework Preset: **Vite** (자동 감지됨)
- [ ] Build Command / Output 은 `EV/vercel.json` 값 따름 (`npm run build` / `dist`)

### 3-2. `EV/vercel.json` 의 백엔드 URL 교체 ⚠️ **필수**

현재 하드코딩 (2곳):
```json
"destination": "https://ev-charging-backend-5yw3.onrender.com/api/$1"
"destination": "https://ev-charging-backend-5yw3.onrender.com/images/$1"
```

- [ ] `EV/vercel.json` 에서 위 두 URL 을 새 Render URL 로 변경 후 커밋
- [ ] push 하면 Vercel 이 자동 재배포

### 3-3. 환경변수 (Vercel Project Settings → Environment Variables)

| Key | 값 | 메모 |
|---|---|---|
| `VITE_API_URL` | `https://<새render백엔드>.onrender.com` | 빌드 시 치환됨 |
| `VITE_STREAM_A01` ~ `B02` | (공개 MJPEG 엔드포인트 있을 때만) | 비워두면 플레이스홀더 |
| `VITE_USE_MOCK` | `false` (실 API 사용 시) | 포트폴리오 데모면 `true` |

### 3-4. 배포 후 체크

- [ ] Vercel 기본 도메인 확인: `https://<project>.vercel.app`
- [ ] 로그인 페이지 로드 → 네트워크 탭에서 `/api/*` 요청이 새 Render 백엔드로 프록시되는지 확인
- [ ] `/images/*` (plate_logs 이미지) 도 정상 응답 확인

---

## 4. CORS 왕복 마무리

1. [ ] Vercel 에서 확정된 도메인들을 수집
   - 프로덕션: `https://<project>.vercel.app`
   - Preview: `https://<project>-git-main-<team>.vercel.app` 등
   - (커스텀 도메인 쓰면 그것도 포함)
2. [ ] Render 백엔드 `APP_CORS_ALLOWED_ORIGINS` 에 **쉼표 구분**으로 전부 추가
   ```
   https://<project>.vercel.app,https://<project>-git-main-<team>.vercel.app
   ```
3. [ ] Render 재배포 후 브라우저 콘솔에서 CORS 에러 없는지 확인

---

## 5. 최종 스모크 테스트

- [ ] 로그인 / 회원가입 동작
- [ ] `/EvChargingZoneMonitoring` 충전존 모니터링 로드
- [ ] `/EVVideoBoard` CCTV 보드 (스트림 URL 없으면 플레이스홀더 정상)
- [ ] `/EVUserDashboard` 유저 대시보드 차트 렌더
- [ ] `/EvDatabaseUsage` DB 사용량 페이지 차트 렌더
- [ ] 브라우저 DevTools → Console / Network 에 4xx·5xx·CORS 에러 없음

---

## 6. 교체가 필요한 하드코딩 지점 요약

| 파일 | 변경 내용 |
|---|---|
| `EV/vercel.json` | `ev-charging-backend-5yw3.onrender.com` → 새 Render URL (2곳) |
| Render 환경변수 `APP_CORS_ALLOWED_ORIGINS` | Vercel 새 도메인으로 교체 |
| Render 환경변수 `JWT_SECRET` | 새로 발급 (재사용 금지) |
| Vercel 환경변수 `VITE_API_URL` | 새 Render URL |
| (선택) `EV/.env.local` | 로컬 데모용이면 그대로 둬도 됨 |

---

## 7. 절대 커밋하면 안 되는 파일

- `../../.env` (상위 폴더의 루트 env — docker-compose 용)
- `EV/.env.local`
- `Springboot/ev_charging_system1/src/main/resources/application.properties` 의 실제 운영 DB 비밀번호
- Render/Vercel 대시보드에서 발급받은 **토큰·시크릿**

커밋 전 `git status` 로 항상 확인.

---

## 8. 롤백 플랜

- 구 Render 서비스는 **새 배포 검증 완료 전까지 삭제 금지**
- Vercel 도 구 프로젝트 유지 → 새 프로젝트 검증 후 DNS/도메인만 스위치
- 문제 생기면 `EV/vercel.json` 의 백엔드 URL 만 원복해서 구 백엔드로 포인트 가능
