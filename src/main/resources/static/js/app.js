/*
 * Page behaviour: the add/edit dialog, the date field, the fields that depend on the
 * choices made, validation messages, and the flash message timeout.
 */
(function () {
    'use strict';

    var FLASH_MS = 5000;

    function pad(n) {
        return (n < 10 ? '0' : '') + n;
    }

    // ---------- flash messages fade out after 5 seconds ----------
    function autoHideFlash() {
        document.querySelectorAll('[data-flash]').forEach(function (el) {
            setTimeout(function () {
                el.classList.add('fade');
                setTimeout(function () { el.remove(); }, 400);
            }, FLASH_MS);
        });
    }

    // ---------- add / edit dialog ----------
    function setupDialog() {
        var dialog = document.getElementById('entryDialog');
        if (!dialog) {
            return;
        }

        function open() {
            if (!dialog.open) {
                dialog.showModal();
            }
            var first = dialog.querySelector('input[type=text]');
            if (first) {
                first.focus();
            }
        }

        document.querySelectorAll('[data-open-dialog]').forEach(function (button) {
            button.addEventListener('click', open);
        });
        dialog.querySelectorAll('[data-close-dialog]').forEach(function (button) {
            button.addEventListener('click', function () { dialog.close(); });
        });
        // Editing opens the page at /.../{id}/edit; once closed, go back to the plain list.
        dialog.addEventListener('close', function () {
            if (/\/edit$/.test(location.pathname)) {
                history.replaceState(null, '', dialog.dataset.listUrl || location.pathname.replace(/\/\d+\/edit$/, ''));
            }
        });

        if (dialog.dataset.open === 'true') {
            open();
        }
    }

    // ---------- date field: DD/MM/YYYY typing plus a calendar picker ----------
    function toIso(dmy) {
        var m = /^(\d{2})\/(\d{2})\/(\d{4})$/.exec(dmy.trim());
        return m ? m[3] + '-' + m[2] + '-' + m[1] : '';
    }

    function toDmy(iso) {
        var m = /^(\d{4})-(\d{2})-(\d{2})$/.exec(iso);
        return m ? m[3] + '/' + m[2] + '/' + m[1] : '';
    }

    function setupDates() {
        document.querySelectorAll('input[data-date]').forEach(function (text) {
            var wrap = text.closest('.date-field');
            if (!wrap) {
                return;
            }
            var native = wrap.querySelector('input[data-date-native]');
            var button = wrap.querySelector('[data-calendar]');

            // Keep the slashes in place while typing: 24102026 -> 24/10/2026
            text.addEventListener('input', function () {
                var digits = text.value.replace(/\D/g, '').slice(0, 8);
                var out = digits.slice(0, 2);
                if (digits.length > 2) {
                    out += '/' + digits.slice(2, 4);
                }
                if (digits.length > 4) {
                    out += '/' + digits.slice(4);
                }
                text.value = out;
            });

            button.addEventListener('click', function () {
                var today = new Date();
                native.value = toIso(text.value) || (today.getFullYear() + '-' + pad(today.getMonth() + 1) + '-' + pad(today.getDate()));
                if (native.showPicker) {
                    native.showPicker();
                } else {
                    native.click();
                }
            });
            native.addEventListener('change', function () {
                text.value = toDmy(native.value);
            });
        });
    }

    // ---------- fields that depend on a choice ----------
    function setHidden(el, hide) {
        if (el) {
            el.hidden = hide;
        }
    }

    // "Paid by" / "How it came": show the extra detail box only where the choice needs one.
    function updateDetail(select) {
        var target = document.querySelector(select.dataset.detailSelect);
        var option = select.options[select.selectedIndex];
        var label = option ? option.getAttribute('data-detail') : '';
        setHidden(target, !label);
        var span = target && target.querySelector('[data-detail-label]');
        if (span && label) {
            span.textContent = label;
        }
    }

    // "What was it for": show the friend box and the note box only where needed.
    function updatePurpose(select) {
        var option = select.options[select.selectedIndex];
        if (!option) {
            return;
        }
        setHidden(document.getElementById('partyField'), option.getAttribute('data-party') !== 'true');
        setHidden(document.getElementById('noteField'), option.getAttribute('data-note') !== 'true');
    }

    // ---------- validation messages inside the form (no browser pop-ups) ----------
    function setupValidation() {
        document.querySelectorAll('[data-entry-form]').forEach(function (form) {
            form.addEventListener('submit', function (e) {
                form.querySelectorAll('.js-error').forEach(function (el) { el.remove(); });
                var first = null;

                form.querySelectorAll('[data-required]').forEach(function (field) {
                    if (field.closest('[hidden]') || field.value.trim() !== '') {
                        return;
                    }
                    var group = field.closest('.field');
                    var label = group && group.querySelector('label');
                    var msg = document.createElement('small');
                    msg.className = 'field-error js-error';
                    msg.textContent = 'Please fill in: ' + (label ? label.textContent.trim() : 'this field');
                    group.appendChild(msg);
                    field.classList.add('is-invalid');
                    if (!first) {
                        first = field;
                    }
                });

                if (first) {
                    e.preventDefault();
                    first.focus();
                }
            });

            // Typing in a field clears its message.
            form.addEventListener('input', function (e) {
                var group = e.target.closest && e.target.closest('.field');
                if (group) {
                    group.querySelectorAll('.js-error').forEach(function (el) { el.remove(); });
                    e.target.classList.remove('is-invalid');
                }
            });
        });
    }

    document.addEventListener('DOMContentLoaded', function () {
        autoHideFlash();
        setupDialog();
        setupDates();
        setupValidation();

        document.querySelectorAll('[data-detail-select]').forEach(function (select) {
            updateDetail(select);
            select.addEventListener('change', function () { updateDetail(select); });
        });
        document.querySelectorAll('[data-purpose-select]').forEach(function (select) {
            updatePurpose(select);
            select.addEventListener('change', function () { updatePurpose(select); });
        });
    });
})();
