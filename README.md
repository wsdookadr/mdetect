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

Contributing
============

This code is released under the MIT license.


