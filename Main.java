import java.sql.SQLException;
import java.util.Arrays;
import java.util.Scanner;
import java.util.ArrayList;

public class Main {
    static Scanner scanner = new Scanner(System.in);
    static String input = "What would you like to do? see_tables/restart/quit: ";

    private static boolean check_genres(String input, String[] genres) {
        String[] items = input.split(",\\s+");
        for (String item: items) {
            if (!Arrays.asList(genres).contains(item)) {
                return false;
            }
        }
        return true;
    }

    private static double calculate_expected(int player1, int player2) {
        double difference = player2 - player1;
        double division = (difference) / 400;
        double exponent = Math.pow(10, division);
        double denominator = 1 + exponent;
        return 1 / denominator;
    }

    private static int[] calculate_elo(int player1, int player2, boolean won) {
        double expected1 = calculate_expected(player1, player2);
        double expected2 = calculate_expected(player2, player1);
        int actual1 = won ? 1 : 0;
        int actual2 = won ? 0 : 1;
        double delta1 = 32 * (actual1 - expected1);
        double delta2 = 32 * (actual2 - expected2);
        return new int[]{(int) (player1 + delta1), (int) (player2 + delta2)};
    }

    private static int new_elo(PostgreSQLJDBC jdbc, String title, String table) throws SQLException {
        int player1_elo = 1600;
        for (int i = 0; i < 10; i++) {
            ArrayList<String> player2 = jdbc.randomEntry(table);
            if (player2 == null) {
                break;
            }
            String player2_title = player2.get(0);
            int player2_elo = Integer.parseInt(player2.get(1));

            System.out.print("Is " + title + " better than " + player2_title+ " ? y/n: ");
            input = scanner.nextLine();

            boolean valid_input = new ArrayList<String>(Arrays.asList("y", "n")).contains(input);
            while (!valid_input) {
                System.out.print("Sorry, invalid input! Is " + title + " better than " + player2_title+ " ? y/n: ");
                input = scanner.nextLine();
                if (new ArrayList<String>(Arrays.asList("y", "n")).contains(input)) {
                    valid_input = true;
                }
            }

            boolean player1_won = input.equalsIgnoreCase("y");
            int[] new_elos = calculate_elo(player1_elo, player2_elo, player1_won);
            player1_elo = new_elos[0];
            jdbc.update(table, "elo = " + String.valueOf(new_elos[1]), "id = " + player2.get(2));
        }
        return player1_elo;
    }

    private static void match(PostgreSQLJDBC jdbc, String table, String type, int matches) throws SQLException {
        for (int i = 0; i < matches; i++) {
            ArrayList<ArrayList<String>> players = null;
            while (players == null || players.size() != 2) {
                if (type.equalsIgnoreCase("random")) {
                    players = jdbc.get_random_two(table);
                } else {
                    players = jdbc.get_ranked_two(table);
                }
            }

            ArrayList<String> p1 = players.get(0);
            ArrayList<String> p2 = players.get(1);
            System.out.print("Is " + p1.get(1) + " better than " + p2.get(1) + "? y/n: ");
            input = scanner.nextLine();

            boolean valid_input = new ArrayList<String>(Arrays.asList("y", "n")).contains(input);
            while (!valid_input) {
                System.out.print("Sorry, invalid input! Is " + p1.get(1) + " better than " + p2.get(1) + "? y/n: ");
                input = scanner.nextLine();
                if (new ArrayList<String>(Arrays.asList("y", "n")).contains(input)) {
                    valid_input = true;
                }
            }
            boolean p1_won = input.equalsIgnoreCase("y");
            int[] new_elos = calculate_elo(Integer.parseInt(p1.get(2)), Integer.parseInt(p2.get(2)), p1_won);
            jdbc.update(table, "elo = " + String.valueOf(new_elos[0]), "id = " + p1.get(0));
            jdbc.update(table, "elo = " + String.valueOf(new_elos[1]), "id = " + p2.get(0));
        }
    }

