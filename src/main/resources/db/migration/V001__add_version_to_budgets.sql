-- =============================================
-- Migration: Add version column to budgets table
-- Purpose: Enable optimistic locking for concurrent updates
-- Date: 2025-12-11
-- =============================================

-- Add version column with default value 0
ALTER TABLE budgets
ADD COLUMN version BIGINT DEFAULT 0 COMMENT 'Version for optimistic locking';

-- Update existing records to have version = 0
UPDATE budgets SET version = 0 WHERE version IS NULL;

-- Make sure all existing budgets have version set
-- This prevents NullPointerException when JPA tries to increment version
