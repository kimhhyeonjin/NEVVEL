package com.ssafy.novvel.file;

import com.ssafy.novvel.file.service.AwsProxyService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;

@SpringBootTest
public class AwsProxyServiceTest {
    @Autowired
    private AwsProxyService awsProxyService;

    @Test
    @Disabled
    @DisplayName("file upload test")
    void uploadFileTest() throws IOException {

        File file = new File("src/test/resources/test.gif");
        String fileNamePrefix = "files/" + LocalDate.now(ZoneId.of("Asia/Seoul")) + UUID.randomUUID() + "-";
        awsProxyService.uploadFile(file, fileNamePrefix + file.getName());

    }
}
