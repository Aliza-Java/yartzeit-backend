package com.aliza.shul.services;

import com.aliza.shul.DateUtils;
import com.aliza.shul.entities.Hdate;
import com.aliza.shul.entities.Yartzeit;
import com.aliza.shul.repositories.YartzeitRepository;
import com.aliza.shul.session.EmailSessionProvider;
import com.ibm.icu.util.HebrewCalendar;
import com.ibm.icu.util.ULocale;
import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.activation.FileDataSource;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service("emailService")
public class EmailService {

    @Autowired
    YartzeitRepository yartzeitRepository;

    @Autowired
    TemplateEngine templateEngine;

    @Autowired
    EmailSessionProvider sessionProvider;

    @Autowired
    DateUtils dateUtils;

    @Value("${email.days.ahead}")
    private int daysAhead; //how many days before a yartzeit should we send email

    @Value("${admin.1.email}")
    private String adminEmail1;

    @Value("${admin.2.email}")
    private String adminEmail2;


    @Scheduled(cron = "0 0 9 ? * SUN-THU", zone = "Asia/Jerusalem")
    public void sendReminderYartzeits() {
        List<Yartzeit> upcomingYts = findUpcomingYtz();
        for (Yartzeit y : upcomingYts) {
            //TODO - need validation that there is an email
            String subject = "Subject for yartzeit donation";
            String htmlMessage = buildEmailReminderYartzeit(y);
            MimeMessage mimeMessage = createMimeMessage(sessionProvider.getSession(), subject, htmlMessage, y.getMember().getEmail(), null, null, null);

            sendEmail(mimeMessage);//to member, that yartzeit is coming up
        }
    }

    public void letAdminKnow(Yartzeit yartzeit) {

        String subject = "Member proceeded to donate for yartzeit";
        String htmlMessage =  buildEmailAdminNotification(yartzeit);

        MimeMessage mimeMessage = createMimeMessage(sessionProvider.getSession(), subject, htmlMessage, adminEmail1, null, null, null);

        sendEmail(mimeMessage);//to admin, that he clicked
        System.out.println("Admin notified");
    }

    public boolean sendEmail(MimeMessage message) {
        try {
            Transport.send(message);

            Address[] toRecipients = message.getRecipients(Message.RecipientType.TO);
            // int toAmount = (toRecipients != null) ? toRecipients.length : 0;

            Address[] ccRecipients = message.getRecipients(Message.RecipientType.CC);
            // int ccAmount = (ccRecipients != null) ? ccRecipients.length : 0;

            Address[] bccRecipients = message.getRecipients(Message.RecipientType.BCC);
            // int bccAmount = (bccRecipients != null) ? bccRecipients.length : 0;

            Address[] emptyArray = new Address[0];

            List<Address> allRecipients = Stream
                    .of((toRecipients == null ? emptyArray : toRecipients),
                            ccRecipients == null ? emptyArray : ccRecipients,
                            bccRecipients == null ? emptyArray : bccRecipients)
                    .flatMap(Arrays::stream).distinct().collect(Collectors.toList());

            String additionalMessage = "";
            if (allRecipients.size() > 1)
                additionalMessage = String.format("(and %d more) ", allRecipients.size() - 1);

            System.out.println(String.format("Email to %s %s sent successfully!", allRecipients.get(0).toString(),
                    additionalMessage));

        } catch (MessagingException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to send email");
        }

        return true;
    }

    public MimeMessage createMimeMessage(Session session, String subject, String htmlContent, String to, List<String> bccList, File attachment, String attachmentName) {

        // Create the email message
        MimeMessage message = new MimeMessage(session);
        try {
            //message.setFrom(new InternetAddress("elcmembersonline@gmail.com", "ELC Members Online")); //*TODO in future - put this in env file
            message.setFrom(new InternetAddress("davening.list@gmail.com", "ELC Members Online")); //TODO - get elcmembers email to work and replace
            message.setRecipients(Message.RecipientType.TO, to);
            if (bccList != null) {
                bccList.forEach(bcc -> {
                    try {
                        message.addRecipient(Message.RecipientType.BCC, new InternetAddress(bcc));
                    } catch (MessagingException e) {
                        System.out.println("Failed to send to " + bcc + " as bcc");
                    }
                });
            }
            message.setSubject(subject);

            Multipart multipart = getMultipart(htmlContent, attachment, attachmentName);

            message.setContent(multipart);

        } catch (MessagingException | IOException e) {
            throw new RuntimeException("Failed to create MIME message", e);
        }

        return message;
    }

