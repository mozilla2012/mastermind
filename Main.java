package com.mastermind;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * This class plays a game of Mastermind with you! It creates a random pattern and you try to crack the code! You
 * make guesses, and it tells you how close you are to being correct! Just run main() and it can explain the rules.
 * This is version 1.0 - it is command line only. I haven't had time to make a GUI yet.
 */
public class Main {

    private static final BufferedReader READER = new BufferedReader(new InputStreamReader(System.in));
    /*
     * Regular Mastermind has 6 colors and only 4 variable spaces.
     * Red, yellow, green, blue, black, white.
     */
    private static final int NUM_COLORS_IN_REGULAR = 6;
    private static final int NUM_SPACES_IN_REGULAR = 4;
    /*
     * Advanced Mastermind has 8 colors and 5 variable spaces.
     * Red, yellow, green, blue, black, white, orange, purple.
     */
    private static final int NUM_COLORS_IN_ADVANCED = 8;
    private static final int NUM_SPACES_IN_ADVANCED = 5;

    /*
     * Values used for the clues displayed. Usually represented by black/red and white pegs.
     */
    private static final int CORRECT_COLOR_CORRECT_LOCATION = 2; // Black pegs
    private static final int CORRECT_COLOR_INCORRECT_LOCATION = 1; // White pegs

    private static final int MAX_GUESSES = 12;
    private static final String RULES_STRING =
            "Welcome to Mastermind!\n" +
            "The basic premise of the game is that you are trying to guess a secret code.\n" +
            "The code is four different colors. There are six colors to choose from, so a examples code could be:\n" +
            "\"Red blue green white\" or \"Yellow red black green\"\n" +
            "Depending on the rules chosen, the code could possibly contain duplicate colors or blanks, making a large number of possibilities!\n\n" +
            "Gameplay is as follows: Guess the code, and your guess will be \"graded\". Your clue will be in the form of colored pegs.\n" +
            "A black peg means that one of your guessed colors is the correct color and in the correct position.\n" +
            "A white peg means that you have guessed a color correctly but it is in the incorrect location.\n\n" +
            "So a clue of four white pegs means you have guessed all of the correct colors, but they are not in the correct order. \n" +
            "One black peg and two white pegs means that one of your colors is in the correct position and\n" +
            "you have two other correct colors in the incorrect position.\n\n" +
            "\"Regular Mastermind\" has a code of 4 colors, with 6 different available colors.\n" +
            "\"Advanced Mastermind\" has a code of 5 colors, with 8 different available colors.\n " +
            "Have fun!\n\n" +
            "NOTE: For now, the colors are represented as integers from 1-8. Blanks are represented by zero.\n" +
            "Your grade will be numbers: Black pegs are represented by \"2\", and white pegs are represented by \"1\"\n\n";

    public static void main(String[] args) {
        // Game setup
        printRules();
        boolean isRegularGame = getIsRegularGame();
        boolean allowBlanks = getAllowBlanks();
        boolean allowDuplicates = getAllowDoubles();
        final ArrayList<Integer> solution = createSolution(isRegularGame, allowBlanks, allowDuplicates);

        // Gameplay
        boolean victory = false;
        int guessCount = 0;
        System.out.println("Enter your guess!");
        while(!victory && guessCount++ < MAX_GUESSES) {
            ArrayList<Integer> guess = getValidGuessFromConsole(solution, allowBlanks, isRegularGame);
            victory = evaluateGuess(guess, solution);
        }
        System.out.println("\n\n");

        // Endgame
        if(victory) {
            System.out.println("You win! Score: " + (guessCount - 1));
        } else {
            System.out.println("Too many guesses... Here is the solution:" + solution);
        }
    }

    /**
     * Reads a single line of input from the console.
     * @return Optional.of(input string) or Optional.empty() if there are issues.
     */
    private static Optional<String> readLine() {
        String input;
        try {
            input = READER.readLine();
            return Optional.of(input);
        } catch (IOException e) {
            System.out.println("ERROR: There was an error parsing the input.");
            e.printStackTrace();
        }
        return Optional.empty();
    }

    /**
     * Get a line from the user and retry until there is a valid String.
     * @return a trim()ed String of input from the console.
     */
    private static String getConsoleInput() {
        Optional<String> guessString = readLine();
        while(!guessString.isPresent()) {
            guessString = readLine();
        }
        return guessString.get().trim();
    }

