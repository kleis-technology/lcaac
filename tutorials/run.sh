#!/usr/bin/env bash

set -euxo

export GIT_ROOT=$(git rev-parse --show-toplevel)

LCAAC_PATH=$GIT_ROOT/cli/build/install/lcaac/bin
TUTORIALS_PATH=$GIT_ROOT/tutorials

function setup() {
  $GIT_ROOT/gradlew :cli:installDist
}

function lcaac() {
    $LCAAC_PATH/lcaac $@
}

if ! [ -f $LCAAC_PATH/lcaac ]; then
  setup
fi

# Check all lca tests
lcaac test -p $TUTORIALS_PATH/01-basics/01-getting-started
lcaac test -p $TUTORIALS_PATH/01-basics/02-biosphere
lcaac test -p $TUTORIALS_PATH/01-basics/03-impacts

lcaac test -p $TUTORIALS_PATH/02-language-features/01-parametrized-process
lcaac test -p $TUTORIALS_PATH/02-language-features/02-variables
lcaac test -p $TUTORIALS_PATH/02-language-features/03-units
lcaac test -p $TUTORIALS_PATH/02-language-features/04-labels
lcaac test -p $TUTORIALS_PATH/02-language-features/05-datasources

lcaac test -p $TUTORIALS_PATH/03-advanced/01-relational-modeling
lcaac test -p $TUTORIALS_PATH/03-advanced/02-circular-footprint-formula

lcaac test -p $TUTORIALS_PATH/03-advanced/03-project-file/lcaac.yaml main_with_data
lcaac test -p $TUTORIALS_PATH/03-advanced/03-project-file/lcaac-mock.yaml main_with_mock_data

lcaac test -p $TUTORIALS_PATH/03-advanced/04-cached-processes

# Check custom dimensions tutorial
set -euo
# The following assessment is expected to fail.
if lcaac assess -p $TUTORIALS_PATH/02-language-features/03-units customer; then
  exit 0
fi 2> /dev/null
