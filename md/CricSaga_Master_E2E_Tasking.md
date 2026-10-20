# CricSaga — End-to-End Master Tasking & Execution Roadmap

**Tagline:** Har Run Ki Kahani  
**Document Purpose:** The single definitive source of truth for the end-to-end development, architecture, status, and execution roadmap of CricSaga.  
**Audience:** Developer & AI Pair Programmer  
**Last Updated:** October 2026  
**File Location:** `md/CricSaga_Master_E2E_Tasking.md`  

---

## 1. Executive Summary & Product Mission

CricSaga is a high-performance, native Android cricket scoring and community platform purpose-built for grassroots cricket teams, turf tournaments, and scorers across India and South Asia. It serves as a superior, high-integrity alternative to CricHeroes with:
1. **Zero Learning Curve:** Familiar scoring keypad geometry (`0-3`, `4, 6, Wd, Nb`, `B, Lb, OUT, UNDO`) ensuring seamless adoption without scorer retraining.
2. **100% Offline-First Architecture:** Pure Kotlin domain engine and Room SQLite persistence. Zero data loss during network drops or remote ground play.
3. **Discreet & Respectful Monetization:**
   - 3 transparent plans: **₹99 for 3 months**, **₹199 for 1 year** (Most Popular), **₹2,000 for Lifetime VIP**.
   - Single-use dedicated `redeem_codes` SQLite table for direct UPI/offline code distribution.
   - **Minimal Ads on Free Tier:** Clean inline text sponsor banners only. Strictly **NO** unskippable video popups, **NO** full-screen interstitial ads between balls, and **NO** gambling/betting ads. Pro is 100% ad-free.
4. **Professional Minimalist Aesthetic:**
   - Smooth 1.5-second cold-launch GPU intro animation.
   - App-wide de-branding (clean typography headers, zero logo spam).
   - Monochromatic deep slate palette (`#0F1013` canvas, `#17191E` cards, `#262933` 0.5dp borders) with single crimson accent (`#E53935`) reserved strictly for primary CTAs and live dots.

---

## 2. Complete End-to-End Module Status Matrix

