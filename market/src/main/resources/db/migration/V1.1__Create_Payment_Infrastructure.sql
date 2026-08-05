-- V1.1__Create_Payment_Infrastructure.sql
-- Payment infrastructure migration script
-- Created: 2026-08-05
-- This migration adds PRODUCTION-READY payment tables with industry-level constraints,
-- indexing, and security patterns for Safaricom Daraja M-Pesa integration.

-- ============================================================================
-- 1. CREATE PAYMENTS TABLE
-- ============================================================================
-- Core payment entity with optimistic locking, idempotency, and audit trail.
-- UNIQUE(idempotency_key) prevents duplicate charges
-- Indexes optimize queries: status lookups, date range queries, timeout detection
CREATE TABLE IF NOT EXISTS payments (
    id VARCHAR(36) PRIMARY KEY NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    last_modified_at TIMESTAMP,
    created_by VARCHAR(255),
    last_modified_by VARCHAR(255),
    deleted BIT DEFAULT 0,

    -- Foreign keys
    order_id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36) NOT NULL,

    -- Idempotency & deduplication
    idempotency_key VARCHAR(255) NOT NULL UNIQUE,

    -- Payment details
    amount DECIMAL(19, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'KES',
    payment_method VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,

    -- External reference (M-Pesa, Stripe, etc)
    external_transaction_ref VARCHAR(255),
    phone_number VARCHAR(20),
    merchant_request_id VARCHAR(255),
    checkout_request_id VARCHAR(255),

    -- Gateway response tracking
    gateway_response LONGTEXT,
    error_message LONGTEXT,

    -- Timestamps for lifecycle management
    initiated_at TIMESTAMP NOT NULL,
    status_updated_at TIMESTAMP,
    completed_at TIMESTAMP,

    -- Retry strategy
    retry_count INT DEFAULT 0,
    next_retry_at TIMESTAMP,

    -- Reconciliation
    reconciled BIT DEFAULT FALSE,
    reconciled_at TIMESTAMP,

    -- Constraints and indexes
    CONSTRAINT fk_payment_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE RESTRICT,
    CONSTRAINT fk_payment_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT chk_payment_amount CHECK (amount > 0),
    CONSTRAINT chk_payment_status CHECK (status IN ('INITIATED', 'PENDING', 'COMPLETED', 'FAILED', 'REFUNDED', 'TIMEOUT', 'CANCELLED')),
    CONSTRAINT chk_payment_method CHECK (payment_method IN ('M_PESA', 'CARD', 'COD', 'WALLET')),

    INDEX idx_order_id (order_id),
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_idempotency_key (idempotency_key),
    INDEX idx_external_ref (external_transaction_ref),
    INDEX idx_created_at (created_at),
    INDEX idx_initiated_at (initiated_at),
    INDEX idx_next_retry_at (next_retry_at),
    INDEX idx_status_created (status, created_at),
    INDEX idx_user_created (user_id, created_at)
);

-- ============================================================================
-- 2. CREATE PAYMENT TRANSACTIONS TABLE
-- ============================================================================
-- Webhook deduplication and idempotency tracking
-- UNIQUE(external_transaction_id) prevents duplicate webhook processing
CREATE TABLE IF NOT EXISTS payment_transactions (
    id VARCHAR(36) PRIMARY KEY NOT NULL,
    created_at TIMESTAMP NOT NULL,
    last_modified_at TIMESTAMP,
    created_by VARCHAR(255),
    last_modified_by VARCHAR(255),
    deleted BIT DEFAULT 0,

    -- Foreign key
    payment_id VARCHAR(36) NOT NULL,

    -- External transaction ID (webhook deduplication)
    external_transaction_id VARCHAR(255) NOT NULL UNIQUE,

    -- Transaction metadata
    transaction_type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    payload LONGTEXT NOT NULL,

    -- Error tracking
    error_message LONGTEXT,
    response_code VARCHAR(10),

    -- Webhook security
    source_ip_address VARCHAR(45),
    webhook_signature VARCHAR(512),
    signature_verified BIT DEFAULT FALSE,

    -- Retry tracking
    retry_count INT DEFAULT 0,

    -- Constraints and indexes
    CONSTRAINT fk_transaction_payment FOREIGN KEY (payment_id) REFERENCES payments(id) ON DELETE CASCADE,
    CONSTRAINT chk_transaction_type CHECK (transaction_type IN ('INITIATED', 'CALLBACK', 'WEBHOOK', 'WEBHOOK_RETRY')),
    CONSTRAINT chk_transaction_status CHECK (status IN ('RECEIVED', 'PROCESSED', 'FAILED', 'DUPLICATE')),

    INDEX idx_payment_id (payment_id),
    INDEX idx_external_txn_id (external_transaction_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at),
    INDEX idx_payment_created (payment_id, created_at)
);

