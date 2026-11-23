package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * In-memory todo list with completion status support.
 *
 * <p>Task text is always stored trimmed. A parallel list keeps track of which
 * tasks are completed, so the textual order is preserved when tasks are
 * removed.</p>
 */
public class TodoList {

    private static final String OPEN_PREFIX = "[ ] ";
    private static final String DONE_PREFIX = "[x] ";

    private final List<String> tasks = new ArrayList<>();
    private final List<Boolean> doneFlags = new ArrayList<>();

    /**
     * Adds a task to the end of the list.
     *
     * @param task task text; {@code null} and blank values are ignored
     * @return {@code true} if the task was added
     */
    public boolean add(String task) {
        if (task == null) {
            return false;
        }
        String normalized = task.trim();
        if (normalized.isEmpty()) {
            return false;
        }
        tasks.add(normalized);
        doneFlags.add(Boolean.FALSE);
        return true;
    }

    /**
     * Removes a task by its zero-based index.
     *
     * @param index task index
     * @return {@code true} if the task was removed, {@code false} when the index is out of range
     */
    public boolean remove(int index) {
        if (!isValidIndex(index)) {
            return false;
        }
        tasks.remove(index);
        doneFlags.remove(index);
        return true;
    }

    /**
     * Returns a copy of all tasks in display form, including their status.
     *
     * @return new list with entries like {@code "[ ] Buy milk"} or {@code "[x] Read a book"}
     */
    public List<String> getAll() {
        List<String> snapshot = new ArrayList<>(tasks.size());
        for (int index = 0; index < tasks.size(); index++) {
            snapshot.add(format(index));
        }
        return snapshot;
    }

    /**
     * @return number of tasks in the list
     */
    public int size() {
        return tasks.size();
    }

    /**
     * Removes every task from the list.
     */
    public void clear() {
        tasks.clear();
        doneFlags.clear();
    }

    /**
     * Marks a task as completed.
     *
     * @param index task index
     * @return {@code true} if the task was marked, {@code false} when the index is out of range
     */
    public boolean markDone(int index) {
        if (!isValidIndex(index)) {
            return false;
        }
        doneFlags.set(index, Boolean.TRUE);
        return true;
    }

    /**
     * Searches tasks by a case-insensitive substring.
     *
     * @param query text to search for
     * @return matching tasks in display form; empty list when nothing matches or the query is {@code null}
     */
    public List<String> search(String query) {
        if (query == null) {
            return new ArrayList<>();
        }
        String needle = query.trim().toLowerCase(Locale.ROOT);
        List<String> matches = new ArrayList<>();
        for (int index = 0; index < tasks.size(); index++) {
            if (tasks.get(index).toLowerCase(Locale.ROOT).contains(needle)) {
                matches.add(format(index));
            }
        }
        return matches;
    }

    private String format(int index) {
        String prefix = doneFlags.get(index) ? DONE_PREFIX : OPEN_PREFIX;
        return prefix + tasks.get(index);
    }

    private boolean isValidIndex(int index) {
        return index >= 0 && index < tasks.size();
    }
}
