/*
Problem Statement 11: Page Replacement – FIFO
Write a program to simulate the First In First Out (FIFO) page replacement algorithm.
The program should accept a page reference string and number of frames as input,
simulate the process of page replacement, and display the total number of page faults.
*/
import java.util.*;

public class FIFOPageReplacement {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.print("Enter number of frames: ");
        int frames = sc.nextInt();

        System.out.print("Enter length of page reference string: ");
        int n = sc.nextInt();

        int[] pages = new int[n];
        System.out.println("Enter the page reference string:");
        for (int i = 0; i < n; i++) {
            pages[i] = sc.nextInt();
        }

        Set<Integer> frameSet = new HashSet<>();
        Queue<Integer> frameQueue = new LinkedList<>();

        int pageFaults = 0;

        System.out.println("\nPage\tFrames\t\tPage Fault");

        for (int page : pages) {
            if (!frameSet.contains(page)) {
                
                pageFaults++;

                if (frameSet.size() < frames) {
                    frameSet.add(page);
                    frameQueue.add(page);
                } else {
                    int oldest = frameQueue.poll();
                    frameSet.remove(oldest);

                    frameSet.add(page);
                    frameQueue.add(page);
                }
                System.out.println(page + "\t" + frameQueue + "\tYes");
            } else {
                System.out.println(page + "\t" + frameQueue + "\tNo");
            }
        }
        System.out.println("\nTotal Page Faults = " + pageFaults);
        sc.close();
    }
}