    /**
     * Gets an integer option from the console. Retries until the input is an integer.
     * @return an integer from the user.
     */
    private static int getOptionFromConsole() {
        int input = -1;
        boolean validInput = false;
        String stringInput;
        while(!validInput) {
            stringInput = getConsoleInput();
            try {
                input = Integer.valueOf(stringInput);
                validInput = true;
            } catch (NumberFormatException e) {
                System.out.println("Please input one of the given options.");
            }
        }
        return input;
    }

    private static void printRules() {
        int input = -1;
        while (input != 1 && input != 0) {
            System.out.println("Print the rules (1) or not (0)?");
            input = getOptionFromConsole();
        }
        if (input == 1) {
            System.out.println(RULES_STRING);
        }
    }

    /**
     * Asks the user if the game is Regular or Advanced.
     * @return true if Regular, false if Advanced.
     */
    private static boolean getIsRegularGame() {
        int input = -1;
        while (input != 1 && input != 2) {
            System.out.println("Regular Mastermind (1) or Advanced Mastermind (2)?");
            input = getOptionFromConsole();
        }
        return (input == 1);
    }

    /**
     * Asks the user if the game should allow blanks.
     * @return true if yes, false if no.
     */
    private static boolean getAllowBlanks() {
        int input = -1;
        while (input != 1 && input != 0) {
            System.out.println("Allow blanks (1) or no (0)?");
            input = getOptionFromConsole();
        }
        return (input == 1);
    }

    /**
     * Asks the user if the game should allow doubles.
     * @return true if yes, false if no.
     */
    private static boolean getAllowDoubles() {
        int input = -1;
        while (input != 1 && input != 0) {
            System.out.println("Allow doubles (1) or no (0)?");
            input = getOptionFromConsole();
        }
        return (input == 1);
    }

    /**
     * Creates a solution for the player to guess.
     * @param numSpaces The number of elements the solution should contain. (4 for Regular, 5 for Advanced Mastermind)
     * @param allowBlanks Are blank spaces allowed in the solution? Represented by zeros.
     * @param allowDuplicates Are duplicates allowed in the solution?
     * @param numColors How many colors could the solution contain? Each different color is represented by a positive
     * @return an ArrayList of Integers containing a solution based on the desired inputs.
     */
    private static ArrayList<Integer> createSolution(final int numSpaces, final boolean allowBlanks,
                                                     final boolean allowDuplicates, final int numColors) {
        if(!allowBlanks && numColors < numSpaces) {
            System.out.println("You can't make a solution with fewer colors than spaces.");
            return null;
        }
        ArrayList<Integer> solution;
        solution = new ArrayList<>();
        Random rand = new Random();
        int randOffset = (allowBlanks ? 0 : 1); // If we allow allowBlanks, we need to allow zero as a random number.
        int randHigh = numColors + (allowBlanks ? 1 : 0); // If we allow allowBlanks, the max value needs increased by one.

        int colorToAdd;
        for(int i = 0; i < numSpaces; i++) {
            colorToAdd = rand.nextInt(randHigh) + randOffset;
            if(allowDuplicates || !solution.contains(colorToAdd)) {
                solution.add(colorToAdd);
            } else {
                i--;
            }
        }
        return solution;
    }

    /**
     * Based on the users inputs, creates a solution for the desired game type.
     * @param isRegularGame 4 variables with 6 colors or 5 variables with 8 colors.
     * @param allowBlanks Are blank spaces allowed in the solution? Represented by zeros.
     * @param allowDuplicates Are duplicates allowed in the solution?
     * @return an ArrayList of Integers containing a solution based on the desired inputs.
     */
    private static ArrayList<Integer> createSolution(final boolean isRegularGame, final boolean allowBlanks,
                                                     final boolean allowDuplicates) {
        if(isRegularGame) {
            return createSolution(NUM_SPACES_IN_REGULAR, allowBlanks, allowDuplicates, NUM_COLORS_IN_REGULAR);
        } else { // if Advanced:
            return createSolution(NUM_SPACES_IN_ADVANCED, allowBlanks, allowDuplicates, NUM_COLORS_IN_ADVANCED);
        }
    }

