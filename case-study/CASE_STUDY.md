# Case Study Scenarios to discuss

## Scenario 1: Cost Allocation and Tracking
**Situation**: The company needs to track and allocate costs accurately across different Warehouses and Stores. The costs include labor, inventory, transportation, and overhead expenses.

**Task**: Discuss the challenges in accurately tracking and allocating costs in a fulfillment environment. Think about what are important considerations for this, what are previous experiences that you have you could related to this problem and elaborate some questions and considerations

**Questions you may have and considerations:**
Accurate cost allocation is difficult because fulfillment costs are often shared across entities and time periods. Labor can support multiple Warehouses or Stores, transportation can serve several destinations in one route, inventory carrying cost changes over time, and overhead is rarely attributable to a single unit without an allocation rule.

I would first clarify the business objective: are we trying to produce financial reporting, operational profitability analysis, budget control, or decision support for network planning? The right allocation model depends on that goal.

Important considerations:

- Define cost categories clearly: labor, lease, utilities, inventory holding, shrinkage, transportation, packaging, technology, maintenance, and shared corporate overhead.
- Decide whether costs are direct, indirect, fixed or variable. Direct costs should be assigned directly where possible; indirect costs need transparent allocation keys.
- Choose allocation drivers that reflect operational reality, such as order volume, units handled, storage volume, warehouse capacity, stock value, route distance, delivery frequency, labor hours, or store replenishment volume.
- Preserve history. If a Warehouse is archived or replaced, historical costs must remain tied to the old operational period even if the new Warehouse reuses the same Business Unit Code.
- Track effective dates for Warehouses, Stores, allocation rules and cost centers. Without temporal boundaries, cost reports can become misleading after reorganizations.
- Separate accounting data from operational estimates. Finance may need reconciled actuals, while operations may need near-real-time estimates.
- Design for auditability: each allocated amount should be traceable back to source cost, allocation rule, input metrics and period.

Questions I would ask:

- Which costs are already captured in financial systems, and at what granularity?
- Do Warehouses and Stores already map to cost centers, profit centers or Business Unit Codes?
- What is the reporting period: daily, weekly, monthly or fiscal period?
- Are costs reported as actuals, forecasts, accruals, or all of them?
- Who owns the allocation rules: Finance, Operations, or a shared governance process?
- Which decisions should the tool support, for example closing a Warehouse, changing replenishment strategy, or comparing Store profitability?
- What level of precision is acceptable? Some cost allocation is inherently approximate, so the rules should be defensible rather than pretending to be perfect.

A useful approach would be to start with a simple allocation model and evolve it. For example, allocate direct Warehouse costs to that Warehouse, allocate transportation by route/store volume, and allocate shared overhead by activity-based drivers. Then validate the reports with Finance and Operations before making the model more complex.

## Scenario 2: Cost Optimization Strategies
**Situation**: The company wants to identify and implement cost optimization strategies for its fulfillment operations. The goal is to reduce overall costs without compromising service quality.

**Task**: Discuss potential cost optimization strategies for fulfillment operations and expected outcomes from that. How would you identify, prioritize and implement these strategies?

**Questions you may have and considerations:**
I would treat cost optimization as a portfolio of initiatives rather than a single technical feature. The goal is to reduce cost while protecting service quality, so every initiative needs both a financial metric and an operational guardrail.

Potential strategies:

- Optimize Warehouse capacity usage by identifying underused or over-constrained locations.
- Reduce transportation cost through better Store-to-Warehouse assignment, route consolidation, delivery frequency tuning, and avoiding unnecessary long-distance fulfillment.
- Improve inventory placement so high-demand Products are stocked closer to the Stores that need them.
- Reduce labor cost by smoothing peaks, improving picking/packing processes, and automating repetitive planning tasks.
- Reduce stock-related cost by detecting excess stock, dead stock, shrinkage and avoidable transfers.
- Review Store and Warehouse fulfillment assignments against the business constraints, for example each Store being served by a limited number of Warehouses.
- Identify operational exceptions: emergency shipments, repeated stockouts, replacement Warehouses exceeding budget, or locations consistently above planned capacity.

Expected outcomes:

- Lower cost per order, per unit handled, or per Store replenishment.
- Better Warehouse utilization and fewer avoidable transfers.
- Reduced transportation spend while maintaining delivery SLA.
- Better visibility into which Products, Stores or Warehouses drive cost.
- Earlier detection of budget overruns.

How I would identify opportunities:

