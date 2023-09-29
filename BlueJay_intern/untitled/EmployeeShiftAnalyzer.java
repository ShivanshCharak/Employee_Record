import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

class EmployeeShift {
    private String positionId;
    private String positionStatus;
    private Date timeIn;
    private Date timeOut;
    private String employeeName;

    public EmployeeShift(String positionId, String positionStatus, Date timeIn, Date timeOut, String employeeName) {
        this.positionId = positionId;
        this.positionStatus = positionStatus;
        this.timeIn = timeIn;
        this.timeOut = timeOut;
        this.employeeName = employeeName;
    }

    public Date getTimeIn() {
        return timeIn;
    }

    public Date getTimeOut() {
        return timeOut;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public double getHoursWorked() {
        long diffMillis = timeOut.getTime() - timeIn.getTime();
        return diffMillis / (60.0 * 60.0 * 1000); // Convert milliseconds to hours
    }

    public boolean isWithin1To10Hours(EmployeeShift otherShift) {
        double hoursBetweenShifts = (otherShift.getTimeIn().getTime() - timeOut.getTime()) / (60.0 * 60.0 * 1000);
        return hoursBetweenShifts > 1 && hoursBetweenShifts < 10;
    }
}

public class EmployeeShiftAnalyzer {
    public static void main(String[] args) {
        String filePath = "employee_shifts.csv"; // Replace with the path to your CSV file

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            reader.readLine(); // Skip the header line

            List<EmployeeShift> shifts = new ArrayList<>();
            EmployeeShift currentShift = null;

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                String positionId = parts[0];
                String positionStatus = parts[1];
                String timeInStr = parts[2];
                String timeOutStr = parts[3];
                String employeeName = parts[7];

                SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm a");
                Date timeIn = null;
                Date timeOut = null;

                if (!timeInStr.isEmpty() && !timeOutStr.isEmpty()) {
                    try {
                        timeIn = dateFormat.parse(timeInStr);
                        timeOut = dateFormat.parse(timeOutStr);
                    } catch (ParseException e) {
                        // Handle the parsing exception
                        e.printStackTrace();
                    }
                }

                if (timeIn != null && timeOut != null) {
                    EmployeeShift shift = new EmployeeShift(positionId, positionStatus, timeIn, timeOut, employeeName);

                    if (currentShift == null || !employeeName.equals(currentShift.getEmployeeName())) {
                        analyzeEmployeeShifts(shifts);
                        shifts.clear();
                    }

                    shifts.add(shift);
                    currentShift = shift;
                }
            }

            analyzeEmployeeShifts(shifts); // Analyze the last employee's shifts

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void analyzeEmployeeShifts(List<EmployeeShift> shifts) {
        if (shifts.isEmpty()) {
            return;
        }

        EmployeeShift firstShift = shifts.get(0);
        double totalHoursWorked = 0;
        boolean consecutiveDaysWorked = true;

        for (int i = 0; i < shifts.size() - 1; i++) {
            totalHoursWorked += shifts.get(i).getHoursWorked();

            // Check for consecutive days worked
            long dayDifference = (shifts.get(i + 1).getTimeIn().getTime() - shifts.get(i).getTimeIn().getTime()) / (24 * 60 * 60 * 1000);
            if (dayDifference != 1) {
                consecutiveDaysWorked = false;
            }

            // Check for less than 10 hours between shifts
            if (!shifts.get(i).isWithin1To10Hours(shifts.get(i + 1))) {
                System.out.println(shifts.get(i).getEmployeeName() + " worked less than 10 hours between shifts on " + shifts.get(i).getTimeOut());
            }
        }

        totalHoursWorked += shifts.get(shifts.size() - 1).getHoursWorked();

        // Check for consecutive days worked
        if (consecutiveDaysWorked) {
            System.out.println(shifts.get(0).getEmployeeName() + " has worked for 7 consecutive days.");
        }

        // Check for more than 14 hours in a single shift
        if (totalHoursWorked > 14) {
            System.out.println(shifts.get(0).getEmployeeName() + " has worked for more than 14 hours in a single shift.");
        }

        System.out.println(shifts.get(0).getEmployeeName() + " total hours worked: " + totalHoursWorked);
        System.out.println();
    }
}
