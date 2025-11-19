# Logout Functionality Documentation

## Overview

This application now implements a secure logout functionality using a **JWT Token Blacklist** approach. When a user logs out, their JWT token is added to a blacklist, preventing it from being used for future authenticated requests.

## How It Works

1. **Token Blacklisting**: When a user logs out, their JWT token is stored in the `token_blacklist` table
2. **Token Validation**: Every authenticated request checks if the token is blacklisted before allowing access
3. **Automatic Cleanup**: Expired tokens are automatically removed from the blacklist every hour

## Database Setup

### Step 1: Create the Token Blacklist Table

Run the SQL migration script to create the required database table:

```bash
mysql -u your_username -p your_database < create-token-blacklist-table.sql
```

Or manually execute:

```sql
CREATE TABLE IF NOT EXISTS token_blacklist (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    token VARCHAR(512) NOT NULL UNIQUE,
    username VARCHAR(255) NOT NULL,
    blacklisted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expiration_time TIMESTAMP NOT NULL,
    INDEX idx_token (token),
    INDEX idx_expiration (expiration_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

## API Endpoints

### Logout Endpoint

**POST** `/api/auth/logout`

Logs out the user by blacklisting their current JWT token.

#### Request Headers:
```
Authorization: Bearer <your_jwt_token>
```

#### Response (Success - 200):
```json
{
  "success": true,
  "message": "Logged out successfully",
  "data": null,
  "statusCode": 200
}
```

#### Response (Error - 400):
```json
{
  "success": false,
  "message": "Invalid authorization header",
  "data": null,
  "statusCode": 400
}
```

#### Response (Error - 500):
```json
{
  "success": false,
  "message": "Logout failed: <error_message>",
  "data": null,
  "statusCode": 500
}
```

## Testing the Logout Functionality

### Using cURL:

#### 1. Login to get a token:
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123"
  }'
```

**Response:**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "username": "testuser",
    "message": "Login successful"
  },
  "statusCode": 200
}
```

#### 2. Test authenticated endpoint (should work):
```bash
curl -X GET http://localhost:8080/api/auth/connected \
  -H "Authorization: Bearer <your_token_here>"
```

**Response:**
```json
{
  "success": true,
  "message": "You're connected",
  "data": null,
  "statusCode": 200
}
```

#### 3. Logout:
```bash
curl -X POST http://localhost:8080/api/auth/logout \
  -H "Authorization: Bearer <your_token_here>"
```

**Response:**
```json
{
  "success": true,
  "message": "Logged out successfully",
  "data": null,
  "statusCode": 200
}
```

#### 4. Try to use the same token again (should fail):
```bash
curl -X GET http://localhost:8080/api/auth/connected \
  -H "Authorization: Bearer <your_token_here>"
```

**Response:**
```json
{
  "timestamp": "2024-11-18T10:30:00.000+00:00",
  "status": 403,
  "error": "Forbidden",
  "path": "/api/auth/connected"
}
```

## Architecture

### New Components

#### 1. TokenBlacklist Entity (`model/TokenBlacklist.java`)
- Stores blacklisted JWT tokens
- Includes token, username, blacklist timestamp, and expiration time
- Uses database indexes for fast lookup

#### 2. TokenBlacklistRepository (`repository/TokenBlacklistRepository.java`)
- Provides database operations for token blacklist
- Methods:
  - `existsByToken(String token)`: Check if token is blacklisted
  - `deleteExpiredTokens(LocalDateTime now)`: Remove expired tokens
  - `deleteByUsername(String username)`: Remove all tokens for a user

#### 3. TokenBlacklistService (`service/TokenBlacklistService.java`)
- Business logic for token blacklisting
- Methods:
  - `blacklistToken(String token)`: Add token to blacklist
  - `isTokenBlacklisted(String token)`: Check if token is blacklisted
  - `logoutAllDevices(String username)`: Logout from all devices
  - `cleanupExpiredTokens()`: Scheduled cleanup (runs hourly)

#### 4. Updated Components

**JwtRequestFilter** (`security/JwtRequestFilter.java`):
- Now checks if token is blacklisted before authentication
- Rejects blacklisted tokens with 403 Forbidden

**AuthController** (`controller/AuthController.java`):
- Added `/logout` endpoint
- Extracts token from Authorization header
- Calls TokenBlacklistService to blacklist token

**SpringbootApplication** (`SpringbootApplication.java`):
- Added `@EnableScheduling` annotation
- Enables scheduled cleanup of expired tokens

## Security Features

1. **Stateless Authentication**: JWT tokens remain stateless
2. **Immediate Invalidation**: Tokens are invalidated immediately upon logout
3. **Automatic Cleanup**: Expired tokens are removed to prevent database bloat
4. **Indexed Lookups**: Fast token validation using database indexes
5. **Distributed Ready**: Works across multiple application instances

## Performance Considerations

- **Database Lookups**: Each authenticated request performs one additional database query
- **Index Optimization**: Token column is indexed for O(log n) lookup time
- **Automatic Cleanup**: Expired tokens are removed hourly to keep table size manageable
- **Memory Efficient**: No in-memory storage required

## Best Practices

1. **Token Lifetime**: Keep JWT expiration time reasonable (e.g., 1 hour)
2. **Regular Cleanup**: The scheduled cleanup runs hourly by default
3. **Monitor Table Size**: Check `token_blacklist` table size periodically
4. **Consider Redis**: For high-traffic apps, consider Redis for token blacklist

## Troubleshooting

### Issue: Logout endpoint returns 403 Forbidden
**Solution**: The logout endpoint requires authentication. Make sure you're sending the Authorization header with a valid (not yet blacklisted) token.

### Issue: Token still works after logout
**Solution**: 
1. Check if the token was successfully added to the blacklist table
2. Verify JwtRequestFilter is checking the blacklist
3. Ensure TokenBlacklistService is properly autowired

### Issue: Database connection errors
**Solution**: 
1. Verify the `token_blacklist` table exists
2. Check database credentials in `application.properties`
3. Ensure the database user has CREATE, SELECT, INSERT, DELETE permissions

### Issue: Scheduled cleanup not running
**Solution**: 
1. Verify `@EnableScheduling` is present on SpringbootApplication
2. Check application logs for scheduling errors
3. Ensure Spring Boot version supports @Scheduled annotation

## Future Enhancements

Potential improvements to consider:

1. **Logout from All Devices**: Implement endpoint to invalidate all tokens for a user
2. **Redis Integration**: Use Redis for faster blacklist lookups in production
3. **Token Refresh**: Implement refresh tokens for better user experience
4. **Activity Tracking**: Log logout events for security auditing
5. **IP-Based Security**: Track IP addresses for additional security

## Related Files

- `/src/main/java/com/learning/springboot/model/TokenBlacklist.java`
- `/src/main/java/com/learning/springboot/repository/TokenBlacklistRepository.java`
- `/src/main/java/com/learning/springboot/service/TokenBlacklistService.java`
- `/src/main/java/com/learning/springboot/security/JwtRequestFilter.java`
- `/src/main/java/com/learning/springboot/controller/AuthController.java`
- `/src/main/java/com/learning/springboot/SpringbootApplication.java`
- `/create-token-blacklist-table.sql`

