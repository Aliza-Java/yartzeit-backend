package com.aliza.shul.rest;

import com.aliza.shul.entities.Yartzeit;
import com.aliza.shul.services.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@Controller("/email")
public class EmailWebService {

    @Autowired
    EmailService emailService;

    @PostMapping("/notify")
    public void notifyAdmin(@RequestBody Yartzeit yartzeit) {
        emailService.letAdminKnow(yartzeit);
    }
}
