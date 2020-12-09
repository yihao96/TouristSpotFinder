// Initialize Firebase
var config = {
    apiKey: "REMOVED",
    authDomain: "tourist-spot-finder-4249d.firebaseapp.com",
    databaseURL: "https://tourist-spot-finder-4249d.firebaseio.com",
    projectId: "tourist-spot-finder-4249d",
    storageBucket: "tourist-spot-finder-4249d.appspot.com",
    messagingSenderId: "770924790408"
};
firebase.initializeApp(config);

var db = firebase.firestore();
const locationList = document.querySelector("#location-list");

// create element and render cafe
function renderLocation(doc) {
    let li = document.createElement("li");
    let name = document.createElement("span");
    let address = document.createElement("span");
    let category = document.createElement("span");
    let desc = document.createElement("span");

    li.setAttribute("data-id", doc.id);
    name.textContent = doc.data().name;
    address.textContent = doc.data().address;
    category.textContent = doc.data().category;
    desc.textContent = doc.data().description;
    let isExist = doc.data().exist;

    li.appendChild(name);
    li.appendChild(address);
    li.appendChild(category);
    li.appendChild(desc);
    
    if (!isExist) {
        let cross = document.createElement("i");
        cross.setAttribute("class", "material-icons red md-36")
        cross.textContent = "delete";
        li.appendChild(cross);
        cross.addEventListener("click", (e) => {
            let id = e.target.parentElement.getAttribute("data-id");
            db.collection("location").doc(id).delete();
        })
    }
    locationList.appendChild(li);
}

getRealtimeUpdates = function () {
    db.collection("location").orderBy("exist")
        .get()
        .then(function (querySnapshot) {
            querySnapshot.docs.forEach(doc => {
                renderLocation(doc);
            });
        })
        .catch(error => {
            console.log("Error getting documents: ", error);
        });
};

thisIsRealtimeData = function () {
    db.collection("location").orderBy("name").onSnapshot(snapshot => {
        let changes = snapshot.docChanges();
        console.log(changes);
        changes.forEach(change => {
            if (change.type == "added") {
                renderLocation(change.doc);
            } else if (change.type == "removed") {
                let li = locationList.querySelector("[data-id=" + change.doc.id + "]");
                locationList.removeChild(li);
            } else if (change.type == "modified") {
                let li = locationList.querySelector("[data-id=" + change.doc.id + "]");
                locationList.removeChild(li);
                renderLocation(change.doc);
            }
        });
    });
}

// getRealtimeUpdates();
thisIsRealtimeData();
