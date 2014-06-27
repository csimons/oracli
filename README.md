# oracli

A portable, lightweight console interface to Oracle databases.

oracli now supports both UNIX and Windows systems (XP and later),
and to a basic extent supports all RDBMS engines for which a JDBC
driver is available.

## Features

- Requires no cumbersome Oracle Client installation
- Runs on both UNIX-based and Windows systems
- Support for SQL, DML, DDL, PLSQL
- Preserves blank lines in code, unlike SQL\*Plus
- dynamic result set pretty-printing;
  no more adjusting linesize, col/format, etc. in SQL\*Plus
- loading and saving external scripts
- query-persistent shell variables
- parameterized queries (shell variables prefixed
  with '&' so SQL\*Plus scripts can be reused)
- external editor support (with rlwrap installed)
- includes JDBC driver (no additional downloads required)
- support for all JDBC-supported database systems

## Planned features

- support for DBMS\_OUTPUT
- support for logging/spooling
- support for object compilation error messages
- source-fetch: fetch creation code behind objects for immediate editing
- smart describe: describe most types of database objects

## Getting Started

You must have JDK 6 or later installed, and your JAVA\_HOME
environment variable set.  It is recommended on UNIX-based
systems that you also have the "rlwrap" utility installed as
this will allow for command history and external editor
support, but it is not required and will work seamlessly
without it.

1. Set the environment variable ORACLI\_HOME to the project directory,
   and add ORACLI\_HOME to your PATH.
2. Populate your databases' connection info into the configuration file
   ("oracli.conf.example"), and copy this to your home configuration
   directory as "oracli.conf".  This will be ~/.config/oracli.conf on
   UNIX-based systems, or %HOMEDRIVE%%HOMEPATH%\\.config\\oracli.conf
   on Windows systems.

The just run as "oracli [dbname]" from the terminal.
For help, issue the /help command from within the program.

## Using with Non-Oracle Database Systems

The JDBC driver for Oracle Database has been included.  To use oracli
with another RDBMS:

1. Copy the appropriate JDBC driver into /lib.
2. Modify src/init-env.clj to load the appropriate Driver class.
3. Add the entry to your oracli.conf; you will need to utilize the
   :custom database type and provide the JDBC URL.

## Licensing

Copyright 2011-2012 Christopher L. Simons

oracli is distributed under the MIT/X Consortium License,
which can be found in the in the file mit-x-consortium.txt
in the licenses/ subdirectory of this distribution.

The Clojure programming environment included with this project
as file clojure-1.4.0.jar is distributed under the Eclipse
Public License, which can be found in the file epl-v10.html in
the licenses/ subdirectory of this distribution.

The Oracle JDBC driver included with this project as file
ojdbc5.jar is distributed under the Oracle Technology
Network Development and Distribution License, which can be
found in the file oracle-otn.txt in the licenses/ subdirectory
of this distribution.
