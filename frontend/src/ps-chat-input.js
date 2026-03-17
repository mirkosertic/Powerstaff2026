/**
 * <ps-chat-input> – Chat textarea with send button.
 *
 * Expected inner HTML:
 *   <textarea></textarea>
 *   <button type="button">Send</button>
 *
 * Behaviour:
 *   - Textarea auto-sizes up to 6 lines.
 *   - Enter key submits; Shift+Enter inserts a newline.
 *   - When the `pending` attribute is present: button is disabled and
 *     textarea is readonly.
 *   - On submit, emits a `ps-send` CustomEvent with `detail.text` set to
 *     the trimmed textarea value, then clears the textarea.
 */
class PsChatInput extends HTMLElement {
  connectedCallback() {
    this._textarea = this.querySelector('textarea');
    this._button = this.querySelector('button');

    if (!this._textarea || !this._button) return;

    this._lineHeight = null;

    this._onInput = () => this._autosize();
    this._onKeyDown = (e) => {
      if (e.key === 'Enter' && !e.shiftKey) {
        e.preventDefault();
        this._submit();
      }
    };
    this._onButtonClick = () => this._submit();

    this._textarea.addEventListener('input', this._onInput);
    this._textarea.addEventListener('keydown', this._onKeyDown);
    this._button.addEventListener('click', this._onButtonClick);

    this._autosize();
    this._syncPending();
  }

  disconnectedCallback() {
    if (!this._textarea) return;
    this._textarea.removeEventListener('input', this._onInput);
    this._textarea.removeEventListener('keydown', this._onKeyDown);
    this._button.removeEventListener('click', this._onButtonClick);
  }

  static get observedAttributes() {
    return ['pending'];
  }

  attributeChangedCallback() {
    this._syncPending();
  }

  _syncPending() {
    const pending = this.hasAttribute('pending');
    if (this._button) this._button.disabled = pending;
    if (this._textarea) this._textarea.readOnly = pending;
  }

  _autosize() {
    const ta = this._textarea;
    if (!this._lineHeight) {
      const style = window.getComputedStyle(ta);
      this._lineHeight = parseFloat(style.lineHeight) || 20;
    }
    ta.style.height = 'auto';
    const maxHeight = this._lineHeight * 6;
    ta.style.height = Math.min(ta.scrollHeight, maxHeight) + 'px';
  }

  _submit() {
    if (this.hasAttribute('pending')) return;
    const text = this._textarea.value.trim();
    if (!text) return;

    this.dispatchEvent(new CustomEvent('ps-send', {
      bubbles: true,
      detail: { text },
    }));

    this._textarea.value = '';
    this._autosize();
  }
}

customElements.define('ps-chat-input', PsChatInput);
