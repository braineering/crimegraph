var http = require('http')
var fs = require('fs')

http.createServer(function(req, res) {
  res.writeHead(200, {'Content-Type': 'text/html'})
  fs.createReadStream('./test/dev-server/index.html').pipe(res)
}).listen(54741)
