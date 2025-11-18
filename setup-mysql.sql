-- MySQL Database Setup Script
-- Run these commands in MySQL to set up your database

-- Create the database
CREATE DATABASE IF NOT EXISTS userdb;

-- Create a user for the application (optional but recommended)
CREATE USER IF NOT EXISTS 'springuser'@'localhost' IDENTIFIED BY 'springpass';

-- Grant all privileges on userdb to springuser
GRANT ALL PRIVILEGES ON userdb.* TO 'springuser'@'localhost';

-- Apply the changes
FLUSH PRIVILEGES;

-- Verify database was created
SHOW DATABASES;

-- Use the database
USE userdb;

-- Show tables (will be empty initially, Spring Boot will create them)
SHOW TABLES;



