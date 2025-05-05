{
  description = "LCAAC flake";
  outputs = inputs@{ nixpkgs, ...}:
  {
    devShells = {
      aarch64-darwin.default =
      let
        pkgs = nixpkgs.legacyPackages.aarch64-darwin;
      in
      pkgs.mkShell {
        name = "lcaac-dev-shell";
    	packages = with pkgs; [
    	  jdk23
    	  gradle
    	];
      };
    };
  };
}
