# lcaac commands

The help of every command, as printed by `lcaac <command> --help`. Generated: do not edit.

## lcaac

```text
Usage: lcaac [<options>] <command> [<args>]...

Options:
  --stacktrace  Print the stack trace of errors
  -h, --help    Show this message and exit

Commands:
  api      Work with the API declared in the configuration
  assess   Returns the unitary impacts of a process in CSV format
  eval     Evaluate a data expression
  explain  Explain how the impacts of a process are obtained, step by step, from its parameters,
           variables and data sources to its results
  graph    Generate a Mermaid graph of processes
  serve    Serve over HTTP the API declared in the configuration (under the key 'api' of lcaac.yaml)
  skills   Agent skills for lcaac (https://agentskills.io), to help AI agents write models and use
           the CLI
  test     Run specified tests
  trace    Trace the contributions
  version  Get version
```

## lcaac api

```text
Usage: lcaac api [<options>] <command> [<args>]...

  Work with the API declared in the configuration

Options:
  -h, --help  Show this message and exit

Commands:
  openapi  Print the OpenAPI document of the API declared in the configuration
```

## lcaac api openapi

```text
Usage: lcaac api openapi [<options>]

  Print the OpenAPI document of the API declared in the configuration

Options:
  -c, --config=<path>  Path to LCAAC config file. Defaults to 'lcaac.yaml' The location of the
                       config file is the project directory. Defaults to current working directory.
  -s, --source=<path>  Path to LCAAC source folder or zip/tar.gz/tgz file. Defaults to current
                       working directory.
  -h, --help           Show this message and exit
```

## lcaac assess

```text
Usage: lcaac assess [<options>] <name>

  Returns the unitary impacts of a process in CSV format

Options:
  -c, --config=<path>           Path to LCAAC config file. Defaults to 'lcaac.yaml' The location of
                                the config file is the project directory. Defaults to current
                                working directory.
  -s, --source=<path>           Path to LCAAC source folder or zip/tar.gz/tgz file. Defaults to
                                current working directory.
  -f, --file=<path>             CSV file with parameter values. Example: lcaac assess <process name>
                                -f params.csv
  -l, --label=<value>           Specify a process label as a key value pair. Example: lcaac assess
                                <process name> -l model="ABC" -l geo="FR".
  -D, --parameter=<value>       Override parameter value as a key value pair. Example: lcaac assess
                                <process name> -D x="12 kg" -D geo="UK" -f params.csv.
  -G, --global=<value>          Override global parameters as a key value pair. Example: lcaac
                                assess <process name> -G x="12 kg".
  -o, --output=(csv|text|json)  Output format (text, csv, or json)
  -i, --indicators=<text>       Filter indicators by name (repeatable)
  -h, --help                    Show this message and exit

Arguments:
  <name>  Process name
```

## lcaac eval

```text
Usage: lcaac eval [<options>] <expression>

  Evaluate a data expression

Options:
  -c, --config=<path>   Path to LCAAC config file. Defaults to 'lcaac.yaml' The location of the
                        config file is the project directory. Defaults to current working directory.
  -s, --source=<path>   Path to LCAAC source folder or zip/tar.gz/tgz file. Defaults to current
                        working directory.
  -G, --global=<value>  Override global parameters as a key value pair. Example: lcaac eval <process
                        name> -G x="12 kg".
  -h, --help            Show this message and exit

Arguments:
  <expression>  Data expression
```

## lcaac explain

```text
Usage: lcaac explain [<options>] <name>

  Explain how the impacts of a process are obtained, step by step, from its parameters, variables
  and data sources to its results

Options:
  -c, --config=<path>      Path to LCAAC config file. Defaults to 'lcaac.yaml' The location of the
                           config file is the project directory. Defaults to current working
                           directory.
  -s, --source=<path>      Path to LCAAC source folder or zip/tar.gz/tgz file. Defaults to current
                           working directory.
  -l, --label=<value>      Specify a process label as a key value pair. Example: lcaac explain
                           <process name> -l model="ABC" -l geo="FR".
  -D, --parameter=<value>  Override parameter value as a key value pair. Example: lcaac explain
                           <process name> -D x="12 kg" -D geo="UK" -f params.csv.
  -G, --global=<value>     Override global parameters as a key value pair. Example: lcaac explain
                           <process name> -G x="12 kg".
  -i, --indicators=<text>  Only explain these indicators (repeatable)
  --depth=<int>            Only explain the processes up to this depth in the supply chain (the
                           process is at 0)
  --precision=<int>        Number of significant digits (default 6)
  --max-rows=<int>         Maximum number of data source rows listed for a read (default 20)
  -h, --help               Show this message and exit

Arguments:
  <name>  Process name
```

## lcaac graph

