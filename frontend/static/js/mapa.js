/* Sección 1. La lista renderizada por el servidor es la fuente de datos del mapa.
 * Sin geocodificación, claves, rastreo de ubicación ni datos personales. */
(function () {
  "use strict";
  var root = document.getElementById("explorador");
  var mini = document.getElementById("mapa-ficha");
  var reduced = window.matchMedia("(prefers-reduced-motion: reduce)").matches;
  function notice(id, message) {
    var el = document.getElementById(id);
    if (el) {
      el.textContent = message;
      el.hidden = false;
    }
  }
  function coordinates(data) {
    if (!data.lat || !data.lng) return null;
    var lat = Number(data.lat),
      lng = Number(data.lng);
    return Number.isFinite(lat) &&
      Number.isFinite(lng) &&
      Math.abs(lat) <= 90 &&
      Math.abs(lng) <= 180
      ? [lat, lng]
      : null;
  }
  function icon() {
    return L.divIcon({
      className: "pub-pin-icon",
      iconSize: [38, 44],
      iconAnchor: [18, 42],
      popupAnchor: [0, -40],
      html: '<span class="pub-pin"><svg viewBox="0 0 24 24" aria-hidden="true"><path d="M6 18C3 8 11 4 20 4c0 10-4 16-11 14" fill="#dce8c5"/><path d="m6 20 9-10" stroke="#285b45" stroke-width="2" stroke-linecap="round"/></svg></span>',
    });
  }
  function tiles(map, warningId) {
    var failures = 0;
    L.tileLayer("https://tile.openstreetmap.org/{z}/{x}/{y}.png", {
      maxZoom: 18,
      attribution:
        '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>',
    })
      .on("tileerror", function () {
        if (++failures === 3)
          notice(
            warningId,
            "No se pudo cargar el mapa base. Las ubicaciones y las fichas siguen disponibles; revisa tu conexión.",
          );
      })
      .addTo(map);
  }
  function makeMap(container) {
    container.replaceChildren();
    var map = L.map(container, {
      scrollWheelZoom: false,
      zoomControl: false,
      zoomAnimation: !reduced,
      fadeAnimation: !reduced,
      markerZoomAnimation: !reduced,
    });
    L.control
      .zoom({
        position: "topright",
        zoomInTitle: "Acercar",
        zoomOutTitle: "Alejar",
      })
      .addTo(map);
    return map;
  }
  if (mini) {
    var point = coordinates(mini.dataset);
    if (!point) {
      notice(
        "ficha-mapa-aviso",
        "La ubicación registrada no es válida. Puedes seguir explorando la información de la comunidad.",
      );
    } else if (typeof L === "undefined") {
      notice(
        "ficha-mapa-aviso",
        "El mapa no está disponible. Consulta la localidad indicada en esta ficha.",
      );
    } else {
      try {
        var smallMap = makeMap(mini).setView(point, 13);
        tiles(smallMap, "ficha-mapa-aviso");
        // Tooltip de texto: nunca se interpreta el nombre como HTML.
        var label = document.createElement("span");
        label.textContent = mini.dataset.nombre;
        L.marker(point, {
          icon: icon(),
          title: mini.dataset.nombre,
          alt: mini.dataset.nombre,
        })
          .addTo(smallMap)
          .bindTooltip(label);
      } catch (error) {
        notice(
          "ficha-mapa-aviso",
          "No se pudo iniciar el mapa. La ficha sigue disponible.",
        );
      }
    }
  }
  if (!root) return;
  var cards = Array.from(root.querySelectorAll(".pub-garden"));
  var search = document.getElementById("buscar-huerto");
  var province = document.getElementById("provincia");
  var stage = document.getElementById("etapa");
  var mapNode = document.getElementById("mapa");
  var map = null,
    layer = null;
  var markers = new Map();
  function listOnly(message) {
    var explorer = root.querySelector(".pub-explorer");
    explorer.classList.add("is-list-only");
    explorer.before(document.getElementById("mapa-aviso"));
    root.querySelector(".pub-map-panel").hidden = true;
    root.querySelectorAll(".pub-locate").forEach(function (button) {
      button.hidden = true;
    });
    notice("mapa-aviso", message);
  }
  function normalize(value) {
    return (value || "")
      .normalize("NFD")
      .replace(/[\u0300-\u036f]/g, "")
      .toLocaleLowerCase("es")
      .replace(/\s+/g, " ")
      .trim();
  }
  function options(select, key) {
    Array.from(
      new Set(
        cards
          .map(function (card) {
            return card.dataset[key];
          })
          .filter(Boolean),
      ),
    )
      .sort(function (a, b) {
        return a.localeCompare(b, "es");
      })
      .forEach(function (value) {
        select.add(new Option(value, value));
      });
  }
  options(province, "provincia");
  options(stage, "etapa");
  document.getElementById("mapa-filtros").hidden = false;
  function showAll() {
    if (!map) return;
    var points = cards
      .filter(function (card) {
        return !card.hidden;
      })
      .map(function (card) {
        return coordinates(card.dataset);
      })
      .filter(Boolean);
    if (points.length)
      map.fitBounds(L.latLngBounds(points), {
        padding: [48, 48],
        maxZoom: 13,
        animate: false,
      });
    else map.setView([-1.45, -78.4], 6, { animate: false });
  }
  function highlight(card) {
    cards.forEach(function (item) {
      item.classList.toggle("is-selected", item === card);
    });
  }
  function popup(card) {
    var content = document.createElement("div");
    content.className = "pub-popup";
    var place = document.createElement("span");
    place.className = "pub-place";
    place.textContent = card.dataset.provincia;
    var title = document.createElement("strong");
    title.textContent = card.dataset.nombre;
    var detail = document.createElement("p");
    detail.textContent =
      (card.dataset.huerto || "Huerto por registrar") +
      " · " +
      card.dataset.canton;
    var link = document.createElement("a");
    link.href = card.querySelector(".pub-garden-link").href;
    link.textContent = "Conocer la comunidad ↗";
    content.append(place, title, detail, link);
    return content;
  }
  if (typeof L !== "undefined") {
    try {
      map = makeMap(mapNode).setView([-1.45, -78.4], 6);
      tiles(map, "mapa-aviso");
      L.control.scale({ imperial: false, position: "bottomleft" }).addTo(map);
      layer = L.layerGroup().addTo(map);
      cards.forEach(function (card) {
        var point = coordinates(card.dataset);
        if (!point) return;
        var marker = L.marker(point, {
          icon: icon(),
          title: card.dataset.nombre,
          alt: card.dataset.nombre,
          keyboard: true,
        }).bindPopup(popup(card));
        marker.on("click", function () {
          highlight(card);
          card.scrollIntoView({
            block: "nearest",
            behavior: reduced ? "auto" : "smooth",
          });
        });
        markers.set(card, marker);
        var button = card.querySelector(".pub-locate");
        button.hidden = false;
        button.addEventListener("click", function () {
          highlight(card);
          mapNode.scrollIntoView({
            block: "center",
            behavior: reduced ? "auto" : "smooth",
          });
          map.setView(point, 14, { animate: !reduced });
          marker.openPopup();
        });
      });
      document.getElementById("ver-todos").hidden = false;
      document.getElementById("ver-todos").addEventListener("click", showAll);
      if (markers.size < cards.length)
        notice(
          "mapa-aviso",
          "Algunas comunidades no tienen coordenadas válidas. Sus fichas están disponibles en la lista.",
        );
    } catch (error) {
      map = null;
      listOnly(
        "El mapa no se pudo iniciar. Puedes buscar y abrir las fichas desde la lista.",
      );
    }
  } else {
    listOnly(
      "El mapa no está disponible. Puedes buscar y abrir todas las fichas desde la lista.",
    );
  }
  function filter(writeUrl) {
    var query = normalize(search.value);
    var visible = 0;
    if (map) {
      map.closePopup();
      layer.clearLayers();
    }
    cards.forEach(function (card) {
      var data = card.dataset;
      var text = normalize(
        [data.nombre, data.huerto, data.canton, data.provincia].join(" "),
      );
      card.hidden = !(
        text.includes(query) &&
        (!province.value || data.provincia === province.value) &&
        (!stage.value || data.etapa === stage.value)
      );
      card.classList.remove("is-selected");
      if (!card.hidden) {
        visible++;
        if (map && markers.has(card)) layer.addLayer(markers.get(card));
      }
    });
    document.getElementById("sin-resultados").hidden =
      visible !== 0 || cards.length === 0;
    document.getElementById("mapa-resultados").textContent =
      visible + (visible === 1 ? " comunidad" : " comunidades");
    if (writeUrl) {
      var url = new URL(window.location.href);
      [
        ["q", search.value.trim()],
        ["provincia", province.value],
        ["etapa", stage.value],
      ].forEach(function (pair) {
        if (pair[1]) url.searchParams.set(pair[0], pair[1]);
        else url.searchParams.delete(pair[0]);
      });
      window.history.replaceState(null, "", url);
    }
    showAll();
  }
  function readUrl() {
    var params = new URLSearchParams(window.location.search);
    search.value = (params.get("q") || "").slice(0, 150);
    province.value = params.get("provincia") || "";
    stage.value = params.get("etapa") || "";
    filter(false);
  }
  document
    .getElementById("mapa-filtros")
    .addEventListener("submit", function (event) {
      event.preventDefault();
      filter(true);
    });
  search.addEventListener("input", function () {
    filter(true);
  });
  province.addEventListener("change", function () {
    filter(true);
  });
  stage.addEventListener("change", function () {
    filter(true);
  });
  document
    .getElementById("limpiar-filtros")
    .addEventListener("click", function () {
      search.value = "";
      province.value = "";
      stage.value = "";
      filter(true);
      search.focus();
    });
  window.addEventListener("popstate", readUrl);
  readUrl();
})();