    private static Multipart getMultipart(String htmlContent, File attachment, String attachmentName) throws MessagingException, IOException {
        Multipart multipart = new MimeMultipart();

        MimeBodyPart textPart = new MimeBodyPart();
        textPart.setContent(htmlContent, "text/html; charset=UTF-8");
        multipart.addBodyPart(textPart);

        MimeBodyPart imagePart = new MimeBodyPart();
        DataSource fds = new FileDataSource("src/main/resources/static/logo.jpeg"); // adjust path
        imagePart.setDataHandler(new DataHandler(fds));
        imagePart.setHeader("Content-ID", "<logoImage>");   // match 'cid:logoImage'
        imagePart.setDisposition(MimeBodyPart.INLINE);
        multipart.addBodyPart(imagePart);

        if (attachment != null) {
            MimeBodyPart attachmentPart = new MimeBodyPart();
            attachmentPart.attachFile(attachment);
            attachmentPart.setFileName(attachmentName);
            multipart.addBodyPart(attachmentPart); // the attachment
        }
        return multipart;
    }

    //todo - can make these smarter and unified
    private String buildEmailReminderYartzeit(Yartzeit y) {
        Context context = new Context();

        context.setVariable("name", y.getMember().getFirstName());
        context.setVariable("relationship", y.getRelationship());
        context.setVariable("fullname", y.getName());
        context.setVariable("date", buildUpcomingDateString(y.getDate()));

        return templateEngine.process("email-before-yt", context);
    }

    private String buildEmailAdminNotification(Yartzeit y) {
        Context context = new Context();

        context.setVariable("firstname", y.getMember().getFirstName());
        context.setVariable("lastname", y.getMember().getLastName());
        context.setVariable("relationship", y.getRelationship());

        return templateEngine.process("notify-admin-donation-clicked", context);
    }
    private List<Yartzeit> findUpcomingYtz() {

        Map<Integer, Set<Integer>> upcomingDates = new HashMap<>();
        HebrewCalendar cal = new HebrewCalendar();
        boolean isLeapYear = cal.isLeapYear(cal.get(HebrewCalendar.YEAR)); //TODO - find alternative for deprecated isLeapYear(int)

        System.out.println(cal.get(HebrewCalendar.YEAR));

        HebrewCalendar dayToSend = new HebrewCalendar(new ULocale("iw_IL")); //start with today

        System.out.println(dayToSend);

        dayToSend.add(HebrewCalendar.DAY_OF_MONTH, daysAhead);
        considerDate(upcomingDates, dayToSend);
        System.out.println(upcomingDates);

        //find day of week - on thursday we need to get also next 2 dates
        DayOfWeek dow = LocalDate.now().getDayOfWeek();
        if (DayOfWeek.THURSDAY.equals(dow)) //take care of Friday and Shabbat
        {
            dayToSend.add(HebrewCalendar.DAY_OF_MONTH, 1);
            considerDate(upcomingDates, dayToSend);
            dayToSend.add(HebrewCalendar.DAY_OF_MONTH, 1);
            considerDate(upcomingDates, dayToSend);
        }

        int hebrewYear = dateUtils.getHebrewDate(LocalDate.now()).get(Calendar.YEAR);
        System.out.println(hebrewYear);

        List<Yartzeit> allYartzeits = yartzeitRepository.findAll();
        List<Yartzeit> relevantYartzeits = allYartzeits.stream()
                .filter(y -> {
                    //convert each yartzeit date to this year's dates, i.e. which day (and month) would we remember it?
                    Integer monthNum = DateUtils.hebrewMonthToInt(y.getDate().getMonth(), isLeapYear);
                    System.out.println(monthNum);
                    return upcomingDates.containsKey(monthNum) &&
                            upcomingDates.get(monthNum).contains(y.getDate().getDay());
                })
                .peek(y -> {
                    Hdate date = y.getDate();
                    date.setEngDate(dateUtils.convertHebrewToEnglish(y.getDate().getMonth(), y.getDate().getDay(), isLeapYear, hebrewYear));
                })
                .collect(Collectors.toList());

        System.out.println(relevantYartzeits);
        System.out.println(relevantYartzeits.get(0).getMember().getEmail());
        System.out.println(relevantYartzeits.get(1).getMember().getEmail());
        return relevantYartzeits;
    }

    private void considerDate(Map<Integer, Set<Integer>> upcomingDates, HebrewCalendar dateToBe) {

        int day = dateToBe.get(HebrewCalendar.DATE);
        int month = dateToBe.get(HebrewCalendar.MONTH); // 1-based

        if (day != 30) //on 29th we do both 29 and 30
            upcomingDates
                    .computeIfAbsent(month, k -> new HashSet<>())
                    .add(day);

        if (day == 29) //even if no 30, cover all by putting and searching
            upcomingDates
                    .computeIfAbsent(month, k -> new HashSet<>())
                    .add(30);
    }

    private String buildUpcomingDateString(Hdate date) {
        //Sunday 27th July 2025 / 2nd Av  5785
        int hebrewYear = dateUtils.getHebrewDate(date.getEngDate()).get(Calendar.YEAR);

        return date.getEngDate().getDayOfWeek() + " " +
                DateUtils.formatEnglishWithOrdinal(date.getEngDate()) +
                " / " +
                DateUtils.formatHebrewWithOrdinal(hebrewYear, date.getMonth(), date.getDay());
    }
}
