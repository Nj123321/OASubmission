# Project Stucture:
- LogAnalysis class in src/main/java/ is solution, MainTest class is an easy way to run
  the solution by analyzing data logs in data/logs/, lookup table in data/lookup.csv,
  AWS protocol mapping in data/protocol-numbers.csv, and outputting to output.txt
- tests are in src/test/ with data & output in data/test. The tests that were done include 
  checking for correctness (with invalid logs, different protocols, multiple mappings),
  checking for fileoutput collisions, checking for empty filelogs, and invalid input 
  (just Junit dependency is used for testing - hopefully ok :) )
- NOTE: when running LogParsing solution multiple times, make sure to delete the previous outputfile
   or change the outputfile name. This is because LogParsing is designed not to allow overriding
   previous summaries for safety.

# Additional Assumptions:
- Everything - the log file, look up file - are all contained locally
- the second output requirement "Count of matches for each port/protocol combination" is the count for each unique
   port/protocol combination in the logs, regardless of if it is found in the lookup table or not.
   (the test cases provided in the technical assessment email did not exactly give a clear definition)
   For example: In the email, 22,tcp,1 and sv_P4 were listed in the output, but nowhere in the logs is there an entry
   with port 22. Additionally, sum(counts of tag| 7 tagged, 9 untagged) != sum(counts of pairs 10) != sum(total logs 14).
   But, if being in the lookup table matters, than just put a condition before updating the count
   table checking if it was added to the tagcount previously: if (!tag.equals("Untagged")))
- Assumed the "port" in Port/Protocol pairs refers to dstport
- the entries in lookuptable are all valid and correct
- performance on 10MB using one thread is acceptable, possible further optimization with multithreading is possible but needs time/tuning
- log entries are formatted in documentation defaults version 2 - dstport index will be at 6, and protocol will be 7
- Invalid log records(corrupt or missing dstport/protocol values) should be ignored and logged
- logfile does not have leading headers, but match files does and only one line
- aws log protocol name-integer mapping is downloaded from documentation https://www.iana.org/assignments/protocol-numbers/protocol-numbers.xhtml as data/protocol-numbers.csv
  (and is cleaned to fit csv file format - there were some formatting issues when I downloaded it)
- order of output doesn't matter
- aws flow logs should not start with a leading space in each record
- lookup table stored as csv (I assume tag mappings file is the lookup table, which is stored as csv)
