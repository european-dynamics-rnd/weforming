# Weforming Connector Test Demo

This folder provides a local integration test environment for the Weforming connector stack using Docker Compose.

It starts:

- 1 provider connector
- 1 consumer connector
- 1 Identity Proxy + MongoDB
- 1 Hyperledger Besu node
- 2 PostgreSQL databases (provider and consumer)
- 1 issuer bootstrap script

It also includes a Postman collection to test a provider/consumer negotiation flow end to end.

## Contents

- `docker-compose.yml`: full local stack
- `TestWeformingConnectors.postman_collection.json`: API requests for provider/consumer tests
- `credentials/`: VC files mounted into each connector
- `test-keys/`: test key pairs for issuer, provider, and consumer
- `test-wallets.txt`: test wallet addresses/private keys (test only)

## Prerequisites

- Docker + Docker Compose
- Access to the project source folder at `../source` (the connector image is built from there)
- Postman (for manual API tests)

## Start The Stack

From this folder:

```bash
docker compose up --build -d
```

Check status:

```bash
docker compose ps
```

Follow logs (optional):

```bash
docker compose logs -f
```

## Stop The Stack

Stop and remove containers/network:

```bash
docker compose down
```

Stop and also remove persisted volumes/data:

```bash
docker compose down -v
```

## Exposed Endpoints

### Infrastructure

- Identity Proxy: `http://localhost:8080`
- Besu RPC HTTP: `http://localhost:8545`

### Provider Connector

- Management API: `http://localhost:8084`
- Control API: `http://localhost:8083`
- Protocol API: `http://localhost:8085`

### Consumer Connector

- Management API: `http://localhost:8094`
- Control API: `http://localhost:8093`
- Protocol API: `http://localhost:8095`

## What Happens On Startup

1. Besu and Identity Proxy start.
2. The issuer script configures the trusted issuer DID in Identity Proxy.
3. Provider and consumer connectors start with their test keys/VCs.
4. Each connector uses its own PostgreSQL database.

## Test Identities (Non-Production)

This setup uses deterministic test wallets and keys.

- Trusted Issuer DID: `did:indy:besu:wf:0xed088cb405441491c0d47dd9d7935671bd12cb24`
- Provider DID: `did:indy:besu:wf:0x4ce8b7105877a67a5abdef241cf10cb897cc6c7f`
- Consumer DID: `did:indy:besu:wf:0x44f5e09b2c0889be51f562a1c25c4ae046994bec`

See `test-wallets.txt` for the full test wallet details.

## Postman Collection

Import:

- `TestWeformingConnectors.postman_collection.json`

### Suggested Request Order

1. `[Provider] Create Asset`
2. `[Provider] Create Policy`
3. `[Provider] Create Contract Definition`
4. `[Consumer] Request Provider Catalog`
5. Copy the offer id returned in the catalog into `TEST_OFFER_ID`
6. `[Consumer] Start negotiation with provider`
7. `[Consumer] Get Negotiations`
8. `[Provider] Get Negotiations`

If `counterPartyAddress` requests fail in your local version, test `PROVIDER_DSP_URL` with a path suffix (for example `/api/dsp`) depending on your connector build.

## Troubleshooting

- Build fails for `weforming-connector`:
	- Verify `../source` exists and builds in your machine.
- APIs return errors right after startup:
	- Wait a little longer and inspect logs: `docker compose logs -f weforming_test_provider weforming_test_consumer weforming_test_id_proxy`
- Stale state from previous runs:
	- Run `docker compose down -v` and start again.

## Notes

- This is a local test environment only.
- Do not use test keys/wallets in production.
