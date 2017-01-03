package twitter;

import twitter4j.*; //set the classpath to lib\twitter4j-core-4.0.4.jar
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.Date;
import java.util.HashMap;

public class TwitterWordCount {
	private static PrintStream consolePrint;

	public static void main(String[] args) throws TwitterException, IOException {
		consolePrint = System.out; // this preserves the standard output so we
									// can get to it later

		// PART 1
		// set up classpath and properties file

		TJTwitter bigBird = new TJTwitter(consolePrint);

		// Create and set a String called message here

		String message = "Hello World";
		// bigBird.tweetOut(message);

		// PART 2
		// Choose a public Twitter user's handle
		// String test1="hello";
		// String test2="w,o?rld.";
		// System.out.println(bigBird.removePunctuation(test1));
		// System.out.println(bigBird.removePunctuation(test2));
		Scanner scan = new Scanner(System.in);
		consolePrint.print("Please enter a Twitter handle, do not include the @symbol --> ");
		String twitter_handle = scan.next();
	//	bigBird.fetchTweets(twitter_handle);
	//	bigBird.splitIntoWords();
		bigBird.queryHandle(twitter_handle);
		System.out.println("Total words used: " + bigBird.getNumWords());
		//bigBird.findUniques();
		System.out.println(twitter_handle + " has used " + bigBird.getNumUniqueWords()+ " unique words");
		System.out.println(twitter_handle +"'s most commonly used word is: " +  bigBird.mostPopularWord() + " used " + bigBird.getFrequencyMax() + " times");
		
		// Find and print the most popular word they tweet while
		// while (!twitter_handle.equals("done")) {
		// bigBird.queryHandle(twitter_handle);
		// consolePrint.println(
		// "The most common word from @" + twitter_handle + " is: " +
		// bigBird.mostPopularWord() + ".");
		// consolePrint.println("The word appears " + bigBird.getFrequencyMax()
		// + " times.");
		//
		// consolePrint.println();
		// consolePrint.print("Please enter a Twitter handle, do not include the
		// @ symbol --> ");
		// twitter_handle = scan.next();
		// }
		//
		scan.close();

		// PART 3
		// bigBird.investigate();

	}// end main

}// end driver

class TJTwitter {
	private Twitter twitter;
	private PrintStream consolePrint;
	private List<Status> statuses;
	private List<String> terms;
	private Map<String, Integer> uniqueWords;
	private List<String> trivial;
	private String popularWord;
	private int frequencyMax;

	public TJTwitter(PrintStream console) {
		// Makes an instance of Twitter - this is re-useable and thread safe.
		// Connects to Twitter and performs authorizations.
		twitter = TwitterFactory.getSingleton();
		consolePrint = console;
		statuses = new ArrayList<Status>();
		terms = new ArrayList<String>();
	}

	/*******************
	 * Part 1 ****************** This method tweets a given message.
	 * 
	 * @param String
	 *            a message you wish to Tweet out
	 */
	public void tweetOut(String message) throws TwitterException, IOException {

		twitter.updateStatus(message);
	}

	/****************** Part 2 *******************/
	/**
	 * This method queries the tweets of a particular user's handle.
	 * 
	 * @param String
	 *            the Twitter handle (username) without the @sign
	 */
	@SuppressWarnings("unchecked")
	public void queryHandle(String handle) throws TwitterException, IOException {
		statuses.clear();
		terms.clear();
		fetchTweets(handle);
		splitIntoWords();
		findUniques();
		removeCommonEnglishWords();
		wordCount();
	}
	
	public int getNumWords() {
		return terms.size();
	}
	
	public int getNumUniqueWords() {
		return uniqueWords.size();
	}
	/**
	 * This method fetches the most recent 2,000 tweets of a particular user's
	 * handle and * stores them in an arrayList of Status objects. Populates
	 * statuses. * @param String the Twitter handle (username) without the @sign
	 */
	public void fetchTweets(String handle) throws TwitterException, IOException {
		// Creates file for debugging purposes
		PrintStream fileout = new PrintStream(new FileOutputStream("tweets.txt"));
		Paging page = new Paging(1, 200);
		int p = 1;
		while (p <= 10) {
			page.setPage(p);
			statuses.addAll(twitter.getUserTimeline(handle, page));
			p++;
		}
		int numberTweets = statuses.size();
		fileout.println("Number of tweets = " + numberTweets);
		int count = 1;
		for (Status j : statuses) {
			fileout.println(count + ".  " + j.getText());
			count++;
		}
	}

