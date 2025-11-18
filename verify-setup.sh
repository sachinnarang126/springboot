#!/bin/bash

echo "========================================="
echo "Database Setup Verification"
echo "========================================="
echo ""

echo "1. Checking tables in userdb..."
mysql -u springuser -pspringpass -D userdb -e "SHOW TABLES;" 2>/dev/null

echo ""
echo "2. If 'users' table exists, showing structure..."
mysql -u springuser -pspringpass -D userdb -e "DESCRIBE users;" 2>/dev/null

echo ""
echo "3. Counting users..."
mysql -u springuser -pspringpass -D userdb -e "SELECT COUNT(*) as total_users FROM users;" 2>/dev/null || echo "Table doesn't exist yet - restart Spring Boot app!"

echo ""
echo "========================================="
echo "If table doesn't exist:"
echo "  → Restart your Spring Boot application"
echo "  → Spring Boot will auto-create the table"
echo "========================================="



