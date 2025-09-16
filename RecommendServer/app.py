# app.py
from flask import Flask, request, jsonify
import os
import pymysql.cursors
import pandas as pd
from surprise import SVD, Dataset, Reader
from surprise import Prediction

app = Flask(__name__)

# --- MariDB 설정 (⚠️ 실제 정보로 변경) ---
DB_CONFIG = {
    'host': os.getenv('DB_HOST', 'localhost'),
    'user': os.getenv('DB_USER', 'default_user'),
    'password': os.getenv('DB_PASSWORD', 'default_password'),
    'db': os.getenv('DB_NAME', 'sellog'),
    'charset': 'utf8mb4',
    'cursorclass': pymysql.cursors.DictCursor,
}

# --- 추천 시스템 설정 값 ---
MIN_INTERACTIONS_FOR_ML = 3    # ML 추천을 위한 최소 상호작용 수 (Spring Boot의 application.properties와 동일값)
POPULAR_ITEMS_LIMIT = 5        # 인기 아이템 반환 개수 (Spring Boot의 application.properties와 동일값)
RECOMMENDED_ITEMS_COUNT = 10   # ML 모델로 추천할 아이템 개수

# --- 전역 변수: 모델과 학습 데이터 ---
global_recommendation_model = None  # 학습된 Surprise 모델이 로드될 변수
global_item_ids = []                # 현재 서비스 가능한 모든 아이템 ID 리스트
global_reader = None                # Dataset Reader (Surprise 라이브러리용)

# --- 모델 로딩 및 학습 함수 ---
# Flask 앱이 시작될 때 이 함수를 한 번 호출하여 모델을 메모리에 로드하고 학습.
def load_and_train_model():
    global global_recommendation_model, global_item_ids, global_reader

    conn = None
    try:
        conn = pymysql.connect(**DB_CONFIG)
        
        # 1. UserInteraction 데이터 로드 (학습 데이터)
        # 상호작용 데이터를 불러옵니다. 여기서 'rating'은 상호작용 발생 여부만 나타내므로 1로 고정.
        # 실제 서비스에서는 'rating' 컬럼이 별점이나 선호도 점수일 수 있습니다.
        sql_interactions = """
            SELECT user_id, item_id, 1 AS rating
            FROM user_interaction
            WHERE interaction_type IN ('VIEW', 'LIKE', 'REVIEW')
            """
        df_interactions = pd.read_sql(sql_interactions, conn)
        if df_interactions.empty:
            print("[ML Model] No interaction data available for training. Model will not be loaded.")
            return

        # 2. 모든 아이템 ID 로드 (추론 시 필요)
        sql_all_items = "SELECT id FROM item"
        df_all_items = pd.read_sql(sql_all_items, conn)
        global_item_ids = df_all_items['id'].tolist()

        # 3. Surprise Reader 정의: user_id, item_id, rating 컬럼 순서와 평점 척도 지정
        # 이 예시에서는 상호작용이 있으면 1로 가정. (최소 1, 최대 1)
        global_reader = Reader(rating_scale=(1, 1))

        # 4. Dataset 생성
        data = Dataset.load_from_df(df_interactions[['user_id', 'item_id', 'rating']], global_reader)

        # 5. 전체 데이터로 학습 세트 빌드
        # train_test_split을 사용하여 모델 성능을 평가할 수도 있지만,
        # 여기서는 전체 데이터로 모델을 학습시켜 실제 추론에 사용
        trainset = data.build_full_trainset()

        # 6. SVD (Singular Value Decomposition) 모델 초기화 및 학습
        model = SVD(random_state=42, n_epochs=20, n_factors=100)
        model.fit(trainset)

        global_recommendation_model = model
        print("[ML Model] Recommendation model loaded and trained successfully.")

    except pymysql.Error as e:
        print(f"[ML Model] DB connection or query error during model loading: {e}")
    except Exception as e:
        print(f"[ML Model] Error during model loading/training: {e}")
    finally:
        if conn:
            conn.close()

