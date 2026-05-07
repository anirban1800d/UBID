## UBID — Unified Business Identifier & Active Business Intelligence

A hackathon solution for Karnataka Commerce & Industries to solve the 
cross-department business identity problem.

### What it does
- **Entity Resolution**: Automatically links business records across 40+ 
  Karnataka State department systems (Shop Establishment, Factories, Labour, 
  KSPCB, ESCOMs, BWSSB, Fire, Food Safety, and more) — even when names, 
  addresses, and identifiers are inconsistent or missing.
- **UBID Assignment**: Assigns every real-world business a single Unique 
  Business Identifier, anchored to PAN/GSTIN where available.
- **Confidence Scoring**: Every linkage decision carries an explainable 
  confidence score — high-confidence matches auto-commit, ambiguous ones 
  route to human review, low-confidence records stay isolated.
- **Activity Classification**: Infers Active, Dormant, or Closed status per 
  UBID from heterogeneous event streams (inspections, renewals, compliance 
  filings, utility consumption) with a full evidence timeline.
- **Human-in-the-Loop**: Reviewer workflow for ambiguous cases, with decisions 
  feeding back to improve matching over time.

### Key Constraints Respected
- Zero changes to source department systems
- Works entirely on scrambled/synthetic data (no raw PII)
- Every automated decision is explainable and reversible

### Example Query Unlocked
> "Active factories in pin code 560058 with no inspection in the last 18 months"

### Tech Stack
[Add your stack here — e.g. Python, FastAPI, PostgreSQL, spaCy/recordlinkage, 
React dashboard, etc.]

### Built for
Karnataka Commerce & Industry — Smart Governance Hackathon, Round 1
