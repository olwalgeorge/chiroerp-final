#!/usr/bin/env bash
# =============================================================================
# Log Gate Scanner — ADR-008 Compliance
# =============================================================================
# Scans build/test outputs for unexpected ERROR, Exception, or FATAL patterns.
# Used as a quality gate after build and test jobs.
#
# Usage:
#   scripts/ci/log-gate.sh [allowlist-file]
#
# Exit codes:
#   0 — No unexpected errors found
#   1 — Unexpected errors detected (gate failed)
#
# The allowlist file contains one regex pattern per line.
# Lines starting with # are comments. Blank lines are ignored.
# =============================================================================
set -euo pipefail

ALLOWLIST="${1:-scripts/ci/error-allowlist.txt}"
LOG_DIR="${2:-.}"
EXIT_CODE=0
TOTAL_MATCHES=0
FILTERED_MATCHES=0

echo "╔══════════════════════════════════════════════════════════════╗"
echo "║  Log Gate Scanner — ADR-008 Quality Gate                    ║"
echo "╚══════════════════════════════════════════════════════════════╝"
echo ""

# Build grep exclusion pattern from allowlist
EXCLUDE_PATTERN=""
if [[ -f "$ALLOWLIST" ]]; then
  while IFS= read -r line; do
    # Skip comments and blank lines
    [[ "$line" =~ ^#.*$ || -z "$line" ]] && continue
    if [[ -z "$EXCLUDE_PATTERN" ]]; then
      EXCLUDE_PATTERN="$line"
    else
      EXCLUDE_PATTERN="${EXCLUDE_PATTERN}|${line}"
    fi
  done < "$ALLOWLIST"
  echo "📋 Loaded allowlist from: $ALLOWLIST"
else
  echo "⚠️  No allowlist found at: $ALLOWLIST (all matches will be reported)"
fi

echo ""

# Scan patterns: ERROR, Exception, FATAL (case-sensitive for Java conventions)
SCAN_PATTERN='(ERROR|FATAL|Exception|OutOfMemoryError|StackOverflowError|ClassNotFoundException|NoSuchMethodError|LinkageError)'

# Find all log-like and build output files
LOG_FILES=$(find "$LOG_DIR" \
  -type f \
  \( -name '*.log' -o -name '*.txt' -o -name 'output-*.xml' \) \
  -not -path '*/.gradle/*' \
  -not -path '*/.git/*' \
  -not -path '*/node_modules/*' \
  -not -path '*/build/tmp/*' \
  2>/dev/null || true)

# Also scan recent Gradle build output if available
BUILD_OUTPUTS=$(find "$LOG_DIR" \
  -type f \
  -path '*/build/reports/*' \
  -name '*.html' \
  2>/dev/null || true)

if [[ -z "$LOG_FILES" && -z "$BUILD_OUTPUTS" ]]; then
  echo "ℹ️  No log files found in: $LOG_DIR"
  echo "✅ Log gate passed (no files to scan)"
  exit 0
fi

# Scan each file
scan_file() {
  local file="$1"
  local matches

  matches=$(grep -nE "$SCAN_PATTERN" "$file" 2>/dev/null || true)

  if [[ -n "$matches" ]]; then
    # Apply allowlist filter
    if [[ -n "$EXCLUDE_PATTERN" ]]; then
      filtered=$(echo "$matches" | grep -vE "$EXCLUDE_PATTERN" 2>/dev/null || true)
    else
      filtered="$matches"
    fi

    if [[ -n "$filtered" ]]; then
      local count
      count=$(echo "$filtered" | wc -l | tr -d ' ')
      TOTAL_MATCHES=$((TOTAL_MATCHES + count))
      echo "❌ $file ($count unexpected error(s)):"
      echo "$filtered" | head -10
      if [[ $count -gt 10 ]]; then
        echo "   ... and $((count - 10)) more"
      fi
      echo ""
      EXIT_CODE=1
    else
      local total_count
      total_count=$(echo "$matches" | wc -l | tr -d ' ')
      FILTERED_MATCHES=$((FILTERED_MATCHES + total_count))
    fi
  fi
}

for file in $LOG_FILES $BUILD_OUTPUTS; do
  scan_file "$file"
done

echo "════════════════════════════════════════════════════════════════"
echo "📊 Log Gate Summary"
echo "────────────────────────────────────────────────────────────────"
echo "  Unexpected errors:  $TOTAL_MATCHES"
echo "  Allowlisted (OK):   $FILTERED_MATCHES"
echo "════════════════════════════════════════════════════════════════"

if [[ $EXIT_CODE -eq 0 ]]; then
  echo "✅ Log gate PASSED — no unexpected errors detected"
else
  echo "❌ Log gate FAILED — $TOTAL_MATCHES unexpected error(s) found"
  echo ""
  echo "To allowlist known patterns, add them to: $ALLOWLIST"
fi

exit $EXIT_CODE
