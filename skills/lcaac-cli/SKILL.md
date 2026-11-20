---
name: lcaac-cli
description: Run and configure the lcaac command-line tool (LCA as Code) - assess, trace, explain and test models, override parameters and data sources, configure lcaac.yaml (connectors, data sources), and serve a model as an HTTP API. Use when running lcaac commands, reading their output or errors, writing lcaac.yaml, or serving or querying an LCA model over HTTP.
license: AGPL-3.0 or commercial license, see LICENSE.txt of kleis-technology/lcaac
compatibility: Requires the lcaac CLI on the PATH.
metadata:
  lcaac-version: "dev"
---

# Using the lcaac CLI

`lcaac` evaluates life cycle assessment models written in the LCA as Code language (`.lca` files). To write or
change the model itself, see the skill `lcaac-modeling`.

Check the installed version with `lcaac version`. Every command prints its options with `--help`; all of them are
in [references/commands.md](references/commands.md).

## A project

```
my-project/
├── lcaac.yaml          # optional: name, connectors (where CSV data lives), API to serve
├── main.lca            # .lca files, found recursively under the source directory
├── src/...
└── data/*.csv          # rows of the data sources declared in the .lca files
```

- Run commands from the project directory. Otherwise, `-c path/to/lcaac.yaml` gives the configuration (its directory
  is the project directory) and `-s path/to/sources` the `.lca` files (a directory, or a `.zip`, `.tar.gz`, `.tgz`).
- Every `.lca` file under the source directory is loaded: names of processes, units and data sources are global.
- The location of a data source (`location = "data/inventory.csv"`) is relative to the directory of the CSV
  connector, the project directory by default. See [references/configuration.md](references/configuration.md).

## Which command

| Question | Command |
|---|---|
| Do the tests of the model pass? | `lcaac test` (all tests), `lcaac test <process>` |
| What are the impacts of one unit of the reference product of a process? | `lcaac assess <process>` |
| Where do the impacts come from (contributions of the supply chain)? | `lcaac trace <process>` |
| How is a number obtained, step by step (parameters, variables, data, formula)? | `lcaac explain <process>` |
| What is the value of an expression (units, conversions)? | `lcaac eval "2 kg + 300 g"` |
| What does the supply chain look like? | `lcaac graph <process>` (Mermaid, or `-o html`) |
| Serve the model as an HTTP API | `lcaac serve`, see [references/serve.md](references/serve.md) |

## Inputs of an evaluation

The commands `assess`, `trace`, `explain` and `graph` take the same inputs:

- `-D name=value`: a parameter of the process, e.g. `-D vcpu="8 u" -D region=DE`. Quantities are text in the language
  syntax, with their unit (`"12 kg"`), quoted for the shell.
- `-l name=value`: a label, to choose a variant of the process (processes of the same name with different labels).
  A process with labels needs all of them: without, `Could not get template for <process>{}`.
- `-G name=value`: a global parameter of the model (declared in a top-level `params` block), for this run only.
- `-f params.csv` (`assess`, `trace`, `test`): one evaluation per row, the columns being parameters; `-D` values apply
  to every row.

A `-D` name that is not a parameter of the process is ignored: the output only gains a column. Likewise, a `-G`
name that is not a global parameter of the model is ignored. Check the names (`lcaac trace` shows the arguments of
each process; global parameters are in top-level `params` blocks).

```
$ lcaac assess virtual_machine -f params.csv -o csv     # params.csv: vcpu,region / 2 u,FR / 4 u,DE
vcpu,region,product,amount,reference unit,GWP,GWP_unit
2 u,FR,vm_hour,1.0,hour,0.001,kg_CO2_Eq
4 u,DE,vm_hour,1.0,hour,0.016,kg_CO2_Eq
```

## Outputs

`assess` and `trace` print a text table by default; use `-o csv` or `-o json` to read results in a program, and
`-i <indicator>` (repeatable) to keep some indicators only.

```
$ lcaac assess vm_fleet -o json
[
{
  "request": {},
  "product": "fleet",
  "amount": 1.0,
  "reference_unit": "p",
  "impacts": {"GWP": {"amount": 8.639999999999999, "unit": "kg CO2-Eq"}}
}
]
```

`assess` gives the impacts of one unit of each product of the process (`amount` is that unit). `trace` gives one row
per product of the supply chain: `depth` (0 is the process), `name` (product), `a` (process), `b` (labels), `c`
(arguments), `amount` (how much of it the process needs) and, per indicator, the impact embodied in that amount,
everything upstream included: rows do not add up.

Numbers are floating point: compare with a tolerance (`8.639999999999999` is 8.64).

A column of `assess` named after a product or substance rather than an indicator (e.g. `electricity`, `kWh`) is a flow
that no process supplies, or that no substance characterizes: its impacts are missing from the results. It is not an
error; see the pitfalls of the skill `lcaac-modeling`.

## The verification loop

After every change to a model:

1. `lcaac test`: prints `Run N tests, P passed, F failed`, and exits with 1 if a test fails. A failure names the
   assertion and its location:
   ```
   ❌  fleet[0] GWP = 8.639999999999999 kg CO2-Eq is not in between 9.0 kg CO2-Eq and 10.0 kg CO2-Eq
        in assertion on 'GWP' (./main.lca:65:9)
   ```
   `--show-success` also prints the assertions that pass, with their values.
2. `lcaac assess <process>` and `lcaac trace <process>`: check the orders of magnitude, and which parts of the supply
   chain contribute.
3. `lcaac explain <process>` when a number is surprising: it shows the values of the parameters, variables and data
   source rows used, and the formulas, down the supply chain (`--depth`, `-i` to narrow it).

## Errors

All errors exit with 1, and are located `file:line:column`:

- Syntax errors, one per line, from the parser:
  ```
  ./main.lca:41:39: extraneous input 'from' expecting ID
  ```
  The first one is the cause; the following ones often follow from it.
- Evaluation errors, with the chain of frames that led to them, innermost first, and related locations (`see ...`):
  ```
  incompatible dimensions: mass vs energy for product electricity
    in input 'electricity' (./main.lca:41:9)
    in process 'virtual_machine' (./main.lca:31:1)
    see product 'electricity' of process 'grid' (./main.lca:45:1)
  ```
- Errors in `lcaac.yaml` are located the same way (`./lcaac.yaml:12:5: <path>: <message>`).

`--stacktrace` (before the command: `lcaac --stacktrace assess ...`) prints the stack trace of an error: only useful
to report a bug of lcaac itself.

## Configuration and serving

- `lcaac.yaml`: [references/configuration.md](references/configuration.md).
- `lcaac serve`, the API declared under the key `api`: [references/serve.md](references/serve.md).
