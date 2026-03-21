/**
 * <ps-modal> – Light-DOM modal dialog custom element.
 *
 * Attributes:
 *   open              – when present the modal is visible
 *   data-confirm-url  – when set, the form inside is submitted via fetch
 *                       instead of the native browser submit
 *
 * Events emitted:
 *   ps-modal-close    – fired on the element after the modal closes
 *
 * Public API:
 *   show()   – opens the modal
 *   close()  – closes the modal
 */
class PsModal extends HTMLElement {
  connectedCallback() {
    this._onKeyDown = (e) => {
      if (e.key === 'Escape' && this.hasAttribute('open')) {
        this.close();
      }
    };
    document.addEventListener('keydown', this._onKeyDown);

    this._onOverlayClick = (e) => {
      if (e.target === this) {
        this.close();
      }
    };
    this.addEventListener('click', this._onOverlayClick);

    const confirmUrl = this.dataset.confirmUrl;
    if (confirmUrl) {
      const form = this.querySelector('form');
      if (form) {
        form.addEventListener('submit', (e) => this._handleSubmit(e, confirmUrl));
      }
    }
  }

  disconnectedCallback() {
    document.removeEventListener('keydown', this._onKeyDown);
  }

  static get observedAttributes() {
    return ['open'];
  }

  show() {
    this.setAttribute('open', '');
  }

  close() {
    this.removeAttribute('open');
    this.dispatchEvent(new CustomEvent('ps-modal-close', { bubbles: true }));
  }

  async _handleSubmit(e, url) {
    e.preventDefault();
    const form = e.target;
    const body = new FormData(form);

    try {
      const resp = await fetch(url, { method: 'POST', body });
      if (resp.ok) {
        this.close();
      }
    } catch (err) {
      console.error('[ps-modal] submit error', err);
    }
  }
}

customElements.define('ps-modal', PsModal);
