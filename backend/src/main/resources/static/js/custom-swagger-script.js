class SwaggerCleaner {
    constructor() {
        this.init();
    }

    init() {
        console.log('CLEANING ACTIVATED!');
        this.killAllWhiteBackgrounds();
        this.killAllSchemas();
        this.setupAggressiveObserver();
        this.forceDarkMode();
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

    killAllSchemas() {
        const elementsToKill = [
            '.schemes', '.scheme-container', '.schemas', '.models',
            '.model-box', 'section.models', '.model-container'
        ];

        elementsToKill.forEach(selector => {
            document.querySelectorAll(selector).forEach(el => {
                el.style.display = 'none';
                el.style.visibility = 'hidden';
                el.style.height = '0';
                el.style.width = '0';
                el.style.overflow = 'hidden';
            });
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
        `;
        document.head.appendChild(forceStyle);
    }

    setupObserver() {
       setInterval(() => {
           document.querySelectorAll('.schemes, .scheme-container, .schemas, .models, .model-box, section.models, .model-container').forEach(el => {
               el.style.display = 'none';
           });
       }, 1000);

        const observer = new MutationObserver(() => {
            this.killAllWhiteBackgrounds();
            this.killAllSchemas();
        });

        observer.observe(document.body, {
            childList: true,
            subtree: true
        });
    }
}


new SwaggerCleaner();