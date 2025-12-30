package Utils;

/**
 * Class managing the current user's Session.
 * <br>
 * <b>Responsibility:</b> Stores global information of the staff after successful login.
 * These variables can be accessed from anywhere in the software to:
 * <ul>
 * <li>Display user name ("Hello, Nguyen Van A").</li>
 * <li>Record history (Who created this invoice?).</li>
 * <li>Authorize (Hide/Show buttons based on role).</li>
 * </ul>
 */
public class Session {

    // --- 1. STATE ---

    /**
     * Staff ID in the database.
     * <br>Default value: -1 (No one logged in).
     */
    public static int loggedInStaffID = -1;

    /**
     * Display name of the staff.
     */
    public static String loggedInStaffName = "";

    /**
     * Staff role (e.g., "Admin", "Staff", "SaleStaff", "StorageStaff", "Manager").
     * Used to determine access permissions.
     */
    public static String userRole = "";

    /**
     * Login status flag.
     * <br>True: Logged in | False: Not logged in.
     */
    public static boolean isLoggedIn = false;


    // --- 2. BEHAVIOR ---

    /**
     * Clears the current session information.
     * <br>
     * <b>Usage:</b> When the user clicks "Log Out".
     * Values are reset to default to prevent the next user from using the old account.
     */
    public static void clear() {
        loggedInStaffID = -1;
        loggedInStaffName = "";
        userRole = "";
        isLoggedIn = false;
    }

    /**
     * Checks for Administrator privileges.
     *
     * @return <b>true</b> if the role is "Admin" (case-insensitive).
     */
    public static boolean isAdmin() {
        return userRole.equalsIgnoreCase("Admin");
    }

    /**
     * Checks for Manager privileges.
     *
     * @return <b>true</b> if the role is "Manager" or "Admin".
     */
    public static boolean isManager() {
        return userRole.equalsIgnoreCase("Manager") || isAdmin();
    }

    /**
     * Checks for Sale Staff privileges.
     *
     * @return <b>true</b> if the role is "SaleStaff" or "Manager".
     */
    public static boolean isSaleStaff() {
        return userRole.equalsIgnoreCase("SaleStaff") || isManager();
    }

    /**
     * Checks for Storage Staff privileges.
     *
     * @return <b>true</b> if the role is "StorageStaff" or "Manager".
     */
    public static boolean isStorageStaff() {
        return userRole.equalsIgnoreCase("StorageStaff") || isManager();
    }

    // --- Helper methods for specific permissions ---

    /**
     * Checks if the user can manage customers.
     *
     * @return <b>true</b> if the user has permission.
     */
    public static boolean canManageCustomers() {
        return isSaleStaff();
    }

    /**
     * Checks if the user can create invoices.
     *
     * @return <b>true</b> if the user has permission.
     */
    public static boolean canCreateInvoice() {
        return isSaleStaff();
    }

    /**
     * Checks if the user can manage suppliers.
     *
     * @return <b>true</b> if the user has permission.
     */
    public static boolean canManageSuppliers() {
        return isStorageStaff();
    }

    /**
     * Checks if the user can manage products.
     *
     * @return <b>true</b> if the user has permission.
     */
    public static boolean canManageProducts() {
        return isStorageStaff();
    }

    /**
     * Checks if the user can edit or delete invoices.
     *
     * @return <b>true</b> if the user has permission.
     */
    public static boolean canEditDeleteInvoice() {
        return isManager();
    }

    /**
     * Checks if the user can view statistics.
     *
     * @return <b>true</b> if the user has permission.
     */
    public static boolean canViewStats() {
        return isManager();
    }

    /**
     * Checks if the user can manage product types.
     *
     * @return <b>true</b> if the user has permission.
     */
    public static boolean canManageTypes() {
        return isManager();
    }

    /**
     * Checks if the user can manage discounts.
     *
     * @return <b>true</b> if the user has permission.
     */
    public static boolean canManageDiscounts() {
        return isManager();
    }

    /**
     * Checks if the user can manage staff.
     *
     * @return <b>true</b> if the user has permission.
     */
    public static boolean canManageStaff() {
        return isAdmin();
    }
}
