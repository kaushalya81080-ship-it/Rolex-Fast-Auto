# Rolex Fast Auto - Setup, Architecture & Build Guide

## 1. Project Overview & Configuration
- **Application Name:** Rolex Fast Auto
- **Platform:** Android Only (Jetpack Compose, Kotlin DSL)
- **Target Game:** Miniclip's *Carrom Pool: Disc Game* ONLY
- **Theme:** Luxury Dark Onyx (`#0A0B0E`, `#13151B`) and Rolex Gold (`#FFD700`, `#C59B27`) with Emerald Green accents (`#006039`)
- **Backend Compatibility:** Supabase PostgreSQL + Android Room Local Database Cache

---

## 2. Separate License Systems Architecture

Rolex Fast Auto strictly segregates administrative access from customer gameplay access:

### A. Admin Panel License System
- **Key Prefix:** `RFAX-ADM-XXXX-XXXX`
- **Initial Setup Cost:** ₹500 (Project configured price)
- **Privileges:** Administrator Dashboard access, AutoPlay license key generation, custom pricing configuration, payment approval/rejection center, audit log inspection, license revocation.
- **Security Rule:** An Admin Panel Key **NEVER** activates Carrom Pool AutoPlay or gameplay features.

### B. AutoPlay License System
- **Key Prefix:** `RFAX-PLAY-XXXX-XXXX`
- **Supported Plans:**
  - 1 Day Trial Key (Configurable, default ₹99)
  - 2 Days Weekend Pass (default ₹180)
  - 3 Days Standard Pass (default ₹250)
  - 7 Days Weekly VIP (default ₹500)
  - 30 Days Monthly Pro (default ₹1500)
  - Custom Duration (Admin configurable)
- **Privileges:** Carrom Simulator Auto-Aim trajectory calculations, cushion bounce reflection paths, floating overlay controls.
- **Security Rule:** An AutoPlay Key **NEVER** grants administrative permissions. Expiry and validity are verified against server/monotonic timestamps to prevent local device clock tampering.

---

## 3. Customer Buy Key & Payment Workflow
1. **Plan Selection:** Customer selects their desired duration pass.
2. **Dynamic Price:** The app displays the real-time price configured by the administrator in the Admin Panel.
3. **Payment Details:** App displays Administrator UPI VPA (default `rolexfastauto@icici`, customizable) and interactive QR Code.
4. **UTR Submission:** Customer transfers funds via any UPI app (GPay, PhonePe, Paytm, BHIM) and submits the 12-digit Bank Reference / UTR Number.
5. **Backend Verification:** Payment is recorded with status `PENDING`.
6. **Admin Approval:** Administrator reviews transactions in the Admin Panel and taps **Approve & Issue Key**. The system atomically issues the matching license key.
7. **Instant Activation:** Customer receives their unique key and activates it in the app.

---

## 4. Carrom Pool Account Compatibility & Safety
Rolex Fast Auto is a high-grade companion and trajectory calculation utility designed with zero tolerance for game account compromises:
- **No Password Interception:** Rolex Fast Auto never asks for or intercepts Carrom Pool credentials, session cookies, or tokens.
- **Supported Login Guidelines:**
  - **Google Play Games:** Official guidance for cloud synchronization and striker preservation.
  - **Facebook Login:** Instructions to link official Facebook OAuth without exposing credentials.
  - **Guest Mode Safeguard:** Warning against device-only Guest storage; guidance on linking to cloud before using external utilities.
- **Game Launcher Shortcut:** Directly launches Miniclip's official package `com.miniclip.carrom`.

---

## 5. Android Permissions & Floating Overlay
- `android.permission.INTERNET`: Backend licensing and verification.
- `android.permission.ACCESS_NETWORK_STATE`: Network check.
- `android.permission.SYSTEM_ALERT_WINDOW`: Android Display Over Other Apps permission to render the non-intrusive floating control panel and aim guideline overlay.
- `android.permission.FOREGROUND_SERVICE`: Background overlay maintenance.

---

## 6. Build Instructions
To build the application on any standard Android development machine:

```bash
# Clone the repository
git clone <repo-url>
cd rolex-fast-auto

# Build debug APK
gradle assembleDebug

# Output APK path:
# app/build/outputs/apk/debug/app-debug.apk
```

---

## 7. Signed APK Generation Instructions
To generate a signed release APK or Android App Bundle (AAB):

```bash
# 1. Generate Keystore (if not already existing)
keytool -genkey -v -keystore my-upload-key.jks -alias upload \
  -keyalg RSA -keysize 2048 -validity 10000

# 2. Set environment variables
export KEYSTORE_PATH="$(pwd)/my-upload-key.jks"
export STORE_PASSWORD="your_keystore_password"
export KEY_PASSWORD="your_key_password"

# 3. Assemble Release APK
gradle assembleRelease

# Output signed APK:
# app/build/outputs/apk/release/app-release.apk
```

---

## 8. Supabase Backend Deployment
1. Open your Supabase Project Dashboard.
2. Navigate to the **SQL Editor**.
3. Copy the contents of `supabase_schema.sql` (located in the project root) and run the script.
4. This creates all 8 tables, indexes, Row Level Security (RLS) policies, and the `verify_payment_and_issue_key` stored procedure.