    private static String table_selected(PostgreSQLJDBC jdbc, String table) throws SQLException {
        System.out.print("You have selected " + table + "! Would would you like to do? new/search/match/see_ranks/restart/quit: ");
        input = scanner.nextLine();

        boolean run = true;
        while (run) {
            if (input.equalsIgnoreCase("new")) {
                String[] possible_columns = {"title", "year", "genre", "synopsis", "notable_actors"};
                StringBuilder columns = new StringBuilder();
                StringBuilder values = new StringBuilder();
                System.out.println("\n* indicates a required field");
                String title = "";
                for (String column : possible_columns) {
                    if (column.equalsIgnoreCase("title")) {
                        System.out.print("What is the title*: ");
                        input = scanner.nextLine();
                        while (input.length() == 0) {
                            System.out.print("Please enter the title*: ");
                            input = scanner.nextLine();
                        }
                        columns.append(column).append(", ");
                        title = input;
                        values.append("'").append(input).append("'").append(", ");
                    } else if (column.equalsIgnoreCase("year")) {
                        System.out.print("What is the year*: ");
                        input = scanner.nextLine();
                        boolean is_year = false;
                        while (input.length() == 0 || !is_year) {
                            try {
                                Integer.parseInt(input);
                                is_year = true;
                            } catch (NumberFormatException e) {
                                System.out.print("Please enter an integer for the year*: ");
                                input = scanner.nextLine();
                            }
                        }
                        columns.append(column).append(", ");
                        values.append(input).append(", ");
                    } else if (column.equalsIgnoreCase("genre")) {
                        String[] genres = {"action", "adventure", "animation", "comedy", "crime & mystery", "documentary", "drama", "fantasy", "historical", "horror", "musical", "romance", "sci-fi", "thriller"};
                        System.out.print("What are the genre(s)? Select 1 or more from the following, separating them each with a comma.\n" + Arrays.toString(genres) + ": ");
                        input = scanner.nextLine();
                        while (!check_genres(input, genres) && input.length() != 0) {
                            System.out.print("Invalid genres were inputted. Please try again: ");
                            input = scanner.nextLine();
                        }
                        if(input.length() != 0) {
                            columns.append(column).append(", ");
                            values.append("ARRAY[");
                            String[] items = input.split(",\\s+");
                            for (String item: items) {
                                values.append("'").append(item).append("',");
                            }
                            values = new StringBuilder(values.substring(0, values.length() - 1));
                            values.append("], ");
                        }
                    } else if (column.equalsIgnoreCase("notable_actors")) {
                        System.out.print("Who are some notable_actors: ");
                        input = scanner.nextLine();
                        if(input.length() != 0) {
                            columns.append(column).append(", ");
                            values.append("ARRAY[");
                            String[] items = input.split(",\\s+");
                            for (String item: items) {
                                values.append("'").append(item).append("',");
                            }
                            values = new StringBuilder(values.substring(0, values.length() - 1));
                            values.append("], ");
                        }
                    } else {
                        System.out.print("What is the " + column + ": ");
                        input = scanner.nextLine();
                        if (input.length() != 0) {
                            columns.append(column).append(", ");
                            values.append("'").append(input).append("'").append(", ");
                        }
                    }
                }
                columns.append("elo");
                values.append(new_elo(jdbc, title, table));
                jdbc.insert(table, columns.toString(), values.toString());
                System.out.print("You have selected " + table + "! Would would you like to do? new/search/match/see_ranks/restart/quit: ");
                input = scanner.nextLine();
            } else if (input.equalsIgnoreCase("search")) {
                System.out.print("Which title would you like to search for? ");
                input = scanner.nextLine();
                boolean found = false;
                int id = 0;
                while(!found) {
                    try {
                        id = jdbc.search(table, input);
                        found = true;
                    } catch (SQLException e) {
                        System.out.print("Sorry, I was not able to find that.\nPlease try again: ");
                        input = scanner.nextLine();
                    }
                }
                System.out.print("Would you like to edit this entry? y/n: ");
                input = scanner.nextLine();
                boolean valid_input = new ArrayList<String>(Arrays.asList("y", "n")).contains(input);
                while (!valid_input) {
                    System.out.print("Sorry, invalid input! Would you like to edit this entry? y/n: ");
                    input = scanner.nextLine();
                    if (new ArrayList<String>(Arrays.asList("y", "n")).contains(input)) {
                        valid_input = true;
                    }
                }
                if (input.equalsIgnoreCase("y")){
                    System.out.print("Choose one from the following to edit: [genre, synopsis, notable_actors]: ");
                    input = scanner.nextLine();
                    if (input.equalsIgnoreCase("genre")) {
                        String[] genres = {"action", "adventure", "animation", "comedy", "crime & mystery", "documentary", "drama", "fantasy", "historical", "horror", "musical", "romance", "sci-fi", "thriller"};
                        System.out.print("What are the genre(s)? Select 1 or more from the following, separating them each with a comma.\n" + Arrays.toString(genres) + ": ");
                        input = scanner.nextLine();
                        while (!check_genres(input, genres) && input.length() != 0) {
                            System.out.print("Invalid genres were inputted. Please try again: ");
                            input = scanner.nextLine();
                        }
                        StringBuilder set = new StringBuilder();
                        if(input.length() != 0) {
                            set.append("ARRAY[");
                            String[] items = input.split(",\\s+");
                            for (String item: items) {
                                set.append("'").append(item).append("',");
                            }
                            set = new StringBuilder(set.substring(0, set.length() - 1));
                            set.append("]");
                        }
                        jdbc.update(table, "genre = " + set, "id = " + id);
                    } else if (input.equalsIgnoreCase("synopsis")) {
                        System.out.print("What is the synopsis: ");
                        input = scanner.nextLine();
                        if (input.length() != 0) {
                            jdbc.update(table, "synopsis = '" + input + "'", " id = " + id);
                        }
                    } else if (input.equalsIgnoreCase("notable_actors")) {
                        System.out.print("Who are some notable actors? ");
                        input = scanner.nextLine();
                        StringBuilder set = new StringBuilder();
                        if(input.length() != 0) {
                            set.append("ARRAY[");
                            String[] items = input.split(",\\s+");
                            for (String item: items) {
                                set.append("'").append(item).append("',");
                            }
                            set = new StringBuilder(set.substring(0, set.length() - 1));
                            set.append("]");
                            System.out.println(set);
                        }
                        jdbc.update(table, "notable_actors = " + set, "id = " + id);
                    } else {
                        System.out.println("Invalid input. Restarting.");
                    }
                }
                System.out.print("You have selected " + table + "! Would would you like to do? new/search/match/see_ranks/restart/quit: ");
                input = scanner.nextLine();
            } else if (input.equalsIgnoreCase("match")) {
                System.out.print("What kind of match would you like to do? ranked/random: ");
                input = scanner.nextLine();
                if (input.equalsIgnoreCase("ranked") || input.equalsIgnoreCase("random")) {
                    String type = input;
                    int matches = 0;
                    System.out.print("How many matches would you like done? ");
                    input = scanner.nextLine();
                    boolean valid_input = false;
                    while (input.length() == 0 || !valid_input) {
                        try {
                            Integer.parseInt(input);
                            matches = Integer.parseInt(input);
                            valid_input = true;
                        } catch (NumberFormatException e) {
                            System.out.print("Please enter an integer: ");
                            input = scanner.nextLine();
                        }
                    }
                    match(jdbc, table, type, matches);
                } else {
                    System.out.println("Invalid input. Restarting.");
                }
                System.out.print("You have selected " + table + "! Would would you like to do? new/search/match/see_ranks/restart/quit: ");
                input = scanner.nextLine();
            } else if (input.equalsIgnoreCase("see_ranks")) {
                System.out.print("What ranks would you like to see? all/top_10/bottom_10: ");
                input = scanner.nextLine();
                if (input.equalsIgnoreCase("all") || input.equalsIgnoreCase("top_10") || input.equalsIgnoreCase("bottom_10")) {
                    jdbc.see_ranks(table, input);
                } else {
                    System.out.println("Invalid input. Restarting.");
                }
                System.out.print("You have selected " + table + "! Would would you like to do? new/search/match/see_ranks/restart/quit: ");
                input = scanner.nextLine();
            } else if (!new ArrayList<String>(Arrays.asList("restart", "quit")).contains(input)) {
                System.out.print("\nInvalid input!\nYou have selected " + table + "! Would would you like to do? new/search/match/see_ranks/restart/quit: ");
                input = scanner.nextLine();
            } else {
                run = false;
            }
        }
        return input;
    }

