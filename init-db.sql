-- Create custom user
CREATE USER menghor WITH ENCRYPTED PASSWORD 'Hour1819';

-- Create database
CREATE DATABASE ks_it_school OWNER menghor;

-- Grant all privileges
GRANT ALL PRIVILEGES ON DATABASE ks_it_school TO menghor;

-- Connect to the new database
\c ks_it_school menghor;

-- Grant all privileges on schema
GRANT ALL ON SCHEMA public TO menghor;
GRANT CREATE ON SCHEMA public TO menghor;