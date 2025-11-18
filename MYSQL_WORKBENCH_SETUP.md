# MySQL Workbench Connection Guide

## ✅ Your MySQL is Now Ready!

MySQL has been successfully installed and configured on your machine.

---

## 🔌 Connect with MySQL Workbench

### Method 1: Using springuser (Recommended)

1. **Open MySQL Workbench**
2. **Click the "+" icon** next to "MySQL Connections"
3. **Fill in the connection details:**

```
Connection Name: SpringBoot UserDB
Connection Method: Standard (TCP/IP)
Hostname: 127.0.0.1
Port: 3306
Username: springuser
Default Schema: userdb
```

4. **Click "Store in Keychain..." next to Password**
   - Enter password: `springpass`
   
5. **Click "Test Connection"**
   - Should show: "Successfully made the MySQL connection"
   
6. **Click "OK"** to save the connection

7. **Double-click the connection** to open it

---

### Method 2: Using root (Alternative)

If you prefer to use root:

```
Connection Name: MySQL Root
Connection Method: Standard (TCP/IP)
Hostname: 127.0.0.1
Port: 3306
Username: root
Password: (leave empty - no password)
Default Schema: userdb
```

---

## 🎯 What to Do After Connecting

### 1. View Your Database Structure

In MySQL Workbench:
- Look at the left sidebar under "SCHEMAS"
- You should see `userdb`
- Expand it to see tables (will be empty until you run Spring Boot app)

### 2. Run Your Spring Boot Application

- Start `SpringbootApplication.java` from your IDE
- Spring Boot will automatically create tables:
  - `users`
  - `auth_users`

### 3. Refresh in MySQL Workbench

- Right-click on `userdb` → **Refresh All**
- Expand "Tables" to see the new tables

### 4. Query Your Data

Click the table icon next to a table name to view data:

```sql
-- View all users
SELECT * FROM userdb.users;

-- View all auth users (without passwords for safety)
SELECT id, username, email FROM userdb.auth_users;
```

---

## 📊 Useful MySQL Workbench Features

### Execute Queries

1. Click **File → New Query Tab** (or Ctrl+T / Cmd+T)
2. Write your SQL:
```sql
USE userdb;

-- Show all tables
SHOW TABLES;

-- Count users
SELECT COUNT(*) as total FROM users;

-- Find specific user
SELECT * FROM users WHERE email = 'john@example.com';
```
3. Click the **lightning bolt icon** to execute

### View Table Structure

- Right-click table → **Table Inspector**
- See columns, indexes, foreign keys, etc.

### Export Data

- Right-click table → **Table Data Export Wizard**
- Export to CSV, JSON, etc.

### Import Data

- Right-click table → **Table Data Import Wizard**
- Import from CSV, JSON, etc.

---

## 🛠️ Troubleshooting

### Issue: "Can't connect to MySQL server on '127.0.0.1'"

**Solution:**
```bash
# Check if MySQL is running
brew services list | grep mysql

# If not running, start it
brew services start mysql
```

### Issue: "Access denied for user 'springuser'@'localhost'"

**Solution:**
```bash
# Recreate the user
mysql -u root -e "DROP USER IF EXISTS 'springuser'@'localhost';"
mysql -u root -e "CREATE USER 'springuser'@'localhost' IDENTIFIED BY 'springpass';"
mysql -u root -e "GRANT ALL PRIVILEGES ON userdb.* TO 'springuser'@'localhost';"
mysql -u root -e "FLUSH PRIVILEGES;"
```

### Issue: Tables not appearing

**Solution:**
- Make sure you've started your Spring Boot application at least once
- Spring Boot creates tables on first startup
- Refresh MySQL Workbench (Right-click → Refresh)

---

## 🔐 Security Best Practices

### For Development:
- Current setup is fine with simple passwords
- `springuser` / `springpass` is easy to remember

### For Production:
- Use strong passwords
- Store credentials in environment variables
- Never commit passwords to Git
- Use SSL/TLS connections

---

## 📝 Quick Commands in MySQL Workbench

| Action | Shortcut (Mac) | Shortcut (Windows) |
|--------|---------------|-------------------|
| New Query Tab | Cmd+T | Ctrl+T |
| Execute Query | Cmd+Enter | Ctrl+Enter |
| Execute All | Cmd+Shift+Enter | Ctrl+Shift+Enter |
| Format Query | Cmd+B | Ctrl+B |
| Comment Line | Cmd+/ | Ctrl+/ |

---

## 🎯 Next Steps

1. ✅ Connect to MySQL Workbench using the settings above
2. ✅ Start your Spring Boot application
3. ✅ Register a user via API
4. ✅ Check MySQL Workbench to see the data appear!

---

## 📚 Additional Resources

- MySQL Workbench Docs: https://dev.mysql.com/doc/workbench/en/
- MySQL Tutorial: https://www.mysqltutorial.org/
- Spring Data JPA: https://spring.io/projects/spring-data-jpa

---

**Your Connection Details:**

```
Hostname: 127.0.0.1 or localhost
Port: 3306
Database: userdb
Username: springuser
Password: springpass
```

Happy coding! 🚀



