# Notes

## Running it

```
make test    # all tests, Docker only
make run     # http://localhost:8090, develop profile
```

With Java 17 and Maven: `mvn test` and `mvn spring-boot:run`. CI runs `mvn test` on every push.

The develop profile loads three EUR wallets: `00000000-0000-7000-8000-000000000001` (10.00), `…0002` (0.00) and `…0003` (100.00).

```
curl localhost:8090/wallets/00000000-0000-7000-8000-000000000001

curl -X POST localhost:8090/wallets/00000000-0000-7000-8000-000000000001/top-ups \
  -H 'Content-Type: application/json' \
  -d '{"amount": {"amount": 15.00, "currency": "EUR"}, "cardNumber": "4242 4242 4242 4242"}'
```

## Disclaimer

The provided sandbox simulator answers 404 to every charge, so under `make run` a top-up ends `FAILED` (502). I did not treat it as blocking: the tests mock the provider on purpose. WireMock reproduces the simulator's contract (a charge, a 422 decline, errors and timeouts) in the end-to-end tests.

## API

- `GET /wallets/{id}`: 200 with the balance, 404 for an unknown wallet.
- `POST /wallets/{id}/top-ups`: answers the top-up (`id`, `status`, `amount`), with an HTTP status that depends on the provider:

| Provider answer | Top-up | HTTP |
|---|---|---|
| 2xx with a payment id | `COMPLETED` | 201 |
| 422 | `DECLINED` | 422 |
| Other 4xx, or connection refused | `FAILED` | 502 |
| 5xx, 2xx without an id, or a timeout | `UNCONFIRMED` | 202 |

The rule: when the charge may have happened, the wallet is never credited and the top-up is never reported as failed.

Invalid requests (400) and unknown wallets (404) are Problem Details (RFC 9457).

## Design

- Layers: `domain` (model, rules, repository and payment ports), `application` (the top-up use case and the read models; the only layer that opens transactions), `infrastructure` (HTTP, JPA, payment provider).
- `Wallet` is the only aggregate. Each credit adds a `WalletMovement` that records its cause; a unique constraint on `(type, cause_id)` makes crediting the same top-up twice impossible.
- `TopUp` records the process across the provider and the wallet: `PENDING → CHARGED → COMPLETED`, or `PENDING → DECLINED | FAILED | UNCONFIRMED`.
- The charge is a slow HTTP call, so it runs outside any transaction, between three short ones:
  1. Check the wallet and the amount, and save the top-up as `PENDING`.
  2. Charge the card.
  3. Save the outcome. A charge is saved as `CHARGED`, with its payment reference, before the wallet is touched.
  4. Credit the wallet and mark the top-up `COMPLETED`.
- Invalid input is rejected before the charge. The HTTP layer rejects a missing amount, currency or card, and an unknown currency. Step 1 rejects an unknown wallet, a non-positive amount, another currency and more decimals than the currency allows.
- The credit takes pessimistic locks on the top-up and then the wallet (`SELECT … FOR UPDATE`), always in that order, so concurrent credits on the same wallet are applied one after another and none is lost once the card has been charged. The locks live in the database, so they hold across instances. `ConcurrentTopUpsTest` runs 20 top-ups on one wallet at once; without the lock it loses credits.
- `Money` carries its currency and 4 decimals, and rejects more instead of rounding them.
- Only the masked card number (`**** 4242`) is stored, and `CardNumber.toString()` masks it.
- Reads use small projections (`WalletView`, `TopUpView`) over the same tables, separate from the aggregate used for writes.
- The provided Stripe code is wrapped by the payment adapter. It gets connect and read timeouts, and its error handler now tells 422, other 4xx and 5xx apart (before, only 422 threw). Its `refund()` is unused and untouched.

## Tests

The Cucumber features in `src/test/resources/features` are the specification: they run the whole service over HTTP, with WireMock as the provider. Below them are domain unit tests, `@WebMvcTest` for the HTTP contract, `@DataJpaTest` for the repositories and lock modes, `MockRestServiceServer` for the payment adapter, Spring integration tests for the transaction boundaries and a failed credit, and the concurrency test.

## Trade-offs

- H2 with `ddl-auto`, as provided. Production would need PostgreSQL and schema migrations.
- JPA annotations on the domain model, to avoid a second persistence model in a proof of concept.

## Not implemented

- Spend and refund.
- Reading the transaction history: every credit is already recorded as a `WalletMovement`, but there is no endpoint to list them.
- Recovery: a top-up left `PENDING`, `CHARGED` or `UNCONFIRMED` stays there. There is no reconciler and no `GET` for a top-up, so a 202 cannot be followed up.
- A charged top-up whose credit fails stays `CHARGED`, but the request answers Spring's default 500 without its id.
- An idempotency key: a retried request charges the card again.
- Card format validation and a maximum top-up amount.
- Publishing events to other services.
- Wallet creation: wallets are assumed to come from another service.

The first commit is the provided skeleton, unchanged. The behaviour was specified first as Cucumber features and built outside-in.
