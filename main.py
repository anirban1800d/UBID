"""
Hybrid Business Recommendation API  v4.0
=========================================
Endpoints
---------
GET  /                  → health check
GET  /recommend         → BUID-based similarity (URL query params)
POST /recommend         → BUID-based similarity (JSON body)
POST /search-by-name    → free-text semantic search (live query encoding)

Output columns
--------------
/recommend        → BUID, name, homepage_url, market, funding_total_usd,
                    status, region, category_list, city,
                    Similarity_Rank, Similarity_Score, Rule_Based_Label

/search-by-name   → name, homepage_url, market, category_list,
                    status, funding_total_usd,
                    similarity_score, similarity_rank
                    (matches notebook output exactly)

Assets required (same directory as this file)
----------------------------------------------
business_embeddings.pkl          – DataFrame for BUID-based endpoint
business_recommendation.pkl      – DataFrame for name-search endpoint
business_recommendation_model/   – SentenceTransformer model directory
funding_model.pkl                – sklearn Pipeline (reserved, NOT used in responses)
"""

from __future__ import annotations

import logging
from contextlib import asynccontextmanager
from pathlib import Path
from typing import Any

import joblib
import numpy as np
import pandas as pd
import uvicorn
from fastapi import FastAPI, HTTPException, status
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel, Field
from sentence_transformers import SentenceTransformer

# ── Logging ───────────────────────────────────────────────────────────────────

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s | %(levelname)-8s | %(name)s | %(message)s",
)
log = logging.getLogger("recommendation_api")

# ── Paths ─────────────────────────────────────────────────────────────────────

BASE_DIR = Path(__file__).parent

BUID_EMBEDDINGS_PATH = BASE_DIR / "business_embeddings.pkl"
NAME_EMBEDDINGS_PATH = BASE_DIR / "business_recommendation.pkl"
SENTENCE_MODEL_PATH  = BASE_DIR / "business_recommendation_model"
FUNDING_MODEL_PATH   = BASE_DIR / "funding_model.pkl"

# ── Application state ─────────────────────────────────────────────────────────

class AppState:
    # /recommend assets
    buid_df: pd.DataFrame
    buid_emb_matrix: np.ndarray

    # /search-by-name assets
    name_df: pd.DataFrame
    name_emb_matrix: np.ndarray
    sentence_model: SentenceTransformer

    # reserved
    funding_model: Any


state = AppState()

# ── Lifespan ──────────────────────────────────────────────────────────────────

@asynccontextmanager
async def lifespan(app: FastAPI):
    log.info("Loading assets …")

    # 1. BUID DataFrame + matrix
    if not BUID_EMBEDDINGS_PATH.exists():
        raise FileNotFoundError(f"Missing: {BUID_EMBEDDINGS_PATH}")
    state.buid_df = joblib.load(BUID_EMBEDDINGS_PATH)
    if "embadding" in state.buid_df.columns and "embedding" not in state.buid_df.columns:
        state.buid_df.rename(columns={"embadding": "embedding"}, inplace=True)
    state.buid_emb_matrix = np.vstack(
        state.buid_df["embedding"].values
    ).astype(np.float32)
    log.info("BUID matrix: %s", state.buid_emb_matrix.shape)

    # 2. Name-search DataFrame + L2-normalised matrix
    if not NAME_EMBEDDINGS_PATH.exists():
        raise FileNotFoundError(f"Missing: {NAME_EMBEDDINGS_PATH}")
    state.name_df = joblib.load(NAME_EMBEDDINGS_PATH)
    raw = np.vstack(state.name_df["embedding"].values).astype(np.float32)
    row_norms = np.linalg.norm(raw, axis=1, keepdims=True)
    row_norms = np.where(row_norms == 0, 1e-10, row_norms)
    state.name_emb_matrix = raw / row_norms
    log.info("Name matrix: %s", state.name_emb_matrix.shape)

    # 3. Sentence transformer
    if not SENTENCE_MODEL_PATH.exists():
        raise FileNotFoundError(f"Missing: {SENTENCE_MODEL_PATH}")
    state.sentence_model = SentenceTransformer(str(SENTENCE_MODEL_PATH))
    log.info("SentenceTransformer loaded.")

    # 4. Funding model (reserved)
    if FUNDING_MODEL_PATH.exists():
        state.funding_model = joblib.load(FUNDING_MODEL_PATH)
        log.info("Funding model loaded (reserved).")
    else:
        state.funding_model = None
        log.warning("funding_model.pkl not found.")

    log.info("All assets ready — API is live.")
    yield
    log.info("Shutting down.")


