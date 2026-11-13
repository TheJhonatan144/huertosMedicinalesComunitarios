// Mapa público de Ecuador con Leaflet + OpenStreetMap. Si el mapa externo falla, la lista de la página sigue disponible.
(function () {
    var contenedor = document.getElementById('mapa');
    if (!contenedor) {
        return;
    }
    var aviso = document.getElementById('mapa-aviso');

    function avisar(texto) {
        if (aviso) {
            aviso.textContent = texto;
            aviso.classList.remove('d-none');
        }
    }

    if (typeof L === 'undefined') {
        contenedor.classList.add('d-none');
        avisar('El mapa no está disponible en este momento. Consulta la lista de huertos.');
        return;
    }

    var mapa = L.map(contenedor).setView([-1.45, -78.4], 7);
    var capa = L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
        maxZoom: 18,
        attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">Colaboradores de OpenStreetMap</a>'
    }).addTo(mapa);

    var fallos = 0;
    capa.on('tileerror', function () {
        fallos += 1;
        if (fallos === 3) {
            avisar('No se pudieron cargar los mosaicos del mapa. Consulta la lista de huertos.');
        }
    });

    fetch(contenedor.getAttribute('data-url'), {headers: {'Accept': 'application/json'}})
        .then(function (respuesta) {
            if (!respuesta.ok) {
                throw new Error('respuesta ' + respuesta.status);
            }
            return respuesta.json();
        })
        .then(function (huertos) {
            huertos.forEach(function (h) {
                var popup = document.createElement('div');
                var titulo = document.createElement('strong');
                titulo.textContent = h.comunidad;
                var detalle = document.createElement('div');
                detalle.textContent = h.huerto + ' · ' + h.canton + ', ' + h.provincia;
                var estado = document.createElement('div');
                estado.textContent = 'Etapa: ' + h.etapa + ' · ' + h.puntosComunitarios + ' puntos';
                var enlace = document.createElement('a');
                enlace.href = '/comunidades/' + encodeURIComponent(h.comunidadId);
                enlace.textContent = 'Ver comunidad';
                [titulo, detalle, estado, enlace].forEach(function (nodo) {
                    popup.appendChild(nodo);
                });
                L.marker([h.latitud, h.longitud]).addTo(mapa).bindPopup(popup);
            });
        })
        .catch(function () {
            avisar('No se pudieron cargar los huertos en el mapa. Consulta la lista de huertos.');
        });
})();
