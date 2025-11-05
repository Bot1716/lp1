/*
Problem Statement 9: Memory Allocation – Next Fit
Write a program to simulate Next Fit memory allocation strategy.
The program should continue searching for the next suitable memory block from the last allocated position instead of starting from the beginning.
Display the memory allocation table and fragmentation details.
*/
import java.util.*;
public class NextFit {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter number of memory blocks: ");
        int m = sc.nextInt();
        int[] blockSize = new int[m];
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
        int lastAllocatedIndex = 0;
        for (int i = 0; i < n; i++) {
            int count = 0;
            boolean allocated = false;
            while (count < m) {
                if (blockSize[lastAllocatedIndex] >= processSize[i]) {
                    allocation[i] = lastAllocatedIndex;
                    blockSize[lastAllocatedIndex] -= processSize[i];
                    allocated = true;
                    break;
                }
                lastAllocatedIndex = (lastAllocatedIndex + 1) % m;
                count++;
            }
            if (allocated)
                lastAllocatedIndex = (lastAllocatedIndex + 1) % m; // Move to next block after allocation
        }
        System.out.println("\nMemory Allocation Table:");
        System.out.println("Process\tSize\tBlock Allocated\tInternal Fragmentation");
        for (int i = 0; i < n; i++) {
            if (allocation[i] != -1) {
                System.out.println("P" + (i + 1) + "\t" + processSize[i] + "\tB" + (allocation[i] + 1) + "\t\t" + blockSize[allocation[i]]);
            } else {
                System.out.println("P" + (i + 1) + "\t" + processSize[i] + "\tNot Allocated\t-");
            }
        }
    }
}
