package io.agora.cloudgame.network;

import io.agora.cloudgame.model.JsonModel;
import io.agora.cloudgame.network.model.ApiResult;
import io.agora.cloudgame.network.model.GameDetailResult;
import io.agora.cloudgame.network.model.GameListResult;
import io.agora.cloudgame.network.model.GameStateResult;
import io.agora.cloudgame.network.model.SendMessageBody;
import io.agora.cloudgame.network.model.StartGameBody;
import io.agora.cloudgame.network.model.StartGameResult;
import io.agora.cloudgame.network.model.StopGameBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface RareService {

    @GET("/v2/projects/{appid}/cloud-bullet-game/api/live-data/games")
    Call<ApiResult<GameListResult>> getGames(@Path("appid") String appId);

    @GET("/v2/projects/{appid}/cloud-bullet-game/api/live-data/games/{gameid}")
    Call<ApiResult<GameDetailResult>> gamesDetails(@Path("appid") String appId, @Path("gameid") String gameId);

    @POST("/v2/projects/{appid}/cloud-bullet-game/api/live-data/games/{gameid}/mode/cloud/rooms/{roomid}:start")
    Call<ApiResult<StartGameResult>> startGame(@Path("appid") String appId, @Path("gameid") String gameId, @Path("roomid") String roomId, @Body StartGameBody body);

    @POST("/v2/projects/{appid}/cloud-bullet-game/api/live-data/games/{gameid}/mode/cloud/rooms/{roomid}:stop")
    Call<ApiResult<JsonModel.Empty>> stopGame(@Path("appid") String appId, @Path("gameid") String gameId, @Path("roomid") String roomId, @Body StopGameBody body);

    @GET("/v2/projects/{appid}/cloud-bullet-game/api/live-data/tasks/{taskid}/status")
    Call<ApiResult<GameStateResult>> gameState(@Path("appid") String appId, @Path("taskid") String taskId);

    @POST("/v2/projects/{appid}/bullet-game/api/live-data/games/{gameid}/rooms/{roomid}/msgType/live_gift:push")
    Call<ApiResult<JsonModel.Empty>> sendGiftV2(@Path("appid") String appId, @Path("roomid") String roomId, @Path("gameid") String gameId, @Body SendMessageBody body);

    @POST("/v2/projects/{appid}/bullet-game/api/live-data/games/{gameid}/rooms/{roomid}/msgType/live_comment:push")
    Call<ApiResult<JsonModel.Empty>> gameCommentV2(@Path("appid") String appId, @Path("roomid") String roomId, @Path("gameid") String gameId, @Body SendMessageBody body);

    @POST("/v2/projects/{appid}/bullet-game/api/live-data/games/{gameid}/rooms/{roomid}/msgType/live_like:push")
    Call<ApiResult<JsonModel.Empty>> gameLikeV2(@Path("appid") String appId, @Path("roomid") String roomId, @Path("gameid") String gameId, @Body SendMessageBody body);
}
