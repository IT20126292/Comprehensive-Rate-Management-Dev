import axios from 'axios';
import config from '../config/config';

// Axios global defaults for CSRF + credentials
axios.defaults.withCredentials = true;
axios.defaults.xsrfCookieName = 'XSRF-TOKEN';
axios.defaults.xsrfHeaderName = 'X-XSRF-TOKEN';

// Ensure CSRF header is sent even for cross-origin (different port) requests
function getCookie(name) {
  const value = `; ${document.cookie}`;
  const parts = value.split(`; ${name}=`);
  if (parts.length === 2) return parts.pop().split(';').shift();
}

axios.interceptors.request.use((cfg) => {
  try {
    const method = (cfg.method || 'get').toLowerCase();
    if ([ 'post', 'put', 'patch', 'delete' ].includes(method)) {
      const token = getCookie('XSRF-TOKEN');
      if (token) {
        cfg.headers = cfg.headers || {};
        if (!cfg.headers['X-XSRF-TOKEN']) {
          cfg.headers['X-XSRF-TOKEN'] = token;
        }
      }
    }
    cfg.withCredentials = true;
  } catch (_) {}
  return cfg;
});

const API_BASE = config.API_BASE;
const RATES_API_BASE = config.RATES_API_BASE;
const AUTH_API_BASE = config.AUTH_API_BASE;
const EXC_API_BASE = config.EXC_API_BASE;

export const authLogin = (formData) => axios.post(`${AUTH_API_BASE}/login`, formData);

export const runSegmentation = () => axios.post(`${API_BASE}/run`);

export const fetchSegmentResults = () => axios.get(`${API_BASE}/report`);

export const saveDefineSegment = (segment) => axios.post(`${API_BASE}/save`, segment);

export const fetchSegmentByCode = (code) => axios.get(`${API_BASE}/${code}`);

export const fetchCurrenciesByDate = (date) => axios.get(`${RATES_API_BASE}/currencies/${date}`);

export const insertCurrenciesByDate = (date) => axios.post(`${RATES_API_BASE}/fetch/${date}`);

export const searchRateRequests = (date) => axios.get(`${RATES_API_BASE}/search/${date}`);

export const fetchAllRateRequests = () => axios.get(`${EXC_API_BASE}/all`);

export const submitRateRequest = (rateRequestPayload) => axios.post(`${EXC_API_BASE}/submit`, rateRequestPayload);

export const approveRejectRateRequest = (id, reviewer, payload) => axios.post(`${EXC_API_BASE}/approve/${id}?reviewer=${reviewer}`, payload);

export const updateOutputCurrencyUnits = (currencies) => axios.post(`${RATES_API_BASE}/process/today`, currencies);

export const downloadReport = async () => {
  const response = await axios.get(`${API_BASE}/report`, {
    responseType: 'blob'
  });
  const url = window.URL.createObjectURL(new Blob([response.data]));
  const link = document.createElement('a');
  link.href = url;
  link.setAttribute('download', 'Segmentation_Report.xlsx');
  document.body.appendChild(link);
  link.click();
};
