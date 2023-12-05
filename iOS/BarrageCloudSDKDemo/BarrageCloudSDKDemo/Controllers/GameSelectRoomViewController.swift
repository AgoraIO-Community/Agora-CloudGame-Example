//
//  GameSelectRoomViewController.swift
//  BarrageCloudSDKDemo
//
//  Created by FanPengpeng on 2023/9/21.
//

import UIKit
import CloudGameFramework
import AgoraRtcKit

private let joinRoomSegueId = "joinRoom"

class GameSelectRoomViewController: UIViewController {

    @IBOutlet weak var roomIdTF: UITextField!
    
    @IBOutlet weak var assistantTokenTF: UITextField!
    
    @IBOutlet weak var roleSegmentControl: UISegmentedControl!
    
    var game: CloudGameBaseInfo?
    
    override func viewDidLoad() {
        super.viewDidLoad()
    }
    
    // MARK: - Navigation

    // In a storyboard-based application, you will often want to do a little preparation before navigation
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        if segue.identifier == joinRoomSegueId {
            let vc = segue.destination as? GameLiveViewController
            if !textFiledTextIsEmpty(roomIdTF) {
                vc?.roomId = roomIdTF.text
            }
            
            let role: AgoraClientRole = roleSegmentControl.selectedSegmentIndex == 0 ? .broadcaster : .audience
            vc?.game = game
            vc?.role = role
        }
    }
    
    override func shouldPerformSegue(withIdentifier identifier: String, sender: Any?) -> Bool {
        if identifier == joinRoomSegueId {
            view.endEditing(true)
            return !textFiledTextIsEmpty(roomIdTF)
        }
        return true
    }
    
    func textFiledTextIsEmpty(_ tf: UITextField) -> Bool {
        let text = tf.text?.trimmingCharacters(in: .whitespacesAndNewlines)
        return text?.isEmpty ?? true
    }

    @IBAction func segmentControlValueChanged(_ sender: UISegmentedControl) {
//        let isBroadcastor = sender.selectedSegmentIndex == 0
//        assistantUidTF.isHidden = !isBroadcastor
//        assistantTokenTF.isHidden = !isBroadcastor
    }
}
