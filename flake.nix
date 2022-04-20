{
  description = "A very basic flake";

  inputs = {
    nixpkgs.url = "github:nixos/nixpkgs/nixos-21.11";
    scopes.url = "github:fundament-software/scopes";
    scopes.inputs.nixpkgs.follows = "nixpkgs";
    nuklear-cross.url = "github:DeXP/nuklear_cross";
    nuklear-cross.flake = false;
  };

  outputs = { self, nixpkgs, scopes, nuklear-cross }:
    let
      supportedSystems = [ "x86_64-linux" "aarch64-linux" ];
      forAllSystems = nixpkgs.lib.genAttrs supportedSystems;
      nixpkgsFor = forAllSystems (system: import nixpkgs { inherit system; });

    in {

      packages = forAllSystems (system:
        let
          pkgs = nixpkgsFor.${system};
          selfpkgs = self.packages.${system};
          scopespkgs = scopes.packages.${system};
        in {
          nkc = pkgs.stdenv.mkDerivation {
            pname = "nuklear_cross";
            version = "1.0";
            src = nuklear-cross;

            dontBuild = true;
            installPhase = ''
              mkdir -p $out/include
              cp -r nkc_frontend nuklear_drivers nuklear.h nuklear_cross.h stb_image.h $out/include
            '';
          };
          experiment = pkgs.stdenv.mkDerivation rec {
            pname = "experiment";
            version = "0.0";
            src = ./.;

            buildInputs = [
              scopespkgs.scopes
              pkgs.libdevil
              selfpkgs.nkc
              pkgs.glew
              pkgs.glfw
              # pkgs.glibc
            ];

            LD_LIBRARY_PATH = pkgs.lib.concatMapStringsSep ":"
              (lib: "${pkgs.lib.getLib lib}/lib") buildInputs;
          };
        });

      defaultPackage =
        forAllSystems (system: self.packages.${system}.experiment);

      devShell = forAllSystems (system:
        self.packages.${system}.experiment.overrideAttrs (old: {
          buildInputs = old.buildInputs ++ [ nixpkgsFor.${system}.gdb ];
        }));

    };
}
