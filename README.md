# 🐾 PawPOS - Pet Shop Inventory Control & Cashier POS System (Android)

A modern, offline-first Android application designed specifically for pet shops, veterinary clinics, pet boutiques, and pet grooming salons. Built with **100% Jetpack Compose**, **Material 3**, **Room SQLite Database**, **Coroutines / Flow**, and **Clean MVVM Architecture**.

---

## 🌟 Key Features

### 1. 🛒 Fast Cashier POS (Point of Sale)
- **Instant Product Search & Barcode Lookup**: Scan or search by name, barcode, or SKU.
- **Pet Category Filtering**: Quick tabs for 🐶 Dogs, 🐱 Cats, 🦜 Birds, 🐠 Aquatics, 🐹 Small Pets, and ✂️ Services.
- **Cart Management**: Real-time quantity steppers, subtotal, tax calculation (configurable, default 5%), and manual discount support.
- **Multi-Method Checkout**:
  - **Cash**: Preset fast denomination buttons (`Exact`, `$20`, `$50`, `$100`) with automatic change calculation.
  - **Card**: Debit / Credit card processing.
  - **QRIS / E-Wallet**: Digital QR payments.
- **Digital Thermal Receipt**:
  - Displays business header, itemized breakdown, tax, payment tender, and change.
  - Shareable and printable to standard 58mm / 80mm Bluetooth thermal ESC/POS printers.
- **Atomic Stock Deduction**: Automatically decreases tangible inventory stocks upon completed sale.

### 2. 📦 Pet Shop Inventory Control
- **Real-Time Stock Health Monitoring**:
  - 🟢 **In Stock**: Healthy supply levels.
  - 🟡 **Low Stock Alert**: Below minimum alert threshold (default 5 units).
  - 🔴 **Out of Stock**: Depleted items highlighted with warning banners.
- **Pet-Specific Attributes**:
  - Expiry date tracking (essential for pet food bags, wet pouches, and medications).
  - Unit tracking (`bag`, `can`, `pouch`, `pack`, `pcs`, `session`).
  - Cost price vs Selling price with live **Profit Margin %** calculation.
- **Quick Stock Adjustments**:
  - One-tap stock in/out dialog with audit reasons: `Restock / Shipment Arrival`, `Customer Sale`, `Damaged / Defect`, `Expired Disposal`, `Inventory Count Audit`.
- **Add & Edit Products**: Full CRUD modal bottom sheet with input validation.

### 3. 📊 Sales Analytics & Reporting
- **Today's Financial KPIs**:
  - Real-time Gross Revenue.
  - Real-time Gross Profit Margin ($).
  - Total Transactions completed.
  - Total Items sold.
- **🏆 Top 5 Best-Selling Products**: Leaderboard ranking most popular items by volume and revenue.
- **⚠️ Low Stock Reorder List**: Direct view of all items needing immediate supplier restock.
- **📜 Transaction History**: Full invoice log with one-tap receipt reprint and inspection.

### 4. 📷 Barcode Scanner
- Viewfinder overlay with animated laser alignment line.
- Manual barcode keypad entry.
- Quick test shortcuts for preloaded pet products.

---

## 🏗️ Architecture & Tech Stack

```
PetShopPOS/
├── app/
│   ├── src/main/
│   │   ├── java/com/petshop/pos/
│   │   │   ├── PetShopApp.kt                     # Application class & DI setup
│   │   │   ├── MainActivity.kt                   # Single Activity host
│   │   │   ├── data/
│   │   │   │   ├── local/
│   │   │   │   │   ├── PetShopDatabase.kt        # Room Database & Sample Preload
│   │   │   │   │   ├── dao/
│   │   │   │   │   │   ├── ProductDao.kt         # Product CRUD & stock queries
│   │   │   │   │   │   ├── TransactionDao.kt     # Sales & top seller analytics
│   │   │   │   │   │   └── StockMovementDao.kt   # Stock audit history
│   │   │   │   │   └── entity/
│   │   │   │   │       ├── ProductEntity.kt      # Products with pet attributes
│   │   │   │   │       ├── TransactionEntity.kt  # Completed sales orders
│   │   │   │   │       ├── TransactionItemEntity.kt # Line items
│   │   │   │   │       ├── StockMovementEntity.kt# Inventory log
│   │   │   │   │       └── PetCategory.kt        # Enums (Dogs, Cats, Birds, etc.)
│   │   │   │   └── repository/
│   │   │   │       └── PetShopRepository.kt      # Atomic checkout & data orchestration
│   │   │   └── ui/
│   │   │       ├── theme/                        # Material 3 Warm Teal & Amber Palette
│   │   │       ├── components/                   # ReceiptView, Badges, Pills, KPI cards
│   │   │       ├── viewmodel/                    # PosViewModel, InventoryViewModel, AnalyticsViewModel
│   │   │       └── screens/                      # PosScreen, InventoryScreen, AnalyticsScreen, ScannerScreen
│   │   └── res/                                  # Drawables, themes, strings, XML rules
│   └── build.gradle.kts                          # Module dependencies
├── build.gradle.kts                              # Root build config
└── settings.gradle.kts                           # Module include
```

- **Language**: Kotlin 1.9.24
- **UI Toolkit**: Jetpack Compose (BOM 2024.05.00) + Material 3
- **Local Persistence**: Room SQLite 2.6.1 with Coroutines & reactive Flow
- **Architecture**: MVVM (Model-View-ViewModel) + Repository Pattern
- **Target SDK**: Android 14 (API 34)
- **Minimum SDK**: Android 8.0 (API 26)

---

## 🚀 How to Run in Android Studio

1. Open **Android Studio** (Giraffe, Hedgehog, Iguana, Jellyfish, Koala or newer).
2. Select **File -> Open...** and choose the `PetShopPOS` folder.
3. Allow Gradle to sync the project dependencies automatically.
4. Select an Android Emulator (Pixel 7 / 8 recommended) or connect a physical Android device with USB Debugging enabled.
5. Click **Run ▶** (or press `Shift + F10`).
6. The app will launch with a preloaded catalog of pet food, toys, health supplies, and grooming services ready for testing!

---

## 🖨️ Thermal Printer & Barcode Hardware Integration
The database and architecture are designed to connect to hardware POS accessories:
- **Bluetooth ESC/POS Thermal Printers**: Use the data generated by `ThermalReceiptView` or send raw bytes to ESC/POS thermal printers (58mm/80mm).
- **USB / Bluetooth Barcode Scanners**: Hardware scanners that operate as HID keyboards automatically trigger search and cart additions through the top search input.
