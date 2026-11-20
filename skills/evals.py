#!/usr/bin/env python3
"""
Evaluations of the lcaac skills (https://agentskills.io/skill-creation/evaluating-skills).

Each skill has its cases in <skill>/evals/evals.json: a prompt, input files, the expected output, and assertions.
An assertion with a "check" is graded by a command of this script, run in the directory where the agent worked;
the others are for a human or an LLM judge.

    # Run every case with and without the skills, with any agent that takes a prompt as its last argument
    python3 skills/evals.py run --agent "claude -p --permission-mode acceptEdits --allowedTools=Bash(lcaac:*)" \\
        --skills-dir .claude/skills

    # Grade an iteration again (e.g. after changing the checks)
    python3 skills/evals.py grade /tmp/lcaac-skill-evals/iteration-1

    # Run one check in the current directory
    python3 skills/evals.py check impact-between espresso 'kg CO2-Eq' 0.0374 0.0376

The workspace (by default in the temporary directory, outside the repository, so that the agent sees nothing of it)
follows the layout of the guide: iteration-N/eval-<skill>-<name>/<with_skill|without_skill>/ with outputs/,
transcript.txt, timing.json and grading.json, and iteration-N/benchmark.json.
"""

import argparse
import json
import os
import re
import shlex
import shutil
import socket
import statistics
import subprocess
import sys
import tempfile
import time
import urllib.request
from pathlib import Path

SKILLS = Path(__file__).resolve().parent
CONFIGS = ["with_skill", "without_skill"]
LCAAC = os.environ.get("LCAAC", "lcaac")


# Checks: each returns (passed, evidence), run in the directory where the agent worked

def lcaac(cwd, *args, timeout=180):
    return subprocess.run([LCAAC, *args], cwd=cwd, capture_output=True, text=True, timeout=timeout)


def assess_impacts(cwd, process):
    result = lcaac(cwd, "assess", process, "-o", "json")
    if result.returncode != 0:
        raise CheckError(f"lcaac assess {process} failed: {(result.stdout + result.stderr).strip()[-400:]}")
    rows = json.loads(result.stdout)
    return rows[0]["impacts"] if rows else {}


class CheckError(Exception):
    pass


def check_tests_pass(cwd):
    result = lcaac(cwd, "test")
    output = (result.stdout + result.stderr).strip()
    match = re.search(r"Run (\d+) tests", output)
    passed = result.returncode == 0 and match is not None and int(match.group(1)) > 0
    return passed, output[-400:]


def same_unit(a, b):
    """The same unit, however spelled: `kg CO2-Eq`, `kg_CO2_eq`, ... The prelude's own is checked by impact-unit."""
    normalize = lambda u: re.sub(r"[^a-z0-9]", "", u.lower())
    return normalize(a) == normalize(b)


def check_impact_between(cwd, process, unit, lo, hi):
    impacts = assess_impacts(cwd, process)
    found = {k: v for k, v in impacts.items() if same_unit(v["unit"], unit)}
    passed = any(float(lo) <= v["amount"] <= float(hi) for v in found.values())
    return passed, f"impacts of {process}: {impacts}"


def check_impact_unit(cwd, process, unit):
    impacts = assess_impacts(cwd, process)
    return any(v["unit"] == unit for v in impacts.values()), f"units of {process}: {[v['unit'] for v in impacts.values()]}"


def check_impacts_only(cwd, process, *names):
    impacts = assess_impacts(cwd, process)
    others = sorted(set(impacts) - set(names))
    return not others, f"columns of {process}: {sorted(impacts)}" + (f", unexpected: {others}" if others else "")


def check_trace_mentions(cwd, process, *texts):
    result = lcaac(cwd, "trace", process, "-o", "csv")
    output = result.stdout
    missing = [t for t in texts if t not in output]
    return result.returncode == 0 and not missing, f"missing: {missing}" if missing else f"trace of {process} mentions {list(texts)}"


def numbers_in(text):
    return [float(x) for x in re.findall(r"-?\d+(?:\.\d+)?(?:[eE][-+]?\d+)?", text)]


def close(a, b, rel=1e-3):
    return abs(a - b) <= rel * max(abs(a), abs(b)) + 1e-12


def check_file_numbers(cwd, file, *expected):
    path = Path(cwd) / file
    if not path.is_file():
        return False, f"{file} not found"
    numbers = numbers_in(path.read_text())
    missing = [e for e in expected if not any(close(float(e), n) for n in numbers)]
    return not missing, f"{file}: {path.read_text()[:400]}" + (f"; missing {missing}" if missing else "")


def check_openapi(cwd):
    result = lcaac(cwd, "api", "openapi")
    if result.returncode != 0:
        return False, (result.stdout + result.stderr).strip()[-400:]
    paths = list(json.loads(result.stdout).get("paths", {}))
    return True, f"paths: {paths}"


