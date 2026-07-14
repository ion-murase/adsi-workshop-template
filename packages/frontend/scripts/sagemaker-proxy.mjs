import http from 'node:http';

const LISTEN_PORT = 3000;
const TARGET_PORT = 3001;
const RESTORE_PREFIX = '/codeeditor/default';

const server = http.createServer((req, res) => {
  // code-server(8888) strips "/codeeditor/default" and forwards "/absports/3000/..."
  // Restore the prefix so Next.js (basePath=/codeeditor/default/absports/3000) can match
  const url = RESTORE_PREFIX + req.url;

  const options = {
    hostname: '127.0.0.1',
    port: TARGET_PORT,
    path: url,
    method: req.method,
    headers: req.headers,
  };

  const proxyReq = http.request(options, (proxyRes) => {
    res.writeHead(proxyRes.statusCode, proxyRes.headers);
    proxyRes.pipe(res, { end: true });
  });

  proxyReq.on('error', (err) => {
    console.error('Proxy error:', err.message);
    res.writeHead(502);
    res.end('Bad Gateway');
  });

  req.pipe(proxyReq, { end: true });
});

server.listen(LISTEN_PORT, '0.0.0.0', () => {
  console.log(`SageMaker proxy listening on :${LISTEN_PORT} -> :${TARGET_PORT} (restore prefix: ${RESTORE_PREFIX})`);
});
