# 🔐 Credentials Table Refactoring - Complete Guide

## ✅ What Was Implemented

Successfully refactored authentication credentials into a **separate `user_credentials` table** for better security separation and architectural flexibility.

---

## 📊 Architecture Change

### Before (Single Table):
```sql
users table:
├── id
├── username
├── password (hashed)  ← Mixed with profile data
├── email
├── name
├── created_at
└── updated_at
```

### After (Separate Tables):
```sql
users table:                    user_credentials table:
├── id                          ├── id
├── username                    ├── user_id (FK → users.id)
├── email                       ├── password_hash
├── name                        ├── algorithm (BCrypt)
├── created_at                  ├── last_changed
└── updated_at                  ├── must_change_password
                                ├── failed_login_attempts
                                ├── locked_until
                                ├── created_at
                                └── updated_at
```

**Relationship:** One-to-One (One user has one credential record)

---

## 🎯 Files Created/Modified

### New Files Created:

1. **`UserCredential.java`** ✨
   - New entity for credential storage
   - Tracks password hash, algorithm, failed attempts
   - Account lockout functionality built-in
   - Password change tracking

2. **`UserCredentialRepository.java`** ✨
   - JPA repository for credential operations
   - Methods: `findByUserId()`, `existsByUserId()`, `deleteByUserId()`

3. **`UserCredentialService.java`** ✨
   - Service layer for credential management
   - Password creation, verification, updates
   - Failed login tracking
   - Account lockout logic (5 attempts = 30 min lock)

### Modified Files:

4. **`User.java`** ✏️
   - **Removed:** `password` field
   - **Added:** `@OneToOne` relationship with `UserCredential`
   - Now only stores profile data

5. **`CustomUserDetailsService.java`** ✏️
   - Updated `loadUserByUsername()` to fetch from both tables
   - Updated `registerUser()` to create user + credential atomically
   - Added account lockout check during login

6. **`AuthController.java`** ✏️
   - Minor update to error message handling
   - Registration logic remains the same (service handles split)

---

## 🗄️ Database Schema

### `users` Table:
```sql
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    created_at DATETIME(6),
    updated_at DATETIME(6)
);
```

### `user_credentials` Table:
```sql
CREATE TABLE user_credentials (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    password_hash VARCHAR(60) NOT NULL,
    algorithm VARCHAR(20) NOT NULL DEFAULT 'BCrypt',
    last_changed DATETIME(6),
    must_change_password BOOLEAN DEFAULT FALSE,
    failed_login_attempts INT DEFAULT 0,
    locked_until DATETIME(6),
    created_at DATETIME(6),
    updated_at DATETIME(6),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
```

**Foreign Key:** `user_credentials.user_id` → `users.id` (CASCADE DELETE)

---

## 🚀 How to Test

### Step 1: Start Spring Boot Application

**Important:** Spring Boot will automatically create both tables when you start the app.

```bash
# In your IDE: Run SpringbootApplication.java
# OR
./mvnw spring-boot:run
```

**Watch for these logs:**
```
Hibernate: create table users (...)
Hibernate: create table user_credentials (...)
```

### Step 2: Verify Tables Created

```bash
mysql -u springuser -pspringpass -D userdb -e "SHOW TABLES;"
```

**Expected output:**
```
Tables_in_userdb
user_credentials
users
```

### Step 3: Check Table Structures

```bash
# Users table
mysql -u springuser -pspringpass -D userdb -e "DESCRIBE users;"

# Credentials table
mysql -u springuser -pspringpass -D userdb -e "DESCRIBE user_credentials;"
```

### Step 4: Register a User

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john",
    "password": "pass123",
    "email": "john@example.com",
    "name": "John Doe"
  }'
```

**Response:**
```json
{
  "success": true,
  "message": "User registered successfully",
  "statusCode": 201
}
```

### Step 5: Verify Data in Both Tables

```bash
# Check users table
mysql -u springuser -pspringpass -D userdb \
  -e "SELECT id, username, email, name FROM users;"

