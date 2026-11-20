# Modeling patterns

Generic ways to structure LCA as Code models. Adapt the names; check each step with `lcaac test`.

## The functional unit at the top

Start from one process whose reference product is the functional unit, and whose parameters are the questions the
study answers (sizes, durations, locations, scenarios). Everything else hangs below it.

```lca
process website_year {
    params {
        visits = 1000000 u
        page_weight = 2e-3 GB
        geo = "FR"
    }
    products {
        1 year website
    }
    inputs {
        visits * page_weight data_transfer from network match (geo = geo)
    }
}

unit GB {
    symbol = "GB"
    dimension = "data"
}

process network {
    labels {
        geo = "FR"
    }
    products {
        1 GB data_transfer
    }
    inputs {
        0.06 kWh electricity
    }
}

process grid {
    products {
        1 kWh electricity
    }
    impacts {
        0.05 kg_CO2_Eq GWP
    }
}

test website_year {
    given {
        1 year website from website_year(visits = 1000 u)
    }
    assert {
        electricity between 0.12 kWh and 0.12 kWh
        GWP between 0.0059 kg_CO2_Eq and 0.0061 kg_CO2_Eq
    }
}
```

## Variants with labels

When the same product can come from alternative processes (countries, technologies, suppliers), give them the same
name and product, tell them apart with labels, and choose with a parameter: `from electricity match (geo = geo)`.
Each variant must exist for every value the parameter can take, with exactly the labels given by the `match`.

## Life cycle steps

To report impacts per life cycle step (manufacturing, transport, use, end of life), write an entry process that
consumes one unit of each step, each step being a labelled variant of the same process. `lcaac trace` then shows
one row per step, and selections (e.g. in `lcaac serve`) can target `phase = embodied` or `lc_step = transport`.

```lca
process phone {
    products {
        1 p phone
    }
    inputs {
        1 p phone from phone(lc_step = "manufacturing") match (phase = "embodied")
        1 p phone from phone(lc_step = "transport") match (phase = "embodied")
        1 p phone from phone() match (phase = "use")
    }
}

process phone {
    labels {
        phase = "embodied"
    }
    params {
        lc_step = "manufacturing"
    }
    products {
        1 p phone
    }
    impacts {
        for_each step from phone_steps match lc_step = lc_step {
            step.GWP GWP
        }
    }
}

process phone {
    labels {
        phase = "use"
    }
    products {
        1 p phone
    }
    inputs {
        5 kWh electricity
    }
}

datasource phone_steps {
    location = "phone_steps.csv"
    schema {
        lc_step = "manufacturing"
        GWP = 0 kg_CO2_Eq
    }
}
```

The entry process has no labels; its steps share its name and product. The entry is selected without `match`, the
steps with theirs.

## Inventories from data sources

Keep the data of many similar items (servers, buildings, products) in a CSV file, and the model generic:

- one process per kind of item, taking the id of an item as a parameter and reading its row with
  `lookup <datasource> match id = id`;
- a process for the whole inventory, with `for_each item from <datasource> { 1 p item from item_process(id = item.id) }`.
  Passing the id makes each item a process of its own, with its own row in `lcaac trace`;
- totals and shares with `sum(<datasource> match ..., quantity * size)`, e.g. to allocate a shared infrastructure
  between its users in proportion of their size.

The CSV file can be replaced without changing the model: `lcaac assess -f`, another connector directory in
`lcaac.yaml` (`-c lcaac-mock.yaml`), or the rows of a request with `lcaac serve`.

## Shared infrastructure

A datacenter, a factory or a network serves many users. Model it once, producing its capacity over its lifetime
(e.g. `dc.power * dc.lifespan capacity`, a product in `kW * year`), and let each user consume its share
(`share * 1 kW * year capacity`). Mark it `@cached` when many processes use it: it is then solved once, with the same
results.

## Global settings

Study-wide settings (the period assessed, a scenario year, a grid mix) are global parameters in a `params` block at
the top level, overridden per run with `-G` (or by an API input `{ global: name }`). A reference product can depend
on them (`timewindow server`), so that results follow the period.

## Multi-output processes

A process with several products shares its burdens with `allocate` factors (relative weights, e.g. economic value or
mass). Document the choice in a comment or a `meta` block: it is a methodological decision.

## Avoided burdens

Recycling or energy recovery credits are negative inputs of the product they replace: `-0.8 kg steel` in the
end-of-life process. Keep them in their own process, so that the credit is visible and can be switched off.

## Tests as specification

- A test per important process, with a range computed by hand (show the computation in a comment).
- Exact checks on inventory flows: `steel between 2 kg and 2 kg` checks a mass balance.
- Wider ranges on indicators that come from data, so that updating data does not break every test.
