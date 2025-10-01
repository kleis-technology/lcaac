{
  inputs = {
    nixpkgs.url = "github:nixos/nixpkgs/nixos-25.05";
    flake-utils.url = "github:numtide/flake-utils";
  };
  description = "LCAAC flake";
  outputs = inputs@{ nixpkgs, flake-utils, ...}:
    flake-utils.lib.eachDefaultSystem (
      system:
      let
        pkgs = nixpkgs.legacyPackages.${system};
      in
      {
        devShells.default = pkgs.mkShell {
          name = "lcaac-dev-shell";
          packages = with pkgs; [
            jdk17
            gradle
          ];
        };
      }
    );
}
