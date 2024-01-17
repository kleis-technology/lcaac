# LCAAC Command-Line Interface

## Set-up

```bash
./gradlew :cli:installDist
alias lcaac=$GIT_ROOT/cli/build/install/cli/bin/cli
```

## Impact assessment

```bash
lcaac assess "electricity_mix"
```

```bash
lcaac assess "electricity_mix" --data params.csv
```
