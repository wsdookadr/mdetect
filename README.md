
Overview
========

As with any software, this too has limitations and a certain range of
applicability.
The goal of mdetect is to detect potentially malicious PHP code. To reach
that goal, it parses the PHP code, it checks its provenance by looking
up checksums in a datastore, for the PHP code of unknown provenance,
it runs different heuristics to assess if the code contains logic that
might be malicious.

The use-cases for this tool are the following:
- as a preventive check on file upload
- in the aftermath of a break-in, for cleanup purposes
- as a peridoic check of existing code on a machine, to assess the existance
  of potentially suspicious behaviour


Details
=======

In tests, building and storing the parse trees had a peak memory usage of 4G memory.
Building MAST was found to take 4.3G memory.

Contributing
============

This code is released under the MIT license.


