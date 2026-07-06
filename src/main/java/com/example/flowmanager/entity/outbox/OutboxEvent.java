package com.example.flowmanager.entity.outbox;

import com.example.flowmanager.entity.FileRecord;
import com.example.flowmanager.enums.OutboxStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "outbox_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OutboxEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "aggregate_id", nullable = false)
    private UUID aggregateId;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "payload", columnDefinition = "TEXT")
    private String payload;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private OutboxStatus status; 

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "retry_count")
    private Integer retryCount = 0;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OutboxEvent)) return false;
        OutboxEvent that = (OutboxEvent) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aggregate_id", referencedColumnName = "id", insertable = false, updatable = false)
    private FileRecord fileRecord;
}