# ── FastAPI app ───────────────────────────────────────────────────────────────

app = FastAPI(
    title="Business Recommendation API",
    description="Two endpoints: BUID-based recommendation + free-text semantic search.",
    version="4.0.0",
    lifespan=lifespan,
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# ═════════════════════════════════════════════════════════════════════════════
# SCHEMAS — /recommend  (BUID-based)
# ═════════════════════════════════════════════════════════════════════════════

class RecommendRequest(BaseModel):
    buid: str = Field(..., example="BUID30101")
    top_n: int = Field(default=5, ge=1, le=50)


class RecommendedBusiness(BaseModel):
    """Output schema for /recommend — BUID-based endpoint."""
    BUID: str
    name: str | None
    homepage_url: str | None
    market: str | None
    funding_total_usd: float | None
    status: str | None
    region: str | None
    category_list: str | None
    city: str | None
    Similarity_Rank: int
    Similarity_Score: float
    Rule_Based_Label: str


class RecommendResponse(BaseModel):
    query_buid: str
    total_results: int
    recommendations: list[RecommendedBusiness]


# ═════════════════════════════════════════════════════════════════════════════
# SCHEMAS — /search-by-name  (notebook output, exactly)
# ═════════════════════════════════════════════════════════════════════════════

class NameSearchRequest(BaseModel):
    name: str     = Field(..., example="Netflix")
    region: str   = Field(..., example="us_west_coast")
    category: str = Field(..., example="media_entertainment")
    top_n: int    = Field(default=5, ge=1, le=50)


class NameSearchResult(BaseModel):
    """
    Output schema for /search-by-name.
    Matches notebook columns exactly:
        name, homepage_url, market, category_list,
        status, funding_total_usd, similarity_score, similarity_rank
    """
    name: str | None
    homepage_url: str | None
    market: str | None
    category_list: str | None
    status: str | None
    funding_total_usd: str | None
    similarity_score: float
    similarity_rank: int


class NameSearchResponse(BaseModel):
    query: str
    total_results: int
    results: list[NameSearchResult]


# ═════════════════════════════════════════════════════════════════════════════
# HEALTH SCHEMA
# ═════════════════════════════════════════════════════════════════════════════

class HealthResponse(BaseModel):
    status: str
    buid_businesses_loaded: int
    name_businesses_loaded: int
    buid_embedding_shape: list[int]
    name_embedding_shape: list[int]
    sentence_model_loaded: bool
    funding_model_loaded: bool


# ── Helpers ───────────────────────────────────────────────────────────────────

def _assign_label(score: float) -> str:
    if score >= 0.75:
        return "High"
    if score >= 0.72:
        return "Medium"
    return "Low"


def _safe_str(v: Any) -> str | None:
    if v is None:
        return None
    try:
        if pd.isna(v):
            return None
    except (TypeError, ValueError):
        pass
    return str(v)


def _safe_float(v: Any) -> float | None:
    if v is None:
        return None
    try:
        f = float(v)
        return None if np.isnan(f) else f
    except (TypeError, ValueError):
        return None


# ═════════════════════════════════════════════════════════════════════════════
# CORE LOGIC — /recommend  (BUID-based)
# ═════════════════════════════════════════════════════════════════════════════

def _recommend_by_buid(buid: str, top_n: int) -> list[dict]:
    mask = state.buid_df["BUID"] == buid
    if not mask.any():
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"BUID '{buid}' not found.",
        )

    query_vec = state.buid_df.loc[mask, "embedding"].values[0].astype(np.float32)
    query_idx = int(np.where(mask)[0][0])

    dot   = state.buid_emb_matrix @ query_vec
    norms = (
        np.linalg.norm(state.buid_emb_matrix, axis=1)
        * np.linalg.norm(query_vec)
    )
    norms = np.where(norms == 0, 1e-10, norms)
    sims  = dot / norms

    sims[query_idx] = -np.inf
    top_idx = np.argsort(sims)[::-1][:top_n]

    results: list[dict] = []
    for rank, idx in enumerate(top_idx, start=1):
        row   = state.buid_df.iloc[idx]
        score = float(sims[idx])
        results.append({
            "BUID":              str(row["BUID"]),
            "name":              _safe_str(row.get("name")),
            "homepage_url":      _safe_str(row.get("homepage_url")),
            "market":            _safe_str(row.get("market")),
            "funding_total_usd": _safe_float(row.get("funding_total_usd")),
            "status":            _safe_str(row.get("status")),
            "region":            _safe_str(row.get("region")),
            "category_list":     _safe_str(row.get("category_list")),
            "city":              _safe_str(row.get("city")),
            "Similarity_Rank":   rank,
            "Similarity_Score":  round(score, 6),
            "Rule_Based_Label":  _assign_label(score),
        })
    return results


