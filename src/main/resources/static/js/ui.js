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

function createRankingDuo(ranking, mode) {
    const rankingSection = document.getElementById('ranking-section')
    const rankingTable = document.createElement('div')
    rankingTable.classList.add("ranking-table")
    mode === "1160" ? rankingTable.classList.remove("hidden") : rankingTable.classList.add("hidden")
    rankingTable.id = "ranking-table-duo"
    const header = createRankingDuoHeader()
    rankingTable.appendChild(header)

    const alreadyCreatedTable = document.getElementById("ranking-table-duo")
    if (alreadyCreatedTable !== null) {
        alreadyCreatedTable.remove()
    }

    ranking.forEach(result => {
        const row = document.createElement('div')
        row.classList.add('ranking-row-duo')

        const duoColumn = document.createElement('span')
        duoColumn.classList.add('column-item')
        duoColumn.textContent = `${result.duo.summoner1} and ${result.duo.summoner2}`

        const averageColumn = document.createElement('span')
        averageColumn.classList.add('column-item')
        averageColumn.textContent = result.avg

        row.appendChild(duoColumn)
        row.appendChild(averageColumn)
        rankingTable.appendChild(row)
    })

    rankingSection.appendChild(rankingTable)
}

function createRankingDuoHeader() {
    const header = document.createElement('div')
    header.classList.add('ranking-header')
    const duoSpan = document.createElement('span')
    duoSpan.classList.add('column-item')
    duoSpan.textContent = "Duo"
    const avgSpan = document.createElement('span')
    avgSpan.classList.add('column-item')
    avgSpan.textContent = "Average Placement"
    header.appendChild(duoSpan)
    header.appendChild(avgSpan)
    return header
}

function createEvents(events, currentLoggedUser) {
    Object.values(events).forEach(eventResults => createEvent(eventResults, currentLoggedUser))
}

function createEvent(eventResults, currentLoggedUser) {
    const eventName = eventResults.title
    const results = eventResults.eventResults
    const isSignedUp = eventResults.length !== 0
        ? results.map(result => result.summonerName).includes(currentLoggedUser)
        : false

    //creating event header
    createEventHeader(eventResults, isSignedUp, currentLoggedUser)

    if (isSignedUp) {
        //creating ranking table to be filled with event results of each Summoner
        const table = createRankingTable(eventName)

        // Clear existing rows (except header)
        // document.querySelectorAll('.event-row')
        //     .forEach(row => row.remove())

        //Create results for each summoner
        results.forEach(result => {
            // Populate table with fetched data
            const row = document.createElement('div')
            row.classList.add('event-row')

            eventName === "avg"
                ? createColumnsForAvgEvent(row, result)
                : createColumnsForPlacementEvent(row, result)

            table.appendChild(row)
        })
    }
}

function createEventHeader(eventResults, isSignedUp, currentLoggedUser) {
    const eventSection = document.getElementById('event-section')
    const header = document.createElement('event-header')

    const title = document.createElement('div')
    title.classList.add('event-title')
    title.innerText = eventResults.title
    const start = document.createElement('div')
    start.classList.add('event-start')
    start.innerText = 'Starts at: ' + eventResults.start
    const end = document.createElement('div')
    end.classList.add('event-end')
    end.innerText = 'Ends at: ' + eventResults.end
    header.appendChild(title)
    header.appendChild(start)
    header.appendChild(end)

    if (!isSignedUp) {
        const signUpButton = document.createElement('button')
        signUpButton.innerText = 'Sign Up'
        signUpButton.classList.add('event-button')
        signUpButton.type = 'submit'
        signUpButton.onclick = () => signUpForEvent(title.innerText, currentLoggedUser)
        header.appendChild(signUpButton)
    }

    eventSection.appendChild(header)
}

function createRankingTable(eventName) {
    const eventSection = document.getElementById('event-section')
    const table = document.createElement('div')
    table.classList.add('ranking-table')

    const rankingHeader = document.createElement('div')
    rankingHeader.classList.add('ranking-header')

    eventName === "avg"
        ? createRankingTableForAvgEvent(rankingHeader)
        : createRankingTableForPlacementEvent(rankingHeader)


    table.appendChild(rankingHeader)
    eventSection.appendChild(table)

    return table
}

