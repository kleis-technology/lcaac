{ pkgs, config, ... }:

{
  # Java 17, as in the CI
  languages.java = {
    enable = true;
    jdk.package = pkgs.jdk17;
    gradle.enable = true;
  };

  env.GIT_ROOT = config.devenv.root;
}
