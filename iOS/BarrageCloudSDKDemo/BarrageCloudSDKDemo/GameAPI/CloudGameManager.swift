//
//  GameManager.swift
//  MyFramework
//
//  Created by FanPengpeng on 2023/9/19.
//

import UIKit

public enum CloudGameStatus: String {
    case schedule  // 被调度
    case starting   // 正在开始
    case running    // 正在运行
    case started     // 已经开始
    case stopped    // 已停止
}


public class CloudGameStartConfigure: NSObject {
    public var vid: String?
    public var roomId: String?
    public var openId: String?
    public var nickname: String?
    public var avatar: String?
    public var assistantUid: UInt = 0
    public var broadcastUid: UInt = 0
    public var token: String?
    public var channelName: String?
    public var mode:Int = 0
    public var secret:String?
    public var salt: String?
}

public class CloudGameSendGiftConfigure: NSObject {
    public var vid: String?
    public var roomId: String?
    public var msgId: String?
    public var content: String?
    public var avatar: String?
    public var timestamp: Int?
    public var openId:String?
    public var nickname: String?
    public var giftId: String?
    public var giftNum = 1
    public var giftValue = 0
    
}

public class CloudGameSendCommentConfigure: NSObject {
    public var vid: String?
    public var roomId: String?
    public var msgId: String?
    public var content: String?
    public var avatar: String?
    public var timestamp: Int?
    public var openId:String?
    public var nickname: String?
}

public class CloudGameSendLikeConfigure: NSObject {
    public var vid: String?
    public var roomId: String?
    public var msgId: String?
    public var avatar: String?
    public var timestamp: Int?
    public var openId:String?
    public var nickname: String?
    public var likeNum = 1
}

public class CloudGameManager: NSObject {
    
    private var host = ""
    
    private var appId: String = ""
    
    @objc public static let shared = CloudGameManager()
    
    private override init() {}
    
    public weak var delegate: ICloudGameEventHandler?

    /// 初始化service
    /// - Parameters:
    ///   - appId: appid
    ///   - host: host
    public func configCloudService(appId: String, host: String){
        self.appId = appId
        self.host = host
    }
    
    /// 获取游戏列表
    public func getGames(pageNum: Int = 1, pageSize: Int = 10){
        
        httpRequest(uri: "cloud-bullet-game/games", httpMethod: "GET", params: ["page_num": pageNum, "page_size": pageSize]) {[weak self] result,code in
            if let list = result?["list"] as? [[String: Any]] {
                var gameList = [CloudGameBaseInfo]()
                list.forEach { dic in
                    let game = CloudGameBaseInfo()
                    game.gameId = dic["game_id"] as? String
                    game.name = dic["name"] as? String
                    game.vendor = dic["vendor"] as? String
                    game.thumbnail = dic["thumbnail"] as? String
                    game.introduce = dic["introduce"] as? String
                    game.vendorGameId = dic["vendor_game_id"] as? String
                    gameList.append(game)
                }
                self?.delegate?.onGamesResults(gameList)
            }
        }
    }
    
    /// 获取游戏详情
    /// - Parameters:
    ///   - gameId: 游戏id
    ///   - completion: 成功回调
    public func getGameInformation(with gameId: String, completion: ((_ gameInfo: CloudGameDetailInfo?,_ code: Int?)->Void)? = nil) {
        httpRequest(uri: "cloud-bullet-game/gameid/\(gameId)", httpMethod: "GET") {[weak self] result, code in
            let gameInfo = CloudGameDetailInfo()
            gameInfo.gameId = result?["game_id"] as? String
            gameInfo.name = result?["name"] as? String
            gameInfo.vendor = result?["vendor"] as? String
            gameInfo.thumbnail = result?["thumbnail"] as? String
            gameInfo.introduce = result?["introduce"] as? String
            gameInfo.vendorGameId = result?["vendor_game_id"] as? String
            
            if let featureDic = result?["feature"] as? [String: Int] {
                let feature = CloudGameFeature()
                feature.comment = featureDic["comment"] ?? 0
                feature.like = featureDic["like"] ?? 0
                gameInfo.feature = feature
            }
            
            if let giftsDic = result?["gifts"] as? [[String: Any]] {
                var giftList = [CloudGameGift]()
                giftsDic.forEach { dic in
                    let gift = CloudGameGift()
                    gift.gameId = dic["game_id"] as? String
                    gift.name = dic["name"] as? String
                    gift.price = dic["price"] as? Int ?? 0
                    gift.thumbnail = dic["thumbnail"] as? String
                    gift.vendorGiftId = dic["vendor_gift_id"] as? String
                    gift.giftId = dic["id"] as? String
                    giftList.append(gift)
                }
                gameInfo.gifts = giftList
            }
            completion?(gameInfo,code)
            self?.delegate?.onGameInformationResult(gameInfo)
        }
    }
    
