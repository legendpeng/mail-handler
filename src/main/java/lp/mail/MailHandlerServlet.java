package lp.mail;

// [START mail_handler_servlet]

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class MailHandlerServlet extends HttpServlet {

    private final static Logger logger = LoggerFactory.getLogger(MailHandlerServlet.class);
    private static final Properties props = new Properties();
    private final static String[] allowedExt = {".jpg", ".jpeg", ".png"};
    private final static CloudStorageHelper storage = new CloudStorageHelper();

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Session session = Session.getDefaultInstance(props, null);
        try {
            MimeMessage message = new MimeMessage(session, req.getInputStream());
            String from = message.getFrom()[0].toString();
            String subject = message.getSubject();
            String sentDate = message.getSentDate().toString();
            String contentType = message.getContentType();
            String messageContent = "";
            // store attachment file name, separated by comma
            String attachFiles = "";

            if (contentType.contains("multipart")) {
                // content may contain attachments
                Multipart multiPart = (Multipart) message.getContent();
                int numberOfParts = multiPart.getCount();
                for (int partCount = 0;
                     partCount < numberOfParts;
                     partCount++) {
                    MimeBodyPart part = (MimeBodyPart) multiPart.getBodyPart(partCount);
                    if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
                        // this part is attachment
                        String fileName = part.getFileName();
                        String fileContentType = part.getContentType();
                        checkFileExtension(fileName);
                        attachFiles += fileName + ", ";
                        storage.uploadFile(fileName, part.getInputStream(), "image/jpeg");
                    } else {
                        // this part may be the message content
                        messageContent = part.getContent().toString();
                    }
                }

                if (attachFiles.length() > 1) {
                    attachFiles = attachFiles.substring(0, attachFiles.length() - 2);
                }
            } else if (contentType.contains("text/plain") || contentType.contains("text/html")) {
                Object content = message.getContent();
                if (content != null) {
                    messageContent = content.toString();
                }
            }

            logger.info(String.format("Received mail message from: %s subject: %s date: %s contentType: %s attachFiles: %s messageContent: %s", from, subject,
                    sentDate, contentType, attachFiles, messageContent));
        } catch (MessagingException e) {
            logger.error("Received mail message but error occurred.", e);
            // ...
        } catch (Exception e) {
            logger.error("unexpected error", e);
        }
        // ...
    }

    /**
     * Checks that the file extension is supported.
     */
    private void checkFileExtension(String fileName) {
        if (fileName != null && !fileName.isEmpty() && fileName.contains(".")) {
            for (String ext : allowedExt) {
                if (fileName.endsWith(ext)) {
                    return;
                }
            }
            throw new IllegalArgumentException("file must be an image");
        }
    }

}
// [END mail_handler_servlet]