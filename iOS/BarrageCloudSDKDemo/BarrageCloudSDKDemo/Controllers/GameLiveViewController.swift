//
//  GameLiveViewController.swift
//  BarrageCloudSDKDemo
//
//  Created by FanPengpeng on 2023/9/20.
//

import UIKit
import AgoraRtcKit
import CloudGameFramework
import RTMTokenBuilder
import SVProgressHUD
import IQKeyboardManager

private let showGiftSegueId = "showGiftList"
private let showGuideSegueId = "showGuide"

private let kBroadcastorUid: UInt = 123456
private let kAssistantUid = "456789"
private let kBroadcasterOpenId = "abcd"

class GameLiveViewController: UIViewController {
    
    var roomId: String!
//    var token: String?
    
    var game: CloudGameBaseInfo?
    var gameInfo: CloudGameDetailInfo?
    var role: AgoraClientRole = .broadcaster {
        didSet{
            // 主播的uid固定 实际开发中应该由服务端确定
            let isBroadcaster = role == .broadcaster
            myUid = isBroadcaster ? kBroadcastorUid : RandomIdCreator.RTC_UID
            openId = isBroadcaster ? kBroadcasterOpenId : RandomIdCreator.OPEN_ID
        }
    }
    
    private var assistantUid = kAssistantUid
    private var openId: String!
    
    private var startDate: Date?
    private var startCostTime = 0 {
        didSet {
            updateLogText()
        }
    }
    
    private var gameStatus: CloudGameStatus? {
        didSet{
            updateLogText()
            self.startButton.isHidden = gameStatus == .started
            if gameStatus == .stopped {
                self.taskId = nil
            }
        }
    }
    
    private var taskId: String? {
        didSet{
            UserDefaults.standard.set(taskId, forKey: cachedTaskIdKey)
            print("cachedTaskIdKey taskId = \(taskId ?? "nil")")
            if taskId != nil {
                startButton.isEnabled = true
                startButton.isSelected = true
                startQueryGameStatus()
            }else{
                startButton.isEnabled = true
                startButton.isSelected = false
                stopQueryGameStatus()
            }
        }
    }
    
    private var cachedTaskIdKey: String {
        get{
            "taskId_\(roomId!)"
        }
    }
    
    private var timer: Timer?
    
    private var streamId: Int = 0
    
    private var msgesArray = [Agora_Pb_Rctrl_RctrlMsg]()
    
    private var myUid: UInt!
    
    @IBOutlet weak var commentButton: UIButton!
    @IBOutlet weak var likeButton: UIButton!
    @IBOutlet weak var zButton: UIButton!
    @IBOutlet weak var startButton: UIButton!
    @IBOutlet weak var gameView: GameView!
    @IBOutlet weak var commentTextField: UITextField!
    
    @IBOutlet weak var gameStatusLabel: UILabel!
    
    @IBOutlet weak var gameViewHeightCon: NSLayoutConstraint!
    @IBOutlet weak var inputViewBottomCon: NSLayoutConstraint!
    
    @IBOutlet weak var gameViewWidthCon: NSLayoutConstraint!
    
    @IBOutlet weak var inputBar: UIView!
    
    private lazy var rtcEngineConfig: AgoraRtcEngineConfig = {
       let config = AgoraRtcEngineConfig()
        config.appId = KeyCenter.AppId
        config.channelProfile = .liveBroadcasting
        config.areaCode = .global
        return config
    }()
    
    fileprivate (set) lazy var agoraKit: AgoraRtcEngineKit = {
        let kit = AgoraRtcEngineKit.sharedEngine(with: rtcEngineConfig, delegate: self)
        return kit
    }()
    
