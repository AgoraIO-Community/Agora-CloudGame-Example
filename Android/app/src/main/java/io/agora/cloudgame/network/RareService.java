package io.agora.cloudgame.network;

import io.agora.cloudgame.model.JsonModel;
import io.agora.cloudgame.network.model.ApiResult;
import io.agora.cloudgame.network.model.GameEntity;
import io.agora.cloudgame.network.model.GameResult;
import io.agora.cloudgame.network.model.SendMessage;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface RareService {

    @GET("/v1/apps/{app_id}/cloud-bullet-game/games")
    Call<ApiResult<GameResult>> getGames(@Path("app_id") String id);

    @POST("/v1/apps/{app_id}/cloud-bullet-game/gameid/{game_id}/start")
    Call<ApiResult<GameResult>> startGame(@Path("app_id") String appId, @Path("game_id") String gameId, @Body GameEntity entity);

    @POST("/v1/apps/{app_id}/cloud-bullet-game/gameid/{game_id}/stop")
    Call<ApiResult<JsonModel.Empty>> stopGame(@Path("app_id") String appId, @Path("game_id") String gameId, @Body GameEntity entity);

    @GET("/v1/apps/{app_id}/cloud-bullet-game/gameid/{game_id}/status")
    Call<ApiResult<GameResult>> gameState(@Path("app_id") String appId, @Path("game_id") String gameId, @Query("task_id") String taskId);

    @GET("/v1/apps/{app_id}/cloud-bullet-game/gameid/{game_id}")
    Call<ApiResult<GameResult>> gamesDetails(@Path("app_id") String appId, @Path("game_id") String gameId);

    @POST("/v1/apps/{app_id}/cloud-bullet-game/gameid/{game_id}/gift")
    Call<ApiResult<JsonModel.Empty>> sendGift(@Path("app_id") String appId, @Path("game_id") String gameId, @Body SendMessage entity);

    @POST("/v1/apps/{app_id}/cloud-bullet-game/gameid/{game_id}/comment")
    Call<ApiResult<JsonModel.Empty>> gameComment(@Path("app_id") String appId, @Path("game_id") String gameId, @Body SendMessage entity);

    @POST("/v1/apps/{app_id}/cloud-bullet-game/gameid/{game_id}/like")
    Call<ApiResult<JsonModel.Empty>> gameLike(@Path("app_id") String appId, @Path("game_id") String gameId, @Body SendMessage entity);


}
