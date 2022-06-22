#!/bin/bash

# Oracle/OpenJDK JRE version 1.8+ has to be present on your path
# OR in a directory called "jre" in the same directory as this script

# detect real location of this script, regardless aliases and symlinks
if [[ "$OSTYPE" == "darwin"* ]]; then
    REAL_SCRIPT_PATH=$(python -c 'import os,sys;print(os.path.realpath(sys.argv[1]))' "$0")
else
    REAL_SCRIPT_PATH=$(readlink -f "$0")
fi

SCRIPT_DIR=$(dirname "$REAL_SCRIPT_PATH")

if [ -d "${SCRIPT_DIR}/jre" ]; then
    "${SCRIPT_DIR}/jre/bin/java" -cp '${project.artifactId}-${project.version}.jar:lib/*:jdbc/*:plugins/*' de.eztools.ezdb.shell.ShellApp "$@"
else
    java -cp '${project.artifactId}-${project.version}.jar:lib/*:jdbc/*:plugins/*' de.eztools.ezdb.shell.ShellApp "$@"
fi
