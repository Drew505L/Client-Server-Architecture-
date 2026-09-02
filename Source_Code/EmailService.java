package ClientServerArchitureProject;


/**
 * Create gmail account: username = <<username>>@gmail.com
 password = <<password>>
 Manage your google account (from initial icon, upper right)
 Security section
 Turn on 2 factor authentication
 In the search box, search for (and go to) App passwords
 Generate password wwww xxxx yyyy zzzz
 Remove spaces, copy and paste into the password string of this class (replace wwwwxxxxyyyyzzzz)
 Replace <<username>> in username string of this class with your username (from above)

 Run the program
 The sent email may end up in a junk/spam folder of the recipient
 */

//-- Download JavaMail API from here: https://javaee.github.io/javamail/
//-- Download JavaBeans Activation Framework from here: http://www.oracle.com/technetwork/java/javasebusiness/downloads/java-archive-downloads-java-plat-419418.html#jaf-1.1.1-fcs-oth-JPR
import java.sql.SQLException;
import java.util.Properties;
import java.util.Scanner;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class EmailService {
    // -- set the gmail host URL
    final static private String host = "smtp.gmail.com";

    // -- You must have a valid gmail username/password pair to use gmail as a SMTP service
    static private String senderUsername = "fiendnic60";
    static private String password = "vtny qgzc lfdm cuqj "; //jayden - vtny qgzc lfdm cuqj  nico - xvkk ftxg qrax cxbk

    private DBOperations dbc = new DBOperations();

    public static void main(String[] args) {
        EmailService es = new EmailService();
        try{es.recoverPass("Jayden");} catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public void recoverPass(String username) throws SQLException {
        String pass = dbc.getPassword(username);
        String messagetext = "Here is your password: " + pass + ". DON'T FORGET IT THIS TIME";
        String to = dbc.getEmail(username);

        // -- set up host properties
        //    refer to https://javaee.github.io/javamail/docs/api/com/sun/mail/smtp/package-summary.html for additional properties
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.ssl.enable", "true"); // -- to use port 465, the SSL port
        props.put("mail.smtp.port", "465");        // -- TLS port is 587);

        // -- Get the Session object.
        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(senderUsername, password);
                    }
                });

        // -- Set up the sender's email account information
        String from = senderUsername + "@gmail.com";

        try {
            // -- Create a default MimeMessage object.
            Message message = new MimeMessage(session);

            // -- Set From: header field of the header.
            message.setFrom(new InternetAddress(from));

            // -- Set To: header field of the header.
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));

            // -- Set Subject: header field
            message.setSubject("Password Recovery Email");

            // Now set the actual message
            message.setText(messagetext);

            // -- Send message
            // -- use either these three lines...
            // Transport t = session.getTransport("smtp");
            // t.connect();
            // t.sendMessage(message, message.getAllRecipients());

            // -- ...or this one (which ultimately calls sendMessage(...)
            Transport.send(message);

            System.out.println("Sent message successfully....");
            dbc.resetLoginAttempts(username);

        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }


    }
}
