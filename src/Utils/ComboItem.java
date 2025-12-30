package Utils;

/**
 * Helper object class for JComboBox and JList.
 * <br>
 * Responsibility: Stores a data pair consisting of (Display Name - Hidden ID).
 * Helps display the name on the UI but allows the programmer to retrieve the ID (for DB storage).
 */
public class ComboItem {

    // --- 1. VARIABLES ---
    private final String key;   // Display text string (e.g., "Employee A", "Laptop Dell")
    private final int value;    // Corresponding hidden ID value in Database (e.g., 1, 50, 102)

    // --- 2. CONSTRUCTOR ---

    /**
     * Constructs a ComboItem with a key and a value.
     *
     * @param key   The display text.
     * @param value The hidden ID value.
     */
    public ComboItem(String key, int value) {
        this.key = key;
        this.value = value;
    }

    // --- 3. METHODS ---

    /**
     * Retrieves the hidden ID value of the object.
     * Used when the user selects an item in the ComboBox and you need the ID to save to the DB.
     *
     * @return The ID as an integer.
     */
    public int getValue() {
        return value;
    }

    /**
     * Overrides the toString() method.
     * <br>
     * <b>Important:</b> Components like JComboBox, JList in Java Swing will call this method
     * to decide what text to display on the screen.
     *
     * @return The display name string.
     */
    @Override
    public String toString() {
        return key;
    }
}
