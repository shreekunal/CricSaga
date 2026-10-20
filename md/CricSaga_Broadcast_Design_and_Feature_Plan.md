# CricSaga — Broadcast Design System & Platform Evolution Plan

**Project:** CricSaga ("Har Run Ki Kahani")  
**Target:** Production-Grade Grassroots Cricket Platform  
**Document Purpose:** Architectural and Design Specification for Professional UI Transformation, Zero-Cost Auth, Dynamic Themes, Haptics, and Notifications  
**Storage Location:** `md/CricSaga_Broadcast_Design_and_Feature_Plan.md`  
**Date:** October 2026  

---

## 1. Executive Summary & Core Design Philosophy

CricSaga is transitioning from a functional local MVP into an elite, broadcast-grade cricket platform designed to compete directly against CricHeroes and international sports products (FotMob, The Athletic, Apple Sports).

### Core Aesthetic Directives
1. **Zero-Emoji Mandate:** Emojis are strictly banned across all screens, headers, buttons, dialogs, and notifications. All visual indicators must use precision vector iconography (Material Symbols) and crisp, uppercase typographic micro-tags (`LIVE`, `POTM`, `CRR`, `RRR`, `TARGET`).
2. **Broadcast-Grade Visual Hierarchy:** Moving beyond flat "dark gray boxes" to a layered stadium aesthetic with subtle depth, luminous hairline borders, team crest colors, and athletic typography.
3. **Zero Recurring Infrastructure Cost:** Completely eliminate paid SMS OTP gateways (saving ₹5,000+ TRAI DLT registration and per-SMS fees) by leveraging native 1-Tap Google Sign-In with an instant offline Guest Scorer fallback.
4. **All-Weather Usability:** Full light and dark theme switching engineered specifically for pitch visibility under intense midday sunlight glare as well as night turf floodlights.

---

## 2. Authentication & Onboarding: Zero-Cost Architecture

### The SMS Problem
- TRAI DLT registration mandates business entity approvals, sender ID fees, and template verification.
- SMS gateways charge per delivery and fail under poor cellular reception on remote grounds.

### The Solution: 1-Tap Google Sign-In + Offline Guest Mode
1. **Google Sign-In (Primary):**
   - Cost: 0.00 INR forever for unlimited users via Google Play Services / Credential Manager.
   - Automatically retrieves the player's verified Full Name and Google Profile Photo.
   - Instant 1-tap authorization with zero typing, zero passwords, and zero SMS waiting times.
2. **Contact Number Collection (Optional, Zero SMS):**
   - During initial player profile setup, users provide an optional WhatsApp/phone number.
   - Stored purely as a contact attribute for team rosters and tournament matchmaking; requires zero OTP verification.
3. **Guest Pitch Mode (Fallback):**
   - Dedicated 1-tap action: "Continue as Guest Scorer".
   - Immediately enables full offline scoring capabilities without sign-in, essential when cellular connectivity is unavailable on the pitch.

---

## 3. Dynamic Theme Engine: Dark Stadium vs Crisp Sunlight Light

Grassroots cricket spans two extreme lighting environments: blazing midday outdoor grounds and night box-turf floodlights.

### Theme Palette Specifications

