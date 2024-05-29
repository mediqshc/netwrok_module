package com.homemedics.app.twilio.call

import androidx.constraintlayout.widget.ConstraintLayout
import androidx.viewbinding.ViewBinding
import com.homemedics.app.databinding.LayoutCallParticipantBinding
import com.twilio.video.RemoteParticipant
import com.twilio.video.VideoView

class RemoteViewAndParticipant {
    var identity: String = ""
    var remoteParticipant: RemoteParticipant? = null
    lateinit var parentViewBinding: LayoutCallParticipantBinding
    lateinit var videoView: VideoView
    var userHasJoined = false
}