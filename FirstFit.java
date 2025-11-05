/*
Problem Statement 7: Memory Allocation – First Fit
Write a program to simulate First Fit memory allocation strategy.
The program should allocate each process to the first available memory block that is large enough to accommodate it.
Display the memory allocation table and identify any unused or fragmented memory.
*/
import java.util.*;

public class FirstFit {
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
            for (int j = 0; j < m; j++) {
                if (!allocatedBlock[j] && blockSize[j] >= processSize[i]) {
                    allocation[i] = j;
                    allocatedBlock[j] = true;
                    blockSize[j] -= processSize[i];
                    break;
                }
            }
        }

      
        System.out.println("\nMemory Allocation Table:");
        System.out.println("Process\tSize\tBlock Allocated\tRemaining Block Size");
        for (int i = 0; i < n; i++) {
            if (allocation[i] != -1) {
                System.out.println("P" + (i + 1) + "\t" + processSize[i] + "\tB" + (allocation[i] + 1) +
                        "\t\t" + blockSize[allocation[i]]);
            } else {
                System.out.println("P" + (i + 1) + "\t" + processSize[i] + "\tNot Allocated\t-");
            }
        }

        
        System.out.println("\nUnused / Fragmented Memory in Blocks:");
        for (int i = 0; i < m; i++) {
            System.out.println("Block " + (i + 1) + " -> Remaining Size: " + blockSize[i]);
        }
    }
}