| Design Token | Dark Stadium Mode (Default) | Crisp Sunlight Light Mode | Usage |
| :--- | :--- | :--- | :--- |
| `backgroundCanvas` | `#0A0B0E` (Deep OLED Slate) | `#F4F5F8` (Clean Pitch Off-White) | Root scaffold canvas |
| `surfaceCard` | `#14171F` (Charcoal Slate) | `#FFFFFF` (Pure White) | Scorecards, player cards, modules |
| `surfaceElevated` | `#1C202B` (Elevated Slate) | `#EDF0F5` (High-Contrast Neutral) | Keypad buttons, badge backgrounds |
| `cardBorder` | `#222733` (0.5dp Hairline Slate) | `#D1D5DB` (0.8dp Contrast Border) | Card structural definition |
| `brandPrimary` | `#E53935` (Broadcast Crimson) | `#C62828` (Deep Cricket Crimson) | Primary CTAs, active indicators |
| `textPrimary` | `#F9FAFB` (High-Contrast White) | `#0F172A` (Ink Black) | Runs, player names, primary headers |
| `textSecondary` | `#9CA3AF` (Muted Cool Gray) | `#475569` (Slate Gray) | Overs, strike rate, economy rate |
| `textTertiary` | `#64748B` (Subtle Slate) | `#94A3B8` (Muted Neutral) | Secondary labels, dates, venues |
| `accentFour` | `#D97706` (Cricket Amber) | `#B45309` (Deep Amber) | 4s, milestones, Pro accents |
| `accentSix` | `#16A34A` (Boundary Green) | `#15803D` (Forest Green) | 6s, match wins, positive run rates |
| `alertWicket` | `#DC2626` (Dismissal Red) | `#B91C1C` (Dark Alert Red) | Wickets, penalties, warnings |

### Technical Architecture
- `ThemeManager.kt`: Manages theme preferences (`DARK`, `LIGHT`, `SYSTEM`) via local storage.
- UI Toggle: Precision vector icon (`Icons.Default.DarkMode` / `Icons.Default.LightMode`) positioned in the top navigation app bar and inside the Settings screen.

---

## 4. Tactile Haptic Feedback Engine

Scorers on grassroots grounds cannot continuously look at the phone screen while tracking live deliveries and umpire signals. Haptics provide immediate thumb confirmation.

### Vibration Profile Matrix (`HapticManager.kt`)

| Action / Delivery | Haptic Vibration Profile | Sensation / Purpose |
| :--- | :--- | :--- |
| **Dot Ball / Singles (0, 1, 2, 3 runs)** | Single crisp micro-tap (12ms) | Subtle confirmation of registered ball |
| **Boundary (4 runs)** | Rapid double pulse (15ms - 30ms gap - 15ms) | Distinct tactile rhythm for 4s |
| **Maximum (6 runs)** | Deep double punch (25ms - 40ms gap - 25ms) | Energetic confirmation for sixes |
| **Wicket / Dismissal** | Sharp warning pulse (75ms firm buzz) | High-salience alert for fall of wicket |
| **Undo Action** | Triple warning buzz (15ms - 20ms - 15ms - 20ms - 15ms) | Clear confirmation that a delivery was reverted |
| **Over Completion** | Rhythmic pulse (30ms - 40ms - 30ms) | Signals over end and prompt for bowler selection |

- Settings Toggle: Scorer can toggle haptic feedback on or off at any time.

---

## 5. Dual-Layer Notification System

A multi-tiered notification system providing immediate in-app broadcast alerts during active play and native Android system notifications for background updates.

### Layer A: In-App Broadcast Toasts (Overlay Banner)
- Floating, high-contrast banner displayed at the top of the screen for 2.5 seconds.
- Driven by vector iconography and typographic tags:
  - `MILESTONE`: "Rohit Sharma reaches 50 (52 off 28 balls • 5 fours, 3 sixes)"
  - `INNINGS BREAK`: "Warriors set a target of 143 runs • Required Run Rate: 7.15"
  - `RESULT`: "Warriors won by 14 runs • Player of the Match: Jasprit Bumrah"
  - `REVERTED`: "Delivery 3.2 undone from match record"

### Layer B: Android System Notification Channels
- Channel 1: `cricsaga_match_events` (Importance: High)
  - Notifies users when an innings concludes or a match finishes.
- Channel 2: `cricsaga_community` (Importance: Default)
  - Alerts users to responses on their Discover "Looking For" requirements.

---

## 6. Broadcast-Grade UI & Collectible Player Card

