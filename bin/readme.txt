ABOUT THE BIN DIRECTORY
=======================

This directory contains scripts for using Program D.
Generally speaking, there are two versions of each
script: one is a DOS/Windows batch file, and the other
is a *nix shell script.  Both versions are named so that,
on either operating system, you should be able to type
just the name of the script to run it.  You can also
execute the scripts starting from any directory, referring
to them by whatever path is necessary.  For instance,

./server

and

bin/server

should both work fine under *nix, starting from within
this directory and just above it, respectively.  Similarly,

server

and

bin\server

should both work fine under DOS/Windows starting from
the same respective locations.

The "common_functions.sh" file contains several functions
that are used by the *nix scripts.  The file itself is not
meant to be executable.

The "dos" subdirectory here contains a number of batch
files that are used by the main scripts.  This is necessary
because DOS/Windows doesn't support user-defined functions.
You will probably not need to directly execute the scripts
located in the "dos" subdirectory.
