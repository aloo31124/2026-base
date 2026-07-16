const root = document.documentElement;
const savedTheme = localStorage.getItem('agentflow-theme');
const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches;
root.dataset.theme = savedTheme || (prefersDark ? 'dark' : 'light');

function updateThemeButtons() {
  const isDark = root.dataset.theme === 'dark';
  document.querySelectorAll('[data-theme-toggle]').forEach((button) => {
    button.setAttribute('aria-label', isDark ? '切換為日間模式' : '切換為夜間模式');
    button.setAttribute('title', isDark ? '切換為日間模式' : '切換為夜間模式');
    button.innerHTML = isDark
      ? '<svg class="icon" viewBox="0 0 24 24"><circle cx="12" cy="12" r="4"/><path d="M12 2v2M12 20v2M4.93 4.93l1.42 1.42M17.66 17.66l1.41 1.41M2 12h2M20 12h2M4.93 19.07l1.42-1.42M17.66 6.34l1.41-1.41"/></svg>'
      : '<svg class="icon" viewBox="0 0 24 24"><path d="M20.5 14.1A8.5 8.5 0 0 1 9.9 3.5 8.5 8.5 0 1 0 20.5 14.1Z"/></svg>';
  });
}

document.addEventListener('click', (event) => {
  const themeButton = event.target.closest('[data-theme-toggle]');
  if (themeButton) {
    root.dataset.theme = root.dataset.theme === 'dark' ? 'light' : 'dark';
    localStorage.setItem('agentflow-theme', root.dataset.theme);
    updateThemeButtons();
  }

  const sidebarButton = event.target.closest('[data-sidebar-toggle]');
  if (sidebarButton) {
    const sidebar = document.querySelector('.sidebar');
    if (window.innerWidth <= 600) sidebar?.classList.toggle('is-open');
    else sidebar?.classList.toggle('is-collapsed');
  }

  const filterButton = event.target.closest('[data-filter-toggle]');
  if (filterButton) {
    const filter = filterButton.closest('.filter-card');
    filter?.classList.toggle('is-collapsed');
    filterButton.setAttribute('aria-expanded', String(!filter?.classList.contains('is-collapsed')));
  }

  const switchButton = event.target.closest('.switch');
  if (switchButton) {
    switchButton.setAttribute('aria-checked', String(switchButton.getAttribute('aria-checked') !== 'true'));
  }
});

window.addEventListener('resize', () => {
  if (window.innerWidth > 600) document.querySelector('.sidebar')?.classList.remove('is-open');
});

updateThemeButtons();
requestAnimationFrame(() => root.classList.add('theme-ready'));