# Check credentials table
mysql -u springuser -pspringpass -D userdb \
  -e "SELECT id, user_id, LEFT(password_hash, 20) as hash, algorithm, failed_login_attempts, locked_until FROM user_credentials;"
```

**Expected:**
```
users table:
id | username | email             | name
1  | john     | john@example.com  | John Doe

user_credentials table:
id | user_id | hash                   | algorithm | failed_login_attempts | locked_until
1  | 1       | $2a$10$N9qo8uLOickgx | BCrypt    | 0                     | NULL
```

### Step 6: Test Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john",
    "password": "pass123"
  }'
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

### Step 7: Test Failed Login Tracking

```bash
# Try wrong password 3 times
for i in {1..3}; do
  curl -X POST http://localhost:8080/api/auth/login \
    -H "Content-Type: application/json" \
    -d '{"username":"john","password":"wrongpass"}'
  echo ""
done

# Check failed attempts in database
mysql -u springuser -pspringpass -D userdb \
  -e "SELECT user_id, failed_login_attempts, locked_until FROM user_credentials WHERE user_id=1;"
```

**After 5 failed attempts:**
```
user_id | failed_login_attempts | locked_until
1       | 5                     | 2025-11-17 11:30:00
```

Account is locked for 30 minutes! 🔒

---

## 🔐 New Security Features

### 1. **Account Lockout**
- After 5 failed login attempts
- Account locked for 30 minutes
- Automatic unlock after lockout period expires
- Failed attempts reset on successful login

### 2. **Password Change Tracking**
- `last_changed` timestamp updated automatically
- Can implement password expiry policies later
- Track when passwords were last updated

### 3. **Algorithm Flexibility**
- Current: BCrypt
- Can migrate to Argon2, scrypt, etc. later
- Algorithm tracked per credential

### 4. **Force Password Change**
- `must_change_password` flag
- Can force users to change password on next login
- Useful for admin-reset passwords

---

## 📊 Benefits of This Architecture

### Security Benefits:
✅ **Clear Separation** - Profile data separate from authentication data  
✅ **Lockout Protection** - Prevents brute force attacks  
✅ **Audit Trail** - Track password changes, failed attempts  
✅ **Algorithm Flexibility** - Can upgrade hashing algorithms  
✅ **Granular Access Control** - Can restrict who accesses credential table  

### Architectural Benefits:
✅ **Extensible** - Easy to add OAuth, SAML, biometrics later  
✅ **Maintainable** - Clear single responsibility  
✅ **Testable** - Can test credential logic independently  
✅ **Scalable** - Can add password history, MFA secrets, etc.  

---

## 🔄 Data Flow

### Registration Flow:
```
1. POST /api/auth/register
   ↓
2. AuthController.register()
   ↓
3. CustomUserDetailsService.registerUser()
   ↓
4. [Transaction Start]
   ├─ Save User to 'users' table (get user.id)
   ├─ Save Credential to 'user_credentials' table (with user.id)
   └─ [Transaction Commit]
   ↓
5. Return success response
```

### Login Flow:
```
1. POST /api/auth/login
   ↓
2. AuthController.login()
   ↓
3. AuthenticationManager.authenticate()
   ↓
4. CustomUserDetailsService.loadUserByUsername()
   ├─ Query 'users' table for username
   ├─ Query 'user_credentials' table by user_id
   ├─ Check if account is locked
   └─ Return UserDetails with password hash
   ↓
5. Generate JWT token
   ↓
6. Return token + username
```

### Failed Login Flow:
```
1. Wrong password entered
   ↓
2. Authentication fails
   ↓
3. UserCredentialService.verifyPassword()
   ↓
4. credential.incrementFailedAttempts()
   ├─ failed_login_attempts++
   └─ If >= 5: Set locked_until = now + 30 minutes
   ↓
5. Save updated credential
   ↓