-- ============================================================================
-- 3. CREATE PAYMENT WEBHOOK LOGS TABLE
-- ============================================================================
-- Immutable audit trail of all webhook activity (PCI DSS compliance requirement)
CREATE TABLE IF NOT EXISTS payment_webhook_logs (
    id VARCHAR(36) PRIMARY KEY NOT NULL,
    created_at TIMESTAMP NOT NULL,
    last_modified_at TIMESTAMP,
    created_by VARCHAR(255),
    last_modified_by VARCHAR(255),
    deleted BIT DEFAULT 0,

    -- Foreign key
    payment_id VARCHAR(36),

    -- Webhook identification
    webhook_id VARCHAR(255),

    -- Request tracking
    request_body LONGTEXT NOT NULL,
    request_headers LONGTEXT,
    http_method VARCHAR(10),
    endpoint VARCHAR(500),
    source_ip VARCHAR(45),

    -- Processing status
    status VARCHAR(50) NOT NULL,
    signature_valid BIT DEFAULT FALSE,
    processing_result LONGTEXT,
    response_code VARCHAR(10),

    -- Performance metrics
    processing_time_ms BIGINT,

    -- Success/failure tracking
    processed BIT DEFAULT FALSE,
    retry_count INT DEFAULT 0,
    next_retry_at TIMESTAMP,
    processed_at TIMESTAMP,

    -- Constraints and indexes
    CONSTRAINT fk_webhook_payment FOREIGN KEY (payment_id) REFERENCES payments(id) ON DELETE SET NULL,
    CONSTRAINT chk_webhook_status CHECK (status IN ('RECEIVED', 'VALIDATED', 'PROCESSED', 'FAILED', 'DISCARDED')),
    CONSTRAINT chk_http_method CHECK (http_method IN ('GET', 'POST', 'PUT', 'DELETE', 'PATCH')),

    INDEX idx_payment_id (payment_id),
    INDEX idx_webhook_id (webhook_id),
    INDEX idx_status (status),
    INDEX idx_processed (processed),
    INDEX idx_created_at (created_at),
    INDEX idx_source_ip (source_ip),
    INDEX idx_status_created (status, created_at),
    INDEX idx_next_retry (next_retry_at)
);

-- ============================================================================
-- 4. CREATE PAYMENT RETRY POLICIES TABLE
-- ============================================================================
-- Configurable exponential backoff strategies per payment method
CREATE TABLE IF NOT EXISTS payment_retry_policies (
    id VARCHAR(36) PRIMARY KEY NOT NULL,
    created_at TIMESTAMP NOT NULL,
    last_modified_at TIMESTAMP,
    created_by VARCHAR(255),
    last_modified_by VARCHAR(255),
    deleted BIT DEFAULT 0,

    -- Payment method this policy applies to (NULL = applies to all)
    payment_method VARCHAR(50),

    -- Policy configuration
    name VARCHAR(255) NOT NULL,
    description LONGTEXT,

    -- Exponential backoff parameters
    base_delay_seconds INT NOT NULL DEFAULT 1,
    backoff_multiplier INT NOT NULL DEFAULT 2,
    max_retries INT NOT NULL DEFAULT 5,
    max_retry_duration_seconds INT NOT NULL DEFAULT 300,

    -- Status
    active BIT DEFAULT TRUE,

    -- Constraints and indexes
    CONSTRAINT chk_base_delay CHECK (base_delay_seconds > 0),
    CONSTRAINT chk_multiplier CHECK (backoff_multiplier > 0),
    CONSTRAINT chk_max_retries CHECK (max_retries > 0),
    CONSTRAINT chk_duration CHECK (max_retry_duration_seconds > 0),
    CONSTRAINT chk_method CHECK (payment_method IN ('M_PESA', 'CARD', 'COD', 'WALLET', NULL)),

    INDEX idx_payment_method (payment_method),
    INDEX idx_active (active),
    UNIQUE INDEX uk_method_active (payment_method, active)
);

