#!/usr/bin/env bash
#
# Copyright 2019. http://devonline.academy
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
########################################################################################################################
# The bash script for installing the commit-msg hook for Linux systems                                                 #
#                                                                                                                      #
# @author devonline                                                                                                    #
# @email  devonline.academy@gmail.com                                                                                  #
########################################################################################################################
# Install instructions:                                                                                                #
#                                                                                                                      #
# cd /path/to/the/git/local/repository                                                                                 #
# SCRIPT=https://raw.githubusercontent.com/devonline-academy/tools/master/git/commit-msg-verifier/setup-lin.sh         #
# wget $SCRIPT -O /tmp/setup -q && chmod 755 /tmp/setup && /tmp/setup -q && rm -rf /tmp/setup                          #
########################################################################################################################
HOOK_STORAGE_ROOT_URL=https://raw.githubusercontent.com/devonline-academy/tools/master/git/commit-msg-verifier
HOOK_SCRIPT_NAME=CommitMsgVerifier
VERBS_FILE_NAME=.verbs
# ----------------------------------------------------------------------------------------------------------------------
DOWNLOAD_HOOK_SCRIPT_URL=${HOOK_STORAGE_ROOT_URL}/src/main/java/${HOOK_SCRIPT_NAME}.java
DOWNLOAD_VERBS_FILE_URL=${HOOK_STORAGE_ROOT_URL}/${VERBS_FILE_NAME}
# ----------------------------------------------------------------------------------------------------------------------
HOOKS_DIR=.git/hooks
HOOK_NAME=commit-msg
# ----------------------------------------------------------------------------------------------------------------------
# Exit when any command fails
set -eu -o pipefail
# ----------------------------------------------------------------------------------------------------------------------
if [[ $# -eq 0 ]]; then
  WGET_QUITE=""
elif [[ $1 == "-q" ]]; then
  WGET_QUITE="-q"
else
  WGET_QUITE=""
fi
# ----------------------------------------------------------------------------------------------------------------------
# Verify that the current dir is a git repository:
if [[ ! -d "$HOOKS_DIR" ]]; then
  echo "------------------------------------------------------------------------" >&2
  echo "'${HOOKS_DIR}' not found. Is the '${PWD}' a git repository?" >&2
  echo "------------------------------------------------------------------------" >&2
  exit 1
fi
# ----------------------------------------------------------------------------------------------------------------------
# Verify that the commit-msg hook is not installed:
HOOK_PATH=${HOOKS_DIR}/${HOOK_NAME}
if [[ -f "$HOOK_PATH" ]]; then
  echo "------------------------------------------------------------------------" >&2
  echo "'${HOOK_PATH}' already exists. Skip installation." >&2
  echo "------------------------------------------------------------------------" >&2
  exit 2
fi
# ----------------------------------------------------------------------------------------------------------------------
# Verify that the wget is available:
set +e
WGET_CMD=$(which wget)
set -e
if [[ ! -x "$WGET_CMD" ]]; then
  echo "------------------------------------------------------------------------" >&2
  echo "Download and add \"wget\" to the \"PATH\" variable, before using this script!" >&2
  echo "------------------------------------------------------------------------" >&2
  exit 3
fi
# ----------------------------------------------------------------------------------------------------------------------
# Verify that the javac is available:
set +e
JAVAC_CMD=$(which javac)
set -e
if [[ ! -x "$JAVAC_CMD" ]]; then
  echo "------------------------------------------------------------------------" >&2
  echo "Download and add \"javac\" to the \"PATH\" variable, before using this script!" >&2
  echo "------------------------------------------------------------------------" >&2
  exit 4
fi
# ----------------------------------------------------------------------------------------------------------------------
# Verify that the java is available:
set +e
JAVA_CMD=$(which java)
set -e
if [[ ! -x "$JAVA_CMD" ]]; then
  echo "------------------------------------------------------------------------" >&2
  echo "Download and add \"java\" to the \"PATH\" variable, before using this script!" >&2
  echo "------------------------------------------------------------------------" >&2
  exit 5
fi
# ----------------------------------------------------------------------------------------------------------------------
# Download the source code for CommitMsgVerifier class
wget -O ${HOOKS_DIR}/${HOOK_SCRIPT_NAME}.java ${WGET_QUITE} ${DOWNLOAD_HOOK_SCRIPT_URL}
cd ${HOOKS_DIR}
# Compile the CommitMsgVerifier class
javac ${HOOK_SCRIPT_NAME}.java
# Create the commit-msg hook
{
echo "#!/bin/sh"
echo "java -cp .git/hooks/ ${HOOK_SCRIPT_NAME} \$1"
} > commit-msg
# Make the commit-msg executable
chmod 755 commit-msg
# ----------------------------------------------------------------------------------------------------------------------
# Download the .verbs to the $HOME directory if not found
VERBS_FILE_PATH=${HOME}/${VERBS_FILE_NAME}
if [[ ! -f "$VERBS_FILE_PATH" ]]; then
  # Download the .verbs to the $HOME directory
  wget -O "${VERBS_FILE_PATH}" ${WGET_QUITE} ${DOWNLOAD_VERBS_FILE_URL}
  # Print success message
  echo "------------------------------------------------------------------------"
  echo "Init version of the '${VERBS_FILE_NAME}' file downloaded successful."
fi
# ----------------------------------------------------------------------------------------------------------------------
# Show the success message
echo "------------------------------------------------------------------------"
echo "'${HOOK_NAME}' installed successful."
echo "------------------------------------------------------------------------"
