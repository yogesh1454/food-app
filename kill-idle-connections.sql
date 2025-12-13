-- Kill all idle connections to free up connection slots
-- Run this in DBeaver or psql when you get "remaining connection slots" error

SELECT 
    pg_terminate_backend(pid),
    usename,
    application_name,
    state,
    state_change
FROM pg_stat_activity 
WHERE datname = 'nastto_db'
  AND pid <> pg_backend_pid()  -- Don't kill your own connection
  AND state = 'idle'            -- Only kill idle connections
  AND state_change < NOW() - INTERVAL '5 minutes';  -- Idle for more than 5 minutes

-- To see all current connections:
-- SELECT pid, usename, application_name, client_addr, state, state_change 
-- FROM pg_stat_activity 
-- WHERE datname = 'nastto_db';
