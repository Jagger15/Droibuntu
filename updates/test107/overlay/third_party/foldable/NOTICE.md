# Foldable runtime dependencies

`dependencies.json` records exact official download URLs, versions and SHA-256
hashes for all added dependencies. AndroidX, Kotlin, Kotlin coroutines, JetBrains
annotations and Guava listenablefuture use Apache License 2.0. License notices
embedded in the original AAR/JAR files are retained. Existing collection/arch
support libraries remain supplied by `third_party/wear`.

Phone builds replace the older core, annotation, lifecycle runtime/common and
versionedparcelable runtime entries with the versions listed here. The build
verifies each archive before using it. Optional OEM WindowManager extension and
sidecar libraries are declared in the application manifest; they are not bundled.
