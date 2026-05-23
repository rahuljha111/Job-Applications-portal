-- Create database if it doesn't exist
-- Run this with: psql -U postgres -h localhost -p 5432 -f setup-db.sql

-- Drop if exists (optional - only if you want a fresh start)
-- DROP DATABASE IF EXISTS jobportal1;

-- Create database
CREATE DATABASE jobportal1;

-- Grant permissions
GRANT ALL PRIVILEGES ON DATABASE jobportal1 TO postgres;
