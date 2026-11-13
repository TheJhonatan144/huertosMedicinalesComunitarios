// Interacciones de las pantallas internas de ALLPANA. Solo mejora la presentación:
// sin JavaScript todo se ve completo y funciona igual.
(function () {
    var sinMovimiento = window.matchMedia('(prefers-reduced-motion: reduce)').matches;

    // 1) Barra superior: marca la página actual (el enlace más específico que coincida con la ruta).
    var ruta = window.location.pathname.replace(/\/+$/, '') || '/';
    var mejor = null;
    document.querySelectorAll('.al-nav-principal a.nav-link, .al-nav-principal a.dropdown-item').forEach(function (a) {
        var destino = (a.getAttribute('href') || '').replace(/\/+$/, '') || '/';
        if (destino === '#' || destino === '/') {
            return;
        }
        if ((ruta === destino || ruta.indexOf(destino + '/') === 0) && (!mejor || destino.length > mejor.destino.length)) {
            mejor = { a: a, destino: destino };
        }
    });
    if (mejor) {
        mejor.a.setAttribute('aria-current', 'page');
        var desplegable = mejor.a.closest('.dropdown');
        if (desplegable) {
            var disparador = desplegable.querySelector('.dropdown-toggle');
            if (disparador) {
                disparador.setAttribute('aria-current', 'page');
            }
        }
    }

    if (document.querySelector('.al-portada') || sinMovimiento) {
        return;
    }
    document.documentElement.classList.add('al-int-js');

    // 2) Números de las tarjetas de métricas: suben desde cero ("4 / 25" anima cada número).
    document.querySelectorAll('.tarjeta-huerto .fs-3').forEach(function (el) {
        var original = el.textContent;
        if (!/\d/.test(original)) {
            return;
        }
        var partes = original.split(/(\d+)/);
        var inicio = null;
        var duracion = 900;
        var paso = function (t) {
            if (inicio === null) {
                inicio = t;
            }
            var avance = Math.min(1, (t - inicio) / duracion);
            var suave = 1 - Math.pow(1 - avance, 3);
            el.textContent = partes.map(function (p) {
                return /^\d+$/.test(p) ? String(Math.round(parseInt(p, 10) * suave)) : p;
            }).join('');
            if (avance < 1) {
                window.requestAnimationFrame(paso);
            } else {
                el.textContent = original;
            }
        };
        window.requestAnimationFrame(paso);
    });

    // 3) Bloques que empiezan fuera de pantalla aparecen al llegar a ellos.
    if (!('IntersectionObserver' in window)) {
        return;
    }
    var alto = window.innerHeight;
    var candidatos = document.querySelectorAll(
        '.app-main .card:not(.tarjeta-huerto), .app-main .table-responsive, .app-main .plantas-panel, .app-main .hv-huerto');
    var vigia = new IntersectionObserver(function (entradas) {
        entradas.forEach(function (e) {
            if (e.isIntersecting) {
                e.target.classList.add('visible');
                vigia.unobserve(e.target);
            }
        });
    }, { threshold: 0.08, rootMargin: '0px 0px -30px 0px' });
    candidatos.forEach(function (el) {
        if (el.getBoundingClientRect().top > alto) {
            el.classList.add('al-revela');
            vigia.observe(el);
        }
    });
})();
