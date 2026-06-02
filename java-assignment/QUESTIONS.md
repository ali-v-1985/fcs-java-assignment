# Questions

Here we have 3 questions related to the code base for you to answer. It is not about right or wrong, but more about what's the reasoning behind your decisions.

1. In this code base, we have some different implementation strategies when it comes to database access layer and manipulation. If you would maintain this code base, would you refactor any of those? Why?

**Answer:**
```txt
Yes, I would refactor toward one consistent persistence style per bounded area.

The current code intentionally shows several approaches:

- Store uses the Panache active-record style directly on the entity, with business operations moved into StoreService.(changed by me)
- Product uses a PanacheRepository, but transactional and validation logic still lives mostly in the REST resource.
- Warehouse uses a clearer ports/adapters style: domain use cases depend on WarehouseStore and LocationResolver, while the database adapter maps DbWarehouse to a domain Warehouse model.

For a small assignment this is acceptable, but if I had to maintain the system I would reduce the variation. My preferred direction would be closer to the Warehouse style: resources should be thin HTTP adapters, services/use cases should contain business rules and transaction boundaries, and repositories should own persistence queries. That separation makes validation easier to test, keeps REST concerns out of business logic, and gives more room to change storage details without rewriting endpoint handlers.

I would not necessarily introduce a heavy architecture everywhere immediately. I would start by moving Product CRUD business logic out of ProductResource into a ProductService, avoid direct Store.findById / Store.listAll calls from resources, and keep repository methods explicit for queries that encode business concepts. I would also keep database entities separate from domain models where the business rules are non-trivial, as in Warehouse replacement/archive logic.

The main reason is maintainability rather than purity. Mixed database access styles increase cognitive load and make it harder to know where transactions, validations and side effects belong. A consistent service/repository/use-case boundary would make future changes safer, especially around integrations and cost-control rules.
```
----
2. When it comes to API spec and endpoints handlers, we have an Open API yaml file for the `Warehouse` API from which we generate code, but for the other endpoints - `Product` and `Store` - we just coded directly everything. What would be your thoughts about what are the pros and cons of each approach and what would be your choice?

**Answer:**
```txt
Both approaches are valid, but they optimize for different things.

The OpenAPI-first approach used for Warehouse is stronger when the API is part of an external contract. It gives consumers a stable specification, allows generated clients/server interfaces, documents request/response shapes consistently, and helps catch accidental endpoint drift. That is valuable for Warehouse because the replace/archive behavior is domain-specific and likely to be consumed by other systems.

The downside is extra process and maintenance. The YAML must be kept accurate, generated code can be awkward to navigate, and small changes require updating the spec and regenerating code. If the generated layer is too tightly coupled to implementation details, it can also make simple changes feel heavier than they need to be.

The code-first approach used by Product and Store is faster for simple CRUD endpoints. It has fewer moving parts, is easy to read inside the Java code, and works well for internal or early-stage APIs where the contract is still moving. The downside is that documentation and implementation can diverge unless OpenAPI is generated from annotations and actively reviewed. It also makes client integration less predictable because the contract is implicit in the code and tests.

My choice would depend on the endpoint's role. For public, cross-team, integration-heavy or long-lived APIs, I would choose OpenAPI-first. For simple internal CRUD endpoints, code-first is acceptable if the project still produces and validates an OpenAPI document from the implementation.

For this project specifically, I would lean toward standardizing on OpenAPI-first for all externally consumed APIs once Product and Store become more than simple CRUD. In the meantime I would keep generated interfaces at the edge only, with implementation delegated to services/use cases, so the spec does not leak into the domain model.
```
----
3. Given the need to balance thorough testing with time and resource constraints, how would you prioritize and implement tests for this project? Which types of tests would you focus on, and how would you ensure test coverage remains effective over time?

**Answer:**
```txt
I would prioritize tests by business risk and by how expensive a defect would be to diagnose later.

The first priority would be business-rule tests around Warehouse creation, replacement and archive behavior. These rules have the highest domain complexity: unique business unit codes, valid locations, location capacity limits, stock/capacity constraints, replacement stock matching, and preserving history by archiving the previous warehouse. I would keep fast unit tests around WarehouseValidator and the use cases, then add a smaller number of endpoint tests to prove HTTP status codes and serialization.

The second priority would be transaction and integration side effects. Store synchronization with the legacy gateway is a good example: the important behavior is not only that the gateway is called, but that it is called only after a successful commit and not called when validation fails. I would keep tests for those exact guarantees.

The third priority would be repository/query behavior that enforces constraints, especially FulfilmentAssignment limits. These are easy to get subtly wrong because they rely on distinct counts across product, store and warehouse relationships. Endpoint-level tests are useful here because they exercise persistence and the real query behavior.

I would not try to maximize line coverage. Instead I would define a compact regression suite around:

- Domain/use-case unit tests for rules and edge cases.
- REST endpoint tests for contract, status codes and error responses.
- Repository/integration tests for database queries and transaction behavior.
- A few negative-path tests for validation failures and not-found cases.

To keep coverage effective over time, I would make the tests map to business rules rather than implementation details, keep test data explicit and readable, and require every bug fix or new business rule to add a regression test. I would also run the suite in CI and periodically review coverage reports only as a signal for missing important scenarios, not as the primary quality goal.
```
