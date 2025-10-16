class SwaggerCleaner {
    constructor() {
        this.init();
    }

    init() {
        console.log('CLEANING ACTIVATED!');
        this.killAllWhiteBackgrounds();
        this.forceDarkMode();
        this.setupObserver();
    }

    killAllWhiteBackgrounds() {
        document.querySelectorAll('*').forEach(el => {
            const bg = window.getComputedStyle(el).backgroundColor;
            const isWhite = bg === 'rgb(255, 255, 255)' ||
                           bg === 'rgb(248, 249, 250)' ||
                           bg === 'rgb(250, 250, 250)' ||
                           bg.includes('255, 255, 255') ||
                           bg === 'white' ||
                           bg === '#ffffff' ||
                           bg === '#fff';

            if (isWhite) {
                el.style.backgroundColor = 'transparent';
                el.style.background = 'transparent';
                el.style.backgroundImage = 'none';
            }

            if (el.classList.contains('opblock-body') ||
                el.classList.contains('responses-inner') ||
                el.classList.contains('parameters-container')) {
                el.style.backgroundColor = 'rgba(42, 39, 50, 0.8)';
            }
        });
    }

    forceDarkMode() {
        const forceStyle = document.createElement('style');
        forceStyle.textContent = `
            .swagger-ui * {
                background-color: transparent !important;
            }
            .swagger-ui .opblock-body,
            .swagger-ui .responses-inner,
            .swagger-ui .parameters-container {
                background: rgba(42, 39, 50, 0.8) !important;
            }
            .swagger-ui .opblock-section-header {
                background: rgba(73, 68, 88, 0.7) !important;
            }
            .swagger-ui .schemes,
            .swagger-ui .scheme-container,
            .swagger-ui .schemas,
            .swagger-ui .models,
            .swagger-ui .model-box,
            .swagger-ui section.models,
            .swagger-ui .model-container {
                background: rgba(42, 39, 50, 0.8) !important;
                border-radius: 12px !important;
                padding: 20px !important;
                margin: 15px 0 !important;
                border: 1px solid rgba(42, 39, 50, 0.8) !important;
            }
        `;
        document.head.appendChild(forceStyle);
    }

    setupObserver() {
        const observer = new MutationObserver(() => {
            this.killAllWhiteBackgrounds();
        });

        observer.observe(document.body, {
            childList: true,
            subtree: true
        });
    }
}

new SwaggerCleaner();