-- =============================================
-- Manual fix: Update existing budgets to set version = 0
-- Run this AFTER restarting the application
-- =============================================

-- Check current state
SELECT id, name, version FROM budgets LIMIT 5;

-- Update all budgets that have NULL version
UPDATE budgets SET version = 0 WHERE version IS NULL;

-- Verify the update
SELECT id, name, version FROM budgets LIMIT 5;