    private static String see_tables(PostgreSQLJDBC jdbc) throws SQLException {
        ArrayList<String> tables = jdbc.get_tables();
        System.out.print("Please select a table from the following list: " + tables + " or restart/quit: ");
        input = scanner.nextLine();

        boolean run = true;
        boolean table_selected = false;
        while (run) {
            if (tables.contains(input) || table_selected) {
                table_selected = true;
                input = table_selected(jdbc, input);
                if (new ArrayList<String>(Arrays.asList("restart", "quit")).contains(input)) {
                    table_selected = false;
                }
            } else if (!new ArrayList<String>(Arrays.asList("restart", "quit")).contains(input)) {
                System.out.print("\nInvalid input!\nPlease select a table from the following list: " + tables + " or restart/quit: ");
                input = scanner.nextLine();
            } else {
                run = false;
            }
        }
        return input;
    }

    public static void main(String[] args) {
         String db_url = "jdbc:postgresql://localhost:5432/elo";
         String user = "postgres";
         String pass = "Sx@040802";
         PostgreSQLJDBC jdbc = new PostgreSQLJDBC(db_url, user, pass);

         try {
             jdbc.connect();

            System.out.print("What would you like to do? create_table/see_tables/restart/quit: ");
            input = scanner.nextLine();

            boolean run = true;
            boolean seeing_tables = false;
            while (run) {
                if (input.equalsIgnoreCase("quit")) {
                    run = false;
                } else if (input.equalsIgnoreCase("create_table")) {
                    System.out.print("What is the table name: ");
                    input = scanner.nextLine();
                    jdbc.create_table(input);
                    System.out.print("What would you like to do next? create_table/see_tables/restart/quit: ");
                    input = scanner.nextLine();
                } else if (input.equalsIgnoreCase("restart")) {
                    seeing_tables = false;
                    System.out.print("What would you like to do? create_table/see_tables/restart/quits: ");
                    input = scanner.nextLine();
                } else if (input.equalsIgnoreCase("see_tables") || seeing_tables) {
                    seeing_tables = true;
                    input = see_tables(jdbc);
                } else {
                    System.out.print("\nInvalid input!\nWhat would you like to do? create_table/see_tables/restart/quit: ");
                    input = scanner.nextLine();
                }
            }
         } catch (SQLException e) {
             e.printStackTrace();
         } finally {
             try {
                 jdbc.disconnect();
             } catch (SQLException e) {
                 e.printStackTrace();
             }
         }
    }
}