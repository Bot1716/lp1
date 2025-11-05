/*
Problem Statement 12: Page Replacement – LRU
Write a program to simulate the Least Recently Used (LRU) page replacement algorithm.
The program should display the frame contents after each page reference and the total number of page faults.
*/
import java.util.*;

public class LRUPageReplacement {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.print("Enter number of frames: ");
        int framesCount = sc.nextInt();

        System.out.print("Enter number of pages: ");
        int n = sc.nextInt();

        int[] pages = new int[n];
        System.out.println("Enter page reference string:");
        for (int i = 0; i < n; i++) {
            pages[i] = sc.nextInt();
        }

        List<Integer> frames = new ArrayList<>();
        int pageFaults = 0;

        System.out.println("\nPage Reference\tFrames\t\tPage Fault");

        for (int page : pages) {
            if (!frames.contains(page)) { 
                if (frames.size() == framesCount) {
                    frames.remove(0); 
                }
                frames.add(page);
                pageFaults++;
                System.out.println(page + "\t\t" + frames + "\tYes");
            } else {
                
                frames.remove(Integer.valueOf(page));
                frames.add(page);
                System.out.println(page + "\t\t" + frames + "\tNo");
            }
        }

        System.out.println("\nTotal Page Faults = " + pageFaults);
        sc.close();
    }
}
