package com.dawn.common.outbox;

import com.dawn.common.event.OrderCreatedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class OutboxPublisherTest {

    @Test
    @DisplayName("publish: lưu payload JSON + đúng topic/aggregate")
    void publish_savesSerializedEvent() {
        OutboxRepository repository = mock(OutboxRepository.class);
        OutboxPublisher publisher = new OutboxPublisher(repository, JsonMapper.builder().build());

        publisher.publish("order-events", "ORDER", 42L, new OrderCreatedEvent(42L, 7L));

        ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(repository).save(captor.capture());
        OutboxEvent saved = captor.getValue();
        assertThat(saved.getTopic()).isEqualTo("order-events");
        assertThat(saved.getAggregateId()).isEqualTo(42L);
        assertThat(saved.getEventType()).isEqualTo("OrderCreatedEvent");
        assertThat(saved.getPayload()).contains("\"orderId\":42", "\"userId\":7");
    }
}
