// Comportamientos mínimos de interfaz. La seguridad y las reglas se aplican siempre en el servidor.
(function () {
    // Evita dobles envíos: deshabilita los botones tras el clic, después de que el navegador serialice el formulario.
    document.addEventListener('submit', function (evento) {
        var formulario = evento.target;
        if (!formulario.matches || !formulario.matches('form[data-una-vez]')) {
            return;
        }
        window.setTimeout(function () {
            formulario.querySelectorAll('button[type="submit"]').forEach(function (boton) {
                boton.disabled = true;
            });
        }, 0);
    });

    // Confirmación para acciones sensibles: <button data-confirmar="¿Seguro?">
    document.addEventListener('click', function (evento) {
        var boton = evento.target.closest('[data-confirmar]');
        if (boton && !window.confirm(boton.getAttribute('data-confirmar'))) {
            evento.preventDefault();
        }
    });
})();
