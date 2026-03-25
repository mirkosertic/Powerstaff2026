import './main.css';
import { marked } from 'marked';
import './ps-modal.js';
import './ps-dirty-banner.js';
import './ps-infinite-scroll.js';
import './ps-chat-input.js';

/**
 * getCsrfToken – liest das XOR-maskierte CSRF-Token aus dem Meta-Tag, das
 * von Thymeleaf via ${_csrf.token} gerendert wird.
 * Diesen Wert erwartet Spring Security im X-XSRF-TOKEN-Header für
 * fetch()-Requests ohne _csrf-Formularparameter (DELETE, bodyloser POST, JSON-POST).
 * @returns {string|null}
 */
function getCsrfToken() {
  return document.querySelector('meta[name="csrf-token"]')?.content ?? null;
}
window.getCsrfToken = getCsrfToken;

/**
 * guardedNavigate – navigiert zu einer URL, warnt aber vorher bei ungespeicherten Änderungen.
 * Rückgabe false verhindert Standard-Navigation, Weiterleitung erfolgt ggf. programmatisch.
 * @param {HTMLAnchorElement} link
 * @returns {boolean}
 */
function guardedNavigate(link) {
  const dirty = document.querySelector('ps-dirty-banner')?.classList.contains('visible');
  if (!dirty || confirm('Es gibt ungespeicherte Änderungen. Trotzdem navigieren?')) {
    location.href = link.href;
  }
  return false;
}
window.guardedNavigate = guardedNavigate;

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

/**
 * renderMarkdown – wandelt Markdown-Text in HTML um (GFM + breaks).
 * Einzelne Zeilenumbrüche werden zu <br>, Fett/Kursiv/Code/Listen etc. werden
 * korrekt gerendert. Verwendet marked.js (client-seitig, streaming-kompatibel).
 * @param {string} text
 * @returns {string} HTML-String
 */
marked.use({ gfm: true, breaks: true });
function renderMarkdown(text) {
  return marked.parse(text || '');
}
window.renderMarkdown = renderMarkdown;