| Module | Core Features | Status | Code Files |
| :--- | :--- | :---: | :--- |
| **01. Cold Start & Splash** | • 1.5s GPU intro animation (speed ball swoop, title & tagline fade)<br>• Clean crossfade into main app | **COMPLETED** | [`SplashScreen.kt`](file:///home/shrik/Work/CricSaga/app/src/main/java/com/cricsaga/app/ui/splash/SplashScreen.kt), [`MainActivity.kt`](file:///home/shrik/Work/CricSaga/app/src/main/java/com/cricsaga/app/MainActivity.kt) |
| **02. Design System & Theme** | • Deep slate monochromatic palette<br>• 0.5dp hairline borders, no neon borders<br>• Clean typography & de-branded top bars | **COMPLETED** | [`Color.kt`](file:///home/shrik/Work/CricSaga/app/src/main/java/com/cricsaga/app/ui/theme/Color.kt), [`Theme.kt`](file:///home/shrik/Work/CricSaga/app/src/main/java/com/cricsaga/app/ui/theme/Theme.kt), [`Type.kt`](file:///home/shrik/Work/CricSaga/app/src/main/java/com/cricsaga/app/ui/theme/Type.kt) |
| **03. Scoring Domain Engine** | • Pure Kotlin domain model (0 Android imports)<br>• Legal runs, boundaries, strike rotation, bowler rotation<br>• Extras (Wide, No-Ball, Bye, Leg-Bye)<br>• 10 dismissal modes + fielder credit<br>• Multi-level sequential undo<br>• 10/10 automated unit tests passing green | **COMPLETED** | [`ScoringEngine.kt`](file:///home/shrik/Work/CricSaga/app/src/main/java/com/cricsaga/app/domain/engine/ScoringEngine.kt), [`ScoringEngineTest.kt`](file:///home/shrik/Work/CricSaga/app/src/test/java/com/cricsaga/app/domain/engine/ScoringEngineTest.kt) |
| **04. Local SQLite Database** | • Room v3 with WAL mode<br>• Tables: `users`, `teams`, `team_members`, `matches`, `innings`, `deliveries`, `player_stats`, `redeem_codes`<br>• Foreign key safe migrations & crash-free restarts | **COMPLETED** | [`CricSagaDatabase.kt`](file:///home/shrik/Work/CricSaga/app/src/main/java/com/cricsaga/app/data/local/CricSagaDatabase.kt), [`entity/`](file:///home/shrik/Work/CricSaga/app/src/main/java/com/cricsaga/app/data/local/entity), [`dao/`](file:///home/shrik/Work/CricSaga/app/src/main/java/com/cricsaga/app/data/local/dao) |
| **05. Monetization & Promo Codes** | • 3 subscription plans (₹99 / ₹199 / ₹2,000)<br>• Dedicated single-use `redeem_codes` SQLite validation<br>• Pro entitlement state flow & ad-removal toggle | **COMPLETED** | [`ProManager.kt`](file:///home/shrik/Work/CricSaga/app/src/main/java/com/cricsaga/app/data/local/ProManager.kt), [`RedeemCodeScreen.kt`](file:///home/shrik/Work/CricSaga/app/src/main/java/com/cricsaga/app/ui/redeem/RedeemCodeScreen.kt) |
| **06. Home Feed Tab** | • Live Match Radar carousel<br>• Scorer Launchpad (+ Start Match, Teams, Tournaments)<br>• Top Performers sports card<br>• Minimal inline sponsor banner | **COMPLETED** | [`HomeScreen.kt`](file:///home/shrik/Work/CricSaga/app/src/main/java/com/cricsaga/app/ui/home/HomeScreen.kt) |
| **07. Matches Hub Tab** | • Live matches & Completed matches list<br>• Direct navigation to in-progress scoring or full scorecard | **COMPLETED** | [`MatchListScreen.kt`](file:///home/shrik/Work/CricSaga/app/src/main/java/com/cricsaga/app/ui/matches/MatchListScreen.kt) |
| **08. Discover Hub Tab** | • "Looking For" matchmaker (Opponents, Players, Umpires)<br>• Direct WhatsApp click-to-chat action<br>• Grounds & Turfs directory<br>• Open tournaments list | **COMPLETED** (Interactive Dialog in Phase 4) | [`DiscoverScreen.kt`](file:///home/shrik/Work/CricSaga/app/src/main/java/com/cricsaga/app/ui/discover/DiscoverScreen.kt) |
| **09. Tournaments Hub Tab** | • League Points Table with ICC Net Run Rate (NRR)<br>• Leaderboards (Most Runs & Most Wickets)<br>• Fixtures & tournament details | **COMPLETED** (Creation Dialog in Phase 4) | [`TournamentListScreen.kt`](file:///home/shrik/Work/CricSaga/app/src/main/java/com/cricsaga/app/ui/tournaments/TournamentListScreen.kt) |
| **10. My Career Hub Tab** | • Profile header card<br>• 4-metric hero strip (Matches, Runs, Wickets, Average)<br>• Clean tabular statistics (Batting, Bowling, Fielding, Matches) | **COMPLETED** (Dynamic DB update in Phase 2) | [`PlayerProfileScreen.kt`](file:///home/shrik/Work/CricSaga/app/src/main/java/com/cricsaga/app/ui/profile/PlayerProfileScreen.kt) |
| **11. Match Setup & Toss** | • Team selection (Team A vs Team B)<br>• Overs per side configuration<br>• Toss winner & elected decision (Bat / Bowl)<br>• Opening Striker, Non-Striker & Opening Bowler selection | **COMPLETED** | [`MatchSetupScreen.kt`](file:///home/shrik/Work/CricSaga/app/src/main/java/com/cricsaga/app/ui/matchsetup/MatchSetupScreen.kt) |
| **12. Scoring Cockpit & Dialogs** | • 12-button keypad matching CricHeroes muscle memory<br>• Live over timeline with color-coded ball tokens<br>• Dismissal bottom sheet (10 modes + fielder picker)<br>• Bowler change dialog enforcing consecutive over rule | **COMPLETED** | [`ScoringScreen.kt`](file:///home/shrik/Work/CricSaga/app/src/main/java/com/cricsaga/app/ui/scoring/ScoringScreen.kt), [`ScoringViewModel.kt`](file:///home/shrik/Work/CricSaga/app/src/main/java/com/cricsaga/app/ui/scoring/ScoringViewModel.kt) |
| **13. Full Scorecard & Summary** | • Batting scorecard (Runs, Balls, 4s, 6s, SR, Dismissal)<br>• Bowling scorecard (O, M, R, W, ER)<br>• Fall of Wickets table<br>• WhatsApp Status match summary poster card | **COMPLETED** (Native Bitmap Share in Phase 3) | [`FullScorecardScreen.kt`](file:///home/shrik/Work/CricSaga/app/src/main/java/com/cricsaga/app/ui/scorecard/FullScorecardScreen.kt) |
| **14. 2nd Innings & Match Result** | • Detect 1st Innings end (10 wickets down or overs finished)<br>• 2nd Innings opener selection<br>• Live Target Chase Equation (`Need X runs in Y balls \| RRR: Z.ZZ`)<br>• Win condition calculation & Match Result modal<br>• Player of the Match award selection | **TO BUILD (Phase 1 - Immediate)** | *Target:* [`ScoringEngine.kt`](file:///home/shrik/Work/CricSaga/app/src/main/java/com/cricsaga/app/domain/engine/ScoringEngine.kt), [`ScoringViewModel.kt`](file:///home/shrik/Work/CricSaga/app/src/main/java/com/cricsaga/app/ui/scoring/ScoringViewModel.kt), [`ScoringScreen.kt`](file:///home/shrik/Work/CricSaga/app/src/main/java/com/cricsaga/app/ui/scoring/ScoringScreen.kt) |
| **15. Dynamic Career Persistence** | • Aggregate finished match stats into Room `player_stats`<br>• Auto-update "My Career" tab with live career records | **TO BUILD (Phase 2)** | *Target:* [`MatchRepository.kt`](file:///home/shrik/Work/CricSaga/app/src/main/java/com/cricsaga/app/data/repository/MatchRepository.kt), [`PlayerProfileViewModel.kt`](file:///home/shrik/Work/CricSaga/app/src/main/java/com/cricsaga/app/ui/profile/PlayerProfileViewModel.kt) |
| **16. Native WhatsApp Poster Share** | • Render summary card to Android `Bitmap`<br>• Fire native `Intent(Intent.ACTION_SEND)` directly to WhatsApp | **TO BUILD (Phase 3)** | *Target:* [`FullScorecardScreen.kt`](file:///home/shrik/Work/CricSaga/app/src/main/java/com/cricsaga/app/ui/scorecard/FullScorecardScreen.kt), [`ShareUtils.kt`](file:///home/shrik/Work/CricSaga/app/src/main/java/com/cricsaga/app/ui/utils/ShareUtils.kt) |
| **17. Interactive Creation Dialogs** | • "+ Create Tournament" dialog on Tournaments tab<br>• "+ Post Requirement" dialog on Discover tab | **TO BUILD (Phase 4)** | *Target:* [`TournamentListScreen.kt`](file:///home/shrik/Work/CricSaga/app/src/main/java/com/cricsaga/app/ui/tournaments/TournamentListScreen.kt), [`DiscoverScreen.kt`](file:///home/shrik/Work/CricSaga/app/src/main/java/com/cricsaga/app/ui/discover/DiscoverScreen.kt) |
| **18. Cloud Sync & Multi-Device** | • Node.js / PostgreSQL REST & WebSocket API<br>• WorkManager background synchronization<br>• Dedicated companion OCR extraction tool | **PHASE 5 (Post-MVP)** | Future Architecture |

---

## 3. End-to-End User Journeys

```
                                USER JOURNEY MAP
                                
[App Cold Launch] ──> (1.5s GPU Ball Swoop & Branding Fade) ──> [Home Feed]
                                                                     │
     ┌───────────────────────────────────────────────────────────────┤
     │                                                               │
[Start Match]                                                 [Pro Membership]
     │                                                               │
[Match Setup & Toss]                                          [Redeem Promo Code]
(Overs, Teams, Openers)                                        (Single-use unlock)
     │                                                               │
[1st Innings Scoring]                                         (100% Ad-Free active)
(Runs, Extras, Wickets, Undo)
     │
[1st Innings End Detection] ──> (All out or max overs)
     │
[2nd Innings Transition] ───> (Invert sides, Target Equation calculated)
     │
[2nd Innings Scoring] ─────> (Live Target Chase: Need X runs in Y balls)
     │
[Match Concluded] ──────────> [Victory Modal & Player of Match]
     │                               │
     ├───────────────────────────────┴───────────────────────────────┐
     │                                                               │
[Room Career Stats Updated]                                   [WhatsApp Summary Share]
("My Career" tab updates dynamically)                         (Native Image Intent dispatched)
```

---

## 4. Detailed Step-by-Step Execution Plan

### Phase 1: 2nd Innings Transition & Match Conclusion Lifecycle (Immediate Sprint)
*Goal: Enable end-to-end scoring of an entire 2-innings cricket match with win detection and celebration.*

- [ ] **Task 1.1: 1st Innings End Detection in Engine**
  - Trigger when `wickets >= 10` OR `legalBalls >= overs * 6`.
  - Mark `Innings 1` as `isCompleted = true`.
  - Calculate `targetRuns = innings1Total + 1`.
  - Calculate `targetOvers = totalOvers`.
- [ ] **Task 1.2: 2nd Innings Transition Flow & Openers Picker**
  - When Innings 1 finishes, display the **Innings Break Modal** showing:
    - Summary of Innings 1: Total runs, wickets, overs, top scorer, top bowler.
    - Required equation: `"Team B needs X runs from Y overs (RRR: Z.ZZ)"`.
  - Prompt scorer to select:
    - 2nd Innings Striker (from Team B).
    - 2nd Innings Non-Striker (from Team B).
    - 2nd Innings Opening Bowler (from Team A).
- [ ] **Task 1.3: 2nd Innings Chase Cockpit Header in UI**
  - Scoring header dynamically displays chase metrics:
    - `Target: X`
    - `Need: R runs from B balls`
    - `Required Run Rate (RRR): Z.ZZ`
    - `Current Run Rate (CRR): Z.ZZ`
- [ ] **Task 1.4: Match Conclusion Calculation**
  - **Win by Chasing (Batting Team):** `innings2Total >= targetRuns` → `"Team B won by (10 - wickets) wickets (with B balls remaining)"`.
  - **Win by Defending (Bowling Team):** `legalBalls >= overs * 6` OR `wickets >= 10` when `innings2Total < targetRuns - 1` → `"Team A won by (innings1Total - innings2Total) runs"`.
  - **Tie:** `innings2Total == targetRuns - 1` when overs completed or all out → `"Match Tied"`.
- [ ] **Task 1.5: Victory Celebration Dialog & Award**
  - Display clean, professional victory card with final scores.
  - Radio selection for **Player of the Match** award.
  - Primary button: `"View Full Scorecard"`.
  - Secondary button: `"Share Result to WhatsApp"`.

---

### Phase 2: Dynamic Room Career Stats Persistence
*Goal: Ensure match results automatically update the player's lifetime statistics in the "My Career" tab.*

- [ ] **Task 2.1: Compile Match Stats on Conclusion**
  - For every player who batted: sum runs, balls, 4s, 6s, dismissals.
  - For every player who bowled: sum overs, maidens, runs conceded, wickets taken.
- [ ] **Task 2.2: Upsert into Room `player_stats` Table**
  - Update `matchesPlayed = matchesPlayed + 1`.
  - Update batting figures: `runs = runs + matchRuns`, `innings = innings + 1`, compute updated `battingAverage` and `strikeRate`.
  - Update bowling figures: `wickets = wickets + matchWickets`, `overs = overs + matchOvers`, compute updated `bowlingAverage` and `economyRate`.
- [ ] **Task 2.3: Reactive UI Bind**
  - In [`PlayerProfileViewModel.kt`](file:///home/shrik/Work/CricSaga/app/src/main/java/com/cricsaga/app/ui/profile/PlayerProfileViewModel.kt), bind `PlayerStatsDao.getStatsFlow(userId)` so the profile instantly reflects real match data.

---

### Phase 3: Native WhatsApp Match Poster Sharing
*Goal: Enable scorers to export and share match posters directly to WhatsApp with 1 tap.*

- [ ] **Task 3.1: Bitmap Generation Utility**
  - Create [`ShareUtils.kt`](file:///home/shrik/Work/CricSaga/app/src/main/java/com/cricsaga/app/ui/utils/ShareUtils.kt).
  - Capture the WhatsApp summary card from [`FullScorecardScreen.kt`](file:///home/shrik/Work/CricSaga/app/src/main/java/com/cricsaga/app/ui/scorecard/FullScorecardScreen.kt) as an Android `Bitmap`.
- [ ] **Task 3.2: FileProvider & Cache Storage**
  - Save generated PNG to `context.cacheDir/shares/match_summary.png`.
  - Configure `androidx.core.content.FileProvider` in `AndroidManifest.xml` with `res/xml/file_paths.xml`.
- [ ] **Task 3.3: Native Intent Dispatch**
  - Launch `Intent(Intent.ACTION_SEND)` with `type = "image/png"` and `EXTRA_STREAM = contentUri`.
  - Automatically targets WhatsApp or system sharesheet.

---

### Phase 4: Interactive Creation Dialogs
*Goal: Allow users to create custom tournaments and post recruitment requests.*

- [ ] **Task 4.1: "+ Create Tournament" Dialog in `TournamentListScreen`**
  - Dialog inputs: Tournament Name, Ball Type (Tennis/Leather), Overs per match, Participating Teams selection.
  - Inserts new tournament and teams into Room DB.
  - Automatically initializes Points Table with 0 P, 0 W, 0 L, 0.000 NRR.
- [ ] **Task 4.2: "+ Post Requirement" Dialog in `DiscoverScreen`**
  - Dialog inputs: Category (Need Opponent / Need Player / Need Umpire), Date & Time, Ground location, Match format, WhatsApp contact number.
  - Inserts into local database and displays at the top of the Discover feed with active WhatsApp click-to-chat.

---

### Phase 5: Cloud Synchronization & Multi-Device Live Streaming (Post-MVP)
*Goal: Multi-device live viewer stream and remote backup.*

- [ ] Node.js / PostgreSQL REST & WebSocket backend API.
- [ ] WorkManager idempotent background sync.
- [ ] Standalone companion utility for scorecard screenshot OCR extraction.

---

## 5. Active Environment & Testing Directives

- **Physical Device:** Nothing / CMF Phone 1 (`A059`) connected wirelessly via ADB over Wi-Fi (`192.168.1.2`).
- **Memory Tuning:** Gradle daemon locked to `-Xmx2048m` with 2 workers to safeguard the Intel i3 CPU.
- **Git Policy:** **Zero automated git executions.** All git commits, pushes, and branch management are strictly handled by the user.
- **Documentation Policy:** All markdown documentation MUST reside in the `md/` directory.

---

## 6. Execution Priority Order

```
[NOW]   Phase 1: 2nd Innings Transition & Match Result Lifecycle
  ↓
[NEXT]  Phase 2: Dynamic Room Career Stats Persistence
  ↓
[THEN]  Phase 3: Native WhatsApp Match Poster Sharing
  ↓
[LATER] Phase 4: Interactive Creation Dialogs (+Tournament & +Requirement)
  ↓
[CLOUD] Phase 5: Backend API & Multi-Device Live Streaming
```