# ═════════════════════════════════════════════════════════════════════════════
# CORE LOGIC — /search-by-name  (free-text semantic search)
# ═════════════════════════════════════════════════════════════════════════════

def _search_by_name(name: str, region: str, category: str, top_n: int) -> tuple[str, list[dict]]:
    # Build query exactly like notebook
    query = f"{name} {region} {category}"
    log.info("Encoding query: '%s'", query)

    query_vec: np.ndarray = state.sentence_model.encode(
        query,
        convert_to_numpy=True,
        normalize_embeddings=True,
    ).astype(np.float32)

    # Pre-normalised matrix → dot == cosine similarity
    sims: np.ndarray = state.name_emb_matrix @ query_vec

    top_idx = np.argsort(sims)[::-1][:top_n]

    results: list[dict] = []
    for rank, idx in enumerate(top_idx, start=1):
        row   = state.name_df.iloc[idx]
        score = float(sims[idx])
        results.append({
            # ── exactly your notebook columns ──
            "name":              _safe_str(row.get("name")),
            "homepage_url":      _safe_str(row.get("homepage_url")),
            "market":            _safe_str(row.get("market")),
            "category_list":     _safe_str(row.get("category_list")),
            "status":            _safe_str(row.get("status")),
            "funding_total_usd": _safe_str(row.get("funding_total_usd")),
            "similarity_score":  round(score, 4),
            "similarity_rank":   rank,
        })
    return query, results


# ═════════════════════════════════════════════════════════════════════════════
# ENDPOINTS
# ═════════════════════════════════════════════════════════════════════════════

@app.get("/", response_model=HealthResponse, tags=["Health"])
def health_check():
    """Confirm the API is live and all assets are loaded."""
    return HealthResponse(
        status="ok",
        buid_businesses_loaded=len(state.buid_df),
        name_businesses_loaded=len(state.name_df),
        buid_embedding_shape=list(state.buid_emb_matrix.shape),
        name_embedding_shape=list(state.name_emb_matrix.shape),
        sentence_model_loaded=state.sentence_model is not None,
        funding_model_loaded=state.funding_model is not None,
    )


@app.get(
    "/recommend",
    response_model=RecommendResponse,
    tags=["BUID Recommendation"],
    summary="BUID similarity search — paste directly in browser",
)
def recommend_get(buid: str, top_n: int = 5):
    """Browser URL: `http://localhost:8000/recommend?buid=BUID30101&top_n=5`"""
    recs = _recommend_by_buid(buid=buid, top_n=top_n)
    return RecommendResponse(query_buid=buid, total_results=len(recs), recommendations=recs)


@app.post(
    "/recommend",
    response_model=RecommendResponse,
    tags=["BUID Recommendation"],
    summary="BUID similarity search — JSON body",
)
def recommend_post(body: RecommendRequest):
    """
    Returns: `BUID, name, homepage_url, market, funding_total_usd,
    status, region, category_list, city, Similarity_Rank, Similarity_Score, Rule_Based_Label`
    """
    recs = _recommend_by_buid(buid=body.buid, top_n=body.top_n)
    return RecommendResponse(query_buid=body.buid, total_results=len(recs), recommendations=recs)


@app.post(
    "/search-by-name",
    response_model=NameSearchResponse,
    tags=["Semantic Search"],
    summary="Free-text semantic search — live query encoding",
)
def search_by_name(body: NameSearchRequest):
    query, results = _search_by_name(
        name=body.name,
        region=body.region,
        category=body.category,
        top_n=body.top_n,
    )
    return NameSearchResponse(query=query, total_results=len(results), results=results)

@app.get(
    "/search-by-name",
    response_model=NameSearchResponse,
    tags=["Semantic Search"],
    summary="Free-text semantic search — paste directly in browser",
)
def get_search_by_name(name: str, region: str, category: str, top_n: int = 5):
    query, results = _search_by_name(
        name=name,
        region=region,
        category=category,
        top_n=top_n,
    )
    return NameSearchResponse(query=query, total_results=len(results), results=results)


# ── Entry point ───────────────────────────────────────────────────────────────

if __name__ == "__main__":
    uvicorn.run(
        "main:app",
        host="0.0.0.0",
        port=8000,
        reload=False,
        workers=1,
    )
