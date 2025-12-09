# JWT Security Setup Guide

## 🔐 IMPORTANT: JWT Secret Configuration

This application uses JWT for authentication. **You MUST configure a secure JWT secret before deploying to production.**

### For Development

The application includes a weak default secret for development only. This is **NOT** secure for production use.

### For Production

#### Option 1: Environment Variable (Recommended)

1. Generate a secure secret key (256+ bits):
```bash
openssl rand -base64 32
```

2. Set the environment variable:
```bash
export JWT_SECRET="your-generated-secret-here"
export SPRING_PROFILES_ACTIVE=prod
```

3. Run the application:
```bash
./mvnw spring-boot:run
```

#### Option 2: Application Properties

Create `application-prod.properties`:
```properties
spring.profiles.active=prod
app.jwt.secret=${JWT_SECRET}
app.jwt.expiration-ms=86400000
```

Then set the environment variable before running:
```bash
export JWT_SECRET="your-generated-secret-here"
java -jar target/expense-management.jar --spring.profiles.active=prod
```

#### Option 3: Docker

```dockerfile
docker run -e JWT_SECRET="your-secret" -e SPRING_PROFILES_ACTIVE=prod expense-management
```

### Security Best Practices

✅ **DO:**
- Generate secrets using cryptographically secure random generators
- Use at least 256 bits (32 bytes) for HS256
- Store secrets in environment variables or secure vaults (AWS Secrets Manager, Azure Key Vault, HashiCorp Vault)
- Rotate secrets periodically
- Use different secrets for different environments

❌ **DON'T:**
- Commit secrets to version control
- Use default/example secrets in production
- Share secrets in plain text
- Use weak/predictable secrets

### Validation

The application will fail to start if:
- JWT secret is not configured
- Secret is too short (< 32 bytes)

### Generate Secret Examples

```bash
# Using OpenSSL (recommended)
openssl rand -base64 32

# Using Python
python3 -c "import secrets; print(secrets.token_urlsafe(32))"

# Using Node.js
node -e "console.log(require('crypto').randomBytes(32).toString('base64'))"
```

### Troubleshooting

**Error: "app.jwt.secret is required"**
- You forgot to set the JWT_SECRET environment variable

**Error: "Secret key too short"**
- Your secret must be at least 32 bytes (characters)

**Token validation fails**
- Secret changed after tokens were issued
- All users must re-login when secret changes
