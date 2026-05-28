(function () {
    function getCsrf() {
        const token = document.querySelector('meta[name="_csrf"]')?.content;
        const header = document.querySelector('meta[name="_csrf_header"]')?.content || 'X-CSRF-TOKEN';
        return { token, header };
    }

    window.showStaffToast = function (message, ok) {
        const el = document.createElement('div');
        el.className = `toast align-items-center text-bg-${ok ? 'success' : 'danger'} border-0 position-fixed bottom-0 end-0 m-3`;
        el.style.zIndex = '12000';
        el.innerHTML = `<div class="d-flex"><div class="toast-body">${message}</div>
            <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button></div>`;
        document.body.appendChild(el);
        const toast = new bootstrap.Toast(el, { delay: 2200 });
        toast.show();
        el.addEventListener('hidden.bs.toast', () => el.remove());
    };

    window.submitStaffForm = async function (form, options) {
        const { successMsg, onSuccess } = options || {};
        try {
            const res = await fetch(form.action, {
                method: 'POST',
                body: new FormData(form),
                credentials: 'same-origin',
                redirect: 'manual'
            });
            if (res.type === 'opaqueredirect' || res.status === 302 || res.status === 303
                || res.status === 301 || res.status === 0) {
                if (typeof onSuccess === 'function') {
                    onSuccess();
                } else {
                    window.showStaffToast(successMsg || 'Updated successfully', true);
                    setTimeout(() => window.location.reload(), 500);
                }
                return;
            }
            if (!res.ok) throw new Error('Request failed');
            window.showStaffToast(successMsg || 'Updated successfully', true);
            setTimeout(() => window.location.reload(), 500);
        } catch (err) {
            window.showStaffToast('Retrying with standard submit…', false);
            HTMLFormElement.prototype.submit.call(form);
        }
    };

    window.bindCheckinRowForm = function (form) {
        form.addEventListener('submit', async function (e) {
            e.preventDefault();
            const row = form.closest('tr');
            const statusCell = row.querySelector('.status-cell');
            const actionCell = row.querySelector('.action-cell');
            const isCheckIn = form.classList.contains('checkin-form');
            const bookingId = row.dataset.bookingId;
            const csrf = getCsrf();

            await window.submitStaffForm(form, {
                successMsg: isCheckIn ? 'Guest checked in successfully' : 'Guest checked out successfully',
                onSuccess: function () {
                    if (isCheckIn) {
                        statusCell.innerHTML = '<span class="badge badge-checked-in">CHECKED_IN</span>';
                        actionCell.innerHTML =
                            `<form method="post" action="/staff/checkin/${bookingId}/checkout" class="d-inline checkout-form">` +
                            `<input type="hidden" name="_csrf" value="${csrf.token}">` +
                            `<button type="submit" class="btn action-btn-checkout btn-sm">CHECK OUT</button></form>`;
                        window.bindCheckinRowForm(actionCell.querySelector('.checkout-form'));
                    } else {
                        statusCell.innerHTML = '<span class="badge badge-checked-out">CHECKED_OUT</span>';
                        actionCell.innerHTML = '<span class="text-muted small">Completed</span>';
                    }
                    window.showStaffToast(
                        isCheckIn ? 'Guest checked in successfully' : 'Guest checked out successfully',
                        true
                    );
                }
            });
        });
    };

    document.addEventListener('DOMContentLoaded', function () {
        document.querySelectorAll('.checkin-form, .checkout-form').forEach(window.bindCheckinRowForm);

        document.querySelectorAll('.staff-action-form').forEach((form) => {
            form.addEventListener('submit', function (e) {
                e.preventDefault();
                window.submitStaffForm(form, {
                    successMsg: form.dataset.successMsg || 'Updated successfully'
                });
            });
        });

        const params = new URLSearchParams(window.location.search);
        if (params.get('toast') === 'ok') {
            window.showStaffToast(decodeURIComponent(params.get('msg') || 'Success'), true);
        }
    });
})();
