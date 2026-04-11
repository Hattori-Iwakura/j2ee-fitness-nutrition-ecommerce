/**
 * FitShop AI Assistant — Chat Widget
 */
(function () {
    'use strict';

    const CSRF_META = () => document.querySelector('meta[name="_csrf"]');
    const CSRF_HEADER_META = () => document.querySelector('meta[name="_csrf_header"]');

    let chatOpen = false;
    let sending = false;

    // --- Toggle ---
    window.toggleChat = function () {
        const panel = document.getElementById('chatPanel');
        const fab = document.getElementById('chatToggleBtn');
        if (!panel) return;

        chatOpen = !chatOpen;
        panel.classList.toggle('d-none', !chatOpen);

        if (chatOpen) {
            // Change FAB icon to X
            fab.innerHTML = '<i class="bi bi-x-lg"></i>';
            document.getElementById('chatInput').focus();
            scrollToBottom();
        } else {
            fab.innerHTML = '<i class="bi bi-chat-dots-fill"></i>';
        }
    };

    // --- Send ---
    window.sendMessage = function (event) {
        event.preventDefault();
        if (sending) return;

        const input = document.getElementById('chatInput');
        const message = input.value.trim();
        if (!message) return;

        appendMessage('user', message);
        input.value = '';
        showTyping();
        setSending(true);

        const csrfToken = CSRF_META() ? CSRF_META().content : '';
        const csrfHeader = CSRF_HEADER_META() ? CSRF_HEADER_META().content : 'X-CSRF-TOKEN';

        fetch('/api/chat', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                [csrfHeader]: csrfToken
            },
            body: JSON.stringify({ message: message })
        })
            .then(function (r) {
                if (!r.ok) throw new Error('HTTP ' + r.status);
                return r.json();
            })
            .then(function (data) {
                hideTyping();
                // Show action badges
                if (data.actions && data.actions.length > 0) {
                    data.actions.forEach(function (a) {
                        appendAction(a.tool, a.summary);
                    });
                }
                appendMessage('assistant', data.reply || 'No response');
            })
            .catch(function (err) {
                hideTyping();
                console.error('Chat error:', err);
                appendMessage('assistant', 'Xin lỗi, đã có lỗi xảy ra. Vui lòng thử lại.');
            })
            .finally(function () {
                setSending(false);
                document.getElementById('chatInput').focus();
            });
    };

    // --- Clear ---
    window.clearChat = function () {
        const csrfToken = CSRF_META() ? CSRF_META().content : '';
        const csrfHeader = CSRF_HEADER_META() ? CSRF_HEADER_META().content : 'X-CSRF-TOKEN';

        fetch('/api/chat/clear', {
            method: 'POST',
            headers: { [csrfHeader]: csrfToken }
        });

        var container = document.getElementById('chatMessages');
        container.innerHTML = '';
        appendWelcome();
    };

    // --- DOM helpers ---

    function appendMessage(role, text) {
        var container = document.getElementById('chatMessages');
        var div = document.createElement('div');
        div.className = 'chat-msg chat-msg-' + role;

        if (role === 'assistant') {
            div.innerHTML = formatMarkdown(text);
        } else {
            div.textContent = text;
        }

        container.appendChild(div);
        scrollToBottom();
    }

    function appendAction(tool, summary) {
        var container = document.getElementById('chatMessages');
        var div = document.createElement('div');
        div.className = 'chat-action-badge';

        var iconMap = {
            searchProducts: 'bi-search',
            getProductDetail: 'bi-box-seam',
            listCategories: 'bi-grid',
            calculateTDEE: 'bi-calculator',
            recommendProducts: 'bi-stars',
            addToCart: 'bi-cart-plus',
            getCart: 'bi-cart3',
            getOrderHistory: 'bi-clock-history',
            getOrderStatus: 'bi-truck',
            getWishlist: 'bi-heart'
        };

        var icon = iconMap[tool] || 'bi-gear';
        div.innerHTML = '<i class="bi ' + icon + '"></i> ' + escapeHtml(summary);
        container.appendChild(div);
        scrollToBottom();
    }

    function appendWelcome() {
        appendMessage('assistant',
            'Xin chào! Tôi là **FitShop AI Assistant** 🤖\n\n' +
            'Tôi có thể giúp bạn:\n' +
            '- 🔍 Tìm kiếm sản phẩm\n' +
            '- 💪 Tư vấn dinh dưỡng & tính TDEE\n' +
            '- 🛒 Thêm sản phẩm vào giỏ hàng\n' +
            '- 📦 Tra cứu đơn hàng\n\n' +
            'Hãy hỏi tôi bất cứ điều gì!'
        );
    }

    function showTyping() {
        var container = document.getElementById('chatMessages');
        var existing = document.getElementById('chatTyping');
        if (existing) return;

        var div = document.createElement('div');
        div.id = 'chatTyping';
        div.className = 'chat-typing';
        div.innerHTML = '<span></span><span></span><span></span>';
        container.appendChild(div);
        scrollToBottom();
    }

    function hideTyping() {
        var el = document.getElementById('chatTyping');
        if (el) el.remove();
    }

    function setSending(state) {
        sending = state;
        var btn = document.getElementById('chatSendBtn');
        var input = document.getElementById('chatInput');
        if (btn) btn.disabled = state;
        if (input) input.disabled = state;
    }

    function scrollToBottom() {
        var container = document.getElementById('chatMessages');
        if (container) {
            setTimeout(function () {
                container.scrollTop = container.scrollHeight;
            }, 50);
        }
    }

    function escapeHtml(text) {
        var div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }

    function formatMarkdown(text) {
        if (!text) return '';
        var escaped = escapeHtml(text);

        // Bold: **text**
        escaped = escaped.replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>');

        // Inline code: `text`
        escaped = escaped.replace(/`([^`]+)`/g, '<code>$1</code>');

        // Bullet lists: lines starting with - or *
        escaped = escaped.replace(/^[\-\*]\s+(.+)$/gm, '<li>$1</li>');
        escaped = escaped.replace(/(<li>.*<\/li>\n?)+/g, '<ul>$&</ul>');

        // Line breaks
        escaped = escaped.replace(/\n/g, '<br>');

        // Clean up double br inside ul
        escaped = escaped.replace(/<ul><br>/g, '<ul>');
        escaped = escaped.replace(/<br><\/ul>/g, '</ul>');

        return escaped;
    }

    // --- Init ---
    document.addEventListener('DOMContentLoaded', function () {
        var container = document.getElementById('chatMessages');
        if (container && container.children.length === 0) {
            appendWelcome();
        }
    });
})();
