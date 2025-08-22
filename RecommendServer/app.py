# app.py (Python ML Server)
from flask import Flask, request, jsonify
import pymysql.cursors

app = Flask(__name__)

# --- MariDB 설정 (⚠️ 실제 정보로 변경) ---
DB_CONFIG = {
    'host': 'localhost', # 실제 MariDB IP/호스트로 변경 (Docker 사용 시 'host.docker.internal' 등)
    'user': 'your_mariadb_username',
    'password': 'your_mariadb_password',
    'db': 'your_database_name',
    'charset': 'utf8mb4',
    'cursorclass': pymysql.cursors.DictCursor, # 결과를 딕셔너리로 받기 위함
    'autocommit': True # SELECT만 하는 경우 고려. 필요에 따라 변경
}

# ML 추천을 위한 최소 상호작용 수
MIN_INTERACTIONS_FOR_ML = 3
# 인기 아이템 반환 개수 (레거시 추천 시)
POPULAR_ITEMS_LIMIT = 5

# --- 모든 추천 로직을 처리하는 핵심 함수 ---
def get_hybrid_recommendations_logic(user_id):
    conn = None
    try:
        conn = pymysql.connect(**DB_CONFIG)
        with conn.cursor() as cursor:
            # 1. 사용자 상호작용 횟수 조회
            sql_interaction_count = "SELECT COUNT(*) AS count FROM user_interaction WHERE user_id = %s"
            cursor.execute(sql_interaction_count, (user_id,))
            interaction_count = cursor.fetchone()['count']

            print(f"ML Server: User {user_id} has {interaction_count} interactions.")

            if interaction_count < MIN_INTERACTIONS_FOR_ML:
                # Case 1: 상호작용이 적은 사용자 (콜드 스타트 또는 신규 유저) -> 레거시(인기 기반) 추천
                print(f"ML Server: User {user_id} - insufficient interactions. Providing popular items.")
                sql_popular_items = "SELECT id FROM item ORDER BY popularity_score DESC LIMIT %s"
                cursor.execute(sql_popular_items, (POPULAR_ITEMS_LIMIT,))
                recommended_item_ids = [row['id'] for row in cursor.fetchall()]
                return recommended_item_ids
            else:
                # Case 2: 상호작용이 충분한 사용자 -> ML 모델 기반 추천
                print(f"ML Server: User {user_id} - sufficient interactions. Using actual ML model.")
                # === 이곳에 실제 ML 모델 로드 및 추론 로직이 들어갑니다 ===
                # 예: model = load_trained_model(); predictions = model.predict(user_features)
                # 현재는 가상의/규칙 기반 ML 추천 결과 반환
                if user_id == 101:
                    # User 101이 봤던 아이템: (1, '청춘 드라마'), (2, 'SF 액션'), (3, '코미디')
                    # ML 모델이 추가로 '판타지 소설'과 '추리 소설'을 추천한다고 가정
                    return [6, 7] # Item ID 반환
                elif user_id == 102:
                    # User 102 (상호작용 적지만 이젠 ML로도 넘김)
                    return [5] # 로맨스 영화
                else:
                    return [] # ML 모델이 예측하지 못하는 경우

    except Exception as e:
        print(f"ML Server DB or Logic Error: {e}")
        # 오류 발생 시 빈 리스트 반환하여 Spring Boot에서 폴백 처리 유도
        return []
    finally:
        if conn:
            conn.close()

@app.route('/recommend', methods=['POST'])
def recommend():
    try:
        user_id = request.get_json() # Spring Boot에서 보낸 userId를 JSON 형태로 받음
        if not isinstance(user_id, (int, float)):
            return jsonify({"error": "Invalid userId format"}), 400

        # 모든 추천 로직을 핵심 함수로 위임
        recommended_item_ids = get_hybrid_recommendations_logic(int(user_id))
        return jsonify(recommended_item_ids) # 추천된 아이템 ID 목록을 JSON 배열로 반환

    except Exception as e:
        print(f"ML Server Request Processing Error: {e}")
        return jsonify({"error": str(e)}), 500

if __name__ == '__main__':
    app.run(port=5000, debug=True) # debug=True는 개발 중 유용