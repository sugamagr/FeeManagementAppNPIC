# NPIC Fees Management App

A comprehensive Fees Management Android App for Navodit Public Inter College. This app digitizes the manual fee collection and ledger management process.

## Features

### Core Features
- **Student Management**: Add, edit, view, and search students with complete details
- **Fee Collection**: Record fee payments with manual receipt number entry
- **Student Ledger**: Track all debit/credit transactions with running balance
- **Audit Log**: All changes are logged and can be viewed/reverted

### Centralized Settings
- **School Profile**: Configure school name, address, contact details
- **Academic Sessions**: Manage academic years (April to March)
- **Fee Structure**: 
  - Monthly fees for NC to 8th class
  - Annual/lump sum fees for 9th to 12th class
  - Admission fee (one-time)
  - Registration fee (for 9th-12th)
- **Transport Routes**: Manage bus routes and monthly transport fees

### Fee Types
| Class | Fee Type | Discount |
|-------|----------|----------|
| NC to 8th | Monthly | Pay 12 months, get 1 month free |
| 9th to 12th | Annual (Lump Sum) | None |

### Reports
- Daily Collection Report
- Defaulters List (students with dues)
- Class-wise Summary
- Receipt Register

## Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose with Material 3
- **Architecture**: Clean Architecture + MVVM
- **Database**: Room (local SQLite)
- **DI**: Hilt
- **Navigation**: Navigation Compose
- **Preferences**: DataStore

## Project Structure

```
app/
├── data/
│   ├── local/
│   │   ├── dao/          # Database Access Objects
│   │   ├── entity/       # Room Entities
│   │   ├── Converters.kt
│   │   ├── DataSeeder.kt
│   │   └── FeesDatabase.kt
│   └── repository/       # Repository Implementations
│
├── di/                   # Hilt Dependency Injection
│
├── domain/
│   ├── model/           # Business Models
│   └── repository/      # Repository Interfaces
│
├── presentation/
│   ├── components/      # Reusable UI Components
│   ├── navigation/      # Navigation Setup
│   ├── screens/         # All App Screens
│   │   ├── dashboard/
│   │   ├── students/
│   │   ├── fee_collection/
│   │   ├── ledger/
│   │   ├── reports/
│   │   └── settings/
│   └── theme/           # Colors, Typography, Theme
│
└── util/                # Utility Functions
```

## Getting Started

### Prerequisites
- Android Studio Hedgehog or newer
- JDK 17
- Android SDK 35

### Build Instructions
1. Clone or download this project
2. Open in Android Studio
3. Sync Gradle files
4. Run on emulator or device (API 26+)

### First Launch
On first launch, the app will:
1. Create the database
2. Seed default classes (NC, LKG, UKG, 1st-12th) with sections A & B
3. Create the current academic session
4. Initialize school settings with default values

## Usage

### Initial Setup
1. Go to **Settings > School Profile** and update your school details
2. Go to **Settings > Academic Sessions** to verify/add sessions
3. Go to **Settings > Fee Structure** to set fee amounts for each class
4. Go to **Settings > Transport Routes** to add transport routes (if applicable)

### Adding Students
1. Go to **Students** tab
2. Tap **+** button
3. Fill in SR Number, A/C Number, Name, Father's Name, Class, Section, Phone
4. Optionally add transport route if applicable
5. Save

### Collecting Fees
1. Go to **Collect** tab or tap **Collect Fee** from dashboard
2. Enter the receipt number from your physical receipt book
3. Select the date
4. Search and select the student
5. Choose fee items to collect
6. Select payment mode (Cash/Cheque/UPI/Online)
7. Review summary and save

### Viewing Ledger
1. Open student details
2. Tap **View Ledger**
3. See all debit (charges) and credit (payments) with running balance

## Color Theme

The app uses a **Saffron/Bhagwa** color theme:
- Primary: #FF6F00 (Deep Saffron)
- Primary Dark: #E65100
- Primary Light: #FFB74D
- Background: #FFF8F0 (Warm Cream)

## Future Enhancements (Phase 2)

- Firebase Authentication (multi-user login)
- Firestore sync (cloud backup and multi-device sync)
- Receipt printing
- PDF export for reports
- Data backup/restore

## Important Notes

- All data is stored locally on the device
- Receipt numbers are entered manually (mirrors physical receipt book)
- The app does not auto-generate receipt numbers
- Changes are logged in the Audit Log for tracking

## Support

For issues or feature requests, contact the development team.

---

**Navodit Public Inter College**
*Approved By the Government*
Myuna Khudaganj, Shahjahanpur


