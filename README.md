# Quản Lý Tòa Nhà (Building Management System)

A comprehensive JavaFX-based building management application designed for managing residential buildings, apartments, and financial operations. This system provides role-based access control for administrators, accountants, and residents.

##  Table of Contents
- [Project Overview](#project-overview)
- [Features](#features)
- [Technology Stack](#technology-stack)
- [System Architecture](#system-architecture)
- [Installation & Setup](#installation--setup)
- [Database Configuration](#database-configuration)
- [Running the Application](#running-the-application)
- [User Roles & Permissions](#user-roles--permissions)
- [Project Structure](#project-structure)
- [Testing](#testing)

##  Project Overview

The Building Management System is a desktop application built with JavaFX that helps manage residential buildings efficiently. The system handles resident information, apartment assignments, fee management, invoice generation, and financial tracking with a robust role-based permission system.

##  Features

### For Administrators

### For Accountants

### For Residents

##  Technology Stack

- **Frontend**: JavaFX 21.0.6 with FXML
- **Backend**: Java 21 with modular architecture
- **Database**: PostgreSQL
- **Security**: Spring Security Core for password hashing
- **Build Tool**: Gradle with Kotlin DSL
- **UI Components**: ControlsFX, Ikonli (for icons)
- **Testing**: 

##  System Architecture

The application follows a layered MVC (Model-View-Controller) architecture:

```
┌─────────────────┐
│   View Layer    │ ← FXML files, Controllers
├─────────────────┤
│  Service Layer  │ ← Business logic, Validation
├─────────────────┤
│   DAO Layer     │ ← Data Access Objects
├─────────────────┤
│  Model Layer    │ ← Entity classes, DTOs
└─────────────────┘
         │
    PostgreSQL Database
```

### Key Components
- **Models**: User, Apartment, Invoice, Transaction, Notification
- **Controllers**: Handle UI interactions and business logic
- **Services**: Implement business rules and validation
- **DAOs**: Manage database operations
- **Utils**: Database connections, password utilities, session management

## Installation & Setup

### Fast Install
- Download the .jar at [Releases](https://github.com/macehex/quan-ly-toa-nha-nhom-05/releases/tag/v1.0.snapshot)

### Prerequisites
- Java 21 or higher

### Quick Start

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd quan-ly-toa-nha
   ```


2. **Configure database connection**
   ```bash
   cp src/main/resources/database.properties.example src/main/resources/database.properties
   ```
   Edit `database.properties` with your database credentials:
   ```properties
   db.url=jdbc:postgresql://localhost:5432/your_database_name
   db.username=your_username
   db.password=your_password
   ```

3. **Build and run the application**
   
   **For Linux/Mac:**
   ```bash
   ./build-app.sh
   ```
   
   **For Windows:**
   ```cmd
   build-app.bat
   ```

- for more details on building and running options, see [How to build](how_to_build.md).
##  Database Configuration

The application uses PostgreSQL with the following main tables:
- `users` - User account information
- `roles` - User role definitions
- `permissions` - System permissions
- `apartments` - Apartment details
- `invoices` - Invoice records
- `transactions` - Financial transactions
- `notifications` - System notifications

Refer to `src/main/resources/reference_database.md` for the complete database schema.



##  User Roles & Permissions

### Administrator (ADMIN)

### Accountant (ACCOUNTANT)

### Resident (RESIDENT)

##  Project Structure

```
src/main/java/com/example/quanlytoanha/
├── controller/          # UI Controllers
├── model/              # Entity classes
├── service/            # Business logic
├── dao/                # Data access layer
├── utils/              # Utility classes
├── session/            # Session management
├── ui/                 # Custom UI components
├── config/             # Configuration classes
├── Main.java           # Application entry point
└── Launcher.java       # Alternative entry point

src/main/resources/
├── com/example/quanlytoanha/view/  # FXML files
├── database.properties.example     # Database config template
└── reference_database.md          # Database schema
```


# Testing
- [Detailed testing guide](src/test/README.md)

