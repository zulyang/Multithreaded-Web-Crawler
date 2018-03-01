package com.company;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;


public class Crawler implements Callable<String> {

    private String url;
    private String keyword;
    private ExecutorService exec;

    public Crawler(String url, String keyword, ExecutorService exec){
        this.url = url;
        this.keyword = keyword;
        this.exec = exec;
    }

    @Override
    public String call() throws Exception{

            try {
                Document doc = Jsoup.connect(url).get();
                String title = doc.title();

                //Add the link if title contains keyword and if toReturn size is less than the Max.
                if(title.contains(keyword) && WebCrawlerMain.toReturn.size()<= WebCrawlerMain.MIN){
                    WebCrawlerMain.addWebsiteToReturn(url);
                    //System.out.println(WebCrawlerMain.toReturn.size());
                }

                //Gets all the sub pages for that page
                Elements links = doc.select("a[href]");
                for (Element link : links) {
                    //Ensures that it returns the absolute URL instead of the reference URl
                    String linkURL = link.attr("abs:href");
                    WebCrawlerMain.addToQueue(linkURL);
                }

            }catch(IOException e){
                //Do something
            }

            return null;
        }

}

