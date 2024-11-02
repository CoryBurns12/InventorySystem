import java.util.Scanner;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.DriverManager;
import java.sql.Statement;

public class MenuSystem {

    private Scanner scan = new Scanner(System.in);
    private final String dataBase = "jdbc:mysql://localhost:3306/Inventory";
    private final String userName = System.getenv("DATABASEUSER");
    private final String passWord = System.getenv("DATABASEPW");
    private String queryMessage = null;
    private Connection connect;

    public Connection Connect() throws SQLException {
        if (connect == null || connect.isClosed())
            connect = DriverManager.getConnection(dataBase, userName, passWord);

        return connect;
    }

    public void closeConnection() {
        try {
            if (connect != null && !connect.isClosed()) {
                connect.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void Menu() {
        int[] optionInt = {1, 2, 3, 4};
        String[] optionNames = {"ADD STOCK", "VIEW INVENTORY", "BUY ITEM", "EXIT"};

        for (int i = 0; i < optionInt.length; i++) {
            System.out.println(i + 1 + ". " + optionNames[i]);
        }
        
        System.out.println();

        System.out.print("What would you like to do? -> ");
        int userChoice = scan.nextInt();

        switch (userChoice) {
            case 1:
                try
                {
                    Add();
                    Menu();
                }
                catch (SQLException e)
                {
                    e.printStackTrace();
                }
                break;
            case 2:
                ViewInventory();
                Menu();
                break;
            case 3:
                try
                {
                    Buy();
                    Menu();
                }
                catch (SQLException e)
                {
                    e.printStackTrace();
                }
                break;
            case 4:
                System.out.println("PROGRAM TERMINATED");
                break;
        }
    }

    public void ViewInventory() {
        try {
            queryMessage = "SELECT * FROM INVENTORY";
            Statement state = Connect().createStatement();
            ResultSet resSet = state.executeQuery(queryMessage);

            while (resSet.next()) {
                int ITEMID = resSet.getInt("ITEMID");
                String ITEMNAME = resSet.getString("ITEMNAME");
                double ITEMPRICE = resSet.getDouble("ITEMPRICE");
                int ITEMQUANTITY = resSet.getInt("ITEMQUANTITY");
                String ITEMVENDOR = resSet.getString("ITEMVENDOR");

                String Red = "\u001B[31m";
                String Green = "\u001B[32m";
                String Yellow = "\u001B[33m";
                String Reset = "\u001B[0m";
                String ITEMQUANTITYFORMAT = String.format("%d", ITEMQUANTITY);
                
                if (ITEMQUANTITY <= 5 || ITEMQUANTITY <= 9 ) {
                    ITEMQUANTITYFORMAT = Red + ITEMQUANTITY + Reset;
                } else if (ITEMQUANTITY >= 10 && ITEMQUANTITY <= 15) {
                    ITEMQUANTITYFORMAT = Yellow + ITEMQUANTITY + Reset;
                } else if (ITEMQUANTITY >= 20) {
                    ITEMQUANTITYFORMAT = Green + ITEMQUANTITY + Reset;
                }
                
                System.out.printf("ITEMID: %s%n" +
                                  "ITEMNAME: %s%n" +
                                  "ITEMPRICE: %.2f%n" +
                                  "ITEMQUANTITY: %s%n" +
                                  "ITEMVENDOR: %s%n", 
                                  ITEMID, ITEMNAME, ITEMPRICE, ITEMQUANTITYFORMAT, ITEMVENDOR);
                
                System.out.println();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void Buy() throws SQLException {
        ViewInventory();
        System.out.print("What item would you like to buy? -> ");
        int ID = scan.nextInt();
        System.out.print("How many would you like to buy? -> ");
        int SUBTRACT_VALUE = scan.nextInt();

        // Use PreparedStatement for the update
        String queryMessage = "UPDATE INVENTORY SET ITEMQUANTITY = ITEMQUANTITY - ? WHERE ITEMID = ?";
        try (PreparedStatement updateStmt = Connect().prepareStatement(queryMessage)) {
            updateStmt.setInt(1, SUBTRACT_VALUE);
            updateStmt.setInt(2, ID);
            int affectedRows = updateStmt.executeUpdate();

            if (affectedRows > 0) {
                String selectQuery = "SELECT ITEMID, ITEMNAME, ITEMPRICE, ITEMQUANTITY, ITEMVENDOR FROM INVENTORY WHERE ITEMID = ?";
                try (PreparedStatement selectStmt = Connect().prepareStatement(selectQuery)) {
                    selectStmt.setInt(1, ID);
                    ResultSet resSet = selectStmt.executeQuery();

                    while (resSet.next()) {
                        int ITEMID = resSet.getInt("ITEMID");
                        String ITEMNAME = resSet.getString("ITEMNAME");
                        double ITEMPRICE = resSet.getDouble("ITEMPRICE");
                        int ITEMQUANTITY = resSet.getInt("ITEMQUANTITY");
                        String ITEMVENDOR = resSet.getString("ITEMVENDOR");

                        String Red = "\u001B[31m";
                        String Green = "\u001B[32m";
                        String Yellow = "\u001B[33m";
                        String Reset = "\u001B[0m";
                        String ITEMQUANTITYFORMAT = String.format("%d", ITEMQUANTITY);
                        
                        if (ITEMQUANTITY <= 5) {
                            ITEMQUANTITYFORMAT = Red + ITEMQUANTITY + Reset;
                        } else if (ITEMQUANTITY >= 10 && ITEMQUANTITY <= 15) {
                            ITEMQUANTITYFORMAT = Yellow + ITEMQUANTITY + Reset;
                        } else if (ITEMQUANTITY >= 20) {
                            ITEMQUANTITYFORMAT = Green + ITEMQUANTITY + Reset;
                        }

                        System.out.printf("ITEMID: %s%n" +
                                          "ITEMNAME: %s%n" +
                                          "ITEMPRICE: %.2f%n" +
                                          "ITEMQUANTITY: %s%n" +
                                          "ITEMVENDOR: %s%n", 
                                          ITEMID, ITEMNAME, ITEMPRICE, ITEMQUANTITYFORMAT, ITEMVENDOR);
                    }
                }
                System.out.println("Item has been bought!");
                System.out.println();
            } else {
                System.out.println("No item was updated. Check if the ITEMID is valid and quantity is sufficient.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void Add() throws SQLException
    {
        String queryMessage = "UPDATE INVENTORY SET ITEMQUANTITY = ITEMQUANTITY + ? WHERE ITEMID = ?";
        ViewInventory();
        System.out.print("Which item would you like to add? (enter numbers only) -> ");
        int ID = scan.nextInt();
        System.out.print("How many would you like to add? -> ");
        int addedAmt = scan.nextInt();

        try
        {
            PreparedStatement updateStatement = Connect().prepareStatement(queryMessage);
            updateStatement.setInt(1, addedAmt);
            updateStatement.setInt(2, ID);
            int affectedRows = updateStatement.executeUpdate();

            if(affectedRows > 0)
            {
                String selectQuery = "SELECT ITEMID, ITEMNAME, ITEMPRICE, ITEMQUANTITY, ITEMVENDOR FROM INVENTORY WHERE ITEMID = ?";
                PreparedStatement selectStatement = Connect().prepareStatement(selectQuery);
                selectStatement.setInt(1, ID);
                ResultSet resSet = selectStatement.executeQuery();

                while(resSet.next())
                {
                    int ITEMID = resSet.getInt("ITEMID");
                    String ITEMNAME = resSet.getString("ITEMNAME");
                    double ITEMPRICE = resSet.getDouble("ITEMPRICE");
                    int ITEMQUANTITY = resSet.getInt("ITEMQUANTITY");
                    String ITEMVENDOR = resSet.getString("ITEMVENDOR");

                    String Red = "\u001B[31m";
                    String Green = "\u001B[32m";
                    String Yellow = "\u001B[33m";
                    String Reset = "\u001B[0m";
                    String ITEMQUANTITYFORMAT = String.format("%d", ITEMQUANTITY);
                    
                    if (ITEMQUANTITY <= 5 || ITEMQUANTITY <= 9 ) {
                        ITEMQUANTITYFORMAT = Red + ITEMQUANTITY + Reset;
                    } else if (ITEMQUANTITY >= 10 && ITEMQUANTITY <= 15) {
                        ITEMQUANTITYFORMAT = Yellow + ITEMQUANTITY + Reset;
                    } else if (ITEMQUANTITY >= 20) {
                        ITEMQUANTITYFORMAT = Green + ITEMQUANTITY + Reset;
                    }

                    System.out.printf("ITEMID: %s%n" +
                                      "ITEMNAME: %s%n" +
                                      "ITEMPRICE: %.2f%n" +
                                      "ITEMQUANTITY: %s%n" +
                                      "ITEMVENDOR: %s%n", 
                                      ITEMID, ITEMNAME, ITEMPRICE, ITEMQUANTITYFORMAT, ITEMVENDOR);
                }
                System.out.println("Item has been added!");
                System.out.println();
            }
            else
            {
                System.out.println("No item was updated. Check if the ITEMID is valid and quantity is sufficient.");
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }
}
