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

            buildInputs = [ pkgs.glew pkgs.glfw ];

            outputs = [ "out" "static" ];
            buildPhase = ''
              gcc -c -o nkc.o -D NKC_USE_OPENGL=3 -D NKCD=NKC_GLFW nuklear_cross.c
            '';
            installPhase = ''
              mkdir -p $out/include
              cp -r nkc_frontend nuklear_drivers nuklear.h nuklear_cross.h stb_image.h $out/include
              cp nkc.o $static
            '';
          };
          smoke-test = pkgs.stdenv.mkDerivation rec {
            pname = "experiment";
            version = "0.0";
            src = ./.;

            buildInputs = [
              scopespkgs.scopes
              pkgs.libdevil
              selfpkgs.nkc
              pkgs.glew
              pkgs.glfw
              pkgs.libglvnd
              # pkgs.glibc
            ];

            buildPhase = ''
              SCOPES_CACHE=$(pwd)/scopes-cache scopes smoke-test/build.sc
              gcc -o main main.o ${selfpkgs.nkc.static} -lglfw -lGLEW -lm -lGL
            '';

            installPhase = ''
              mkdir -p $out/bin
              install main $out/bin/smoke-test

            '';

            LD_LIBRARY_PATH = pkgs.lib.concatMapStringsSep ":"
              (lib: "${pkgs.lib.getLib lib}/lib") buildInputs;
          };
        });

      defaultPackage =
        forAllSystems (system: self.packages.${system}.smoke-test);

      devShell = forAllSystems (system:
        self.packages.${system}.smoke-test.overrideAttrs (old: {
          buildInputs = old.buildInputs ++ [ nixpkgsFor.${system}.gdb ];
        }));

    };
}
