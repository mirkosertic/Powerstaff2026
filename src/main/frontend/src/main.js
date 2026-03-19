import './main.css';
import './ps-modal.js';
import './ps-dirty-banner.js';
import './ps-infinite-scroll.js';
import './ps-chat-input.js';

/**
 * Reads the XSRF-TOKEN cookie value.
 */
function getCsrfToken() {
  const match = document.cookie.match(/(?:^|;\s*)XSRF-TOKEN=([^;]*)/);
  return match ? decodeURIComponent(match[1]) : null;
}

/**
 * apiFetch – thin wrapper around fetch that automatically adds the
 * X-XSRF-TOKEN header for non-GET requests.
 *
 * @param {string} url
 * @param {RequestInit} [options]
 * @returns {Promise<Response>}
 */
export function apiFetch(url, options = {}) {
  const method = (options.method || 'GET').toUpperCase();
  const headers = new Headers(options.headers || {});

  if (method !== 'GET' && method !== 'HEAD') {
    const token = getCsrfToken();
    if (token) {
      //headers.set('X-XSRF-TOKEN', token);
    }
  }

  return fetch(url, { ...options, headers });
}

window.apiFetch = apiFetch;

/**
 * toggleCard – öffnet/schließt eine fcard-Sektion.
 * @param {string} id – ID des .fcard-body Elements
 */
function toggleCard(id) {
  const body = document.getElementById(id);
  const chv = body?.previousElementSibling?.querySelector('.fcard-chv');
  if (!body) return;
  const isOpen = !body.classList.contains('hidden');
  body.classList.toggle('hidden', isOpen);
  chv?.classList.toggle('open', !isOpen);
}
window.toggleCard = toggleCard;