```text
Usage: lcaac graph [<options>] <name>

  Generate a Mermaid graph of processes

Options:
  -c, --config=<path>          Path to LCAAC config file. Defaults to 'lcaac.yaml' The location of
                               the config file is the project directory. Defaults to current working
                               directory.
  -s, --source=<path>          Path to LCAAC source folder or zip/tar.gz/tgz file. Defaults to
                               current working directory.
  -l, --label=<value>          Specify a process label as a key value pair. Example: lcaac graph
                               <process name> -l model="ABC" -l geo="FR".
  -D, --parameter=<value>      Override parameter value as a key value pair. Example: lcaac graph
                               <process name> -D x="12 kg" -D geo="UK" -f params.csv.
  -G, --global=<value>         Override global parameters as a key value pair. Example: lcaac graph
                               <process name> -G x="12 kg".
  --hide-products              Hide product names on edges
  --show-quantities            Show quantities on edges
  --show-biosphere             Show biosphere edges
  --show-impacts               Show impact edges
  -i, --indicator=<text>       Show port contribution for this indicator on each edge
  --absolute                   Report indicator contributions in absolute values (default: relative
                               %)
  -o, --output=(mermaid|html)  Output format (mermaid or html)
  -h, --help                   Show this message and exit

Arguments:
  <name>  Process name
```

## lcaac serve

```text
Usage: lcaac serve [<options>]

  Serve over HTTP the API declared in the configuration (under the key 'api' of lcaac.yaml)

Options:
  -c, --config=<path>  Path to LCAAC config file. Defaults to 'lcaac.yaml' The location of the
                       config file is the project directory. Defaults to current working directory.
  -s, --source=<path>  Path to LCAAC source folder or zip/tar.gz/tgz file. Defaults to current
                       working directory.
  --host=<text>        Host to listen on
  -p, --port=<int>     Port to listen on
  --max-batch=<int>    Maximum number of requests in a batch
  --hide-locations     Do not show the locations of the model in the errors returned to clients
  -h, --help           Show this message and exit
```

## lcaac skills

```text
Usage: lcaac skills [<options>] <command> [<args>]...

  Agent skills for lcaac (https://agentskills.io), to help AI agents write models and use the CLI

Options:
  -h, --help  Show this message and exit

Commands:
  install  Install the skills bundled with this version of lcaac, by default into .agents/skills of
           the current directory, where agents look for the skills of a project. Skills installed by
           lcaac before are replaced.
  list     List the skills bundled with this version of lcaac
```

## lcaac skills install

```text
Usage: lcaac skills install [<options>]

  Install the skills bundled with this version of lcaac, by default into .agents/skills of the
  current directory, where agents look for the skills of a project. Skills installed by lcaac before
  are replaced.

Options:
  --user        Install into ~/.agents/skills, for all the projects of the user
  --dir=<path>  Install into this directory, e.g. .claude/skills for an agent with its own directory
  -h, --help    Show this message and exit
```

## lcaac skills list

```text
Usage: lcaac skills list [<options>]

  List the skills bundled with this version of lcaac

Options:
  -h, --help  Show this message and exit
```

## lcaac test

```text
Usage: lcaac test [<options>] [<name>]

  Run specified tests

Options:
  -c, --config=<path>  Path to LCAAC config file. Defaults to 'lcaac.yaml' The location of the
                       config file is the project directory. Defaults to current working directory.
  -s, --source=<path>  Path to LCAAC source folder or zip/tar.gz/tgz file. Defaults to current
                       working directory.
  -f, --file=<path>    CSV file with parameter values. Example: lcaac test <process name> -f
                       params.csv
  --show-success       Show successful assertions
  -h, --help           Show this message and exit

Arguments:
  <name>  Process name
```

## lcaac trace

```text
Usage: lcaac trace [<options>] <name>

  Trace the contributions

Options:
  -c, --config=<path>           Path to LCAAC config file. Defaults to 'lcaac.yaml' The location of
                                the config file is the project directory. Defaults to current
                                working directory.
  -s, --source=<path>           Path to LCAAC source folder or zip/tar.gz/tgz file. Defaults to
                                current working directory.
  -f, --file=<path>             CSV file with parameter values. Example: lcaac trace <process name>
                                -f params.csv
  -l, --label=<value>           Specify a process label as a key value pair. Example: lcaac trace
                                <process name> -l model="ABC" -l geo="FR".
  -D, --parameter=<value>       Override parameter value as a key value pair. Example: lcaac trace
                                <process name> -D x="12 kg" -D geo="UK" -f params.csv.
  -G, --global=<value>          Override global parameters as a key value pair. Example: lcaac trace
                                <process name> -G x="12 kg".
  -o, --output=(csv|text|json)  Output format (text, csv, or json)
  -i, --indicators=<text>       Filter indicators by name (repeatable)
  -h, --help                    Show this message and exit

Arguments:
  <name>  Process name
```

## lcaac version

```text
Usage: lcaac version [<options>]

  Get version

Options:
  -h, --help  Show this message and exit
```
