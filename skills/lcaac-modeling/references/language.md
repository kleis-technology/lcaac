# The LCA as Code language

## Files and names

- A model is every `.lca` file under the source directory. Names of processes, products, substances, units, data
  sources, indicators and global parameters are global: split a model into files freely (e.g. `units.lca`,
  `datasources.lca`, one file per subsystem).
- Identifiers: letters, digits and `_`, not starting with a digit. Comments: `// ...` and `/* ... */`.
- Names of parameters, variables and `for_each` rows share one namespace with units and global parameters: `s`
  (seconds), `m`, `g`, `l`, `p`, `u`, `W`, ... cannot be used as names (`'s' ... already defined in the process or as
  a global`).
- The order of definitions, and of blocks within a process, does not matter.

## Quantities and units

- A quantity is a number followed by a unit or an expression: `2 kg`, `1e-3 kg`, `-0.8 kg`, `0.5 u`, `3 x` (3 times
  the variable `x`). A number alone is not an expression.
- Operators: `+`, `-`, `*`, `/`, parentheses, and `^` with a number exponent (`(2 m)^2`). `+` and `-` need operands
  of the same dimension; units convert automatically (`1 kWh + 3.6 MJ` is `2 kWh`).
- Strings are literals in double quotes (`"FR"`), used for parameters, labels and data source columns. They cannot be
  concatenated.

### Units of the prelude

| Dimension | Units |
|---|---|
| none | `u`, `p`, `piece`, `person`, `dimensionless`, `percent` (0.01 u), `guest_night` |
| mass | `kg`, `g`, `ton` |
| length | `m`, `mm`, `cm`, `km` |
| area, volume | `m2`, `ha`, `km2`; `m3`, `l`, `cl`, `ml` |
| time | `s`, `min`, `hour`, `day`, `year` (365 days) |
| energy, power | `Wh`, `kWh`, `MWh`, `J`, `kJ`, `MJ`; `W` |
| combined | `tkm` (ton·km), `personkm`, `m2a` (m²·year), `my`, `kgy`, `kga`, `m3y` |
| other | `K`, `Bq`, `kBq`, `lumen`, `lux` |
| impact categories | `kg_CO2_Eq`, `kg_Sb_Eq`, `MJ_net_calorific_value`, `mol_H_p_Eq`, `kg_P_Eq`, `kg_N_Eq`, `mol_N_Eq`, `CTUe`, `CTUh`, `kBq_U235_Eq`, `kg_CFC_11_Eq`, `disease_incidence`, `kg_NMVOC_Eq`, `m3_world_eq_deprived` |

Units combine with `*` and `/`: `1 person * km`, `100 kg_CO2_Eq / year`. Any other unit is declared, either as an
alias of a quantity, or with a new dimension:

```lca
unit kW {
    symbol = "kW"
    alias_for = 1000 W
}

unit GB {
    symbol = "GB"
    dimension = "memory"
}

unit TB {
    symbol = "TB"
    alias_for = 1024 GB
}
```

## Processes

```lca
@cached                                  // optional: solve this process and its supply chain once, then reuse it
process bake {
    params {                             // values given by callers, with defaults
        salt_amount = 30 g
    }
    variables {                          // computed values, may use params and other variables
        flour_amount = 1 kg - salt_amount
    }
    products {                           // outputs: the first one is the reference product
        1 kg bread
    }
    inputs {                             // products consumed
        flour_amount flour
        salt_amount salt from salt_mine
    }
    emissions {                          // substances released to the environment
        0.1 kg co2(compartment = "air")
    }
    resources {                          // substances taken from the environment
        2 l water(compartment = "water")
    }
    land_use {
        1 m2a occupation(compartment = "land")
    }
    impacts {                            // impacts given directly, per indicator
        0.01 kg_CO2_Eq GWP
    }
    meta {                               // free text: sources, assumptions
        "source" = "baker's notes, 2024"
    }
}
```

### Inputs and the supplying process

An input is `<quantity> <product>`, optionally followed by the process that supplies it:

- `2 kg flour`: the process that produces `flour`, if exactly one does. If several do, it is an error (`more than one
  processes found providing flour`): use `from`. If none does, the product stays an unresolved flow (see pitfalls).
