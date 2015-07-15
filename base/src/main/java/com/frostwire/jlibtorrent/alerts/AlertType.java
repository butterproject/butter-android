package com.frostwire.jlibtorrent.alerts;

import com.frostwire.jlibtorrent.swig.add_torrent_alert;
import com.frostwire.jlibtorrent.swig.anonymous_mode_alert;
import com.frostwire.jlibtorrent.swig.block_downloading_alert;
import com.frostwire.jlibtorrent.swig.block_finished_alert;
import com.frostwire.jlibtorrent.swig.block_timeout_alert;
import com.frostwire.jlibtorrent.swig.cache_flushed_alert;
import com.frostwire.jlibtorrent.swig.dht_announce_alert;
import com.frostwire.jlibtorrent.swig.dht_bootstrap_alert;
import com.frostwire.jlibtorrent.swig.dht_get_peers_alert;
import com.frostwire.jlibtorrent.swig.dht_get_peers_reply_alert;
import com.frostwire.jlibtorrent.swig.dht_immutable_item_alert;
import com.frostwire.jlibtorrent.swig.dht_mutable_item_alert;
import com.frostwire.jlibtorrent.swig.dht_put_alert;
import com.frostwire.jlibtorrent.swig.dht_reply_alert;
import com.frostwire.jlibtorrent.swig.external_ip_alert;
import com.frostwire.jlibtorrent.swig.fastresume_rejected_alert;
import com.frostwire.jlibtorrent.swig.file_completed_alert;
import com.frostwire.jlibtorrent.swig.file_error_alert;
import com.frostwire.jlibtorrent.swig.file_rename_failed_alert;
import com.frostwire.jlibtorrent.swig.file_renamed_alert;
import com.frostwire.jlibtorrent.swig.hash_failed_alert;
import com.frostwire.jlibtorrent.swig.i2p_alert;
import com.frostwire.jlibtorrent.swig.invalid_request_alert;
import com.frostwire.jlibtorrent.swig.listen_failed_alert;
import com.frostwire.jlibtorrent.swig.listen_succeeded_alert;
import com.frostwire.jlibtorrent.swig.lsd_peer_alert;
import com.frostwire.jlibtorrent.swig.metadata_failed_alert;
import com.frostwire.jlibtorrent.swig.metadata_received_alert;
import com.frostwire.jlibtorrent.swig.peer_ban_alert;
import com.frostwire.jlibtorrent.swig.peer_blocked_alert;
import com.frostwire.jlibtorrent.swig.peer_connect_alert;
import com.frostwire.jlibtorrent.swig.peer_disconnected_alert;
import com.frostwire.jlibtorrent.swig.peer_error_alert;
import com.frostwire.jlibtorrent.swig.peer_snubbed_alert;
import com.frostwire.jlibtorrent.swig.peer_unsnubbed_alert;
import com.frostwire.jlibtorrent.swig.performance_alert;
import com.frostwire.jlibtorrent.swig.piece_finished_alert;
import com.frostwire.jlibtorrent.swig.portmap_alert;
import com.frostwire.jlibtorrent.swig.portmap_error_alert;
import com.frostwire.jlibtorrent.swig.portmap_log_alert;
import com.frostwire.jlibtorrent.swig.read_piece_alert;
import com.frostwire.jlibtorrent.swig.request_dropped_alert;
import com.frostwire.jlibtorrent.swig.rss_alert;
import com.frostwire.jlibtorrent.swig.rss_item_alert;
import com.frostwire.jlibtorrent.swig.save_resume_data_alert;
import com.frostwire.jlibtorrent.swig.save_resume_data_failed_alert;
import com.frostwire.jlibtorrent.swig.scrape_failed_alert;
import com.frostwire.jlibtorrent.swig.scrape_reply_alert;
import com.frostwire.jlibtorrent.swig.state_changed_alert;
import com.frostwire.jlibtorrent.swig.state_update_alert;
import com.frostwire.jlibtorrent.swig.stats_alert;
import com.frostwire.jlibtorrent.swig.storage_moved_alert;
import com.frostwire.jlibtorrent.swig.storage_moved_failed_alert;
import com.frostwire.jlibtorrent.swig.torrent_added_alert;
import com.frostwire.jlibtorrent.swig.torrent_checked_alert;
import com.frostwire.jlibtorrent.swig.torrent_delete_failed_alert;
import com.frostwire.jlibtorrent.swig.torrent_deleted_alert;
import com.frostwire.jlibtorrent.swig.torrent_error_alert;
import com.frostwire.jlibtorrent.swig.torrent_finished_alert;
import com.frostwire.jlibtorrent.swig.torrent_need_cert_alert;
import com.frostwire.jlibtorrent.swig.torrent_paused_alert;
import com.frostwire.jlibtorrent.swig.torrent_removed_alert;
import com.frostwire.jlibtorrent.swig.torrent_resumed_alert;
import com.frostwire.jlibtorrent.swig.torrent_update_alert;
import com.frostwire.jlibtorrent.swig.tracker_announce_alert;
import com.frostwire.jlibtorrent.swig.tracker_error_alert;
import com.frostwire.jlibtorrent.swig.tracker_reply_alert;
import com.frostwire.jlibtorrent.swig.tracker_warning_alert;
import com.frostwire.jlibtorrent.swig.trackerid_alert;
import com.frostwire.jlibtorrent.swig.udp_error_alert;
import com.frostwire.jlibtorrent.swig.unwanted_block_alert;
import com.frostwire.jlibtorrent.swig.url_seed_alert;

/**
 * @author gubatron
 * @author aldenml
 */
