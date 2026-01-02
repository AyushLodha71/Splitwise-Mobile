# Splitwise Mobile

An Android expense-sharing application that helps users split bills and track shared expenses within groups. Built with Java and Android SDK, this app provides a simple and intuitive interface for managing group finances.

## 📱 Features

### User Management
- **User Registration & Login**: Secure authentication with SHA-256 password hashing
- **User Profile Management**: Personalized user experience with username tracking

### Group Management
- **Create Groups**: Start new expense-sharing groups with unique group codes
- **Join Groups**: Use group codes to join existing groups
- **Enter Groups**: Access groups you're already a member of

### Expense Tracking
- **Add Expenses**: Record new transactions with amount, reason, and payee
- **Multiple Split Methods**:
  - Split equally among all members
  - Split equally among selected members
  - Split unequally with custom amounts
  - Split by percentages
- **Transaction History**: View complete history of all group expenses
- **Amount Spent**: Track individual spending within groups

### Balance Management
- **Real-time Balances**: View who owes whom and how much
- **Settle Up**: Mark debts as settled between members
- **Balance Overview**: See consolidated view of all balances

### Transaction Management
- **Delete Expenses**: Remove incorrect or unwanted transactions
- **Transaction Details**: View detailed information for each expense

## 🛠️ Tech Stack

- **Language**: Java 17
- **Platform**: Android (API 24+)
- **Build System**: Gradle with Kotlin DSL
- **Architecture**: Activity-Fragment pattern with ViewBinding
- **Networking**: Retrofit 2.11.0 with Gson converter
- **Backend API**: RESTful API hosted on Render
- **Security**: SHA-256 password hashing

## 📋 Dependencies

```gradle
- androidx.appcompat:appcompat:1.6.1
- com.google.android.material:material:1.10.0
- androidx.constraintlayout:constraintlayout:2.1.4
- androidx.navigation:navigation-fragment:2.6.0
- androidx.navigation:navigation-ui:2.6.0
- com.squareup.retrofit2:retrofit:2.11.0
- com.squareup.retrofit2:converter-gson:2.11.0
```

## 🏗️ Project Structure

```
app/
├── src/
│   ├── main/
│   │   ├── java/com/example/splitwise/
│   │   │   ├── Activities
│   │   │   │   ├── MainActivity.java           # Entry point
│   │   │   │   ├── WelcomeActivity.java        # Welcome screen
│   │   │   │   ├── LoginActivity.java          # User login
│   │   │   │   ├── RegisterActivity.java       # User registration
│   │   │   │   ├── GroupActivity.java          # Group management hub
│   │   │   │   ├── CreateGroupActivity.java    # Create new groups
│   │   │   │   ├── JoinGroupActivity.java      # Join existing groups
│   │   │   │   ├── EnterGroupActivity.java     # Enter groups
│   │   │   │   ├── MainPageActivity.java       # Group dashboard
│   │   │   │   └── AddTransactionActivity.java # Add expenses
│   │   │   ├── Fragments
│   │   │   │   ├── HistoryFragment.java        # Transaction history
│   │   │   │   ├── BalancesFragment.java       # Balance overview
│   │   │   │   ├── SettleFragment.java         # Settle debts
│   │   │   │   ├── DeleteFragment.java         # Delete transactions
│   │   │   │   ├── AmountSpentFragment.java    # Spending summary
│   │   │   │   ├── SplitBySomeFragment.java    # Split by some members
│   │   │   │   ├── SplitUnequalFragment.java   # Unequal split
│   │   │   │   └── SplitPercentageFragment.java# Percentage split
│   │   │   ├── Utils
│   │   │   │   ├── ApiCaller.java              # HTTP request handler
│   │   │   │   ├── HashUtils.java              # Password hashing
│   │   │   │   └── Exists.java                 # Data existence checker
│   │   │   └── Models
│   │   │       └── TransactionModel.java       # Transaction data model
│   │   ├── res/
│   │   │   ├── layout/                         # XML layouts
│   │   │   ├── values/                         # Strings, colors, themes
│   │   │   └── mipmap/                         # App icons
│   │   └── AndroidManifest.xml
│   └── test/                                    # Unit tests
└── build.gradle.kts
```

## 🚀 Getting Started

### Quick Install (For Users)

**Download and install the APK directly:**
1. Download [`Splitwise Mobile.apk`](./Splitwise%20Mobile.apk) (6.1 MB)
2. Enable "Install from Unknown Sources" in your Android device settings
3. Open the downloaded APK file and install
4. Launch the app and start tracking expenses!

> **Note:** This is a debug build. For production use, create a signed release build.

