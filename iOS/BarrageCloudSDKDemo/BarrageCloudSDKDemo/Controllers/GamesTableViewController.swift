//
//  GamesTableViewController.swift
//  BarrageCloudSDKDemo
//
//  Created by FanPengpeng on 2023/9/20.
//

import UIKit
import CloudGameFramework
import Kingfisher

private let showRoomVCSegueId = "showRoomVC"

class GamesTableViewController: UITableViewController {
    
    private var gameList: [CloudGameBaseInfo]?
    
    private var selectedGame: CloudGameBaseInfo?

    override func viewDidLoad() {
        super.viewDidLoad()

        let manager = CloudGameManager.shared
        manager.configCloudService(appId: KeyCenter.AppId, host: KeyCenter.Host)
        manager.delegate = self
        manager.getGames()
    }

    // MARK: - Table view data source

    override func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return gameList?.count ?? 0
    }

    override func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell: GameTableViewCell = tableView.dequeueReusableCell(withIdentifier: "gameCell", for: indexPath) as! GameTableViewCell
        let game = gameList?[indexPath.row]
        cell.titleLabel.text = game?.name
        cell.introduceLabel.text = game?.introduce
        cell.iconImageView.kf.setImage(with: URL(string: "https://img1.baidu.com/it/u=3539595421,754041626&fm=253&app=138&size=w931&n=0&f=JPEG&fmt=auto?sec=1695315600&t=74a72267e2523915820887087eb265d7"))
        return cell
    }

    override func tableView(_ tableView: UITableView, willSelectRowAt indexPath: IndexPath) -> IndexPath? {
        self.selectedGame = gameList?[indexPath.row]
        return indexPath
    }
    
    override func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        tableView.deselectRow(at: indexPath, animated: true)
    }
    
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        if segue.identifier == showRoomVCSegueId {
            if let destinationVC = segue.destination as? GameSelectRoomViewController {
                destinationVC.game = selectedGame
            }
        }
    }
}

extension GamesTableViewController: ICloudGameEventHandler {
    
    func onServiceInitEvent() {
        
    }
    
    func onGamesResults(_ games: [CloudGameBaseInfo]) {
        self.gameList = games
        tableView.reloadData()
    }
    
    func onGameInformationResult(_ game: CloudGameDetailInfo) {
        
    }
    
    func onGameState(_ state: CloudGameState, message: String) {
        
    }
    
}
