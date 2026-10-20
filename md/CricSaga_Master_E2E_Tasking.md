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
| **08. Discover Hub Tab** | • "Looking For" matchmaker (Opponents, Players, Umpires)<br>• Direct WhatsApp click-to-chat action<br>• Grounds & Turfs directory<br>• Interactive "+ Post Requirement" modal | **COMPLETED** | [`DiscoverScreen.kt`](file:///home/shrik/Work/CricSaga/app/src/main/java/com/cricsaga/app/ui/discover/DiscoverScreen.kt) |
| **09. Tournaments Hub Tab** | • League Points Table with ICC Net Run Rate (NRR)<br>• Leaderboards (Most Runs & Most Wickets)<br>• Interactive "+ Create Tournament" modal | **COMPLETED** | [`TournamentListScreen.kt`](file:///home/shrik/Work/CricSaga/app/src/main/java/com/cricsaga/app/ui/tournaments/TournamentListScreen.kt) |
| **10. My Career Hub Tab** | • Profile header card<br>• 4-metric hero strip (Matches, Runs, Wickets, Average)<br>• Clean tabular statistics<br>• Dynamic Room SQLite live updates | **COMPLETED** | [`PlayerProfileScreen.kt`](file:///home/shrik/Work/CricSaga/app/src/main/java/com/cricsaga/app/ui/profile/PlayerProfileScreen.kt) |
| **11. Match Setup & Toss** | • Team selection (Team A vs Team B)<br>• Overs per side configuration<br>• Toss winner & elected decision (Bat / Bowl)<br>• Opening Striker, Non-Striker & Opening Bowler selection | **COMPLETED** | [`MatchSetupScreen.kt`](file:///home/shrik/Work/CricSaga/app/src/main/java/com/cricsaga/app/ui/matchsetup/MatchSetupScreen.kt) |
| **12. Scoring Cockpit & Dialogs** | • 12-button keypad matching CricHeroes muscle memory<br>• Live over timeline with color-coded ball tokens<br>• Dismissal bottom sheet (10 modes + fielder picker)<br>• Bowler change dialog enforcing consecutive over rule | **COMPLETED** | [`ScoringScreen.kt`](file:///home/shrik/Work/CricSaga/app/src/main/java/com/cricsaga/app/ui/scoring/ScoringScreen.kt), [`ScoringViewModel.kt`](file:///home/shrik/Work/CricSaga/app/src/main/java/com/cricsaga/app/ui/scoring/ScoringViewModel.kt) |
| **13. Full Scorecard & Summary** | • Batting scorecard (Runs, Balls, 4s, 6s, SR, Dismissal)<br>• Bowling scorecard (O, M, R, W, ER)<br>• Fall of Wickets table<br>• WhatsApp Status match summary poster card | **COMPLETED** | [`FullScorecardScreen.kt`](file:///home/shrik/Work/CricSaga/app/src/main/java/com/cricsaga/app/ui/scorecard/FullScorecardScreen.kt) |
| **14. 2nd Innings & Match Result** | • Detect 1st Innings end (10 wickets down or overs finished)<br>• 2nd Innings opener selection<br>• Live Target Chase Equation (`Need X runs in Y balls \| RRR: Z.ZZ`)<br>• Win condition calculation & Match Result modal<br>• Player of the Match award selection | **COMPLETED** | [`ScoringEngine.kt`](file:///home/shrik/Work/CricSaga/app/src/main/java/com/cricsaga/app/domain/engine/ScoringEngine.kt), [`ScoringViewModel.kt`](file:///home/shrik/Work/CricSaga/app/src/main/java/com/cricsaga/app/ui/scoring/ScoringViewModel.kt), [`ScoringScreen.kt`](file:///home/shrik/Work/CricSaga/app/src/main/java/com/cricsaga/app/ui/scoring/ScoringScreen.kt) |
| **15. Dynamic Career Persistence** | • Aggregate finished match stats into Room `player_stats`<br>• Auto-update "My Career" tab with live career records | **COMPLETED** | [`MatchRepository.kt`](file:///home/shrik/Work/CricSaga/app/src/main/java/com/cricsaga/app/data/repository/MatchRepository.kt), [`PlayerProfileScreen.kt`](file:///home/shrik/Work/CricSaga/app/src/main/java/com/cricsaga/app/ui/profile/PlayerProfileScreen.kt) |
| **16. Native WhatsApp Poster Share** | • Render summary card to Android `Bitmap`<br>• Fire native `Intent(Intent.ACTION_SEND)` directly to WhatsApp | **COMPLETED** | [`ShareUtils.kt`](file:///home/shrik/Work/CricSaga/app/src/main/java/com/cricsaga/app/ui/utils/ShareUtils.kt), [`FullScorecardScreen.kt`](file:///home/shrik/Work/CricSaga/app/src/main/java/com/cricsaga/app/ui/scorecard/FullScorecardScreen.kt) |
| **17. Interactive Creation Dialogs** | • "+ Create Tournament" dialog on Tournaments tab<br>• "+ Post Requirement" dialog on Discover tab | **COMPLETED** | [`TournamentListScreen.kt`](file:///home/shrik/Work/CricSaga/app/src/main/java/com/cricsaga/app/ui/tournaments/TournamentListScreen.kt), [`DiscoverScreen.kt`](file:///home/shrik/Work/CricSaga/app/src/main/java/com/cricsaga/app/ui/discover/DiscoverScreen.kt) |
| **18. Account Creation & Auth** | • Phone number login with +91 country prefix<br>• OTP verification (dev/offline bypass & instant verify)<br>• Player profile setup (Role, RHB/LHB, Bowling, City, Jersey)<br>• "Edit Profile" dialog in My Career tab<br>• Session persistence & guest mode | **IN PROGRESS (Phase 5)** | [`ui/auth/`](file:///home/shrik/Work/CricSaga/app/src/main/java/com/cricsaga/app/ui/auth), [`AuthManager.kt`](file:///home/shrik/Work/CricSaga/app/src/main/java/com/cricsaga/app/data/local/AuthManager.kt) |
| **19. Secondary UI & Team Hub** | • Public Player Detail screen (tap player from scorecard/table)<br>• Team Hub & Squad roster (11-15 players, C/VC/WK badges)<br>• App Settings screen (overs default, ground mode, export)<br>• Tournament fixture linkage to live scoring | **PLANNED (Phase 6)** | `ui/player/`, `ui/teams/`, `ui/settings/` |
| **20. Visual Match Analytics** | • Over-by-over Manhattan bar chart<br>• Comparative Cumulative Worm graph (Innings 1 vs 2)<br>• CRR vs RRR trajectory curve<br>• 8-zone Wagon Wheel shot direction radar | **PLANNED (Phase 7)** | `ui/charts/` |
| **21. Scorer Assist & Audio** | • Native TTS audio commentary (English/Hindi)<br>• High-contrast outdoor sunlight mode<br>• Multi-pattern haptic feedback for no-look scoring | **PLANNED (Phase 8)** | `ui/scoring/assist/` |
| **22. Grassroots Custom Rules** | • Box/Turf cricket penalty rules (2-run wide, no re-ball)<br>• Dynamic bowler max overs quota validation<br>• Tied match Super Over shootout engine | **PLANNED (Phase 9)** | `domain/rules/` |
| **23. Tournament Organizer Suite**| • Visual knockout & stage bracket tree (Group -> Semis -> Final)<br>• Official ICC A4 PDF scoresheet generation & WhatsApp share<br>• Squad roster & jersey number management | **PLANNED (Phase 10)** | `ui/tournament/bracket/`, `ui/utils/PdfExport.kt` |
| **24. Cloud Sync & Live Web** | • Node.js / PostgreSQL REST & WebSocket API<br>• WorkManager background idempotent sync<br>• Zero-friction mobile-web live match link (`cricsaga.app/live/{id}`) | **PLANNED (Phase 11)** | Post-MVP Cloud |


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

- [x] **Task 1.1: 1st Innings End Detection in Engine**
  - Trigger when `wickets >= 10` OR `legalBalls >= overs * 6`.
  - Mark `Innings 1` as `isCompleted = true`.
  - Calculate `targetRuns = innings1Total + 1`.
  - Calculate `targetOvers = totalOvers`.
- [x] **Task 1.2: 2nd Innings Transition Flow & Openers Picker**
  - When Innings 1 finishes, display the **Innings Break Modal** showing:
    - Summary of Innings 1: Total runs, wickets, overs, top scorer, top bowler.
    - Required equation: `"Team B needs X runs from Y overs (RRR: Z.ZZ)"`.
  - Prompt scorer to select:
    - 2nd Innings Striker (from Team B).
    - 2nd Innings Non-Striker (from Team B).
    - 2nd Innings Opening Bowler (from Team A).
- [x] **Task 1.3: 2nd Innings Chase Cockpit Header in UI**
  - Scoring header dynamically displays chase metrics:
    - `Target: X`
    - `Need: R runs from B balls`
    - `Required Run Rate (RRR): Z.ZZ`
    - `Current Run Rate (CRR): Z.ZZ`
- [x] **Task 1.4: Match Conclusion Calculation**
  - **Win by Chasing (Batting Team):** `innings2Total >= targetRuns` → `"Team B won by (10 - wickets) wickets (with B balls remaining)"`.
  - **Win by Defending (Bowling Team):** `legalBalls >= overs * 6` OR `wickets >= 10` when `innings2Total < targetRuns - 1` → `"Team A won by (innings1Total - innings2Total) runs"`.
  - **Tie:** `innings2Total == targetRuns - 1` when overs completed or all out → `"Match Tied"`.
- [x] **Task 1.5: Victory Celebration Dialog & Award**
  - Display clean, professional victory card with final scores.
  - Radio selection for **Player of the Match** award.
  - Primary button: `"View Full Scorecard"`.
  - Secondary button: `"Share Result to WhatsApp"`.

---

### Phase 2: Dynamic Room Career Stats Persistence
*Goal: Ensure match results automatically update the player's lifetime statistics in the "My Career" tab.*

- [x] **Task 2.1: Compile Match Stats on Conclusion**
  - For every player who batted: sum runs, balls, 4s, 6s, dismissals.
  - For every player who bowled: sum overs, maidens, runs conceded, wickets taken.
- [x] **Task 2.2: Upsert into Room `player_stats` Table**
  - Update `matchesPlayed = matchesPlayed + 1`.
  - Update batting figures: `runs = runs + matchRuns`, `innings = innings + 1`, compute updated `battingAverage` and `strikeRate`.
  - Update bowling figures: `wickets = wickets + matchWickets`, `overs = overs + matchOvers`, compute updated `bowlingAverage` and `economyRate`.
- [x] **Task 2.3: Reactive UI Bind**
  - In [`PlayerProfileScreen.kt`](file:///home/shrik/Work/CricSaga/app/src/main/java/com/cricsaga/app/ui/profile/PlayerProfileScreen.kt), bind `PlayerStatsDao.getStatsFlowForUser(userId)` so the profile instantly reflects real match data.

---

### Phase 3: Native WhatsApp Match Poster Sharing
*Goal: Enable scorers to export and share match posters directly to WhatsApp with 1 tap.*

- [x] **Task 3.1: Bitmap Generation Utility**
  - Create [`ShareUtils.kt`](file:///home/shrik/Work/CricSaga/app/src/main/java/com/cricsaga/app/ui/utils/ShareUtils.kt).
  - Capture and generate high-res 1080x1350 match summary poster bitmap on device.
- [x] **Task 3.2: FileProvider & Cache Storage**
  - Save generated PNG to `context.cacheDir/images/match_poster.png`.
  - Configure `androidx.core.content.FileProvider` in `AndroidManifest.xml` with `res/xml/file_paths.xml`.
- [x] **Task 3.3: Native Intent Dispatch**
  - Launch `Intent(Intent.ACTION_SEND)` with `type = "image/png"` and `EXTRA_STREAM = contentUri`.
  - Automatically targets WhatsApp or system sharesheet.

---

### Phase 4: Interactive Creation Dialogs
*Goal: Allow users to create custom tournaments and post recruitment requests.*

- [x] **Task 4.1: "+ Create Tournament" Dialog in `TournamentListScreen`**
  - Dialog inputs: Tournament Name, Ball Type, Overs per match, Participating Teams selection.
  - Inserts and initializes Points Table with 0 P, 0 W, 0 L, 0.000 NRR.
- [x] **Task 4.2: "+ Post Requirement" Dialog in `DiscoverScreen`**
  - Dialog inputs: Category (Need Opponent / Need Player / Need Umpire), Date & Time, Ground location, Match format, WhatsApp contact number.
  - Renders directly at the top of the Discover feed with active WhatsApp click-to-chat.

---

### Phase 5: User Onboarding, Account Creation & Profile Customization
*Goal: Provide a production-grade, frictionless onboarding flow for new grassroots players with local persistence.*

- [ ] **Task 5.1: Auth State & Onboarding Gatekeeper in `MainActivity`**
  - Session manager (`AuthManager.kt`) checking if active user exists in Room `users` / preferences.
  - Smooth launch routing: Splash -> If new user: `PhoneLoginScreen`; If authenticated: `HomeScreen`.
  - Seamless "Continue as Guest" option so local scorers at the ground are never blocked.
- [ ] **Task 5.2: Phone Login & OTP Verification Screen (`PhoneLoginScreen.kt`)**
  - Modern +91 Indian mobile input with phone number validation.
  - 6-digit OTP code entry with auto-fill, resend countdown, and instant dev-bypass.
- [ ] **Task 5.3: Player Profile Creation Screen (`ProfileSetupScreen.kt`)**
  - Inputs: Full Name (required), Nickname, Jersey Number, City & Home Turf.
  - Selectable chips: Primary Playing Role (Batter, Bowler, All-Rounder, Wicketkeeper).
  - Selectable chips: Batting Style (RHB / LHB).
  - Selectable chips: Bowling Style (Right-arm Fast / Medium / Spin, Left-arm Fast / Spin).
  - Minimalist vector cricket avatar picker.
  - Inserts directly into Room SQLite `users` table and marks session active.
- [ ] **Task 5.4: Edit Profile Flow from "My Career" Tab**
  - Edit Profile modal dialog in `PlayerProfileScreen.kt` allowing real-time edits to name, city, styles, jersey number.
- [ ] **Task 5.5: Account Switcher & Logout Options**
  - Switch active player profile or log out back to onboarding.

---

### Phase 6: Secondary UI Deepening & Team Hub
*Goal: Complete essential secondary navigation and management screens for full platform parity.*

- [ ] **Task 6.1: Public Player Detail Screen (`PlayerDetailScreen.kt`)**
  - Clicking any player in a scorecard, tournament leaderboard, or team squad navigates to their full public profile card.
  - Displays their career batting/bowling statistics, playing style, and recent matches.
- [ ] **Task 6.2: Team Hub & Squad Roster Screen (`TeamDetailScreen.kt`)**
  - Tapping a team card opens full squad roster (11–15 players).
  - Captain (C), Vice Captain (VC), and Wicketkeeper (WK) role badges.
  - Add/remove players from squad and view team match record (P, W, L, Win %).
- [ ] **Task 6.3: App Settings & Preferences Screen (`SettingsScreen.kt`)**
  - Gear icon `(⚙)` in top app bar.
  - Match scoring defaults (default overs: 8/10/20, default ball type: Tennis/Leather).
  - Sunlight High-Contrast mode toggle, audio toggle, haptic vibration toggle.
  - Database backup / export / reset and About CricSaga section.
- [ ] **Task 6.4: Tournament Fixture Linkage to Live Scoring**
  - Tapping an upcoming fixture in Tournament fixtures launches `MatchSetup` pre-filled with the two teams and linked to the tournament ID.
  - Match conclusion automatically updates the tournament Points Table (NRR) and leaderboards.

---

### Phase 7: Visual Analytics & Match Progression Charts
*Goal: Provide broadcast-grade visual charts that players and captains love to analyze and share.*

- [ ] **Task 7.1: Over-by-Over Manhattan Bar Chart**
  - Jetpack Compose Canvas-rendered bar chart showing runs scored in each over.
  - Color-coded: Neutral slate bars for normal overs, amber for overs with boundaries, crimson badges for wickets taken in that over.
  - Interactive touch tooltips showing over number, runs conceded, and bowler name.
- [ ] **Task 7.2: Comparative Worm Graph (Cumulative Run Trajectory)**
  - Dual-line graph comparing Innings 1 vs Innings 2 cumulative score over balls/overs.
  - Highlight key events: Wickets (red markers) and 50/100 team milestones.
  - Clear visual indicator of when the chasing team surpassed or fell behind the required curve.
- [ ] **Task 7.3: Run Rate Progression & Equation Curve**
  - Real-time CRR vs RRR trajectory curve.
  - Displays over-by-over run rate fluctuations.
- [ ] **Task 7.4: Shot Direction & Wagon Wheel Radar**
  - 8-zone cricket field selector (Third Man, Point, Cover, Long-Off, Long-On, Mid-Wicket, Square Leg, Fine Leg).
  - Optional quick tap on shot zone after boundary/runs.
  - Interactive radial wagon wheel tab on player profile & match scorecard.

---

### Phase 8: Ground-Ready Scorer Assist & Audio Commentary
*Goal: Empower scorers on hot, noisy grounds with audio confirmation and sunlight readability.*

- [ ] **Task 8.1: Native Text-to-Speech (TTS) Audio Commentary**
  - Integrated Android `TextToSpeech` engine announcing scored deliveries automatically.
  - Dual language support: English ("Four runs! Rohit Sharma hits a boundary") and Hindi / Hinglish ("Chauka! Rohit Sharma 48 par").
  - Mute / Audio toggle switch on top scoring cockpit header.
- [ ] **Task 8.2: High-Sunlight Contrast Mode (Ground Sunlight Mode)**
  - High-contrast outdoor theme toggle for midday sun play.
  - Crisp high-contrast monochrome tokens with heavy black borders and pure white card surfaces ensuring 100% legibility under direct glare.
- [ ] **Task 8.3: Multi-Pattern Haptic Scorer Feedback**
  - Android `Vibrator` / `HapticFeedbackConstants` integration for thumb confirmation without screen glances:
    - 1-3 runs: Single gentle vibration pulse.
    - 4 / 6 boundaries: Double celebratory pulse.
    - Wicket / Dismissal: Sharp warning pulse.
    - Undo: Distinct triple pulse.

---

### Phase 9: Grassroots Custom Rules & Match Formats
*Goal: Support the diverse real-world rules of gully, turf, box, and corporate cricket.*

- [ ] **Task 9.1: Box & Turf Cricket Rule Engine**
  - Custom Wide penalty configuration: Standard (1 run + extra ball) vs Box Turf (2 runs direct, no re-ball).
  - Custom No-Ball penalty configuration: Free-hit toggle vs 2 runs direct.
  - Last Man Standing rule toggle: Allows the lone surviving batsman to complete the innings from one end.
- [ ] **Task 9.2: Dynamic Bowler Overs Limit & Quota Enforcement**
  - Match setup allows specifying maximum overs per individual bowler (e.g., 2 overs in an 8-over turf match, 4 overs in 20-over match).
  - Engine validates bowler quota and disables bowler in selection dialog once limit is reached.
- [ ] **Task 9.3: Tied Match Super Over Engine**
  - Automatic Super Over trigger when Innings 2 scores finish tied.
  - 1-over, 2-wicket mini-innings shootout lifecycle with dedicated result banners.

---

### Phase 10: Tournament Organizer Suite & PDF Scoresheet Export
*Goal: Provide tournament organizers with professional management and official printed match records.*

- [ ] **Task 10.1: Tournament Knockout & Stage Brackets**
  - Visual tournament bracket tree: Group Stage -> Quarter Finals -> Semi Finals -> Grand Final.
  - Automatic team qualification seeding based on final Points Table Net Run Rate (NRR).
- [ ] **Task 10.2: Official ICC Grassroots A4 PDF Match Scoresheet Export**
  - Android native `PdfDocument` zero-dependency generator.
  - Standard 2-page A4 match record including: Match details, playing XIs, full batting & bowling tables, fall of wickets, over progression summary, umpire signature fields, and official CricSaga seal.
  - 1-tap download to device storage and direct WhatsApp document share.
- [ ] **Task 10.3: Custom Team Squad & Jersey Management**
  - Manage 15-player team rosters with designated Captain (C), Vice Captain (VC), and Wicket Keeper (WK) tags.
  - Player jersey numbers and playing role badges (Top-order batter, Pace bowler, Spin all-rounder).

---

### Phase 11: Cloud Synchronization, Real-Time Web Viewer & Live Streaming (Post-MVP)
*Goal: Enable multi-device live streaming and cloud backup so fans and organizers can follow matches anywhere.*

- [ ] **Task 11.1: Lightweight Live Match Backend API**
  - Node.js (TypeScript) + Fastify backend with PostgreSQL & Prisma.
  - REST endpoints for match lifecycle + WebSocket / Server-Sent Events (SSE) for sub-second ball-by-ball updates.
- [ ] **Task 11.2: Offline-First Synchronization Engine**
  - `WorkManager` background sync worker dispatching queued deliveries to cloud.
  - Zero disruption to local scoring: scoring remains 100% responsive offline in SQLite; syncs quietly when network is detected.
- [ ] **Task 11.3: Mobile-Responsive Live Match Web Scoreboard**
  - Frictionless web viewer URL: `cricsaga.app/live/{matchId}`.
  - Anyone can open the link in any mobile or desktop browser to watch live ball-by-ball commentary without downloading the app.
- [ ] **Task 11.4: Scorecard Screenshot OCR Companion Tool**
  - Standalone utility extracting existing scoresheets or paper charts via OCR into digital match data.

---

## 5. Active Environment & Testing Directives

- **Physical Device:** Nothing / CMF Phone 1 (`A059`) connected wirelessly via ADB over Wi-Fi (`192.168.1.2`).
- **Memory Tuning:** Gradle daemon locked to `-Xmx2048m` with 2 workers to safeguard the Intel i3 CPU.
- **Git Policy:** **Zero automated git executions.** All git commits, pushes, and branch management are strictly handled by the user.
- **Documentation Policy:** All markdown documentation MUST reside in the `md/` directory.

---

## 6. Execution Priority Roadmap

```
[COMPLETED] Phase 1: 2nd Innings Transition & Match Conclusion Lifecycle
[COMPLETED] Phase 2: Dynamic Room Career Stats Persistence
[COMPLETED] Phase 3: Native WhatsApp Match Poster Sharing
[COMPLETED] Phase 4: Interactive Creation Dialogs (+Tournament & +Requirement)
     │
     ▼
[IN PROGRESS] Phase 5: User Onboarding, Account Creation & Profile Customization
     │
     ▼
[PHASE 6]   Phase 6: Secondary UI Deepening & Team Hub (Player Detail, Team Hub, Settings)
     │
     ▼
[PHASE 7]   Phase 7: Visual Analytics & Match Charts (Manhattan, Worm, Wagon Wheel)
     │
     ▼
[PHASE 8]   Phase 8: Ground-Ready Scorer Assist (TTS Audio, Sunlight Mode, Haptics)
     │
     ▼
[PHASE 9]   Phase 9: Grassroots Custom Rules (Box/Turf cricket, Super Over, Quotas)
     │
     ▼
[PHASE 10]  Phase 10: Tournament Organizer Suite (Knockout Brackets, Official A4 PDF Scoresheets)
     │
     ▼
[PHASE 11]  Phase 11: Cloud Sync, Real-Time Web Live Scoreboard & Multi-Device Streaming
```
