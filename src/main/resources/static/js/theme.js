/**
 * Smart PG & Hostel Management System - Global Theme Manager
 * Supports smooth switching and persistent localStorage dark/light mode.
 */
(function () {
    'use strict';

    const THEME_STORAGE_KEY = 'smart_hostel_theme';

    function getPreferredTheme() {
        const storedTheme = localStorage.getItem(THEME_STORAGE_KEY);
        if (storedTheme === 'dark' || storedTheme === 'light') {
            return storedTheme;
        }
        return 'light'; // Default first visit to light
    }

    function setStoredTheme(theme) {
        localStorage.setItem(THEME_STORAGE_KEY, theme);
    }

    function applyTheme(theme) {
        document.documentElement.setAttribute('data-theme', theme);
        updateToggleButtons(theme);
    }

    function updateToggleButtons(theme) {
        const buttons = document.querySelectorAll('.theme-toggle-btn');
        buttons.forEach(btn => {
            const icon = btn.querySelector('.theme-icon') || btn.querySelector('i');
            const text = btn.querySelector('.theme-text') || btn.querySelector('span');

            if (theme === 'dark') {
                if (icon) {
                    icon.className = 'bi bi-sun-fill text-warning theme-icon';
                }
                if (text) {
                    text.textContent = '☀️ Light Mode';
                }
                btn.setAttribute('title', 'Switch to Light Mode');
                btn.setAttribute('aria-label', 'Switch to Light Mode');
            } else {
                if (icon) {
                    icon.className = 'bi bi-moon-stars-fill theme-icon';
                }
                if (text) {
                    text.textContent = '🌙 Dark Mode';
                }
                btn.setAttribute('title', 'Switch to Dark Mode');
                btn.setAttribute('aria-label', 'Switch to Dark Mode');
            }
        });
    }

    function toggleTheme() {
        const currentTheme = document.documentElement.getAttribute('data-theme') || 'light';
        const newTheme = currentTheme === 'dark' ? 'light' : 'dark';
        setStoredTheme(newTheme);
        applyTheme(newTheme);
    }

    // Expose globally
    window.toggleTheme = toggleTheme;

    // Apply immediately to prevent flash
    const currentTheme = getPreferredTheme();
    document.documentElement.setAttribute('data-theme', currentTheme);

    // Bind event listeners on DOM ready
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', initTheme);
    } else {
        initTheme();
    }

    function initTheme() {
        applyTheme(getPreferredTheme());

        document.querySelectorAll('.theme-toggle-btn').forEach(btn => {
            btn.removeEventListener('click', toggleTheme);
            btn.addEventListener('click', toggleTheme);
        });
    }
})();