# --- 추천 로직의 핵심 함수 ---
def get_recommendations_logic(user_id):
    conn = None
    try:
        conn = pymysql.connect(**DB_CONFIG)
        with conn.cursor() as cursor:
            # 1. 사용자 상호작용 횟수 조회
            sql_interaction_count = "SELECT COUNT(*) AS count FROM user_interaction WHERE user_id = %s"
            cursor.execute(sql_interaction_count, (user_id,))
            interaction_count = cursor.fetchone()['count']

            print(f"[Recommend Logic] User {user_id} has {interaction_count} interactions.")

            # 2. 하이브리드 로직: 상호작용 수 또는 모델 유무에 따른 분기
            if global_recommendation_model is None or interaction_count < MIN_INTERACTIONS_FOR_ML:
                # 모델이 없거나 사용자 상호작용이 부족하면 인기 아이템 추천 (Fallback)
                print(f"[Recommend Logic] User {user_id} - Insufficient interactions or model not loaded. Providing popular items.")
                sql_popular_items = "SELECT id FROM item ORDER BY popularity_score DESC LIMIT %s" # item 테이블에 popularity_score가 있다고 가정
                cursor.execute(sql_popular_items, (POPULAR_ITEMS_LIMIT,))
                popular_item_ids = [row['id'] for row in cursor.fetchall()]
                return popular_item_ids
            else:
                # ML 모델 기반 개인화 추천
                print(f"[Recommend Logic] User {user_id} - Sufficient interactions. Using ML model.")
                
                # 사용자가 이미 상호작용했던 아이템 조회 (추천에서 제외하기 위함)
                sql_interacted_items = "SELECT item_id FROM user_interaction WHERE user_id = %s"
                cursor.execute(sql_interacted_items, (user_id,))
                interacted_item_ids = {row['item_id'] for row in cursor.fetchall()}

                # 추천 예측 대상 아이템 목록 (모든 아이템 - 이미 상호작용한 아이템)
                items_to_predict = [item_id for item_id in global_item_ids if str(item_id) not in interacted_item_ids]

                predictions = []
                for item_id in items_to_predict:
                    # ML 모델로 예측 평점 계산 (Surprise predict 함수 사용)
                    prediction: Prediction = global_recommendation_model.predict(user_id, item_id, verbose=False)
                    predictions.append((item_id, prediction.est))

                # 예측 평점이 높은 순서대로 정렬하여 상위 N개 아이템 추천
                predictions.sort(key=lambda x: x[1], reverse=True)
                recommended_ids = [item_id for item_id, _ in predictions[:RECOMMENDED_ITEMS_COUNT]]
                
                # ML 추천 결과가 비어있을 경우 인기 아이템으로 Fallback
                if not recommended_ids:
                    print(f"[Recommend Logic] ML model provided no recommendations for User {user_id}. Falling back to popular items.")
                    sql_popular_items = "SELECT id FROM item ORDER BY popularity_score DESC LIMIT %s" # item 테이블에 popularity_score가 있다고 가정
                    cursor.execute(sql_popular_items, (POPULAR_ITEMS_LIMIT,))
                    recommended_ids = [row['id'] for row in cursor.fetchall()]

                return recommended_ids

    except pymysql.Error as e:
        print(f"[Recommend Logic] DB connection or query error: {e}")
        return [] # 에러 발생 시 빈 리스트 반환
    except Exception as e:
        print(f"[Recommend Logic] Error during recommendation logic: {e}")
        return []
    finally:
        if conn:
            conn.close()

# --- Flask 라우트 설정 ---
@app.route('/', methods=['POST'])
def recommend():
    try:
        data = request.get_json()
        if not data or 'userId' not in data:
            return jsonify({"error": "userId is required in the JSON body"}), 400

        user_id = data['userId']

        print(f"[Flask Server] Received recommendation request for userId: {user_id}")
        
        recommended_item_ids = get_recommendations_logic(user_id)
        
        # Java(UUID)와 Python(str) 간의 데이터 교환을 위해 ID를 문자열로 변환
        recommended_item_ids = [str(item_id) for item_id in recommended_item_ids]
        return jsonify(recommended_item_ids) # 추천된 아이템 ID 목록을 JSON 배열로 반환

    except Exception as e:
        print(f"[Flask Server] Error processing request: {e}")
        return jsonify({"error": str(e)}), 500

# --- Flask 앱 시작 시 모델 로드 ---
# before_first_request는 Flask 앱이 첫 번째 요청을 처리하기 직전에 호출됨
with app.app_context():
    load_and_train_model()

if __name__ == '__main__':
    # 운영 환경에서는 Gunicorn과 같은 WSGI 서버를 사용해야 합니다.
    app.run(host='0.0.0.0', port=5000, debug=False)
