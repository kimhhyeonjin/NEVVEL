import React, { useEffect, useState, useRef } from "react";
import styled from "styled-components";
import Image from "next/image";
import { useAtomValue } from "jotai";
import { numAtom } from "@/src/store/ViewerScroll";
import { useRouter } from "next/dist/client/router";
import { useParams } from "next/navigation";
import { GetStaticPaths, GetStaticProps } from "next";

import springApi from "@/src/api";
import { AiFillSetting } from "react-icons/ai";
import { mobile, tabletH } from "@/src/util/Mixin";
import { EpisodeView } from "viewer";

import ViewHeader from "../../../components/viewer/ViewHeader";
import ViewerTabMain from "../../../components/viewer/Main/ViewerTabMain";
import ViewerPageMain from "@/src/components/viewer/Main/ViewerPageMain";
import Dummy_Episode from "../../../components/viewer/DummyEpisodeData.json";

import eyes from "@/src/assets/img/eyes.png";
import SettingBox from "@/src/components/viewer/SettingBox";
import DummyAssetData_audio from "@/src/components/assetstore/DummyAssetData_Audio.json";

function viewer() {
  const router = useRouter();
  const id = router.query.id;
  const [headerToggle, setHeaderToggle] = useState(true); // header on/off
  const [tabNumber, setTabNumber] = useState(0); // tab mode 일 때 사용
  const [eventCatch, setEventCatch] = useState(false); // tab mode 일때 이벤트 있는 경우 사용
  const [settingBox, setSettingBox] = useState(false); // 설정 box 보여 줄 때 사용
  const [writeMode, setWriteMode] = useState(false); // tab or page 모드 설정 토글
  const [audioEventCatch, setAudioEventCatch] = useState(false); // 오디오 재생
  const [fontStyle, setFontStyle] = useState(""); //font_style 변경 ""은 기본 pretendard
  const [fontSize, setFontSize] = useState(3);
  const [whiteSpace, setWhiteSpace] = useState(1);
  const [interval, setInterval] = useState(1);
  const audioRef = useRef<any>(null);
  const scrollRef = useRef<any>();
  const nowTextBlock = useAtomValue(numAtom);
  const [EpisodeData, setEpisodeData] = useState<EpisodeView>(Dummy_Episode);

  const getViewerData = async (Id: number) => {
    const res = await springApi.get(`/episodes/${Id}`);
    if (res) {
      console.log(res);
      setEpisodeData(res.data);
    }
  };

  useEffect(() => {
    console.log(id);
    if (id) {
      const Id = Number(id);
      console.log("router", Id);
      getViewerData(Id);
    } else {
      setEpisodeData(Dummy_Episode);
    }
  }, [id]);

  useEffect(() => {
    console.log(EpisodeData);
  }, [EpisodeData]);

  useEffect(() => {
    // console 찍었을때 content 젤 마지막 index 값이 나오고 현재 스크롤 마지막 값이 나옴..
    if (nowTextBlock !== EpisodeData.contents.length) {
      if (EpisodeData.contents[nowTextBlock].event.length !== 0) {
        const events = EpisodeData.contents[nowTextBlock].event;
        for (const event of events) {
          if (event.type === "IMAGE") {
            console.log("이미지당");
            setEventCatch(true);
          }
          if (event.type === "AUDIO") {
            console.log("소리당");
            setAudioEventCatch(true);
          }
        }
      }
    }
    return () => {
      if (eventCatch) {
        setEventCatch(false);
      }
      if (audioEventCatch) {
        setAudioEventCatch(false);
      }
    };
    console.log(nowTextBlock);
  }, [nowTextBlock]);

  useEffect(() => {
    if (EpisodeData.contents[tabNumber].event.length !== 0) {
      const events = EpisodeData.contents[tabNumber].event;
      for (const event of events) {
        if (event.type === "IMAGE") {
          console.log("이미지당");
          setEventCatch(true);
        }
        if (event.type === "AUDIO") {
          console.log("소리당");
          setAudioEventCatch(true);
        }
      }
    }
    scrollRef.current.scrollTop = scrollRef.current?.scrollHeight;
    return () => {
      if (eventCatch) {
        setEventCatch(false);
      }
      if (audioEventCatch) {
        setAudioEventCatch(false);
      }
    };
  }, [tabNumber]);

  useEffect(() => {
    if (audioEventCatch) {
      audioRef.current.play();
    }
  }, [audioEventCatch]);

  useEffect(() => {
    const contents = EpisodeData.contents;
    for (const content of contents) {
      console.log(content);
    }
  }, [audioEventCatch]);

  useEffect(() => {
    // 읽는 모드 변경 했을 때 에셋 나와 있으면 안되니까 감지하는 로직

    if (audioEventCatch) {
      setAudioEventCatch(false);
    }
    if (eventCatch) {
      setEventCatch(false);
    }
  }, [writeMode]);

  const clickhandler = (e: string) => {
    console.log(e);
    if (e === "head") {
      setHeaderToggle(true);
    } else if (e === "block") {
      setHeaderToggle(false);
      // 비동기 처리 아직 못했음
    }
  };

  const countHandler = () => {
    const contentLength = EpisodeData.contents.length;
    if (tabNumber < contentLength - 1) {
      setTabNumber(tabNumber + 1);
    } else if (tabNumber === contentLength - 1) {
      console.log("마지막 입니다. ");
    }
    console.log(tabNumber);
  };

  return (
    <ViewerWrapper>
      <HeaderContainer onClick={() => clickhandler("head")}>
        {headerToggle ? <ViewHeader EpisodeData={EpisodeData} /> : null}
      </HeaderContainer>

      <MainWrapper onClick={() => setHeaderToggle(false)}>
        {eventCatch ? (
          <ImageEvent>
            <Image src={eyes} alt="Logo" fill />
          </ImageEvent>
        ) : null}
        {writeMode ? (
          <MainContainer writeMode={writeMode}>
            <ViewerPageMain
              EpisodeData={EpisodeData}
              fontSize={fontSize}
              fontStyle={fontStyle}
              whiteSpace={whiteSpace}
              interval={interval}
            />
          </MainContainer>
        ) : (
          <MainContainer
            writeMode={writeMode}
            ref={scrollRef}
            onClick={countHandler}
          >
            {tabNumber === 0 ? (
              <div>{EpisodeData.title}</div>
            ) : (
              <ViewerTabMain
                fontSize={fontSize}
                fontStyle={fontStyle}
                whiteSpace={whiteSpace}
                interval={interval}
                EpisodeData={EpisodeData}
                tabNumber={tabNumber}
                setEventCatch={setEventCatch}
              />
            )}
          </MainContainer>
        )}
      </MainWrapper>
      <SettingBtn onClick={() => setSettingBox(!settingBox)}>
        <AiFillSetting size="28" />
      </SettingBtn>
      {settingBox ? (
        <>
          <SettingBox
            fontSize={fontSize}
            whiteSpace={whiteSpace}
            interval={interval}
            setFontStyle={setFontStyle}
            setWriteMode={setWriteMode}
            setInterval={setInterval}
            setWhiteSpace={setWhiteSpace}
            setFontSize={setFontSize}
          />
        </>
      ) : null}
      {audioEventCatch && (
        <audio ref={audioRef} src={`${DummyAssetData_audio.content[1].url}`} />
      )}
    </ViewerWrapper>
  );
}