    deinit {
        // 移除键盘高度变化的通知
        NotificationCenter.default.removeObserver(self, name: UIResponder.keyboardWillChangeFrameNotification, object: nil)
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        title = "\(roomId!),\(myUid!)"
        let dic = UserDefaults.standard.dictionaryRepresentation()
        dic.forEach { key, value in
            print("key = \(key), value = \(value)")
        }
        if let gameId = game?.gameId {
            CloudGameManager.shared.getGameInformation(with: gameId) { [weak self] gameInfo, code in
                self?.gameInfo = gameInfo
                self?.updateUI()
                if let key = self?.cachedTaskIdKey, let taskId = UserDefaults.standard.string(forKey: key) {
                    self?.taskId = taskId
                }
                if code != 0 {
                    SVProgressHUD.showError(withStatus: "获取游戏详情失败：\(String(describing: code))")
                }
            }
        }
       
        // 只有主播才需要发dataStream和操作游戏的手势
        if role == .broadcaster {
            createStream()
            addGameGesture()
        }
        
        joinChannel()
        addObserver()
        SVProgressHUD.setMinimumDismissTimeInterval(1)
    }
    
    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        IQKeyboardManager.shared().isEnabled = false
    }
    
    func updateUI(){
        self.startButton.isHidden = role == .audience
        self.zButton.isHidden = role == .audience
        self.likeButton.isHidden = self.gameInfo?.feature?.like != 1
        self.commentButton.isHidden = self.gameInfo?.feature?.comment != 1
        self.gameStatusLabel.isHidden = role == .audience
    }
    
    func addControllView(){
        let gameControllerView = GameControllerView(frame: CGRect(x: 100, y: 450, width: 200, height: 200))
        gameControllerView.delegate = self
        view.addSubview(gameControllerView)
    }
    
    func addGameGesture() {
        gameView.touchDelegate = self
    }
    
    func addObserver(){
        NotificationCenter.default.addObserver(self, selector: #selector(keyboardWillChange(notification:)), name: UIResponder.keyboardWillChangeFrameNotification, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(keyboardWillHide(notification:)), name: UIResponder.keyboardWillHideNotification, object: nil)
    }
    
    @objc func keyboardWillChange(notification: NSNotification) {
        guard let userInfo = notification.userInfo else { return }
        
        // 获取键盘高度
        let keyboardFrame = (userInfo[UIResponder.keyboardFrameEndUserInfoKey] as! NSValue).cgRectValue
        let keyboardHeight = keyboardFrame.height
        
        let window = UIApplication.shared.windows.first
        let safeBottomInset = window?.safeAreaInsets.bottom ?? 0.0
        
        // 处理键盘高度变化
        inputViewBottomCon.constant = max(keyboardHeight - safeBottomInset, 150)
    }
    
    @objc func keyboardWillHide(notification: NSNotification) {
        // 处理键盘高度变化
        inputViewBottomCon.constant = 0
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.3) {
            self.inputBar.isHidden = true
        }
    }
    
    @IBAction func didClickExitButton(_ sender: UIBarButtonItem) {
        navigationController?.popViewController(animated: true)
        agoraKit.leaveChannel()
        if let gameId = game?.gameId, role == .broadcaster {
           stopGame(gameId: gameId)
        }
        taskId = nil
    }
    
    @IBAction func didClickStartButton(_ sender: UIButton) {
        guard let gameId = game?.gameId, role == .broadcaster else {
            return
        }
        if sender.isSelected {
           stopGame(gameId: gameId)
        }else {
           startGame(gameId: gameId, assistantUid: assistantUid)
        }
    }
    
    // 点击发送按钮
    @IBAction func didClickSendButton(_ sender: Any) {
        commentTextField.resignFirstResponder()
        guard let gameId = game?.gameId else {return}
        if let text = commentTextField.text?.trimmingCharacters(in: .whitespacesAndNewlines), text.count > 0 {
            let config = CloudGameSendCommentConfigure()
            config.roomId = roomId
            config.openId = openId
            config.nickname = "user_\(myUid!)"
            config.avatar = "/avatar.png"
            config.content = text
            config.timestamp = Int(Date().timeIntervalSince1970 * 1000)
            config.msgId = "\(config.timestamp!)_\(arc4random()%10000)"
            CloudGameManager.shared.sendComment(gameId: gameId, commentConfig: config) {[weak self] code in
                if code == 0 {
                    self?.commentTextField.text = nil
                    SVProgressHUD.showSuccess(withStatus: "发送评论成功")
                }else{
                    SVProgressHUD.showError(withStatus: "发送评论失败：\(String(describing: code))")
                }
            }
        }
    }
    
    @IBAction func didClickLikeButton(_ sender: UIButton) {
        guard let gameId = game?.gameId else {return}
        let config = CloudGameSendLikeConfigure()
        config.roomId = roomId
        config.openId = openId
        config.nickname = "user_\(myUid!)"
        config.avatar = "/avatar.png"
        config.likeNum = 1
        config.timestamp = Int(Date().timeIntervalSince1970 * 1000)
        config.msgId = "\(config.timestamp!)_\(arc4random()%10000)"
        CloudGameManager.shared.sendLike(gameId: gameId, commentConfig: config) { code in
            if code == 0 {
                SVProgressHUD.showSuccess(withStatus: "点赞成功")
            }else{
                SVProgressHUD.showError(withStatus: "点赞失败：\(String(describing: code))")
            }
        }
    }
    
    @IBAction func didClickCommentButton(_ sender: Any) {
        self.inputBar.isHidden = false
        self.commentTextField.becomeFirstResponder()
    }
    
    
    @IBAction func didTouchDownZbutton(_ sender: UIButton) {
        sendKeyboardEvent(type: .keyboardEventKeyDown, key: "Z")
    }
    
    @IBAction func didTouchUpZbutton(_ sender: UIButton) {
        sendKeyboardEvent(type: .keyboardEventKeyUp, key: "Z")
    }
    
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        if segue.identifier == showGiftSegueId {
            let vc = segue.destination as? GameGiftsViewController
            if let gifts = gameInfo?.gifts, let gameId = game?.gameId {
                vc?.giftList = gifts
                vc?.whenSendGift({[weak self] gift, count in
                    guard let self = self else { return }
                    let config = CloudGameSendGiftConfigure()
                    config.openId = openId
                    config.nickname = "user_\(myUid!)"
                    config.roomId = self.roomId
                    config.giftId = gift?.vendorGiftId
                    config.avatar = "/avatar.png"
                    config.giftNum = count
                    config.giftValue = (gift?.price ?? 0) * count
                    config.timestamp = Int(Date().timeIntervalSince1970 * 1000)
                    config.msgId = "\(config.timestamp!)_\(arc4random()%10000)"
                    CloudGameManager.shared.sendGift(gameId: gameId, giftConfig: config) { code in
                        if code == 0 {
                            SVProgressHUD.showSuccess(withStatus: "发送礼物成功")
                        }else{
                            SVProgressHUD.showError(withStatus: "发送礼物失败：\(String(describing: code))")
                        }
                    }
                })
            }
        }
        
        if segue.identifier == showGuideSegueId {
            let vc = segue.destination as? GameGuideViewController
            if let introduce = gameInfo?.introduce {
                vc?.guideText = introduce
            }
        }
    }
}