    /// 开启游戏
    /// - Parameters:
    ///   - gameId: 游戏id
    ///   - config: 开始游戏需要的配置项
    ///   - completion: 开启游戏成功回调
    public func startGame(with gameId: String,config: CloudGameStartConfigure, completion: ((_ taskId: String?,_ code: Int?)->Void)? = nil) {
        var params = [String: Any]()
        params["vid"] = config.vid
        params["room_id"] = config.roomId
        params["open_id"] =  config.openId
        params["nickname"] = config.nickname
        params["avatar"] = config.avatar
        
        var rtcConfig = [String: Any]()
        rtcConfig["uid"] = config.assistantUid
        rtcConfig["broadcast_uid"] = config.broadcastUid
        rtcConfig["token"] = config.token
        rtcConfig["channel_name"] = config.channelName
      
        var encryption = [String: Any]()
        encryption["mode"] = config.mode
        encryption["secret"] = config.secret
        encryption["salt"] = config.salt
        rtcConfig["encryption"] = encryption
        
        params["rtc_config"] = rtcConfig
        
        httpRequest(uri: "cloud-bullet-game/gameid/\(gameId)/start", httpMethod: "POST", params: params) { result, code in
            completion?(result?["task_id"] as? String,code)
        }
    }
    
    /// 结束游戏
    /// - Parameters:
    ///   - id: 游戏id
    ///   - vid: 声网vid
    ///   - roomId: 房间id
    ///   - openId: openid
    ///   - taskId: taskId
    public func endGame(with id:String, vid: String, roomId: String, openId: String, taskId: String, completion: ((_ code: Int?)->Void)? = nil) {
        var params = [String: Any]()
        params["vid"] = vid
        params["room_id"] = roomId
        params["open_id"] = openId
        params["task_id"] = taskId
        httpRequest(uri: "cloud-bullet-game/gameid/\(id)/stop", httpMethod: "POST", params: params) { result, code in
            completion?(code)
        }
    }
    
    /// 查询游戏状态
    /// - Parameters:
    ///   - id: 游戏id
    ///   - taskId: taskId
    public func getGameStatus(with id:String, taskId: String,completion: ((_ status: String?)->Void)?) {
        var params = [String: Any]()
        params["task_id"] = taskId
        httpRequest(uri: "cloud-bullet-game/gameid/\(id)/status", httpMethod: "GET", params: params) { result, code in
            completion?(result?["status"] as? String)
        }
    }
 
    
    /// 发送礼物
    /// - Parameters:
    ///   - gameId: 游戏id
    ///   - giftConfig: 发送礼物配置项
    ///   - completion: 成功回调
    public func sendGift(gameId: String, giftConfig: CloudGameSendGiftConfigure, completion: ((_ code: Int?)->Void)? = nil){
        var params = [String: Any]()
        params["vid"] = giftConfig.vid
        params["room_id"] = giftConfig.roomId
        
        var payload = [String: Any]()
        payload["msg_id"] = giftConfig.msgId
        payload["open_id"] = giftConfig.openId
        payload["nickname"] = giftConfig.nickname
        payload["timestamp"] = giftConfig.timestamp
        payload["avatar"] = giftConfig.avatar
        payload["gift_id"] = giftConfig.giftId
        payload["gift_num"] = giftConfig.giftNum
        payload["gift_value"] = giftConfig.giftValue
        
        params["payload"] = [payload]
        
        httpRequest(uri: "cloud-bullet-game/gameid/\(gameId)/gift", httpMethod: "POST", params: params) { result, code in
            completion?(code)
        }
    }
    
