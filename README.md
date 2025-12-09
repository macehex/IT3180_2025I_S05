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
  - [Classes for Product Management](##classes-for-product-management)
- [Testing](#testing)

##  Project Overview

The Building Management System is a desktop application built with JavaFX that helps manage residential buildings efficiently. The system handles resident information, apartment assignments, fee management, invoice generation, and financial tracking with a robust role-based permission system.

##  Features

### For Administrators
- **User Management**: Create and manage user accounts for residents, accountants, and other administrators
- **Apartment Management**: Add, edit, and assign apartments to residents
- **Announcement System**: Create and send announcements to residents
- **Request Management**: Review and process service requests from residents
- **Profile Change Requests**: Approve or reject resident profile modification requests
- **System Configuration**: Configure system settings, permissions, and access controls
- **Asset Management**: Track and manage building assets and equipment
- **Population Reports**: Generate demographic and occupancy reports

### For Accountants
- **Invoice Management**: Generate monthly invoices for residents including utility fees
- **Financial Tracking**: Monitor payment status and outstanding debts
- **Fee Management**: Set up and manage different types of fees (electricity, water, management fees, service fees)
- **Transaction History**: View detailed payment transactions and financial records
- **Debt Reports**: Generate reports on outstanding payments and debt collection
- **Asset Financial Reports**: Track financial aspects of building asset management

### For Residents
- **Dashboard Overview**: View account summary including current debt, monthly payments, and unread notifications
- **Service Consumption Charts**: Monitor utility usage with pie charts (current month) and trend analysis (6-month history)
- **Invoice & Transaction History**: Access detailed billing information and payment history
- **Notification System**: Receive and read announcements from building management
- **Service Requests**: Submit maintenance requests, security issues, service complaints, and other concerns with priority levels
- **Request Tracking**: Monitor status of submitted requests (New, In Progress, Completed, Cancelled)
- **Profile Management**: Request changes to personal information (phone, email, etc.)
- **Login History**: Monitor account access for security purposes
- **Recent Activities**: View last 7 days of account activities

##  Technology Stack

- **Frontend**: JavaFX 21.0.6 with FXML
- **Backend**: Java 21 with modular architecture
- **Database**: PostgreSQL
- **Security**: Spring Security Core for password hashing
- **Build Tool**: Gradle with Kotlin DSL
- **UI Components**: ControlsFX, Ikonli (for icons)
- **Testing**: JUnit 5, TestFX for UI testing

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
- Download the .jar at [Releases](https://github.com/macehex/IT3180_2025I_S05/releases)

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
- Full system access and management capabilities
- User account creation and management for all roles
- Apartment assignment and configuration
- System-wide announcement broadcasting
- Service request review and resolution
- Profile change request approval/rejection
- Asset and equipment management
- Report generation and system analytics
- Security and access control configuration

### Accountant (ACCOUNTANT)
- Financial management and invoice generation
- Fee structure configuration and management
- Payment tracking and debt monitoring
- Financial transaction oversight
- Monthly billing cycle management
- Debt collection reporting
- Asset financial tracking
- Financial report generation and export

### Resident (RESIDENT)
- **Personal Dashboard Access**: 
  - View current debt balance and monthly payment summary
  - Monitor unread notification count
  - Access service consumption analytics with visual charts
- **Financial Information**: 
  - View detailed invoice history with payment status
  - Access complete transaction records
  - Track monthly and periodic billing cycles
- **Communication**: 
  - Receive and read building management announcements
  - Mark notifications as read/unread
- **Service Requests**: 
  - Submit various types of requests (maintenance, security, complaints, other)
  - Set priority levels (Low, Medium, High)
  - Track request status progression
- **Account Management**: 
  - Request personal information changes (phone, email)
  - Monitor login history for security
  - View recent account activities (7-day history)
- **Security**: 
  - Secure logout functionality
  - Access to login audit trail

## User Guide & Workflows

### Getting Started & Installation
1. **Download the Application**: Download the .jar file from [Releases](https://github.com/macehex/quan-ly-toa-nha-nhom-05/releases/tag/v1.0.snapshot)
2. **System Requirements**: Java 21 or higher required. Download from official Java website if needed.
3. **Application Launch**: Double-click the .jar file or desktop application icon
4. **Login**: Enter your assigned username and password credentials

### Default Login Credentials (For Demo/Testing)
- **Administrator**: admin / admin123
- **Accountant**: ketoan / ketoan123
- **Police**: congan / congan123
- **Resident**: resident / resident

## Detailed User Workflows by Role

### For Residents (Cư dân)

#### 1. Dashboard Overview
The main dashboard provides comprehensive information at a glance:
- **Purple Tile**: Current debt balance (if any) - displays outstanding payments
- **Pink Tile**: Monthly payment amount - current month's fees
- **Blue Tile**: Unread notification count - important announcements

#### 2. Service Consumption Analytics
Two main views for monitoring utility usage:
- **"This Month" View**: Interactive pie chart showing current month's service cost breakdown by category
- **"6-Month Trend" View**: Line chart displaying consumption trends and patterns over the last 6 months
- Switch between views using the toggle buttons at the top

#### 3. Invoice & Transaction Management
Complete financial tracking capabilities:
1. Navigate to "Tra Cứu Hóa Đơn, Lịch Sử" in the left menu
2. **Dashboard Summary**:
   - Total outstanding debt
   - Total unpaid invoices
   - Total payments made today
   - Online payment options
3. **Invoice Details**:
   - Payment status (Paid/Unpaid)
   - Detailed fee breakdown (electricity, water, management fees, services)
   - Filter invoices by date using the calendar icon and "Filter" button
4. **Payment Processing**:
   - Select invoices to pay using "Chọn hóa đơn"
   - Review payment amount
   - Click "Thanh Toán Ngay" to process payment

#### 4. Notification System
Stay informed with building announcements:
1. Click "Xem Thông Báo" in the left menu
2. **Notification Status**:
   - **Unread notifications**: Bold text with light blue background
   - **Read notifications**: Normal text with gray color
3. **Interaction**:
   - **Single click**: Mark notification as read
   - **Double click**: Open detailed notification content with full text
   - **Hover**: View summary tooltip
4. Notifications include timestamps and importance levels

#### 5. Service Request Submission
Submit maintenance and service requests efficiently:
1. Click "Tạo Phản Ánh" in the left menu
2. Fill out the comprehensive request form:
   - **Title (*)**: Brief problem summary (required)
   - **Request Type**: Select from dropdown:
     - Bảo trì thiết bị (Equipment maintenance)
     - Sửa chữa cơ sở hạ tầng (Infrastructure repairs)  
     - Vấn đề an ninh (Security issues)
     - Khiếu nại về dịch vụ (Service complaints)
     - Yêu cầu khác (Other requests)
   - **Detailed Description (*)**: Complete problem description (required)
   - **Priority Level**: Low/Medium/High
   - **Area**: Location where issue occurs (hallway, elevator, lobby, etc.)
3. Click "Gửi Yêu cầu" to submit

#### 6. Request Tracking
Monitor the progress of submitted requests:
1. Navigate to "Yêu Cầu Của Tôi" in left menu
2. **Request Status Tracking**:
   - **Mới (New)**: Recently created, not yet processed
   - **Đang xử lý (In Progress)**: Being handled by management
   - **Hoàn thành (Completed)**: Successfully resolved
   - **Đã hủy (Cancelled)**: Request was cancelled
3. Click individual requests for detailed responses and feedback from management
4. View submission date, request type, and admin notes

#### 7. Profile Change Requests
Request updates to personal information:
1. Click "Thay Đổi Thông Tin" in left menu
2. **Interface Layout**:
   - Light blue guidance box with instructions
   - Left white panel: Current account information (reference)
   - Right white panel: New information input fields
   - Yellow notice box: Important notes and warnings
3. **Editable Information**:
   - Username
   - Phone number
   - Email address
   - Full name
   - Relationship to apartment owner
   - Date of birth
   - ID card number (CMND/CCCD)
4. Enter current value and new desired value with justification
5. Submit for management approval and track in "My Requests"

#### 8. Login History & Security Management
Monitor account security and manage password:
1. Access "Quản Lý Đăng Nhập" in left menu
2. **Login History Review**:
   - Login timestamps
   - IP addresses
   - Device information  
   - Session status (Active/Ended)
3. **Password Management**:
   - Change password using "Đổi mật khẩu" section
   - Enter new password and click "Đổi mật khẩu" button
4. **Security Monitoring**:
   - Report suspicious activity to management immediately
   - Contact building management for unusual login patterns

### For Administrators (Ban quản trị)

#### 1. User Management
Complete user account lifecycle management:
- **Add New Accounts**: Create accounts for residents, accountants, and other administrators
- **Role Assignment**: Assign appropriate permissions (Resident, Accountant, Admin, Police)
- **Account Modification**: Edit user information and permissions
- **Password Reset**: Reset passwords for users who have forgotten credentials
- **Account Deactivation**: Remove or disable user accounts when needed

#### 2. Resident Management
Comprehensive resident information management:
- **Add New Residents**: Complete profile creation with personal details, apartment assignment
- **Profile Updates**: Modify resident information directly
- **Change Request Approval**: Review and approve resident-submitted profile change requests
- **Relationship Tracking**: Manage relationships to apartment owners (owner, spouse, child, tenant, etc.)
- **History Tracking**: View complete audit trail of all changes made to resident profiles

#### 3. Apartment Management
Full apartment lifecycle and assignment management:
- **Add New Apartments**: Register new units with area, ownership details
- **Assignment Management**: Assign residents to apartments
- **Vacancy Tracking**: Monitor empty units
- **Ownership Updates**: Modify apartment owner information

#### 4. Announcement System
Comprehensive communication management:
- **Create Announcements**: Draft and send building-wide communications
- **Target Audience**: Send to all residents or specific apartment groups
- **Urgency Levels**: Mark announcements as urgent for priority attention
- **Delivery History**: Track sent announcements and delivery status

#### 5. Service Request Management
Handle resident requests and track resolution:
- **Request Queue**: View all incoming service requests with categorization
- **Status Updates**: Change request status (Pending → In Progress → Completed)
- **Priority Management**: Handle urgent requests first
- **Response Tracking**: Provide feedback and updates to residents
- **Right-click Context Menu**: Quick status updates via context menu

#### 6. Asset Management
Track and manage building assets and equipment:
- **Asset Registration**: Add new equipment with purchase details, location, cost
- **Status Tracking**: Monitor equipment status (Available, In Use, Under Maintenance, Broken)
- **Location Management**: Track where each asset is located within the building
- **Cost Tracking**: Record initial purchase costs and maintenance expenses

#### 7. Maintenance Management
Plan and track building maintenance activities:
- **Maintenance Scheduling**: Create maintenance plans for equipment
- **Work Order Management**: Track scheduled vs. completed maintenance
- **Cost Recording**: Log maintenance costs and personnel
- **Status Updates**: Mark maintenance tasks as pending or completed
- **Historical Records**: Maintain complete maintenance history

#### 8. Security & Access Control
Manage building security and visitor access:
- **Vehicle Tracking**: Log vehicle entry/exit with license plates and times
- **Visitor Management**: Register visitor check-ins/check-outs with ID verification
- **Access History**: Review complete logs of building access
- **Security Reports**: Generate reports for security analysis

#### 9. Reporting & Analytics
Generate comprehensive reports for management:
- **Population Reports**: Track resident move-ins and move-outs over time periods
- **Debt Reports**: Detailed financial reports showing outstanding payments by apartment
- **Asset Reports**: Financial overview of building assets and maintenance costs
- **Export Capabilities**: Generate PDF reports for record-keeping and external sharing

### For Accountants (Kế toán)

#### 1. Fee Structure Management
Complete fee and pricing configuration:
- **Fee Type Creation**: Define new fee categories (management fees, parking, utilities)
- **Pricing Models**: Set fixed rates or per-square-meter calculations
- **Default Fee Setup**: Configure automatically applied fees vs. optional services
- **Price Updates**: Modify fee rates with automatic future application
- **Fee Lifecycle**: Deactivate obsolete fees while preserving historical data

#### 2. Automated Invoicing & Notifications
Streamline billing processes:
- **Bulk Invoice Generation**: Create monthly invoices for all apartments
- **Automatic Notifications**: Send invoice notifications to residents automatically
- **Due Date Reminders**: Configure automatic reminders before due dates
- **Overdue Alerts**: Send urgent notifications for overdue payments
- **Manual Notification Options**: Send custom reminders to specific residents

#### 3. Financial Tracking & Reporting
Comprehensive financial oversight:
- **Debt Monitoring**: Track outstanding balances by apartment
- **Payment Processing**: Record and verify resident payments
- **Financial Analytics**: Monitor building financial health with key metrics
- **Collection Reports**: Generate reports for debt collection efforts
- **Transaction History**: Maintain complete audit trail of all financial activities

#### 4. Invoice Management
Detailed invoice lifecycle management:
- **Invoice Generation**: Create detailed invoices with fee breakdowns
- **Payment Tracking**: Monitor payment status and history
- **Late Fee Assessment**: Apply penalties for overdue payments
- **Payment Plans**: Manage installment arrangements for residents
- **Dispute Resolution**: Handle billing questions and adjustments

### For Police/Security (Công an)

#### 1. Read-Only Access
Security personnel have view-only permissions for safety and security oversight:
- **No Data Modification**: Cannot add, edit, or delete any system data
- **Report Generation**: Full access to security and population reports
- **Export Capabilities**: Generate PDF reports for official records
- **Audit Trail**: All viewing activities are logged for security

#### 2. Security Reports
Comprehensive security monitoring capabilities:
- **Vehicle Access Logs**: Track all vehicle entry/exit with timestamps and license plates
- **Visitor Logs**: Monitor guest access with identification and visit purposes
- **Access Pattern Analysis**: Identify unusual access patterns or security concerns
- **Time-Based Filtering**: Generate reports for specific date ranges

#### 3. Population Monitoring
Track resident population changes for civic purposes:
- **Move-in/Move-out Reports**: Monitor population changes over time
- **Demographic Tracking**: Understand building occupancy patterns
- **Compliance Monitoring**: Ensure building occupancy meets regulations
- **Historical Analysis**: Track population trends for planning purposes

#### 4. Report Export
Generate official documentation:
- **PDF Generation**: Create official reports for government filing
- **Data Verification**: Ensure report accuracy before export
- **Secure Access**: Maintain confidentiality of sensitive information
- **Audit Compliance**: Meet requirements for official documentation

## Security Best Practices

### For All Users
- **Never share login credentials** with others
- **Always logout** after use, especially on shared computers
- **Change passwords regularly** using secure, unique passwords
- **Monitor login history** frequently for unauthorized access
- **Report suspicious activity** to building management immediately

### For Residents
- **Check notifications regularly** for important updates
- **Provide detailed information** in service requests for faster resolution
- **Pay invoices on time** to avoid late fees
- **Verify payment information** before submitting online payments
- **Keep personal information updated** through proper request channels

### For Administrators
- **Review audit trails** regularly for unauthorized changes
- **Backup data regularly** to prevent data loss
- **Maintain user permissions** according to role requirements
- **Monitor system activity** for unusual patterns
- **Follow proper procedures** for data modification and user management

## Support & Troubleshooting

### Common Issues & Solutions
- **Forgotten password**: Contact building management directly for reset using the admin password reset function
- **Data display errors**: Try logging out and logging back in; if persistent, contact technical support
- **Payment processing issues**: Verify invoice selection and amount before submitting payment
- **Notification problems**: Check notification settings and contact management if issues persist

### Technical Support
- **Application issues**: Create a service request with type "Other" or contact building management
- **System errors**: Document error messages and contact technical support
- **Feature requests**: Submit suggestions through the service request system

### Emergency Contacts
- **Building Management**: BlueMoon Apartment Complex
- **Technical Support**: Through building management
- **Security Issues**: Contact building security or local authorities as appropriate

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
### Classes for Product Management
```
src/main/java/com/example/quanlytoanha/
├── model/              # Look for Product-related entity classes
│   ├── Product.java
│   └── ProductCategory.java
├── service/            # Product business logic
│   ├── ProductService.java
│   └── ProductServiceImpl.java
├── dao/                # Product data access
│   ├── ProductDAO.java
│   └── ProductDAOImpl.java
└── controller/         # Product UI controllers
    └── ProductController.java
```


# Testing
- [Detailed testing guide](src/test/README.md)

