const icons = {

    "Clear": "clear.svg",
    "Partly cloudy": "partly-cloudy.svg",
    "Foggy": "fog.svg",
    "Rainy": "rain.svg",
    "Snowy": "snow.svg",
    "Rain showers": "rain.svg",
    "Thunderstorm": "thunderstorm.svg"

};

fetch('/api/weather').then(response=>response.json()).then(data=> {

    document.getElementById('temp').textContent = Math.round(data.temperature) + "°C";

    document.getElementById('condition').textContent = data.condition;

    document.getElementById('icon').src = "/images/" + (icons[data.condition] || "clear.svg");
    
    document.getElementById('feelsLike').textContent = Math.round(data.feelsLike) + "°C";
    
    document.getElementById('humidity').textContent = data.humidity + "%";
    
    document.getElementById('wind').textContent = Math.round(data.windSpeed) + " km/h";
    
    document.getElementById('rain').textContent = data.rainChance + "%";
}).catch (error=> {
    document.getElementById('condition').textContent = "Could not load weather";
    console.log(error);

})
