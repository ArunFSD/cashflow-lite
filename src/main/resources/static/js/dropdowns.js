/*
 * Styled dropdowns.
 *  - A text box with a list (<input list="...">) becomes a combo: pick a suggestion or type anything.
 *  - A <select> becomes a styled button with a menu. The original <select> stays in the form,
 *    hidden, so its value still submits and its change event still fires.
 */
(function () {
    'use strict';

    var MAX_ITEMS = 60;

    function closeAll(except) {
        document.querySelectorAll('.combo-menu.open').forEach(function (menu) {
            if (menu !== except) {
                menu.classList.remove('open');
            }
        });
    }

    function buildMenu(wrap) {
        var menu = document.createElement('ul');
        menu.className = 'combo-menu';
        menu.setAttribute('role', 'listbox');
        wrap.appendChild(menu);
        return menu;
    }

    // items: [{label, onPick}] - a mousedown is used so the text box keeps its focus
    function fill(menu, items) {
        menu.innerHTML = '';
        items.slice(0, MAX_ITEMS).forEach(function (item) {
            var li = document.createElement('li');
            li.textContent = item.label;
            li.setAttribute('role', 'option');
            li.addEventListener('mousedown', function (e) {
                e.preventDefault();
                item.onPick();
            });
            menu.appendChild(li);
        });
        return menu.children.length;
    }

    function enhanceCombo(input) {
        if (input._dropdown) {
            return;
        }
        var list = document.getElementById(input.getAttribute('list'));
        // Remove the list attribute so the browser's own suggestion popup does not appear on top of ours.
        input.removeAttribute('list');
        if (!list) {
            return;
        }
        var values = Array.from(list.options).map(function (o) { return o.value; });

        var wrap = document.createElement('div');
        wrap.className = 'combo';
        input.parentNode.insertBefore(wrap, input);
        wrap.appendChild(input);
        var menu = buildMenu(wrap);
        var active = -1;

        function matches() {
            var q = input.value.trim().toLowerCase();
            return values
                .filter(function (v) { return !q || v.toLowerCase().indexOf(q) !== -1; })
                .map(function (v) {
                    return {
                        label: v,
                        onPick: function () {
                            input.value = v;
                            hide();
                            input.dispatchEvent(new Event('change', { bubbles: true }));
                        }
                    };
                });
        }

        function show() {
            closeAll(menu);
            active = -1;
            var count = fill(menu, matches());
            menu.classList.toggle('open', count > 0);
        }

        function hide() {
            menu.classList.remove('open');
            active = -1;
        }

        function move(step) {
            var items = menu.children;
            if (!items.length) {
                return;
            }
            if (active >= 0 && items[active]) {
                items[active].classList.remove('active');
            }
            active = (active + step + items.length) % items.length;
            items[active].classList.add('active');
            items[active].scrollIntoView({ block: 'nearest' });
        }

        input.addEventListener('focus', show);
        input.addEventListener('input', show);
        input.addEventListener('blur', hide);
        input.addEventListener('keydown', function (e) {
            if (e.key === 'ArrowDown') {
                e.preventDefault();
                if (!menu.classList.contains('open')) {
                    show();
                }
                move(1);
            } else if (e.key === 'ArrowUp') {
                e.preventDefault();
                move(-1);
            } else if (e.key === 'Enter' && active >= 0 && menu.children[active]) {
                e.preventDefault();
                menu.children[active].dispatchEvent(new MouseEvent('mousedown'));
            } else if (e.key === 'Escape') {
                hide();
            }
        });
        input._dropdown = { hide: hide };
    }

    function enhanceSelect(select) {
        if (select._dropdown) {
            return;
        }
        var wrap = document.createElement('div');
        wrap.className = 'combo select-combo';
        select.parentNode.insertBefore(wrap, select);
        wrap.appendChild(select);
        select.classList.add('native-select');

        var button = document.createElement('button');
        button.type = 'button';
        button.className = 'combo-btn';
        button.setAttribute('aria-haspopup', 'listbox');
        wrap.insertBefore(button, select);
        var menu = buildMenu(wrap);

        function sync() {
            var option = select.options[select.selectedIndex];
            button.textContent = option ? option.text : '';
        }

        function hide() {
            menu.classList.remove('open');
        }

        function choose(index) {
            select.selectedIndex = index;
            sync();
            hide();
            select.dispatchEvent(new Event('change', { bubbles: true }));
        }

        function show() {
            closeAll(menu);
            fill(menu, Array.from(select.options).map(function (o, i) {
                return { label: o.text, onPick: function () { choose(i); } };
            }));
            menu.classList.add('open');
        }

        button.addEventListener('click', function () {
            if (menu.classList.contains('open')) {
                hide();
            } else {
                show();
            }
        });
        button.addEventListener('keydown', function (e) {
            if (e.key === 'Escape') {
                hide();
            }
        });
        select.addEventListener('change', sync);
        select._dropdown = { sync: sync, hide: hide };
        sync();
    }

    function init(root) {
        var scope = root || document;
        scope.querySelectorAll('input[list][data-combo]').forEach(enhanceCombo);
        scope.querySelectorAll('select').forEach(enhanceSelect);
    }

    // Close any open menu when clicking elsewhere.
    document.addEventListener('mousedown', function (e) {
        if (!e.target.closest || !e.target.closest('.combo')) {
            closeAll(null);
        }
    });

    document.addEventListener('DOMContentLoaded', function () {
        init(document);
    });

    window.CashflowDropdowns = { init: init };
})();
