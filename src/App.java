import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;

import java.sql.*;

public class App {
    static Scanner sc = new Scanner(System.in);
    static Connection con;
    static int currentUserId;
    static String[] pref = { "AC Sleeper", "AC Seater", "Non AC Sleeper", "Non AC Seater" };

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
                    st1.setInt(2, pref - 1);
                    st1.setInt(3, fare);
                    st1.setInt(4, num);
                    st1.executeUpdate();
                    for (int i = 1; i <= num; i++) {
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
            seatId = bus + "" + (char) ((rawSeat % 4 == 0 ? rawSeat / 4 : (rawSeat / 4) + 1) + 64) + ""
                    + (rawSeat % 4 == 0 ? 4 : rawSeat % 4);
        } else {
            seatId = bus + "" + (char) ((rawSeat % 6 == 0 ? rawSeat / 6 : (rawSeat / 6) + 1) + 64) + ""
                    + ((rawSeat % 6 == 1 || rawSeat % 6 == 3) ? 1 : (rawSeat % 6 == 2 || rawSeat % 6 == 4) ? 2 : 3) + ""
                    + ((rawSeat % 6 == 3 || rawSeat % 6 == 4 || rawSeat % 6 == 0) ? "U" : "L");
        }
        return seatId;
    }

    public static String seatNoGenerator(int pref, int rawSeat) {
        String seatId = "";
        if (pref % 2 == 0) {
            seatId = (char) ((rawSeat % 4 == 0 ? rawSeat / 4 : (rawSeat / 4) + 1) + 64) + ""
                    + (rawSeat % 4 == 0 ? 4 : rawSeat % 4);
        } else {
            seatId = (char) ((rawSeat % 6 == 0 ? rawSeat / 6 : (rawSeat / 6) + 1) + 64) + ""
                    + ((rawSeat % 6 == 1 || rawSeat % 6 == 3) ? 1 : (rawSeat % 6 == 2 || rawSeat % 6 == 4) ? 2 : 3) + ""
                    + ((rawSeat % 6 == 3 || rawSeat % 6 == 4 || rawSeat % 6 == 0) ? "U" : "L");
        }
        return seatId;
    }

    public static void bookTickets() throws Exception {
        String viewBusQuery = "Select * from bus;";
        String viewAvailQuery = "Select count(*) from seats where bus = ? and avail=1";
        ResultSet res = con.prepareStatement(viewBusQuery).executeQuery();
        int numberOfBus = 0;
        ArrayList<Integer> busId = new ArrayList<>();
        ArrayList<Integer> busPref = new ArrayList<>();
        while (res.next()) {
            PreparedStatement st = con.prepareStatement(viewAvailQuery);
            st.setInt(1, res.getInt(1));
            ResultSet avail = st.executeQuery();
            avail.next();
            System.out.println("Choice : " + (numberOfBus + 1) + " Bus Id : " + res.getInt(1) + " Bus type : "
                    + pref[res.getInt(2)] + " Available seat(s) : " + avail.getInt(1));
            busId.add(res.getInt(1));
            busPref.add(res.getInt(2));
            numberOfBus += 1;
        }
        int busChoice = 0;
        try {
            busChoice = sc.nextInt();
        } catch (Exception e) {
        }
        if (busChoice > 0 && busChoice <= numberOfBus) {
            ArrayList<ArrayList<String>> seatInfoList = printSeats(busId.get(busChoice - 1),
                    busPref.get(busChoice - 1));
            System.out.println("Enter number of seats to book : ");
            int noSeats = sc.nextInt();
            sc.nextLine();
            ArrayList<String> chosenSeats = new ArrayList<>();
            ArrayList<String> chosenSeatsGender = new ArrayList<>();
            ArrayList<String> chosenSeatsName = new ArrayList<>();
            ArrayList<Integer> chosenSeatsAge = new ArrayList<>();
            HashSet<String> chosenseatSet = new HashSet<>();
            for (int i = 0; i < noSeats; i++) {
                int chance = 0;
                String seat;
                String name;
                String gender;
                int age;
                do {
                    System.out.println("Enter your " + (i + 1) + " th selection");
                    if (chance > 0) {
                        System.out.println("Last Chance");
                    }
                    System.out.println("Enter seat number : ");
                    seat = sc.nextLine();
                    System.out.println("Enter name : ");
                    name = sc.nextLine();
                    System.out.println("Enter gender : ");
                    gender = sc.nextLine();
                    System.out.println("Enter age : ");
                    age = sc.nextInt();
                    sc.nextLine();
                } while (!isseatvalid(seatInfoList, seat, gender, busPref.get(busChoice - 1)) && chance < 2);
                if (isseatvalid(seatInfoList, seat, gender, busPref.get(busChoice - 1))) {
                    chosenSeats.add(seat);
                    chosenSeatsGender.add(gender);
                    chosenSeatsName.add(name);
                    chosenSeatsAge.add(age);
                    chosenseatSet.add(seat);
                }
            }
            if (chosenSeats.size() == chosenseatSet.size() && chosenSeats.size() == noSeats) {
                String findFare = "select fare from bus where id = ?;";
                PreparedStatement st = con.prepareStatement(findFare);
                st.setInt(1, busId.get(busChoice - 1));
                ResultSet x = st.executeQuery();
                x.next();
                System.out.println("Tickets selected are : " + chosenSeats.toString());
                System.out.println("Total Amount to be paid : " + x.getInt(1) * noSeats);
                System.out.println("Do you like to continue ? (y/n) ");
                String confirmation = sc.nextLine();
                if (confirmation.equalsIgnoreCase("y")) {
                    int idTicket = 0;
                    String getId = "select max(id) from tickets;";
                    String getBal = "select wallet from credentials where id = ?;";
                    String insertTicket = "insert into tickets(id,seats,bookedby,bus,fare) values(?,?,?,?,?);";
                    String updateSeats = "update seats set name=?,age=?,gender=?,avail=0,ticket=? where id = ?;";
                    String updateBal = "update credentials set wallet = wallet - ? where id = ?";
                    String updateBus = "update bus set collectedfare = collectedfare + ? where id = ?;";
                    ResultSet getid = con.prepareStatement(getId).executeQuery();
                    while (getid.next()) {
                        idTicket = getid.getInt(1) + 1;
                    }
                    double bal = 0;
                    PreparedStatement getbal = con.prepareStatement(getBal);
                    getbal.setInt(1, currentUserId);
                    ResultSet ball = getbal.executeQuery();
                    while (ball.next()) {
                        bal = ball.getDouble(1);
                    }
                    if (bal >= x.getInt(1) * noSeats) {
                        PreparedStatement ticketinsert = con.prepareStatement(insertTicket);
                        ticketinsert.setInt(1, idTicket);
                        ticketinsert.setString(2, chosenSeats.toString());
                        ticketinsert.setInt(3, currentUserId);
                        ticketinsert.setInt(4, busId.get(busChoice - 1));
                        ticketinsert.setDouble(5, x.getInt(1) * noSeats);
                        ticketinsert.executeUpdate();
                        for (int i = 0; i < noSeats; i++) {
                            PreparedStatement seatsupdate = con.prepareStatement(updateSeats);
                            seatsupdate.setString(1, chosenSeatsName.get(i));
                            seatsupdate.setInt(2, chosenSeatsAge.get(i));
                            seatsupdate.setString(3, chosenSeatsGender.get(i));
                            seatsupdate.setInt(4, idTicket);
                            seatsupdate.setString(5, busId.get(busChoice - 1) + chosenSeats.get(i));
                            seatsupdate.executeUpdate();
                        }
                        PreparedStatement balupdate = con.prepareStatement(updateBal);
                        balupdate.setDouble(1, x.getInt(1) * noSeats);
                        balupdate.setInt(2, currentUserId);
                        balupdate.executeUpdate();
                        PreparedStatement busupdate = con.prepareStatement(updateBus);
                        busupdate.setDouble(1, x.getInt(1) * noSeats);
                        busupdate.setInt(2, busId.get(busChoice - 1));
                        busupdate.executeUpdate();
                    } else {
                        System.out.println("Insufficient Balance!");
                    }
                } else if (confirmation.equalsIgnoreCase("n")) {
                    System.out.println("See you next time");
                } else {
                    System.out.println("Invalid option");
                }
            }
        } else {
            System.out.println("Enter a valid choice....");
        }
    }

    public static boolean isseatvalid(ArrayList<ArrayList<String>> seatsinfo, String seat, String gender, int pref) {
        boolean isGender = true;
        boolean isAseat = false;
        boolean isSeatAvail = true;
        boolean isNeighAvail = true;
        if (!gender.equalsIgnoreCase("m") && !gender.equalsIgnoreCase("f")) {
            isGender = false;
        }
        for (int i = 0; i < seatsinfo.get(0).size(); i++) {
            if (seatsinfo.get(0).get(i).equalsIgnoreCase(seat)) {
                isAseat = true;
                if (seatsinfo.get(1).get(i) != null) {
                    isSeatAvail = false;
                }
            }

        }
        if (pref % 2 == 0) {
            for (int i = 0; i < seatsinfo.get(0).size(); i++) {
                if ((seat.charAt(0) + "").equalsIgnoreCase(seatsinfo.get(0).get(i).charAt(0) + "") && isset(
                        Integer.parseInt(seat.substring(1, seat.length() - 1)),
                        Integer.parseInt(seatsinfo.get(0).get(i).substring(1, seatsinfo.get(0).get(i).length() - 1)))) {
                    if ((seat.charAt(seat.length() - 1) + "").equalsIgnoreCase(seatsinfo.get(0).get(i)
                            .charAt(seatsinfo.get(0).get(i).length() - 1) + "")) {
                        if (seatsinfo.get(1).get(i) != null && !seatsinfo.get(1).get(i).equalsIgnoreCase(gender)) {
                            isNeighAvail = false;
                        }
                    }
                }
            }
        } else {
            for (int i = 0; i < seatsinfo.get(0).size(); i++) {
                if ((seat.charAt(0) + "").equalsIgnoreCase(seatsinfo.get(0).get(i).charAt(0) + "")
                        && round(Integer.parseInt(seat.substring(1))) == round(
                                Integer.parseInt(seatsinfo.get(0).get(i).substring(1)))) {

                    if (seatsinfo.get(1).get(i) != null && !seatsinfo.get(1).get(i).equalsIgnoreCase(gender)) {
                        isNeighAvail = false;
                    }
                }
            }
        }
        return isAseat && isSeatAvail && isNeighAvail && isGender;
    }

    public static int round(int i) {
        return i % 2 == 0 ? i / 2 : (i / 2) + 1;
    }

    public static boolean isset(int a, int b) {
        boolean res = false;
        if ((a % 3 == 0 ? a / 3 : (a / 3 + 1)) == (a % 3 == 0 ? b / 3 : (b / 3 + 1))) {
            if (a % 3 == 1 && b % 3 == 2 || a % 3 == 2 && b % 3 == 1) {
                res = true;
            }
        }
        return res;
    }

    public static void cancelTickets() throws Exception {
        ArrayList<Integer> busesBooked = new ArrayList<>();
        ArrayList<Integer> ticketsBooked = new ArrayList<>();
        ArrayList<Integer> seatCount = new ArrayList<>();
        String showTickets = "select * from tickets where bookedby = ?;";
        PreparedStatement showtickets = con.prepareStatement(showTickets);
        showtickets.setInt(1, currentUserId);
        ResultSet res = showtickets.executeQuery();
        int totaltickets = 0;
        if (res.next()) {
            do {
                busesBooked.add(res.getInt(4));
                ticketsBooked.add(res.getInt(1));
                String showPassengers = "select * from seats where ticket = ?;";
                PreparedStatement showpassengers = con.prepareStatement(showPassengers);
                showpassengers.setInt(1, res.getInt(1));
                ResultSet pass = showpassengers.executeQuery();
                System.out.println("Ticket Choice number : " + (totaltickets + 1));
                System.out.println("Ticket ID : " + res.getInt(1) + " Bus ID : " + res.getInt(4));
                int count = 0;
                while (pass.next()) {
                    count += 1;
                    System.out.println("Seat No. : " + pass.getString(8) + " " + pass.getString(2) + " - "
                            + pass.getString(3) + "(" + pass.getInt(4) + ")");
                }
                seatCount.add(count);
                System.out.println("-------------------------------------");
                System.out.println("Total Fare : " + res.getDouble(5));
                System.out.println("-------------------------------------");
                totaltickets += 1;
            } while (res.next());

            System.out.println("Enter your choice of ticket cancellation : ");
            int ticketChoice = sc.nextInt();
            ticketChoice -= 1;
            sc.nextLine();
            if (ticketChoice >= 0 && ticketChoice < totaltickets) {
                System.out.println("1-Cancel Ticket");
                System.out.println("2-Cancel Seats");
                int cancellationchoice = sc.nextInt();
                sc.nextLine();
                if (cancellationchoice == 1) {
                    String ticketFare = "select type,fare from bus where id = ?;";
                    String ticketCancel = "delete from tickets where id=?;";
                    String updateSeat = "update seats set name=null,age=null,gender=null,avail=1,cancelled=cancelled+1,ticket=null where ticket = ?;";
                    String updateBal = "update credentials set wallet = wallet+? where id = ?;";
                    String updateFare = "update bus set collectedfare = collectedfare - ? where id = ?;";
                    PreparedStatement getfare = con.prepareStatement(ticketFare);
                    getfare.setInt(1, busesBooked.get(ticketChoice));
                    ResultSet fare = getfare.executeQuery();
                    fare.next();
                    System.out.println("Amount refunded : "
                            + fare.getInt(2) * seatCount.get(ticketChoice) / (fare.getInt(1) < 2 ? 2 : (4 / 3)));
                    System.out.println("Do you want to continue? (y/n)");
                    String confirmation = sc.nextLine();
                    if (confirmation.equalsIgnoreCase("y")) {
                        PreparedStatement cancelticket = con.prepareStatement(ticketCancel);
                        cancelticket.setInt(1, ticketsBooked.get(ticketChoice));
                        cancelticket.executeUpdate();
                        PreparedStatement seatsupdate = con.prepareStatement(updateSeat);
                        seatsupdate.setInt(1, ticketsBooked.get(ticketChoice));
                        seatsupdate.executeUpdate();
                        PreparedStatement balupdate = con.prepareStatement(updateBal);
                        balupdate.setInt(2, currentUserId);
                        balupdate.setDouble(1,
                                fare.getDouble(2) * seatCount.get(ticketChoice) / (fare.getInt(1) < 2 ? 2 : (4 / 3)));
                        balupdate.executeUpdate();
                        PreparedStatement colfare = con.prepareStatement(updateFare);
                        colfare.setDouble(1,
                                fare.getDouble(2) * seatCount.get(ticketChoice) / (fare.getInt(1) < 2 ? 2 : 4));
                        colfare.setInt(2, busesBooked.get(ticketChoice));
                        colfare.executeUpdate();
                    } else if (confirmation.equalsIgnoreCase("n")) {
                        System.out.println("You made a good choice");
                    } else {
                        System.out.println("Invalid option");
                    }
                } else if (cancellationchoice == 2) {
                    ArrayList<String> ticketSeats = new ArrayList<>();
                    ArrayList<Integer> cancelSeats = new ArrayList<>();
                    String getSeats = "select * from seats where ticket=?;";
                    PreparedStatement getseat = con.prepareStatement(getSeats);
                    getseat.setInt(1, ticketsBooked.get(ticketChoice));
                    ResultSet seatsOfTicket = getseat.executeQuery();
                    int totalSeats = 0;
                    while (seatsOfTicket.next()) {
                        totalSeats += 1;
                        ticketSeats.add(seatsOfTicket.getString(1));
                    }
                    for (int i = 0; i < ticketSeats.size(); i++) {
                        System.out.println((i + 1) + " - " + ticketSeats.get(i));
                    }
                    System.out.println("Enter number of seats to cancel : ");
                    int toCancel = sc.nextInt();
                    sc.nextLine();
                    if (toCancel > 0 && toCancel < seatCount.get(ticketChoice)) {
                        for (int i = 0; i < toCancel; i++) {
                            System.out.println("Enter seat Choice : ");
                            int seatchoice = sc.nextInt();
                            if (seatchoice <= totalSeats && seatchoice > 0) {
                                cancelSeats.add(seatchoice - 1);
                            }
                        }
                        ArrayList<String> ticketsToCancel = new ArrayList<>();
                        if (cancelSeats.size() == toCancel) {
                            String ticketFare = "select type,fare from bus where id = ?;";
                            PreparedStatement getfare = con.prepareStatement(ticketFare);
                            getfare.setInt(1, busesBooked.get(ticketChoice));
                            ResultSet fare = getfare.executeQuery();
                            fare.next();
                            System.out.println("Amount refunded : "
                                    + fare.getInt(2) * toCancel / (fare.getInt(1) < 2 ? 2 : (4 / 3)));
                            sc.nextLine();
                            System.out.println("Do you wish to continue? (y/n)");
                            String confirmation = sc.nextLine();
                            if (confirmation.equalsIgnoreCase("y")) {
                                String updateSeat = "update seats set name=null,age=null,gender=null,avail=1,cancelled=cancelled+1,ticket = null where id = ?;";
                                System.out.println(ticketSeats);
                                for (int i = 0; i < cancelSeats.size(); i++) {
                                    PreparedStatement seatupdate = con.prepareStatement(updateSeat);
                                    seatupdate.setString(1, ticketSeats.get(cancelSeats.get(i)));
                                    seatupdate.executeUpdate();
                                    ticketsToCancel.add(ticketSeats.get(cancelSeats.get(i)));
                                }
                                String updateTicket = "update tickets set seats=? where id = ?;";
                                PreparedStatement ticketupdate = con.prepareStatement(updateTicket);
                                ticketupdate.setString(1, ticketsToCancel.toString());
                                ticketupdate.setInt(2, ticketsBooked.get(ticketChoice));
                                ticketupdate.executeUpdate();
                                String updateBal = "update credentials set wallet = wallet+? where id = ?;";
                                String updateFare = "update bus set collectedfare = collectedfare - ? where id = ?;";
                                PreparedStatement balupdate = con.prepareStatement(updateBal);
                                balupdate.setInt(2, currentUserId);
                                balupdate.setDouble(1,
                                        fare.getDouble(2) * ticketsToCancel.size()
                                                / (fare.getInt(1) < 2 ? 2 : (4 / 3)));
                                balupdate.executeUpdate();
                                PreparedStatement colfare = con.prepareStatement(updateFare);
                                colfare.setDouble(1,
                                        fare.getDouble(2) * ticketsToCancel.size() / (fare.getInt(1) < 2 ? 2 : 4));
                                colfare.setInt(2, busesBooked.get(ticketChoice));
                                colfare.executeUpdate();
                            } else if (confirmation.equalsIgnoreCase("n")) {
                                System.out.println("You made a right choice ");
                            } else {
                                System.out.println("Invalid option");
                            }
                        }
                    } else if (toCancel == seatCount.get(ticketChoice)) {
                        String ticketFare = "select type,fare from bus where id = ?;";
                        String ticketCancel = "delete from tickets where id=?;";
                        String updateSeat = "update seats set name=null,age=null,gender=null,avail=1,cancelled=cancelled+1,ticket=null where ticket = ?;";
                        String updateBal = "update credentials set wallet = wallet+? where id = ?;";
                        String updateFare = "update bus set collectedfare = collectedfare - ? where id = ?;";
                        PreparedStatement getfare = con.prepareStatement(ticketFare);
                        getfare.setInt(1, busesBooked.get(ticketChoice));
                        ResultSet fare = getfare.executeQuery();
                        fare.next();
                        System.out.println("Amount refunded : "
                                + fare.getInt(2) * seatCount.get(ticketChoice) / (fare.getInt(1) < 2 ? 2 : (4 / 3)));
                        System.out.println("Do you want to continue? (y/n)");
                        String confirmation = sc.nextLine();
                        if (confirmation.equalsIgnoreCase("y")) {
                            PreparedStatement cancelticket = con.prepareStatement(ticketCancel);
                            cancelticket.setInt(1, ticketsBooked.get(ticketChoice));
                            cancelticket.executeUpdate();
                            PreparedStatement seatsupdate = con.prepareStatement(updateSeat);
                            seatsupdate.setInt(1, ticketsBooked.get(ticketChoice));
                            seatsupdate.executeUpdate();
                            PreparedStatement balupdate = con.prepareStatement(updateBal);
                            balupdate.setInt(2, currentUserId);
                            balupdate.setDouble(1,
                                    fare.getDouble(2) * seatCount.get(ticketChoice)
                                            / (fare.getInt(1) < 2 ? 2 : (4 / 3)));
                            balupdate.executeUpdate();
                            PreparedStatement colfare = con.prepareStatement(updateFare);
                            colfare.setDouble(1,
                                    fare.getDouble(2) * seatCount.get(ticketChoice) / (fare.getInt(1) < 2 ? 2 : 4));
                            colfare.setInt(2, busesBooked.get(ticketChoice));
                            colfare.executeUpdate();
                        } else if (confirmation.equalsIgnoreCase("n")) {
                            System.out.println("You made a good choice");
                        } else {
                            System.out.println("Invalid option");
                        }
                    } else {
                        System.out.println("Enter valid number of seats...");
                    }
                } else {
                    System.out.println("Enter a valid option...");
                }
            }
        } else {
            System.out.println("Nothing ton cancel");
        }
    }

    public static ArrayList<ArrayList<String>> printSeats(int bus, int pref) throws Exception {
        ArrayList<ArrayList<String>> result = new ArrayList<>();
        ArrayList<String> nameList = new ArrayList<>();
        ArrayList<String> genderList = new ArrayList<>();
        ArrayList<Integer> ageList = new ArrayList<>();
        ArrayList<String> seatList = new ArrayList<>();
        String viewQuery = "select name,gender,age,seat from seats where bus = ?;";
        PreparedStatement st = con.prepareStatement(viewQuery);
        st.setInt(1, bus);
        ResultSet res = st.executeQuery();
        if (pref % 2 == 1) {
            int iter = 1;
            String row1 = "";
            String row2 = "";
            String row3 = "";
            String row4 = "";
            while (res.next()) {
                nameList.add(res.getString(1));
                genderList.add(res.getString(2));
                ageList.add(res.getInt(3));
                seatList.add(res.getString(4));
                if (iter % 4 == 1) {
                    if (res.getString(2) != null) {
                        row1 = row1 + "| " + "seat : " + res.getString(4) + " " + res.getString(1) + " - "
                                + res.getString(2) + " (" + res.getInt(3) + ") " + " |";
                    } else {
                        row1 = row1 + "| " + "seat : " + res.getString(4) + " " + "Available |";
                    }
                } else if (iter % 4 == 2) {
                    if (res.getString(2) != null) {
                        row2 = row2 + "| " + "seat : " + res.getString(4) + " " + res.getString(1) + " - "
                                + res.getString(2) + " (" + res.getInt(3) + ") " + " |";
                    } else {
                        row2 = row2 + "|" + "seat : " + res.getString(4) + " " + " Available |";
                    }
                } else if (iter % 4 == 3) {
                    if (res.getString(2) != null) {
                        row3 = row3 + "| " + "seat : " + res.getString(4) + " " + res.getString(1) + " - "
                                + res.getString(2) + " (" + res.getInt(3) + ") " + " |";
                    } else {
                        row3 = row3 + "| " + "seat : " + res.getString(4) + " " + "Available |";
                    }
                } else if (iter % 4 == 0) {
                    if (res.getString(2) != null) {
                        row4 = row4 + "| " + "seat : " + res.getString(4) + " " + res.getString(1) + " - "
                                + res.getString(2) + " (" + res.getInt(3) + ") " + " |";
                    } else {
                        row4 = row4 + "|" + "seat : " + res.getString(4) + " " + " Available |";
                    }
                }
                iter += 1;
            }
            System.out.println(row1);
            System.out.println(row2);
            System.out.println();
            System.out.println(row3);
            System.out.println(row4);
        } else {
            String u1 = "";
            String u2 = "";
            String u3 = "";
            String l1 = "";
            String l2 = "";
            String l3 = "";
            int iter = 1;
            while (res.next()) {
                nameList.add(res.getString(1));
                genderList.add(res.getString(2));
                ageList.add(res.getInt(3));
                seatList.add(res.getString(4));
                if (res.getString(4).charAt(res.getString(4).length() - 1) == 'U') {
                    if (Integer.parseInt(res.getString(4).substring(1, res.getString(4).length() - 1)) % 3 == 1) {
                        if (res.getString(2) != null) {
                            u1 = u1 + "| (" + res.getString(4) + ") seat : " + res.getString(4) + " " + res.getString(1)
                                    + " - "
                                    + res.getString(2) + " (" + res.getInt(3) + ") " + " |";
                        } else {
                            u1 = u1 + "| " + "seat : " + res.getString(4) + " " + "Available " + res.getString(4) + "|";
                        }
                    } else if (Integer.parseInt(res.getString(4).substring(1, res.getString(4).length() - 1))
                            % 3 == 2) {
                        if (res.getString(2) != null) {
                            u2 = u2 + "| (" + res.getString(4) + ") seat : " + res.getString(4) + " " + res.getString(1)
                                    + " - "
                                    + res.getString(2) + " (" + res.getInt(3) + ") " + " |";
                        } else {
                            u2 = u2 + "| " + "seat : " + res.getString(4) + " " + "Available " + res.getString(4)
                                    + " |";
                        }
                    } else if (Integer.parseInt(res.getString(4).substring(1, res.getString(4).length() - 1))
                            % 3 == 0) {
                        if (res.getString(2) != null) {
                            u3 = u3 + "| (" + res.getString(4) + ") seat : " + res.getString(4) + " " + res.getString(1)
                                    + " - "
                                    + res.getString(2) + " (" + res.getInt(3) + ") " + " |";
                        } else {
                            u3 = u3 + "| " + "seat : " + res.getString(4) + " " + "Available " + res.getString(4) + "|";
                        }
                    }
                } else {
                    if (Integer.parseInt(res.getString(4).substring(1, res.getString(4).length() - 1)) % 3 == 1) {
                        if (res.getString(2) != null) {
                            l1 = l1 + "| (" + res.getString(4) + ") seat : " + res.getString(4) + " " + res.getString(1)
                                    + " - "
                                    + res.getString(2) + " (" + res.getInt(3) + ") " + " |";
                        } else {
                            l1 = l1 + "| " + "seat : " + res.getString(4) + " " + "Available " + res.getString(4) + "|";
                        }
                    } else if (Integer.parseInt(res.getString(4).substring(1, res.getString(4).length() - 1))
                            % 3 == 2) {
                        if (res.getString(2) != null) {
                            l2 = l2 + "| (" + res.getString(4) + ") seat : " + res.getString(4) + " " + res.getString(1)
                                    + " - "
                                    + res.getString(2) + " (" + res.getInt(3) + ") " + " |";
                        } else {
                            l2 = l2 + "| " + "seat : " + res.getString(4) + " " + "Available " + res.getString(4) + "|";
                        }
                    } else if (Integer.parseInt(res.getString(4).substring(1, res.getString(4).length() - 1))
                            % 3 == 0) {
                        if (res.getString(2) != null) {
                            l3 = l3 + "| (" + res.getString(4) + ") seat : " + res.getString(4) + " " + res.getString(1)
                                    + " - "
                                    + res.getString(2) + " (" + res.getInt(3) + ") " + " |";
                        } else {
                            l3 = l3 + "| " + "seat : " + res.getString(4) + " " + "Available " + res.getString(4) + "|";
                        }
                    }
                }
                iter += 1;
            }
            System.out.println(l1);
            System.out.println(l2);
            System.out.println();
            System.out.println(l3);
            System.out.println();
            System.out.println();
            System.out.println(u1);
            System.out.println(u2);
            System.out.println();
            System.out.println(u3);
            System.out.println();
            System.out.println();

        }
        result.add(seatList);
        result.add(genderList);
        return result;
    }

    public static void printTickets() throws Exception {
        String showTickets = "select * from tickets;";
        PreparedStatement showtickets = con.prepareStatement(showTickets);
        ResultSet res = showtickets.executeQuery();
        int totaltickets = 0;
        if (res.next()) {
            do {
                String showPassengers = "select * from seats where ticket = ?;";
                PreparedStatement showpassengers = con.prepareStatement(showPassengers);
                showpassengers.setInt(1, res.getInt(1));
                ResultSet pass = showpassengers.executeQuery();
                System.out.println("Ticket Choice number : " + (totaltickets + 1));
                System.out.println("Ticket ID : " + res.getInt(1) + " Bus ID : " + res.getInt(4));
                int count = 0;
                while (pass.next()) {
                    count += 1;
                    System.out.println("Seat No. : " + pass.getString(8) + " " + pass.getString(2) + " - "
                            + pass.getString(3) + "(" + pass.getInt(4) + ")");
                }
                System.out.println("-------------------------------------");
                System.out.println("Total Fare : " + res.getDouble(5));
                System.out.println("-------------------------------------");
                totaltickets += 1;
            } while (res.next());
        } else {
            System.out.println("No tickets were booked");
        }
    }

    public static void viewBus() throws Exception {
        String viewBusQuery = "Select * from bus;";
        String viewAvailQuery = "Select count(*) from seats where bus = ? and avail=1";
        ResultSet res = con.prepareStatement(viewBusQuery).executeQuery();
        int ch = 0;
        if (res.next()) {
            do {
                PreparedStatement st = con.prepareStatement(viewAvailQuery);
                st.setInt(1, res.getInt(1));
                ResultSet avail = st.executeQuery();
                avail.next();
                System.out.println("Choice : " +(ch+1)+ " Bus Id : " + res.getInt(1) + " Bus type : "
                        + pref[res.getInt(2)] + " Available seat(s) : " + avail.getInt(1));
                printSeats(res.getInt(1), res.getInt(2));
                ch+=1;
            } while (res.next());
        } else {
            System.out.println("No buses to display");
        }
    }

    public static void viewMyTickets() throws Exception {
        String showTickets = "select * from tickets where bookedby = ?;";
        PreparedStatement showtickets = con.prepareStatement(showTickets);
        showtickets.setInt(1, currentUserId);
        ResultSet res = showtickets.executeQuery();
        int totaltickets = 0;
        if (res.next()) {
            do {
                String showPassengers = "select * from seats where ticket = ?;";
                PreparedStatement showpassengers = con.prepareStatement(showPassengers);
                showpassengers.setInt(1, res.getInt(1));
                ResultSet pass = showpassengers.executeQuery();
                System.out.println("Ticket Choice number : " + (totaltickets + 1));
                System.out.println("Ticket ID : " + res.getInt(1) + " Bus ID : " + res.getInt(4));
                int count = 0;
                while (pass.next()) {
                    count += 1;
                    System.out.println("Seat No. : " + pass.getString(8) + " " + pass.getString(2) + " - "
                            + pass.getString(3) + "(" + pass.getInt(4) + ")");
                }
                System.out.println("-------------------------------------");
                System.out.println("Total Fare : " + res.getDouble(5));
                System.out.println("-------------------------------------");
                totaltickets += 1;
            } while (res.next());
        } else {
            System.out.println("No tickets were booked");
        }
    }

    public static void addBalance() throws Exception {
        System.out.println("Enter amount to add : ");
        double x = sc.nextDouble();
        String setBal = "update credentials set wallet = wallet + ? where id = ?;";
        PreparedStatement st = con.prepareStatement(setBal);
        st.setDouble(1, x);
        st.setInt(2, currentUserId);
        st.executeUpdate();
    }

    public static void viewBalance() throws Exception {
        String bal = "select wallet from credentials where id = ?;";
        PreparedStatement st = con.prepareStatement(bal);
        st.setInt(1, currentUserId);
        ResultSet res = st.executeQuery();
        res.next();
        System.out.println(res.getDouble(1));
    }

    public static void deleteBus() throws Exception {
        ResultSet showBus = con.prepareStatement("select * from bus;").executeQuery();
        ArrayList<Integer> busId = new ArrayList<>();
        ArrayList<Integer> seats = new ArrayList<>();
        if (showBus.next()) {
            int ch = 0;
            do {
                System.out.println("Choice : " + (ch + 1) + " Bus Id : " + showBus.getInt(1) + " Bus type : "
                        + showBus.getInt(2) + " Fare per seat : " + showBus.getDouble(3));
                System.out.println("Total Seats : " + showBus.getInt(4) + "Fare Collected" + showBus.getDouble(5));
                System.out.println();
                busId.add(showBus.getInt(1));
                seats.add(showBus.getInt(4));
                ch += 1;
            } while (showBus.next());
            System.out.println("Enter your choice : ");
            int busToCancel = sc.nextInt();
            sc.nextLine();
            if (busToCancel > 0 && busToCancel <= ch) {
                String isBooked = "select count(*) from seats where avail=1 and bus = ?;";
                PreparedStatement isbooked = con.prepareStatement(isBooked);
                isbooked.setInt(1, busId.get(busToCancel - 1));
                ResultSet avail = isbooked.executeQuery();
                avail.next();
                if (avail.getInt(1) == seats.get(busToCancel - 1)) {
                    String delBus = "delete from bus where id = ?;";
                    String delSeats = "delete from seats where bus = ?;";
                    PreparedStatement busdel = con.prepareStatement(delBus);
                    busdel.setInt(1, busId.get(busToCancel - 1));
                    busdel.executeUpdate();
                    PreparedStatement seatdel = con.prepareStatement(delSeats);
                    seatdel.setInt(1, busId.get(busToCancel - 1));
                    seatdel.executeUpdate();
                    System.out.println("Bus Deleted");
                } else {
                    System.out.println("Tickets already booked, you cannot delete the bus");
                }
            } else {
                System.out.println("Invalid Choice");
            }

        } else {
            System.out.println("No more buses to delete");
        }
    }

    public static void showAvailability() throws Exception{
        ArrayList<Integer> busId = new ArrayList<>();
        ArrayList<Integer> type = new ArrayList<>();
        ArrayList<Integer> avail = new ArrayList<>();
        String viewBus = "select id,type from bus;";
        ResultSet busSet = con.prepareStatement(viewBus).executeQuery();
        if(busSet.next()){
            do{
                String getAvail = "select count(*) from seats where bus = ?;";
                PreparedStatement availSt = con.prepareStatement(getAvail);
                availSt.setInt(1, busSet.getInt(1));
                ResultSet availset = availSt.executeQuery();
                availset.next();
                busId.add(busSet.getInt(1));
                type.add(busSet.getInt(2));
                avail.add(availset.getInt(1));
            }while(busSet.next());

            for(int i=0;i<busId.size();i++){
                for(int j=i+1;j<busId.size();j++){
                    if(avail.get(i)<avail.get(j)){
                        int tempid = busId.get(i);
                        busId.set(i, busId.get(j));
                        busId.set(j, tempid);
                        int temppref = type.get(i);
                        type.set(i, type.get(j));
                        type.set(j, temppref);
                        int tempavail = avail.get(i);
                        avail.set(i, avail.get(j));
                        avail.set(j, tempavail);
                    }
                    if(avail.get(i)==avail.get(j)){
                        if(type.get(i)>type.get(j)){
                            int tempid = busId.get(i);
                        busId.set(i, busId.get(j));
                        busId.set(j, tempid);
                        int temppref = type.get(i);
                        type.set(i, type.get(j));
                        type.set(j, temppref);
                        int tempavail = avail.get(i);
                        avail.set(i, avail.get(j));
                        avail.set(j, tempavail);
                        }
                    }
                }
            }
            for(int i=0;i<busId.size();i++){
                if(avail.get(i)>0){
                    System.out.println("Bus ID : "+busId.get(i)+" Bus Type : "+pref[type.get(i)]+" seats available =  "+avail.get(i));
                }
            }
        }else{
            System.out.println("No buses exist");
        }
    }

    public static void busSummary() throws Exception{
        String getBus = "select id,collectedfare from bus;";
        String getAvail = "select count(*) from seats where bus = ? and avail = 0;";
        String getCancelled = "select sum(cancelled) from seats where bus = ?;";
        ResultSet busSet = con.prepareStatement(getBus).executeQuery();
        if(busSet.next()){
            do{
                PreparedStatement availst = con.prepareStatement(getAvail);
                availst.setInt(1, busSet.getInt(1));
                ResultSet av = availst.executeQuery();
                PreparedStatement cancelst = con.prepareStatement(getCancelled);
                cancelst.setInt(1, busSet.getInt(1));
                ResultSet cn = cancelst.executeQuery();
                av.next();
                cn.next();
                System.out.println("Bus Id : "+busSet.getInt(1));
                System.out.println(av.getInt(1)+" available + "+cn.getInt(1)+" cancelled");
                System.out.println("Total Fare collected : " + busSet.getDouble(2));
            }while(busSet.next());
        }else{
            System.out.println("No buses available");
        }
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
                                                bookTickets();
                                                break;
                                            case 2:
                                                cancelTickets();
                                                break;
                                            case 3:
                                                viewMyTickets();
                                                break;
                                            case 4:showAvailability();
                                                break;
                                            case 5:
                                                viewBalance();
                                                break;
                                            case 6:
                                                addBalance();
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
                            System.out.println("6-Exit");
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
                                    deleteBus();
                                    break;
                                case 3:busSummary();
                                    break;
                                case 4:
                                    printTickets();
                                    break;
                                case 5:
                                    viewBus();
                                    break;
                                case 6:
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
