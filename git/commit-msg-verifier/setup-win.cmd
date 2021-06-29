@rem
@rem Copyright 2019. http://devonline.academy
@rem
@rem Licensed under the Apache License, Version 2.0 (the "License");
@rem you may not use this file except in compliance with the License.
@rem You may obtain a copy of the License at
@rem
@rem     http://www.apache.org/licenses/LICENSE-2.0
@rem
@rem Unless required by applicable law or agreed to in writing, software
@rem distributed under the License is distributed on an "AS IS" BASIS,
@rem WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@rem See the License for the specific language governing permissions and
@rem limitations under the License.
@rem ###################################################################################################################
@rem The cmd script for installing the commit-msg hook for Windows systems
@rem
@rem @author devonline
@rem @email  devonline.academy@gmail.com
@rem ###################################################################################################################
@rem Install instructions:
@rem
@rem cd /path/to/the/git/local/repository
@rem set SCRIPT=https://raw.githubusercontent.com/devonline-academy/tools/master/git/commit-msg-verifier/setup-win.cmd
@rem wget %SCRIPT% -O %TMP%\setup.cmd -q && %TMP%\setup.cmd -q && del /f /q %TMP%\setup.cmd
@rem ###################################################################################################################
@rem Disable system output
@echo off
@rem -------------------------------------------------------------------------------------------------------------------
@rem Define local variables
setlocal
@rem -------------------------------------------------------------------------------------------------------------------
set HOOK_STORAGE_ROOT_URL=https://raw.githubusercontent.com/devonline-academy/tools/master/git/commit-msg-verifier
set HOOK_SCRIPT_NAME=CommitMsgVerifier
set VERBS_FILE_NAME=.verbs
@rem -------------------------------------------------------------------------------------------------------------------
set DOWNLOAD_HOOK_SCRIPT_URL=%HOOK_STORAGE_ROOT_URL%/src/main/java/%HOOK_SCRIPT_NAME%.java
set DOWNLOAD_VERBS_FILE_URL=%HOOK_STORAGE_ROOT_URL%/%VERBS_FILE_NAME%
@rem -------------------------------------------------------------------------------------------------------------------
set HOOKS_DIR=.git\hooks
set HOOK_NAME=commit-msg
@rem -------------------------------------------------------------------------------------------------------------------
if "%1" == "-q" (
    set WGET_QUITE=-q
)
@rem -------------------------------------------------------------------------------------------------------------------
@rem -------------------------------------------------------------------------------------------------------------------
@rem -------------------------------------------------------------------------------------------------------------------
@rem Verify that the current dir is a git repository:
if not exist %HOOKS_DIR% (
    echo ------------------------------------------------------------------------ >&2
    echo '%HOOKS_DIR%' not found. Is the '%CD%' a local git repository? >&2
    echo ------------------------------------------------------------------------ >&2
    exit /b 1
)
@rem -------------------------------------------------------------------------------------------------------------------
@rem Verify that the commit-msg hook is not installed:
set HOOK_PATH=%HOOKS_DIR%\%HOOK_NAME%
if exist %HOOK_PATH% (
    echo ------------------------------------------------------------------------ >&2
    echo '%HOOK_PATH%' already exists. Skip installation. >&2
    echo ------------------------------------------------------------------------ >&2
    exit /b 2
)
@rem -------------------------------------------------------------------------------------------------------------------
@rem Verify that the wget is available:
where /Q wget
if %ERRORLEVEL% NEQ 0 (
    echo ------------------------------------------------------------------------ >&2
    echo Download "wget.exe" and add it to the "PATH" variable, before using this script! >&2
    echo ------------------------------------------------------------------------ >&2
    exit /b 3
)
@rem -------------------------------------------------------------------------------------------------------------------
@rem Verify that the javac is available:
where /Q javac
if %ERRORLEVEL% NEQ 0 (
    echo ------------------------------------------------------------------------ >&2
    echo Download "javac.exe" and add it to the "PATH" variable, before using this script! >&2
    echo ------------------------------------------------------------------------ >&2
    exit /b 4
)
@rem -------------------------------------------------------------------------------------------------------------------
@rem Verify that the java is available:
where /Q java
if %ERRORLEVEL% NEQ 0 (
    echo ------------------------------------------------------------------------ >&2
    echo Download "java.exe" and add it to the "PATH" variable, before using this script! >&2
    echo ------------------------------------------------------------------------ >&2
    exit /b 5
)
@rem -------------------------------------------------------------------------------------------------------------------
@rem Download the source code for CommitMsgVerifier class
wget -O %HOOKS_DIR%\%HOOK_SCRIPT_NAME%.java %WGET_QUITE% %DOWNLOAD_HOOK_SCRIPT_URL%
cd %HOOKS_DIR%
@rem Compile the CommitMsgVerifier class
javac %HOOK_SCRIPT_NAME%.java
@rem Create the commit-msg hook
(
echo #!/bin/sh
echo java -cp .git/hooks/ %HOOK_SCRIPT_NAME% $1
) > commit-msg
@rem -------------------------------------------------------------------------------------------------------------------
@rem Download the init version of '.verbs' file to the %HOMEPATH% directory if not found
set VERBS_FILE_PATH=%HOMEPATH%\%VERBS_FILE_NAME%
if not exist %VERBS_FILE_PATH% (
    @rem Download the init version of '.verbs' file to the %HOMEPATH% directory
    wget -O %VERBS_FILE_PATH% %WGET_QUITE% %DOWNLOAD_VERBS_FILE_URL%
    @rem Set hidden attribute
    attrib +h %VERBS_FILE_PATH%
    @rem Print success message
    echo ------------------------------------------------------------------------
    echo Init version of the '%VERBS_FILE_NAME%' file downloaded successful.
)
@rem -------------------------------------------------------------------------------------------------------------------
@rem Back to local git repository directory
cd ../../
@rem Show the success message
echo ------------------------------------------------------------------------
echo '%HOOK_NAME%' installed successful.
echo ------------------------------------------------------------------------
@rem -------------------------------------------------------------------------------------------------------------------
endlocal