	/**
	 * This method takes each status and splits them into individual words. 134
	 * * Remove punctuation by calling removePunctuation, then store the word in
	 * terms.
	 */
	public void splitIntoWords() {
		for (Status j : statuses) {
			if (!j.isRetweet()) {
				String message = removePunctuation(j.getText());
				String tweet[] = message.split(" ");
			//	System.out.println(j.getText());
			//	System.out.println("Words in tweet: " + tweet.length);
				for (int i = 0; i < tweet.length; i++) {
					terms.add(tweet[i]);
				}
			}
		}
	}

	public void findUniques() {
		Map<String, Integer> wordCount = new TreeMap<String, Integer>();
		for (int i = 0; i < terms.size(); i++) {
			String word = terms.get(i);
			word=word.toLowerCase();
			if (wordCount.containsKey(word)) {
				int count = wordCount.get(word);
				wordCount.remove(word);
				wordCount.put(word, count + 1);
			} else {
				wordCount.put(word, 1);
			}
		}
		if (wordCount.containsKey(" ")) {
			wordCount.remove(" ");
		}
		if (wordCount.containsKey("")) {
			wordCount.remove("");
		}
		uniqueWords = wordCount;
	}

	private void commonWords() throws FileNotFoundException {
		ArrayList<String> list = new ArrayList<String>();
		try {
			File commons = new File("commonWords.txt");
			Scanner scan = new Scanner(commons);
			while (scan.hasNextLine()) {
				list.add(scan.nextLine());
			}
	
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		trivial = list;
	}


	private String removePunctuation(String s) {
		String result = "";
		for (int i = 0; i < s.length(); i++) {
			if (!isPunc(s.charAt(i))) {
				result += s.charAt(i);
			}
		}
		return result;
	}

	private boolean isPunc(char c) {
		String punct = ".,:;?!&*";
		for (int i = 0; i < punct.length(); i++) {
			if (punct.charAt(i) == c) {
				return true;
			}
		}
		return false;
	}

	/**
	 * This method removes common English words from the list of terms. Remove
	 * all words found in commonWords.txt from the argument list. The count will
	 * not be given in commonWords.txt. You must count the number of words in
	 * this method. This method should NOT throw an exception. Use
	 * try/catch. 
	 */
	@SuppressWarnings("unchecked")
	private void removeCommonEnglishWords() throws FileNotFoundException {
		try {
			commonWords();
			System.out.println("We have " + trivial.size() + " common words.");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		for (int i=0; i<trivial.size(); i++) {
			if (uniqueWords.containsKey(trivial.get(i))) {
				uniqueWords.remove(trivial.get(i));
			}
		}
		if (uniqueWords.containsKey(" ")) {
			uniqueWords.remove(" ");
		}
	}
	private void wordCount() {
		int max = 0;
		String maxWord = "";
		for (Entry<String, Integer> s : uniqueWords.entrySet()) {
			if (s.getValue()>max) {
				max = s.getValue();
				maxWord=s.getKey();
			}
		}
		popularWord = maxWord;
		frequencyMax = max;
	}
	/**
	 * This method returns the most common word from terms. Consider
	 * case - should it be case sensitive? The choice is yours. @return
	 * String the word that appears the most times 183 * @post will popopulate
	 * the frequencyMax variable with the frequency of the most common word 184
	 */
	@SuppressWarnings("unchecked")
	public String mostPopularWord() {
		return popularWord;
	}

	/**
	 * 192 * This method returns the number of times the most common word 
	 * appears. 193 * Note: variable is populated in mostPopularWord() 194
	 * * @return int frequency of most common word
	 */
	public int getFrequencyMax() {
		return frequencyMax;
	}

	/****************** Part 3 *******************/
	public void investigate() {
		// Enter your code here
	}

	/**
	 * 209 * This method determines how many people in Arlington, VA 210 * tweet
	 * about the Miami Dolphins. Hint: not many. :( 211
	 */
	public void sampleInvestigate() {

		Query query = new Query("Miami Dolphins");
		query.setCount(100);
		query.setGeoCode(new GeoLocation(38.8372839, -77.1082443), 5, Query.MILES);
		query.setSince("2015-12-1");
		try {
			QueryResult result = twitter.search(query);
			System.out.println("Count : " + result.getTweets().size());
			for (Status tweet : result.getTweets()) {
				System.out.println("@" + tweet.getUser().getName() + ": " + tweet.getText());
			}
		} catch (TwitterException e) {
			e.printStackTrace();
		}
		System.out.println();
	}

}
