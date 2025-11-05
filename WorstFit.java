/*
Problem Statement 10: Memory Allocation – Worst Fit
Write a program to simulate Worst Fit memory allocation strategy.
The program should allocate each process to the largest available memory block.
Display the memory allocation results and any unused space.
*/
import java.util.Scanner;

public class WorstFit {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter number of memory blocks: ");
        int m = sc.nextInt();
        int[] blockSize = new int[m];
        System.out.println("Enter sizes of memory blocks:");
        for (int i = 0; i < m; i++) {
            blockSize[i] = sc.nextInt();
        }
        System.out.print("Enter number of processes: ");
        int n = sc.nextInt();
        int[] processSize = new int[n];
        System.out.println("Enter sizes of processes:");
        for (int i = 0; i < n; i++) {
            processSize[i] = sc.nextInt();
        }
        int[] allocation = new int[n];
        for (int i = 0; i < n; i++) allocation[i] = -1;
        for (int i = 0; i < n; i++) {
            int worstIdx = -1;
            for (int j = 0; j < m; j++) {
                if (blockSize[j] >= processSize[i]) {
                    if (worstIdx == -1 || blockSize[j] > blockSize[worstIdx]) {
                        worstIdx = j;
                    }
                }
            }
            if (worstIdx != -1) {
                allocation[i] = worstIdx;
                blockSize[worstIdx] -= processSize[i];
            }
        }
        System.out.println("\nProcess No.\tProcess Size\tBlock No.\tRemaining Space");
        for (int i = 0; i < n; i++) {
            if (allocation[i] != -1)
                System.out.println((i + 1) + "\t\t" + processSize[i] + "\t\t" + (allocation[i] + 1) + "\t\t" + blockSize[allocation[i]]);
            else
                System.out.println((i + 1) + "\t\t" + processSize[i] + "\t\tNot Allocated\t-");
        }
        sc.close();
    }
}
