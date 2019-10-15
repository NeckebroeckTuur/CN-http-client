# CN-http-client
A pure-java CLI HTTP client application written as an assignment for the Computer Networks (G0Q43A) course at KU Leuven.
The client can send get, head, post and put requests to a given website or IP address and a given port.
With post and put requests, a custom body can be added.
Both fixed-length and chunked responses can be read.

## Usage
`java ./bin/ClientMain [GET/HEAD/POST/PUT] [URI] [PORT]`

For example:
`java ./bin/ClientMain GET http://webs.cs.berkeley.edu/tos 80`

## Examples
Example requests can be found in `./example_requests.txt`

## TODO
- [ ] Fully implement CacheManager
- [ ] Implement if-modified-since header
- [ ] Opening extra socket and sending request if an external source is requested
