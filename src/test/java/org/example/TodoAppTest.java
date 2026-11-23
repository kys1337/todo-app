package org.example;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TodoAppTest {

    private TodoList todoList;
    private ByteArrayOutputStream output;
    private PrintStream originalOut;

    @BeforeEach
    void setUp() {
        todoList = new TodoList();
        originalOut = System.out;
        output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output, true, StandardCharsets.UTF_8));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    @Test
    @DisplayName("add and list print tasks with indices")
    void addAndListTasks() {
        TodoApp.execute("add Buy milk", todoList);
        TodoApp.execute("add Read a book", todoList);
        TodoApp.execute("list", todoList);

        String printed = printedOutput();
        assertTrue(printed.contains("Added: Buy milk"));
        assertTrue(printed.contains("Todo list:"));
        assertTrue(printed.contains("0. [ ] Buy milk"));
        assertTrue(printed.contains("1. [ ] Read a book"));
    }

    @Test
    @DisplayName("done marks a task as completed")
    void doneMarksTask() {
        TodoApp.execute("add Buy milk", todoList);
        TodoApp.execute("done 0", todoList);
        TodoApp.execute("list", todoList);

        String printed = printedOutput();
        assertTrue(printed.contains("Marked task at index 0 as done."));
        assertTrue(printed.contains("0. [x] Buy milk"));
    }

    @Test
    @DisplayName("done reports an invalid index")
    void doneReportsInvalidIndex() {
        TodoApp.execute("add Buy milk", todoList);
        TodoApp.execute("done 5", todoList);

        assertTrue(printedOutput().contains("Error: index 5 is out of range. Valid range: 0..0."));
    }

    @Test
    @DisplayName("search finds tasks ignoring case")
    void searchFindsTasks() {
        TodoApp.execute("add Buy milk", todoList);
        TodoApp.execute("add Buy bread", todoList);
        TodoApp.execute("add Read a book", todoList);
        TodoApp.execute("search BUY", todoList);

        String printed = printedOutput();
        assertTrue(printed.contains("Found 2 task(s):"));
        assertTrue(printed.contains("[ ] Buy milk"));
        assertTrue(printed.contains("[ ] Buy bread"));
    }

    @Test
    @DisplayName("search reports when nothing matches")
    void searchReportsNoMatches() {
        TodoApp.execute("add Buy milk", todoList);
        TodoApp.execute("search homework", todoList);

        assertTrue(printedOutput().contains("No tasks match \"homework\"."));
    }

    @Test
    @DisplayName("search requires an argument")
    void searchRequiresArgument() {
        TodoApp.execute("search", todoList);

        assertTrue(printedOutput().contains("Error: search text is required."));
    }

    @Test
    @DisplayName("clear removes every task")
    void clearRemovesEveryTask() {
        TodoApp.execute("add Buy milk", todoList);
        TodoApp.execute("add Read a book", todoList);
        TodoApp.execute("clear", todoList);
        TodoApp.execute("list", todoList);

        String printed = printedOutput();
        assertTrue(printed.contains("Todo list cleared."));
        assertTrue(printed.contains("Todo list is empty."));
        assertTrue(todoList.getAll().isEmpty());
    }

    @Test
    @DisplayName("remove reports an invalid index")
    void removeReportsInvalidIndex() {
        TodoApp.execute("add Buy milk", todoList);
        TodoApp.execute("remove 3", todoList);

        assertTrue(printedOutput().contains("Error: index 3 is out of range."));
        assertEquals(1, todoList.size());
    }

    @Test
    @DisplayName("commands validate their arguments")
    void commandsValidateArguments() {
        TodoApp.execute("add", todoList);
        assertTrue(printedOutput().contains("Error: task text is required."));

        TodoApp.execute("remove abc", todoList);
        assertTrue(printedOutput().contains("Error: index must be a non-negative integer"));

        TodoApp.execute("remove", todoList);
        assertTrue(printedOutput().contains("Error: index is required."));
    }

    @Test
    @DisplayName("unknown command is reported")
    void unknownCommandIsReported() {
        TodoApp.execute("dance", todoList);

        assertTrue(printedOutput().contains("Unknown command: dance."));
    }

    @Test
    @DisplayName("exit stops the loop")
    void exitStopsTheLoop() {
        assertFalse(TodoApp.execute("exit", todoList));
    }

    private String printedOutput() {
        return output.toString(StandardCharsets.UTF_8);
    }
}
