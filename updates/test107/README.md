# Test107: foldable devices and controller navigation

This repository hosts Droibuntu's download site; the full Android source has
historically been a release attachment. This directory keeps the Test107 changes
reviewable without checking generated APKs, signing keys or dependency binaries
into the site repository.

See [the changes and controller mapping](overlay/CHANGES-Test107.md).
The `overlay` contains every changed/new source file relative to Test106, plus
pinned official dependency metadata. `prepare_source.py` verifies the public
Test106 archive and the new dependencies before assembling the complete source.

```sh
python3 updates/test107/prepare_source.py build/test107
cd build/test107
bash tools/test-fold-controller.sh
python3 -m unittest discover -s tests -p 'test_*.py' -q
export ANDROID_JAR="$ANDROID_HOME/platforms/android-34/android.jar"
export BUILD_TOOLS="$ANDROID_HOME/build-tools/35.0.0"
export ECJ_JAR=/absolute/path/to/ecj-3.37.0.jar
python3 tools/build-with-watch.py phone unsigned
```

Use `--baseline /path/to/Droibuntu-Test106-Separate-source.zip` to reuse a local
baseline. An existing destination is rejected to protect local work.
For an update-compatible signed build, set `DROIBUNTU_KEYSTORE` to your private
Test106 recovery keystore and omit `unsigned`. Never commit the keystore.

Validation: Android resource/Java/DEX build, APK signature/alignment checks,
525 Python tests (31 skipped), and 4,018 Java policy assertions. Physical-device
acceptance testing remains pending. See the changes file for the test checklist.
