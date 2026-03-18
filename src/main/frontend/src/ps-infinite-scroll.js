/**
 * <ps-infinite-scroll> – IntersectionObserver-based infinite scroll sentinel.
 *
 * Attributes:
 *   data-next-url  – URL to fetch the next page (HTML fragment)
 *   data-target    – CSS selector for the container to append results into
 *
 * Behaviour:
 *   - When the sentinel becomes visible, fetches `data-next-url`.
 *   - Appends the returned HTML to the element matching `data-target`.
 *   - If the response contains an `X-Next-Url` header, updates `data-next-url`.
 *   - If there is no `X-Next-Url` header, disconnects the observer (no more pages).
 */
class PsInfiniteScroll extends HTMLElement {
  connectedCallback() {
    this._observer = new IntersectionObserver((entries) => {
      if (entries[0].isIntersecting) {
        this._loadNext();
      }
    }, { rootMargin: '200px' });

    this._observer.observe(this);
  }

  disconnectedCallback() {
    if (this._observer) {
      this._observer.disconnect();
    }
  }

  async _loadNext() {
    const url = this.dataset.nextUrl;
    if (!url || this._loading) return;

    this._loading = true;

    try {
      const resp = await window.apiFetch(url);
      if (!resp.ok) return;

      const html = await resp.text();
      const target = document.querySelector(this.dataset.target);
      if (target) {
        target.insertAdjacentHTML('beforeend', html);
      }

      const nextUrl = resp.headers.get('X-Next-Url');
      if (nextUrl) {
        this.dataset.nextUrl = nextUrl;
        this._loading = false;
      } else {
        this._observer.disconnect();
      }
    } catch (err) {
      console.error('[ps-infinite-scroll] fetch error', err);
      this._loading = false;
    }
  }
}

customElements.define('ps-infinite-scroll', PsInfiniteScroll);