extension GameLiveViewController {
    // 加入主频道
    func joinChannel() {
        let token = TokenCreater.createRctToken(uid: Int32(myUid!), channelName: roomId, role: Int32(role.rawValue))
        let option = AgoraRtcChannelMediaOptions()
        option.clientRoleType = role
        option.autoSubscribeVideo = true
        let ret = agoraKit.joinChannel(byToken: token, channelId: roomId, uid: myUid, mediaOptions: option)
        print(" roomid = \(roomId!), uid = \(myUid!) join ret = \(ret)")
    }
    
    func setupRemoteView(uid: UInt, canvasView: UIView) {
        let videoCanvas = AgoraRtcVideoCanvas()
        videoCanvas.uid = uid
        videoCanvas.view = canvasView
        videoCanvas.renderMode = .fit
        agoraKit.setupRemoteVideo(videoCanvas)
    }
    
    func startGame(gameId: String,assistantUid: String ){
        startButton.isEnabled = false
        if let taskId = UserDefaults.standard.string(forKey: cachedTaskIdKey) {
            self.taskId = taskId
        }else {
            let uid = Int32(assistantUid) ?? 0
            let token = TokenCreater.createRctToken(uid: Int32(assistantUid) ?? 0, channelName: roomId, role: Int32(1))
            print("create token uid = \(uid), channel = \(roomId!), token = \(token)")
            let config = CloudGameStartConfigure()
            config.roomId = roomId
            config.openId = openId
            config.nickname = "user_\(myUid!)"
            config.avatar = ""
            config.assistantUid = UInt(assistantUid) ?? 0
            config.token = token
            config.channelName = roomId
            config.mode = 0
            config.secret = ""
            config.salt = ""
            config.broadcastUid = myUid
            startDate = Date()
            CloudGameManager.shared.startGame(with: gameId, config: config) {[weak self] taskId, code in
                self?.taskId = taskId
                if code == 0 {
                    SVProgressHUD.showSuccess(withStatus: "开始游戏成功")
                }else{
                    SVProgressHUD.showError(withStatus: "开始游戏失败：\(String(describing: code))")
                }
            }
        }
    }
    
    func stopGame(gameId: String){
        if let taskId = taskId {
            startButton.isEnabled = false
            CloudGameManager.shared.endGame(with: gameId, roomId: roomId, openId: openId, taskId: taskId) {[weak self] code in
                self?.taskId = nil
                if code == 0 {
                    SVProgressHUD.showSuccess(withStatus: "结束游戏成功")
                }else{
                    SVProgressHUD.showError(withStatus: "结束游戏失败：\(String(describing: code))")
                }
            }
        }
    }
    
    func updateLogText(){
        gameStatusLabel.text = "游戏状态：\(gameStatus?.rawValue ?? "") \n开始耗时：\(startCostTime)ms"
    }
}

extension GameLiveViewController: GameControllerViewDelegate {
    func onDirectionChanged(_ direction: Direction) {
        switch direction {
        case .up:
            print("向上")
        case .down:
            print("向下")
        case .left:
            print("向左")
        case .right:
            print("向右")
        case .idle:
            print("停止")
        }
    }
    
