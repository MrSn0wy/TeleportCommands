{
  description = "Flake for developing snowstorm!";

  inputs = {
    nixpkgs.url = "github:nixos/nixpkgs/nixos-unstable";
  };

  outputs =
    {
      nixpkgs,
      ...
    }:
    let
      system = "x86_64-linux";
      pkgs = import nixpkgs {
        inherit system;
      };
      project = "TeleportCommands";
    in
    {
      devShells."${system}" = {
        default = pkgs.mkShell {
          packages = with pkgs; [
            jetbrains.jdk-no-jcef # Jetbrains jdk
            flite # Make mc not complain

            # Took these from https://github.com/NixOS/nixpkgs/blob/nixos-25.05/pkgs/by-name/pr/prismlauncher/package.nix#L123
            # Thanks nixos packagers <3!
            xorg.xrandr

            glfw3-minecraft
            openal

            alsa-lib
            libjack2
            libpulseaudio
            pipewire

            libGL
            libx11
            xorg.libXcursor
            xorg.libXext
            xorg.libXrandr
            xorg.libXxf86vm
          ];

          LD_LIBRARY_PATH =
            with pkgs;
            lib.makeLibraryPath [
              flite # Make mc not complain

              glfw3-minecraft
              openal

              alsa-lib
              libjack2
              libpulseaudio
              pipewire

              libGL
              libx11
              xorg.libXcursor
              xorg.libXext
              xorg.libXrandr
              xorg.libXxf86vm
            ];

          shellHook = ''
            echo -e "\n\x1b[36;1m📦 Welcome to the default flake for \x1b[32;1m${project}\x1b[0m\x1b[36m!\x1b[0m"
          '';
        };
      };
    };
}
