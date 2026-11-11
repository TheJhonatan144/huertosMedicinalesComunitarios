// Portada ALLPANA: parallax de la fotografía, índice lateral, revelado al hacer scroll y contador.
// Solo mejora la presentación: sin JavaScript la página se ve completa y estática.
(function () {
    var portada = document.querySelector('.al-portada');
    if (!portada) {
        return;
    }
    document.documentElement.classList.add('al-js');

    var sinMovimiento = window.matchMedia('(prefers-reduced-motion: reduce)').matches;

    // Parallax con el puntero (solo dispositivos con puntero fino) y con el desplazamiento.
    if (!sinMovimiento) {
        var pendiente = false;
        var objetivo = { x: 0, y: 0 };
        var aplicar = function () {
            pendiente = false;
            portada.style.setProperty('--al-px', objetivo.x.toFixed(3));
            portada.style.setProperty('--al-py', objetivo.y.toFixed(3));
        };
        if (window.matchMedia('(pointer: fine)').matches) {
            portada.addEventListener('pointermove', function (e) {
                var r = portada.getBoundingClientRect();
                objetivo.x = ((e.clientX - r.left) / r.width - 0.5) * -2;
                objetivo.y = ((e.clientY - r.top) / r.height - 0.5) * -2;
                if (!pendiente) {
                    pendiente = true;
                    window.requestAnimationFrame(aplicar);
                }
            });
            portada.addEventListener('pointerleave', function () {
                objetivo.x = 0;
                objetivo.y = 0;
                window.requestAnimationFrame(aplicar);
            });
        }
    }

    // Elementos que reaccionan al desplazamiento.
    var valores = Array.prototype.slice.call(document.querySelectorAll('.al-valor-texto'));
    var linea = document.querySelector('.al-linea-tiempo');
    var pasos = linea ? Array.prototype.slice.call(linea.querySelectorAll('.al-paso')) : [];
    var sumate = document.querySelector('.al-s-sumate');
    var limitar = function (v) { return Math.max(0, Math.min(1, v)); };

    // Barra superior sólida al bajar, parallax de la foto y progreso de cada sección.
    var marcoPendiente = false;
    var alDesplazar = function () {
        marcoPendiente = false;
        var alto = portada.offsetHeight || 1;
        var y = window.scrollY;
        var vh = window.innerHeight || 1;
        document.body.classList.toggle('al-nav-solida', y > 40);
        document.body.classList.toggle('al-fuera-portada', y > alto - 160);
        if (!sinMovimiento && y < alto) {
            portada.style.setProperty('--al-scroll', (y / alto).toFixed(3));
        }
        if (sinMovimiento) {
            return;
        }
        // 01 · Cada valor se "llena" de color mientras sube por la pantalla.
        valores.forEach(function (v) {
            var r = v.getBoundingClientRect();
            v.style.setProperty('--llenado', limitar((vh * 0.9 - r.top) / (vh * 0.45)).toFixed(3));
        });
        // 02 · La línea de tiempo avanza y enciende los pasos en orden.
        if (linea) {
            var rl = linea.getBoundingClientRect();
            var avance = limitar((vh * 0.85 - rl.top) / (vh * 0.5));
            linea.style.setProperty('--avance', avance.toFixed(3));
            pasos.forEach(function (paso, i) {
                paso.classList.toggle('activo', avance >= (pasos.length > 1 ? i / (pasos.length - 1) : 0) - 0.001);
            });
        }
        // 04 · La raíz de Súmate crece con el scroll y el índice cambia a tonos claros sobre el café.
        if (sumate) {
            var rs = sumate.getBoundingClientRect();
            sumate.style.setProperty('--crece', limitar((vh - rs.top) / (rs.height * 0.85)).toFixed(3));
            document.body.classList.toggle('al-en-oscuro', rs.top < vh * 0.5 && rs.bottom > vh * 0.5);
        }
    };
    window.addEventListener('scroll', function () {
        if (!marcoPendiente) {
            marcoPendiente = true;
            window.requestAnimationFrame(alDesplazar);
        }
    }, { passive: true });
    alDesplazar();

    if (!('IntersectionObserver' in window)) {
        document.querySelectorAll('[data-revelar]').forEach(function (el) { el.classList.add('visible'); });
        return;
    }

    // Revelado de bloques al entrar en pantalla.
    var revelador = new IntersectionObserver(function (entradas) {
        entradas.forEach(function (entrada) {
            if (entrada.isIntersecting) {
                entrada.target.classList.add('visible');
                revelador.unobserve(entrada.target);
            }
        });
    }, { threshold: 0.15, rootMargin: '0px 0px -40px 0px' });
    document.querySelectorAll('[data-revelar]').forEach(function (el) { revelador.observe(el); });

    // Índice lateral: marca la sección visible.
    var enlaces = Array.prototype.slice.call(portada.querySelectorAll('.al-indice a'));
    var marcar = function (id) {
        enlaces.forEach(function (a) {
            var activo = a.getAttribute('href') === '#' + id;
            a.classList.toggle('activo', activo);
            if (activo) {
                a.setAttribute('aria-current', 'true');
            } else {
                a.removeAttribute('aria-current');
            }
        });
    };
    var vigia = new IntersectionObserver(function (entradas) {
        entradas.forEach(function (entrada) {
            if (entrada.isIntersecting) {
                marcar(entrada.target.id);
            }
        });
    }, { rootMargin: '-45% 0px -50% 0px' });
    document.querySelectorAll('[data-seccion][id]').forEach(function (s) { vigia.observe(s); });

    // Súmate: luz que sigue al cursor, hojas que se apartan con suavidad y botón magnético.
    if (sumate && !sinMovimiento && window.matchMedia('(pointer: fine)').matches) {
        var hojas = Array.prototype.slice.call(sumate.querySelectorAll('.al-hoja'));
        var iman = sumate.querySelector('.al-iman');
        var boton = sumate.querySelector('.al-sumate-boton');
        var puntero = null;
        var cuadroPendiente = false;
        var reaccionar = function () {
            cuadroPendiente = false;
            if (!puntero) {
                return;
            }
            var rs = sumate.getBoundingClientRect();
            sumate.style.setProperty('--mx', (puntero.x - rs.left) + 'px');
            sumate.style.setProperty('--my', (puntero.y - rs.top) + 'px');
            hojas.forEach(function (hoja) {
                var rh = hoja.getBoundingClientRect();
                var cx = rh.left + rh.width / 2 - (parseFloat(hoja.style.getPropertyValue('--dx')) || 0);
                var cy = rh.top + rh.height / 2 - (parseFloat(hoja.style.getPropertyValue('--dy')) || 0);
                var dx = cx - puntero.x;
                var dy = cy - puntero.y;
                var d = Math.sqrt(dx * dx + dy * dy) || 1;
                var radio = 190;
                if (d < radio) {
                    var fuerza = (1 - d / radio) * 70;
                    hoja.style.setProperty('--dx', (dx / d * fuerza).toFixed(1) + 'px');
                    hoja.style.setProperty('--dy', (dy / d * fuerza).toFixed(1) + 'px');
                    hoja.style.setProperty('--giro', ((dx > 0 ? 1 : -1) * (1 - d / radio) * 60).toFixed(1) + 'deg');
                } else {
                    hoja.style.removeProperty('--dx');
                    hoja.style.removeProperty('--dy');
                    hoja.style.removeProperty('--giro');
                }
            });
            if (iman && boton) {
                var ri = iman.getBoundingClientRect();
                var bx = puntero.x - (ri.left + ri.width / 2);
                var by = puntero.y - (ri.top + ri.height / 2);
                var cerca = Math.abs(bx) < ri.width && Math.abs(by) < ri.height * 2;
                boton.style.setProperty('--bx', cerca ? (bx * 0.25).toFixed(1) + 'px' : '0px');
                boton.style.setProperty('--by', cerca ? (by * 0.35).toFixed(1) + 'px' : '0px');
            }
        };
        sumate.addEventListener('pointermove', function (e) {
            puntero = { x: e.clientX, y: e.clientY };
            if (!cuadroPendiente) {
                cuadroPendiente = true;
                window.requestAnimationFrame(reaccionar);
            }
        });
        sumate.addEventListener('pointerleave', function () {
            puntero = null;
            hojas.forEach(function (hoja) {
                hoja.style.removeProperty('--dx');
                hoja.style.removeProperty('--dy');
                hoja.style.removeProperty('--giro');
            });
            if (boton) {
                boton.style.setProperty('--bx', '0px');
                boton.style.setProperty('--by', '0px');
            }
        });
    }

    // Contador de comunidades: sube desde cero cuando la portada carga.
    var numero = portada.querySelector('[data-contar]');
    var meta = numero ? parseInt(numero.getAttribute('data-contar'), 10) : NaN;
    if (numero && !isNaN(meta) && meta > 0 && !sinMovimiento) {
        var inicio = null;
        var duracion = Math.min(1800, 600 + meta * 60);
        numero.textContent = '0';
        var paso = function (t) {
            if (inicio === null) {
                inicio = t;
            }
            var avance = Math.min(1, (t - inicio) / duracion);
            numero.textContent = String(Math.round(meta * (1 - Math.pow(1 - avance, 3))));
            if (avance < 1) {
                window.requestAnimationFrame(paso);
            }
        };
        window.setTimeout(function () { window.requestAnimationFrame(paso); }, 1400);
    }
})();
