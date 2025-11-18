# 🚀 Quick Start Guide - Separate Credentials Table

## ✅ What's Done

Your Spring Boot app has been refactored with a **separate credentials table** architecture!

---

## 🎯 What You Need to Do NOW

### Step 1: Restart Your Spring Boot Application

**Stop your current app** (if running) and **start it again**.

```bash
# In your IDE: Run SpringbootApplication.java
```

**Spring Boot will automatically create:**
- ✅ `users` table (profile data)
- ✅ `user_credentials` table (passwords, lockouts)

**Look for these logs:**
```
Hibernate: create table users (...)
Hibernate: create table user_credentials (...)
```

---

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

---

### Step 3: Register Your First User

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

**Expected Response:**
```json
{
  "success": true,
  "message": "User registered successfully",
  "statusCode": 201
}
```

---

### Step 4: Check Data in Both Tables

```bash
# Users table (profile data)
mysql -u springuser -pspringpass -D userdb \
  -e "SELECT * FROM users;"

# Credentials table (password data)
mysql -u springuser -pspringpass -D userdb \
  -e "SELECT id, user_id, LEFT(password_hash, 20) as hash, failed_login_attempts FROM user_credentials;"
```

**You should see:**
```
users:
id=1, username=john, email=john@example.com, name=John Doe

user_credentials:
id=1, user_id=1, hash=$2a$10$N9qo8uLOickgx, failed_login_attempts=0
```

---

### Step 5: Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john",
    "password": "pass123"
  }'
```

**Copy the JWT token from the response!**

---

### Step 6: Get User Profile (No Password!)

```bash
# Replace YOUR_TOKEN with actual token
curl -X GET http://localhost:8080/api/users \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "username": "john",
      "email": "john@example.com",
      "name": "John Doe"
    }
  ]
}
```

✅ **No password field!** Credentials are completely separate.

---

## 🔐 New Features You Got

### 1. Account Lockout
Try wrong password 5 times:
```bash
for i in {1..5}; do
  curl -X POST http://localhost:8080/api/auth/login \
    -H "Content-Type: application/json" \
    -d '{"username":"john","password":"wrong"}'
  echo ""
done
```

**Result:** Account locked for 30 minutes! 🔒

### 2. Failed Login Tracking
```sql
mysql -u springuser -pspringpass -D userdb \
  -e "SELECT failed_login_attempts, locked_until FROM user_credentials WHERE user_id=1;"
```

### 3. Password Change Tracking
```sql
mysql -u springuser -pspringpass -D userdb \
  -e "SELECT last_changed FROM user_credentials WHERE user_id=1;"
```

---

## 📊 Architecture Overview

```
Registration:
User fills form → AuthController → UserService
                                   ├─ Create User (users table)
                                   └─ Create Credential (user_credentials table)

Login:
User enters password → AuthController → CustomUserDetailsService
                                        ├─ Query User (users table)
                                        ├─ Query Credential (user_credentials table)
                                        ├─ Check lockout
                                        └─ Verify password → Track failed attempts

Get Profile:
User requests /api/users → UserController → UserService
                                            └─ Return User (NO password!)
```

---

## 📁 What Was Created

### New Files:
1. ✨ `UserCredential.java` - Credential entity
2. ✨ `UserCredentialRepository.java` - Credential repository
3. ✨ `UserCredentialService.java` - Credential service
4. ✨ `CREDENTIALS_REFACTORING.md` - Full documentation

### Modified Files:
5. ✏️ `User.java` - Removed password field
6. ✏️ `CustomUserDetailsService.java` - Updated for separate tables
7. ✏️ `AuthController.java` - Minor fixes

---

## 🎯 Key Benefits

✅ **Security Separation** - Profile data separate from passwords  
✅ **Account Lockout** - Automatic brute-force protection  
✅ **Audit Trail** - Track password changes, failed logins  
✅ **Extensible** - Easy to add OAuth, MFA, password history  
✅ **Production-Ready** - Enterprise-grade architecture  

---

## 📖 Documentation

- **`CREDENTIALS_REFACTORING.md`** - Complete refactoring guide
- **`TEST_GUIDE.md`** - API testing guide
- **`PASSWORD_SECURITY.md`** - Password security best practices

---

## 🐛 Troubleshooting

**Tables not created?**
→ Restart Spring Boot app

**403 on register?**
→ Check logs, ensure database connection works

**Account locked?**
→ Wait 30 min or unlock manually:
```sql
UPDATE user_credentials SET failed_login_attempts=0, locked_until=NULL WHERE user_id=1;
```

---

**That's it! You now have enterprise-grade credential management!** 🎉

Start your app and test it out! 🚀


