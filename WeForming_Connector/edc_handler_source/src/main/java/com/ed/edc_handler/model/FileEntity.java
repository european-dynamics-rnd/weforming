package com.ed.edc_handler.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Data
@Table(name = "edc_file_entity")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "VARCHAR(36)")
    private String id;

    @Column(name = "file_content", columnDefinition="bytea")
    private byte[] fileContent;

    private String fileName;
    private long fileSize;

    private Instant creationDate;

    @PrePersist
    @PreUpdate
    void setIdIfMissing() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
    }
}
