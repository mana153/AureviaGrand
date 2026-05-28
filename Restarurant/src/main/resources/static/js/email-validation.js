(function () {
    const EMAIL_REGEX = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

    window.validateEmailField = function (input, errorEl) {
        const value = input.value.trim();
        const valid = EMAIL_REGEX.test(value);
        if (errorEl) {
            errorEl.classList.toggle('d-none', valid || value === '');
            errorEl.textContent = 'Please enter a valid email address';
        }
        return valid;
    };

    window.bindEmailValidation = function (formSelector, inputSelector, errorId) {
        const form = document.querySelector(formSelector);
        if (!form) return;
        const emailInput = form.querySelector(inputSelector || 'input[type="email"]');
        const inlineError = errorId ? document.getElementById(errorId) : null;
        if (!emailInput) return;

        const validate = () => window.validateEmailField(emailInput, inlineError);
        emailInput.addEventListener('input', validate);
        emailInput.addEventListener('blur', validate);
        form.addEventListener('submit', function (e) {
            if (!validate()) {
                e.preventDefault();
                if (inlineError) inlineError.classList.remove('d-none');
                emailInput.focus();
            }
        });
    };
})();
