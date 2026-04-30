package cn.kong.cosmos.biz.theater.dto.req;

import lombok.Data;

/**
 * 播放上报请求体
 */
@Data
public class PlayReportReq {

    /** 当前播放剧集（可选，单集内容类型可不传） */
    private Integer episodeNumber;
}