function createRankingTableForAvgEvent(rankingHeader) {
    const summonerColumn = document.createElement('span')
    summonerColumn.classList.add('column-item')
    summonerColumn.textContent = 'Summoner'

    const avgPlacementColumn = document.createElement('span')
    avgPlacementColumn.classList.add('column-item')
    avgPlacementColumn.textContent = 'Average Placement'

    const gamesColumn = document.createElement('span')
    gamesColumn.classList.add('column-item')
    gamesColumn.textContent = 'Games'

    rankingHeader.appendChild(summonerColumn)
    rankingHeader.appendChild(avgPlacementColumn)
    rankingHeader.appendChild(gamesColumn)
}

function createRankingTableForPlacementEvent(rankingHeader) {
    const summonerColumn = document.createElement('span')
    summonerColumn.classList.add('column-item')
    summonerColumn.textContent = 'Summoner'

    const topColumn = document.createElement('span')
    topColumn.classList.add('column-item')
    topColumn.textContent = 'Top'

    const bottomColumn = document.createElement('span')
    bottomColumn.classList.add('column-item')
    bottomColumn.textContent = 'Bottom'

    const gamesColumn = document.createElement('span')
    gamesColumn.classList.add('column-item')
    gamesColumn.textContent = 'Games'

    rankingHeader.appendChild(summonerColumn)
    rankingHeader.appendChild(topColumn)
    rankingHeader.appendChild(bottomColumn)
    rankingHeader.appendChild(gamesColumn)
}

function createColumnsForAvgEvent(row, result) {
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
}

function createColumnsForPlacementEvent(row, result) {
    const nameColumn = document.createElement('span')
    nameColumn.classList.add('column-item')
    nameColumn.textContent = result.summonerName

    const topColumn = document.createElement('span')
    topColumn.classList.add('column-item')
    topColumn.textContent = result.top

    const bottomColumn = document.createElement('span')
    bottomColumn.classList.add('column-item')
    bottomColumn.textContent = result.bottom

    const gamesCountColumn = document.createElement('span')
    gamesCountColumn.classList.add('column-item')
    gamesCountColumn.textContent = result.games

    row.appendChild(nameColumn)
    row.appendChild(topColumn)
    row.appendChild(bottomColumn)
    row.appendChild(gamesCountColumn)
}

function createFriendList(friends) {
    const friendListContainer = document.getElementById("friendList");
    friendListContainer.innerHTML = ""; // Clear previous list

    if (friends.length === 0) {
        return;
    }

    const tooltip = document.createElement('div')
    tooltip.id = 'tooltip'
    tooltip.className = 'tooltip hidden'
    friendListContainer.appendChild(tooltip);

    friends.forEach(friend => {
        const listItem = document.createElement("li");
        listItem.textContent = `${friend.friend.gameName}#${friend.friend.tag}`;
        friendListContainer.appendChild(listItem);

        listItem.addEventListener('mouseover', () => {
            let tooltipContent = ''

            if (friend.rankedStats !== null) {
                tooltipContent += `RANKED Rank: ${friend.rankedStats.tier} ${friend.rankedStats.rank} Points: ${friend.rankedStats.points}<br>`
            }

            if (friend.doubleUpStats !== null) {
                tooltipContent += `DOUBLE-UP Rank: ${friend.doubleUpStats.tier} ${friend.doubleUpStats.rank} Points: ${friend.doubleUpStats.points}`
            }

            tooltip.innerHTML = tooltipContent
            tooltip.classList.remove('hidden')
        })

        listItem.addEventListener('mouseout', () => {
            tooltip.classList.add('hidden')
        })
    });
}

function createUserInfo(info) {
    const name = document.getElementById("currentUserName")
    name.innerText = `${info.friend.gameName}#${info.friend.tag}`;
    const rank = document.getElementById("currentUserRank")
    let rankContent = ''

    if (info.rankedStats !== null) {
        rankContent += `RANKED: ${info.rankedStats.tier} ${info.rankedStats.rank} ${info.rankedStats.points}lp <br>`
    }
    if (info.doubleUpStats !== null) {
        rankContent += `DOUBLE-UP: ${info.doubleUpStats.tier} ${info.doubleUpStats.rank} ${info.doubleUpStats.points}lp`
    }

    rank.innerHTML = rankContent
}
