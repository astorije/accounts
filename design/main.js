document.querySelector('#item .close').addEventListener('click', event => {
  document.querySelector('#item').setAttribute('class', 'hidden');
  document.querySelector('#main').setAttribute('class', 'full');
});

document.querySelector('.flash .close').addEventListener('click', event => {
  document.querySelector('.flash').setAttribute('class', 'flash');
});

document.querySelectorAll('#main tr').forEach(element => {
  element.addEventListener('click', event => {
    document.querySelector('#item').setAttribute('class', '');
    document.querySelector('#main').setAttribute('class', '');
    document.querySelectorAll('#item input').forEach(i => {
      i.disabled = true;
    });
    document.querySelector('#date-input').value = '2014-05-22';
    document.querySelector('#description-input').value = 'Item N';
    document.querySelector('#amount-input').value = '42';
  });
});

document.querySelector('#edit').addEventListener('click', event => {
  document.querySelectorAll('#item input').forEach(element => {
    element.disabled = false;
  });
  document.querySelector('#item input').focus();
  document.querySelector('.edit-buttons').setAttribute('class', 'edit-buttons');
});

document.querySelector('#delete').addEventListener('click', event => {
  document.querySelector('#item').setAttribute('class', 'hidden');
  document.querySelector('#main').setAttribute('class', 'full');
  document.querySelector('.flash').setAttribute('class', 'flash visible');
  document.querySelector('tr:not(.removed)').setAttribute('class', 'removed');
});

document.querySelectorAll('.edit-buttons button').forEach(button => {
  button.addEventListener('click', event => {
    document.querySelectorAll('#item input').forEach(element => {
      element.disabled = true;
    });
    document.querySelector('.edit-buttons').setAttribute('class', 'edit-buttons hidden');
  });
});

document.querySelector('.mobile-menu-button').addEventListener('click', event => {
  var c = document.querySelector('#sidebar').getAttribute('class', '');
  if (c === '' || c === null) {
    document.querySelector('#sidebar').setAttribute('class', 'visible');
  } else {
    document.querySelector('#sidebar').setAttribute('class', '');
  }
});

document.querySelector('.add-entry').addEventListener('click', event => {
  document.querySelector('#item').setAttribute('class', '');
  document.querySelector('#main').setAttribute('class', '');
  document.querySelectorAll('#item input').forEach(element => {
    element.value = '';
    element.disabled = false;
  });
  document.querySelector('#item input').focus();
  document.querySelector('.edit-buttons').setAttribute('class', 'edit-buttons');
});
