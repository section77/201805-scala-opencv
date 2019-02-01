#
# - this project uses javafx, so it needs oraclejdk 8
#
# - oraclejdk needs:
#
#      oraclejdk.accept_license = true;
#
#   in ~/.config/nixpkgs/config.nix
#
let


  inherit (import <nixpkgs> {}) fetchFromGitHub;

  # cmd:
  #   nix-prefetch-git https://github.com/mozilla/nixpkgs-mozilla
  #
  # Commit date is 2019-01-25 17:40:39 +0100
  nixpkgs-mozilla = fetchFromGitHub {
    owner = "mozilla";
    repo = "nixpkgs-mozilla";
    rev = "507efc7f62427ded829b770a06dd0e30db0a24fe";
    sha256 = "17p1krbs6x6rnz59g46rja56b38gcigri3h3x9ikd34cxw77wgs9";
  };

  # cmd:
  #   nix-prefetch-git https://github.com/NixOS/nixpkgs-channels --rev refs/heads/nixos-18.09
  #
  # Commit date is 2019-01-23 07:13:01 -0500
  nixpkgs = fetchFromGitHub {
    owner = "NixOS";
    repo = "nixpkgs-channels";
    rev = "749a3a0d00b5d4cb3f039ea53e7d5efc23c296a2";
    sha256 = "14dqndpxa4b3d3xnzwknjda21mm3p0zmk8jbljv66viqj5plvgdw";
  };

  pkgs = import nixpkgs {};

  my-opencv3 = pkgs.opencv3.override {
    enableContrib = true;
  };

  my-opencv3-java = my-opencv3.overrideAttrs( origAttrs: rec {
    name = "${origAttrs.name}-java";
    cmakeFlags = origAttrs.cmakeFlags ++ [
      "-DBUILD_opencv_dnn=OFF"
      "-DBUILD_SHARED_LIBS=OFF"
    ];
    buildInputs = origAttrs.buildInputs ++ [ pkgs.ant ];
    propagatedBuildInputs = origAttrs.propagatedBuildInputs ++
      [ pkgs.oraclejdk pkgs.python3Full ];
  });

in pkgs.mkShell rec {

  buildInputs = with pkgs; [
    my-opencv3-java ant
    oraclejdk
    sbt
  ];

  shellHook = ''
      export JAVA_HOME="${pkgs.oraclejdk}/bin"
      export JDK_HOME=$JAVA_HOME
      export PATH=$JAVA_HOME/bin:$PATH

      # use oraclejdk for javafx support
      alias sbt="${pkgs.oraclejdk}/bin/java \
        -XX:+CMSClassUnloadingEnabled -XX:MaxMetaspaceSize=512M -XX:MetaspaceSize=256M -Xmx2G \
        -jar ${pkgs.sbt}/share/sbt/bin/sbt-launch.jar"
  '';
}
