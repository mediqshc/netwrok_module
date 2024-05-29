package com.homemedics.app.twilio.call

import android.content.Context
import android.util.Log
import android.view.View
import com.twilio.video.*
import tvi.webrtc.VideoSink

class TwilioVideoUtils(var context: Context) {
    private val tag = this::class.simpleName

    //connection
    private var accessToken = ""
    var room: Room? = null
        get() {return field}
        private set

    // Create an audio track
    private var localAudioTrack: LocalAudioTrack? = null

    //video
    private lateinit var localVideoSink: VideoSink
    var localVideoTrack: LocalVideoTrack? = null
    private lateinit var cameraCapturerCompat: CameraCapturerCompat

    //views
    private lateinit var localVideoView: VideoTextureView
//    private lateinit var remoteVideoView: VideoView
    private lateinit var remoteVideoView1: VideoView
    private lateinit var remoteVideoView2: VideoView

    //encoding config
    private val encodingParameters: EncodingParameters
        get() {
            val maxAudioBitrate = 0
            val maxVideoBitrate = 0

            return EncodingParameters(maxAudioBitrate, maxVideoBitrate)
        }

    val connectionState: Room.State?
        get() {
            return if(room == null) null
            else room?.state
        }

    var remoteVideoViewList: ArrayList<RemoteViewAndParticipant> = arrayListOf()

    private fun initCameraCapturerCompat(){
        cameraCapturerCompat = CameraCapturerCompat(context, CameraCapturerCompat.Source.FRONT_CAMERA)
    }

    fun connectToRoom(roomName: String, accessToken: String, roomListener: Room.Listener) {
        val connectOptions =
            ConnectOptions.Builder(accessToken)
                .roomName(roomName)
                .audioTracks(listOf(localAudioTrack))
                .videoTracks(if(localVideoTrack != null) listOf(localVideoTrack) else arrayListOf())
                .preferAudioCodecs(listOf(OpusCodec()))
                .preferVideoCodecs(listOf(Vp8Codec()))
                .enableNetworkQuality(true)
                .encodingParameters(encodingParameters)
                .networkQualityConfiguration(getNetworkQualityConfig())
                .build()


        room = Video.connect(context, connectOptions, roomListener)
    }

    fun releaseStream(){
        localVideoTrack?.let { room?.localParticipant?.unpublishTrack(it) }
        localVideoTrack?.release()
        localVideoTrack = null
    }

    fun disconnect(){
        room?.disconnect()
    }

    fun isConnected(): Boolean{
        return connectionState == Room.State.CONNECTED
    }

    fun createAudioAndVideoTracks(localVideoView: VideoTextureView, remoteVideoView: VideoView) {
        this.localVideoView = localVideoView
//        this.remoteVideoView = remoteVideoView
        initCameraCapturerCompat()
    }

    fun initStream(localAudioEnable: Boolean, localVideoEnable: Boolean){
        initLocalAudioTrack(localAudioEnable)
        initLocalVideoTrack(localVideoEnable)
    }

    fun setLocalParticipantListener(listener: LocalParticipant.Listener){
        room?.localParticipant?.apply {
            setListener(listener)
        }
    }

    fun initLocalAudioTrack(localAudioEnable: Boolean){
        if(localAudioTrack == null) {
            localAudioTrack = LocalAudioTrack.create(context, localAudioEnable)
            publishLocalAudioTrack()
        }
    }

    fun initLocalVideoTrack(localVideoEnable: Boolean){
        if(localVideoTrack == null){
            if(::cameraCapturerCompat.isInitialized )
            localVideoTrack = LocalVideoTrack.create(context, localVideoEnable, cameraCapturerCompat, "camera")
        }

        localVideoView.mirror = true
        localVideoTrack?.addSink(localVideoView)
        room?.localParticipant?.setEncodingParameters(encodingParameters)

        room?.let {
            Log.d(tag, "Connected to ${it.name}")
        }
    }

    fun publishLocalAudioTrack(){
        localAudioTrack?.let{
            room?.localParticipant?.publishTrack(it)
        }
    }

    fun publishLocalVideoTrack(){
        localVideoTrack?.let { room?.localParticipant?.publishTrack(it) }
    }

    private fun getNetworkQualityConfig(): NetworkQualityConfiguration{
        val networkConfig = NetworkQualityConfiguration(
            NetworkQualityVerbosity.NETWORK_QUALITY_VERBOSITY_MINIMAL,
            NetworkQualityVerbosity.NETWORK_QUALITY_VERBOSITY_MINIMAL
        )

        return networkConfig
    }