### Developer Setup

### Prerequisites
- Android Studio Arctic Fox or later
- JDK 17
- Android SDK (API 24 or higher)
- Gradle 8.5.1+

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/AyushLodha71/Splitwise-Mobile.git
   cd splitwise-mobile
   ```

2. **Open in Android Studio**
   - Launch Android Studio
   - Select "Open an Existing Project"
   - Navigate to the cloned directory

3. **Sync Gradle**
   - Android Studio will automatically prompt to sync Gradle
   - Wait for dependencies to download

4. **Configure Backend URL**
   - Update the `BASE_URL` constant in your Java activity files to point to your backend server
   - The URL is currently hardcoded in multiple activity files and should be centralized

5. **Run the App**
   - Connect an Android device or start an emulator
   - Click the "Run" button in Android Studio

## 📱 How to Use

1. **Registration**
   - Open the app and tap "Register"
   - Enter a unique username and password
   - Your password is securely hashed before storage

2. **Create or Join a Group**
   - After login, choose to create a new group or join an existing one
   - Creating a group generates a unique group code
   - Share the code with friends to let them join

3. **Add Expenses**
   - Tap the floating action button (+) on the group dashboard
   - Enter the amount, reason, and select who paid
   - Choose how to split the expense
   - Submit to record the transaction

4. **View Balances**
   - Navigate to the "Balances" tab to see who owes whom
   - Amounts are automatically calculated based on transactions

5. **Settle Up**
   - Go to the "Settle" tab
   - Select the person you want to settle with
   - Confirm the settlement to mark debts as paid

## 🔐 Security

- **Password Encryption**: All passwords are hashed using SHA-256 before transmission
- **Secure Communication**: API calls are made over HTTPS
- **No Plain Text Storage**: Passwords are never stored in plain text

## 🌐 API Integration

The app communicates with a RESTful backend API for:
- User authentication and registration
- Group management (create, join, fetch)
- Transaction operations (add, delete, update)
- Balance calculations and settlements

### Backend Configuration
The backend URL is configured in individual activity files. For production use, it's recommended to:
- Centralize the URL in a Constants or Configuration file
- Use environment variables or BuildConfig for different environments
- Implement proper API authentication and security measures

## 📊 App Configuration

- **Package Name**: `com.example.splitwise`
- **Min SDK**: 24 (Android 7.0 Nougat)
- **Target SDK**: 34 (Android 14)
- **Compile SDK**: 34
- **Version**: 1.0

## 🎨 UI Components

- **Material Design**: Modern UI with Material Components
- **ViewBinding**: Type-safe view access
- **Bottom Navigation**: Easy navigation between main features
- **RecyclerView**: Efficient list displays for transactions and balances
- **Fragments**: Modular UI components
- **Custom Dialogs**: Interactive settle-up dialogs

## 🧪 Testing

The project includes test configurations for:
- Unit Tests (JUnit)
- Instrumented Tests (AndroidX Test)
- UI Tests (Espresso)

Run tests with:
```bash
./gradlew test           # Unit tests
./gradlew connectedTest  # Instrumented tests
```

## 🔧 Build Configuration

### Debug Build (Included)
A pre-built debug APK is included in the repository:
- **File:** `Splitwise Mobile.apk`
- **Size:** 6.1 MB
- **Type:** Debug build
- **Min Android:** 7.0 (API 24)

### Release Build
To create a signed release build:
```bash
./gradlew assembleRelease
```

The APK will be available at:
```
app/build/outputs/apk/release/app-release.apk
```

### ProGuard
ProGuard rules are defined in `app/proguard-rules.pro` for code optimization and obfuscation in release builds.

## 📄 License

This project is open source and available for educational purposes.

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## 📧 Support

For issues, questions, or suggestions, please open an issue on GitHub.

## 🔮 Future Enhancements

- [ ] Add expense categories
- [ ] Implement push notifications
- [ ] Add support for multiple currencies
- [ ] Enable image attachments for receipts
- [ ] Implement data export functionality
- [ ] Add dark mode support
- [ ] Include expense analytics and charts
- [ ] Support for recurring expenses

## 👨‍💻 Developer Notes

### Key Classes
- **ApiCaller**: Handles all HTTP communications with the backend
- **HashUtils**: Provides secure password hashing
- **TransactionModel**: Data structure for expense transactions
- **Exists**: Utility for checking data existence in backend

### Navigation Flow
```
MainActivity → WelcomeActivity → LoginActivity/RegisterActivity
    → GroupActivity → MainPageActivity (Group Dashboard)
        → Add/View/Manage Expenses
```

---

**Built with ❤️ for better expense management**
