function appendUsers(users) {
    const userSelect = document.getElementById('userSelect')

    users.forEach(user => {
        const option = document.createElement('option')
        option.value = user.gameName
        option.textContent = user.gameName
        userSelect.appendChild(option)
    })
}

function showResult(avg) {
    const resultDiv = document.getElementById("result")
    resultDiv.innerText = avg
}