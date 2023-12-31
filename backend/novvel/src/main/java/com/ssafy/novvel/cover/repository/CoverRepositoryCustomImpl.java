package com.ssafy.novvel.cover.repository;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.ssafy.novvel.cover.dto.CoverSearchConditions;
import com.ssafy.novvel.cover.dto.CoverSortType;
import com.ssafy.novvel.cover.dto.CoverWithConditions;
import com.ssafy.novvel.cover.dto.EpisodeInfoDto;
import com.ssafy.novvel.cover.dto.QEpisodeInfoDto;
import com.ssafy.novvel.cover.entity.Cover;
import com.ssafy.novvel.cover.entity.CoverStatusType;
import com.ssafy.novvel.cover.util.DefaultImage;
import com.ssafy.novvel.episode.entity.EpisodeStatusType;
import com.ssafy.novvel.member.entity.Member;
import com.ssafy.novvel.transactionhistory.entity.PointChangeType;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import static com.ssafy.novvel.cover.entity.QCover.cover;
import static com.ssafy.novvel.episode.entity.QEpisode.episode;
import static com.ssafy.novvel.episode.entity.QReadEpisode.readEpisode;
import static com.ssafy.novvel.transactionhistory.entity.QTransactionHistory.transactionHistory;
import static com.ssafy.novvel.cover.entity.QLikedCover.likedCover;

@Slf4j
public class CoverRepositoryCustomImpl implements CoverRepositoryCustom {

    private final EntityManager entityManager;

    public CoverRepositoryCustomImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public Page<CoverWithConditions> searchCover(CoverSearchConditions coverSearchConditions,
        Pageable pageable, DefaultImage defaultImage) {

        JPAQuery<List<Cover>> query = new JPAQuery<>(entityManager);
        JPAQuery<Long> countQuery = new JPAQuery<>(entityManager);

        List<Cover> content = query.select(cover)
            .from(cover)
            .leftJoin(cover.resource)
            .fetchJoin()
            .leftJoin(cover.member)
            .fetchJoin()
            .leftJoin(cover.genre)
            .fetchJoin()
            .where(
                checkStatus(coverSearchConditions.getStatus()),
                checkGenre(coverSearchConditions.getGenre()),
                checkKeyword(coverSearchConditions.getKeyword()),
                cover.firstPublishDate.isNotNull()
            )
            .orderBy(order(coverSearchConditions.getSorttype()))
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        Long count = countQuery.select(cover.count())
            .from(cover)
            .where(
                checkStatus(coverSearchConditions.getStatus()),
                checkGenre(coverSearchConditions.getGenre()),
                cover.firstPublishDate.isNotNull()
            )
            .orderBy(order(coverSearchConditions.getSorttype()))
            .fetchOne();

        return new PageImpl<>(
            content.stream().map(
                    c -> covertCoverWithConditions(c, defaultImage))
                .collect(Collectors.toList()),
            pageable,
            count == null ? 0 : count
        );
    }

    @Override
    public List<EpisodeInfoDto> findEpisodesInfoDto(Cover cover, Member member) {

        JPAQuery<EpisodeInfoDto> query = new JPAQuery<>(entityManager);
        checkMemberForSelect(query, member);
        query.from(episode);
        checkMemberForJoin(query, member);
        query.where(
            episode.cover.id.eq(cover.getId()),
            checkPurchased(member),
            checkWriter(cover.getMember(), member)
        );

        return query.fetch();
    }

    @Override
    public Page<CoverWithConditions> findCoverById(Member member, Long id, Pageable pageable,
        DefaultImage defaultImage) {

        JPAQuery<List<Cover>> query = new JPAQuery<>(entityManager);
        JPAQuery<Long> countQuery = new JPAQuery<>(entityManager);

        List<Cover> content = query.select(cover)
            .from(cover)
            .leftJoin(cover.resource)
            .fetchJoin()
            .leftJoin(cover.member)
            .fetchJoin()
            .leftJoin(cover.genre)
            .fetchJoin()
            .where(
                checkStatus(CoverStatusType.ALL),
                checkMine(id, member),
                cover.member.id.eq(id)
            )
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        Long count = countQuery.select(cover.count())
            .from(cover)
            .where(
                checkStatus(CoverStatusType.ALL),
                checkMine(id, member),
                cover.id.eq(id)
            )
            .fetchOne();

        return new PageImpl<>(
            content.stream().map(
                c -> covertCoverWithConditions(c, defaultImage)).collect(Collectors.toList()),
            pageable,
            count == null ? 0 : count
        );
    }