def free_port():
    with socket.socket() as s:
        s.bind(("127.0.0.1", 0))
        return s.getsockname()[1]


def check_serve_post(cwd, operation, body, expected):
    port = free_port()
    server = subprocess.Popen([LCAAC, "serve", "-p", str(port)], cwd=cwd, stdout=subprocess.PIPE,
                              stderr=subprocess.STDOUT, text=True)
    base = f"http://127.0.0.1:{port}"
    try:
        for _ in range(120):
            if server.poll() is not None:
                return False, f"lcaac serve exited: {server.stdout.read()[-400:]}"
            try:
                urllib.request.urlopen(f"{base}/health", timeout=1)
                break
            except OSError:
                time.sleep(0.5)
        request = urllib.request.Request(f"{base}/{operation}", body.encode(), {"Content-Type": "application/json"})
        try:
            response = urllib.request.urlopen(request, timeout=60).read().decode()
        except urllib.error.HTTPError as e:
            return False, f"{e.code}: {e.read().decode()[:400]}"
        outputs = json.loads(response).get("outputs", {})
        passed = any(close(float(expected), n) for n in numbers_in(json.dumps(outputs)))
        return passed, f"POST /{operation} {body}: {response[:400]}"
    finally:
        server.terminate()
        server.wait(timeout=30)


CHECKS = {
    "tests-pass": check_tests_pass,
    "impact-between": check_impact_between,
    "impact-unit": check_impact_unit,
    "impacts-only": check_impacts_only,
    "trace-mentions": check_trace_mentions,
    "file-numbers": check_file_numbers,
    "openapi": check_openapi,
    "serve-post": check_serve_post,
}


def run_check(cwd, check):
    name, *args = shlex.split(check)
    try:
        return CHECKS[name](cwd, *args)
    except CheckError as e:
        return False, str(e)
    except Exception as e:  # a broken output must fail the assertion, not the grading
        return False, f"{type(e).__name__}: {e}"


# Grading and aggregation

def grade(case, run_dir):
    outputs = run_dir / "outputs"
    results = []
    for assertion in case["assertions"]:
        if "check" in assertion:
            passed, evidence = run_check(outputs, assertion["check"])
        else:
            passed, evidence = None, "not checked: for a human or an LLM judge"
        results.append({"text": assertion["text"], "passed": passed, "evidence": evidence})
    graded = [r for r in results if r["passed"] is not None]
    nb_passed = sum(1 for r in graded if r["passed"])
    grading = {
        "assertion_results": results,
        "summary": {
            "passed": nb_passed,
            "failed": len(graded) - nb_passed,
            "total": len(graded),
            "ungraded": len(results) - len(graded),
            "pass_rate": nb_passed / len(graded) if graded else None,
        },
    }
    (run_dir / "grading.json").write_text(json.dumps(grading, indent=2))
    return grading


def stats(values):
    values = [v for v in values if v is not None]
    if not values:
        return None
    return {"mean": statistics.mean(values), "stddev": statistics.stdev(values) if len(values) > 1 else 0.0}


def benchmark(iteration):
    rows = []
    for grading_file in sorted(iteration.glob("eval-*/*/**/grading.json")):
        run_dir = grading_file.parent
        config = next(p.name for p in [run_dir, *run_dir.parents] if p.name in CONFIGS)
        timing = json.loads((run_dir / "timing.json").read_text()) if (run_dir / "timing.json").exists() else {}
        summary = json.loads(grading_file.read_text())["summary"]
        rows.append((config, summary["pass_rate"], timing.get("duration_ms", 0) / 1000, timing.get("total_tokens")))
    summary = {config: {
        "pass_rate": stats([r[1] for r in rows if r[0] == config]),
        "time_seconds": stats([r[2] for r in rows if r[0] == config]),
        "tokens": stats([r[3] for r in rows if r[0] == config]),
    } for config in CONFIGS}
    delta = {}
    for key in ["pass_rate", "time_seconds", "tokens"]:
        a, b = summary["with_skill"][key], summary["without_skill"][key]
        delta[key] = a["mean"] - b["mean"] if a and b else None
    result = {"run_summary": {**summary, "delta": delta}}
    (iteration / "benchmark.json").write_text(json.dumps(result, indent=2))
    return result


def cases(skill_filter, case_filter):
    for evals_file in sorted(SKILLS.glob("*/evals/evals.json")):
        spec = json.loads(evals_file.read_text())
        if skill_filter and spec["skill_name"] not in skill_filter:
            continue
        for case in spec["evals"]:
            if case_filter and case["name"] not in case_filter:
                continue
            yield spec["skill_name"], case


def run_dirs(config_dir, runs):
    return [config_dir] if runs == 1 else [config_dir / f"run-{k}" for k in range(1, runs + 1)]


