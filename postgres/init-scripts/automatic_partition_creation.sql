CREATE OR REPLACE PROCEDURE create_transaction_partition(start_block BIGINT, end_block BIGINT)
LANGUAGE plpgsql AS $$
DECLARE
    partition_name TEXT; -- Holds the name of the new partition
BEGIN
    -- Generate the partition name based on the block range
    partition_name := format('transactions_p%010d_%010d', start_block, end_block - 1);

    -- Create the partition if it does not already exist
    EXECUTE format('
        CREATE TABLE IF NOT EXISTS %I
        PARTITION OF transactions
        FOR VALUES FROM (%L) TO (%L);
    ', partition_name, start_block, end_block);

    -- Create a trigger for the partition to maintain the tsvector search index
    EXECUTE format('
        DO $$ BEGIN
            IF NOT EXISTS (
                SELECT 1
                FROM pg_trigger
                WHERE tgname = %L
            ) THEN
                CREATE TRIGGER tsvectorupdate
                BEFORE INSERT OR UPDATE
                ON %I
                FOR EACH ROW
                EXECUTE FUNCTION transactions_search_vector_update();
            END IF;
        END $$;
    ', partition_name || '_tsvectorupdate', partition_name);

    -- Create indexes for the new partition (including trigram indexes for partial word matching)
    EXECUTE format('
        DO $$ BEGIN
            IF NOT EXISTS (
                SELECT 1
                FROM pg_indexes
                WHERE indexname = %L
            ) THEN
                CREATE INDEX idx_transactions_hash_%I ON %I (hash);
            END IF;
        END $$;
    ', 'idx_transactions_hash_' || partition_name, partition_name, partition_name);

    EXECUTE format('
        DO $$ BEGIN
            IF NOT EXISTS (
                SELECT 1
                FROM pg_indexes
                WHERE indexname = %L
            ) THEN
                CREATE INDEX idx_transactions_from_address_%I ON %I (from_address);
            END IF;
        END $$;
    ', 'idx_transactions_from_address_' || partition_name, partition_name, partition_name);

    EXECUTE format('
        DO $$ BEGIN
            IF NOT EXISTS (
                SELECT 1
                FROM pg_indexes
                WHERE indexname = %L
            ) THEN
                CREATE INDEX idx_transactions_to_address_%I ON %I (to_address);
            END IF;
        END $$;
    ', 'idx_transactions_to_address_' || partition_name, partition_name, partition_name);

    -- Create trigram index for partial word matching
    EXECUTE format('
        DO $$ BEGIN
            IF NOT EXISTS (
                SELECT 1
                FROM pg_indexes
                WHERE indexname = %L
            ) THEN
                CREATE INDEX idx_transactions_from_address_trgm_%I ON %I USING gin (from_address gin_trgm_ops);
            END IF;
        END $$;
    ', 'idx_transactions_from_address_trgm_' || partition_name, partition_name, partition_name);

    EXECUTE format('
        DO $$ BEGIN
            IF NOT EXISTS (
                SELECT 1
                FROM pg_indexes
                WHERE indexname = %L
            ) THEN
                CREATE INDEX idx_transactions_to_address_trgm_%I ON %I USING gin (to_address gin_trgm_ops);
            END IF;
        END $$;
    ', 'idx_transactions_to_address_trgm_' || partition_name, partition_name, partition_name);

    -- Create a GIN index for the search_vector (full-text search)
    EXECUTE format('
        DO $$ BEGIN
            IF NOT EXISTS (
                SELECT 1
                FROM pg_indexes
                WHERE indexname = %L
            ) THEN
                CREATE INDEX idx_transactions_search_vector_%I ON %I USING GIN (search_vector);
            END IF;
        END $$;
    ', 'idx_transactions_search_vector_' || partition_name, partition_name, partition_name);

    -- Log the successful creation of the partition
    RAISE NOTICE 'Partition % created successfully for range [% - %]', partition_name, start_block, end_block - 1;

EXCEPTION
    WHEN OTHERS THEN
        -- Handle unexpected errors
        RAISE EXCEPTION 'Error creating partition %: %', partition_name, SQLERRM;
END;
$$;
