#!/bin/bash

echo "========================================="
echo "MySQL Connection Test"
echo "========================================="
echo ""

# Check if MySQL is running
echo "1. Checking MySQL Service Status..."
brew services list | grep mysql
echo ""

# Test connection
echo "2. Testing Connection to MySQL..."
mysql -u springuser -pspringpass -e "SELECT 'Connection Successful!' as Status;" 2>/dev/null
if [ $? -eq 0 ]; then
    echo "✅ Connection successful!"
else
    echo "❌ Connection failed!"
    exit 1
fi
echo ""

# Show databases
echo "3. Available Databases:"
mysql -u springuser -pspringpass -e "SHOW DATABASES;" 2>/dev/null
echo ""

# Show userdb tables
echo "4. Tables in userdb:"
mysql -u springuser -pspringpass -D userdb -e "SHOW TABLES;" 2>/dev/null
echo ""

# Show connection info
echo "5. Connection Information:"
echo "   Hostname: localhost"
echo "   Port: 3306"
echo "   Database: userdb"
echo "   Username: springuser"
echo "   Password: springpass"
echo ""

echo "========================================="
echo "✅ MySQL is ready for your Spring Boot app!"
echo "========================================="



