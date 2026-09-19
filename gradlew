#!/bin/sh
set -e
if command -v gradle >/dev/null 2>&1; then
  exec gradle "$@"
fi
echo "Gradle executable not found. Use Android Studio or install Gradle, or run the GitHub Actions workflow." >&2
exit 1
