# Elo Ranker for Movies and Shows
The Elo Ranker for Movies and Shows is a Java-based program that utilizes the ELO ranking technique to evaluate and rank movies and TV shows based on user preferences. The system uses a PostgreSQL database to store user ratings. The JDBC library is used to connect to the database.

## Features
* Create a new table: The program can create a new table to store movies and show data. Personally, I use it to separate korean dramas from english shows.
* List tables: The program can list all the tables in the database. You can then choose from the table list to enter and edit the table.
* Enter a new entry: Users can enter a new movie or show in the database and it will then prompt the user to rank it against random entries in the table.
* Search the table: The user is able to search the table by title and if it exist, all the information will be displayed.
* Random and ranked matches: Users can select to either have a random match, potentially pitting a 200 ELO again a 1000 ELO, or a ranked match, where the ELOs will be similar.
* View Ranking: Users can view the current ranking of movies and shows. All, the top 10, or the bottom 10 can se shown.

## Getting Started
The Movie and Show ELO Ranker requires Java 8 or higher and a PostgreSQL database to run. Clone this repository and import the project into your favorite IDE.

In main.java, update the db_url, user, and pass with the appropriate database connection details.

## Usage
The Movie and Show ELO Ranker can be used through the command line interface. Once started, the program will prompt you with question. Answer the questions accordingly and the program should work.
1. Open a terminal window.
2. Navigate to the directory that contains the files.
3. Compile the java files by running the following command:
```
javac Main.java
```
4. Run the compiled Java program by running the command:
```
java -cp "postgresql-42.6.0.jar;." Main
```

## Example

## Contributions
Contributions are welcome! Please open a pull request with your changes or bug fixes.

## License
MIT License

Copyright (c) [2021] [Samantha Xiong]

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.