package com.company;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.*;

public class WebCrawlerMain {

    //Stores all the links that haven't been visited. Used LinkedList so that can use Queue Data Structure (FIFO)
    private static Queue<String> linksToVisit = new LinkedList<>();

    //Stores all the links that have already been visited.
    private static ArrayList<String> marked = new ArrayList<>();

    //Stores all dead links, or links that throws an Exception.
    private static ArrayList<String> deadLinks = new ArrayList<>();

    //Stores all the links that have the keyword in it.
    public static ArrayList<String> toReturn = new ArrayList<>();

    //Number of minimum links to be returned to the user. Can adjust accordingly.
    public static final int MIN = 20;

    //The level the tree is currently at.
    private static int level=0;

    private static ExecutorService exec;

    public static void main(String[] args) throws Exception {
        // Prompts the user for a keyword
        // Crawls a domain and all it's pages for the keyword.


        Scanner sc = new Scanner(System.in);
        System.out.println("Welcome to the Spider-Man Web crawler Service! ");
        System.out.println("Please enter a keyword or a keyphrase that you would like to search");
        System.out.println("Eg. Trump, Taliban, Tesla");
        System.out.print("Enter Keyword Here: ");

        //Gets user input
        String keyWord = sc.nextLine();

        // Grabs list of web pages to be examined from predefined list of websites.
        // For purpose of demonstration and for time, only have one website in the list, http://bbc.com

        List<String> fullDataSet = readWebsiteList("websites.txt");

        for(int i =0; i<fullDataSet.size(); i++){
            linksToVisit.add(fullDataSet.get(i)); //Adds the list of websites to a linksToVisit
        }

        //Number of Threads executing crawlers
        exec = Executors.newFixedThreadPool(50);

        List<Crawler> tasks = new ArrayList<>();

        //If the linksToVisit is not empty or if at least MIN links are not found, enter while loop.
        boolean exit = false;

        while(!linksToVisit.isEmpty()){

            if(exit == true){
                break;
            }
            //Pops a new link from the linksToVisit
            String website = linksToVisit.poll();

            //Mark it as visited
            marked.add(website);

            //Create a new crawler for each link.
            Crawler task = new Crawler(website,keyWord,exec);

            //Add it to task list
            tasks.add(task);

            //Once all links have been popped and added into the queue, send this level for execution
            if(linksToVisit.isEmpty() && exit==false) {
                System.out.println("Tree is at Level: " + level + " now");
                System.out.println("Executing  " + tasks.size() + " tasks in this level. Please hold..");

                //EXECUTE.
                try {
                    //If MIN Links have been reached OR if takes longer than 1 min to query, shutdown.
                    //If invokeAll > 60 Seconds, Terminate whole while loop.
                    List<Future<String>> list = exec.invokeAll(tasks, 30, TimeUnit.SECONDS);

                    //Go to the next level
                    level++;
                    System.out.println("Number of links found for keyword  \"" + keyWord +   "\" at this level: " + toReturn.size());
                    System.out.println();

                    if (toReturn.size() >= MIN) {
                        exec.shutdown();
                        exit = true;
                        break;
                    }

                    //Not entering here.
                    for (Future<String> f: list){
                        boolean s = f.isCancelled();
                        if(s){
                            exec.shutdown();
                            exit = true;
                            break;
                        }
                    }

                }catch(Exception e){
                    //
                }

                //Clears task list for next batch of nodes on the next level.
                tasks.clear();
            }
        }

        System.out.println("Links Retrieved! Here you go: ");
        for(int i=0; i<toReturn.size(); i++){
            System.out.println("URL: " + toReturn.get(i));
        }
    }

    //Method that reads the website.txt file
    public static List<String> readWebsiteList(String fileName) throws IOException {
        ArrayList<String> websiteList = new ArrayList<>();
        Files.lines(Paths.get(fileName))
                .forEach(line -> {
                    websiteList.add(line);
                });
        return websiteList;
    }

    //Method that adds websites to the toReturn list.
    public synchronized static void addWebsiteToReturn(String website){
        //Check if toReturn doesn't contain the same website.
        if(!toReturn.contains(website)){
            toReturn.add(website);
        }
    }

    //Method that adds websites into the queue.
    public synchronized static void addToQueue(String website){
        //Checks if the website has already been added to linksToVisit or if it has already been visited
        //If linksToVisit doesn't contain website or if the website has not been visited before, add to the linksToVisit.
        if(website!=null) {
            if (!linksToVisit.contains(website)) {
                linksToVisit.add(website);
            }
        }
    }

    public synchronized static void addDeadLinks(String website){
        deadLinks.add(website);
    }

}
