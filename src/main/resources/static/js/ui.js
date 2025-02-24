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

function createRanking(ranking) {
    const table = document.getElementById('ranking-table')
    // Clear existing rows (except header)
    document.querySelectorAll('.ranking-row')
        .forEach(row => row.remove())

    // Populate table with fetched data
    ranking.forEach(summoner => {
        const row = document.createElement('div')
        row.classList.add('ranking-row')

        const nameColumn = document.createElement('span')
        nameColumn.classList.add('column-item')
        nameColumn.textContent = summoner.summonerName

        const averageColumn = document.createElement('span')
        averageColumn.classList.add('column-item')
        averageColumn.textContent = summoner.avg

        row.appendChild(nameColumn)
        row.appendChild(averageColumn)
        table.appendChild(row)
    })
}

function createEventResults(results) {
    const table = document.getElementById('event-table')
    // Clear existing rows (except header)
    document.querySelectorAll('.event-row')
        .forEach(row => row.remove())

    // Populate table with fetched data
    results.forEach(result => {
        const row = document.createElement('div')
        row.classList.add('event-row')

        const nameColumn = document.createElement('span')
        nameColumn.classList.add('column-item')
        nameColumn.textContent = result.summonerName

        const averageColumn = document.createElement('span')
        averageColumn.classList.add('column-item')
        averageColumn.textContent = result.avg

        const gamesCountColumn = document.createElement('span')
        gamesCountColumn.classList.add('column-item')
        gamesCountColumn.textContent = result.games

        row.appendChild(nameColumn)
        row.appendChild(averageColumn)
        row.appendChild(gamesCountColumn)
        table.appendChild(row)

        const title = document.getElementById('event-title')
        title.innerText = result.eventInfo.title
        const start = document.getElementById('event-start')
        start.innerText = 'Starts at: ' + result.eventInfo.start
        const end = document.getElementById('event-end')
        end.innerText = 'Ends at: ' + result.eventInfo.end
    })
}
