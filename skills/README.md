# Agent skills

Skills in the [Agent Skills](https://agentskills.io) format, to help AI agents use lcaac:

- `lcaac-modeling`: design and write models (`.lca` files).
- `lcaac-cli`: run and configure the CLI (`lcaac.yaml`, `lcaac serve`).

The CLI bundles them (each skill directory, without its `evals/`), and installs them with `lcaac skills install`.

## Keeping them true

The tests of the CLI (`./gradlew :cli:test`) check that:

- the frontmatter of each `SKILL.md` follows the specification;
- `lcaac-cli/references/commands.md` is the help of the commands: regenerate it with
  `./gradlew :cli:writeCommandReference` after changing a command;
- every `lca` block of the skills parses, and a block with tests is a complete model on its own whose tests pass.

## Evaluations

Each skill has cases in `<skill>/evals/evals.json` (prompt, input files in `evals/files/`, expected output,
assertions), as in the [guide](https://agentskills.io/skill-creation/evaluating-skills). An assertion with a `check` is
graded by `evals.py`; the others are for a human or an LLM judge.

`evals.py run` runs every case with and without the skills, with any agent that takes a prompt as its last
argument, then grades the outputs. It uses `lcaac` on the `PATH`, or the one given by `LCAAC`. With Claude Code:

```bash
./gradlew :cli:installDist
LCAAC=$PWD/cli/build/install/lcaac/bin/lcaac python3 skills/evals.py run \
    --agent "claude -p --permission-mode acceptEdits --allowedTools Bash(lcaac:*)" \
    --skills-dir .claude/skills
```

Options: `--skill` and `--case` to select cases, `--configs with_skill` to skip the baseline, `--runs` for several
runs per case, `--workspace` (by default in the temporary directory, outside the repository, so that the agent sees
nothing of it). Each run leaves its outputs, transcript, timing and grading in the workspace, and the iteration a
`benchmark.json`. `evals.py grade <iteration>` grades an iteration again; `evals.py check <check> ...` runs a check
in the current directory.

Running the cases costs agent time and tokens: a full iteration is 5 cases, twice.
