#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
STRUCTURE_FILE="$ROOT_DIR/COMPLETE_STRUCTURE.txt"

if [[ ! -f "$STRUCTURE_FILE" ]]; then
  echo "ERROR: Missing required file: $STRUCTURE_FILE"
  exit 1
fi

FAILURES=0

pass() {
  printf 'PASS  %s\n' "$1"
}

fail() {
  printf 'FAIL  %s\n' "$1"
  FAILURES=$((FAILURES + 1))
}

require_pattern() {
  local label="$1"
  local pattern="$2"

  if grep -Fq -- "$pattern" "$STRUCTURE_FILE"; then
    pass "$label"
  else
    fail "$label (missing: $pattern)"
  fi
}

echo "=== COMPLETE_STRUCTURE ADR Compliance Validation ==="

# Mandatory contract block
require_pattern "Contract: ADR structure contract section" "MANDATORY STRUCTURE CONTRACT (ADR-001, ADR-003, ADR-010, ADR-011, ADR-013, ADR-020)"
require_pattern "Contract: transactional outbox rule" "Event publishers MUST write through transactional outbox"
require_pattern "Contract: inbox/idempotency rule" "adapter/output/messaging/inbox/EventInboxProcessor.kt"
require_pattern "Contract: REST validation rule" "adapter/input/rest/validation/"
require_pattern "Contract: Kafka restriction rule" "Kafka publishing from application handlers is prohibited; only outbox relay publishes to broker"

# finance-gl
require_pattern "GL: hexagonal application module" "gl-application/"
require_pattern "GL: event publisher port" "EventPublisherPort.kt"
require_pattern "GL: REST validation bean param" "GLBeanParam.kt"
require_pattern "GL: outbox event publisher" "GLOutboxEventPublisher.kt"
require_pattern "GL: kafka outbox publisher" "GLKafkaOutboxMessagePublisher.kt"
require_pattern "GL: outbox scheduler" "GLOutboxEventScheduler.kt"
require_pattern "GL: inbox consumer" "GLEventInboxConsumer.kt"
require_pattern "GL: outbox migration" "V7__create_event_outbox_table.sql"
require_pattern "GL: inbox migration" "V8__create_event_inbox_table.sql"

# finance-ar
require_pattern "AR: hexagonal application module" "ar-application/"
require_pattern "AR: REST validation bean param" "ARBeanParam.kt"
require_pattern "AR: outbox event publisher" "AROutboxEventPublisher.kt"
require_pattern "AR: kafka outbox publisher" "ARKafkaOutboxMessagePublisher.kt"
require_pattern "AR: outbox scheduler" "AROutboxEventScheduler.kt"
require_pattern "AR: inbox consumer" "AREventInboxConsumer.kt"
require_pattern "AR: outbox migration" "V4__create_event_outbox_table.sql"
require_pattern "AR: inbox migration" "V5__create_event_inbox_table.sql"

# finance-ap
require_pattern "AP: hexagonal application module" "ap-application/"
require_pattern "AP: REST validation bean param" "APBeanParam.kt"
require_pattern "AP: outbox event publisher" "APOutboxEventPublisher.kt"
require_pattern "AP: kafka outbox publisher" "APKafkaOutboxMessagePublisher.kt"
require_pattern "AP: outbox scheduler" "APOutboxEventScheduler.kt"
require_pattern "AP: inbox consumer" "APEventInboxConsumer.kt"
require_pattern "AP: outbox migration" "V5__create_event_outbox_table.sql"
require_pattern "AP: inbox migration" "V6__create_event_inbox_table.sql"

# finance-assets
require_pattern "Assets: hexagonal application module" "assets-application/"
require_pattern "Assets: REST validation bean param" "AssetsBeanParam.kt"
require_pattern "Assets: outbox event publisher" "FAOutboxEventPublisher.kt"
require_pattern "Assets: kafka outbox publisher" "FAKafkaOutboxMessagePublisher.kt"
require_pattern "Assets: outbox scheduler" "FAOutboxEventScheduler.kt"
require_pattern "Assets: inbox consumer" "FAEventInboxConsumer.kt"
require_pattern "Assets: outbox migration" "V6__create_event_outbox_table.sql"
require_pattern "Assets: inbox migration" "V7__create_event_inbox_table.sql"

# finance-tax
require_pattern "Tax: hexagonal application module" "tax-application/"
require_pattern "Tax: REST validation bean param" "TaxBeanParam.kt"
require_pattern "Tax: outbox event publisher" "TaxOutboxEventPublisher.kt"
require_pattern "Tax: kafka outbox publisher" "TaxKafkaOutboxMessagePublisher.kt"
require_pattern "Tax: outbox scheduler" "TaxOutboxEventScheduler.kt"
require_pattern "Tax: inbox consumer" "TaxEventInboxConsumer.kt"
require_pattern "Tax: outbox migration" "V6__create_event_outbox_table.sql"
require_pattern "Tax: inbox migration" "V7__create_event_inbox_table.sql"

# Database summary must include outbox/inbox where required
require_pattern "DB: AR includes outbox and inbox" "customer_account, invoice, invoice_line, payment, payment_allocation, aging_snapshot, event_outbox, event_inbox"
require_pattern "DB: Assets includes outbox and inbox" "fixed_asset, depreciation_schedule, depreciation_entry, asset_disposal, asset_transfer, maintenance_record, event_outbox, event_inbox"

if [[ "$FAILURES" -gt 0 ]]; then
  echo ""
  echo "Validation failed with $FAILURES issue(s)."
  exit 1
fi

echo ""
echo "Validation passed: COMPLETE_STRUCTURE ADR compliance checks are satisfied."