    private Predicate checkStatus(CoverStatusType coverStatusType) {
        switch (coverStatusType) {
            case FINISHED:
                return cover.coverStatusType.eq(CoverStatusType.FINISHED);
            case SERIALIZED:
                return cover.coverStatusType.eq(CoverStatusType.SERIALIZED);
            case LATEST:
                return cover.coverStatusType.eq(CoverStatusType.SERIALIZED)
                    .and(cover.firstPublishDate.goe(LocalDate.now().minusDays(7L)));
            case ALL:
            default:
                return cover.coverStatusType.ne(CoverStatusType.DELETED);
        }
    }

    private Predicate checkGenre(Long genreId) {
        if (genreId.equals(1L)) {
            return null;
        }
        return cover.genre.id.eq(genreId);
    }

    private Predicate checkKeyword(String keyword) {
        log.info(keyword);
        if ("".equals(keyword)) {
            return null;
        }
        return cover.title.contains(keyword)
            .or(cover.member.nickname.contains(keyword));
    }

    private OrderSpecifier<?> order(CoverSortType coverSortType) {

        switch (coverSortType) {
            case date:
                return new OrderSpecifier<>(Order.DESC, cover.lastPublishDate);
            case like:
                return new OrderSpecifier<>(Order.DESC, cover.likes.coalesce(0L));
            case hit:
            default:
                return new OrderSpecifier<>(Order.DESC, cover.viewCount.coalesce(0L));
        }
    }

    private void checkMemberForSelect(JPAQuery<EpisodeInfoDto> query, Member member) {
        if (member == null) {
            query.select(
                new QEpisodeInfoDto(episode.id, episode.title, episode.point, episode.viewCount,
                    episode.publishedDate));
        } else {
            query.select(
                new QEpisodeInfoDto(episode.id, episode.title, episode.point, episode.viewCount,
                    episode.publishedDate, transactionHistory.pointChangeType,
                    readEpisode.member.id.isNotNull()));
        }
    }

    private void checkMemberForJoin(JPAQuery<EpisodeInfoDto> query, Member member) {
        if (member != null) {
            query
                .leftJoin(readEpisode)
                .on(
                    readEpisode.episode.eq(episode)
                        .and(readEpisode.member.id.eq(member.getId()))
                )
                .leftJoin(transactionHistory)
                .on(
                    transactionHistory.episode.eq(episode)
                        .and(transactionHistory.member.id.eq(member.getId()))
                );
        }
    }

    private Predicate checkPurchased(Member member) {
        if (member == null) {
            return episode.statusType.eq(EpisodeStatusType.PUBLISHED);
        }

        return new CaseBuilder().when(
                (transactionHistory.id.isNull()
                    .or(transactionHistory.pointChangeType.ne(PointChangeType.BUY_EPISODE)))
                    .and(episode.statusType.eq(EpisodeStatusType.DELETED)))
            .then(true)
            .otherwise(false)
            .eq(false);
    }

    private Predicate checkWriter(Member writer, Member member) {
        if (member == null || !writer.getId().equals(member.getId())) {
            return episode.statusType.ne(EpisodeStatusType.TEMPORARY);
        }
        return null;
    }

    private Predicate checkMine(Long writerId, Member member) {
        BooleanExpression booleanExpression = cover.coverStatusType.ne(CoverStatusType.DELETED);
        if (member == null || !writerId.equals(member.getId())) {
            return booleanExpression.and(cover.firstPublishDate.isNotNull());
        }

        return booleanExpression;
    }

    private CoverWithConditions covertCoverWithConditions(Cover cover, DefaultImage defaultImage) {

        String thumbnail = null;
        if (cover.getResource() != null) {
            thumbnail = cover.getResource().getThumbnailUrl();
        }

        return CoverWithConditions.builder()
            .id(cover.getId())
            .title(cover.getTitle())
            .status(cover.getCoverStatusType())
            .thumbnail(thumbnail == null ?
                defaultImage.getImageByGenreName(cover.getGenre().getName())
                : thumbnail)
            .views(cover.getViewCount())
            .genre(cover.getGenre())
            .writerId(cover.getMember().getId())
            .writerNickname(cover.getMember().getNickname())
            .isUploaded(cover.getLastPublishDate() == null ? Boolean.FALSE
                : cover.getLastPublishDate().isAfter(LocalDate.now().minusDays(7L)))
            .isNew(cover.getFirstPublishDate() == null ? Boolean.FALSE
                : cover.getFirstPublishDate().isAfter(LocalDate.now().minusDays(7L)))
            .likes(cover.getLikes())
            .build();
    }
}
