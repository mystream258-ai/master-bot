/**
 * nodejs-project/main.js - Script de prueba de integración de nodejs-mobile (Node 24 / moderna)
 * Este archivo corre directamente dentro del motor Node embebido en el teléfono.
 */

const fs = require('fs');
const path = require('path');

function runDiagnostics() {
  const report = [];
  report.push('==========================================');
  report.push('NODEJS-MOBILE ENGINE DIAGNOSTIC REPORT');
  report.push('Fecha/Hora: ' + new Date().toISOString());
  report.push('==========================================');

  // 1. Versión de Node y arquitectura
  report.push('\n[1/3] Entorno Node.js:');
  report.push('  • process.version: ' + process.version);
  report.push('  • process.platform: ' + process.platform);
  report.push('  • process.arch: ' + process.arch);
  report.push('  • process.pid: ' + process.pid);

  // 2. crypto.subtle (requerido obligatoriamente por Baileys)
  report.push('\n[2/3] Soporte de WebCrypto (crypto.subtle):');
  try {
    const crypto = require('crypto');
    const subtle = crypto.webcrypto ? crypto.webcrypto.subtle : (globalThis.crypto ? globalThis.crypto.subtle : null);
    if (subtle) {
      report.push('  ✓ crypto.subtle está PRESENTE');
      report.push('  • Métodos disponibles: ' + Object.getOwnPropertyNames(Object.getPrototypeOf(subtle)).filter(m => typeof subtle[m] === 'function').join(', '));
    } else {
      report.push('  ✗ crypto.subtle NO está disponible en este runtime.');
    }
  } catch (err) {
    report.push('  ✗ Error evaluando crypto.subtle: ' + err.message);
  }

  // 3. fetch nativo (requerido por APIs modernas de red)
  report.push('\n[3/3] Soporte de fetch nativo:');
  try {
    if (typeof fetch === 'function') {
      report.push('  ✓ fetch() nativo está DISPONIBLE (tipo: ' + typeof fetch + ')');
    } else {
      report.push('  ✗ fetch() nativo NO está disponible en el entorno global.');
    }
  } catch (err) {
    report.push('  ✗ Error evaluando fetch: ' + err.message);
  }

  report.push('\n==========================================');
  report.push('FIN DEL DIAGNÓSTICO');
  report.push('==========================================');

  const outputText = report.join('\n');
  console.log(outputText);

  // Escribir en archivo dentro del almacenamiento propio para que la app lo lea si no hay IPC activo
  try {
    const outFile = path.join(__dirname, 'node_diagnostics.log');
    fs.writeFileSync(outFile, outputText, 'utf8');
  } catch (e) {
    console.error('No se pudo guardar node_diagnostics.log:', e.message);
  }

  return outputText;
}

// Ejecutar inmediatamente al cargar
runDiagnostics();
