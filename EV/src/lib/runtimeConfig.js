const trimTrailingSlash = (value = '') => value.replace(/\/+$/, '');

const API_BASE = trimTrailingSlash(import.meta.env.VITE_API_BASE || '');
const MEDIA_BASE = trimTrailingSlash(import.meta.env.VITE_MEDIA_BASE_URL || API_BASE);

const STREAM_URLS = {
  A01: import.meta.env.VITE_STREAM_A01 || '',
  A02: import.meta.env.VITE_STREAM_A02 || '',
  B01: import.meta.env.VITE_STREAM_B01 || '',
  B02: import.meta.env.VITE_STREAM_B02 || '',
};

const ensureLeadingSlash = (path = '') => (path.startsWith('/') ? path : `/${path}`);

export const api = (path) => `${API_BASE}${ensureLeadingSlash(path)}`;

export const mediaUrl = (path) => {
  if (!path) return '';
  if (/^https?:\/\//i.test(path)) return path;
  return `${MEDIA_BASE}${ensureLeadingSlash(path)}`;
};

export const streamUrlForStation = (station) => {
  if (!station) return '';
  const key = station.toUpperCase().replace(/[^A-Z0-9]/g, '');
  return STREAM_URLS[key] || '';
};

export const runtimeConfig = {
  apiBase: API_BASE,
  mediaBase: MEDIA_BASE,
};
