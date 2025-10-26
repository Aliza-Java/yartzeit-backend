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
//TODO - really both urls can be unified and attach different middle
    @Value("${url.when.donating}")
    private String urlWhenDonating;

    @Value("${url.when.requesting.contact}")
    private String urlWhenRequestingContact;

    private Map<String, List<Integer>> chagim = Map.of(
            "Tishrei", List.of(1, 2, 10, 15, 22),
            "Nissan", List.of(15, 21),
            "Sivan", List.of(6)
    );
    @Scheduled(cron = "0 0 9 ? * SUN-FRI", zone = "Asia/Jerusalem")
    public void sendReminderYartzeits() {

        //on Chagim don't run (days before will cover chagim also)


        List<Yartzeit> upcomingYts = findUpcomingYtz();
        for (Yartzeit y : upcomingYts) {
            //TODO - need validation that there is an email
            String subject = "Subject for yartzeit donation";
            String htmlMessage = buildEmailReminderYartzeit(y);
            MimeMessage mimeMessage = createMimeMessage(sessionProvider.getSession(), subject, htmlMessage, y.getMember().getEmail(), null, null, null);

            sendEmail(mimeMessage);//to member, that yartzeit is coming up
        }
    }

    public void notifyAdminThatDonated(Long yartzeitId) {

        Optional<Yartzeit> optionalYartzeit = yartzeitRepository.findById(yartzeitId);
        if (optionalYartzeit.isEmpty())
            System.out.println("yartzeit with id " + yartzeitId + " not found"); //TODO send email, make here as error.
        else {
        String subject = "Member proceeded to donate for yartzeit";
        String htmlMessage = buildEmailAdminNotification(optionalYartzeit.get());

        MimeMessage mimeMessage = createMimeMessage(sessionProvider.getSession(), subject, htmlMessage, adminEmail1, null, null, null);

        sendEmail(mimeMessage);//to admin, that member clicked on donate
        System.out.println(adminEmail1 + " notified");
        }
    }

    //TODO - unite with above method using enum (duplicate code)
    public void notifyAdminToContact(Long yartzeitId) {

        Optional<Yartzeit> optionalYartzeit = yartzeitRepository.findById(yartzeitId);
        if (optionalYartzeit.isEmpty())
            System.out.println("yartzeit with id " + yartzeitId + " not found"); //TODO send email, make here as error.
        else {
            String subject = "Please contact " + optionalYartzeit.get().getMember().getFirstName() + " for donation instructions";
            String htmlMessage = buildEmailAdminPleaseContact(optionalYartzeit.get());

            MimeMessage mimeMessage = createMimeMessage(sessionProvider.getSession(), subject, htmlMessage, adminEmail1, null, null, null);

            sendEmail(mimeMessage);//to admin, that member clicked on donate
            System.out.println(adminEmail1 + " notified to contact");
        }
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
            message.setFrom(new InternetAddress("elcmembersonline@gmail.com", "ELC Members Online"));
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
        String customUrlDonate = String.format(urlWhenDonating, y.getId()); //insert yartzeit id into url
        context.setVariable("urlwhendonating", customUrlDonate);
        String customUrlPleaseContact = String.format(urlWhenRequestingContact, y.getId()); //insert yartzeit id into url
        context.setVariable("urlwhenrequestingcontact", customUrlPleaseContact);


        return templateEngine.process("email-before-yt", context);
    }

    private String buildEmailAdminNotification(Yartzeit y) {
        Context context = new Context();

        context.setVariable("firstname", y.getMember().getFirstName());
        context.setVariable("lastname", y.getMember().getLastName());
        context.setVariable("relationship", y.getRelationship());

        return templateEngine.process("notify-admin-donation-clicked", context);
    }

    private String buildEmailAdminPleaseContact(Yartzeit y){
        Context context = new Context();

        context.setVariable("firstname", y.getMember().getFirstName());
        context.setVariable("lastname", y.getMember().getLastName());
        context.setVariable("relationship", y.getRelationship());
        context.setVariable("day", y.getDate().getDay());
        context.setVariable("month", y.getDate().getMonth());
        context.setVariable("email", y.getMember().getEmail());
        context.setVariable("phone", y.getMember().getPhone());

        return templateEngine.process("notify-admin-to-contact-member", context);
    }

    private List<Yartzeit> findUpcomingYtz() {

        Map<Integer, Set<Integer>> upcomingDates = new HashMap<>();
        HebrewCalendar cal = new HebrewCalendar();
        boolean isLeapYear = cal.isLeapYear(cal.get(HebrewCalendar.YEAR)); //TODO - find alternative for deprecated isLeapYear(int)

        //on chagim - don't send
        int day = cal.get(HebrewCalendar.DATE);
        int monthNum = cal.get(HebrewCalendar.MONTH);
        String monthName = DateUtils.hebrewMonthToString(monthNum, isLeapYear);

        if (chagim.containsKey(monthName) && chagim.get(monthName).contains(day)) {
            return List.of(); //don't run method today
        }
        //TODO: need to test and check with edge cases (RH on thursday+friday)
        HebrewCalendar dayToSend = new HebrewCalendar(new ULocale("iw_IL")); //start with today

        dayToSend.add(HebrewCalendar.DAY_OF_MONTH, daysAhead);
        considerDate(upcomingDates, dayToSend); //check if upcoming date (x days ahead) can be added

        boolean sentTomorrow = false; //to avoid moving day forward ALSO for chag and ALSO for Friday

        if("ELUL".equals(monthName) && day == 29) { //both days of RH
            dayToSend.add(HebrewCalendar.DAY_OF_MONTH, 1);
            considerDate(upcomingDates, dayToSend);
            dayToSend.add(HebrewCalendar.DAY_OF_MONTH, 1);
            considerDate(upcomingDates, dayToSend);
            sentTomorrow = true;
        }

        else if (chagim.containsKey(monthName) && chagim.get(monthName).contains(day)) {
            dayToSend.add(HebrewCalendar.DAY_OF_MONTH, 1);
            considerDate(upcomingDates, dayToSend);
            sentTomorrow = true;
        }

        //find day of week - //on Friday send also for Shabbat
        DayOfWeek dow = LocalDate.now().getDayOfWeek();
        if (DayOfWeek.FRIDAY.equals(dow) && !sentTomorrow)
        {
            dayToSend.add(HebrewCalendar.DAY_OF_MONTH, 1);
            considerDate(upcomingDates, dayToSend);
        }

        int hebrewYear = dateUtils.getHebrewDate(LocalDate.now()).get(Calendar.YEAR);

        List<Yartzeit> allYartzeits = yartzeitRepository.findAll();
        List<Yartzeit> relevantYartzeits = allYartzeits.stream()
                .filter(y -> {
                    //convert each yartzeit date to this year's dates, i.e. which day (and month) would we remember it?
                    Integer monthNumber = DateUtils.hebrewMonthToInt(y.getDate().getMonth(), isLeapYear);
                    return upcomingDates.containsKey(monthNumber) &&
                            upcomingDates.get(monthNumber).contains(y.getDate().getDay());
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
