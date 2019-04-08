# CN-http-client

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
