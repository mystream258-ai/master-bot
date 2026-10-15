/**
 * Panel del Bot - Servidor Puente para PC / Servidor Remoto (Modo B)
 * Corre en Node.js v20/v22 en tu PC junto a tu index.js y conecta con la app de Android.
 *
 * Uso:
 *   1. Instala ws: npm install ws
 *   2. Inicia: node bot-server.js
 *   3. En la app Android: Ajustes -> Avanzado -> Dirección: ws://TU_IP_LOCAL:3000
 */

const { WebSocketServer } = require('ws');
const http = require('http');
const fs = require('fs');
const path = require('path');
const { spawn } = require('child_process');

const PORT = process.env.PORT || 3000;
const server = http.createServer((req, res) => {
  res.writeHead(200, { 'Content-Type': 'application/json' });
  res.end(JSON.stringify({ status: 'ok', name: 'PanelBot-Server', version: '1.0' }));
});

const wss = new WebSocketServer({ server });
let botProcess = null;

function broadcast(obj) {
  const data = JSON.stringify(obj);
  wss.clients.forEach(client => {
    if (client.readyState === 1) client.send(data);
  });
}

function startBot() {
  if (botProcess) return;
  const script = path.join(__dirname, 'index.js');
  if (!fs.existsSync(script)) {
    broadcast({ type: 'status', status: 'missing_index', title: 'Falta index.js' });
    return;
  }
  botProcess = spawn('node', [script], { stdio: ['pipe', 'pipe', 'pipe'] });
  broadcast({ type: 'status', status: 'online', title: 'Atendiendo clientes' });

  botProcess.stdout.on('data', (chunk) => {
    const text = chunk.toString();
    broadcast({ type: 'console', text });
    if (/c.digo|code/i.test(text)) {
      const match = text.match(/[A-Za-z0-9]{4}-?[A-Za-z0-9]{4}/);
      if (match) broadcast({ type: 'code', code: match[0].toUpperCase() });
    }
  });

  botProcess.stderr.on('data', (chunk) => {
    broadcast({ type: 'console', text: '[stderr] ' + chunk.toString() });
  });

  botProcess.on('close', (code) => {
    botProcess = null;
    broadcast({ type: 'status', status: 'stopped', title: 'Bot detenido (código ' + code + ')' });
  });
}

function stopBot() {
  if (botProcess) {
    botProcess.kill();
    botProcess = null;
  }
  broadcast({ type: 'status', status: 'stopped', title: 'Bot detenido' });
}

wss.on('connection', (ws) => {
  console.log('[+] App Android conectada');
  ws.send(JSON.stringify({
    type: 'status',
    status: botProcess ? 'online' : 'stopped',
    title: botProcess ? 'Atendiendo clientes' : 'Bot detenido'
  }));

  ws.on('message', (msg) => {
    try {
      const data = JSON.parse(msg.toString());
      if (data.action === 'start') startBot();
      if (data.action === 'stop') stopBot();
      if (data.action === 'command' && botProcess) {
        botProcess.stdin.write(data.text + '\n');
      }
    } catch (e) {
      console.error('Error procesando mensaje:', e);
    }
  });
});

server.listen(PORT, '0.0.0.0', () => {
  console.log(`[Panel del Bot] Servidor puente activo en http://0.0.0.0:${PORT}`);
  console.log(`Conecta tu teléfono a la misma red WiFi y pon en la app: ws://<IP_DE_ESTA_PC>:${PORT}`);
});
