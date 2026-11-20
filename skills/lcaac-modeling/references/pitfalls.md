# Pitfalls

Each one was observed with lcaac. The silent ones (no error, wrong result) are the most dangerous: check the
columns of `lcaac assess` and the rows of `lcaac trace` after every change.

## Silent: missing impacts

| Symptom | Cause | Fix |
|---|---|---|
| `lcaac assess` shows a column named after a product (`electricity`, `water`), and impacts are lower than expected | No process produces this product: it is an unresolved flow (a cut-off) | Add the process, or correct the product name (names are case sensitive) |
| Same, although the supplier exists with `from p match (...)` | The `match` does not give exactly the labels of `p` (a subset does not select it), or `p` has labels and the input has no `match` | Give all the labels of the variant |
| A column named after a substance (`co2`), and missing impacts | The emission does not match the substance: `250 kg co2` without the compartment of the `substance` definition | Write `250 kg co2(compartment = "air")` |
| An indicator appears twice in the columns | The same indicator name is used with two dimensions (`1 kg_CO2_Eq GWP` and `1 kWh GWP`) | One dimension per indicator |
| A value read from a data source belongs to another item | `lookup` takes the first matching row when several match | Filter on a unique column (an id), or with several conditions: `match (id = id, pool = pool)` |
| `-D name=...` has no effect | `name` is not a parameter of the process: `-D` ignores it (the output only gets a column) | Check the parameter names (`lcaac trace` shows the arguments of each process) |
| `-G name=...` has no effect | `name` is not a global parameter (a top-level `params` block): `-G` ignores it | Declare it at the top level, not in the `params` of a process |

## Errors

| Message | Cause | Fix |
|---|---|---|
| `extraneous input '*' expecting ...` | A bare number: `0.5 * x` | `0.5 u * x` or `0.5 x` |
| `mismatched input '<EOF>'` (in `lcaac eval "2"`) | A number without unit | Give a unit: `2 u` |
| `kW is not reduced` | Unknown unit | Declare it: `unit kW { symbol = "kW" alias_for = 1000 W }` |
| `incompatible dimensions: mass vs energy for product electricity` | The quantity of an input does not have the dimension of the product's unit | Use the unit of the supplier's product, or convert |
| `incompatible dimensions: mass vs length in left=kg and right=m` | `+` or `-` between different dimensions | Check the formula (often a missing factor) |
| `more than one processes found providing flour` | Several processes produce the product, and the input has no `from` | Add `from <process>` (and `match` for variants) |
| `Could not get template for bake{}` | The process has labels, and none were given (`lcaac assess bake`) | `lcaac assess bake -l recipe=classic`, or `match` in the model |
| `Duplicate process [p] defined` | Two processes with the same name and the same labels | Rename one, or give them different labels |
| `unknown parameters: [y]` | An argument `from p(y = ...)` that `p` does not declare | Use the names of the `params` of `p` |
| `'s' ... already defined in the process or as a global` | A parameter, variable or `for_each` row named like a unit (`s`, `m`, `g`, `l`, `p`, `u`) or a global | Rename it (`srv`, `mass`, ...) |
| `no record found in datasource 'x' [x.csv] matching {id=zz}` | `lookup` found no row | Check the value and the CSV file |
| `invalid index: 'vcpu' not in [id, hours]` | A column of the schema is missing from the CSV file | Add the column to the file |
| `unknown reference 'salt'` (in a test) | The assertion names something absent from the system of the test | Assert on an indicator, substance or unresolved product of that system |

## Numbers

- Results are floating point: `8.639999999999999` is 8.64. Test with ranges, not exact values, for computed results.
- Multiplying by a dimensionless column gives units such as `p_kg_CO2_Eq` (kg CO2-Eq times pieces): the value is
  right, the unit only shows the factor.
