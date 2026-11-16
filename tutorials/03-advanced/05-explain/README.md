# Explaining a result

`lcaac assess` gives the impacts of a process. `lcaac explain` shows how they are obtained, step by step:
from the parameters, variables and data sources of each process of the supply chain, to the impact per
unit of each product. Use it to check a model by hand, or to understand a result you did not expect.

## Try it yourself

The file `main.lca` defines a `bakery` that bakes bread with electricity and ingredients delivered by
truck. The oven and the recipe come from the CSV files in `data/`.

```bash
cd tutorials/03-advanced/05-explain
lcaac explain bakery
```

The output has four parts: the result, one section per process, the substances and the globals.

## The result

```
RESULT
  1 kg bread, reference product of [1] bakery:
    climate_change  0.060306 kg CO2-Eq  (0.060306 kg CO2-Eq/kg)
  50 g crumbs, reference product of [1] bakery:
    climate_change  0.003174 kg CO2-Eq  (6.348e-5 kg CO2-Eq/g)
```

The impacts of each product of the process, as `lcaac assess` gives them: the impact of the amount
produced, and in parentheses, the impact per unit of the product.

## The processes

Then each process of the supply chain gets a section, numbered from the explained process `[1]`.
The header gives where the process is defined, and which processes use it:

```
================================================================================
[3] truck   ./main.lca:48:1
  used by: [1]
================================================================================
```

### Parameters

```
  parameters:
    distance = 200 km   (set by caller; default: 100 km)   (line 50)
```

The value used for each parameter, and where it comes from: its default value, or the caller, here
`bakery` with `from truck(distance = 200 km)`. With `-D`, the explained process is called with your
values.

### Variables

Each variable is derived in up to three lines: its formula as written, the formula with the values of
the names it uses, and its value.

```
    energy = selected.energy_per_batch / selected.batch_size   (line 25)
           = 30 kWh / (120 u)
           = 0.25 kWh.u⁻¹ (= 0.25 kWh)
```

When a unit is a product of units, as `kWh.u⁻¹` here, its simplified form follows in parentheses:
the dimensionless factors (`u`, `percent`) are removed, the value is the same.

A variable that reads a data source also gives the rows it read. A `lookup` gives the data source and
its filter, the record being its value. A `sum` lists the rows it added up (`--max-rows` limits them).

```
    selected = lookup ovens match id = oven   (line 24)
             = lookup ovens match id = "large"
             = {id: "large", energy_per_batch: 30 kWh, batch_size: 120 u}
               from ovens (data/ovens.csv) where id = "large"
```

### Exchanges

The products, inputs, emissions and impacts follow, as written, with their value when it differs from
the formula. Each input gives its supplier: a process of the explanation `-> [2] power_grid`, or
`-> end point` when no process supplies it.

A `for_each` gives the rows it read, then for each row, its variables and exchanges:

```
    for_each item from ingredients match recipe = "bread"   (line 33)
      read ingredients (data/ingredients.csv) where recipe = "bread": 3 rows
      row #1: item = {recipe: "bread", ingredient: "flour", mass: 0.6 kg}
        item.mass delivery from truck(distance = 200 km)   -> [3] truck
          = 0.6 kg
```

### Contributions

Last, each product of the process gets the table of the contributions to its impact per unit:

```
  contributions to 1 kg bread (share 95%):
    climate_change:
      electricity [2]       0.2375 kWh  x 0.1 kg CO2-Eq/kWh  = 0.02375 kg CO2-Eq
      delivery [3], row #1  0.57 kg     x 0.04 kg CO2-Eq/kg  = 0.0228 kg CO2-Eq
      ...
      total                                                  = 0.060306 kg CO2-Eq
      per kg bread          / 1 kg                           = 0.060306 kg CO2-Eq/kg   [ok: matches solver]
```

Each line is an exchange of the process: its amount, times its impact per unit, gives its contribution.
The impact per unit of an input is the one of the product of its supplier, given in the section of the
supplier (`[2]`, `[3]`). The sum of the contributions, divided by the amount of the product, is the
impact per unit of the product.

With several products, the process is shared between them: the share of each product is its
allocation weight divided by the total weight, here 95% for the bread. The amounts in its table are
already multiplied by its share.

Each impact per unit is checked against the one of the solver, which computes all of them at once:
`[ok: matches solver]`. A `MISMATCH` would mean that the explanation does not account for the result.

### Loops and cached processes

When processes supply each other, e.g. a power plant that needs steel made with its electricity, their
impacts per unit depend on each other. Their sections then say `loop with [n]`: their tables are
equations that the solver solves together, each table still matches.

A `@cached` process is evaluated as a separate system (see `04-cached-processes`): its section only
gives its aggregated exchanges. Explain it on its own to see inside.

## Options

| Option | |
|---|---|
| `-i climate_change` | only explain these indicators (repeatable) |
| `--depth 1` | only explain the processes up to this depth in the supply chain, the explained process being at 0 |
| `--precision 3` | the number of significant digits (6 by default) |
| `--max-rows 5` | the number of data source rows listed for a read (20 by default) |
| `-D`, `-G`, `-l` | the arguments, globals and labels, as for `lcaac assess` |
