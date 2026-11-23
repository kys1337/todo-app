package org.example;

import java.util.List;
import java.util.Locale;
import java.util.Scanner;

/**
 * Console entry point for the todo application.
 */
public final class TodoApp {

    private static final String PROMPT = "> ";

    private TodoApp() {
    }

    public static void main(String[] args) {
        TodoList todoList = new TodoList();
        printWelcome();
        try (Scanner scanner = new Scanner(System.in)) {
            boolean running = true;
            while (running) {
                System.out.print(PROMPT);
                if (!scanner.hasNextLine()) {
                    System.out.println();
                    break;
                }
                String line = scanner.nextLine().trim();
                if (line.isEmpty()) {
                    continue;
                }
                running = execute(line, todoList);
            }
        }
        System.out.println("Goodbye!");
    }

    /**
     * Executes one command line.
     *
     * @param line     raw command line
     * @param todoList target list
     * @return {@code false} when the application should stop
     */
    static boolean execute(String line, TodoList todoList) {
        String command;
        String argument;
        int separator = line.indexOf(' ');
        if (separator < 0) {
            command = line;
            argument = "";
        } else {
            command = line.substring(0, separator);
            argument = line.substring(separator + 1).trim();
        }

        switch (command.toLowerCase(Locale.ROOT)) {
            case "add" -> {
                if (argument.isEmpty()) {
                    System.out.println("Error: task text is required. Usage: add <task>");
                    return true;
                }
                todoList.add(argument);
                System.out.println("Added: " + argument);
            }
            case "remove" -> removeTask(todoList, argument);
            case "done" -> markTaskDone(todoList, argument);
            case "search" -> searchTasks(todoList, argument);
            case "list" -> printTasks(todoList);
            case "clear" -> {
                todoList.clear();
                System.out.println("Todo list cleared.");
            }
            case "help" -> printWelcome();
            case "exit" -> {
                return false;
            }
            default -> System.out.println("Unknown command: " + command
                    + ". Type 'help' to see available commands.");
        }
        return true;
    }

    private static void removeTask(TodoList todoList, String argument) {
        Integer index = parseIndex(argument);
        if (index == null) {
            return;
        }
        if (todoList.remove(index)) {
            System.out.println("Removed task at index " + index + ".");
        } else {
            printIndexError(todoList, index);
        }
    }

    private static void markTaskDone(TodoList todoList, String argument) {
        Integer index = parseIndex(argument);
        if (index == null) {
            return;
        }
        if (todoList.markDone(index)) {
            System.out.println("Marked task at index " + index + " as done.");
        } else {
            printIndexError(todoList, index);
        }
    }

    private static void searchTasks(TodoList todoList, String argument) {
        if (argument.isEmpty()) {
            System.out.println("Error: search text is required. Usage: search <text>");
            return;
        }
        List<String> matches = todoList.search(argument);
        if (matches.isEmpty()) {
            System.out.println("No tasks match \"" + argument + "\".");
            return;
        }
        System.out.println("Found " + matches.size() + " task(s):");
        for (int index = 0; index < matches.size(); index++) {
            System.out.println("  " + (index + 1) + ". " + matches.get(index));
        }
    }

    private static void printTasks(TodoList todoList) {
        List<String> tasks = todoList.getAll();
        if (tasks.isEmpty()) {
            System.out.println("Todo list is empty.");
            return;
        }
        System.out.println("Todo list:");
        for (int index = 0; index < tasks.size(); index++) {
            System.out.println("  " + index + ". " + tasks.get(index));
        }
    }

    private static Integer parseIndex(String argument) {
        if (argument.isEmpty()) {
            System.out.println("Error: index is required. Usage: <command> <index>");
            return null;
        }
        try {
            return Integer.valueOf(argument);
        } catch (NumberFormatException exception) {
            System.out.println("Error: index must be a non-negative integer, got \"" + argument + "\".");
            return null;
        }
    }

    private static void printIndexError(TodoList todoList, int index) {
        if (todoList.size() == 0) {
            System.out.println("Error: todo list is empty.");
        } else {
            System.out.println("Error: index " + index + " is out of range. Valid range: 0.."
                    + (todoList.size() - 1) + ".");
        }
    }

    private static void printWelcome() {
        System.out.println("=== Todo App ===");
        System.out.println("Available commands:");
        System.out.println("  add <task>     - add a new task");
        System.out.println("  remove <index> - remove a task by its index");
        System.out.println("  done <index>   - mark a task as completed");
        System.out.println("  search <text>  - search tasks by substring");
        System.out.println("  list           - show all tasks");
        System.out.println("  clear          - remove all tasks");
        System.out.println("  help           - show this help");
        System.out.println("  exit           - quit the application");
        System.out.println("Indices are zero-based.");
    }
}
