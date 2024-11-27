package simpleDatabase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class Otp {

    private Connection connection;
    private String otp;
    private String expiryTime;
    private String roles;

    public Otp(Connection connection) {
        this.connection = connection;
    }

    public Otp(String otp, String expiryTime, String roles) {
        this.otp = otp;
        this.expiryTime = expiryTime;
        this.roles = roles;
    }
    
    // generate OTP
    public String createOTP(String roles) {
		String otp = "";
		for (int i = 0; i < 6; i++) {
			otp += (int) (Math.random() * 10);
		}
		String expiryTime = LocalDateTime.now().plusMinutes(5).toString();
		try {
			insertOTP(otp, expiryTime, roles);
		} catch (SQLException e) {
			System.err.println("Database error while creating OTP: " + e.getMessage());
		}
		return otp;
	}


    // insert OTP
    public void insertOTP(String otp, String expiryTime, String roles) throws SQLException {
        String insertOTP = "INSERT INTO otpTable (otp, expiryTime, roles) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(insertOTP)) {
            pstmt.setString(1, otp);
            pstmt.setString(2, expiryTime);
            pstmt.setString(3, roles);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Database error while inserting OTP: " + e.getMessage());
        }
    }

    // verify OTP and and delete OTP -> return false if OTP not found or Make an OTP object and return it
    public Otp verifyOTP(String otp) {
        String selectOTP = "SELECT * FROM otpTable WHERE otp = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(selectOTP)) {
            pstmt.setString(1, otp);
            pstmt.execute();
            pstmt.getResultSet();
            return new Otp(otp, expiryTime, roles);
        } catch (SQLException e) {
            System.err.println("Database error while verifying OTP: " + e.getMessage());
        }
        return null;
    }

    // check if OTP is expired through expiry time
    public boolean isExpired(String expiryTime, String otpString) {
        Otp verifiedOtp = verifyOTP(otpString);
        LocalDateTime expiry = LocalDateTime.parse(verifiedOtp.expiryTime);
        return LocalDateTime.now().isAfter(expiry);
    }

}
