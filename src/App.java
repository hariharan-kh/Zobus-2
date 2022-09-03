import java.util.Scanner;
import java.sql.*;

public class App {
    static Scanner sc = new Scanner(System.in);
    static Connection con;
    static int currentUserId;

    public static int validate(boolean isAdmin) throws Exception {
        String query = "select * from credentials where username = ? and password = ? and adminprivilege = ?;";
        int id = -1;
        System.out.print("Enter your UserName : ");
        String username = sc.nextLine();
        System.out.print("Enter your Password : ");
        String password = sc.nextLine();
        PreparedStatement st = con.prepareStatement(query);
        st.setString(1, username);
        st.setString(2, password);
        st.setInt(3, isAdmin ? 1 : 0);
        ResultSet res = st.executeQuery();
        while (res.next()) {
            id = res.getInt(1);
        }
        currentUserId = id;
        return id;
    }

    public static void register(boolean isAdmin) throws Exception {
        String getId = "select max(id) from credentials where adminprivilege = 0;";
        String insertQuery = "insert into credentials (id,username,password,adminprivilege,wallet) values (?,?,?,?,0);";
        ResultSet res = con.prepareStatement(getId).executeQuery();
        res.next();
        int id = 0;
        id = res.getInt(1);
        System.out.print("Enter your UserName : ");
        String username = sc.nextLine();
        System.out.print("Enter your Password : ");
        String password = sc.nextLine();
        PreparedStatement st = con.prepareStatement(insertQuery);
        st.setInt(1, id + 1);
        st.setString(2, username);
        st.setString(3, password);
        st.setInt(4, isAdmin ? 1 : 0);
        st.executeUpdate();
        System.out.println("Registered Successfully ! ");
    }

    public static void createBus() throws Exception {
        String getId = "select max(id) from bus;";
        String insertQuery = "insert into bus (id,type,fare,seats,collectedfare) values (?,?,?,?,0);";
        String insertSeatQuery = "insert into seats (id,name,gender,age,cancelled,avail,bus,seat) value (?,null,null,null,0,1,?,?);";
        System.out.println("Enter Bus Preferance : ");
        System.out.println("1-AC Sleeper");
        System.out.println("2-AC Seater");
        System.out.println("3-NonAC Sleeper");
        System.out.println("4-NonAC Seater");
        int id = 1;
        int pref = sc.nextInt();
        sc.nextLine();
        if (pref > 0 && pref <= 4) {
            System.out.println("Enter fare amount : ");
            int fare = sc.nextInt();
            if (fare > 0) {
                System.out.println("Enter number of seats : ");
                int num = sc.nextInt();
                if (num > 0) {
                    PreparedStatement st = con.prepareStatement(getId);
                    ResultSet res = st.executeQuery();
                    while (res.next()) {
                        id = res.getInt(1) + 1;
                    }
                    PreparedStatement st1 = con.prepareStatement(insertQuery);
                    st1.setInt(1, id);
                    st1.setInt(2, pref);
                    st1.setInt(3, fare);
                    st1.setInt(4, num);
                    st1.executeUpdate();
                    for (int i = 1; i <=num; i++) {
                        PreparedStatement st2 = con.prepareStatement(insertSeatQuery);
                        st2.setString(1, seatIdGenerator(id, i, pref));
                        st2.setInt(2, id);
                        st2.setString(3, seatNoGenerator(pref, i));
                        st2.executeUpdate();
                    }
                }
            } else {
                System.out.println("Enter a valid fare...");
            }
        } else {
            System.out.println("Enter a valid preference...");
        }

    }

    public static String seatIdGenerator(int bus, int rawSeat, int pref) {
        String seatId = "";
        if (pref % 2 == 0) {
            seatId = bus + "" + (char) ((rawSeat % 4 == 0 ? rawSeat / 4 : (rawSeat / 4) + 1) + 64) + "" + (rawSeat%4==0?4:rawSeat%4);
        } else {
            seatId =bus + "" + (char)((rawSeat%6==0?rawSeat/6:(rawSeat/6)+1)+((rawSeat%6==3||rawSeat%6==4||rawSeat%6==0)?64:96))+""+((rawSeat%6==1 || rawSeat%6==3)?1:(rawSeat%6==2 || rawSeat%6==4)?2:3);
        }
        return seatId;
    }

