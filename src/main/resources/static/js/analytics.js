(function () {
    'use strict';

    const config = document.getElementById('talentbridge-analytics');
    if (!config) return;

    const measurementId = String(config.dataset.measurementId || '').trim();
    if (!/^G-[A-Z0-9]{4,20}$/.test(measurementId)) return;

    const consentKey = 'talentbridge.analyticsConsent.v1';
    const banner = document.getElementById('analytics-consent');
    const acceptButton = document.getElementById('analytics-consent-accept');
    const rejectButton = document.getElementById('analytics-consent-reject');
    const preferencesButton = document.getElementById('analytics-preferences');
    let loaded = false;

    window.dataLayer = window.dataLayer || [];
    window.gtag = window.gtag || function () {
        window.dataLayer.push(arguments);
    };

    window.gtag('consent', 'default', {
        analytics_storage: 'denied',
        ad_storage: 'denied',
        ad_user_data: 'denied',
        ad_personalization: 'denied',
        wait_for_update: 500
    });

    function readConsent() {
        try {
            return window.localStorage.getItem(consentKey);
        } catch (error) {
            return null;
        }
    }

    function saveConsent(value) {
        try {
            window.localStorage.setItem(consentKey, value);
        } catch (error) {
            // The current choice still applies for this page when storage is unavailable.
        }
    }

    function loadAnalytics() {
        if (loaded) return;
        loaded = true;
        window.gtag('consent', 'update', { analytics_storage: 'granted' });
        window.gtag('js', new Date());
        window.gtag('config', measurementId, {
            send_page_view: true,
            anonymize_ip: true,
            allow_google_signals: false,
            allow_ad_personalization_signals: false,
            cookie_flags: 'SameSite=None;Secure'
        });

        const script = document.createElement('script');
        script.async = true;
        script.src = 'https://www.googletagmanager.com/gtag/js?id=' + encodeURIComponent(measurementId);
        document.head.appendChild(script);
    }

    function setBannerVisible(visible) {
        if (!banner) return;
        banner.hidden = !visible;
    }

    function acceptAnalytics() {
        saveConsent('granted');
        setBannerVisible(false);
        loadAnalytics();
    }

    function rejectAnalytics() {
        saveConsent('denied');
        window.gtag('consent', 'update', { analytics_storage: 'denied' });
        setBannerVisible(false);
    }

    function track(eventName) {
        if (readConsent() !== 'granted' || !/^[a-z][a-z0-9_]{0,39}$/.test(eventName)) return;
        window.gtag('event', eventName);
    }

    acceptButton?.addEventListener('click', acceptAnalytics);
    rejectButton?.addEventListener('click', rejectAnalytics);
    preferencesButton?.addEventListener('click', function () {
        setBannerVisible(true);
        acceptButton?.focus();
    });

    document.addEventListener('click', function (event) {
        const target = event.target.closest('[data-analytics-event]');
        if (target) track(target.dataset.analyticsEvent);
    });
    document.addEventListener('submit', function (event) {
        const form = event.target.closest('form[data-analytics-event]');
        if (form) track(form.dataset.analyticsEvent);
    });

    const consent = readConsent();
    if (consent === 'granted') {
        loadAnalytics();
    } else if (consent !== 'denied') {
        setBannerVisible(true);
    }
})();