const ViewerWrapper = styled.div`
  display: flex;
  flex-direction: column;
  height: 100vh;
  -webkit-user-select: none;
  -moz-user-select: none;
  -ms-user-select: none;
  user-select: none;
  ${mobile} {
    font-size: 12px;
  }
  /* font-family: "Malgun Gothic"; */
`;

const HeaderContainer = styled.div`
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 15vh;
`;
const MainWrapper = styled.div``;

const MainContainer = styled.div<{ writeMode: boolean }>`
  height: ${(props) => (props.writeMode ? 50 : 70)}vh;
  margin-left: 30%;
  margin-right: 30%;
  overflow-y: scroll;

  ::-webkit-scrollbar {
    display: none;
  }
  ${tabletH} {
    margin-left: 20%;
    margin-right: 20%;
  }
  ${mobile} {
    margin-left: 10%;
    margin-right: 10%;
  }
  /* border: 1px solid black; */
`;

const ImageEvent = styled.div`
  /* position: relative; */
  position: fixed;
  opacity: 0.7;
  left: 25%;
  z-index: 10;
  width: 600px;
  height: 600px;
  ${tabletH} {
    left: 20%;
    width: 500px;
    height: 500px;
  }
  ${mobile} {
    left: 10%;
    width: 300px;
    height: 300px;
  }
`;
const SettingBtn = styled.button`
  position: fixed;
  border-radius: 50%;
  width: 3rem;
  height: 3rem;
  box-shadow: 0rem 0rem 0.5rem ${({ theme }) => theme.color.text1};
  top: 80%;
  left: 90%;
  color: ${({ theme }) => theme.color.text1};
  ${mobile} {
    left: 80%;
  }
`;

export default viewer;
