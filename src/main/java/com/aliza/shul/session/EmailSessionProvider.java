package com.aliza.shul.session;

import jakarta.mail.Session;

public interface EmailSessionProvider {
    Session getSession();
}