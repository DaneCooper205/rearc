This java application downloads 2 datasets from https://download.bls.gov/pub/time.series/pr/ 
and uploads them to an S3 bucket.  The application takes the stream and holds it temporarily 
in a List<String>. Then it opens a stream to upload to S3. This was done so that the size 
of the stream would be known for the upload process.

There is also an API call that retrieves a JSON object and uploads it to S3 as well.
The dataset URL was throwing a Bad Gateway error. I tested it against a quick api that I cobbled 
together and it was successful.

It does still takes the hard coded file names and still reloads the files every time. Since the S3 bucket 
is not set up for versioning, this results in an overwrite. 
