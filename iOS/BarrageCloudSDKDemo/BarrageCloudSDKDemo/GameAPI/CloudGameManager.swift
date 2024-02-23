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
    
    private var outsideGameHost = ""
    private var inGameHost = ""
    
    private var appId: String = ""
    
    @objc public static let shared = CloudGameManager()
    
    private override init() {}
    
    public weak var delegate: ICloudGameEventHandler?

    /// 初始化service
    /// - Parameters:
    ///   - appId: appid
    ///   - host: host
    public func configCloudService(appId: String, outsizeHost: String,inHost: String){
        self.appId = appId
        self.outsideGameHost = outsizeHost
        self.inGameHost = inHost
    }
    
    /// 获取游戏列表
    public func getGames(pageNum: Int = 1, pageSize: Int = 10){
        let uri = "cloud-bullet-game/api/live-data/games"
        let params = ["page_num": pageNum, "page_size": pageSize]
        outsideGameHttpRequest(uri: uri,
                               httpMethod: "GET",
                               params: params) {[weak self] result,code in
            if let list = result?["list"] as? [[String: Any]] {
                var gameList = [CloudGameBaseInfo]()
                list.forEach { dic in
                    let game = CloudGameBaseInfo()
                    game.gameId = dic["game_id"] as? String
                    game.name = dic["name"] as? String
                    game.vendor = dic["vendor"] as? String
                    game.thumbnail = dic["thumbnail"] as? String
                    game.introduce = dic["introduce"] as? String
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
        let uri = "cloud-bullet-game/api/live-data/games/\(gameId)"
        outsideGameHttpRequest(uri: uri, httpMethod: "GET") {[weak self] result, code in
            let gameInfo = CloudGameDetailInfo()
            gameInfo.gameId = result?["game_id"] as? String
            gameInfo.name = result?["name"] as? String
            gameInfo.vendor = result?["vendor"] as? String
            gameInfo.thumbnail = result?["thumbnail"] as? String
            gameInfo.introduce = result?["introduce"] as? String
            
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
    public func startGame(with gameId: String, roomId: String, config: CloudGameStartConfigure, completion: ((_ taskId: String?,_ code: Int?)->Void)? = nil) {
        var params = [String: Any]()
        params["openid"] =  config.openId
        params["nickname"] = config.nickname
        params["avatar_url"] = config.avatar
        
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
        let uri = "cloud-bullet-game/api/live-data/games/\(gameId)/mode/cloud/rooms/\(roomId):start"
        outsideGameHttpRequest(uri: uri, httpMethod: "POST", params: params) { result, code in
            completion?(result?["task_id"] as? String,code)
        }
    }
    
    /// 结束游戏
    /// - Parameters:
    ///   - id: 游戏id
    ///   - roomId: 房间id
    ///   - openId: openid
    ///   - taskId: taskId
    public func endGame(with id:String, roomId: String, openId: String, taskId: String, completion: ((_ code: Int?)->Void)? = nil) {
        var params = [String: Any]()
        params["openid"] = openId
        params["task_id"] = taskId
        let uri = "cloud-bullet-game/api/live-data/games/\(id)/mode/cloud/rooms/\(roomId):stop"
        outsideGameHttpRequest(uri: uri, httpMethod: "POST", params: params) { result, code in
            completion?(code)
        }
    }
    
    /// 查询游戏状态
    /// - Parameters:
    ///   - id: 游戏id
    ///   - taskId: taskId
    public func getGameStatus(with id:String, taskId: String,completion: ((_ status: String?)->Void)?) {
        var params = [String: Any]()
        params["taskid"] = taskId
        let uri = "cloud-bullet-game/api/live-data/tasks/\(taskId)/status"
        outsideGameHttpRequest(uri: uri, httpMethod: "GET", params: params) { result, code in
            completion?(result?["status"] as? String)
        }
    }
 
    
    /// 发送礼物
    /// - Parameters:
    ///   - gameId: 游戏id
    ///   - giftConfig: 发送礼物配置项
    ///   - completion: 成功回调
    public func sendGift(gameId: String, giftConfig: CloudGameSendGiftConfigure, completion: ((_ code: Int?)->Void)? = nil){
        guard let roomId = giftConfig.roomId else {
            assertionFailure("roomId 不能为空 ")
            return
        }
        var params = [String: Any]()
        
        var payload = [String: Any]()
        payload["msg_id"] = giftConfig.msgId
        payload["openid"] = giftConfig.openId
        payload["gift_id"] = giftConfig.giftId
        payload["gift_num"] = giftConfig.giftNum
        payload["gift_value"] = giftConfig.giftValue
        payload["avatar_url"] = giftConfig.avatar
        payload["nickname"] = giftConfig.nickname
        payload["timestamp"] = giftConfig.timestamp
        
        guard let data = try? JSONSerialization.data(withJSONObject: [payload], options: []), let jsonStr = String(data: data, encoding: .utf8) else {
            return
        }
        params["payload"] = jsonStr
        
        inGameHttpRequest(uri: "bullet-game/api/live-data/games/\(gameId)/rooms/\(appId)_\(roomId)/msgType/live_gift:push", httpMethod: "POST", params: params) { result, code in
            completion?(code)
        }
    }
    
    /// 发送评论
    /// - Parameters:
    ///   - gameId: 游戏id
    ///   - giftConfig: 发送评论参数
    ///   - completion: 成功回调
    public func sendComment(gameId: String, commentConfig: CloudGameSendCommentConfigure, completion: ((_ code: Int?)->Void)? = nil){
        guard let roomId = commentConfig.roomId else {
            assertionFailure("roomId 不能为空 ")
            return
        }
        var params = [String: Any]()
        
        var payload = [String: Any]()
        payload["msg_id"] = commentConfig.msgId
        payload["openid"] = commentConfig.openId
        payload["content"] = commentConfig.content
        payload["avatar_url"] = commentConfig.avatar
        payload["nickname"] = commentConfig.nickname
        payload["timestamp"] = commentConfig.timestamp
        
        guard let data = try? JSONSerialization.data(withJSONObject: [payload], options: []), let jsonStr = String(data: data, encoding: .utf8) else {
            return
        }
        params["payload"] = jsonStr
    
        inGameHttpRequest(uri: "bullet-game/api/live-data/games/\(gameId)/rooms/\(appId)_\(roomId)/msgType/live_comment:push", httpMethod: "POST", params: params) { result, code in
            completion?(code)
        }
    }
    
    /// 点赞
    /// - Parameters:
    ///   - gameId: 游戏id
    ///   - giftConfig: 发送评论配置项
    ///   - completion: 成功回调
    public func sendLike(gameId: String, commentConfig: CloudGameSendLikeConfigure, completion: ((_ code: Int?)->Void)? = nil){
        guard let roomId = commentConfig.roomId else {
            assertionFailure("roomId 不能为空 ")
            return
        }
        var params = [String: Any]()
        var payload = [String: Any]()
        payload["msg_id"] = commentConfig.msgId
        payload["openid"] = commentConfig.openId
        payload["like_num"] = commentConfig.likeNum
        payload["avatar_url"] = commentConfig.avatar
        payload["nickname"] = commentConfig.nickname
        payload["timestamp"] = commentConfig.timestamp
        
        guard let data = try? JSONSerialization.data(withJSONObject: [payload], options: []), let jsonStr = String(data: data, encoding: .utf8) else {
            return
        }
        params["payload"] = jsonStr
        
       
        inGameHttpRequest(uri: "bullet-game/api/live-data/games/\(gameId)/rooms/\(appId)_\(roomId)/msgType/live_like:push", httpMethod: "POST", params: params) { result, code in
            completion?(code)
        }
    }
    
    public func marshalTouchEvent(_ event: String){
        
    }
}

typealias HttpCompletion = (_ result: [String: Any]?, _ code: Int?)->Void

extension CloudGameManager {
    
    private func inGameHttpRequest(uri: String, httpMethod:String, params:[String: Any]? = nil, completion: HttpCompletion?) {
        let urlStr = "\(inGameHost)\(appId)/\(uri)"
        httpRequest(urlStr:urlStr, httpMethod: httpMethod, params: params) { dic in
            let msg = dic["err_msg"] as? [String: Any]
            let code = dic["err_no"] as? Int
            print(" response dic = \(dic)")
            DispatchQueue.main.async {
                completion?(msg,code)
            }
        }
    }
    
    /// 游戏外的接口
    private func outsideGameHttpRequest(uri: String, httpMethod:String, params:[String: Any]? = nil, completion: HttpCompletion?) {
        let urlStr = "\(outsideGameHost)\(appId)/\(uri)"
        httpRequest(urlStr:urlStr, httpMethod: httpMethod, params: params) { dic in
            let result = dic["data"] as? [String: Any]
            let code = dic["err_no"] as? Int
            let logid = dic["logid"] as? String
            print(" response dic = \(dic)")
            DispatchQueue.main.async {
                completion?(result,code)
            }
        }
    }
    
    private func httpRequest(urlStr: String, httpMethod:String, params:[String: Any]? = nil, completion: (([String: Any])->())?) {
        var url = URL(string: urlStr)!
        if let params = params {
            if httpMethod == "GET" {
                url = appendQueryParams(to: url, queryParams: params) ?? url
                print(" GET url = \(url)")
            }
        }
       
        var request = URLRequest(url: url)
        request.addValue("application/json", forHTTPHeaderField: "accept")
        request.addValue("application/json", forHTTPHeaderField: "Content-Type")
        request.addValue("agora token=\(TokenCreater.rtmToken)", forHTTPHeaderField: "Authorization")
        print(" agora token=\(TokenCreater.rtmToken)")

        request.httpMethod = httpMethod
        if let params = params {
            if httpMethod == "POST" {
                let jsonBody = try? JSONSerialization.data(withJSONObject: params)
                request.httpBody = jsonBody
                print(" POST url = \(url), params = \(params as NSDictionary)")
                print(" curl = \(request.cURL())")
            }
        }
        let task = URLSession.shared.dataTask(with: request) { data, response, error in
            guard let data = data, error == nil else {
                print("Error: \(error?.localizedDescription ?? "Unknown error")")
                return
            }
            print(" httpRequest data = \(data) ")
            if let dic = try? JSONSerialization.jsonObject(with: data) as? [String: Any] {
                completion?(dic)
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



public extension URLRequest {
    func cURL(pretty: Bool = false) -> String {
        let newLine = pretty ? "\\\n" : ""
        let method = (pretty ? "--request " : "-X ") + "\(httpMethod ?? "GET") \(newLine)"
        let url: String = (pretty ? "--url " : "") + "\'\(url?.absoluteString ?? "")\' \(newLine)"

        var cURL = "curl "
        var header = ""
        var data = ""

        if let httpHeaders = allHTTPHeaderFields, httpHeaders.keys.count > 0 {
            for (key, value) in httpHeaders {
                header += (pretty ? "--header " : "-H ") + "\'\(key): \(value)\' \(newLine)"
            }
        }

        if let bodyData = httpBody, let bodyString = String(data: bodyData, encoding: .utf8), !bodyString.isEmpty {
            data = "--data '\(bodyString)'"
        }

        cURL += method + url + header + data

        return cURL
    }
}
