package com.student;

import java.util.Scanner;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class App {
    static String[][] readingList = new String[20][3]; // [title, category, url]
    static String[][] results = new String[20][3];
    static int listCount = 0, resCount = 0;
    static Scanner input = new Scanner(System.in);

    public static void main(String[] args) {
        int choice;
        do {
            System.out.println("\n=== GUTENBERG BOOK EXPLORER ===");
            System.out.println("1. Explore Books\n2. Search by Title\n3. Search by Category");
            System.out.println("4. Add Book to Reading List\n5. View Reading List");
            System.out.println("6. Remove Book\n7. Display Book Details\n0. Exit");
            System.out.print("Enter your choice: ");
            choice = readInt();

            if (choice == 1)
                exploreBooks();
            else if (choice == 2) {
                System.out.print("Enter title: ");
                searchBook(input.nextLine().trim());
            } else if (choice == 3) {
                System.out.print("Enter category (e.g. Travel, Mystery, Poetry): ");
                searchBook(input.nextLine().trim(), true);
            } else if (choice == 4)
                addBook();
            else if (choice == 5)
                viewReadingList();
            else if (choice == 6)
                removeBook();
            else if (choice == 7)
                displayBookDetails();
            else if (choice == 0)
                System.out.println("Goodbye!");
            else
                System.out.println("Invalid choice.");
        } while (choice != 0);
        input.close();
    }

    static int readInt() {
        try {
            return Integer.parseInt(input.nextLine().trim());
        } catch (Exception e) {
            return -1;
        }
    }

    static boolean scrape(String url, String category, String keyword) {
        try {
            Document doc = Jsoup.connect(url).get();
            Elements cards = doc.select("article.product_pod");
            for (Element card : cards) {
                if (resCount == results.length)
                    break;
                Element link = card.selectFirst("h3 a");
                String title = link.attr("title");
                if (keyword.isEmpty() || title.toLowerCase().contains(keyword)) {
                    results[resCount++] = new String[] { title, category, link.absUrl("href") };
                }
            }
            return true;
        } catch (Exception e) {
            System.out.println("Connection error: " + e.getMessage());
            return false;
        }
    }

    static void exploreBooks() {
        resCount = 0;
        if (!scrape("https://books.toscrape.com/", "Catalog", ""))
            return;
        if (resCount == 0)
            System.out.println("No books found.");
        for (int i = 0; i < resCount; i++)
            System.out.println((i + 1) + ". " + results[i][0]);
    }

    static void searchBook(String title) {
        searchBook(title, false);
    }

    // Uses category when true because books.toscrape.com has no authors
    static void searchBook(String keyword, boolean byCategory) {
        if (keyword.isEmpty()) {
            System.out.println("Search term cannot be empty.");
            return;
        }
        resCount = 0;
        String key = keyword.toLowerCase();
        if (byCategory) {
            try {
                Document home = Jsoup.connect("https://books.toscrape.com/").get();
                for (Element c : home.select(".side_categories ul li ul li a")) {
                    if (c.text().toLowerCase().contains(key)) {
                        if (!scrape(c.absUrl("href"), c.text(), ""))
                            return;
                        break;
                    }
                }
            } catch (Exception e) {
                System.out.println("Connection error: " + e.getMessage());
                return;
            }
        } else {
            for (int p = 1; p <= 5 && resCount < results.length; p++) {
                if (!scrape("https://books.toscrape.com/catalogue/page-" + p + ".html", "Catalog", key))
                    return;
            }
        }
        if (resCount == 0)
            System.out.println("No books found.");
        for (int i = 0; i < resCount; i++)
            System.out.println((i + 1) + ". " + results[i][0] + " (" + results[i][1] + ")");
    }

    static void addBook() {
        if (resCount == 0)
            System.out.println("Explore or search first.");
        else if (listCount == readingList.length)
            System.out.println("Reading list is full.");
        else {
            System.out.print("Enter book number (1-" + resCount + "): ");
            int n = readInt();
            if (n < 1 || n > resCount)
                System.out.println("Invalid book number.");
            else {
                readingList[listCount++] = results[n - 1];
                System.out.println("Book added.");
            }
        }
    }

    static void viewReadingList() {
        if (listCount == 0)
            System.out.println("Reading list is empty.");
        for (int i = 0; i < listCount; i++)
            System.out.println((i + 1) + ". " + readingList[i][0]);
    }

    static void removeBook() {
        if (listCount == 0)
            System.out.println("Reading list is empty.");
        else {
            viewReadingList();
            System.out.print("Enter book number to remove: ");
            int n = readInt();
            if (n < 1 || n > listCount)
                System.out.println("Invalid book number.");
            else {
                for (int i = n - 1; i < listCount - 1; i++)
                    readingList[i] = readingList[i + 1];
                readingList[--listCount] = null;
                System.out.println("Book removed.");
            }
        }
    }

    static void displayBookDetails() {
        if (listCount == 0)
            System.out.println("Reading list is empty. Add a book first.");
        else {
            viewReadingList();
            System.out.print("Enter book number: ");
            int n = readInt();
            if (n < 1 || n > listCount)
                System.out.println("Invalid book number.");
            else {
                System.out.println("Title: " + readingList[n - 1][0]);
                System.out.println("Category: " + readingList[n - 1][1]);
                System.out.println("URL: " + readingList[n - 1][2]);
            }
        }
    }
}