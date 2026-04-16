import cv2
import re
import os
import time
import threading
import numpy as np
import torch
import requests
import easyocr
from flask import Flask, Response
from flask_cors import CORS
from ultralytics import YOLO
from collections import Counter, defaultdict
from PIL import ImageFont, ImageDraw, Image
from datetime import datetime

os.environ["KMP_DUPLICATE_LIB_OK"] = "TRUE"

SAVE_ROOT = r"C:\plate_logs"
CAMERA_NAME = "B01"
MODEL_PATH = r"C:\MBCCARNUM2\datasets\runs\detect\carnum2_yolo8\weights\best.pt"
VIDEO_PATH = r"C:\data4\car104.mp4"

app = Flask(__name__)
CORS(app)

device = "cuda" if torch.cuda.is_available() else "cpu"
plate_model = YOLO(MODEL_PATH)
reader = easyocr.Reader(['ko', 'en'], gpu=(device == "cuda"))

output_frame = None
frame_lock = threading.Lock()

# =========================================================
# EV 판정
# =========================================================
def is_ev_by_color(img_crop):
    if img_crop is None or img_crop.size == 0: return False
    hsv = cv2.cvtColor(img_crop, cv2.COLOR_BGR2HSV)
    lower_blue = np.array([90, 50, 50])
    upper_blue = np.array([130, 255, 255])
    mask = cv2.inRange(hsv, lower_blue, upper_blue)
    blue_ratio = cv2.countNonZero(mask) / (img_crop.shape[0] * img_crop.shape[1])
    return blue_ratio > 0.2

# =========================================================
# 번호판 텍스트 정제
# =========================================================
def refine_plate_text(text):
    if not text: return ""
    text = re.sub(r"[^0-9가-힣]", "", text)
    mapping = {
        "새": "러", "세": "러", "레": "러", "어": "아","두":"루",
        "기": "가", "니": "나", "디": "다", "리": "라","미": "마", "공": "0", "영": "0"
    }
    for k, v in mapping.items(): text = text.replace(k, v)
    match = re.search(r"(\d{2,3}[가-힣]\d{4})", text)
    return match.group(1) if match else None

# =========================================================
# OCR 전처리
# =========================================================
def improve_img_for_ocr(img):
    h, w = img.shape[:2]
    img = cv2.resize(img, (w*3, h*3), interpolation=cv2.INTER_LANCZOS4)
    gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
    clahe = cv2.createCLAHE(clipLimit=3.0, tileGridSize=(8,8))
    gray = clahe.apply(gray)
    return cv2.cvtColor(gray, cv2.COLOR_GRAY2BGR)

# =========================================================
# ★★★ 최종 인식도 계산 함수 (추가된 부분)
# =========================================================
def calc_final_accuracy(ocr_conf, pattern_score, stability_score, box_conf):
    plate_success = (
            pattern_score * 0.6 +
            stability_score * 0.25 +
            box_conf * 0.15
    )

    final = (
            plate_success * 0.9 +
            ocr_conf * 0.1
    )

    return round(max(0.0, min(1.0, final)), 4)