### 6.1 The Collectible Athlete Card ("My Career" Tab)
Replaces the flat list view with a digital sports card:
- **Jersey Watermark:** A stylized, semi-transparent `#18` rendered in athletic grotesque font in the card header.
- **Player Archetype Tag:** Typographic pill badge (e.g., `[ POWER HITTER ]`, `[ DEATH BOWLER ]`, `[ ALL-ROUNDER ]`).
- **Metric Quad-Strip:** Clear numeric contrast for Matches, Runs, Wickets, and Average with bold athletic figures.
- **Honours & Milestone Shelf:** Vector-driven badges honoring personal career achievements:
  - `CENTURY CLUB` (Vector trophy icon)
  - `FIVE-FOR` (Vector target icon)
  - `SUPER STRIKER` (Vector lightning icon)
  - `SAFE HANDS` (Vector shield icon)
- **Share Card Action:** Single tap renders and exports the athlete card as a clean 1080x1920 graphic for Instagram stories or WhatsApp status.

### 6.2 Broadcast Live Match Radar ("Home" Tab)
Transforms match preview cards into a live sports television widget:
- **Team Crest Shields:** Geometric circular team shields featuring primary team colors (e.g., Royal Blue vs Gold Amber).
- **Chase Pressure Bar:** Dynamic progress bar comparing balls remaining against runs required:
  - Slate: Balanced equation (RRR <= 7.0).
  - Amber: High pressure (RRR 7.1 - 10.0).
  - Crimson: Critical pressure (RRR > 10.0).
- **Match Status:** High-contrast `LIVE` vector dot with CRR and over details.

---

## 7. Secondary UI Platform Deepening (Phase 6)

1. **Public Player Detail Screen (`PlayerDetailScreen.kt`):**
   - Tapping any player on a scorecard, leaderboard, or team squad opens their public career record, batting/bowling tables, and recent matches.
2. **Team Hub & Full Squad Management (`TeamDetailScreen.kt`):**
   - Full 11-15 player squad roster.
   - Captain (`C`), Vice-Captain (`VC`), and Wicketkeeper (`WK`) badges.
   - Team record summary (Played, Won, Lost, Win %).
   - Add/remove player actions.
3. **App Settings & Preferences (`SettingsScreen.kt`):**
   - Theme Selector (Dark Stadium / Crisp Sunlight / Follow System).
   - Haptic Feedback Toggle.
   - Default match rules (Overs: 8, 10, 15, 20; Ball: Tennis, Leather).
   - Data Management (Clear cache, reset demo data).
   - About CricSaga, Open-source licenses, Terms & Privacy.
4. **Tournament Fixture Linkage:**
   - Tapping a fixture under the Tournament tab opens `MatchSetup` pre-configured for those two teams and linked to the tournament ID.
   - Concluding the match immediately updates the tournament Net Run Rate (NRR) and leaderboards.

---

## 8. Sequential Implementation Roadmap

```
PHASE 5.1: Zero-Cost Auth & Onboarding
  - 1-Tap Google Sign-In UI with Guest Scorer pitch mode
  - Profile setup (Role, RHB/LHB, Bowling, City, Jersey)
  - Edit Profile modal with reactive persistence

PHASE 5.2: Dynamic Theme Engine & Styling
  - ThemeManager with Dark Stadium and Crisp Sunlight Light modes
  - High-contrast sunlight tokens for outdoor play
  - Top app bar vector theme toggle

PHASE 5.3: Haptics & Notification Systems
  - HapticManager for 0-6, wicket, undo pulses
  - In-app milestone overlay toast for 50s, 100s, wickets, wins

PHASE 6: Secondary UI Deepening & Team Hub
  - Public Player Detail Screen
  - Team Hub with full 15-player squad and role badges
  - Settings & Preferences Screen
  - Tournament fixture scoring linkage

PHASE 7: Visual Match Analytics (Manhattan, Worm, Wagon Wheel)
PHASE 8: Grassroots Custom Rules (Box cricket, Super Over)
PHASE 9: Tournament Suite & A4 PDF Match Scoresheet Export
```

---

## 9. Verification & Constraints
- **Hardware Protection:** Gradle daemon strictly limited to `-Xmx2048m` with 2 workers.
- **Git Policy:** Zero automated git commands. All version control managed by user.
- **Documentation Policy:** All markdown documentation preserved in `md/`.
- **Zero-Emoji Compliance:** Verified against all files and UI components.
