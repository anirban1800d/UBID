# 🏢 Business Recommendation API

A production-ready **FastAPI** application that provides hybrid business recommendations using:
- **BUID-based cosine similarity** on pre-computed sentence embeddings
- **Free-text semantic search** with live query encoding via SentenceTransformer

---

## 📁 Project Structure

```
investment/
├── main.py                          # FastAPI application (all endpoints)
├── requirements.txt                 # Python dependencies
├── .gitignore                       # Git ignore rules
├── README.md                        # This file
│
├── business_embeddings.pkl          # DataFrame for /recommend endpoint
├── business_recommendation.pkl      # DataFrame for /search-by-name endpoint
├── business_recommendation_model/   # SentenceTransformer model folder
└── funding_model.pkl                # LightGBM model (reserved, not used in responses)
```

---

## 🚀 Getting Started

### 1. Clone the repository
```bash
git clone https://github.com/your-username/your-repo-name.git
cd your-repo-name
```

### 2. Create and activate virtual environment
```bash
# Create
python -m venv myenv

# Activate (Windows)
myenv\Scripts\activate

# Activate (Mac/Linux)
source myenv/bin/activate
```

### 3. Install dependencies
```bash
pip install -r requirements.txt
```

### 4. Add required asset files
Place these files in the project root (not included in repo due to size):
- `business_embeddings.pkl`
- `business_recommendation.pkl`
- `business_recommendation_model/` (folder)
- `funding_model.pkl`

### 5. Run the server
```bash
python main.py
```

Server will start at: `http://localhost:8000`

---

## 📡 API Endpoints

### `GET /`
Health check — confirms API is live and all assets are loaded.

**URL:**
```
http://localhost:8000/
```

**Response:**
```json
{
  "status": "ok",
  "buid_businesses_loaded": 40000,
  "name_businesses_loaded": 40000,
  "buid_embedding_shape": [40000, 384],
  "name_embedding_shape": [40000, 384],
  "sentence_model_loaded": true,
  "funding_model_loaded": true
}
```

---

### `GET /recommend`
BUID-based similarity search. Paste directly in browser.

**URL:**
```
http://localhost:8000/recommend?buid=BUID30101&top_n=5
```

**Response columns:**
| Column | Description |
|---|---|
| `BUID` | Business unique ID |
| `name` | Business name |
| `homepage_url` | Website URL |
| `market` | Market segment |
| `funding_total_usd` | Total funding in USD |
| `status` | Operating / acquired / closed |
| `region` | Geographic region |
| `category_list` | Business categories |
| `city` | City |
| `Similarity_Rank` | Rank (1 = most similar) |
| `Similarity_Score` | Cosine similarity score |
| `Rule_Based_Label` | High / Medium / Low |

---

### `POST /recommend`
Same as GET but accepts JSON body (for Android app / Postman).

**URL:** `http://localhost:8000/recommend`

**Request body:**
```json
{
  "buid": "BUID30101",
  "top_n": 5
}
```

---

### `GET /search-by-name`
Free-text semantic search. Paste directly in browser.

**URL:**
```
http://localhost:8000/search-by-name?name=Netflix&region=us_west_coast&category=media_entertainment&top_n=5
```

**Response columns:**
| Column | Description |
|---|---|
| `name` | Business name |
| `homepage_url` | Website URL |
| `market` | Market segment |
| `category_list` | Business categories |
| `status` | Operating / acquired / closed |
| `funding_total_usd` | Total funding |
| `similarity_score` | Cosine similarity score |
| `similarity_rank` | Rank (1 = most similar) |

---

### `POST /search-by-name`
Same as GET but accepts JSON body.

**URL:** `http://localhost:8000/search-by-name`

**Request body:**
```json
{
  "name": "Netflix",
  "region": "us_west_coast",
  "category": "media_entertainment",
  "top_n": 5
}
```

**Query is built as:** `"{name} {region} {category}"` → `"Netflix us_west_coast media_entertainment"`

---

## 🧠 How It Works

### `/recommend` — BUID-based
1. Look up business row by BUID
2. Retrieve its pre-computed embedding vector
3. Compute cosine similarity against all embeddings (vectorised numpy)
4. Return top-N most similar businesses

### `/search-by-name` — Semantic Search
1. Build query string: `f"{name} {region} {category}"`
2. Encode query live using SentenceTransformer
3. Compute cosine similarity against pre-normalised embedding matrix
4. Return top-N most similar businesses



---

## 🌍 Region & Category Values

### Regions
`us_northeast` · `us_west_coast` · `us_south_central` · `uk` · `india` ·
`eastern_europe` · `southeast_asia` · `australia` · `canada` · `africa`

### Categories
`gaming` · `fintech` · `saas` · `ecommerce_retail` · `media_entertainment` ·
`education` · `food_lifestyle` · `travel_transport` · `healthtech` · `social_communication`

---

## 🧪 Testing

### Swagger UI (built-in, no extra tools)
```
http://localhost:8000/docs
```

### Browser (GET endpoints)
```
http://localhost:8000/recommend?buid=BUID30101&top_n=5
http://localhost:8000/search-by-name?name=Netflix&region=us_west_coast&category=media_entertainment&top_n=5
```

### Python
```python
import requests

# BUID-based
r = requests.get("http://localhost:8000/recommend", params={"buid": "BUID30101", "top_n": 5})
print(r.json())

# Semantic search
r = requests.post("http://localhost:8000/search-by-name", json={
    "name": "Netflix",
    "region": "us_west_coast",
    "category": "media_entertainment",
    "top_n": 5
})
print(r.json())
```

### curl
```bash
curl "http://localhost:8000/recommend?buid=BUID30101&top_n=5"

curl -X POST "http://localhost:8000/search-by-name" \
  -H "Content-Type: application/json" \
  -d '{"name": "Netflix", "region": "us_west_coast", "category": "media_entertainment", "top_n": 5}'
```

---

## 🛠️ Tech Stack

| Tool | Purpose |
|---|---|
| **FastAPI** | Web framework |
| **Uvicorn** | ASGI server |
| **SentenceTransformer** | Live query encoding |
| **NumPy** | Vectorised cosine similarity |
| **Pandas** | DataFrame operations |
| **Joblib** | Model & data loading |
| **LightGBM** | Funding model (reserved) |
| **Pydantic** | Request/response validation |

---

## ⚙️ Performance

- Models and embeddings loaded **once at startup**
- Embedding matrix **pre-normalised** at startup (no per-request overhead)
- Cosine similarity computed with a **single matrix-vector multiply** (no loops)
- All numpy types converted to Python native for clean JSON output

---

## 📝 Notes

- `funding_model.pkl` is loaded but **not used** in any API response — reserved for future use
- The `.pkl` and model folder are excluded from git (see `.gitignore`) due to large file size
- CORS is enabled for all origins — restrict `allow_origins` in production

---

## 📄 License

MIT License — free to use and modify.
