package ui;

import dao.UserDAO;

public class HashExistingPasswords {

    public static void main(String[] args) {
        UserDAO userDAO = new UserDAO();

        boolean adminUpdated =
                userDAO.resetPassword(1, "admin123");

        boolean cashierUpdated =
                userDAO.resetPassword(2, "cash123");

        System.out.println("Admin password updated: " + adminUpdated);
        System.out.println("Cashier password updated: " + cashierUpdated);
    }
}
