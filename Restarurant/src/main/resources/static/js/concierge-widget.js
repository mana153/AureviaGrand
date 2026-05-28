(function () {
    if (window.__conciergeWidgetInit) return;
    window.__conciergeWidgetInit = true;

    const root = document.createElement('div');
    root.className = 'concierge-root';
    root.innerHTML = `
      <button class="concierge-fab" aria-label="Open concierge chat">
        <svg viewBox="0 0 24 24" aria-hidden="true">
          <path fill="currentColor" d="M4 5.5A2.5 2.5 0 0 1 6.5 3h11A2.5 2.5 0 0 1 20 5.5v7A2.5 2.5 0 0 1 17.5 15H10l-4.6 4a.7.7 0 0 1-1.15-.53V15.6A2.49 2.49 0 0 1 4 15V5.5Zm4.2 3.4h7.6a.8.8 0 1 0 0-1.6H8.2a.8.8 0 1 0 0 1.6Zm0 3.3h5.1a.8.8 0 1 0 0-1.6H8.2a.8.8 0 1 0 0 1.6Z"/>
        </svg>
      </button>
      <div class="concierge-panel" aria-hidden="true">
        <div class="concierge-header">
          <div>
            <strong>Your Concierge</strong>
            <div class="small text-light-emphasis">Ask about rooms, dining, amenities</div>
          </div>
          <button class="btn btn-sm btn-outline-light concierge-close">✕</button>
        </div>
        <div class="concierge-messages"></div>
        <form class="concierge-form">
          <input class="form-control concierge-input" type="text" placeholder="Type your question..." />
          <button class="btn btn-gold" type="submit">Send</button>
        </form>
      </div>
    `;
    document.body.appendChild(root);

    const fab = root.querySelector('.concierge-fab');
    const panel = root.querySelector('.concierge-panel');
    const close = root.querySelector('.concierge-close');
    const messages = root.querySelector('.concierge-messages');
    const form = root.querySelector('.concierge-form');
    const input = root.querySelector('.concierge-input');

    function append(text, who) {
        const div = document.createElement('div');
        div.className = `concierge-msg ${who}`;
        div.textContent = text;
        messages.appendChild(div);
        messages.scrollTop = messages.scrollHeight;
        return div;
    }

    append('Welcome to AureviaGrand Concierge. How may I help you today?', 'ai');

    function openPanel() {
        panel.classList.add('open');
        panel.setAttribute('aria-hidden', 'false');
        input.focus();
    }
    function closePanel() {
        panel.classList.remove('open');
        panel.setAttribute('aria-hidden', 'true');
    }
    fab.addEventListener('click', openPanel);
    close.addEventListener('click', closePanel);

    form.addEventListener('submit', async function (e) {
        e.preventDefault();
        const msg = input.value.trim();
        if (!msg) return;
        append(msg, 'user');
        input.value = '';

        const loading = append('Thinking...', 'ai');
        try {
            const res = await fetch('/api/ai/suggest', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ message: msg })
            });
            const data = await res.json();
            loading.remove();
            append(data.reply || data.error || 'I could not respond right now.', 'ai');
        } catch (err) {
            loading.remove();
            append('Connection issue. Please try again.', 'ai');
        }
    });
})();
