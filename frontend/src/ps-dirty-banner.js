/**
 * <ps-dirty-banner> – tracks unsaved changes on the nearest <form>.
 *
 * Usage:
 *   <ps-dirty-banner data-dirty-banner>…</ps-dirty-banner>
 *
 * Behaviour:
 *   - Listens for `input` and `change` events on the closest ancestor <form>
 *     via event delegation from the document.
 *   - Adds CSS class `visible` when any field in the form is changed.
 *   - Removes `visible` after the form is successfully submitted.
 */
class PsDirtyBanner extends HTMLElement {
  connectedCallback() {
    this._form = this.closest('form');
    if (!this._form) return;

    this._dirty = false;

    this._onChange = (e) => {
      if (this._form.contains(e.target)) {
        this._setDirty(true);
      }
    };

    this._onSubmit = () => {
      this._setDirty(false);
    };

    this._form.addEventListener('input', this._onChange);
    this._form.addEventListener('change', this._onChange);
    this._form.addEventListener('submit', this._onSubmit);
  }

  disconnectedCallback() {
    if (!this._form) return;
    this._form.removeEventListener('input', this._onChange);
    this._form.removeEventListener('change', this._onChange);
    this._form.removeEventListener('submit', this._onSubmit);
  }

  _setDirty(dirty) {
    this._dirty = dirty;
    this.classList.toggle('visible', dirty);
  }
}

customElements.define('ps-dirty-banner', PsDirtyBanner);