- `2 kg flour from mill`: the process `mill`, with its default parameters.
- `2 kg flour from mill(yield = 80 percent, region = "FR")`: with arguments. An unknown parameter is an error.
- `1 kWh electricity from electricity match (geo = "FR", mix = "average")`: the variant of `electricity` with exactly
  these labels. A `match` that gives only some of the labels of a process does not select it.
- Quantities may be expressions and negative: `-0.8 kg steel` (avoided production).

The quantity of an input must have the dimension of the unit of the product as the supplier produces it.

### Products and allocation

A process with several products shares its inputs and impacts between them with `allocate`:

```lca
process dairy_cow {
    products {
        10 kg milk allocate 80 percent
        2 kg meat allocate 20 percent
    }
    impacts {
        100 kg_CO2_Eq GWP
    }
}
```

Allocation factors are relative weights (normalized by their sum).

## Substances

An emission, resource or land use names a substance and its compartment. A `substance` definition characterizes it
into indicators; it applies to the exchanges with the same name and compartment:

```lca
substance co2 {
    name = "carbon dioxide"
    type = Emission                      // or Resource, Land_use
    compartment = "air"
    sub_compartment = "urban"            // optional
    reference_unit = kg
    impacts {
        1 kg_CO2_Eq GWP
    }
}
```

An exchange without a matching substance definition stays an elementary flow: it appears in the results under its
name, without impacts.

## Indicators

An indicator is any name used in `impacts` (`GWP`, `climate_change`, ...). Use one unit dimension per indicator: the
same name with two dimensions gives two separate columns.

## Global parameters and variables

```lca
params {
    timewindow = 1 hour
}

variables {
    hours = timewindow / 1 hour
}
```

Global parameters can be overridden for a run (`lcaac assess -G timewindow="720 hour"`); global variables are
computed from them. Both can be used in any process, including in the amounts of products (`timewindow vm`).

## Data sources

A data source declares a table (a CSV file with the `csv` connector) and the default value of each column, which
also fixes its type and unit:

```lca
datasource inventory {
    location = "inventory.csv"
    schema {
        id = "server-01"
        pool = "web"
        quantity = 1 p
        power = 400 W
        GWP = 0 kg_CO2_Eq
    }
}
```

Reading it:

```lca
process server {
    params {
        id = "server-01"
    }
    variables {
        row = lookup inventory match id = id                        // one row (the first that matches)
        embodied = row.GWP                                          // a column, with the unit of its default
    }
    products {
        1 p server
    }
    impacts {
        embodied GWP
    }
}

process pool {
    params {
        pool = "web"
    }
    products {
        1 p pool
    }
    inputs {
        for_each srv from inventory match pool = pool {             // every matching row
            variables {
                n = srv.quantity
            }
            n server from server(id = srv.id)
        }
    }
}

process pool_power {
    products {
        1 p pool_power
    }
    impacts {
        sum(inventory match pool = "web", quantity * GWP) GWP      // sum of products of columns
    }
}
```

- `match` takes one condition (`match id = id`) or several (`match (pool = "web", id = x)`).
- `lookup` fails if no row matches, and takes the first one if several do.
- `for_each` works in `inputs`, `emissions`, `resources`, `land_use` and `impacts`, and may hold `variables`.
- A whole row can be a parameter, with `default_record from <datasource>` as default, and passed as an argument:
  `1 p server from server_from_row(row = srv)`.
- `sum(<datasource> [match ...], c1 * c2 * ...)`: only products of columns.

## Labels

Processes of the same name are told apart by `labels` (strings). A labelled process is only selected with all its
labels: `from electricity match (geo = "FR")` in a model, `-l geo=FR` on the command line. Without them, `lcaac assess`
fails (`Could not get template for electricity{}`), and an input with `from electricity` stays unresolved.

## Tests

```lca
process sandwich {
    params {
        salt_amount = 30 g
    }
    products {
        1 p sandwich
    }
    inputs {
        200 g bread
        salt_amount salt
    }
    impacts {
        0.4 kg_CO2_Eq GWP
    }
}

test sandwich {
    given {
        1 p sandwich from sandwich(salt_amount = 10 g)
    }
    assert {
        salt between 10 g and 10 g
        bread between 0.2 kg and 0.2 kg
        GWP between 0.3 kg_CO2_Eq and 0.5 kg_CO2_Eq
    }
}
```

`given` lists the demand (like inputs); `assert` gives ranges for indicators, substances, or unresolved products of
the system. `lcaac test` runs every test; each assertion is reported.
