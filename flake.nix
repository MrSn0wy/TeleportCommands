{
  description = "Flake for developing TeleportCommands!";

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
            jetbrains.jdk-no-jcef-21 # Jetbrains jdk
            just # for the justfile
            flite # Make mc not complain

            # Took these from https://github.com/NixOS/nixpkgs/blob/master/pkgs/by-name/pr/prismlauncher/package.nix#L89
            # Thanks nixos packagers <3!
            xrandr

            glfw3-minecraft
            openal

            alsa-lib
            libjack2
            libpulseaudio
            pipewire

            libGL
            libx11
            libXcursor
            libXext
            libXrandr
            libXxf86vm

            wayland
            libdecor

            udev # oshi

            vulkan-loader # VulkanMod's lwjgl
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
              libXcursor
              libXext
              libXrandr
              libXxf86vm
            ];

          shellHook = ''
            echo -e "\n\x1b[36;1m📦 Welcome to the default flake for \x1b[32;1m${project}\x1b[0m\x1b[36m!\x1b[0m"
          '';
        };
      };
    };
}