    public static String seatNoGenerator(int pref, int rawSeat) {
        String seatId = "";
        if (pref % 2 == 0) {
            seatId = (char) ((rawSeat % 4 == 0 ? rawSeat / 4 : (rawSeat / 4) + 1) + 64) + "" + ((rawSeat % 4)+1);
        } else {
            seatId = (char)((rawSeat%6==0?rawSeat/6:(rawSeat/6)+1)+((rawSeat%6==3||rawSeat%6==4||rawSeat%6==0)?64:95))+""+((rawSeat%6==1 || rawSeat%6==3)?1:(rawSeat%6==2 || rawSeat%6==4)?2:3);
        }
        return seatId;
    }

    public static void main(String[] args) throws Exception {
        con = DriverManager.getConnection("jdbc:mysql://localhost:3306/zobus2", "root", "19cs046H");
        boolean totalExit = false;
        while (!totalExit) {
            System.out.println("1-User");
            System.out.println("2-Admin");
            System.out.println("3-Exit");
            System.out.println("Enter your Choice : ");
            int totalChoice = -1;
            try {
                totalChoice = sc.nextInt();
            } catch (Exception e) {
            }
            sc.nextLine();
            switch (totalChoice) {
                case 1:
                    boolean loginSignUp = false;
                    while (!loginSignUp) {
                        System.out.println("1-Sign In");
                        System.out.println("2-Sign Up");
                        System.out.println("3-Exit");
                        System.out.println("Enter your choice : ");
                        int onBoardChoice = -1;
                        try {
                            onBoardChoice = sc.nextInt();
                        } catch (Exception e) {
                        }
                        sc.nextLine();
                        switch (onBoardChoice) {
                            case 1:
                                if (validate(false) != -1) {
                                    boolean userPanelExit = false;
                                    while (!userPanelExit) {
                                        System.out.println("1-Book Tickets");
                                        System.out.println("2-Cancel Tickets");
                                        System.out.println("3-View Tickets");
                                        System.out.println("4-Show Availability");
                                        System.out.println("5-View Balance");
                                        System.out.println("6-Add Balance");
                                        System.out.println("7-Exit");
                                        int userChoice = -1;
                                        try {
                                            userChoice = sc.nextInt();
                                        } catch (Exception e) {
                                        }
                                        sc.nextLine();
                                        switch (userChoice) {
                                            case 1:
                                                break;
                                            case 2:
                                                break;
                                            case 3:
                                                break;
                                            case 4:
                                                break;
                                            case 5:
                                                break;
                                            case 6:
                                                break;
                                            case 7:
                                                userPanelExit = true;
                                                break;
                                            default:
                                                System.out.println(
                                                        "\u001B[31m" + "Enter a valid option..." + "\u001B[0m");
                                        }
                                    }
                                }
                                break;
                            case 2:
                                register(false);
                                break;
                            case 3:
                                loginSignUp = true;
                                break;
                            default:
                                System.out.println("Enter a valid option...");
                        }
                    }

                    break;
                case 2:
                    if (validate(true) != -1) {
                        boolean adminExit = false;
                        while (!adminExit) {
                            System.out.println("1-Create Bus");
                            System.out.println("2-Remove Bus");
                            System.out.println("3-Bus Summary");
                            System.out.println("4-View All Tickets");
                            System.out.println("5-View Bus");
                            System.out.println("6-Show Fare");
                            System.out.println("7-Exit");
                            int adminChoice = -1;
                            try {
                                adminChoice = sc.nextInt();
                            } catch (Exception e) {
                            }
                            sc.nextLine();
                            switch (adminChoice) {
                                case 1:
                                    createBus();
                                    break;
                                case 2:
                                    break;
                                case 3:
                                    break;
                                case 4:
                                    break;
                                case 7:
                                    adminExit = true;
                                    break;
                                default:
                                    System.out.println("Enter a valid option...");
                            }
                        }
                    }
                    break;
                case 3:
                    totalExit = true;
                    break;
                default:
                    System.out.println("Enter a valid option....");
            }
        }
    }
}
