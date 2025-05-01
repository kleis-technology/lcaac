set -gx GIT_ROOT (git rev-parse --show-toplevel)

function lcaac
  $GIT_ROOT/cli/build/install/lcaac/bin/lcaac $argv
end
