-- Align journal entry tables with domain model (UUID PK, column names)

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- journal_entries: add UUID id and make it primary key
ALTER TABLE finance.journal_entries
    ADD COLUMN IF NOT EXISTS id UUID;

UPDATE finance.journal_entries
SET id = COALESCE(id, gen_random_uuid());

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.table_constraints
        WHERE constraint_name = 'journal_entries_pkey'
          AND table_schema = 'finance'
          AND table_name = 'journal_entries'
    ) THEN
        ALTER TABLE finance.journal_entries DROP CONSTRAINT journal_entries_pkey;
    END IF;
END $$;

ALTER TABLE finance.journal_entries
    ADD CONSTRAINT journal_entries_pkey PRIMARY KEY (id);

-- Rename reference column to match entity
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'finance'
          AND table_name = 'journal_entries'
          AND column_name = 'reference'
    ) THEN
        ALTER TABLE finance.journal_entries RENAME COLUMN reference TO reference_id;
    END IF;
END $$;

-- Ensure enum-length friendly columns
ALTER TABLE finance.journal_entries
    ALTER COLUMN entry_type TYPE VARCHAR(30),
    ALTER COLUMN status TYPE VARCHAR(20);

-- journal_entry_lines: introduce journal_entry_id uuid FK to journal_entries(id)
ALTER TABLE finance.journal_entry_lines
    ADD COLUMN IF NOT EXISTS journal_entry_id UUID;

-- migrate data from old entry_id (varchar) if present
UPDATE finance.journal_entry_lines jel
SET journal_entry_id = je.id
FROM finance.journal_entries je
WHERE jel.journal_entry_id IS NULL
  AND EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'finance'
          AND table_name = 'journal_entry_lines'
          AND column_name = 'entry_id'
    )
  AND je.entry_id = jel.entry_id;

-- drop old FK and column if they exist
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.constraint_column_usage
        WHERE table_schema = 'finance'
          AND table_name = 'journal_entry_lines'
          AND constraint_name = 'fk_journal_entry'
    ) THEN
        ALTER TABLE finance.journal_entry_lines DROP CONSTRAINT fk_journal_entry;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'finance'
          AND table_name = 'journal_entry_lines'
          AND column_name = 'entry_id'
    ) THEN
        ALTER TABLE finance.journal_entry_lines DROP COLUMN entry_id;
    END IF;
END $$;

ALTER TABLE finance.journal_entry_lines
    ALTER COLUMN journal_entry_id SET NOT NULL;

ALTER TABLE finance.journal_entry_lines
    ADD CONSTRAINT fk_journal_entry_id
        FOREIGN KEY (journal_entry_id)
        REFERENCES finance.journal_entries(id)
        ON DELETE CASCADE;

-- rename text column to description to match entity mapping
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'finance'
          AND table_name = 'journal_entry_lines'
          AND column_name = 'text'
    ) THEN
        ALTER TABLE finance.journal_entry_lines RENAME COLUMN text TO description;
    END IF;
END $$;

-- index for FK lookups
CREATE INDEX IF NOT EXISTS idx_journal_entry_lines_entry_id ON finance.journal_entry_lines(journal_entry_id);
