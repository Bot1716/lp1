/*
Problem Statement 13: Page Replacement – Optimal
Write a program to simulate the Optimal Page Replacement algorithm.
The program should replace the page that will not be used for the longest period of time in the future and display the page replacement steps and page fault count.
*/
import java.util.*;

public class OptimalPageReplacement {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.print("Enter number of frames: ");
        int framesCount = sc.nextInt();

        System.out.print("Enter number of pages in reference string: ");
        int n = sc.nextInt();

        int[] pages = new int[n];
        System.out.println("Enter the page reference string:");
        for (int i = 0; i < n; i++) {
            pages[i] = sc.nextInt();
        }

        List<Integer> frames = new ArrayList<>();
        int pageFaults = 0;

        System.out.println("\nPage Reference\tFrames\t\tStatus");
        for (int i = 0; i < n; i++) {
            int currentPage = pages[i];
            if (frames.contains(currentPage)) {
                System.out.println(currentPage + "\t\t" + frames + "\tHit");
                continue;
            }
            if (frames.size() < framesCount) {
                frames.add(currentPage);
                pageFaults++;
                System.out.println(currentPage + "\t\t" + frames + "\tPage Fault");
                continue;
            }

            int indexToReplace = -1;
            int farthest = i + 1;

            for (int j = 0; j < frames.size(); j++) {
                int nextUse = -1;
                for (int k = i + 1; k < n; k++) {
                    if (pages[k] == frames.get(j)) {
                        nextUse = k;
                        break;
                    }
                }
                if (nextUse == -1) {
                    indexToReplace = j;  
                    break;
                } else if (nextUse > farthest) {
                    farthest = nextUse;
                    indexToReplace = j;
                }
            }

            if (indexToReplace == -1) indexToReplace = 0;
            frames.set(indexToReplace, currentPage);
            pageFaults++;

            System.out.println(currentPage + "\t\t" + frames + "\tPage Fault");
        }

        System.out.println("\nTotal Page Faults: " + pageFaults);
        System.out.println("Total Hits: " + (n - pageFaults));
    }
}
