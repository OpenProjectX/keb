#!/usr/bin/env bash
set -euo pipefail

cd /workspace

gradle_args=(
  -p example
  --offline
  --no-daemon
  --no-configuration-cache
  --no-build-cache
  "-PkebVersion=${KEB_VERSION}"
)

# Dependencies and node_modules are part of the image. Force compilation, UI
# build, and browser tests, but never invoke a package installation at runtime.
gradle "${gradle_args[@]}" \
  :testing:test \
  --rerun-tasks \
  -x :ui:bunInstall

# The Allure runtime was installed while building the image. Gradle sees the
# just-completed tests as current and renders their results without a download.
gradle "${gradle_args[@]}" :testing:allureReport

echo "Allure report: /workspace/example/testing/build/reports/allure-report/allureReport/index.html"
