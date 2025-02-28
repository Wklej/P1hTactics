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

function createEventResult(result) {
    //create header
    const header = document.getElementById('event-header')
    header.innerText = ""

    const title = document.createElement('div')
    title.classList.add('event-title')
    title.innerText = result.eventInfo.title
    const start = document.createElement('div')
    start.classList.add('event-start')
    start.innerText = 'Starts at: ' + result.eventInfo.start
    const end = document.createElement('div')
    end.classList.add('event-end')
    end.innerText = 'Ends at: ' + result.eventInfo.end
    header.appendChild(title)
    header.appendChild(start)
    header.appendChild(end)

    const table = document.getElementById('event-table')
    // Clear existing rows (except header)
    document.querySelectorAll('.event-row')
        .forEach(row => row.remove())

    // Populate table with fetched data
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
}

function createEventSections(results) {
    const eventContainer = document.getElementById('tab-content');

    // Remove previously generated event sections
    document.querySelectorAll('.dynamic-event-section').forEach(section => section.remove());

    Object.keys(results).forEach(eventKey => {
        const eventData = results[eventKey];

        const eventSection = document.createElement('div');
        eventSection.classList.add('container', 'hidden', 'dynamic-event-section');

        const title = document.createElement('div');
        title.classList.add('event-title');
        title.textContent = eventData.avgResults ? eventData.avgResults[0].eventInfo.title
            : eventData.placementCounts[0].eventInfo.title;

        const start = document.createElement('div');
        start.classList.add('event-start');
        start.textContent = 'Starts at: ' +
            (eventData.avgResults ? eventData.avgResults[0].eventInfo.start : eventData.placementCounts[0].eventInfo.start);

        const end = document.createElement('div');
        end.classList.add('event-end');
        end.textContent = 'Ends at: ' +
            (eventData.avgResults ? eventData.avgResults[0].eventInfo.end : eventData.placementCounts[0].eventInfo.end);

        eventSection.appendChild(title);
        eventSection.appendChild(start);
        eventSection.appendChild(end);

        const table = document.createElement('div');
        table.classList.add('ranking-table');

        const header = document.createElement('div');
        header.classList.add('ranking-header');

        if (eventData.avgResults) {
            const nameColumn = document.createElement('span');
            nameColumn.classList.add('column-item');
            nameColumn.textContent = "Summoner";

            const averageColumn = document.createElement('span');
            averageColumn.classList.add('column-item');
            averageColumn.textContent = "Average Placement";

            const gamesColumn = document.createElement('span');
            gamesColumn.classList.add('column-item');
            gamesColumn.textContent = "Games";

            header.appendChild(nameColumn);
            header.appendChild(averageColumn);
            header.appendChild(gamesColumn);
            table.appendChild(header);

            eventData.avgResults.forEach(result => {
                const row = document.createElement('div');
                row.classList.add('event-row');

                const nameCell = document.createElement('span');
                nameCell.classList.add('column-item');
                nameCell.textContent = result.summonerName;

                const avgCell = document.createElement('span');
                avgCell.classList.add('column-item');
                avgCell.textContent = result.avg.toFixed(2);

                const gamesCell = document.createElement('span');
                gamesCell.classList.add('column-item');
                gamesCell.textContent = result.games;

                row.appendChild(nameCell);
                row.appendChild(avgCell);
                row.appendChild(gamesCell);
                table.appendChild(row);
            });
        } else if (eventData.placementCounts) {
            const nameColumn = document.createElement('span');
            nameColumn.classList.add('column-item');
            nameColumn.textContent = "Summoner";

            const topColumn = document.createElement('span');
            topColumn.classList.add('column-item');
            topColumn.textContent = "Top 1 Finishes";

            const bottomColumn = document.createElement('span');
            bottomColumn.classList.add('column-item');
            bottomColumn.textContent = "Bottom 8 Finishes";

            const gamesColumn = document.createElement('span');
            gamesColumn.classList.add('column-item');
            gamesColumn.textContent = "Games";

            header.appendChild(nameColumn);
            header.appendChild(topColumn);
            header.appendChild(bottomColumn);
            header.appendChild(gamesColumn);
            table.appendChild(header);

            eventData.placementCounts.forEach(result => {
                const row = document.createElement('div');
                row.classList.add('event-row');

                const nameCell = document.createElement('span');
                nameCell.classList.add('column-item');
                nameCell.textContent = result.summonerName;

                const topCell = document.createElement('span');
                topCell.classList.add('column-item');
                topCell.textContent = result.top;

                const bottomCell = document.createElement('span');
                bottomCell.classList.add('column-item');
                bottomCell.textContent = result.bottom;

                const gamesCell = document.createElement('span');
                gamesCell.classList.add('column-item');
                gamesCell.textContent = result.games;

                row.appendChild(nameCell);
                row.appendChild(topCell);
                row.appendChild(bottomCell);
                row.appendChild(gamesCell);
                table.appendChild(row);
            });
        }

        eventSection.appendChild(table);
        eventContainer.appendChild(eventSection);
    });
}