-- ============================================================================
-- 5. INSERT DEFAULT RETRY POLICIES
-- ============================================================================
INSERT INTO payment_retry_policies (
    id, payment_method, name, description,
    base_delay_seconds, backoff_multiplier, max_retries, max_retry_duration_seconds, active
) VALUES
(
    UUID(), 'M_PESA', 'Standard M-Pesa Retry',
    'Exponential backoff for M-Pesa STK push failures: 1s, 2s, 4s, 8s, 16s',
    1, 2, 5, 300, TRUE
),
(
    UUID(), 'CARD', 'Aggressive Card Retry',
    'Immediate retry for card payments (often transient failures)',
    1, 1, 3, 60, TRUE
),
(
    UUID(), NULL, 'Default Retry Policy',
    'Default retry strategy for all payment methods',
    2, 2, 4, 300, TRUE
)
ON DUPLICATE KEY UPDATE active=active;

-- ============================================================================
-- 6. INDEXES FOR PERFORMANCE
-- ============================================================================
-- Composite indexes for common query patterns
CREATE INDEX IF NOT EXISTS idx_payment_status_initiated ON payments(status, initiated_at);
CREATE INDEX IF NOT EXISTS idx_payment_user_status ON payments(user_id, status);
CREATE INDEX IF NOT EXISTS idx_webhook_payment_status ON payment_webhook_logs(payment_id, status);
CREATE INDEX IF NOT EXISTS idx_transaction_payment_type ON payment_transactions(payment_id, transaction_type);

-- ============================================================================
-- 7. VIEWS FOR REPORTING
-- ============================================================================
-- Pending payments (awaiting action or timeout)
CREATE OR REPLACE VIEW v_pending_payments AS
SELECT 
    p.id,
    p.order_id,
    p.user_id,
    p.amount,
    p.payment_method,
    p.initiated_at,
    TIMEDIFF(NOW(), p.initiated_at) as elapsed_time,
    CASE 
        WHEN TIMEDIFF(NOW(), p.initiated_at) > '00:30:00' THEN 'TIMED_OUT'
        ELSE 'PENDING'
    END as current_status
FROM payments p
WHERE p.status = 'PENDING' AND p.deleted = FALSE;

-- Payments ready for retry
CREATE OR REPLACE VIEW v_payments_ready_for_retry AS
SELECT 
    p.id,
    p.order_id,
    p.amount,
    p.payment_method,
    p.retry_count,
    p.next_retry_at,
    TIMEDIFF(p.next_retry_at, NOW()) as time_until_retry
FROM payments p
WHERE p.status IN ('INITIATED', 'PENDING', 'FAILED')
  AND p.next_retry_at IS NOT NULL
  AND p.next_retry_at <= NOW()
  AND p.deleted = FALSE
ORDER BY p.next_retry_at ASC;

-- Failed webhooks
CREATE OR REPLACE VIEW v_failed_webhooks AS
SELECT 
    wl.id,
    wl.payment_id,
    wl.webhook_id,
    wl.created_at,
    wl.status,
    wl.retry_count,
    wl.next_retry_at,
    TIMEDIFF(wl.next_retry_at, NOW()) as time_until_retry
FROM payment_webhook_logs wl
WHERE wl.status = 'FAILED'
  AND wl.deleted = FALSE
ORDER BY wl.created_at ASC;

-- Payment reconciliation view
CREATE OR REPLACE VIEW v_unreconciled_payments AS
SELECT 
    p.id,
    p.order_id,
    p.user_id,
    p.amount,
    p.currency,
    p.payment_method,
    p.status,
    p.external_transaction_ref,
    p.completed_at,
    p.reconciled
FROM payments p
WHERE p.reconciled = FALSE
  AND p.status = 'COMPLETED'
  AND p.deleted = FALSE
ORDER BY p.completed_at ASC;