    /// 发送评论
    /// - Parameters:
    ///   - gameId: 游戏id
    ///   - giftConfig: 发送评论参数
    ///   - completion: 成功回调
    public func sendComment(gameId: String, commentConfig: CloudGameSendCommentConfigure, completion: ((_ code: Int?)->Void)? = nil){
        var params = [String: Any]()
        params["vid"] = commentConfig.vid
        params["room_id"] = commentConfig.roomId
        
        var payload = [String: Any]()
        payload["msg_id"] = commentConfig.msgId
        payload["open_id"] = commentConfig.openId
        payload["nickname"] = commentConfig.nickname
        payload["timestamp"] = commentConfig.timestamp
        payload["avatar"] = commentConfig.avatar
        payload["content"] = commentConfig.content
        
        params["payload"] = [payload]
        
        httpRequest(uri: "cloud-bullet-game/gameid/\(gameId)/comment", httpMethod: "POST", params: params) { result, code in
            completion?(code)
        }
    }
    
    /// 点赞
    /// - Parameters:
    ///   - gameId: 游戏id
    ///   - giftConfig: 发送评论配置项
    ///   - completion: 成功回调
    public func sendLike(gameId: String, commentConfig: CloudGameSendLikeConfigure, completion: ((_ code: Int?)->Void)? = nil){
        var params = [String: Any]()
        params["vid"] = commentConfig.vid
        params["room_id"] = commentConfig.roomId
        
        var payload = [String: Any]()
        payload["msg_id"] = commentConfig.msgId
        payload["open_id"] = commentConfig.openId
        payload["nickname"] = commentConfig.nickname
        payload["timestamp"] = commentConfig.timestamp
        payload["avatar"] = commentConfig.avatar
        payload["like_num"] = commentConfig.likeNum
        
        params["payload"] = [payload]
        
        httpRequest(uri: "cloud-bullet-game/gameid/\(gameId)/like", httpMethod: "POST", params: params) { result, code in
            completion?(code)
        }
    }
    
    public func marshalTouchEvent(_ event: String){
        
    }
}

typealias HttpCompletion = (_ result: [String: Any]?, _ code: Int?)->Void

extension CloudGameManager {
    
    private func httpRequest(uri: String, httpMethod:String, params:[String: Any]? = nil, completion: HttpCompletion?) {

        var url = URL(string: "\(host)\(appId)/\(uri)")!
        if let params = params {
            if httpMethod == "GET" {
                url = appendQueryParams(to: url, queryParams: params) ?? url
                print(" GET url = \(url)")
            }
        }
       
        var request = URLRequest(url: url)
        request.addValue("text/plain", forHTTPHeaderField: "Con1tent-Type")
        request.addValue("agora token=\(TokenCreater.rtmToken)", forHTTPHeaderField: "Authorization")
        print(" agora token=\(TokenCreater.rtmToken)")

        request.httpMethod = httpMethod
        if let params = params {
            if httpMethod == "POST" {
                let jsonBody = try? JSONSerialization.data(withJSONObject: params)
                request.httpBody = jsonBody
                print(" POST url = \(url), params = \(params.debugDescription)")
            }
        }
        let task = URLSession.shared.dataTask(with: request) { data, response, error in
            guard let data = data, error == nil else {
                print("Error: \(error?.localizedDescription ?? "Unknown error")")
                return
            }
            print(" httpRequest data = \(data) ")
            if let dic = try? JSONSerialization.jsonObject(with: data) as? [String: Any] {
                let result = dic["result"] as? [String: Any]
                let code = dic["code"] as? Int
                print(" result = \(String(describing: result)), code = \(String(describing: code))")
                DispatchQueue.main.async {
                    completion?(result,code)                    
                }
            }
        }
        task.resume()
    }
    
    private func appendQueryParams(to url: URL, queryParams: [String: Any]) -> URL? {
        var urlComponents = URLComponents(url: url, resolvingAgainstBaseURL: true)
        var urlParams = [String]()
        
        queryParams.forEach { (key, value) in
            urlParams.append("\(key)=\(value)")
        }
        
        let paramsString = urlParams.joined(separator: "&")
        
        if var query = urlComponents?.query {
            query.append("&" + paramsString)
            urlComponents?.query = query
        } else {
            urlComponents?.query = paramsString
        }
        return urlComponents?.url
    }
}
