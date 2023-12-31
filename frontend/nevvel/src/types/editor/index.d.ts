declare module "editor" {
  export interface episodeRegistDto {
    episodeRegistDto: episode;
  }

  export interface episode {
    coverId: number;
    statusType: string;
    point: number;
    title: string;
    contents: content[];
  }

  export interface postEpisode {
    coverId: number;
    statusType: string;
    point: number;
    title: string;
    contents: content[];
    reservation:boolean;
    reservationTime:string;
  }


  export interface content {
    idx: number;
    context: string;
    event: event[];
  }
  export interface event {
    assetId: number;
    type: string;
  }
  export interface AssetTag {
    id: number;
    tagName: string;
    useCount : number;
  }

  export interface AssetUploader {
    id: number;
    nickname: string;
    profileImage: string;
  }

  export interface AssetContent{
    content:Asset[]
  }


  export interface Asset {
    downloadCount: number;
    id: number;
    isAvailable:boolean;
    price: number;
    tags: AssetTag[];
    title: string;
    type: string;
    thumbnail: string;
    url: string;
    uploader: AssetUploader;
  }
}
