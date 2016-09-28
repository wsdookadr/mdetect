Overview
========

As with any software, this too has limitations and a certain range of
applicability.  The goal of mdetect is to detect potentially malicious
PHP code. To reach that goal, it parses the PHP code, it checks its
provenance by looking up checksums in a datastore, for the PHP code of
unknown provenance, it runs different heuristics to assess if the code
contains logic that might be malicious.

The high-level use-cases for this tool are the following:
- in the aftermath of a break-in, for cleanup purposes
- as a periodic check of existing code on a machine, to assess the existance
  of potentially suspicious behaviour

The more specific use-cases are:
- checking files checksums against a database of known checksums
- finding files that might be malicious according to certain rules
- finding files that have the same structure as a given file

See [this post](https://blog.garage-coding.com/2016/09/01/detecting-potentially-malicious-php-code-using-parsers-and-heuristics.html) for more details.

Install
=======

You need openjdk8 installed and maven. Running the following should pull all the
dependencies and build mdetect:

For CentOS install jdk and maven, then build the project:

    yum install maven30.x86_64
    yum install java-1.8.0-openjdk.x86_64
    mvn clean package

For Ubuntu:

    apt-get install openjdk-8-jdk
    apt-get install maven
    mvn clean package

Usage
=====

This will generate checksums for `/home/user/php-codebases/` (these will
be stored in `$HOME/.mdetect.db` ) . The files we compute checksums for
are considered known files and will be skipped during detection.

    ./mdetect -c /path/to/files/known/to/be/safe

This will process the files in `/path/to/files/to/scan`, the files will
be parsed and their parse trees will be stored in `BaseX`. These will
be used in analysis.

    ./mdetect -d /path/to/files/to/scan

To get a report of the potentially malicious files run:

    ./mdetect -r

(this will act on files analyzed when `-d` was run;
 no files with known checksums will be checked, that is, the checksums
 collected when `-c` was run)

Details
=======

Checksums will be stored in `~/.mdetect.db` which is an sqlite flat-file
database.  The parse trees will be stored in the `xtrees` BaseX database
located at `~/BaseXData` (the default location for all BaseX databases).

During tests, the `~/BaseXData/` directory (where BaseX stores its
databases) was found to be 10 times larger than the size of the PHP
code parsed.

Some performance metrics after parsing and storing parse trees for the
entire Joomla 3.6.2 codebase :

| name                   | value          |
| ---------------------- | -------------- |
| processing speed       | 136.71875 kb/s |
| peak memory usage      | 2520 MB        |
| time spent             | 110 seconds    |
| processed data size    | 14 MB          |

Additional note: Parsing certain large PHP files (1MB in size) was found
to cause a memory usage of 4GB.

You may run `./deps/basexgui` which will bring up a GUI including an
editor where you can run custom XQuery queries.

Contributing
============

This code is very much a work in progress. You're free to submit pull
requests with improvements. Please review open issues before posting
new ones.

This code is released under the MIT license.