    fun getConnectedParticipants(): List<RemoteParticipant>{
        room?.let {
            if(BuildConfig.DEBUG){
                it.remoteParticipants.forEach {
                        participant ->
                    Log.d(tag, "HandleParticipants: " + participant.identity + " is in the room.")
                }
            }
        }

        return room?.remoteParticipants ?: arrayListOf()
    }

    fun addRemoteParticipant(participant: RemoteParticipant,
                                     remoteParticipantListener: RemoteParticipant.Listener? = null){
        participant.remoteVideoTracks.firstOrNull()?.let { remoteVideoTrackPublication ->
            if (remoteVideoTrackPublication.isTrackSubscribed) {
                remoteVideoTrackPublication.remoteVideoTrack?.let { addRemoteParticipantVideo(participant, it) }
            }
        }
        participant.setListener(remoteParticipantListener)
    }

    fun removeRemoteParticipant(participant: RemoteParticipant){
        participant.remoteVideoTracks.firstOrNull()?.let { remoteVideoTrackPublication ->
            if (remoteVideoTrackPublication.isTrackSubscribed) {
                remoteVideoTrackPublication.remoteVideoTrack?.let { removeParticipantVideo(participant, it) }
            }
        }

//        val item = remoteVideoViewList.find { it.identity == participant.identity }
//        if(item != null){
//            moveLocalVideoToPrimaryView(item.videoView)
//        }
    }

    fun addRemoteParticipantVideo(remoteParticipant: RemoteParticipant, remoteVideoTrack: RemoteVideoTrack){
        val item = remoteVideoViewList.find { it.identity == remoteParticipant.identity }
        if(item != null){

            moveLocalVideoToThumbnailView(item.videoView)
            item.videoView.mirror = false
            remoteVideoTrack.addSink(item.videoView)
        }
    }

    fun removeParticipantVideo(remoteParticipant: RemoteParticipant, remoteVideoTrack: RemoteVideoTrack){
        val item = remoteVideoViewList.find { it.identity == remoteParticipant.identity }
        if(item != null){
            remoteVideoTrack.removeSink(item.videoView)
        }
    }

    fun removeLocalVideo(){
        room?.localParticipant?.localVideoTracks?.forEach {
            it.localVideoTrack.videoCapturer.stopCapture()
            it.localVideoTrack.removeSink(localVideoView)
        }

        try { //when user reject call before connecting to room
            localVideoTrack?.videoCapturer?.stopCapture()
        }
        catch (e:Exception){e.printStackTrace()}
    }

    fun moveLocalVideoToThumbnailView(remoteVideoView: VideoView) {
        if (localVideoView.visibility == View.GONE) {
            localVideoView.visibility = View.VISIBLE
            with(localVideoTrack) {
                this?.removeSink(remoteVideoView)
                this?.addSink(localVideoView)
            }
            this.localVideoSink = localVideoView
//            localVideoView.mirror = cameraCapturerCompat.cameraSource ==
//                    CameraCapturerCompat.Source.FRONT_CAMERA
            localVideoView.mirror = false
        }
    }

    fun moveLocalVideoToPrimaryView(remoteVideoView: VideoView) {
        if (localVideoView.visibility == View.VISIBLE) {
            localVideoView.visibility = View.GONE
            with(localVideoTrack) {
                this?.removeSink(localVideoView)
                this?.addSink(remoteVideoView)
            }
            this.localVideoSink = remoteVideoView
            remoteVideoView.mirror = cameraCapturerCompat.cameraSource ==
                    CameraCapturerCompat.Source.FRONT_CAMERA
        }
    }

    fun switchCamera(){
        val cameraSource = cameraCapturerCompat.cameraSource
        cameraCapturerCompat.switchCamera()
        if (localVideoView.visibility == View.VISIBLE) {
//            localVideoView.mirror = cameraSource == CameraCapturerCompat.Source.BACK_CAMERA
            localVideoView.mirror = false
        } else {
//            remoteVideoView.mirror = cameraSource == CameraCapturerCompat.Source.BACK_CAMERA
        }
    }

    fun toggleLocalVideo(enable: Boolean){
        localVideoTrack?.let {
            muteLocalVideo(enable)
        }
    }

    fun toggleMic(){
        localAudioTrack?.let {
            val enable = !it.isEnabled
            muteLocalAudio(enable)
        }
    }

    fun muteLocalAudio(state: Boolean){
        localAudioTrack?.enable(state)
    }

    fun muteLocalVideo(state: Boolean){
        localVideoTrack?.enable(state)
    }
}