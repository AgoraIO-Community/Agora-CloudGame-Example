//
//  GameGiftsViewController.swift
//  BarrageCloudSDKDemo
//
//  Created by FanPengpeng on 2023/9/21.
//

import UIKit
import CloudGameFramework
import IQKeyboardManager

class GameGiftsViewController: UIViewController {

    var giftList = [CloudGameGift]()
    
    private var sendGiftAction: ((_ gift: CloudGameGift?,_ count: Int)->Void)?
    
    private var selectedGift: CloudGameGift?
    @IBOutlet weak var countTF: UITextField!
    
    @IBOutlet weak var collectionView: UICollectionView!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        collectionView.delegate = self
        collectionView.dataSource = self
        IQKeyboardManager.shared().isEnabled = true
    }
    
    override func viewDidDisappear(_ animated: Bool) {
        super.viewDidDisappear(animated)
        IQKeyboardManager.shared().isEnabled = false
    }
    
    override func touchesEnded(_ touches: Set<UITouch>, with event: UIEvent?) {
        dismiss(animated: true)
    }

    @IBAction func didClickSendButton(_ sender: UIButton) {
        let count = Int(countTF.text ?? "1") ?? 1
        sendGiftAction?(self.selectedGift,count)
        dismiss(animated: true)
    }

    func whenSendGift(_ action:((_ gift: CloudGameGift?, _ count: Int)->Void)?) {
        self.sendGiftAction = action
    }

}


extension GameGiftsViewController: UICollectionViewDataSource, UICollectionViewDelegateFlowLayout {
    
    func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        return giftList.count
    }
    
    func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
        let cell = collectionView.dequeueReusableCell(withReuseIdentifier: "giftCell", for: indexPath) as! GameGiftCell
        let gift = giftList[indexPath.item]
        cell.gift = gift
        return cell
    }
    
    func collectionView(_ collectionView: UICollectionView, didSelectItemAt indexPath: IndexPath) {
        self.selectedGift = giftList[indexPath.item]
    }
    
    func collectionView(_ collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, sizeForItemAt indexPath: IndexPath) -> CGSize {
        return CGSize(width: 100, height: 128)
    }
    
    func collectionView(_ collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, insetForSectionAt section: Int) -> UIEdgeInsets {
        return UIEdgeInsets(top: 20, left: 20, bottom: 20, right: 20)
    }
    
    func collectionView(_ collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, minimumInteritemSpacingForSectionAt section: Int) -> CGFloat {
        return 10
    }
    
}
