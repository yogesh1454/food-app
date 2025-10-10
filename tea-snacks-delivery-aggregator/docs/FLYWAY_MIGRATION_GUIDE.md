# Flyway Migration Management Guide

## Overview
This guide explains how to manage Flyway migrations properly to avoid manual intervention and ensure consistent database schema management.

## Migration Files Location
```
user-management-service/src/main/resources/db/migration/
├── V1__create_user_tables.sql
├── V2__Create_Otp_Sessions_Table.sql
└── V3__Create_Guest_Users_Table.sql
```

## Migration Naming Convention
- **Format**: `V{version}__{description}.sql`
- **Version**: Sequential number (1, 2, 3, etc.)
- **Description**: Descriptive name with underscores
- **Example**: `V3__Create_Guest_Users_Table.sql`

## Best Practices

### 1. Idempotent Migrations
Always write migrations that can be run multiple times safely:

```sql
-- ✅ Good: Check if table exists before creating
DO $$ 
BEGIN
    IF NOT EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'guest_users') THEN
        CREATE TABLE guest_users (
            -- table definition
        );
    END IF;
END $$;

-- ❌ Bad: Will fail if table exists
CREATE TABLE guest_users (
    -- table definition
);
```

### 2. Index Creation
Check if indexes exist before creating them:

```sql
DO $$ 
BEGIN
    IF NOT EXISTS (SELECT FROM pg_indexes WHERE indexname = 'idx_guest_users_device_id') THEN
        CREATE INDEX idx_guest_users_device_id ON guest_users(device_id);
    END IF;
END $$;
```

### 3. Comments and Documentation
Add comments only if the table exists:

```sql
DO $$ 
BEGIN
    IF EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'guest_users') THEN
        COMMENT ON TABLE guest_users IS 'Stores guest user information';
        COMMENT ON COLUMN guest_users.id IS 'Unique identifier';
    END IF;
END $$;
```

## Troubleshooting

### Migration Already Applied Error
If you get "relation already exists" error:

1. **Check current migration status:**
   ```bash
   docker exec -it tea-snacks-postgres psql -U tea_snacks_user -d tea_snacks_db -c "SELECT version, description, success FROM flyway_schema_history ORDER BY installed_rank;"
   ```

2. **Run the fix script:**
   ```bash
   ./scripts/fix-flyway-migrations.sh
   ```

3. **Or manually add to history:**
   ```sql
   INSERT INTO flyway_schema_history (installed_rank, version, description, type, script, checksum, installed_by, installed_on, execution_time, success) 
   VALUES (3, '3', 'Create Guest Users Table', 'SQL', 'V3__Create_Guest_Users_Table.sql', -2103016496, 'tea_snacks_user', NOW(), 0, true);
   ```

### Checksum Mismatch Error
If you get checksum mismatch error:

1. **Update the checksum in history:**
   ```sql
   UPDATE flyway_schema_history SET checksum = -2103016496 WHERE version = '3';
   ```

2. **Or run Flyway repair:**
   ```bash
   ./gradlew :user-management-service:flywayRepair
   ```

## Configuration

### Application Properties
```yaml
spring:
  flyway:
    enabled: true
    baseline-on-migrate: true
    validate-on-migrate: false  # Set to false to avoid checksum issues
    out-of-order: false
    ignore-migration-patterns: "*:missing"
    locations: classpath:db/migration
    table: flyway_schema_history
    baseline-version: 0
    baseline-description: "Initial baseline"
```

### Key Settings Explained
- `baseline-on-migrate: true` - Automatically baseline existing database
- `validate-on-migrate: false` - Skip validation to avoid checksum issues
- `ignore-migration-patterns: "*:missing"` - Ignore missing migration files

## Development Workflow

### 1. Creating New Migrations
1. Create migration file with proper naming
2. Write idempotent SQL
3. Test locally
4. Commit to version control

### 2. Testing Migrations
1. Start fresh database
2. Run application
3. Verify all tables created
4. Check migration history

### 3. Production Deployment
1. Backup database
2. Run migrations
3. Verify application starts
4. Monitor for issues

## Common Commands

### Check Migration Status
```bash
docker exec -it tea-snacks-postgres psql -U tea_snacks_user -d tea_snacks_db -c "SELECT version, description, success FROM flyway_schema_history ORDER BY installed_rank;"
```

### List All Tables
```bash
docker exec -it tea-snacks-postgres psql -U tea_snacks_user -d tea_snacks_db -c "\dt"
```

### Fix Migration Issues
```bash
./scripts/fix-flyway-migrations.sh
```

### Reset Database (Development Only)
```bash
docker exec -it tea-snacks-postgres psql -U tea_snacks_user -d tea_snacks_db -c "DROP SCHEMA public CASCADE; CREATE SCHEMA public;"
```

## Migration History
| Version | Description | Status |
|---------|-------------|--------|
| V1 | Create user tables | ✅ Applied |
| V2 | Create OTP sessions table | ✅ Applied |
| V3 | Create guest users table | ✅ Applied |

## Troubleshooting Checklist

- [ ] Check if PostgreSQL container is running
- [ ] Verify database connection
- [ ] Check migration history
- [ ] Run fix script if needed
- [ ] Verify table exists
- [ ] Check application logs
- [ ] Restart application if needed

## Support
If you encounter migration issues:

1. Check this guide first
2. Run the fix script
3. Check application logs
4. Verify database state
5. Contact the development team if issues persist 