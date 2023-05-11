package com.ssafy.novvel.cover.dto;

import com.querydsl.core.annotations.QueryProjection;
import com.ssafy.novvel.cover.entity.Cover;
import com.ssafy.novvel.cover.entity.CoverStatusType;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CoverWithConditions {

    private Long id;
    private String title;
    private CoverStatusType status;
    private String thumbnail;
    private String genre;
    private CoverWriter writer;
    private Boolean isUploaded;
    private Boolean isNew;

    @Builder
    //@QueryProjection
    public CoverWithConditions(Long id, String title,
        CoverStatusType status, String thumbnail, String genre, Long writerId,
        String writerNickname, Boolean isUploaded, Boolean isNew) {
        this.id = id;
        this.title = title;
        this.status = status;
        this.thumbnail = thumbnail;
        this.genre = genre;
        this.writer = new CoverWriter(writerId, writerNickname);
        this.isUploaded = isUploaded;
        this.isNew = isNew;
    }

    @Builder
    @QueryProjection
    public CoverWithConditions(Cover cover, Boolean isUploaded) {
        this.id = cover.getId();
        this.title = cover.getTitle();
        this.status = cover.getCoverStatusType();
        this.thumbnail = cover.getResource().getThumbnailUrl();
        this.genre = cover.getGenre().getName();
        this.writer = new CoverWriter(cover.getMember().getId(), cover.getMember().getNickname());
        this.isUploaded = Boolean.TRUE.equals(isUploaded);
        this.isNew = cover.getPublishDate().isAfter(LocalDate.now().minusDays(7L));
    }

}
