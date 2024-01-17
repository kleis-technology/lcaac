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

## Help

```bash
lcaac --help
lcaac assess --help
```
