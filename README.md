# P1hTactics

**P1hTactics** is a web application for **Teamfight Tactics (TFT)** players that helps track performance, compare stats with friends, and compete in community events. The platform aggregates data from Riot’s API, providing users with valuable insights into their matches and gameplay trends.

## Features

- 🧾 **Match History Viewer**  
  View detailed match history for yourself or added friends, including:
  - Placement
  - Game mode
  - Traits (team comp) played

- 📊 **Average Placement Calculator**  
  Calculate your average placement over a selected number of games and game mode.

- 👥 **Friends System**  
  Add friends to view their match history and compare results.

- 🏆 **Events Tab**  
  Join time-limited events and compete against others. Example events include:
  - **Best Average Placement**
  - **TOP/BOTTOM count**

## Architecture Overview

The application is composed of three core components:

1. **MongoDB**  
   Stores user profiles, match history, and event data.

2. **P1hTactics**  
   A web-based UI container that displays aggregated match data, visual analytics, and event participation interfaces.

3. **MatchScrapper**  
   A backend service that regularly interacts with Riot’s API to fetch the latest match data for users and their friends.

## Local Development and Deployment

To deploy the application locally, we use Docker and `docker-compose`.

### Prerequisites

- [Docker](https://docs.docker.com/get-docker/)
- [Docker Compose](https://docs.docker.com/compose/install/)

### Setup Instructions

1. **Clone the Repository**

   ```bash
   git clone https://github.com/wklej/P1hTactics.git
   ```

2. **Set Environment Variables**

   Configure the following AWS-related variables in docker-compose file:

   ```env
   AWS_ACCESS_KEY_ID=your_aws_access_key_id
   AWS_SECRET_ACCESS_KEY=your_aws_secret_access_key
   ```

   This is necessary because Riot API key is held in public AWS secrets storage.

3. **Run Docker Compose**

   Start the full stack using:

   ```bash
   docker-compose up
   ```
    (File is located in src/main/resources)

   This will start:
   - The **MongoDB** database
   - The **P1hTactics** frontend
   - The **MatchScrapper** backend service

4. **Access the Application**

   Once everything is running, open your browser and navigate to:

   ```
   http://localhost:8080
   ```

## Global access

I am also hosting application on http://p1htactics.link, but it was mostly built for me and my friends personal usage, hence it is not always available.

## Contributing

Contributions are welcome! If you want to add a feature or fix a bug, please fork the repository and open a pull request.

## License

[MIT](LICENSE)

---

Feel free to open an issue if you encounter any problems while running the application locally.
