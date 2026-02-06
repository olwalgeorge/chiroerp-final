# ADR-075: Media & Entertainment Industry Solution

**Status**: Planned (P3 Optional - 2029-2030)  
**Date**: 2026-02-06  
**Decision Makers**: CTO, VP Product  
**Consulted**: Production studios, streaming platforms, music publishers  
**Informed**: Sales, customer success

---

## Context

### Business Problem

Media and entertainment companies need:

- **Rights Management**: Content rights acquisition (film/TV/music), distribution windows (theatrical → streaming → home video), rights expiration tracking
- **Royalty Accounting**: Artist/actor royalties (% of revenue), music publishing (mechanical royalties, performance royalties), participant accounting (profit participants)
- **Content Distribution**: Multi-channel distribution (streaming, theatrical, home video, broadcast), platform analytics (Netflix, Disney+, Hulu views)
- **Project Accounting**: Film/TV production budgets ($10M-200M), above-the-line costs (talent, director, producer), below-the-line costs (crew, equipment, post-production)

### Market Opportunity

**Target Market**:
- Production studios ($100M-2B revenue)
- Streaming platforms (content creation divisions)
- Music publishers ($50M-500M revenue)
- Broadcast networks

**Customer ROI**:
- Royalty calculation time: -70% (automated vs manual Excel)
- Rights management accuracy: 100% (vs 92-95% manual = revenue leakage)
- Project accounting visibility: Real-time budget tracking (vs monthly manual reports)

---

## Decision

### Selected Approach: Media & Entertainment Module

Build **media & entertainment ERP solution** with rights management, royalty accounting, content distribution, and project accounting:

1. **Rights Management**: Content rights acquisition, distribution windows, rights expiration
2. **Royalty Accounting**: Artist/actor royalties, music publishing royalties
3. **Content Distribution**: Multi-channel distribution, platform analytics
4. **Project Accounting**: Film/TV production budgets, cost tracking

### Key Capabilities

#### 1. Rights Management

**Content Rights**:
- Title: Film/TV show/music album
- Rights owner: Production company, artist, publisher
- Rights acquired: Theatrical, streaming, home video, broadcast, international
- Distribution windows:
  - Theatrical: 45-90 days exclusive
  - PVOD (Premium Video on Demand): Day 1 or post-theatrical
  - SVOD (Subscription Video on Demand - Netflix, Disney+): Post-theatrical
  - Home video (Blu-ray, DVD): Post-theatrical
  - Broadcast TV: Post-SVOD
- Rights expiration: 5-10 years (theatrical/home video), 15-25 years (streaming)

**Example (Film Rights)**:
```
Title: "Action Movie 2025"
Rights Owner: ABC Studios
Theatrical Release: 2025-06-15 (90-day exclusive window)
PVOD: 2025-09-13 (post-theatrical)
SVOD (Netflix): 2025-12-15 (post-theatrical + 6mo)
Home Video: 2025-10-15 (post-theatrical + 4mo)
Broadcast TV: 2027-06-15 (post-theatrical + 2yr)
Rights Expiration: 2035-06-15 (10 years)
```

#### 2. Royalty Accounting

**Participant Accounting (Film/TV)**:
- Participant: Actor, director, producer
- Deal structure:
  - Fixed fee (upfront payment)
  - Gross points (% of gross revenue, rare, top-tier talent only)
  - Net points (% of net profit = revenue - production cost - distribution fees)
  - Adjusted gross (revenue - distribution fees only, excludes production cost)
- Royalty calculation: Quarterly

**Example**:
```
Film: "Action Movie 2025" (budget $100M)
Actor Deal: $5M upfront + 5% adjusted gross (revenue - 20% distribution fee)

Q4 2025 Revenue:
- Theatrical: $200M (revenue)
- Distribution Fee: $40M (20% of $200M)
- Adjusted Gross: $200M - $40M = $160M
- Actor Royalty: $160M × 5% = $8M
- Less Upfront: $8M - $5M = $3M (additional payment due)
```

**Music Publishing Royalties**:
- Mechanical royalties: $0.091 per song per copy (CD, digital download)
- Performance royalties: Radio airplay, streaming (ASCAP, BMI, SESAC collection societies)
- Sync licensing: TV/film/commercial use ($10K-500K per sync)

#### 3. Content Distribution

**Multi-Channel Distribution**:
- Platform: Netflix, Disney+, Hulu, Amazon Prime, HBO Max, Apple TV+
- Distribution agreement: Revenue share (70/30, 80/20), flat licensing fee ($10M-100M)
- Analytics: Views, watch time, subscriber retention, revenue per title

**Example**:
```
Title: "Action Movie 2025"
Platform: Netflix
Deal: $50M flat licensing fee (5-year exclusive SVOD rights)
Analytics:
- Q4 2025 Views: 25M (first 28 days)
- Watch Time: 50M hours (2 hours/view average)
- Subscriber Retention: +2% (attributed to title)
```

#### 4. Project Accounting (Film/TV Production)

**Production Budget**:
- Above-the-line: Talent (actors, director, producer), script rights
- Below-the-line: Crew, equipment, locations, post-production (editing, VFX, music)
- Budget range: $10M (indie film) to $200M (blockbuster)

**Cost Tracking**:
- Purchase orders (crew contracts, equipment rentals, location fees)
- Actuals vs budget variance (daily cost reports)
- Forecast to complete (estimated remaining costs)

**Example**:
```
Film: "Action Movie 2025"
Total Budget: $100M
- Above-the-line: $30M (3 actors × $5M, director $10M, producer $5M)
- Below-the-line: $70M (crew $20M, equipment $10M, locations $15M, VFX $25M)

Week 8 of 12-week shoot:
- Actual Spend: $60M
- Budget (Week 8): $55M
- Variance: +$5M over budget
- Forecast to Complete: $105M (estimated final cost)
- Action: Reduce crew overtime, negotiate VFX discount
```

---

## Technology Stack

| Component | Technology | Rationale |
|-----------|-----------|-----------|
| **Backend** | Quarkus 3.x + Kotlin | Existing ChiroERP stack |
| **Royalty Calculation** | Kotlin + complex financial rules | Participant deals, music publishing |
| **Analytics** | Kafka + ClickHouse | Real-time platform analytics (millions of views) |

---

## Success Metrics

| Metric | Target |
|--------|--------|
| **Royalty Calculation Time** | -70% (automated vs manual Excel) |
| **Rights Management Accuracy** | 100% (vs 92-95% manual = revenue leakage) |
| **Project Accounting Visibility** | Real-time budget tracking (vs monthly manual reports) |

---

## Cost Estimate

| Category | Cost |
|----------|------|
| **Development** | $550K-$700K (2 backend × 6mo, 1 media domain expert × 4mo, frontend × 4mo, testing × 3mo) |
| **Analytics Platform** | $50K-$100K (ClickHouse setup, streaming analytics) |
| **Integration** | $50K-$100K (ASCAP/BMI/SESAC APIs, platform APIs Netflix/Disney+) |
| **Total** | **$650K-$900K** |

**P3 Estimate**: **$650K-$850K** (Q4 2029 - Q1 2030, 24 weeks)

---

## Related ADRs

- **ADR-046**: Project Accounting (film/TV production budgets)
- **ADR-022**: Revenue Recognition (royalty revenue)

---

## Approval

**Status**: Planned (P3 Optional - Q4 2029 - Q1 2030)  
**Approved By**: (Pending CTO, VP Product sign-off)  
**Next Review**: Q3 2029 (validate demand with production studios)

---

**Document Owner**: VP Product  
**Last Updated**: 2026-02-06
