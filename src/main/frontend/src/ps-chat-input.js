/**
 * <ps-chat-input> – Chat textarea with inline send and stop buttons.
 *
 * Expected inner HTML:
 *   <textarea></textarea>
 *   <div class="chat-input-btns">
 *     <button id="chat-stop" hidden>Stop</button>
 *     <button id="chat-send">Senden</button>
 *   </div>
 *
 * Behaviour:
 *   - Textarea auto-sizes up to 6 lines.
 *   - Enter key submits; Shift+Enter inserts a newline.
 *   - When the `pending` attribute is present: send is hidden, stop is shown,
 *     textarea is readonly.
 *   - On submit, emits a `ps-send` CustomEvent with `detail.text`.
 *   - On stop click, emits a `ps-stop` CustomEvent.
 */
class PsChatInput extends HTMLElement {
  connectedCallback() {
    this._textarea = this.querySelector('textarea');
    this._sendBtn  = this.querySelector('#chat-send');
    this._stopBtn  = this.querySelector('#chat-stop');

    if (!this._textarea || !this._sendBtn) return;

    this._lineHeight = null;

    this._onInput      = () => this._autosize();
    this._onKeyDown    = (e) => {
      if (e.key === 'Enter' && !e.shiftKey) {
        e.preventDefault();
        this._submit();
      }
    };
    this._onSendClick  = () => this._submit();
    this._onStopClick  = () => this._stop();

    this._textarea.addEventListener('input',   this._onInput);
    this._textarea.addEventListener('keydown', this._onKeyDown);
    this._sendBtn.addEventListener('click',    this._onSendClick);
    if (this._stopBtn) {
      this._stopBtn.addEventListener('click', this._onStopClick);
    }

    this._autosize();
    this._syncPending();
  }

  disconnectedCallback() {
    if (!this._textarea) return;
    this._textarea.removeEventListener('input',   this._onInput);
    this._textarea.removeEventListener('keydown', this._onKeyDown);
    if (this._sendBtn) this._sendBtn.removeEventListener('click', this._onSendClick);
    if (this._stopBtn) this._stopBtn.removeEventListener('click', this._onStopClick);
  }

  static get observedAttributes() {
    return ['pending'];
  }

  attributeChangedCallback() {
    this._syncPending();
  }

  _syncPending() {
    const pending = this.hasAttribute('pending');
    if (this._sendBtn) this._sendBtn.hidden  =  pending;
    if (this._stopBtn) this._stopBtn.hidden  = !pending;
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

  _stop() {
    this.dispatchEvent(new CustomEvent('ps-stop', { bubbles: true }));
  }
}

customElements.define('ps-chat-input', PsChatInput);
