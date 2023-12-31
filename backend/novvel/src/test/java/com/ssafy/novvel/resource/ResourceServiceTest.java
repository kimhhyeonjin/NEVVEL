package com.ssafy.novvel.resource;

import com.ssafy.novvel.resource.entity.Resource;
import com.ssafy.novvel.resource.repository.ResourceRepository;
import com.ssafy.novvel.resource.service.S3Service;
import com.ssafy.novvel.resource.service.ResourceServiceImpl;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@ExtendWith(MockitoExtension.class)
class ResourceServiceTest {

    @InjectMocks
    private ResourceServiceImpl resourceService;
    @Mock
    private ResourceRepository resourceRepository;
    @Mock
    private S3Service s3Service;

    @Test
//    @Disabled
    @DisplayName("fileSaveGifTest")
    void fileSaveGifTest() throws IOException {

        //given
        File file = new File("src/test/resources/test.gif");
        Resource expect = Resource.builder()
                .id(1L)
                .originName("test.gif")
                .url("src/java/resources/uuid-test.gif")
                .isThumbnail(true)
                .thumbnailUrl("src/java/resources/uuid-test_thumbnail.gif")
                .build();

        //when
        Mockito.doReturn(expect).when(resourceRepository).save(Mockito.any());
        Mockito.doReturn("urlPath").when(s3Service).uploadFile(Mockito.any(), Mockito.any());
//        resourceService = new ResourceServiceImpl(awsProxyService, resourceRepository);

        //then
        Resource result = resourceService.saveFile(file);
        Assertions.assertThat(result.getUrl()).isNotNull();
        Assertions.assertThat(result.getOriginName()).isEqualTo("test.gif");

    }


    @Test
//    @Disabled
    @DisplayName("fileSaveJTest")
    void fileSaveJpgTest() throws IOException {

        //given
        File file = new File("src/test/resources/test.jpg");
        Resource expect = Resource.builder()
                .id(1L)
                .originName("test.jpg")
                .url("src/java/resources/uuid-test.jpg")
                .isThumbnail(true)
                .thumbnailUrl("src/java/resources/uuid-test_thumbnail.jpg")
                .build();

        //when
        Mockito.doReturn(expect).when(resourceRepository).save(Mockito.any());
        Mockito.doReturn("urlPath").when(s3Service).uploadFile(Mockito.any(), Mockito.any());
//        resourceService = new ResourceServiceImpl(awsProxyService, resourceRepository);

        //then
        Resource result = resourceService.saveFile(file);

        Assertions.assertThat(result.getUrl()).isNotNull();
        Assertions.assertThat(result.getOriginName()).isEqualTo("test.jpg");

    }

    @Test
//    @Disabled
    @DisplayName("fileSaveMp3Test")
    void fileSaveMp3Test() throws IOException {

        //given
        File file = new File("src/test/resources/sample.mp3");
        Resource expect = Resource.builder()
                .id(1L)
                .originName("sample.mp3")
                .url("src/java/resources/uuid-sample.mp3")
                .isThumbnail(true)
                .thumbnailUrl("src/java/resources/uuid-sample_thumbnail.mp3")
                .build();

        //when
        Mockito.doReturn(expect).when(resourceRepository).save(Mockito.any());
        Mockito.doReturn("urlPath").when(s3Service).uploadFile(Mockito.any(), Mockito.any());
//        resourceService = new ResourceServiceImpl(awsProxyService, resourceRepository);

        //then
        Resource result = resourceService.saveFile(file);

        Assertions.assertThat(result.getUrl()).isNotNull();
        Assertions.assertThat(result.getOriginName()).isEqualTo("sample.mp3");
//        resultOrigin.delete();
    }


    @Test
//    @Disabled
    @DisplayName("get gif thumbnail img")
    void makeThumbnailFromGifTest() throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        File file = new File("src/test/resources/test.gif");
        Method makeThumbnailFromGifMethod = ResourceServiceImpl.class.getDeclaredMethod("makeThumbnailFromGif", File.class);
        makeThumbnailFromGifMethod.setAccessible(true);
        File result = (File) makeThumbnailFromGifMethod.invoke(resourceService, file);
        Assertions.assertThat(result.getName()).isEqualTo("test_thumbnail.gif");
        result.delete();

    }

    @Test
//    @Disabled
    @DisplayName("convert gif to png")
    void convertGifToPngTest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        File file = new File("src/test/resources/test_thumbnail.gif");
        Method makeThumbnailFromGifMethod = ResourceServiceImpl.class.getDeclaredMethod("convertToPng", File.class);
        makeThumbnailFromGifMethod.setAccessible(true);
        File result = (File) makeThumbnailFromGifMethod.invoke(resourceService, file);
        result.delete();

    }

    @Test
//    @Disabled
    @DisplayName("convert gif to jpg")
    void convertGifToJpgTest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        File file = new File("src/test/resources/test_thumbnail.gif");
        Method makeThumbnailFromGifMethod = ResourceServiceImpl.class.getDeclaredMethod("convertToJpg", File.class);
        makeThumbnailFromGifMethod.setAccessible(true);
        File result = (File) makeThumbnailFromGifMethod.invoke(resourceService, file);
        result.delete();

    }

    @Test
//    @Disabled
    @DisplayName("convert jpg to png")
    void convertJpgToPngTest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        File file = new File("src/test/resources/test.jpg");
        Method makeThumbnailFromGifMethod = ResourceServiceImpl.class.getDeclaredMethod("convertToPng", File.class);
        makeThumbnailFromGifMethod.setAccessible(true);
        File result = (File) makeThumbnailFromGifMethod.invoke(resourceService, file);
        result.delete();

    }

    @Test
//    @Disabled
    @DisplayName("convert Height Resolution Png")
    void convertResolutionPngHeightTest() throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        File file = new File("src/test/resources/p.png");
        Method makeThumbnailFromGifMethod =
                ResourceServiceImpl.class.getDeclaredMethod("convertResolutionPng", File.class, Integer.class, Integer.class);
        makeThumbnailFromGifMethod.setAccessible(true);
        File result = (File) makeThumbnailFromGifMethod.invoke(resourceService, file, 200, 200);
        result.delete();
    }

    @Test
//    @Disabled
    @DisplayName("convert Width Resolution Png")
    void convertResolutionPngWidthTest() throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        File file = new File("src/test/resources/p2.png");
        Method makeThumbnailFromGifMethod =
                ResourceServiceImpl.class.getDeclaredMethod("convertResolutionPng", File.class, Integer.class, Integer.class);
        makeThumbnailFromGifMethod.setAccessible(true);
        File result = (File) makeThumbnailFromGifMethod.invoke(resourceService, file, 200, 200);
        result.delete();
    }
}
