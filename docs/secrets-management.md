# Secrets Management

The GitHub Actions workflows rely on several secrets to sign builds and access AI services. This document explains how to create and upload those secrets securely.

## Required Secrets

| Secret | Description |
| --- | --- |
| `ANDROID_KEYSTORE_BASE64` | Base64 representation of the release keystore generated with the `keytool` command below. |
| `ANDROID_KEYSTORE_PASSWORD` | Password defined when creating the keystore. |
| `ANDROID_KEY_ALIAS` | Alias used for the signing key (default `release`). |
| `ANDROID_KEY_PASSWORD` | Password for the key corresponding to the alias. |
| `OPENAI_API_KEY` | Optional: API key for OpenAI integrations used by the app or automation scripts. |

## Creating a Self-Signed Keystore

Use `keytool` to create a 10-year self-signed certificate and write the keystore inside `build/signing` (which stays ignored by Git):

```bash
mkdir -p build/signing
keytool -genkeypair \
  -alias release \
  -keyalg RSA \
  -keysize 4096 \
  -validity 3650 \
  -storetype PKCS12 \
  -keystore build/signing/release.keystore
```

When prompted, record the keystore password, key password, and distinguished name values so you can load them into GitHub later.

## Uploading to GitHub

You can store secrets either as **repository secrets** or through a **GitHub App** that injects environment variables at workflow runtime.

### Option A — Repository secrets

1. Navigate to **Settings → Secrets and variables → Actions** in your repository.
2. Click **New repository secret** for each required secret.
3. For the keystore, encode the file:
   ```bash
   base64 build/signing/release.keystore | pbcopy
   ```
   Paste the Base64 string into the `ANDROID_KEYSTORE_BASE64` secret.
4. Store the passwords and alias in their respective secrets.

### Option B — GitHub App-managed secrets

1. Install or create a GitHub App with permissions to read organization secrets and write workflow variables.
2. Upload the signing credentials and passwords to the App's vault, using the same secret names listed above.
3. Configure the App to inject those secrets into workflows that require signing (e.g., `Android Release`) as environment variables.
4. Audit the App's installations regularly and rotate credentials within the App vault when keystores change.

## Local Development

- Keep the generated keystore out of version control. The command above writes it inside the ignored `build/signing/` directory.
- Team members should generate their own keystores or retrieve them from a secure vault rather than committing sensitive files.

## Rotating Secrets

- Regenerate the keystore and update the GitHub secrets whenever you suspect exposure.
- Update organisation secrets if multiple repositories share the same signing credentials.
- Document rotations in release notes or internal changelogs for traceability.
