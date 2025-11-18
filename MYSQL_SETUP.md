# MySQL Database Setup Guide

## 📦 What Was Implemented

Your Spring Boot application has been successfully migrated from in-memory storage to MySQL database!

### Changes Made:

1. ✅ **Updated `pom.xml`** - Enabled MySQL driver dependency
2. ✅ **Updated `application.properties`** - Added MySQL configuration
3. ✅ **Updated `User.java`** - Added JPA annotations (@Entity, @Table, @Id, @Column)
4. ✅ **Created `AuthUser.java`** - New entity for authentication
5. ✅ **Created `UserRepository.java`** - JPA repository for User CRUD operations
6. ✅ **Created `AuthUserRepository.java`** - JPA repository for authentication
7. ✅ **Updated `UserService.java`** - Now uses UserRepository instead of ArrayList
8. ✅ **Updated `CustomUserDetailsService.java`** - Now uses AuthUserRepository instead of HashMap

---

## 🚀 Installation Steps

### Step 1: Install MySQL

```bash
# Install MySQL using Homebrew
brew install mysql

# Start MySQL service
brew services start mysql

# Secure your MySQL installation (IMPORTANT!)
mysql_secure_installation
```

**During secure installation:**
- Set root password: **YES** (choose a strong password, e.g., "root123")
- Remove anonymous users: **YES**
- Disallow root login remotely: **YES**
- Remove test database: **YES**
- Reload privilege tables: **YES**

---

### Step 2: Create Database and User

Option A: **Run the SQL script** (recommended):
```bash
mysql -u root -p < setup-mysql.sql
```

Option B: **Manual setup**:
```bash
# Login to MySQL
mysql -u root -p

# Run these commands:
CREATE DATABASE userdb;
CREATE USER 'springuser'@'localhost' IDENTIFIED BY 'springpass';
GRANT ALL PRIVILEGES ON userdb.* TO 'springuser'@'localhost';
FLUSH PRIVILEGES;
EXIT;
```

---

### Step 3: Verify MySQL Connection

```bash
# Login with the new user
mysql -u springuser -p
# Password: springpass

# Select the database
USE userdb;

# Show tables (will be empty for now)
SHOW TABLES;

# Exit
EXIT;
```

---

### Step 4: Update application.properties (if needed)

If you used different credentials, update:
```properties
spring.datasource.username=your_username
spring.datasource.password=your_password
```

---

### Step 5: Run Your Spring Boot Application

```bash
# In your IDE, run SpringbootApplication.java
# OR use Maven:
./mvnw spring-boot:run
```

**On first run, Spring Boot will:**
- Connect to MySQL
- Automatically create tables: `users` and `auth_users`
- You'll see SQL statements in the console (because `spring.jpa.show-sql=true`)

---

## 🧪 Testing

### 1. Register a User

```bash
POST http://localhost:8080/api/auth/register
Content-Type: application/json

{
  "username": "john",
  "password": "pass123",
  "email": "john@example.com"
}
```

### 2. Login to Get JWT Token

```bash
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "username": "john",
  "password": "pass123"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "username": "john"
  }
}
```

### 3. Create a User (with JWT)

```bash
POST http://localhost:8080/api/users
Authorization: Bearer YOUR_JWT_TOKEN
Content-Type: application/json

{
  "name": "John Doe",
  "email": "john@example.com"
}
```

### 4. Get All Users (with JWT)

```bash
GET http://localhost:8080/api/users
Authorization: Bearer YOUR_JWT_TOKEN
```

---

## 🔍 View Database

### Using MySQL CLI:

```bash
mysql -u springuser -p
USE userdb;

-- Show all tables
SHOW TABLES;

-- View users
SELECT * FROM users;

-- View auth users
SELECT * FROM auth_users;
```

---

## 📊 Database Schema

Spring Boot automatically created these tables:

### Table: `users`
```sql
CREATE TABLE users (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  email VARCHAR(255) NOT NULL UNIQUE
);
```

### Table: `auth_users`
```sql
CREATE TABLE auth_users (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(255) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL,
  email VARCHAR(255)
);
```

---

## 🛠️ Useful MySQL Commands

```bash
# Check if MySQL is running
brew services list | grep mysql

# Start MySQL
brew services start mysql

# Stop MySQL
brew services stop mysql

# Restart MySQL
brew services restart mysql

# Connect to MySQL
mysql -u springuser -p

# Inside MySQL:
SHOW DATABASES;
USE userdb;
SHOW TABLES;
DESCRIBE users;
SELECT * FROM users;
```

---

## ⚠️ Troubleshooting

### Error: "Access denied for user"
- Check username/password in `application.properties`
- Verify user exists: `SELECT User, Host FROM mysql.user;`

### Error: "Unknown database 'userdb'"
- Create database: `CREATE DATABASE userdb;`

### Error: "Communications link failure"
- Check if MySQL is running: `brew services list`
- Start MySQL: `brew services start mysql`

### Error: "Table doesn't exist"
- Check `spring.jpa.hibernate.ddl-auto=update` in application.properties
- Spring Boot should auto-create tables on startup

---

## 🎯 Next Steps

1. ✅ MySQL is now integrated
2. ✅ Data persists even after app restart
3. ✅ All endpoints work with database
4. 📝 Consider adding indexes for performance
5. 📝 Consider adding data validation
6. 📝 Consider adding audit fields (createdAt, updatedAt)

---

## 🔐 Security Notes

- Change default passwords in production!
- Use environment variables for sensitive data
- Consider using connection pooling for production
- Regularly backup your database

---

## 📚 References

- Spring Data JPA: https://spring.io/projects/spring-data-jpa
- MySQL Documentation: https://dev.mysql.com/doc/
- Hibernate ORM: https://hibernate.org/

---

Happy Coding! 🚀



