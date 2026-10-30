package com.dawn.engagement.service;

import com.dawn.common.event.OrderCreatedEvent;
import com.dawn.engagement.entity.Notification;
import com.dawn.engagement.infra.persistence.JpaNotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationPushServiceTest {

    @Mock
    JpaNotificationRepository notificationRepo;
    @Mock
    SimpMessagingTemplate messaging;

    NotificationPushService service;

    @BeforeEach
    void setUp() {
        service = new NotificationPushService(notificationRepo, messaging);
    }

    @Test
    @DisplayName("Guest (userId null): không push, không đụng DB")
    void guest_skips() {
        service.onOrderCreated(new OrderCreatedEvent(10L, null));

        verifyNoInteractions(notificationRepo, messaging);
    }

    @Test
    @DisplayName("Member: lưu ORDER_CREATED + đẩy WS đúng topic user")
    void member_savesAndPushes() {
        when(notificationRepo.save(any(Notification.class)))
                .thenAnswer(i -> i.getArgument(0));

        service.onOrderCreated(new OrderCreatedEvent(101L, 2L));

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepo).save(captor.capture());
        assertThat(captor.getValue().getUserId()).isEqualTo(2L);
        assertThat(captor.getValue().getType()).isEqualTo("ORDER_CREATED");
        assertThat(captor.getValue().getReferenceId()).isEqualTo(101L);
        verify(messaging).convertAndSend(eq("/topic/notifications/2"), any(Notification.class));
    }

    @Test
    @DisplayName("WS rớt: DB vẫn đã lưu, không throw ra ngoài")
    void messagingFailure_stillSaved() {
        when(notificationRepo.save(any(Notification.class)))
                .thenAnswer(i -> i.getArgument(0));
        doThrow(new RuntimeException("ws down"))
                .when(messaging).convertAndSend(anyString(), any(Object.class));

        assertThatNoException()
                .isThrownBy(() -> service.onOrderCreated(new OrderCreatedEvent(101L, 2L)));

        verify(notificationRepo).save(any(Notification.class));
    }
}
