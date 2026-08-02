let currentStop = "sfu";

async function loadTransit() {
  try {
    const response = await fetch(`/api/transit?stop=${currentStop}`);
    const data = await response.json();

    document.getElementById("stopName").textContent = data.stop;

    const label = document.getElementById("transitLabel");

    const list = document.getElementById("busList");
    list.innerHTML = "";

    if (data.type === "bus") {
      label.textContent = "NEXT BUSES";

      data.buses.forEach(bus => {
        list.innerHTML += `
          <div class="bus-row">
            <div class="route">${bus.route}</div>
            <div class="destination">${bus.destination}</div>
            <div class="minutes">${bus.minutes} min</div>
          </div>
        `;
      });

    } else if (data.type === "train") {
      label.textContent = "NEXT TRAINS";

      data.trains.forEach(train => {
        list.innerHTML += `
          <div class="bus-row">
            <div class="route">🚆</div>
            <div class="destination">${train.destination}</div>
            <div class="minutes">${train.minutes} min</div>
          </div>
        `;
      });
    }

  } catch (error) {
    document.getElementById("busList").innerHTML = '<div class="loading">Transit information unavailable. </div>';
  }
}

// tab switching
document.querySelectorAll(".transit-tab").forEach(tab => {
  tab.addEventListener("click", () => {

    document.querySelectorAll(".transit-tab")
      .forEach(t => t.classList.remove("active"));

    tab.classList.add("active");

    currentStop = tab.dataset.stop;

    loadTransit();
  });
});


loadTransit();
setInterval(loadTransit, 60000);