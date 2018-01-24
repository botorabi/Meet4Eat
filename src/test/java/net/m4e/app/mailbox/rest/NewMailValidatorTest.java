/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.mailbox.rest;

import net.m4e.app.mailbox.business.MailEntity;
import net.m4e.app.mailbox.rest.comm.NewMailCmd;
import net.m4e.app.resources.StatusEntity;
import net.m4e.app.user.business.UserEntity;
import net.m4e.app.user.business.Users;
import org.junit.jupiter.api.*;
import org.mockito.*;

import static net.m4e.tests.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NewMailValidatorTest {

    @Mock
    private Users users;

    private UserEntity user42;

    private UserEntity user84;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        user42 = new UserEntity();
        user42.setId(42L);
        user42.setName("Uncle Bob");
        user42.setStatus(new StatusEntity());

        user84 = new UserEntity();
        user84.setId(84L);
        user84.setName("Name");
        user84.setStatus(new StatusEntity());


        Mockito.when(users.findUser(42L)).thenReturn(user42);
        Mockito.when(users.findUser(84L)).thenReturn(user84);

    }


    @Test
    void cdiConstructable() {
        new NewMailValidator();
    }


    @Test
    void validNewMail() throws Exception {
        NewMailCmd newMailCmd = new NewMailCmd("Subject", "Content...", 84L);

        NewMailValidator newMailValidator = new NewMailValidator(users);


        MailEntity mailEntity = newMailValidator.validateNewEntityInput(newMailCmd, user42);


        assertThat(mailEntity.getContent()).isEqualTo("Content...");
        assertThat(mailEntity.getSubject()).isEqualTo("Subject");
        assertThat(mailEntity.getReceiverId()).isEqualTo(84L);
        assertThat(mailEntity.getReceiverName()).isEqualTo("Name");
        assertThat(mailEntity.getSenderId()).isEqualTo(42L);
        assertThat(mailEntity.getSenderName()).isEqualTo("Uncle Bob");

    }

    @Test
    void inactiveReceiver() {
        user84.getStatus().setEnabled(false);
        NewMailCmd newMailCmd = new NewMailCmd("Subject", "Content...", 84L);

        NewMailValidator newMailValidator = new NewMailValidator(users);


        assertThatThrownBy(() -> newMailValidator.validateNewEntityInput(newMailCmd, user42))
                .isInstanceOf(Exception.class)
                .hasMessage(("Failed to send mail, recipient does not exist."));
    }

    @Test
    void noReceiver() {
        NewMailCmd newMailCmd = new NewMailCmd("Subject", "Content...", 85L);

        NewMailValidator newMailValidator = new NewMailValidator(users);


        assertThatThrownBy(() -> newMailValidator.validateNewEntityInput(newMailCmd, user42))
                .isInstanceOf(Exception.class)
                .hasMessage(("Failed to send mail, recipient does not exist."));
    }

    @Test
    void invalidReceiver() {
        NewMailCmd newMailCmd = new NewMailCmd("Subject", "Content...", 0L);

        NewMailValidator newMailValidator = new NewMailValidator(users);


        assertThatThrownBy(() -> newMailValidator.validateNewEntityInput(newMailCmd, user42))
                .isInstanceOf(Exception.class)
                .hasMessage(("Failed to send mail, invalid recipient."));
    }

    @Test
    void shortSubject() {
        NewMailCmd newMailCmd = new NewMailCmd("", "Content...", 84L);

        NewMailValidator newMailValidator = new NewMailValidator(users);


        assertThatThrownBy(() -> newMailValidator.validateNewEntityInput(newMailCmd, user42))
                .isInstanceOf(Exception.class)
                .hasMessageContaining(("Mail subject must be at least"));
    }

    @Test
    void longSubject() {
        NewMailCmd newMailCmd = new NewMailCmd("This is a very, very long Subject", "Content...", 84L);
        Assumptions.assumeTrue(newMailCmd.getSubject().length() > 32);

        NewMailValidator newMailValidator = new NewMailValidator(users);


        assertThatThrownBy(() -> newMailValidator.validateNewEntityInput(newMailCmd, user42))
                .isInstanceOf(Exception.class)
                .hasMessageContaining(("Mail subject must be at least"));
    }

    @Test
    void nullMail() {
        NewMailValidator newMailValidator = new NewMailValidator(users);

        assertThatThrownBy(() -> newMailValidator.validateNewEntityInput(null, user42))
                .isInstanceOf(Exception.class)
                .hasMessage(("Failed to send mail, invalid input."));
    }

    @Test
    void invalidSender() {
        NewMailCmd newMailCmd = new NewMailCmd("Subject", "Content...", 84L);

        NewMailValidator newMailValidator = new NewMailValidator(users);
        assertThatThrownBy(() -> newMailValidator.validateNewEntityInput(newMailCmd, null))
                .isInstanceOf(Exception.class)
                .hasMessageContaining(("Failed to send mail, invalid sender."));
    }
}
