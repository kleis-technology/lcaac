# LCAAC Command-Line Interface

## Set-up

```bash
./gradlew :cli:installDist
alias lcaac=$GIT_ROOT/cli/build/install/lcaac/bin/lcaac
```

## Impact assessment

The `assess` command runs the impact assessment of a target process.
The results are printed on the standard output in CSV format.

```bash
cd $GIT_ROOT/cli/samples
lcaac assess "electricity_mix"
```

```bash
cd $GIT_ROOT/cli/samples
lcaac assess "electricity_mix" --data params.csv
```

## Run tests

You can run all the tests with the following command.
```bash
cd $GIT_ROOT/cli/samples
lcaac test
```

By default, the command does not show the successful assertions.
To show the successful assertions, run
```bash
cd $GIT_ROOT/cli/samples
lcaac test --show-success
```

## Help

```bash
lcaac --help
lcaac assess --help
```
