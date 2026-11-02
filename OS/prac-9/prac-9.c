
#include <stdio.h>
#include <stdlib.h>

int main() {
    int p, r;

    printf("Enter number of processes: ");
    scanf("%d", &p);

    printf("Enter number of resources: ");
    scanf("%d", &r);

    int allocation[p][r], max[p][r], need[p][r];
    int available[r], safeSeq[p], finish[p];

    printf("Enter Allocation Matrix:\n");
    for (int i = 0; i < p; i++) {
        for (int j = 0; j < r; j++) {
            scanf("%d", &allocation[i][j]);
        }
    }

    printf("Enter Max Matrix:\n");
    for (int i = 0; i < p; i++) {
        for (int j = 0; j < r; j++) {
            scanf("%d", &max[i][j]);
            need[i][j] = max[i][j] - allocation[i][j];
        }
    }

    printf("Enter Available Resources:\n");
    for (int i = 0; i < r; i++) {
        scanf("%d", &available[i]);
    }

    printf("\nNeed Matrix:\n");
    for (int i = 0; i < p; i++) {
        finish[i] = 0;

        for (int j = 0; j < r; j++) {
            printf("%d ", need[i][j]);
        }
        printf("\n");
    }

    int work[r];

    for (int i = 0; i < r; i++) {
        work[i] = available[i];
    }

    int count = 0;

    while (count < p) {
        int found = 0;

        for (int i = 0; i < p; i++) {
            if (finish[i] == 0) {
                int j;

                for (j = 0; j < r; j++) {
                    if (need[i][j] > work[j]) {
                        break;
                    }
                }

                if (j == r) {
                    for (int k = 0; k < r; k++) {
                        work[k] += allocation[i][k];
                    }

                    safeSeq[count++] = i;
                    finish[i] = 1;
                    found = 1;
                }
            }
        }

        if (found == 0) {
            break;
        }
    }

    if (count == p) {
        printf("\nSystem is in a SAFE state.\n");
        printf("Safe sequence is: ");

        for (int i = 0; i < p; i++) {
            printf("P%d ", safeSeq[i]);
            printf("-> ");
        }

        printf("\n");
    }
    else {
        printf("\nSystem is in an UNSAFE state.\n");
    }

    return 0;
}
