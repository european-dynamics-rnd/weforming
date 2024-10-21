package com.ed.edc_handler.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.Id;
import java.time.Instant;

@Entity
@Data
@Table(name = "edc_file_entity")
public class FileEntity {

    @jakarta.persistence.Id
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(name = "id", updatable = false, nullable = false)
    private String id;


    @Column(name = "file_content", columnDefinition="bytea")
    private byte[] fileContent;

    private String fileName;
    private long fileSize;

    private Instant creationDate;
}
