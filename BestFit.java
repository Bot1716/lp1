/*
Problem Statement 8: Memory Allocation – Best Fit
Write a program to simulate Best Fit memory allocation strategy.
The program should allocate each process to the smallest available block that can hold it.
Display the final allocation and show internal fragmentation if any.
*/
import java.util.*;

public class BestFit {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.print("Enter number of memory blocks: ");
        int m = sc.nextInt();
        int[] blockSize = new int[m];
        boolean[] allocatedBlock = new boolean[m];

        System.out.println("Enter size of each memory block:");
        for (int i = 0; i < m; i++) {
            System.out.print("Block " + (i + 1) + ": ");
            blockSize[i] = sc.nextInt();
        }

        System.out.print("\nEnter number of processes: ");
        int n = sc.nextInt();
        int[] processSize = new int[n];
        int[] allocation = new int[n];
        Arrays.fill(allocation, -1);

        System.out.println("Enter size of each process:");
        for (int i = 0; i < n; i++) {
            System.out.print("Process " + (i + 1) + ": ");
            processSize[i] = sc.nextInt();
        }

        
        for (int i = 0; i < n; i++) {
            int bestIndex = -1;
            for (int j = 0; j < m; j++) {
                if (!allocatedBlock[j] && blockSize[j] >= processSize[i]) {
                    if (bestIndex == -1 || blockSize[j] < blockSize[bestIndex]) {
                        bestIndex = j;
                    }
                }
            }
            if (bestIndex != -1) {
                allocation[i] = bestIndex;
                allocatedBlock[bestIndex] = true;
                blockSize[bestIndex] -= processSize[i];
            }
        }

        
        System.out.println("\nMemory Allocation Table:");
        System.out.println("Process\tSize\tBlock Allocated\tInternal Fragmentation");
        for (int i = 0; i < n; i++) {
            if (allocation[i] != -1) {
                System.out.println("P" + (i + 1) + "\t" + processSize[i] + "\tB" + (allocation[i] + 1) +
                        "\t\t" + blockSize[allocation[i]]);
            } else {
                System.out.println("P" + (i + 1) + "\t" + processSize[i] + "\tNot Allocated\t-");
            }
        }
    }
}
