Overview
========

As with any software, this too has limitations and a certain range of
applicability.
The goal of mdetect is to detect potentially malicious PHP code. To reach
that goal, it parses the PHP code, it checks its provenance by looking
up checksums in a datastore, for the PHP code of unknown provenance,
it runs different heuristics to assess if the code contains logic that
might be malicious.

The high-level use-cases for this tool are the following:
- as a preventive check on file upload
- in the aftermath of a break-in, for cleanup purposes
- as a peridoic check of existing code on a machine, to assess the existance
  of potentially suspicious behaviour

The more specific use-cases are:
- building a whitelist checksum database of a set of codebases
- finding files that might be malicious according to certain rules
- finding files that have the same structure as a given file

Details
=======

In tests, building and storing the parse trees had a peak memory usage of 4G memory.
Building MAST was found to take a maximum of 4.3G.
Parsing certain files has also been found to reach 4G memory usage.

Usage
=====


This will generate checksums for `/home/user/php-codebases/` (these will be stored in
`$HOME/.mdetect.db` ) . The files we compute checksums for are considered known
files and will be skipped during detection.

    ./mdetect -c /home/user/php-codebases/

This will process the files in `/home/user/other-code/`, the files will be parsed
and their parse trees will be stored in `BaseX`. These will be used in analysis.

    ./mdetect -d /home/user/other-code/

Currently, some checks are available in `resources/` in the form of `XQuery` programs.
For example, you may run the following check for function call usage:

    ./deps/basexclient -U admin -P admin -p 1984 ./resources/fcall_check.xql

The server should be started for this query to work (the server runner program is in
`./deps/basexserver`).
This will provide information function names and the number of calls, for each file
that is stored in `BaseX`.

Contributing
============

You're free to submit pull requests with improvements. Existing issues take 
priority over new ones.

This code is released under the MIT license.

