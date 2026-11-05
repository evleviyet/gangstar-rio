#!/data/data/com.termux/files/usr/bin/bash
# Restore the data captured before this migration.
set -euo pipefail
here="$(cd "$(dirname "$0")" && pwd)"
echo "Restoring ~/.claude, ~/.claude.json, ~/.bashrc from $here ..."
if [ -f "$here/dot-claude.tgz" ]; then
  # Verify the archive is readable BEFORE removing the live directory, so a
  # corrupt backup can never leave you with neither the old nor the backup.
  tar tzf "$here/dot-claude.tgz" >/dev/null 2>&1 || { echo "backup archive is unreadable; aborting restore to avoid data loss."; exit 1; }
  rm -rf "$HOME/.claude"
  tar xzf "$here/dot-claude.tgz" -C "$HOME"
fi
if [ -f "$here/.claude.json" ]; then cp -a "$here/.claude.json" "$HOME/.claude.json"; fi
if [ -f "$here/.bashrc" ]; then cp -a "$here/.bashrc" "$HOME/.bashrc"; fi
echo "Data restored. Your sessions and login are back regardless of which binary you run."
echo "To reinstall the old pinned binary (optional):"
echo "  npm install -g @anthropic-ai/claude-code@2.1.112"
