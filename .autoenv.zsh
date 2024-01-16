#!/usr/bin/env bash

export GIT_ROOT=$(git rev-parse --show-toplevel)
function lcaac() {
    $GIT_ROOT/cli/build/install/cli/bin/cli $@
}
