import dao.UserDAO;

public class TestUserDAO {

    public static void main(String[] args) {

        UserDAO userDAO = new UserDAO();

        boolean result = userDAO.deleteUser(6);

        if (result) {
            System.out.println("User Deleted Successfully");
        } else {
            System.out.println("User Delete Failed");
        }
    }
}