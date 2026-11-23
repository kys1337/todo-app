package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TodoListTest {

    private TodoList todoList;

    @BeforeEach
    void setUp() {
        todoList = new TodoList();
    }

    @Nested
    @DisplayName("add")
    class Add {

        @Test
        void addsTaskAndTrimsWhitespace() {
            assertTrue(todoList.add("  Buy milk  "));

            assertEquals(1, todoList.size());
            assertEquals(List.of("[ ] Buy milk"), todoList.getAll());
        }

        @Test
        void keepsInsertionOrder() {
            todoList.add("First");
            todoList.add("Second");
            todoList.add("Third");

            assertEquals(List.of("[ ] First", "[ ] Second", "[ ] Third"), todoList.getAll());
        }

        @Test
        void ignoresNullAndBlankValues() {
            assertFalse(todoList.add(null));
            assertFalse(todoList.add(""));
            assertFalse(todoList.add("   "));
            assertFalse(todoList.add("\t\n"));

            assertEquals(0, todoList.size());
            assertTrue(todoList.getAll().isEmpty());
        }
    }

    @Nested
    @DisplayName("remove")
    class Remove {

        @Test
        void removesTaskByIndex() {
            todoList.add("First");
            todoList.add("Second");
            todoList.add("Third");

            assertTrue(todoList.remove(1));

            assertEquals(2, todoList.size());
            assertEquals(List.of("[ ] First", "[ ] Third"), todoList.getAll());
        }

        @Test
        void returnsFalseForNegativeIndex() {
            todoList.add("First");

            assertFalse(todoList.remove(-1));
            assertEquals(1, todoList.size());
        }

        @Test
        void returnsFalseForIndexGreaterOrEqualToSize() {
            todoList.add("First");

            assertFalse(todoList.remove(1));
            assertFalse(todoList.remove(99));
            assertEquals(1, todoList.size());
        }

        @Test
        void returnsFalseWhenListIsEmpty() {
            assertFalse(todoList.remove(0));
        }
    }

    @Nested
    @DisplayName("getAll")
    class GetAll {

        @Test
        void returnsCopyOfTaskList() {
            todoList.add("Task");

            List<String> snapshot = todoList.getAll();
            snapshot.clear();

            assertEquals(1, todoList.size());
            assertEquals(List.of("[ ] Task"), todoList.getAll());
        }

        @Test
        void showsCompletionStatus() {
            todoList.add("Open task");
            todoList.add("Done task");
            todoList.markDone(1);

            assertEquals(List.of("[ ] Open task", "[x] Done task"), todoList.getAll());
        }
    }

    @Nested
    @DisplayName("clear")
    class Clear {

        @Test
        void removesAllTasks() {
            todoList.add("First");
            todoList.add("Second");

            todoList.clear();

            assertEquals(0, todoList.size());
            assertTrue(todoList.getAll().isEmpty());
        }

        @Test
        void canBeUsedOnEmptyList() {
            todoList.clear();

            assertEquals(0, todoList.size());
        }
    }

    @Nested
    @DisplayName("markDone")
    class MarkDone {

        @Test
        void marksTaskAsDone() {
            todoList.add("Read a book");

            assertTrue(todoList.markDone(0));
            assertEquals(List.of("[x] Read a book"), todoList.getAll());
        }

        @Test
        void returnsFalseForInvalidIndex() {
            todoList.add("Read a book");

            assertFalse(todoList.markDone(-1));
            assertFalse(todoList.markDone(1));
            assertEquals(List.of("[ ] Read a book"), todoList.getAll());
        }

        @Test
        void keepsStatusAfterRemovingAnotherTask() {
            todoList.add("First");
            todoList.add("Second");
            todoList.add("Third");
            todoList.markDone(2);

            assertTrue(todoList.remove(0));

            assertEquals(List.of("[ ] Second", "[x] Third"), todoList.getAll());
        }
    }

    @Nested
    @DisplayName("search")
    class Search {

        @Test
        void findsTasksBySubstring() {
            todoList.add("Buy milk");
            todoList.add("Buy bread");
            todoList.add("Read a book");

            List<String> matches = todoList.search("Buy");

            assertEquals(List.of("[ ] Buy milk", "[ ] Buy bread"), matches);
        }

        @Test
        void ignoresCase() {
            todoList.add("Learn Java");

            assertEquals(List.of("[ ] Learn Java"), todoList.search("java"));
            assertEquals(List.of("[ ] Learn Java"), todoList.search("LEARN"));
        }

        @Test
        void returnsEmptyListWhenNothingMatches() {
            todoList.add("Buy milk");

            assertTrue(todoList.search("homework").isEmpty());
        }

        @Test
        void includesCompletionStatusInResults() {
            todoList.add("Buy milk");
            todoList.markDone(0);

            assertEquals(List.of("[x] Buy milk"), todoList.search("milk"));
        }

        @Test
        void returnsEmptyListForNullQuery() {
            todoList.add("Buy milk");

            assertTrue(todoList.search(null).isEmpty());
        }
    }

    @Test
    void reportsSize() {
        assertEquals(0, todoList.size());

        todoList.add("Task");
        todoList.add("Another task");

        assertEquals(2, todoList.size());
    }
}
