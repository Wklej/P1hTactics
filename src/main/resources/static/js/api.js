const port = 8080
const hostname = window.location.hostname

function registerUser() {
    const username = document.getElementById('username').value;
    const gameName = document.getElementById('gameName').value;
    const riotTag = document.getElementById('riotTag').value;
    const password = document.getElementById('password').value;
    const url = `http://${hostname}:${port}/api/register`
    const requestBody = {username: username, gameName: gameName,
        riotTag: riotTag, password: password}

    fetch(url, {
        method: 'POST',
        headers: {
            'Content-type': "application/json"
        },
        body: JSON.stringify(requestBody)
    })
        .then(res => res.json())
        .then(data => console.log(data))
        .then(() => {
            document.getElementById('username').value = '';
            document.getElementById('gameName').value = '';
            document.getElementById('riotTag').value = '';
            document.getElementById('password').value = '';
        })
}

function getUsers() {
    fetch(`http://${hostname}:${port}/api/getUsers`)
        .then(res => res.json())
        .then(users => appendUsers(users))
}

async function getUser(gameName) {
    const response = await fetch(`http://${hostname}:${port}/api/getUser/${gameName}`)
    return await response.json()
}

async function calculateAverage() {
    const selectedUser = document.getElementById("userSelect").value
    const gameMode = document.getElementById("modeSelect").value
    const limit = document.getElementById("matchLimit").value
    let user
    try {
        user = await getUser(selectedUser)
        console.log(user)
    } catch (e) {
        throw new Error(e)
    }

    const url = `http://${hostname}:${port}/history/avg/${user.gameName}/${user.tag}/${gameMode}?limit=${encodeURIComponent(limit)}`
    fetch(url)
        .then(res => res.text())
        .then(avg => showResult(avg))

}

function addFriend() {
    const friendGameName = document.getElementById("friendGameName").value
    const friendRiotTag = document.getElementById("friendRiotTag").value
    const url = `http://${hostname}:${port}/api/register/friend`
    const requestBody = {gameName: friendGameName, tag: friendRiotTag}

    fetch(url, {
        method: 'POST',
        headers: {
            'Content-type': "application/json"
        },
        body: JSON.stringify(requestBody)
    })
        .then(res => res.json())
        .then(data => console.log(data))
        .then(() => {
            document.getElementById('friendGameName').value = '';
            document.getElementById('friendRiotTag').value = '';
        })
}

function loadRanking() {
    fetch(`http://${hostname}:${port}/api/getRanking`)
        .then(res => res.json())
        .then(ranking => createRanking(ranking))
}

function loadEvent() {
    fetch(`http://${hostname}:${port}/api/getEventResults`)
        .then(res => res.json())
        .then(events => createEvents(events))
}

function loadFriendList() {
    fetch(`http://${hostname}:${port}/api/friendList`)
        .then(res => res.json())
        .then(friends => createFriendList(friends))
}