- Build baseline metrics: cost per Warehouse, cost per Store, cost per Product category, cost per route, cost per order/unit, and cost variance by period.
- Compare actuals against budgets and operational drivers.
- Segment costs into controllable and non-controllable costs.
- Look for outliers and trend changes, not only absolute totals.
- Validate findings with operational teams before implementing changes, because data can show symptoms without explaining root causes.

How I would prioritize:

- Estimate financial impact.
- Estimate implementation effort and risk.
- Check service-quality impact, such as delivery time, availability and Store replenishment reliability.
- Prefer reversible, measurable changes first.
- Run pilots for changes that affect fulfillment flow or customer-facing service.

Implementation should be incremental. I would start with dashboards and variance detection, then move into recommendations, and only later automate decisions. Cost optimization systems can create bad incentives if automated too early, so human review and explainability are important.

## Scenario 3: Integration with Financial Systems
**Situation**: The Cost Control Tool needs to integrate with existing financial systems to ensure accurate and timely cost data. The integration should support real-time data synchronization and reporting.

**Task**: Discuss the importance of integrating the Cost Control Tool with financial systems. What benefits the company would have from that and how would you ensure seamless integration and data synchronization?

**Questions you may have and considerations:**
Integration with financial systems is important because the Cost Control Tool should not become a parallel source of truth for money. Finance systems usually own actual costs, accounting periods, cost centers, vendor invoices, accruals and journal entries. The cost-control layer should enrich that data with operational context from Warehouses, Stores, Products and fulfillment activity.

Benefits:

- More accurate cost reporting because operational data is reconciled with financial actuals.
- Less manual reconciliation between Finance and Operations.
- Faster visibility into cost variance and budget overruns.
- Consistent cost-center and Business Unit Code mapping.
- Better auditability because source transactions and allocation logic are traceable.
- Timely reporting for operational decisions, not only month-end analysis.

Integration considerations:

- Identify the system of record for each data type. Financial actuals, budgets and cost centers may come from ERP; Warehouse/Store/Product metadata may come from operational systems.
- Define canonical identifiers and mappings, especially Business Unit Code, Warehouse id, Store id, cost center and effective date.
- Support temporal correctness. If a Warehouse is replaced, the old Warehouse's history must remain reportable while the new active Warehouse inherits the Business Unit Code.
- Decide integration mode per use case. Real-time synchronization is valuable for alerts and dashboards, but month-end financial actuals may arrive as batch or event-driven updates.
- Make synchronization idempotent. Retried events or repeated imports should not duplicate costs.
- Track source timestamps, processing timestamps and reconciliation status.
- Provide error handling, dead-letter queues or retry mechanisms for failed syncs.
- Include data-quality checks: missing mappings, invalid cost centers, unexpected negative costs, duplicated source records, and period mismatches.

Questions I would ask:

- Which financial systems are involved: ERP, accounting, procurement, payroll, transport billing, or budgeting tools?
- Which data needs real-time sync and which can be batch?
- Are financial periods locked, and how should corrections be handled after period close?
- What are the audit and compliance requirements?
- Who owns master data mapping between operational entities and finance entities?
- What latency is acceptable for operational reports versus official financial reports?

For implementation, I would define a clear integration contract, use durable event or batch ingestion depending on source capability, store raw imported records for audit, and then transform them into normalized cost facts. I would expose synchronization status so users can distinguish estimated, pending, reconciled and adjusted costs.

## Scenario 4: Budgeting and Forecasting
**Situation**: The company needs to develop budgeting and forecasting capabilities for its fulfillment operations. The goal is to predict future costs and allocate resources effectively.

**Task**: Discuss the importance of budgeting and forecasting in fulfillment operations and what would you take into account designing a system to support accurate budgeting and forecasting?

**Questions you may have and considerations:**
Budgeting and forecasting matter because fulfillment cost is driven by both planned structure and operational volatility. Warehouse capacity, Store coverage, Product demand, transportation routes, labor availability and inventory levels all affect future cost. Without forecasting, cost control becomes reactive.

What I would design for:

- Budget versions: baseline budget, revised budget, forecast, scenario plans and approved final budget.
- Time granularity: monthly may be enough for Finance, while Operations may need weekly or daily forecasts during peak periods.
- Entity granularity: Warehouse, Store, Product category, route, cost center and Business Unit Code.
- Actual-versus-budget tracking with variance explanations.
- Forecast inputs such as historical cost, demand forecast, stock levels, planned Warehouse replacements, Store growth, seasonal peaks, labor rates, fuel/transport costs and lease/overhead changes.
- Scenario modeling, for example replacing a Warehouse, changing Store fulfillment assignments, increasing capacity, or changing delivery frequency.
- Clear distinction between fixed costs, variable costs and step costs. A Warehouse lease behaves differently from labor overtime or transport spend.
- Effective dating for structural changes. If a Warehouse is archived and replaced mid-period, the forecast needs to know which operation applies to which dates.
- Confidence levels or forecast quality indicators, especially where input data is uncertain.

