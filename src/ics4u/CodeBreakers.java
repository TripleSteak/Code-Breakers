package ics4u;

import java.awt.Color;

/*
 * 	File:			CodeBreakers.java
 * 	Date Created:	03/08/2019
 * 	Last Modified:	03/24/2019
 * 	Authors:		Derrick Cui, Simon Ou, Daniel Qu
 * 
 * 	Class:			ICS4U1-03
 * 	Teacher:		Mr. Anandarajan
 * 
 * 	Description:	A simple GUI-based program that simulates the popular board game "Code
 * 					Breakers." The computer generates a random sequence of four colours (with
 * 					repeats). The user is then allowed to guess up to ten times, with clues on
 * 					the number of correct colours/positions each time (black peg means correct colour in correct position, white peg means correct colour in wrong position). The player wins if they can
 * 					successfully crack the code within ten tries. Enjoy!!!
 */

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class CodeBreakers extends JFrame {
	private static final long serialVersionUID = 5645320735434675451L; // serialization code (here to remove warning)

	private static CodeBreakers instance; // instance of JFrame

	public static int DISPLAY_WIDTH = 800; // JFrame display width
	public static int DISPLAY_HEIGHT = 600; // JFrame display height

	public static final int TOTAL_COLOURS = 6; // number of colours

	public static final int SIZE = 4; // length of code
	public static final int TRIES = 10; // maximum number of tries for user

	public final String VALID_CHARS = "GRBYOP"; // all possible code colours

	public final char[][] GUESS_ARRAY = new char[TRIES][SIZE]; // all previous user guesses
	public final char[][] CLUES_ARRAY = new char[TRIES][SIZE]; // respective clues for the guesses

	public final JButton[] COLOUR_BUTTONS = new JButton[TOTAL_COLOURS]; // array to store six colour buttons

	public final JLabel[][] GUESS_BOXES = new JLabel[TRIES][SIZE]; // display list of user's previous guesses
	public final JLabel[][] CLUES_BOXES = new JLabel[TRIES][SIZE]; // display list of clues to previous guesses
	public final JLabel[] ANSWER_BOXES = new JLabel[SIZE]; // displays the answers at the end of the game

	public JButton clearButton; // clears the last colour entered
	public JButton checkButton; // checks user's guess for correctness
	public JButton resetButton; // used to restart at the end of the game

	public JLabel guessesLabel; // on-screen label for previous guesses
	public JLabel cluesLabel; // on-screen label for previous clues
	public JLabel resultLabel; // on-screen label for the result of the game

	public JLabel background; // background image

	public static int curTries = 0; // number of tries expended
	public static char[] curCode = new char[4]; // current four-colour code
	public static String curGuess = ""; // user's current guess

	public boolean isRunning = false; // if the game isn't paused

	/*
	 * Declaration of assets (image icons)
	 */
	public static ImageIcon[] GUESS_ICONS = new ImageIcon[TOTAL_COLOURS]; // guess colour icons
	public static ImageIcon[] CLUES_ICONS = new ImageIcon[2]; // clues images (black, white)

	/**
	 * Creates a new instance of CodeBreakers, a subclass of JFrame with all
	 * non-static initializations
	 */
	public CodeBreakers() {
		/*
		 * Sets the background image of the JFrame
		 */
		try {
			setContentPane(new JComponent() {
				private static final long serialVersionUID = 1L; // serialization code (here to remove warning)

				BufferedImage background = ImageIO.read(CodeBreakers.class.getResource("/background.png")); // load
																											// image
																											// from file

				@Override
				protected void paintComponent(Graphics g) {
					super.paintComponent(g);
					g.drawImage(background, 0, 0, this); // draw background image to screen
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
		}

		Insets insets = getContentPane().getInsets(); // get insets, used for formatting
		getContentPane().setLayout(null); // remove built-in layout manager, use absolute positioning

		/*
		 * loop through all colour buttons, initialize image icons
		 */
		for (int i = 0; i < TOTAL_COLOURS; i++) {
			/*
			 * Initializes button, loads image icon from file
			 */
			COLOUR_BUTTONS[i] = new JButton(loadImage("large_button_" + i));
			removeButtonFeatures(COLOUR_BUTTONS[i]);

			COLOUR_BUTTONS[i].setPreferredSize(new Dimension(70, 70)); // set button size to 70x70, same as texture

			getContentPane().add(COLOUR_BUTTONS[i]); // adds button to JFrame

			/*
			 * Sets button's location on screen
			 */
			Dimension dimension = COLOUR_BUTTONS[i].getPreferredSize();
			COLOUR_BUTTONS[i].setBounds(60 + insets.left + (i % 2 == 0 ? 0 : 70), 35 + insets.top + 70 * (i / 2),
					dimension.width, dimension.height);
		}

		/*
		 * Add check, clear, and restart buttons
		 */
		checkButton = new JButton(loadImage("check_button"));
		clearButton = new JButton(loadImage("clear_button"));
		resetButton = new JButton(loadImage("reset_button"));

		getContentPane().add(checkButton);
		getContentPane().add(clearButton);
		getContentPane().add(resetButton);

		checkButton.setBounds(12 + insets.left, 280 + insets.top, checkButton.getPreferredSize().width,
				checkButton.getPreferredSize().height);
		clearButton.setBounds(12 + insets.left, 360 + insets.top, clearButton.getPreferredSize().width,
				clearButton.getPreferredSize().height);
		resetButton.setBounds(12 + insets.left, 440 + insets.top, resetButton.getPreferredSize().width,
				resetButton.getPreferredSize().height);

		removeButtonFeatures(checkButton);
		removeButtonFeatures(clearButton);
		removeButtonFeatures(resetButton);

		/*
		 * Add action listeners to the colour buttons
		 */
		for (int i = 0; i < TOTAL_COLOURS; i++) {
			final int buttonNum = i; // determines which colour is added by which button

			COLOUR_BUTTONS[i].addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					if (curGuess.length() < SIZE && isRunning) { // guess isn't of adequate length yet
						curGuess += VALID_CHARS.charAt(buttonNum);
						CodeBreakers.displayGame(GUESS_ARRAY, CLUES_ARRAY); // refresh screen
					}
				}
			});
		}

		/*
		 * Add action listener to check button
		 */
		checkButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (curGuess.length() == SIZE && isRunning) { // user's guess is of correct length
					ArrayList<Character> fullyCorrectList = findFullyCorrect(curCode, curGuess.toCharArray()); // find
																												// black
																												// clues
					ArrayList<Character> remainingCodeList = removeFullyCorrect(curCode, curGuess.toCharArray());
					ArrayList<Character> remainingGuessList = removeFullyCorrect(curGuess.toCharArray(), curCode);

					/*
					 * Convert remaining list to char array for the findColourCorrect() method
					 */
					char[] remainingCodeArray = new char[remainingCodeList.size()];
					char[] remainingGuessArray = new char[remainingGuessList.size()];
					for (int i = 0; i < remainingCodeList.size(); i++) {
						remainingCodeArray[i] = remainingCodeList.get(i);
						remainingGuessArray[i] = remainingGuessList.get(i);
					}

					ArrayList<Character> colourCorrectList = findColourCorrect(remainingCodeArray, remainingGuessArray); // find
																															// white
					// clues

					int index = 0; // counter for initializing clues for this round
					for (Character c : fullyCorrectList) { // add black clues
						CLUES_ARRAY[curTries][index] = c.charValue();
						index++;
					}
					for (Character c : colourCorrectList) { // add white clues
						CLUES_ARRAY[curTries][index] = c.charValue();
						index++;
					}

					for (int i = 0; i < SIZE; i++) // copy current guess to second last guess
						GUESS_ARRAY[curTries][i] = curGuess.charAt(i);

					curTries++;

					if (valid(curCode, curGuess, SIZE)) // if the code matches
						endGame(true);
					else if (curTries == 10) // code doesn't match, but ten turns have elapsed
						endGame(false);

					curGuess = "";

					displayGame(GUESS_ARRAY, CLUES_ARRAY);
				}
			}
		});

		/*
		 * Add action listener to clear button
		 */
		clearButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (isRunning) {
					curGuess = "";
					displayGame(GUESS_ARRAY, CLUES_ARRAY);
				}
			}
		});

		/*
		 * Add action listener to reset button
		 */
		resetButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				resetGame();
			}
		});

		/*
		 * Initialize guess and clues icons
		 */
		for (int i = 0; i < TOTAL_COLOURS; i++)
			GUESS_ICONS[i] = loadImage("guess_" + i);
		for (int i = 0; i < 2; i++)
			CLUES_ICONS[i] = loadImage("clue_" + i);

		this.setTitle("Code Breakers");
		this.setSize(DISPLAY_WIDTH, DISPLAY_HEIGHT);
		this.setResizable(false);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setVisible(true);
	}

	/**
	 * Attempts to load an image icon
	 * 
	 * @param fileName
	 *            name of the file (excluding extension)
	 * @return the ImageIcon instance
	 */
	public ImageIcon loadImage(String fileName) {
		return new ImageIcon(this.getClass().getResource("/" + fileName + ".png"));
	}

	/**
	 * Removes all standard JButton visual characteristics, such as border, etc.
	 * 
	 * @param button
	 *            button to modify
	 */
	public void removeButtonFeatures(JButton button) {
		button.setBorderPainted(false);
		button.setContentAreaFilled(false);
		button.setFocusPainted(false);
		button.setOpaque(false);
	}

	/**
	 * Resets the state of the game, removes guesses, clues, and re-initializes a
	 * new code for the user to play again
	 */
	public void resetGame() {
		getContentPane().remove(resetButton); // remove reset button, will be brought back at the end of the round

		curTries = 0; // reset game stats

		for (int i = 0; i < TRIES; i++) { // clears all stored data from round
			for (int j = 0; j < SIZE; j++) {
				GUESS_ARRAY[i][j] = '\u0000';
				CLUES_ARRAY[i][j] = '\u0000';

				// remove all screen guesses and clues
				if (GUESS_BOXES[i][j] != null)
					getContentPane().remove(GUESS_BOXES[i][j]);
				if (CLUES_BOXES[i][j] != null)
					getContentPane().remove(CLUES_BOXES[i][j]);

				GUESS_BOXES[i][j] = null;
				CLUES_BOXES[i][j] = null;
			}
		}

		/*
		 * Remove the solution at the bottom of the screen
		 */
		for (int i = 0; i < SIZE; i++) {
			if (ANSWER_BOXES[i] != null)
				getContentPane().remove(ANSWER_BOXES[i]);
			ANSWER_BOXES[i] = null;
		}
		
		/*
		 * Remove result label
		 */
		if(resultLabel != null) getContentPane().remove(resultLabel);
		resultLabel = null;

		curCode = createCode(VALID_CHARS, curCode.length); // generate new code

		isRunning = true;
		displayGame(GUESS_ARRAY, CLUES_ARRAY);
	}

	/**
	 * Ends the current round of the game, will reveal reset button for user to
	 * choose to play again
	 * 
	 * @param success
	 *            whether the player was successful in beating the game
	 */
	public void endGame(boolean success) {
		Insets insets = getContentPane().getInsets(); // insets, used for formatting

		isRunning = false;

		for (int i = 0; i < SIZE; i++) {
			ANSWER_BOXES[i] = new JLabel(GUESS_ICONS[VALID_CHARS.indexOf(curCode[i])]);
			instance.getContentPane().add(ANSWER_BOXES[i]);

			ANSWER_BOXES[i].setBounds(320 + insets.left + i * 60, 520 + insets.top,
					ANSWER_BOXES[i].getPreferredSize().width, ANSWER_BOXES[i].getPreferredSize().height);
		}

		if (success) {
			resultLabel = new JLabel("You won!");
		} else {
			resultLabel = new JLabel("You lost!");
		}
		
		resultLabel.setForeground(Color.BLACK);
		resultLabel.setFont(new Font("Times New Roman", Font.BOLD, 28));
		getContentPane().add(resultLabel);

		resultLabel.setBounds(580 + insets.left, 527 + insets.top, resultLabel.getPreferredSize().width,
				resultLabel.getPreferredSize().height);

		/*
		 * Reveal the reset button
		 */
		getContentPane().add(resetButton);
		resetButton.setBounds(12 + insets.left, 440 + insets.top, resetButton.getPreferredSize().width,
				resetButton.getPreferredSize().height);
	}

	/**
	 * Generates a new random sequence of indistinct colours
	 * 
	 * @param colours
	 *            a String representation of all possible colours, each indicated by
	 *            a character
	 * @param length
	 *            the length of the new code
	 * @return a character array consisting of the random code, with character
	 *         representations
	 */
	public static char[] createCode(String colours, int length) {
		char[] code = new char[length];
		for (int i = 0; i < length; i++) {
			code[i] = colours.charAt((int) (Math.random() * colours.length())); // sets given position to random colour
		}

		return code;
	}

	/**
	 * Determines if the code matches the correct code
	 * 
	 * @param code
	 *            four character code generated by computer
	 * @param order
	 *            user's guess
	 * @param length
	 *            length of the code
	 * @return
	 */
	public static boolean valid(char[] code, String order, int length) {
		boolean validated;
		String stringCode = "";

		for (int i = 0; i < length; i++) {
			stringCode += code[i];
		}
		if (stringCode.equalsIgnoreCase(order)) {
			validated = true;
		} else {
			validated = false;
		}
		return validated;
	}

	/**
	 * Using the guess and code colours (String arrays), compare them to find
	 * colours in the same position AND colour. For each of these cases, add a 'b'
	 * to the fullyCorrect arraylist.
	 * 
	 * @param code
	 *            -> the code the user is guessing
	 * @param guess
	 *            -> the guessses the user makes
	 * @return fullyCorrect -> the arraylist with b's to indicate correct colour and
	 *         position
	 */
	public static ArrayList<Character> findFullyCorrect(char[] code, char[] guess) {
		ArrayList<Character> fullyCorrect = new ArrayList<Character>(); // saves how many times the colour is in the
																		// same position and colour
		for (int i = 0; i < code.length; i++) { // loop through code
			if (code[i] == guess[i])
				fullyCorrect.add('b'); // adds a 'b' if the code and guess are equal
		}
		return fullyCorrect; // return the number of colours that are the same colour and position
	}

	/**
	 * Returns an ArrayList of all characters that don't have an identical
	 * counterpart in the correct code (that is to say, wrong guess)
	 * 
	 * @param code
	 *            the code generated by the computer
	 * @param guesses
	 *            the guesses by the user
	 * @return an array list containing the incorrect guesses
	 */
	public static ArrayList<Character> removeFullyCorrect(char[] code, char[] guesses) {
		ArrayList<Character> removeFullyCorrect = new ArrayList<Character>();
		for (int i = 0; i < code.length; i++) {
			if (guesses[i] != code[i]) {
				removeFullyCorrect.add(guesses[i]);
			}
		}
		return removeFullyCorrect;
	}

	/**
	 * Using the guess and code colours (String arrays), compare them to find
	 * colours that are the same and NOT in the same position. For each of these
	 * cases, ad a 'w' to the colourCorrect arraylist.
	 * 
	 * @param code
	 *            -> the code the user is guessing
	 * @param guess
	 *            -> the guesses the user makes
	 * @return colourCorrect -> the arrayList with char w's to indicate correct
	 *         colour but not position
	 */
	public static ArrayList<Character> findColourCorrect(char[] code, char[] guess) {
		ArrayList<Character> colourCorrect = new ArrayList<Character>(); // saves number of colours that are the same,
																			// but not same position
		boolean[][] checked = new boolean[2][code.length]; // array to keep track of which values have been checkeed
		for (int i = 0; i < code.length; i++) { // compares each code index to every guess index
			for (int j = 0; j < guess.length; j++) {
				if (i != j && code[i] == guess[j] && !checked[0][i] && !checked[1][j]) { // if the colours are the same
																							// but not in the same
																							// position,
					colourCorrect.add('w'); // add a 'w'
					checked[0][i] = true;
					checked[1][j] = true;
				}
			}
		}
		return colourCorrect; // returns the number of colours that are the same colour but not position
	}

	/**
	 * Updates the JFrame to display the correct guesses and clues
	 * 
	 * @param guessArray
	 *            array containing guess data
	 * @param clueArray
	 *            array containing clue data
	 */
	public static void displayGame(char[][] guessArray, char[][] clueArray) {
		Insets insets = instance.getContentPane().getInsets(); // insets, used for formatting

		/*
		 * Clears all on-screen guess/clue boxes and reinitializes
		 */
		for (int i = 0; i < TRIES; i++) {
			for (int j = 0; j < SIZE; j++) {
				/*
				 * Remove existing boxes from screen
				 */
				if (instance.GUESS_BOXES[i][j] != null)
					instance.getContentPane().remove(instance.GUESS_BOXES[i][j]);
				if (instance.CLUES_BOXES[i][j] != null)
					instance.getContentPane().remove(instance.CLUES_BOXES[i][j]);

				if (guessArray[i][j] != '\u0000') { // guess exists in this spot
					instance.GUESS_BOXES[i][j] = new JLabel(
							GUESS_ICONS[instance.VALID_CHARS.indexOf(guessArray[i][j])]);
					instance.getContentPane().add(instance.GUESS_BOXES[i][j]);

					instance.GUESS_BOXES[i][j].setBounds(320 + insets.left + j * 60, 10 + insets.top + i * 50,
							instance.GUESS_BOXES[i][j].getPreferredSize().width,
							instance.GUESS_BOXES[i][j].getPreferredSize().height);
				}

				if (clueArray[i][j] != '\u0000') { // clue exists in this spot
					int clueIndex = clueArray[i][j] == 'b' ? 0 : 1; // used to determine index of black/white for
																	// clues
					instance.CLUES_BOXES[i][j] = new JLabel(CLUES_ICONS[clueIndex]);
					instance.getContentPane().add(instance.CLUES_BOXES[i][j]);

					instance.CLUES_BOXES[i][j].setBounds(556 + insets.left + j * 40, 10 + insets.top + i * 50,
							instance.CLUES_BOXES[i][j].getPreferredSize().width,
							instance.CLUES_BOXES[i][j].getPreferredSize().height);
				}

			}
		}

		if (curTries < 10) { // draw current guess to screen
			for (int i = 0; i < curGuess.length(); i++) {
				instance.GUESS_BOXES[curTries][i] = new JLabel(
						GUESS_ICONS[instance.VALID_CHARS.indexOf(curGuess.charAt(i))]);
				instance.getContentPane().add(instance.GUESS_BOXES[curTries][i]);

				instance.GUESS_BOXES[curTries][i].setBounds(320 + insets.left + i * 60, 10 + insets.top + curTries * 50,
						instance.GUESS_BOXES[curTries][i].getPreferredSize().width,
						instance.GUESS_BOXES[curTries][i].getPreferredSize().height);
			}
		}

		instance.repaint(); // updates the screen
	}

	public static void main(String[] args) {
		instance = new CodeBreakers(); // initialize new JFrame object for Code Breakers

		instance.resetGame(); // first-time reset to begin game
	}
}
