# The practitioner's checklist

An LCA model encodes methodological choices. Make them explicit, and ask the user rather than deciding alone: the
user is accountable for the study.

## Before writing the model

Ask, or confirm, and record the answers in comments or `meta` blocks:

1. **Goal.** What decision or communication is the study for? Comparative assertions need more care (same functional
   unit, same boundaries, same data quality for all options).
2. **Functional unit.** What function is assessed, how much, for how long (e.g. "hosting 1000 visits a day for one
   year"). It becomes the reference product of the top process.
3. **System boundaries.** Which life cycle steps (manufacturing, transport, use, end of life), which processes are in
   or out, and the cut-off rule for small flows.
4. **Multi-output processes.** How burdens are shared (physical, economic allocation, or system expansion with
   avoided burdens).
5. **Impact assessment.** Which indicators and method (e.g. EF 3.1, IPCC GWP100), hence the indicators and units of
   `impacts` and substances.
6. **Data.** Sources of each value (measurements, databases, literature, estimates), geography and time period.
   Prefer data sources (CSV) for inventories, so that data can be updated without changing the model.

## While writing

- Name processes and products after what they are; one process per activity.
- Keep assumptions as parameters with defaults, so that they can be varied (`-D`, `-G`) instead of edited.
- Comment every value that is not obvious with its source.
- Unresolved flows (products without a supplier) are cut-offs: list them, and check with the user that they are
  intended.

## Checking the results

- **Inventory checks:** mass and energy balances on key flows, with exact tests.
- **Orders of magnitude:** compare with published results for similar systems.
- **Contributions:** `lcaac trace` shows which parts dominate; a dominant contribution from a minor input usually
  means a unit or factor error.
- **Sensitivity:** vary the uncertain parameters (`-D`, `-f` with several rows) and report the ranges.
- **Explain surprising numbers:** `lcaac explain` shows the values and formulas behind each impact.

## What not to decide alone

The functional unit, boundaries, allocation, indicators, and any value without a source. Propose, explain the
consequences, and let the user choose.
