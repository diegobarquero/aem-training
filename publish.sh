#!/bin/bash

# Validate arguments
if [ "$#" -ne 1 ]; then
  echo "Use: $0 [apps|frontend|both|core]"
  exit 1
fi

# Argument
TARGET=$1

# Validate argument
case "$TARGET" in
  apps)
    echo "Executing apps..."
    cd ./ui.apps
    mvn clean install -PautoInstallPackage
    cd ..
    ;;
  frontend)
    echo "Executing frontend..."
    cd ./ui.frontend
    npm run dev
    cd ..
    ;;
  both)
    echo "Executing frontend & apps..."
    cd ./ui.frontend
    npm run dev
    cd ..
    cd ./ui.apps
    mvn clean install -PautoInstallPackage
    cd ..
    ;;
  core)
      echo "Executing core..."
      cd ./core
      mvn clean install -PautoInstallBundle
      cd ..
      ;;
  *)
    echo "Error: Invalid argument. Use 'core', 'apps', 'frontend' or 'both'."
    exit 1
    ;;
esac