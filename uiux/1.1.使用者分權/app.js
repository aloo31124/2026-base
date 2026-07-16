const views = document.querySelectorAll('.page-view');
const tabs = document.querySelectorAll('[data-view-target]');
const toast = document.querySelector('.toast');
const toastText = document.querySelector('#toast-text');
let toastTimer;

function showToast(message) {
  toastText.textContent = message;
  toast.classList.add('is-visible');
  clearTimeout(toastTimer);
  toastTimer = setTimeout(() => toast.classList.remove('is-visible'), 3000);
}

function showView(target, source) {
  views.forEach((view) => view.classList.toggle('is-active', view.id === target));
  document.querySelectorAll('.page-tabs .tab').forEach((tab) => {
    tab.classList.toggle('active', tab.dataset.viewTarget === target || (target === 'role-assign' && tab.dataset.viewTarget === 'roles'));
  });
  if (target === 'user-edit') {
    const isEdit = source?.dataset.formMode === 'edit';
    document.querySelector('#user-form-title').textContent = isEdit ? '編輯使用者' : '新增使用者';
    document.querySelector('#user-form-description').textContent = isEdit ? '更新帳號資料，並可重新設定登入密碼。' : '建立帳號後可設定密碼，使用者預設為員工角色。';
    document.querySelector('#user-form button[type="submit"]').textContent = isEdit ? '儲存變更' : '建立使用者';
  }
  window.scrollTo({ top: 0, behavior: 'smooth' });
}

document.addEventListener('click', (event) => {
  const target = event.target.closest('[data-view-target]');
  if (target) {
    event.preventDefault();
    showView(target.dataset.viewTarget, target);
  }
  const disableButton = event.target.closest('[data-disable-user]');
  if (disableButton) {
    disableButton.textContent = '已停用';
    disableButton.disabled = true;
    disableButton.classList.remove('btn-danger');
    disableButton.classList.add('btn-secondary');
    showToast('使用者帳號已停用，已無法登入工作空間。');
  }
});

document.querySelector('#user-form').addEventListener('submit', (event) => {
  event.preventDefault();
  showView('users');
  showToast('使用者已儲存，帳號註冊方式為「管理員新增」。');
});

document.querySelector('#role-form').addEventListener('submit', (event) => {
  event.preventDefault();
  showView('roles');
  showToast('主管角色已授予；使用者仍保有員工角色。');
});
