# 🚀 Testing Guide After Schema Drop

## Current Status ✅

- ✅ Database `userdb` recreated
- ✅ User `springuser` has permissions
- ⏳ `users` table doesn't exist yet (will be created by Spring Boot)

---

## 📝 Step-by-Step Testing

### Step 1: Restart Spring Boot Application

**Stop your app** (if running) and **start it again**

```bash
# In your IDE: Run SpringbootApplication.java
# OR
./mvnw spring-boot:run
```

**Look for these logs:**
```
Hibernate: create table users (id bigint not null auto_increment, ...)
```

✅ This means the table was created!

---

### Step 2: Verify Table Creation

Run the verification script:
```bash
./verify-setup.sh
```

**Expected output:**
```
1. Checking tables in userdb...
Tables_in_userdb
users

2. If 'users' table exists, showing structure...
Field      | Type         | Null | Key
-----------+--------------+------+-----
id         | bigint       | NO   | PRI
username   | varchar(50)  | NO   | UNI
password   | varchar(255) | NO   |
email      | varchar(255) | NO   | UNI
name       | varchar(255) | NO   |
created_at | datetime(6)  | YES  |
updated_at | datetime(6)  | YES  |
```

---

### Step 3: Register Your First User

**Endpoint:** `POST http://localhost:8080/api/auth/register`

**Important:** The `/api/auth/**` endpoints are **PUBLIC** (no JWT needed)

**Request:**
```json
{
  "username": "john",
  "password": "pass123",
  "email": "john@example.com",
  "name": "John Doe"
}
```

**Using cURL:**
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

**Expected Response (201 Created):**
```json
{
  "success": true,
  "message": "User registered successfully",
  "timestamp": "2025-11-17T...",
  "statusCode": 201
}
```

---

### Step 4: Login to Get JWT Token

**Endpoint:** `POST http://localhost:8080/api/auth/login`

**Request:**
```json
{
  "username": "john",
  "password": "pass123"
}
```

**Using cURL:**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john",
    "password": "pass123"
  }'
```

**Expected Response (200 OK):**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huIiwiaWF0IjoxNzAwMTIzNDU2LCJleHAiOjE3MDAxNTk0NTZ9...",
    "username": "john",
    "message": "Login successful"
  },
  "timestamp": "2025-11-17T...",
  "statusCode": 200
}
```

**📋 Copy the `token` value** - you'll need it for protected endpoints!

---

### Step 5: Access Protected Endpoints (with JWT)

Now the `/api/users/**` endpoints require authentication.

**Endpoint:** `GET http://localhost:8080/api/users`

**Using cURL:**
```bash
# Replace YOUR_TOKEN with the actual token from login
curl -X GET http://localhost:8080/api/users \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**Expected Response (200 OK):**
```json
{
  "success": true,
  "message": "Users retrieved successfully",
  "data": [
    {
      "id": 1,
      "username": "john",
      "email": "john@example.com",
      "name": "John Doe",
      "createdAt": "2025-11-17T...",
      "updatedAt": "2025-11-17T..."
    }
  ],
  "timestamp": "2025-11-17T...",
  "statusCode": 200
}
```

---

## 🔍 Verify in MySQL

```bash
# View all users
mysql -u springuser -pspringpass -D userdb -e "SELECT id, username, email, name FROM users;"

# View with timestamps
mysql -u springuser -pspringpass -D userdb -e "SELECT * FROM users;"
```

**Or use MySQL Workbench** to browse the data visually!

---

## ⚠️ Understanding 403 Errors

### Public Endpoints (No JWT Required):
- ✅ `POST /api/auth/register` - Anyone can register
- ✅ `POST /api/auth/login` - Anyone can login
- ✅ These should NEVER return 403

### Protected Endpoints (JWT Required):
- 🔒 `GET /api/users` - Requires JWT token
- 🔒 `GET /api/users/{id}` - Requires JWT token
- 🔒 `POST /api/users` - Requires JWT token
- 🔒 `PUT /api/users/{id}` - Requires JWT token
- 🔒 `DELETE /api/users/{id}` - Requires JWT token
- 🔒 `GET /api/auth/validate` - Requires JWT token

**If these return 403:**
- ❌ You didn't include Authorization header
- ❌ Token is invalid or expired
- ❌ Token format is wrong (must be `Bearer YOUR_TOKEN`)

---

## 🐛 Troubleshooting

### Error: "403 Forbidden" on `/api/auth/register`

**Problem:** Spring Boot can't connect to database

**Solution:**
```bash
# Check if database exists
mysql -u root -e "SHOW DATABASES LIKE 'userdb';"

# If not, recreate it
mysql -u root -e "CREATE DATABASE userdb;"
mysql -u root -e "GRANT ALL PRIVILEGES ON userdb.* TO 'springuser'@'localhost';"

# Restart Spring Boot app
```

### Error: "403 Forbidden" on `/api/users`

**This is EXPECTED!** You need a JWT token.

**Solution:**
1. Register a user
2. Login to get token
3. Use token in Authorization header:
   ```
   Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
   ```

### Error: "Unknown database 'userdb'"

**Solution:**
```bash
mysql -u root -e "CREATE DATABASE userdb;"
```

### Error: "Table 'userdb.users' doesn't exist"

**Solution:** Restart your Spring Boot application

### Error: "Access denied for user 'springuser'"

**Solution:**
```bash
mysql -u root -e "GRANT ALL PRIVILEGES ON userdb.* TO 'springuser'@'localhost'; FLUSH PRIVILEGES;"
```

---

## ✅ Complete Test Flow

```bash
# 1. Register user
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"john","password":"pass123","email":"john@example.com","name":"John Doe"}'

# 2. Login (copy the token from response)
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"john","password":"pass123"}'

# 3. Get all users (replace YOUR_TOKEN)
curl -X GET http://localhost:8080/api/users \
  -H "Authorization: Bearer YOUR_TOKEN"

# 4. Get specific user
curl -X GET http://localhost:8080/api/users/1 \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

## 🎯 Quick Checklist

Before testing APIs:
- [ ] MySQL is running (`brew services list | grep mysql`)
- [ ] Database `userdb` exists
- [ ] Spring Boot app is running
- [ ] Table `users` was created (check logs)
- [ ] Can register a user (no 403 on register)
- [ ] Can login and get token
- [ ] Can access protected endpoints with token

---

## 💡 Pro Tip

Save your JWT token in an environment variable:
```bash
# Login and save token
export JWT_TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"john","password":"pass123"}' \
  | jq -r '.data.token')

# Use it
curl -X GET http://localhost:8080/api/users \
  -H "Authorization: Bearer $JWT_TOKEN"
```

---

**Need help?** Run `./verify-setup.sh` to check your setup status!

Good luck! 🚀