    /**
     * Given the guess, generate the clue for the user.
     * A value of 1 represents "Correct color, incorrect location."
     * A value of 2 represents "Correct color, correct location."
     * @param guess from the user
     * @param solution the solution
     */
    private static ArrayList<Integer> generateClue(ArrayList<Integer> guess, ArrayList<Integer> solution) {
        ArrayList<Integer> tempGuess = new ArrayList<>(guess);
        ArrayList<Integer> tempSolution = new ArrayList<>(solution);
        ArrayList<Integer> clue = new ArrayList<>();

        // First, add "Correct color, correct location" hints.
        for (int i = 0; i < tempSolution.size(); i++){
            if(tempSolution.get(i).equals(tempGuess.get(i))) {
                clue.add(CORRECT_COLOR_CORRECT_LOCATION);
                tempSolution.remove(i);
                tempGuess.remove(i);
                i--;
            }
        }
        // Then, add "Correct color, incorrect location" hints.
        for (int i = 0; i < tempSolution.size(); i++){
            int indexOf = tempGuess.indexOf(tempSolution.get(i));
            if(indexOf != -1) {
                clue.add(CORRECT_COLOR_INCORRECT_LOCATION);
                tempSolution.remove(i);
                tempGuess.remove(indexOf);
                i--;
            }
        }
        return clue;
    }

    /**
     * Given the guess, display the clue for the user.
     * @param guess from the user
     * @param solution the solution
     */
    private static void displayClue(final ArrayList<Integer> guess, final ArrayList<Integer> solution) {
        ArrayList<Integer> clue = generateClue(guess, solution);
        System.out.println(clue);
    }

    /**
     * Returns true if the guess matches the solution.
     * @param guess from the user
     * @param solution the solution
     * @return true if the user's guess matches the order and values of the solution
     */
    private static boolean guessIsCorrect(final ArrayList<Integer> guess, final ArrayList<Integer> solution) {
        return Arrays.equals(guess.toArray(), solution.toArray());
    }

    /**
     * Determines if the given guess is correct or not. If yes, returns true. If no, it prints the clue.
     * @param guess from the user
     * @param solution the solution
     * @return true if the guess is correct, false if the guess is not correct.
     */
    private static boolean evaluateGuess(final ArrayList<Integer> guess, final ArrayList<Integer> solution) {
        System.out.print(guess);

        if(guessIsCorrect(guess, solution)) {
            return true;
        }
        displayClue(guess, solution);
        return false;
    }

    /**
     * Will get input from the console, replace all commas with spaces, and split into an array based on whitespace.
     * It's assumed this array contains ints, but it is not validated.
     * @return an array containing the "words" (ideally ints) from the console input.
     */
    private static String[] getGuessFromConsole() {
        String guessString = getConsoleInput();
        guessString = guessString.replaceAll(",", " ");
        return guessString.split("\\s+");
    }

    /**
     * Get a guess from the console and validate it. This function will repeatedly ask for input until the guess has
     * the correct number of variables within the proper range.
     * @param solution takes in the solution to know expected size.
     * @param allowBlanks are blanks allowed in the solution?
     * @param isRegularGame is this Regular Mastermind or Advanced Mastermind?
     * @return a valid guess from the console.
     */
    private static ArrayList<Integer> getValidGuessFromConsole(final ArrayList<Integer> solution,
                                                               final boolean allowBlanks,
                                                               final boolean isRegularGame) {
        ArrayList<Integer> guess;
        int lowestAllowableValue = allowBlanks ? 0 : 1;
        int highestAllowableValue = isRegularGame ? NUM_COLORS_IN_REGULAR : NUM_COLORS_IN_ADVANCED;
        while(true) {
            try {
                guess = Arrays.stream(getGuessFromConsole())
                        .map(Integer::valueOf)
                        .collect(Collectors.toCollection(ArrayList::new));
            } catch (NumberFormatException e) {
                System.out.printf("Please enter only integers from %d to %d separated by spaces. ",
                        lowestAllowableValue, highestAllowableValue);
                continue;
            }
            // Too many, too few, or out of range guesses?
            boolean incorrectNumberOfGuesses = guess.size() != solution.size();
            boolean outOfRangeGuesses = guess.stream()
                    .anyMatch(s -> s < lowestAllowableValue
                            || s > highestAllowableValue);
            if (outOfRangeGuesses || incorrectNumberOfGuesses) {
                System.out.printf("Please enter %d integers between %d and %d. ", solution.size(), lowestAllowableValue,
                        highestAllowableValue);
            } else {
                // Input is valid!
                return guess;
            }
        }
    }
}
