function initPage() {
    tabsHandler()
    getUsers()
    loadRanking()
}

function tabsHandler() {
    const tabButtons = document.querySelectorAll('.tab-button')
    const tabContents = document.querySelectorAll('.container')

    tabButtons.forEach(button => {
        button.addEventListener('click', () => {
            // Remove 'active' class from all buttons
            tabButtons.forEach(btn => btn.classList.remove('active'))
            // Add 'active' class to the clicked button
            button.classList.add('active')

            // Hide all tab contents
            tabContents.forEach(content => content.classList.add('hidden'))
            // Show the content for the selected tab
            const targetId = button.getAttribute('data-target')
            document.getElementById(targetId).classList.remove('hidden')
        });
    });
}

window.onload = initPage;