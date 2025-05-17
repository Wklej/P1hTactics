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
        .then(res => {
            if (!res.ok) {
                return res.json().then(err => { throw err; })
            }
            return res.json()
        })
        .then(() => {
            document.getElementById('username').value = '';
            document.getElementById('gameName').value = '';
            document.getElementById('riotTag').value = '';
            document.getElementById('password').value = '';
            window.location.href = "login.html"
        })
        .catch(error => {
            document.getElementById("errorMessage").textContent = error.error
        })
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
        .then(res => {
            if (!res.ok) {
                return res.json().then(err => { throw err; })
            }
            return res.json()
        })
        .then(() => {
            document.getElementById('friendGameName').value = '';
            document.getElementById('friendRiotTag').value = '';
        })
        .catch(error => {
            document.getElementById("errorMessageFriend").textContent = error.error
        })
}

function getUsers() {
    fetch(`http://${hostname}:${port}/api/getCurrentUserAndFriends`)
        .then(res => res.json())
        .then(users => {
            appendUsers(users)
            appendUsersToHistorySelect(users)
        })
}

async function getUser(gameName) {
    const response = await fetch(`http://${hostname}:${port}/api/getUser/${gameName}`)
    return await response.json()
}

async function getCurrentLoggedUser() {
    const response = await fetch(`http://${hostname}:${port}/api/getCurrentUser`)
    return await response.text()
}

async function calculateAverage() {
    const selectedUser = document.getElementById("userSelect").value
    const gameMode = document.getElementById("modeSelect").value
    const limit = document.getElementById("matchLimit").value
    const set = document.getElementById("setSelect").value
    let user
    try {
        user = await getUser(selectedUser)
        console.log(user)
    } catch (e) {
        throw new Error(e)
    }

    const url = `http://${hostname}:${port}/history/avg/${user.gameName}/${user.tag}/${gameMode}/${set}?limit=${encodeURIComponent(limit)}`
    fetch(url)
        .then(res => res.text())
        .then(avg => showResult(avg))

}

function loadRanking() {
    const set = document.getElementById('rankingSetSelect').value
    const mode = document.getElementById('rankingModeSelect').value

   loadRanked(set, mode)
}

function loadRanked(selectedSet, selectedMode) {
    fetch(`http://${hostname}:${port}/api/getRanking/${selectedSet}/${selectedMode}`)
        .then(res => res.json())
        .then(ranking => createRanking(ranking))

    fetch(`http://${hostname}:${port}/api/ranking/bestDuo/${selectedSet}`)
        .then(res => res.json())
        .then(results => createRankingDuo(results, selectedMode))
}

async function loadEvent() {
    const currentUser = await getCurrentLoggedUser()
    fetch(`http://${hostname}:${port}/api/getEventResults`)
        .then(res => res.json())
        .then(events => createEvents(events, currentUser))
}

function loadFriendList() {
    fetch(`http://${hostname}:${port}/api/friendList`)
        .then(res => res.json())
        .then(friends => createFriendList(friends))
}

function loadUserInfo() {
    fetch(`http://${hostname}:${port}/api/userInfo`)
        .then(res => res.json())
        .then(info => createUserInfo(info))
}

function signUpForEvent(eventTitle, loggedUser) {
    console.log("signing up: " + loggedUser + " for event: " + eventTitle)
    const url = `http://${hostname}:${port}/event/addParticipant`
    const requestBody = {title: eventTitle, username: loggedUser}

    fetch(url, {
        method: 'POST',
        headers: {
            'Content-type': "application/json"
        },
        body: JSON.stringify(requestBody)
    })
        .then(() => {
            location.reload()
        })
}

function loadHistory(selectedSummoner) {
    fetch(`http://${hostname}:${port}/api/matchHistory/${selectedSummoner}`)
        .then(res => res.json())
        .then(history => createMatchHistoryTable(history))
}