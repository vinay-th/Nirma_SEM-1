#include <stdio.h>
#include <stdlib.h>

struct MemoryNode {
    int address;
    int size;
    struct MemoryNode *next;
};

struct MemoryNode *freeList = NULL;
struct MemoryNode *usedList = NULL;

void addToFreeList(int address, int size){
    struct MemoryNode *newNode;
    struct MemoryNode *current;

    newNode = (struct MemoryNode *)malloc(sizeof(struct MemoryNode));

    newNode->address = address;
    newNode->size = size;
    newNode->next = NULL;

    if (freeList == NULL || address < freeList->address) {
        newNode->next = freeList;
        freeList = newNode;
    }
    else {
        current = freeList;

        while (current->next != NULL && current->next->address < address) {
            current = current->next;
        }

        newNode->next = current->next;
        current->next = newNode;
    }

    current = freeList;

    while (current != NULL && current->next != NULL) {
        if (current->address + current->size == current->next->address) {
            struct MemoryNode *temp = current->next;

            current->size += temp->size;
            current->next = temp->next;

            free(temp);
        }
        else {
            current = current->next;
        }
    }
}

void addToUsedList(int address, int size){
    struct MemoryNode *newNode;

    newNode = (struct MemoryNode *)malloc(sizeof(struct MemoryNode));

    newNode->address = address;
    newNode->size = size;

    newNode->next = usedList;
    usedList = newNode;
}

int allocateMemory(int size){
    struct MemoryNode *current;
    struct MemoryNode *prev;
    int address;

    if (size <= 0) {
        printf("Invalid allocation size!\n");
        return -1;
    }

    current = freeList;
    prev = NULL;

    while (current != NULL) {
        if (current->size >= size) {
            address = current->address;
            if (current->size == size) {
                if (prev == NULL) {
                    freeList = current->next;
                }
                else {
                    prev->next = current->next;
                }

                free(current);
            }
            else {
                current->address += size;
                current->size -= size;
            }

            addToUsedList(address, size);

            return address;
        }

        prev = current;
        current = current->next;
    }

    printf("Not enough memory available!\n");

    return -1;
}

void deallocateMemory(int address){
    struct MemoryNode *current;
    struct MemoryNode *prev;

    current = usedList;
    prev = NULL;

    while (current != NULL) {
        if (current->address == address) {
            if (prev == NULL) {
                usedList = current->next;
            }
            else {
                prev->next = current->next;
            }

            addToFreeList(current->address, current->size);

            free(current);

            return;
        }

        prev = current;
        current = current->next;
    }

    printf("Address %d is not allocated!\n", address);
}


void displayFreeList(){
    struct MemoryNode *current = freeList;

    printf("\nFree Memory List:\n");
    printf("-----------------\n");

    if (current == NULL) {
        printf("No free memory\n");
        return;
    }

    while (current != NULL) {
        printf("Address: %d\tSize: %d\n",
               current->address,
               current->size);

        current = current->next;
    }
}

void displayUsedList(){
    struct MemoryNode *current = usedList;

    printf("\nUsed Memory List:\n");
    printf("-----------------\n");

    if (current == NULL) {
        printf("No used memory\n");
        return;
    }

    while (current != NULL) {
        printf("Address: %d\tSize: %d\n", current->address, current->size);

        current = current->next;
    }
}


int main(){
    int mem1, mem2, mem3;

    addToFreeList(0, 1024);

    printf("Initial Memory:");
    displayFreeList();

    mem1 = allocateMemory(256);
    mem2 = allocateMemory(128);
    mem3 = allocateMemory(512);

    printf("\nAfter Allocation:");
    displayFreeList();
    displayUsedList();

    deallocateMemory(mem2);

    deallocateMemory(mem1);

    printf("\nAfter Deallocation of mem1 and mem2:");
    displayFreeList();
    displayUsedList();

    deallocateMemory(mem3);

    printf("\nAfter Deallocation of mem3:");
    displayFreeList();
    displayUsedList();

    return 0;
}
