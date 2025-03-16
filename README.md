# Overview 
XRP Extractor is a Java/Gradle application that analyzes XRP ledgers to extract specific transaction data and store it in a database. It periodically fetches the latest blocks, parses the transactions, and filters out only the ones you need.

## Key Features
- XRP Ledger Analysis: Fetch ledger data and parse transactions.
- Transaction Filtering: Extract only the desired transactions (e.g., Payments).
- Database Insertion: Store extracted transactions in a DB.
- Scheduling: Automatically update and parse ledger data at scheduled intervals.

## Directory Structure
```
.
├── gradle/wrapper
│   ├── gradle-wrapper.jar
│   └── gradle-wrapper.properties
├── src
│   └── main/java/kr/traverse/xrpextractor
│       ├── component/
│       │   └── XrpWebClient.java
│       ├── configuration/
│       │   ├── ScheduledConfiguration.java
│       │   └── Xrp4jConfiguration.java
│       ├── xrpl/
│       │   ├── dto/
│       │   │   ├── LedgerDto.java
│       │   │   ├── PaymentDto.java
│       │   │   ├── XrpInsertDto.java
│       │   │   └── XrpInsertRequest.java
│       │   ├── XrplDataPuller.java
│       │   ├── XrplParser.java
│       │   └── XrpExtractorApplication.java
│   └── test/java/kr/traverse/xrpextractor
├── build.gradle
├── gradlew
├── gradlew.bat
└── settings.gradle
```

- component/
  - XrpWebClient.java: Handles HTTP API calls to the DB Insert API Server.
- configuration/
  - ScheduledConfiguration.java: Manages scheduling for periodic data fetching.
  - Xrp4jConfiguration.java: Configures xrp4j or other relevant libraries.
- xrpl/
  - dto/: Data Transfer Objects (LedgerDto, PaymentDto, XrpInsertDto, XrpInsertRequest) representing ledger and transaction data.
  - XrplDataPuller.java: Core logic for fetching data from the XRP ledger.
  - XrplParser.java: Parses and filters the fetched ledger data.
  - XrpExtractorApplication.java: Main entry point of the Spring Boot or Java application.
- test/
  - Contains unit and integration tests (if implemented).

## Build & Run
1. Clone the Project
```
git clone https://github.com/your-org/xrp-extractor.git
cd xrp-extractor
```

2. Install Dependencies & Build
```
./gradlew clean build
```
On Windows: gradlew.bat clean build

3. Run the Application
```
./gradlew bootRun
```
- java -jar build/libs/xrp-extractor-0.0.1-SNAPSHOT.jar (filename may vary)

4. Configure the Database
Update your application.properties or application.yml with DB connection details:
```
xrpl.url=your URL
neo4j.back.url=your URL
neo4j.back.port=your port
```
You need to modify the database-related settings according to your own configuration.

## Usage
- Scheduled Tasks
  ScheduledConfiguration.java triggers XrplDataPuller at defined intervals, which then uses XrplParser to extract desired transactions and store them in the database.

- Filtering Logic
  Extend the filtering logic in XrplParser.java to process only specific transaction types (e.g., Payment) or addresses.

## License
This project is currently an internal component for our company's React application and is under active development. No public license is provided at this time.

## Contact
If you have any questions or suggestions, please open an issue on GitHub or contact us at [jin@traverse.kr].