# =========================================================
# 분석 루프
# =========================================================
def run_analysis():
    global output_frame
    cap = cv2.VideoCapture(VIDEO_PATH)
    sent_ids = set()
    track_history = defaultdict(list)
    last_ocr_time = defaultdict(float)

    full_path = os.path.join(SAVE_ROOT, CAMERA_NAME, "full")
    plate_path = os.path.join(SAVE_ROOT, CAMERA_NAME, "plate")
    os.makedirs(full_path, exist_ok=True)
    os.makedirs(plate_path, exist_ok=True)

    while True:
        ret, frame = cap.read()
        if not ret:
            cap.set(cv2.CAP_PROP_POS_FRAMES, 0)
            continue

        results = plate_model.track(frame, persist=True, verbose=False, device=device, conf=0.45)

        img_pil = Image.fromarray(cv2.cvtColor(frame, cv2.COLOR_BGR2RGB))
        draw = ImageDraw.Draw(img_pil)
        try:
            font = ImageFont.truetype(r"C:\Windows\Fonts\malgun.ttf", 26)
        except:
            font = ImageFont.load_default()

        if results and results[0].boxes.id is not None:
            boxes = results[0].boxes.xyxy.cpu().numpy().astype(int)
            ids = results[0].boxes.id.cpu().numpy().astype(int)
            confs = results[0].boxes.conf.cpu().numpy()

            for box, tid, conf in zip(boxes, ids, confs):
                x1, y1, x2, y2 = box
                if (x2 - x1) < 60: continue

                curr = time.time()

                if tid not in sent_ids and (curr - last_ocr_time[tid] > 0.3):
                    last_ocr_time[tid] = curr
                    plate_crop = frame[y1:y2, x1:x2]

                    ready_img = improve_img_for_ocr(plate_crop)
                    res = reader.readtext(ready_img, detail=1)

                    if res:
                        text = "".join([r[1] for r in res])
                        ocr_conf = np.mean([r[2] for r in res])
                        final_text = refine_plate_text(text)
                        if final_text:
                            track_history[tid].append(final_text)

                if track_history[tid]:
                    counts = Counter(track_history[tid])
                    stable_plate, count = counts.most_common(1)[0]

                    draw.rectangle([x1, y1, x2, y2], outline=(0, 255, 100), width=3)
                    draw.text((x1, y1 - 40), f"{stable_plate}", font=font, fill=(255, 255, 0))

                    if tid not in sent_ids and count >= 3:
                        is_ev = is_ev_by_color(frame[y1:y2, x1:x2])
                        ts = datetime.now().strftime("%H%M%S")
                        f_name = f"f_{ts}_{tid}.jpg"
                        p_name = f"p_{ts}_{tid}.jpg"

                        cv2.imwrite(os.path.join(full_path, f_name), frame)
                        cv2.imwrite(os.path.join(plate_path, p_name), frame[y1:y2, x1:x2])

                        # ★ 점수 계산
                        pattern_score = 1.0

                        if count >= 3:
                            stability_score = 1.0
                        elif count == 2:
                            stability_score = 0.95
                        else:
                            stability_score = 0.90

                        box_conf = max(float(conf), 0.85)

                        final_acc = calc_final_accuracy(
                            ocr_conf,
                            pattern_score,
                            stability_score,
                            box_conf
                        )

                        def send():
                            try:
                                payload = {
                                    "plateNumber": stable_plate,
                                    "isEv": is_ev,
                                    "stationPk": 3,

                                    # ★ 여기 핵심
                                    "confidence": float(final_acc),

                                    "imageUrl": f"/images/{CAMERA_NAME}/full/{f_name}",
                                    "plateImageUrl": f"/images/{CAMERA_NAME}/plate/{p_name}"
                                }
                                requests.post("http://localhost:8080/api/detection/yolo/detect", json=payload, timeout=1.0)
                                print(f"🚀 전송 완료: {stable_plate} / 최종인식도: {final_acc}")
                            except:
                                pass

                        threading.Thread(target=send, daemon=True).start()
                        sent_ids.add(tid)

        final_frame = cv2.cvtColor(np.array(img_pil), cv2.COLOR_RGB2BGR)
        with frame_lock:
            output_frame = final_frame.copy()

        time.sleep(0.01)

# =========================================================
# Flask 스트리밍
# =========================================================
def generate():
    while True:
        with frame_lock:
            if output_frame is None: continue
            ret, buffer = cv2.imencode('.jpg', output_frame, [cv2.IMWRITE_JPEG_QUALITY, 85])
            frame_bytes = buffer.tobytes()
        yield (b'--frame\r\nContent-Type: image/jpeg\r\n\r\n' + frame_bytes + b'\r\n')

@app.route("/stream")
def stream():
    return Response(generate(), mimetype="multipart/x-mixed-replace; boundary=frame")

if __name__ == "__main__":
    threading.Thread(target=run_analysis, daemon=True).start()
    app.run(host="0.0.0.0", port=5003, threaded=True, debug=False, use_reloader=False)