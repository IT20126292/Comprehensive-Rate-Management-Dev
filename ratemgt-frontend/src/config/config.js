const defaultProtocol = window.location.protocol === 'https:' ? 'https' : 'http';
const defaultBackendPort = defaultProtocol === 'https' ? 8443 : 8080;

const config = {
  API_BASE: window._env_?.API_BASE || `${defaultProtocol}://localhost:${defaultBackendPort}/api/segment`,
  RATES_API_BASE: window._env_?.RATES_API_BASE || `${defaultProtocol}://localhost:${defaultBackendPort}/api/exrates`,
  AUTH_API_BASE: window._env_?.AUTH_API_BASE || `${defaultProtocol}://localhost:${defaultBackendPort}/api/auth`,
  EXC_API_BASE: window._env_?.EXC_API_BASE || `${defaultProtocol}://localhost:${defaultBackendPort}/api/rates`
};

export default config;