public enum AlertType {

    TORRENT_ADDED(torrent_added_alert.alert_type),
    TORRENT_FINISHED(torrent_finished_alert.alert_type),
    TORRENT_REMOVED(torrent_removed_alert.alert_type),
    TORRENT_UPDATE(torrent_update_alert.alert_type),
    TORRENT_DELETED(torrent_deleted_alert.alert_type),
    TORRENT_PAUSED(torrent_paused_alert.alert_type),
    TORRENT_RESUMED(torrent_resumed_alert.alert_type),
    TORRENT_CHECKED(torrent_checked_alert.alert_type),
    TORRENT_NEED_CERT(torrent_need_cert_alert.alert_type),
    TORRENT_ERROR(torrent_error_alert.alert_type),
    ADD_TORRENT(add_torrent_alert.alert_type),
    SAVE_RESUME_DATA(save_resume_data_alert.alert_type),
    FASTRESUME_REJECTED(fastresume_rejected_alert.alert_type),
    BLOCK_FINISHED(block_finished_alert.alert_type),
    METADATA_RECEIVED(metadata_received_alert.alert_type),
    METADATA_FAILED(metadata_failed_alert.alert_type),
    FILE_COMPLETED(file_completed_alert.alert_type),
    FILE_RENAMED(file_renamed_alert.alert_type),
    FILE_RENAME_FAILED(file_rename_failed_alert.alert_type),
    FILE_ERROR(file_error_alert.alert_type),
    HASH_FAILED(hash_failed_alert.alert_type),
    PORTMAP(portmap_alert.alert_type),
    PORTMAP_ERROR(portmap_error_alert.alert_type),
    PORTMAP_LOG(portmap_log_alert.alert_type),
    TRACKER_ANNOUNCE(tracker_announce_alert.alert_type),
    TRACKER_REPLY(tracker_reply_alert.alert_type),
    TRACKER_WARNING(tracker_warning_alert.alert_type),
    TRACKER_ERROR(tracker_error_alert.alert_type),
    READ_PIECE(read_piece_alert.alert_type),
    STATE_CHANGED(state_changed_alert.alert_type),
    DHT_REPLY(dht_reply_alert.alert_type),
    DHT_BOOTSTRAP(dht_bootstrap_alert.alert_type),
    DHT_GET_PEERS(dht_get_peers_alert.alert_type),
    EXTERNAL_IP(external_ip_alert.alert_type),
    LISTEN_SUCCEEDED(listen_succeeded_alert.alert_type),
    STATE_UPDATE(state_update_alert.alert_type),
    SCRAPE_REPLY(scrape_reply_alert.alert_type),
    SCRAPE_FAILED(scrape_failed_alert.alert_type),
    LSD_PEER(lsd_peer_alert.alert_type),
    PEER_BLOCKED(peer_blocked_alert.alert_type),
    PERFORMANCE(performance_alert.alert_type),
    PIECE_FINISHED(piece_finished_alert.alert_type),
    SAVE_RESUME_DATA_FAILED(save_resume_data_failed_alert.alert_type),
    STATS(stats_alert.alert_type),
    STORAGE_MOVED(storage_moved_alert.alert_type),
    TORRENT_DELETE_FAILED(torrent_delete_failed_alert.alert_type),
    URL_SEED(url_seed_alert.alert_type),
    INVALID_REQUEST(invalid_request_alert.alert_type),
    LISTEN_FAILED(listen_failed_alert.alert_type),
    PEER_BAN(peer_ban_alert.alert_type),
    PEER_CONNECT(peer_connect_alert.alert_type),
    PEER_DISCONNECTED(peer_disconnected_alert.alert_type),
    PEER_ERROR(peer_error_alert.alert_type),
    PEER_SNUBBED(peer_snubbed_alert.alert_type),
    PEER_UNSNUBBED(peer_unsnubbed_alert.alert_type),
    REQUEST_DROPPED(request_dropped_alert.alert_type),
    UDP_ERROR(udp_error_alert.alert_type),
    ANONYMOUS_MODE(anonymous_mode_alert.alert_type),
    BLOCK_DOWNLOADING(block_downloading_alert.alert_type),
    BLOCK_TIMEOUT(block_timeout_alert.alert_type),
    CACHE_FLUSHED(cache_flushed_alert.alert_type),
    DHT_ANNOUNCE(dht_announce_alert.alert_type),
    I2P(i2p_alert.alert_type),
    RSS(rss_alert.alert_type),
    RSS_ITEM(rss_item_alert.alert_type),
    STORAGE_MOVED_FAILED(storage_moved_failed_alert.alert_type),
    TRACKERID(trackerid_alert.alert_type),
    UNWANTED_BLOCK(unwanted_block_alert.alert_type),
    DHT_PUT(dht_put_alert.alert_type),
    DHT_MUTABLE_ITEM(dht_mutable_item_alert.alert_type),
    DHT_IMMUTABLE_ITEM(dht_immutable_item_alert.alert_type),
    DHT_GET_PEERS_REPLY_ALERT(dht_get_peers_reply_alert.alert_type),
    UNKNOWN(-1),
    TORRENT_PRIORITIZE(-2);

    private AlertType(int swigValue) {
        this.swigValue = swigValue;
    }

    private final int swigValue;

    public int getSwig() {
        return swigValue;
    }

    public static AlertType fromSwig(int swigValue) {
        AlertType[] enumValues = AlertType.class.getEnumConstants();
        for (AlertType ev : enumValues) {
            if (ev.getSwig() == swigValue) {
                return ev;
            }
        }
        return UNKNOWN;
    }
}
