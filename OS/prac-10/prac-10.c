#include <stdio.h>
void fifo(int pages[], int n, int capacity) {
    int frames[100];
    int page_faults = 0, fifo_index = 0;
    
    for (int i = 0; i < capacity; i++) frames[i] = -1;
    printf("\n--- FIFO Page Replacement ---\n");
    printf("Incoming\tFrame State\t\tStatus\n");

    for (int i = 0; i < n; i++) {
        int page = pages[i];
        int found = 0;
        for (int j = 0; j < capacity; j++) {
            if (frames[j] == page) {
                found = 1;
                break;
            }
        }
        if (!found) {
            frames[fifo_index] = page;
            fifo_index = (fifo_index + 1) % capacity;
            page_faults++;  
            printf("%d\t\t", page);
            for (int j = 0; j < capacity; j++) {
                if (frames[j] != -1) printf("%d ", frames[j]);
                else printf("- ");
            }
            printf("\t\tPage Fault\n");
        } else {
            printf("%d\t\t", page);
            for (int j = 0; j < capacity; j++) {
                if (frames[j] != -1) printf("%d ", frames[j]);
                else printf("- ");
            }
            printf("\t\tHit\n");
        }
    }
    printf("Total Page Faults (FIFO): %d\n", page_faults);
}

void lru(int pages[], int n, int capacity) {
    int frames[100], time[100];
    int page_faults = 0, counter = 0;

    for (int i = 0; i < capacity; i++) {
        frames[i] = -1;
        time[i] = 0;
    }

    printf("\n--- LRU Page Replacement ---\n");
    printf("Incoming\tFrame State\t\tStatus\n");

    for (int i = 0; i < n; i++) {
        int page = pages[i];
        int found = 0;

        for (int j = 0; j < capacity; j++) {
            if (frames[j] == page) {
                counter++;
                time[j] = counter;
                found = 1;
                break;
            }
        }

        if (!found) {
            int pos = -1;
            // Check for empty frame first
            for (int j = 0; j < capacity; j++) {
                if (frames[j] == -1) {
                    pos = j;
                    break;
                }
            }
            // If no empty frame, find LRU frame
            if (pos == -1) {
                int min = time[0];
                pos = 0;
                for (int j = 1; j < capacity; j++) {
                    if (time[j] < min) {
                        min = time[j];
                        pos = j;
                    }
                }
            }

            counter++;
            frames[pos] = page;
            time[pos] = counter;
            page_faults++;

            printf("%d\t\t", page);
            for (int j = 0; j < capacity; j++) {
                if (frames[j] != -1) printf("%d ", frames[j]);
                else printf("- ");
            }
            printf("\t\tPage Fault\n");
        } else {
            printf("%d\t\t", page);
            for (int j = 0; j < capacity; j++) {
                if (frames[j] != -1) printf("%d ", frames[j]);
                else printf("- ");
            }
            printf("\t\tHit\n");
        }
    }
    printf("Total Page Faults (LRU): %d\n", page_faults);
}

int main() {
    FILE *fp;
    int capacity, n = 0;
    int pages[100];

    fp = fopen("input.txt", "r");
    if (fp == NULL) {
        printf("Error opening file!\n");
        return 1;
    }

    fscanf(fp, "%d", &capacity);
    while (fscanf(fp, "%d", &pages[n]) != EOF) {
        n++;
    }
    fclose(fp);

    printf("Number of Frames: %d\n", capacity);
    printf("Reference String (%d pages): ", n);
    for (int i = 0; i < n; i++) {
        printf("%d ", pages[i]);
    }
    printf("\n");

    fifo(pages, n, capacity);
    lru(pages, n, capacity);

    return 0;
}