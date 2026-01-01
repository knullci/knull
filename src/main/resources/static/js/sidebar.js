// Sidebar Toggle Functionality
(function () {
    'use strict';

    document.addEventListener('DOMContentLoaded', function () {
        const sidebar = document.getElementById('sidebar');
        const toggleBtn = document.getElementById('sidebar-toggle');
        const toggleBtnDesktop = document.getElementById('sidebar-toggle-desktop');
        const overlay = document.getElementById('sidebar-overlay');
        const hamburgerIcon = document.getElementById('hamburger-icon');
        const closeIcon = document.getElementById('close-icon');
        const mainContent = document.getElementById('main-content');

        // Mobile toggle
        if (toggleBtn) {
            toggleBtn.addEventListener('click', function () {
                sidebar.classList.toggle('mobile-open');
                overlay.classList.toggle('active');

                // Toggle icons
                hamburgerIcon.classList.toggle('hidden');
                closeIcon.classList.toggle('hidden');
            });
        }

        // Overlay click (mobile)
        if (overlay) {
            overlay.addEventListener('click', function () {
                sidebar.classList.remove('mobile-open');
                overlay.classList.remove('active');
                hamburgerIcon.classList.remove('hidden');
                closeIcon.classList.add('hidden');
            });
        }

        // Desktop toggle
        if (toggleBtnDesktop) {
            // Check for saved state
            const savedState = localStorage.getItem('sidebarCollapsed');
            if (savedState === 'true') {
                sidebar.classList.add('collapsed');
                if (mainContent) {
                    mainContent.classList.add('sidebar-collapsed');
                }
            }

            // Shared toggle function
            function toggleSidebar() {
                sidebar.classList.toggle('collapsed');

                // Update main content margin
                if (mainContent) {
                    mainContent.classList.toggle('sidebar-collapsed');
                }

                // Save state
                const isCollapsed = sidebar.classList.contains('collapsed');
                localStorage.setItem('sidebarCollapsed', isCollapsed);
            }

            // Desktop toggle button click
            toggleBtnDesktop.addEventListener('click', toggleSidebar);

            // Logo click to expand when collapsed
            const logoArea = document.getElementById('sidebar-logo-area');
            if (logoArea) {
                logoArea.addEventListener('click', function () {
                    // Only expand if sidebar is collapsed, don't collapse on logo click
                    if (sidebar.classList.contains('collapsed')) {
                        toggleSidebar();
                    }
                });
            }
        }

        // Function to update main content margin
        function updateContentMargin() {
            if (sidebar && mainContent) {
                if (sidebar.classList.contains('collapsed')) {
                    mainContent.classList.add('sidebar-collapsed');
                } else {
                    mainContent.classList.remove('sidebar-collapsed');
                }
            }
        }

        // Watch for sidebar changes (in case other scripts modify it)
        if (sidebar && mainContent) {
            // Initial check
            updateContentMargin();

            // Use MutationObserver to watch for class changes
            const observer = new MutationObserver(function (mutations) {
                mutations.forEach(function (mutation) {
                    if (mutation.attributeName === 'class') {
                        updateContentMargin();
                    }
                });
            });

            observer.observe(sidebar, {
                attributes: true,
                attributeFilter: ['class']
            });
        }
    });
})();
