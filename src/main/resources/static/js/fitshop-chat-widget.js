(function () {
  'use strict';

  function escapeHtml(s) {
    return String(s)
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;');
  }

  function botReply(userText, productName) {
    var t = userText.toLowerCase();
    if (/giao\s*hàng|ship|vận\s*chuyển/.test(t)) {
      return 'Thời gian giao hàng phụ thuộc khu vực — thường 1–3 ngày làm việc nội thành, 3–5 ngày liên tỉnh. Bạn có thể xem lại đơn tại mục “Đơn hàng của tôi” sau khi đặt.';
    }
    if (/whey|chọn|tập|mass|pre/.test(t)) {
      return 'Gợi ý nhanh: ưu tiên mục tiêu (tăng cơ / giảm mỡ), khẩu vị dễ uống, và ngân sách. Trên trang sản phẩm, hãy so sánh biến thể (vị — khối lượng) và kiểm tra tồn kho trước khi thêm giỏ.';
    }
    if (/đổi\s*trả|hoàn|khiếu\s*nại/.test(t)) {
      return 'Chính sách đổi trả là minh họa trên trang — khi vận hành thật, bạn cần cập nhật điều khoản chính thức và thông báo cho khách.';
    }
    if (/hotline|liên\s*hệ|gọi|phone|1900/.test(t)) {
      return 'Kênh liên hệ mẫu: hotline 1900 xxx (8:00–21:00) và email support@fitshop.vn — phản hồi email trong 24h làm việc.';
    }
    if (/chào|hello|hi\b/.test(t)) {
      return 'Chào bạn! Mình có thể gợi ý cách lọc sản phẩm, chọn biến thể hoặc các bước thanh toán trên FitShop.';
    }
    if (/giá|bao\s*nhiêu|đắt|rẻ/.test(t)) {
      return 'Giá hiển thị theo từng biến thể (vị / khối lượng). Trang danh sách hiện “From …” — vào chi tiết để xem đủ các mức giá.';
    }
    if (productName && (t.indexOf('sản phẩm') >= 0 || t.indexOf('này') >= 0 || t.indexOf(productName.toLowerCase()) >= 0)) {
      return 'Bạn đang xem “' + productName + '”. Kiểm tra mô tả, đánh giá và chọn đúng biến thể còn hàng trước khi thêm vào giỏ.';
    }
    return 'Cảm ơn bạn đã nhắn. Đây là giao diện chat demo — backend AI chưa kết nối. Thử các nút gợi ý phía trên hoặc hỏi về giao hàng / chọn sản phẩm.';
  }

  function init() {
    var root = document.getElementById('fitshop-chat-root');
    if (!root) return;

    var panel = document.getElementById('fitshop-chat-panel');
    var launcher = document.getElementById('fitshop-chat-launcher');
    var closeBtn = document.getElementById('fitshop-chat-close');
    var messagesEl = document.getElementById('fitshop-chat-messages');
    var form = document.getElementById('fitshop-chat-form');
    var input = document.getElementById('fitshop-chat-input');
    var quick = document.getElementById('fitshop-chat-quick');

    var productName = (root.dataset.productName || '').trim();

    function appendBubble(role, text) {
      var wrap = document.createElement('div');
      wrap.className = 'fitshop-chat-bubble fitshop-chat-bubble--' + role;
      wrap.innerHTML =
        '<div class="fitshop-chat-bubble__inner">' + escapeHtml(text).replace(/\n/g, '<br>') + '</div>';
      messagesEl.appendChild(wrap);
      messagesEl.scrollTop = messagesEl.scrollHeight;
    }

    function welcome() {
      var w =
        'Xin chào! Mình là trợ lý FitShop (bản demo). Bạn có thể hỏi nhanh về giao hàng, chọn sản phẩm hoặc dùng các gợi ý bên dưới.';
      if (productName) {
        w += ' Bạn đang xem sản phẩm: “' + productName + '”.';
      }
      appendBubble('bot', w);
    }

    function openPanel() {
      panel.hidden = false;
      launcher.setAttribute('aria-expanded', 'true');
      setTimeout(function () {
        input.focus();
      }, 80);
    }

    function closePanel() {
      panel.hidden = true;
      launcher.setAttribute('aria-expanded', 'false');
      launcher.focus();
    }

    function togglePanel() {
      if (panel.hidden) openPanel();
      else closePanel();
    }

    function sendUser(text) {
      var trimmed = (text || '').trim();
      if (!trimmed) return;
      appendBubble('user', trimmed);
      input.value = '';
      setTimeout(function () {
        appendBubble('bot', botReply(trimmed, productName));
      }, 450);
    }

    launcher.addEventListener('click', togglePanel);
    closeBtn.addEventListener('click', closePanel);

    document.addEventListener('keydown', function (e) {
      if (e.key === 'Escape' && !panel.hidden) closePanel();
    });

    form.addEventListener('submit', function (e) {
      e.preventDefault();
      sendUser(input.value);
    });

    if (quick) {
      quick.addEventListener('click', function (e) {
        var btn = e.target.closest('.fitshop-chat-chip');
        if (!btn || !btn.dataset.prompt) return;
        sendUser(btn.dataset.prompt);
      });
    }

    welcome();
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }
})();
