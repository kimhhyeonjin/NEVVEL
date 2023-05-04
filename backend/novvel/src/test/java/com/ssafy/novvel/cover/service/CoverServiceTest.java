package com.ssafy.novvel.cover.service;

import com.ssafy.novvel.cover.dto.CoverInfoAndEpisodesDto;
import com.ssafy.novvel.cover.dto.CoverRegisterDto;
import com.ssafy.novvel.cover.dto.EpisodeInfoDto;
import com.ssafy.novvel.cover.entity.Cover;
import com.ssafy.novvel.cover.entity.CoverStatusType;
import com.ssafy.novvel.cover.repository.CoverRepository;
import com.ssafy.novvel.genre.entity.Genre;
import com.ssafy.novvel.genre.repository.GenreRepository;
import com.ssafy.novvel.member.entity.Member;
import com.ssafy.novvel.resource.entity.Resource;
import com.ssafy.novvel.resource.service.ResourceService;
import com.ssafy.novvel.transactionhistory.entity.PointChangeType;
import com.ssafy.novvel.util.TestUtil;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class CoverServiceTest {

    @InjectMocks
    CoverServiceImpl coverService;

    @Mock
    CoverRepository coverRepository;

    @Mock
    GenreRepository genreRepository;

    @Mock
    ResourceService resourceService;

    @Test
    void registerCover() throws IOException {

        // given
        File file = new File("src/test/resources/cat.jpeg");

        MultipartFile multipartFile = new MockMultipartFile(file.getName(), file.getName(),
            Files.probeContentType(file.toPath()), Files.readAllBytes(file.toPath()));
        Resource resource = TestUtil.getMemberProfile();

        CoverRegisterDto coverRegisterDto = CoverRegisterDto.builder().genreId(1L).title("test")
            .build();
        Genre genre = Genre.builder().id(1L).build();

        Cover expect = Cover.builder()
            .id(1L).coverStatusType(CoverStatusType.SERIALIZED).genre(genre)
            .member(TestUtil.getUSERMember().orElse(null)).resource(resource)
            .title(coverRegisterDto.getTitle()).build();

        // when
        Mockito.doReturn(resource).when(resourceService).saveFile(multipartFile);
        Mockito.doReturn(genre).when(genreRepository)
            .getReferenceById(Mockito.any());
        Mockito.doReturn(expect).when(coverRepository).save(Mockito.any());
        Cover result = coverService.registerCover(multipartFile, coverRegisterDto,
            TestUtil.getUSERMember().orElse(null));

        // then
        Assertions.assertThat(result.getCoverStatusType()).isEqualTo(expect.getCoverStatusType());
        Assertions.assertThat(result.getTitle()).isEqualTo(expect.getTitle());
        Assertions.assertThat(result.getGenre()).isEqualTo(expect.getGenre());
        Assertions.assertThat(result.getResource()).isEqualTo(expect.getResource());

    }

    @Test
    void getAllEpisode() {

        // given
        Member member = TestUtil.getMember();
        Genre genre = Genre.builder().id(1L).name("test_genre").build();
        Optional<Cover> given = Optional.of(Cover.builder()
            .id(1L).coverStatusType(CoverStatusType.SERIALIZED).genre(genre)
            .member(TestUtil.getUSERMember().orElse(null))
            .title("test").build());

        List<EpisodeInfoDto> list = new ArrayList<>();
        list.add(new EpisodeInfoDto(1L, 0L, 0L, LocalDateTime.now(),
            PointChangeType.BUY_EPISODE, true));
        list.add(new EpisodeInfoDto(2L, 0L, 0L, LocalDateTime.now(),
            PointChangeType.SELL_EPISODE, true));

        CoverInfoAndEpisodesDto expect = CoverInfoAndEpisodesDto.builder()
            .episodes(list)
            .genreName(given.get().getGenre().getName())
            .title(given.get().getTitle())
            .build();

        // when
        Mockito.doReturn(given).when(coverRepository).findById(1L);
        Mockito.doReturn(list).when(coverRepository).findEpisodesInfoDto(1L, member.getId());
        CoverInfoAndEpisodesDto result = coverService.getAllEpisodes(1L, member.getId());

        // then
        Assertions.assertThat(result.getTitle()).isEqualTo(expect.getTitle());
        Assertions.assertThat(result.getDescription()).isEqualTo(expect.getDescription());
        Assertions.assertThat(result.getGenreName()).isEqualTo(expect.getGenreName());
        Assertions.assertThat(result.getEpisodes().size()).isEqualTo(expect.getEpisodes().size());
        Assertions.assertThat(result.getEpisodes().get(0).getIsPurchased()).isEqualTo(true);
        Assertions.assertThat(result.getEpisodes().get(1).getIsPurchased()).isEqualTo(false);

    }

    @Test
    void getAllEpisodeNotExistCover() {

        // given
        Member member = TestUtil.getMember();

        Optional<Cover> given = Optional.empty();

        // when
        Mockito.doReturn(given).when(coverRepository).findById(1L);
        Assertions.assertThatThrownBy(() -> coverService.getAllEpisodes(1L, member.getId()))
            .isInstanceOf(NullPointerException.class);

    }
}