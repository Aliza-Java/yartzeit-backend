package com.aliza.shul.rest;

import com.aliza.shul.services.EmailService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;


@Controller
@RequestMapping("/email")
public class EmailWebService {

    @Autowired
    EmailService emailService;

    @GetMapping("/notify-donation")
    public void donationClicked(@RequestParam String target, @RequestParam Long yid, HttpServletResponse response) throws IOException {
        emailService.notifyAdminThatDonated(yid); //TODO: in future, make enum of admin notifications
        //TODO: put target straight from backend, cut out front-end
        response.sendRedirect(target);
    }

    @GetMapping("/please-contact")
    public String pleaseCallClicked(@RequestParam Long yid, Model model) throws IOException {
        emailService.notifyAdminToContact(yid);
        return "thank-you-we-will-contact";  //show static thymeleaf template
    }
}
