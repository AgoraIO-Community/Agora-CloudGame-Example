//
//  GameGuideViewController.swift
//  BarrageCloudSDKDemo
//
//  Created by FanPengpeng on 2023/10/19.
//

import UIKit

class GameGuideViewController: UIViewController {

    var guideText: String = ""
    
    @IBOutlet weak var textView: UITextView!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        textView.isEditable = false
        textView.text = guideText
    }

    override func touchesEnded(_ touches: Set<UITouch>, with event: UIEvent?) {
        dismiss(animated: true)
    }
}
