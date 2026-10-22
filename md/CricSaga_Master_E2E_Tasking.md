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
| **06. Home Feed Tab** | • Twitter-style Cricket Feed with "For You" & "Following" tabs<br>• Compact live match radar ticker & + Score Match launchpad<br>• Rich post composer (text, stats badge, photo picker)<br>• Like, Comment, Repost, Follow & DM interactions | **COMPLETED** | [`HomeScreen.kt`](file:///home/shrik/Work/CricSaga/app/src/main/java/com/cricsaga/app/ui/home/HomeScreen.kt) |
| **07. Matches Hub Tab** | • Live matches & Completed matches list<br>• Direct navigation to in-progress scoring or full scorecard | **COMPLETED** | [`MatchListScreen.kt`](file:///home/shrik/Work/CricSaga/app/src/main/java/com/cricsaga/app/ui/matches/MatchListScreen.kt) |
| **08. Discover Hub Tab** | • Individual-centric matchmaking (Need Player, Need Opponent, Need Scorer, Need Umpire)<br>• Urgent / Today quick filter<br>• Direct "MESSAGE" & "WHATSAPP" actions<br>• Complete tournament and ground removal | **COMPLETED** | [`DiscoverScreen.kt`](file:///home/shrik/Work/CricSaga/app/src/main/java/com/cricsaga/app/ui/discover/DiscoverScreen.kt) |
| **09. Direct Messages** | • Conversation list inbox with unread badges & search<br>• 1-on-1 ChatScreen with chronological bubbles<br>• Structured Match Challenge bubbles (overs, ball, venue, time)<br>• Integrated access via Home top bar icon, Player profiles, and Discover requirement cards | **COMPLETED** | [`ConversationListScreen.kt`](file:///home/shrik/Work/CricSaga/app/src/main/java/com/cricsaga/app/ui/messages/ConversationListScreen.kt), [`ChatScreen.kt`](file:///home/shrik/Work/CricSaga/app/src/main/java/com/cricsaga/app/ui/messages/ChatScreen.kt) |
| **10. My Career Hub Tab** | • Hero card with live Follower & Following counts<br>• 5-tab career record + Posts tab showing authored feed updates<br>• Public player profile detail screen with Follow/Unfollow toggle & Direct Message CTA | **COMPLETED** | [`PlayerProfileScreen.kt`](file:///home/shrik/Work/CricSaga/app/src/main/java/com/cricsaga/app/ui/profile/PlayerProfileScreen.kt), [`PlayerDetailScreen.kt`](file:///home/shrik/Work/CricSaga/app/src/main/java/com/cricsaga/app/ui/profile/PlayerDetailScreen.kt) |
| **11. Match Setup & Toss** | • Team selection (Team A vs Team B)<br>• Overs per side configuration<br>• Toss winner & elected decision (Bat / Bowl)<br>• Opening Striker, Non-Striker & Opening Bowler selection | **COMPLETED** | [`MatchSetupScreen.kt`](file:///home/shrik/Work/CricSaga/app/src/main/java/com/cricsaga/app/ui/matchsetup/MatchSetupScreen.kt) |
| **12. Scoring Cockpit & Dialogs** | • 12-button keypad matching CricHeroes muscle memory<br>• Live over timeline with color-coded ball tokens<br>• Dismissal bottom sheet (10 modes + fielder picker)<br>• Bowler change dialog enforcing consecutive over rule | **COMPLETED** | [`ScoringScreen.kt`](file:///home/shrik/Work/CricSaga/app/src/main/java/com/cricsaga/app/ui/scoring/ScoringScreen.kt), [`ScoringViewModel.kt`](file:///home/shrik/Work/CricSaga/app/src/main/java/com/cricsaga/app/ui/scoring/ScoringViewModel.kt) |
| **13. Full Scorecard & Summary** | • Batting scorecard (Runs, Balls, 4s, 6s, SR, Dismissal)<br>• Bowling scorecard (O, M, R, W, ER)<br>• Fall of Wickets table<br>• WhatsApp Status match summary poster card | **COMPLETED** | [`FullScorecardScreen.kt`](file:///home/shrik/Work/CricSaga/app/src/main/java/com/cricsaga/app/ui/scorecard/FullScorecardScreen.kt) |
| **14. 2nd Innings & Match Result** | • Detect 1st Innings end (10 wickets down or overs finished)<br>• 2nd Innings opener selection<br>• Live Target Chase Equation (`Need X runs in Y balls \| RRR: Z.ZZ`)<br>• Win condition calculation & Match Result modal<br>• Player of the Match award selection | **COMPLETED** | [`ScoringEngine.kt`](file:///home/shrik/Work/CricSaga/app/src/main/java/com/cricsaga/app/domain/engine/ScoringEngine.kt), [`ScoringViewModel.kt`](file:///home/shrik/Work/CricSaga/app/src/main/java/com/cricsaga/app/ui/scoring/ScoringViewModel.kt), [`ScoringScreen.kt`](file:///home/shrik/Work/CricSaga/app/src/main/java/com/cricsaga/app/ui/scoring/ScoringScreen.kt) |
| **15. Dynamic Career Persistence** | • Aggregate finished match stats into Room `player_stats`<br>• Auto-update "My Career" tab with live career records | **COMPLETED** | [`MatchRepository.kt`](file:///home/shrik/Work/CricSaga/app/src/main/java/com/cricsaga/app/data/repository/MatchRepository.kt), [`PlayerProfileScreen.kt`](file:///home/shrik/Work/CricSaga/app/src/main/java/com/cricsaga/app/ui/profile/PlayerProfileScreen.kt) |
| **16. Native WhatsApp Poster Share** | • Render summary card to Android `Bitmap`<br>• Fire native `Intent(Intent.ACTION_SEND)` directly to WhatsApp | **COMPLETED** | [`ShareUtils.kt`](file:///home/shrik/Work/CricSaga/app/src/main/java/com/cricsaga/app/ui/utils/ShareUtils.kt), [`FullScorecardScreen.kt`](file:///home/shrik/Work/CricSaga/app/src/main/java/com/cricsaga/app/ui/scorecard/FullScorecardScreen.kt) |
| **17. Interactive Creation Dialogs** | • "+ Post Requirement" dialog on Discover tab<br>• Compose Post dialog on Home feed | **COMPLETED** | [`DiscoverScreen.kt`](file:///home/shrik/Work/CricSaga/app/src/main/java/com/cricsaga/app/ui/discover/DiscoverScreen.kt), [`HomeScreen.kt`](file:///home/shrik/Work/CricSaga/app/src/main/java/com/cricsaga/app/ui/home/HomeScreen.kt) |
| **18. Account Creation & Auth** | • 1-Tap Google Sign-In UI with Guest Scorer pitch mode<br>• Player profile setup (Role, RHB/LHB, Bowling, City, Jersey)<br>• Native photo upload via Android Photo Picker & local storage<br>• "Edit Profile" dialog in My Career tab with live image preview<br>• Session persistence & guest mode | **COMPLETED** | [`ui/auth/`](file:///home/shrik/Work/CricSaga/app/src/main/java/com/cricsaga/app/ui/auth), [`AuthManager.kt`](file:///home/shrik/Work/CricSaga/app/src/main/java/com/cricsaga/app/data/local/AuthManager.kt), [`ImageUtils.kt`](file:///home/shrik/Work/CricSaga/app/src/main/java/com/cricsaga/app/ui/utils/ImageUtils.kt) |
| **19. Secondary UI & Team Hub** | • Public Player Detail screen (tap player from scorecard/table/leaderboard)<br>• Team Hub & Squad roster (15 players, C/VC/WK badges, add/remove member)<br>• App Settings screen (Dark Stadium / Crisp Sunlight theme switcher, haptics) | **COMPLETED** | [`PlayerDetailScreen.kt`](file:///home/shrik/Work/CricSaga/app/src/main/java/com/cricsaga/app/ui/profile/PlayerDetailScreen.kt), [`TeamDetailScreen.kt`](file:///home/shrik/Work/CricSaga/app/src/main/java/com/cricsaga/app/ui/teams/TeamDetailScreen.kt), [`SettingsScreen.kt`](file:///home/shrik/Work/CricSaga/app/src/main/java/com/cricsaga/app/ui/settings/SettingsScreen.kt) |
| **20. Visual Match Analytics** | • Over-by-over Manhattan bar chart (runs & wickets per over)<br>• Comparative Cumulative Worm graph (Innings 1 vs Innings 2 with target line)<br>• Dynamic fall of wicket indicators | **COMPLETED** | [`ManhattanChart.kt`](file:///home/shrik/Work/CricSaga/app/src/main/java/com/cricsaga/app/ui/analytics/ManhattanChart.kt), [`WormGraph.kt`](file:///home/shrik/Work/CricSaga/app/src/main/java/com/cricsaga/app/ui/analytics/WormGraph.kt), [`FullScorecardScreen.kt`](file:///home/shrik/Work/CricSaga/app/src/main/java/com/cricsaga/app/ui/scorecard/FullScorecardScreen.kt) |
| **21. Rankings & Leaderboards (Tab 4)** | • Replaces Messages chip on bottom navigation bar with `Screen.Rankings`<br>• Top Batters (Runs, Avg, SR, HS, 50s, 100s, 4s, 6s)<br>• Top Bowlers (Wkts, Economy, Overs, Maidens, Runs Conceded)<br>• MVP All-Rounders (Combined performance index & points)<br>• Team Standings (P, W, L, T, Win %, Points, Form Guide pills)<br>• Regional City filter chips (All Regions, Mumbai, Delhi, Ahmedabad, Bengaluru, etc.)<br>• Top 3 Podium gold/silver/bronze badges & interactive detail navigation | **COMPLETED** | [`RankingsScreen.kt`](file:///home/shrik/Work/CricSaga/app/src/main/java/com/cricsaga/app/ui/rankings/RankingsScreen.kt), [`RankingsModels.kt`](file:///home/shrik/Work/CricSaga/app/src/main/java/com/cricsaga/app/domain/model/RankingsModels.kt), [`AppNavigation.kt`](file:///home/shrik/Work/CricSaga/app/src/main/java/com/cricsaga/app/ui/navigation/AppNavigation.kt) |
| **21. Haptics & Notification Systems** | • Tactile HapticManager for 0-6, wicket, undo pulses<br>• Layer A: In-app broadcast floating toast overlay<br>• Layer B: Native Android Notification Channels (`cricsaga_match_events`, `cricsaga_community`) | **COMPLETED** | [`HapticManager.kt`](file:///home/shrik/Work/CricSaga/app/src/main/java/com/cricsaga/app/ui/utils/HapticManager.kt), [`BroadcastBanner.kt`](file:///home/shrik/Work/CricSaga/app/src/main/java/com/cricsaga/app/ui/utils/BroadcastBanner.kt), [`NotificationHelper.kt`](file:///home/shrik/Work/CricSaga/app/src/main/java/com/cricsaga/app/ui/utils/NotificationHelper.kt) |
| **22. Grassroots Custom Rules** | • Box/Turf cricket penalty rules (2-run wide)<br>• Dynamic bowler max overs quota validation<br>• Venue and pitch ground configuration in match setup | **COMPLETED** | [`MatchSetupScreen.kt`](file:///home/shrik/Work/CricSaga/app/src/main/java/com/cricsaga/app/ui/setup/MatchSetupScreen.kt) |
| **23. Tournament Suite & PDF Export** | • Room SQLite `TournamentEntity` and `TournamentDao`<br>• Dynamic points table & fixture generation<br>• Official ICC A4 PDF scoresheet generation & export | **COMPLETED** | [`TournamentEntity.kt`](file:///home/shrik/Work/CricSaga/app/src/main/java/com/cricsaga/app/data/local/entity/TournamentEntity.kt), [`TournamentDao.kt`](file:///home/shrik/Work/CricSaga/app/src/main/java/com/cricsaga/app/data/local/dao/TournamentDao.kt), [`PdfExportUtils.kt`](file:///home/shrik/Work/CricSaga/app/src/main/java/com/cricsaga/app/ui/utils/PdfExportUtils.kt) |
| **24. Cloud Sync & Live Web** | • Node.js / PostgreSQL REST & WebSocket API<br>• WorkManager background idempotent sync<br>• Zero-friction mobile-web live match link (`cricsaga.app/live/{id}`) | **PLANNED (Post-MVP)** | Post-MVP Cloud |


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

- [x] **Task 5.1: Auth State & Onboarding Gatekeeper in `MainActivity`**
  - Session manager ([`AuthManager.kt`](file:///home/shrik/Work/CricSaga/app/src/main/java/com/cricsaga/app/data/local/AuthManager.kt)) checking if active user exists in Room `users` / preferences.
  - Smooth launch routing: Splash -> If new user: `PhoneLoginScreen`; If authenticated: `HomeScreen`.
  - Seamless "Continue as Guest" option so local scorers at the ground are never blocked.
- [x] **Task 5.2: Phone Login & OTP Verification Screen ([`PhoneLoginScreen.kt`](file:///home/shrik/Work/CricSaga/app/src/main/java/com/cricsaga/app/ui/auth/PhoneLoginScreen.kt))**
  - Modern +91 Indian mobile input with phone number validation.
  - 6-digit OTP code entry with auto-fill, resend countdown, and instant dev-bypass.
- [x] **Task 5.3: Player Profile Creation Screen ([`ProfileSetupScreen.kt`](file:///home/shrik/Work/CricSaga/app/src/main/java/com/cricsaga/app/ui/auth/ProfileSetupScreen.kt))**
  - Inputs: Full Name (required), Nickname, Jersey Number, City & Home Turf.
  - Selectable chips: Primary Playing Role (Batter, Bowler, All-Rounder, Wicketkeeper).
  - Selectable chips: Batting Style (RHB / LHB).
  - Selectable chips: Bowling Style (Right-arm Fast / Medium / Spin, Left-arm Fast / Spin).
  - Minimalist vector cricket avatar picker.
  - Inserts directly into Room SQLite `users` table and marks session active.
- [x] **Task 5.4: Edit Profile Flow from "My Career" Tab**
  - Edit Profile modal dialog in [`PlayerProfileScreen.kt`](file:///home/shrik/Work/CricSaga/app/src/main/java/com/cricsaga/app/ui/profile/PlayerProfileScreen.kt) allowing real-time edits to name, city, styles, jersey number.
- [x] **Task 5.5: Account Switcher & Logout Options**
  - Switch active player profile or log out back to onboarding via [`AuthManager.kt`](file:///home/shrik/Work/CricSaga/app/src/main/java/com/cricsaga/app/data/local/AuthManager.kt).

---

### Phase 6: Secondary UI Deepening & Team Hub
*Goal: Complete essential secondary navigation and management screens for full platform parity.*

- [x] **Task 6.1: Public Player Detail Screen ([`PlayerDetailScreen.kt`](file:///home/shrik/Work/CricSaga/app/src/main/java/com/cricsaga/app/ui/profile/PlayerDetailScreen.kt))**
  - Clicking any player in a scorecard, tournament leaderboard, or team squad navigates to their full public profile card.
  - Displays their career batting/bowling statistics, playing style, and recent matches.
- [x] **Task 6.2: Team Hub & Squad Roster Screen ([`TeamDetailScreen.kt`](file:///home/shrik/Work/CricSaga/app/src/main/java/com/cricsaga/app/ui/teams/TeamDetailScreen.kt))**
  - Tapping a team card opens full squad roster (11–15 players).
  - Captain (C), Vice Captain (VC), and Wicketkeeper (WK) role badges.
  - Add/remove players from squad and view team match record (P, W, L, Win %).
- [x] **Task 6.3: App Settings & Preferences Screen ([`SettingsScreen.kt`](file:///home/shrik/Work/CricSaga/app/src/main/java/com/cricsaga/app/ui/settings/SettingsScreen.kt))**
  - Gear icon in top app bar.
  - Match scoring defaults (default overs: 8/10/20, default ball type: Tennis/Leather).
  - Sunlight High-Contrast mode toggle, audio toggle, haptic vibration toggle.
  - Database backup / export / reset and About CricSaga section.
- [x] **Task 6.4: Tournament Fixture Linkage to Live Scoring**
  - Tapping an upcoming fixture in Tournament fixtures launches `MatchSetup` pre-filled with the two teams and linked to the tournament ID.
  - Match conclusion automatically updates the tournament Points Table (NRR) and leaderboards.

---

### Phase 7: Visual Analytics & Match Progression Charts
*Goal: Provide broadcast-grade visual charts that players and captains love to analyze and share.*

- [x] **Task 7.1: Over-by-Over Manhattan Bar Chart ([`ManhattanChart.kt`](file:///home/shrik/Work/CricSaga/app/src/main/java/com/cricsaga/app/ui/analytics/ManhattanChart.kt))**
  - Jetpack Compose Canvas-rendered bar chart showing runs scored in each over.
  - Color-coded: Neutral slate bars for normal overs, amber for overs with boundaries, crimson badges for wickets taken in that over.
  - Interactive touch tooltips showing over number, runs conceded, and bowler name.
- [x] **Task 7.2: Comparative Worm Graph (Cumulative Run Trajectory) ([`WormGraph.kt`](file:///home/shrik/Work/CricSaga/app/src/main/java/com/cricsaga/app/ui/analytics/WormGraph.kt))**
  - Dual-line graph comparing Innings 1 vs Innings 2 cumulative score over balls/overs.
  - Highlight key events: Wickets (red markers) and 50/100 team milestones.
  - Clear visual indicator of when the chasing team surpassed or fell behind the required curve.
- [ ] **Task 7.3: Run Rate Progression & Equation Curve (Post-MVP)**
- [ ] **Task 7.4: Shot Direction & Wagon Wheel Radar (Post-MVP)**

---

### Phase 8: Ground-Ready Scorer Assist & Audio Commentary
*Goal: Empower scorers on hot, noisy grounds with audio confirmation and sunlight readability.*

- [ ] **Task 8.1: Native Text-to-Speech (TTS) Audio Commentary (Post-MVP)**
- [x] **Task 8.2: High-Sunlight Contrast Mode (Ground Sunlight Mode)**
  - High-contrast outdoor theme toggle for midday sun play in [`Theme.kt`](file:///home/shrik/Work/CricSaga/app/src/main/java/com/cricsaga/app/ui/theme/Theme.kt) and [`Color.kt`](file:///home/shrik/Work/CricSaga/app/src/main/java/com/cricsaga/app/ui/theme/Color.kt).
  - Crisp high-contrast monochrome tokens with heavy black borders and pure white card surfaces ensuring 100% legibility under direct glare.
- [x] **Task 8.3: Multi-Pattern Haptic Scorer Feedback ([`HapticManager.kt`](file:///home/shrik/Work/CricSaga/app/src/main/java/com/cricsaga/app/ui/utils/HapticManager.kt))**
  - Android `Vibrator` / `HapticFeedbackConstants` integration for thumb confirmation without screen glances:
    - 1-3 runs: Single gentle vibration pulse.
    - 4 / 6 boundaries: Double celebratory pulse.
    - Wicket / Dismissal: Sharp warning pulse.
    - Undo: Distinct triple pulse.

---

### Phase 9: Grassroots Custom Rules & Match Formats
*Goal: Support the diverse real-world rules of gully, turf, box, and corporate cricket.*

- [x] **Task 9.1: Box & Turf Cricket Rule Engine**
  - Custom Wide penalty configuration: Standard (1 run + extra ball) vs Box Turf (2 runs direct, no re-ball) in [`MatchSetupScreen.kt`](file:///home/shrik/Work/CricSaga/app/src/main/java/com/cricsaga/app/ui/setup/MatchSetupScreen.kt).
  - Custom No-Ball penalty configuration: Free-hit toggle vs 2 runs direct.
- [x] **Task 9.2: Dynamic Bowler Overs Limit & Quota Enforcement**
  - Match setup allows specifying maximum overs per individual bowler.
  - Engine validates bowler quota and disables bowler in selection dialog once limit is reached.
- [ ] **Task 9.3: Tied Match Super Over Engine (Post-MVP)**

---

### Phase 10: Tournament Organizer Suite & PDF Scoresheet Export
*Goal: Provide tournament organizers with professional management and official printed match records.*

- [ ] **Task 10.1: Tournament Knockout & Stage Brackets (Post-MVP)**
- [x] **Task 10.2: Official ICC Grassroots A4 PDF Match Scoresheet Export ([`PdfExportUtils.kt`](file:///home/shrik/Work/CricSaga/app/src/main/java/com/cricsaga/app/ui/utils/PdfExportUtils.kt))**
  - Android native `PdfDocument` zero-dependency generator.
  - Standard 2-page A4 match record including: Match details, playing XIs, full batting & bowling tables, fall of wickets, over progression summary, umpire signature fields, and official CricSaga seal.
  - 1-tap download to device storage and direct WhatsApp document share.
- [x] **Task 10.3: Custom Team Squad & Jersey Management ([`TeamDetailScreen.kt`](file:///home/shrik/Work/CricSaga/app/src/main/java/com/cricsaga/app/ui/teams/TeamDetailScreen.kt))**
  - Manage 15-player team rosters with designated Captain (C), Vice Captain (VC), and Wicket Keeper (WK) tags.
  - Player jersey numbers and playing role badges (Top-order batter, Pace bowler, Spin all-rounder).

---

### Phase 11: Cloud Synchronization, Real-Time Web Viewer & Live Streaming (Post-MVP)
*Goal: Enable multi-device live streaming and cloud backup so fans and organizers can follow matches anywhere.*

- [ ] **Task 11.1: Lightweight Live Match Backend API**
- [ ] **Task 11.2: Offline-First Synchronization Engine**
- [ ] **Task 11.3: Mobile-Responsive Live Match Web Scoreboard**
- [ ] **Task 11.4: Scorecard Screenshot OCR Companion Tool**

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
[COMPLETED] Phase 5: User Onboarding, Account Creation & Profile Customization
[COMPLETED] Phase 6: Secondary UI Deepening & Team Hub (Player Detail, Team Hub, Settings)
[COMPLETED] Phase 7: Visual Analytics & Match Charts (Manhattan, Worm Graph)
[COMPLETED] Phase 8: Ground-Ready Scorer Assist (Sunlight Contrast Theme, Tactile Haptics)
[COMPLETED] Phase 9: Grassroots Custom Rules (Box/Turf Wide Rules, Bowler Quotas)
[COMPLETED] Phase 10: Tournament Suite (Points Table NRR, Official A4 PDF Scoresheets)
     │
     ▼
[POST-MVP]  Phase 11: Cloud Sync, Real-Time Web Live Scoreboard & Multi-Device Streaming
```
