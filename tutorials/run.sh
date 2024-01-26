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

# Check custom dimensions tutorial
set -euo

# The following assessment is expected to fail.
if lcaac assess -p $TUTORIALS_PATH/02-language-features/03-units customer; then
  exit 0
fi
