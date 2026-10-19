# First-time setup

Prepared target: public repository `Jagger15/Droibuntu`. The repository is public; publish the release and enable Pages to make downloads available.

1. Create the repository, then upload the contents of this folder to its main branch.
2. In Settings → Pages, use Deploy from a branch, main, /docs, then Save.
3. Create tag `test101` and release `Droibuntu Test101`. Copy releases/Test101.md.
4. Attach Droibuntu-Test101.apk, Droibuntu-Test101-public-source.zip and SHA256SUMS.
5. Publish as a regular latest release; the current updater excludes prereleases.
6. Check that GitHub reports an APK SHA-256 digest and that exactly one APK asset
   is attached. Configure the app's repository field using the README instructions.

Planned download page: https://jagger15.github.io/Droibuntu/
Planned release hub: https://github.com/Jagger15/Droibuntu/releases

The page follows GitHub's latest release API so it does not need editing for
each APK. It falls back to the releases page if the API is unavailable.

Use only the public-source ZIP for uploads. The private development source ZIP
contains the signing key needed for existing installations and must stay private.
Keep a private backup of that key. No GitHub Actions signing secrets or background
update checks have been configured by this setup. Android confirms installation.

Official setup references:
- https://docs.github.com/en/pages/getting-started-with-github-pages/configuring-a-publishing-source-for-your-github-pages-site
- https://docs.github.com/en/rest/releases/releases
