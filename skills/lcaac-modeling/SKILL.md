---
name: lcaac-modeling
description: Design and write life cycle assessment (LCA) models in the LCA as Code language (.lca files) - processes, products, impacts, units, data sources, labels and tests. Use when creating or changing an LCAAC model, when the user mentions .lca files, LCA as Code or lcaac, or asks to model the environmental impacts of a product, service or system with lcaac.
license: AGPL-3.0 or commercial license, see LICENSE.txt of kleis-technology/lcaac
compatibility: Requires the lcaac CLI on the PATH, to check models (see the skill lcaac-cli).
metadata:
  lcaac-version: "dev"
---

# Modeling with LCA as Code

An LCA as Code model is a set of `.lca` files: processes that turn input products into output products, with
emissions and impacts, connected by product names. `lcaac` solves the supply chain and computes the impacts of one
unit of a product. To run the CLI (assess, trace, test, serve), see the skill `lcaac-cli`.

The language is small and strict, and it is not widespread: do not guess its syntax. Follow the example below and
[references/language.md](references/language.md), and check every change with `lcaac test`.

## Workflow

1. **Scope the study with the user.** What is assessed (the functional unit, e.g. "one year of use of a laptop"),
   where the boundaries are, which indicators, which data. Ask; do not decide these alone. See
   [references/method.md](references/method.md).
2. **Sketch the processes and products** from the functional unit down: each process produces one or more products
   and consumes others. A product connects to the process that produces it.
3. **Write the model top-down**, one process at a time. Declare the units that the prelude lacks.
4. **Write a `test` for each important process** as soon as it exists: an expected range for an indicator, computed
   by hand or from a reference.
5. **Check after every change:** `lcaac test`, then `lcaac assess <process>`. In the output of `assess`, a column
   named after a product or substance (instead of an indicator) is a flow that no process supplies or no substance
   characterizes: its impacts are missing. See [references/pitfalls.md](references/pitfalls.md).
6. **Explain the results:** `lcaac trace <process>` for the contributions, `lcaac explain <process>` for how a
   number is obtained.

## The language in one example

```lca
// A unit that the prelude lacks (it has W, not kW)
unit kW {
    symbol = "kW"
    alias_for = 1000 W
}

// Global parameter, shared by all processes; `lcaac assess -G lifetime="4 year"` overrides it
params {
    lifetime = 5 year
}

// The functional unit: one year of use of a laptop
process laptop_use {
    params {                            // inputs of the process, with default values
        daily_use = 8 hour
        power = 0.03 kW
        geo = "FR"
    }
    variables {                         // intermediate values
        energy = power * daily_use * 365 u
    }
    products {                          // what the process produces: its reference product first
        1 year laptop_use
    }
    inputs {                            // quantity, product, and optionally which process supplies it
        energy electricity from electricity match (geo = geo)
        (1 year / lifetime) * 1 p laptop from laptop_production
    }
}

process laptop_production {
    products {
        1 p laptop
    }
    emissions {                         // to the environment: substance, with its compartment
        250 kg co2(compartment = "air")
    }
}

// Variants of a process: same name and product, different labels, chosen with `match`
process electricity {
    labels {
        geo = "FR"
    }
    products {
        1 kWh electricity
    }
    impacts {                           // impacts given directly, per indicator
        0.05 kg_CO2_Eq GWP
    }
}

process electricity {
    labels {
        geo = "DE"
    }
    products {
        1 kWh electricity
    }
    impacts {
        0.4 kg_CO2_Eq GWP
    }
}

// A substance, characterized into the indicator GWP
substance co2 {
    name = "carbon dioxide"
    type = Emission
    compartment = "air"
    reference_unit = kg
    impacts {
        1 kg_CO2_Eq GWP
    }
}

test laptop_use {
    given {
        1 year laptop_use from laptop_use(geo = "DE")
    }
    assert {
        // 87.6 kWh * 0.4 kg_CO2_Eq/kWh + 250 kg_CO2_Eq / 5
        GWP between 85 kg_CO2_Eq and 86 kg_CO2_Eq
    }
}
```

Data sources (CSV inventories read with `lookup`, `sum` and `for_each`), allocation, `@cached` and the rest:
[references/language.md](references/language.md). Ways to structure models: [references/patterns.md](references/patterns.md).

## Rules that agents get wrong

- **Every quantity has a unit.** A bare number is not an expression: write `0.5 u * x` or `0.5 x`, never `0.5 * x`;
  dimensionless quantities are in `u` (or `p`, `percent`).
- **Units must exist.** The prelude has SI-style units (`kg`, `g`, `ton`, `m`, `km`, `m2`, `m3`, `l`, `s`, `hour`,
  `day`, `year`, `Wh`, `kWh`, `MJ`, `W`, `u`, `p`, `percent`, `tkm`, ...) and impact units (`kg_CO2_Eq`, ...), but
  not `kW`, `t` or `GB`: declare them (`unit`). An unknown unit fails with `kW is not reduced`.
- **Dimensions must agree.** An input's quantity must have the dimension of the product's unit, and `+`/`-` need
  the same dimension: errors say `incompatible dimensions`.
- **Unresolved flows are silent.** A product that no process produces, a `from` that selects no process (a `match`
  must give exactly the labels of the process), or an emission whose compartment differs from the substance's, is not
  an error: it shows up as a column of `lcaac assess`, and its impacts are missing. Check the columns.
- **`lookup` takes the first matching row** when several match. Make the filter unique (e.g. by id).
- **Two processes with the same name need different labels**; two processes producing the same product need `from`.

All of them, with symptoms and fixes: [references/pitfalls.md](references/pitfalls.md).