6. Return 401 Unauthorized
```

---

## 🎨 API Responses

### Get User (Profile Only - No Password!)

**Request:**
```bash
GET /api/users/1
Authorization: Bearer YOUR_TOKEN
```

**Response:**
```json
{
  "success": true,
  "message": "User found",
  "data": {
    "id": 1,
    "username": "john",
    "email": "john@example.com",
    "name": "John Doe",
    "createdAt": "2025-11-17T10:30:45",
    "updatedAt": "2025-11-17T10:30:45"
  }
}
```

✅ **No password field!** Credential data completely separate.

---

## 🔍 Querying the Database

### Join User with Credentials:
```sql
SELECT 
    u.id,
    u.username,
    u.email,
    u.name,
    c.algorithm,
    c.last_changed,
    c.failed_login_attempts,
    c.locked_until
FROM users u
INNER JOIN user_credentials c ON u.id = c.user_id;
```

### Find Locked Accounts:
```sql
SELECT u.username, c.locked_until
FROM users u
INNER JOIN user_credentials c ON u.id = c.user_id
WHERE c.locked_until > NOW();
```

### Find Users Who Need Password Change:
```sql
SELECT u.username, c.last_changed
FROM users u
INNER JOIN user_credentials c ON u.id = c.user_id
WHERE c.must_change_password = TRUE;
```

### Password Change History (for future):
```sql
-- Get users who haven't changed password in 90 days
SELECT u.username, c.last_changed, 
       DATEDIFF(NOW(), c.last_changed) as days_since_change
FROM users u
INNER JOIN user_credentials c ON u.id = c.user_id
WHERE c.last_changed < DATE_SUB(NOW(), INTERVAL 90 DAY);
```

---

## 🚀 Future Enhancements

With this architecture, you can easily add:

### 1. Password History
```sql
CREATE TABLE password_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    password_hash VARCHAR(60),
    changed_at DATETIME(6),
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

### 2. OAuth Tokens
```sql
CREATE TABLE oauth_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    provider VARCHAR(50),  -- 'google', 'facebook', etc.
    provider_user_id VARCHAR(255),
    access_token TEXT,
    refresh_token TEXT,
    expires_at DATETIME(6),
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

### 3. MFA Secrets
```sql
CREATE TABLE mfa_secrets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    totp_secret VARCHAR(255),
    backup_codes TEXT,
    enabled BOOLEAN,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

### 4. Session Management
```sql
CREATE TABLE user_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    session_token VARCHAR(255),
    ip_address VARCHAR(45),
    user_agent TEXT,
    created_at DATETIME(6),
    expires_at DATETIME(6),
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

---

## 🐛 Troubleshooting

### Issue: "Table user_credentials doesn't exist"

**Solution:** Restart your Spring Boot application. It will auto-create the table.

### Issue: "Foreign key constraint fails"

**Solution:** The user must exist before creating credentials. Use `@Transactional` on the registration method.

### Issue: "Account locked" error during login

**Solution:** Wait 30 minutes or manually unlock:
```sql
UPDATE user_credentials 
SET failed_login_attempts = 0, locked_until = NULL 
WHERE user_id = 1;
```

### Issue: Password field still in API response

**Solution:** Already fixed! User entity no longer has password field. Credentials are separate.

---

## ✅ Testing Checklist

- [ ] Start Spring Boot app - tables created automatically
- [ ] Verify both `users` and `user_credentials` tables exist
- [ ] Register a user - data appears in both tables
- [ ] Login successfully - get JWT token
- [ ] Get user profile - no password in response
- [ ] Try wrong password 5 times - account gets locked
- [ ] Wait or manually unlock - can login again
- [ ] Check failed_login_attempts resets after successful login

---

## 📚 What You Learned

1. ✅ One-to-one relationships in JPA
2. ✅ Foreign key constraints and CASCADE DELETE
3. ✅ Transaction management with `@Transactional`
4. ✅ Separation of concerns in architecture
5. ✅ Account lockout implementation
6. ✅ Audit trail for security events
7. ✅ Enterprise-grade credential management

---

**Congratulations! You now have a production-grade credential management system!** 🎉🔐

This architecture is used by major companies and provides a solid foundation for future security enhancements.

Happy coding! 🚀


