async function loadTransit() {
    try {
      const response = await fetch("/api/transit");
      const data = await response.json();
      document.getElementById("stopName").textContent = data.stop;

      const busList = document.getElementById("busList");
      busList.innerHTML = "";

      data.buses.forEach(bus => {
      const busRow = document.createElement("div");
      busRow.className = "bus-row";

      busRow.innerHTML = `
            <div class="route">${bus.route}</div>
            <div class="destination">${bus.destination}</div>
            <div class="minutes">${bus.minutes} min</div>
          `;

          busList.appendChild(busRow);
        });

      } catch (error) {
        document.getElementById("busList").innerHTML =
          '<div class="loading">Transit information unavailable</div>';
      }
    }

    loadTransit();
    setInterval(loadTransit, 60000);