Questions I would ask:

- Who uses the budget: Finance, Operations, local Warehouse managers, leadership, or all of them?
- What decisions should forecasts support: hiring, capacity expansion, Warehouse replacement, transportation contracts, or Store replenishment planning?
- How accurate do forecasts need to be, and at what level of detail?
- What historical data is available and how clean is it?
- Are there known seasonal cycles, campaigns or peak periods?
- How are budgets approved and revised?
- Should the system support what-if analysis before operational changes are approved?

I would start with deterministic forecasting using historical actuals plus operational drivers, because that is explainable and easier to validate with stakeholders. More advanced statistical or machine-learning forecasting could be added later if the data volume and business value justify it. The system should make variances visible and explainable rather than only producing a number.

## Scenario 5: Cost Control in Warehouse Replacement
**Situation**: The company is planning to replace an existing Warehouse with a new one. The new Warehouse will reuse the Business Unit Code of the old Warehouse. The old Warehouse will be archived, but its cost history must be preserved.

**Task**: Discuss the cost control aspects of replacing a Warehouse. Why is it important to preserve cost history and how this relates to keeping the new Warehouse operation within budget?

**Questions you may have and considerations:**
Warehouse replacement is not just a CRUD update. It is a business transition where the old Warehouse stops being active, the new Warehouse starts operating, and the Business Unit Code continues to represent the area or business unit. Cost history must be preserved so the company can distinguish old-operation performance from new-operation performance.

Why preserving history matters:

- It keeps financial reporting accurate. Costs incurred by the old Warehouse should not be overwritten or silently attributed to the new physical operation.
- It supports auditability. Finance needs to explain which costs belonged to which asset/location/time period.
- It allows before-and-after comparison. The company can evaluate whether the replacement improved cost, capacity, service level or utilization.
- It protects budgeting. If the new Warehouse inherits the Business Unit Code, reports must still separate historical actuals from the new budget period.
- It avoids misleading trends. Without archived history, a new Warehouse could appear responsible for cost overruns or operational issues from before it existed.

Cost-control considerations during replacement:

- Define the replacement effective date and transition period.
- Track one-time replacement costs separately, such as moving inventory, lease termination, setup, integration, equipment, training and temporary labor.
- Ensure the new Warehouse capacity can handle existing stock and expected future demand.
- Preserve stock continuity. The assignment mentions that replacement stock should match the previous Warehouse stock, which helps prevent hidden inventory loss or artificial write-offs.
- Decide how open commitments are handled: purchase orders, transport contracts, maintenance contracts, invoices and accruals.
- Map the old and new Warehouse to the same Business Unit Code while retaining distinct internal ids and lifecycle timestamps.
- Monitor early-life variance for the new Warehouse, because ramp-up periods often have abnormal labor, transport or setup costs.

Questions I would ask:

- What is the official cutover date for financial and operational reporting?
- Are there overlapping operations where both Warehouses are active temporarily?
- Which costs should be treated as one-time transition costs versus recurring operational costs?
- Is the new Warehouse expected to reduce cost, increase capacity, improve service level, or all of these?
- What budget was approved for the replacement project and for the steady-state operation?
- How should historical reports behave when users filter by Business Unit Code?

From a system perspective, I would model Warehouse lifecycle explicitly: active, archived, createdAt, archivedAt, and a stable Business Unit Code. Reports should support both views: aggregate by Business Unit Code for area-level continuity, and split by Warehouse id/lifecycle period for operational accountability. That is how the new operation can be held to its own budget without losing the historical context needed to judge whether the replacement was successful.

## Instructions for Candidates
Before starting the case study, read the [BRIEFING.md](BRIEFING.md) to quickly understand the domain, entities, business rules, and other relevant details.

**Analyze the Scenarios**: Carefully analyze each scenario and consider the tasks provided. To make informed decisions about the project's scope and ensure valuable outcomes, what key information would you seek to gather before defining the boundaries of the work? Your goal is to bridge technical aspects with business value, bringing a high level discussion; no need to deep dive.
