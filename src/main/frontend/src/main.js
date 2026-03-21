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

window.getCsrfToken = getCsrfToken;

/**
 * openModal / closeModal – zeigt oder verbirgt ein Modal-Element (.mbk)
 * durch Toggeln der CSS-Klasse `hidden`.
 * @param {string} id – ID des Modals (ohne `#`)
 */
function openModal(id) {
  document.getElementById(id)?.classList.remove('hidden');
}
function closeModal(id) {
  document.getElementById(id)?.classList.add('hidden');
}
window.openModal = openModal;
window.closeModal = closeModal;

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

/**
 * toggleSearchPanel – zeigt/verbirgt das QBE-Suchpanel.
 * @param {string} id – ID des Such-Panel-Elements
 */
function toggleSearchPanel(id) {
  const panel = document.getElementById(id);
  if (!panel) return;
  panel.classList.toggle('hidden');
}
window.toggleSearchPanel = toggleSearchPanel;

/**
 * escHtml – escapes HTML special characters to prevent XSS when writing
 * user-supplied content via innerHTML.
 * @param {string} str
 * @returns {string}
 */
export function escHtml(str) {
  return (str || '')
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;');
}
window.escHtml = escHtml;