def print_report(iteration):
    print(f"\n{'case':40} {'config':14} pass")
    for grading_file in sorted(iteration.glob("eval-*/*/**/grading.json")):
        summary = json.loads(grading_file.read_text())["summary"]
        relative = grading_file.parent.relative_to(iteration).parts
        print(f"{relative[0]:40} {'/'.join(relative[1:]):14} {summary['passed']}/{summary['total']}")
    print(json.dumps(json.loads((iteration / "benchmark.json").read_text())["run_summary"], indent=2))


def command_run(args):
    workspace = Path(args.workspace)
    workspace.mkdir(parents=True, exist_ok=True)
    iteration = workspace / f"iteration-{1 + sum(1 for _ in workspace.glob('iteration-*'))}"
    env = dict(os.environ)
    lcaac_path = shutil.which(LCAAC)
    if lcaac_path is None:
        sys.exit(f"lcaac not found: put it on the PATH, or set LCAAC")
    env["PATH"] = f"{Path(lcaac_path).parent}{os.pathsep}{env['PATH']}"
    for skill, case in cases(args.skill, args.case):
        for config in args.configs.split(","):
            for run_dir in run_dirs(iteration / f"eval-{skill}-{case['name']}" / config, args.runs):
                outputs = run_dir / "outputs"
                outputs.mkdir(parents=True)
                for files in case.get("files", []):
                    shutil.copytree(SKILLS / skill / files, outputs, dirs_exist_ok=True)
                (outputs / ".keep").unlink(missing_ok=True)
                if config == "with_skill":
                    subprocess.run([LCAAC, "skills", "install", "--dir", args.skills_dir], cwd=outputs, check=True,
                                   capture_output=True)
                print(f"{run_dir.relative_to(workspace)}: running", flush=True)
                start = time.time()
                try:
                    agent = subprocess.run([*shlex.split(args.agent), case["prompt"]], cwd=outputs, env=env,
                                           capture_output=True, text=True, timeout=args.timeout)
                    transcript = agent.stdout + agent.stderr
                except subprocess.TimeoutExpired as e:
                    transcript = f"timeout after {args.timeout} s\n{e.stdout or ''}{e.stderr or ''}"
                duration = time.time() - start
                (run_dir / "transcript.txt").write_text(transcript if isinstance(transcript, str) else transcript.decode())
                (run_dir / "timing.json").write_text(json.dumps({"total_tokens": None, "duration_ms": int(duration * 1000)}))
                summary = grade(case, run_dir)["summary"]
                print(f"{run_dir.relative_to(workspace)}: {summary['passed']}/{summary['total']} in {duration:.0f} s",
                      flush=True)
    benchmark(iteration)
    print_report(iteration)
    print(f"\nWorkspace: {iteration}")


def command_grade(args):
    iteration = Path(args.iteration)
    all_cases = {f"eval-{skill}-{case['name']}": case for skill, case in cases(None, None)}
    for eval_dir in sorted(iteration.glob("eval-*")):
        for outputs in sorted(eval_dir.glob("**/outputs")):
            grade(all_cases[eval_dir.name], outputs.parent)
    benchmark(iteration)
    print_report(iteration)


def command_check(args):
    passed, evidence = run_check(Path.cwd(), shlex.join(args.check))
    print(("PASS" if passed else "FAIL") + f": {evidence}")
    sys.exit(0 if passed else 1)


def main():
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    commands = parser.add_subparsers(dest="command", required=True)

    run = commands.add_parser("run", help="run the cases with and without the skills, then grade them")
    run.add_argument("--agent", required=True, help="command of the agent; the prompt is added as its last argument")
    run.add_argument("--skills-dir", default=".agents/skills", help="where the agent finds skills, in its directory")
    run.add_argument("--skill", action="append", help="only the cases of this skill (repeatable)")
    run.add_argument("--case", action="append", help="only this case, by name (repeatable)")
    run.add_argument("--configs", default=",".join(CONFIGS), help="with_skill, without_skill, or both")
    run.add_argument("--runs", type=int, default=1, help="runs per case and configuration")
    run.add_argument("--timeout", type=int, default=900, help="seconds per run")
    run.add_argument("--workspace", default=str(Path(tempfile.gettempdir()) / "lcaac-skill-evals"))
    run.set_defaults(func=command_run)

    regrade = commands.add_parser("grade", help="grade an iteration again")
    regrade.add_argument("iteration")
    regrade.set_defaults(func=command_grade)

    check = commands.add_parser("check", help="run one check in the current directory")
    check.add_argument("check", nargs=argparse.REMAINDER)
    check.set_defaults(func=command_check)

    args = parser.parse_args()
    args.func(args)


if __name__ == "__main__":
    main()
