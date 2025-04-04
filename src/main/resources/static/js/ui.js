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

function createEvents(events, currentLoggedUser) {
    const eventNames = Object.keys(events)
    eventNames.forEach(eventName => createEvent(events[eventName], eventName, currentLoggedUser))
}

function createEvent(event, eventName, currentLoggedUser) {
    const eventResults = eventName === "avg" ? event["avgResults"] : event["placementCounts"]
    //TODO: should come from function param
    const isSignedUp = eventResults[0].eventInfo.participants.includes(currentLoggedUser)

    //creating event header
    createEventHeader(eventResults, isSignedUp, currentLoggedUser)

    if (isSignedUp) {
        //creating ranking table to be filled with event results of each Summoner
        const table = createRankingTable(eventName)

        // Clear existing rows (except header)
        // document.querySelectorAll('.event-row')
        //     .forEach(row => row.remove())

        //Create results for each summoner
        eventResults.forEach(result => {
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
    title.innerText = eventResults[0].eventInfo.title
    const start = document.createElement('div')
    start.classList.add('event-start')
    start.innerText = 'Starts at: ' + eventResults[0].eventInfo.start
    const end = document.createElement('div')
    end.classList.add('event-end')
    end.innerText = 'Ends at: ' + eventResults[0].eventInfo.end
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
            // tooltip.innerHTML = ""
            tooltip.innerHTML = `Rank: ${friend.stats.rank} Points: ${friend.stats.points}`
            tooltip.classList.remove('hidden')
        })

        listItem.addEventListener('mouseout', () => {
            tooltip.classList.add('hidden')
        })
    });
}
