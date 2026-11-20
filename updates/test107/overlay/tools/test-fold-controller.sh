#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
mkdir -p build/fold-controller-tests
if command -v javac >/dev/null 2>&1; then compiler=(javac); else compiler=(java -jar "${ECJ_JAR:?Set ECJ_JAR or install a JDK}" -8 -proc:none); fi
"${compiler[@]}" -d build/fold-controller-tests src/org/ubuntuquest/pocket/FoldLayoutPolicy.java src/org/ubuntuquest/pocket/ControllerPolicy.java tests/FoldControllerTest.java
java -cp build/fold-controller-tests org.ubuntuquest.pocket.FoldControllerTest