    func onJoystickMoved(distance: CGFloat, angle: CGFloat) {
        print(" distance: \(distance), angle = \(angle)")
    }
}

extension GameLiveViewController: AgoraRtcEngineDelegate {
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, didJoinedOfUid uid: UInt, elapsed: Int) {
        if "\(uid)" == self.assistantUid {
            setupRemoteView(uid: uid, canvasView: gameView)
        }
        print("didJoinedOfUid uid == \(uid)")
    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, didJoinChannel channel: String, withUid uid: UInt, elapsed: Int) {
        print(" roomid = \(channel) didJoinChannel  uid = \(uid)")
    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, firstRemoteVideoFrameOfUid uid: UInt, size: CGSize, elapsed: Int) {
        if "\(uid)" == assistantUid {
            print("size = \(size)")
            gameViewHeightCon.constant = size.height / size.width * view.bounds.width
            print("gameView.bounds = \(gameView.bounds), gameView.frame = \(gameView.frame), gameViewHeightCon.constant  = \(gameViewHeightCon.constant)")
            if let startDate = startDate {
                startCostTime = Int(Date().timeIntervalSince(startDate) * 1000)
            }
        }
    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, remoteVideoStats stats: AgoraRtcRemoteVideoStats) {
//        gameViewHeightCon.constant = CGFloat(stats.height) / CGFloat(stats.width) * view.bounds.width
        let width: CGFloat = CGFloat(stats.width)
        let height: CGFloat = CGFloat(stats.height)
        let estimateHeight = height / width * view.bounds.width
        if (gameView.frame.origin.y + estimateHeight) <= CGRectGetMaxY(view.frame) {
            gameViewHeightCon.constant = estimateHeight
            print(" ===== 没有超出屏幕高度 ")
        }else{
            print(" ===== 超出屏幕高度 ")
            gameViewHeightCon.constant = CGRectGetMaxY(view.bounds) - gameView.frame.origin.y
            gameViewWidthCon.constant = view.bounds.width - width * (gameViewHeightCon.constant / height)
        }
        print("gameView.bounds = \(gameView.bounds), gameView.frame = \(gameView.frame), gameViewHeightCon.constant  = \(gameViewHeightCon.constant)")
    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, didOccurError errorCode: AgoraErrorCode) {
        print("didOccurError errorCode = \(errorCode.rawValue)")
        // 加入频道失败自动停止任务
        if let gameId = game?.gameId {
            stopGame(gameId: gameId)
        }
    }
}

extension GameLiveViewController {
    // 轮询游戏状态
    func startQueryGameStatus() {
        timer = Timer.scheduledTimer(withTimeInterval: 10, repeats: true) { [weak self] t in
            self?.queryGameStatus()
        }
        timer?.fire()
    }
    
    func queryGameStatus(){
        guard let gameId = self.game?.gameId else {return}
        guard let taskId = taskId else { return }
        CloudGameManager.shared.getGameStatus(with: gameId, taskId: taskId) {[weak self] status in
            if let status = status {
                self?.gameStatus = CloudGameStatus(rawValue: status)
            }
            print(" game status == \(String(describing: status))")
        }
    }
    
    func stopQueryGameStatus(){
        timer?.invalidate()
        timer = nil
    }
}

extension GameLiveViewController {
    
    func createStream() {
        let config = AgoraDataStreamConfig()
        config.ordered = true
        config.syncWithAudio = true
        let ret = agoraKit.createDataStream(&streamId, config: config)
        print(" createStream ret = \(ret), streamId = \(streamId)")
    }
    
    func sendStreamMessage(_ data: Data){
        let ret = agoraKit.sendStreamMessage(streamId, data: data)
        print(" sendStreamMessage ret = \(ret) streamId = \(streamId)")
    }
    
    func sendMouseEvent(type: Agora_Pb_Rctrl_MouseEventType, point: CGPoint) {
        
        guard let data = CloudGameMsgHandler.shared.createMouseEvent(type: type, point: point, gameViewSize: gameView.bounds.size) else { return }
        sendStreamMessage(data)
    }
    
    func sendKeyboardEvent(type: Agora_Pb_Rctrl_KeyboardEventType, key:Character){
        guard let data = CloudGameMsgHandler.shared.createKeyboardEvent(type: type, key: key) else { return }
        sendStreamMessage(data)
    }
}

extension GameLiveViewController: GameViewTouchDelegate {
   
    func touchUp(_ view: GameView, point: CGPoint) {
        sendMouseEvent(type: .mouseEventLbuttonUp, point: point)
    }
    
    func touchDown(_ view: GameView, point: CGPoint) {
        sendMouseEvent(type: .mouseEventLbuttonDown, point: point)

    }
}
