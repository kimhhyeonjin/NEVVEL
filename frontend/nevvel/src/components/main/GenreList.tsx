import { useRouter } from "next/router";
import styled from "styled-components";

interface Props {
  id: number;
  name: string;
  nav: string;
}

function GenreList({ id, name, nav }: Props) {
  const router = useRouter();

  const genreSelectHandler = () => {
    router.push(
      { pathname: `/novels/${nav}`, query: { genre: id, sort: "like" } },
      `/novels/${nav}`
    );
  };

  return (
    <GenreWrapper>
      <GenreName type="button" value={name} onClick={genreSelectHandler} />
    </GenreWrapper>
  );
}

export default GenreList;

const GenreWrapper = styled.div`
  display: flex;
`;

const GenreName = styled.input`
  background-color: ${({ theme }) => theme.color.subNavbar};
  border: none;
  color: ${({ theme }) => theme.color.text2};
  font-size: 16px;
  justify-content: center;
  :hover {
    cursor: pointer;
  }
`;